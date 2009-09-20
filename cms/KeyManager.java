package cms;

import java.util.ArrayList;

import util.ActionLog;
import util.Hasher;
import util.Logger;
import util.Utils;
import cms.access.Session;
import cms.access.User;
import cms.access.UserLoader;
import d2o.FlushingDb;
import d2o.KeyRecord;

public class KeyManager {

	Session session;
	DataRelay datarelay;
	String cookie_hook;
	Logger log;

	FlushingDb keydb;
	private final String source = "keydb"; 

	public KeyManager(){
		log = new Logger("KeyManager");
		keydb = new FlushingDb(source);
	}

	public KeyManager(DataRelay datarelay) {
		this();
		this.datarelay = datarelay;
		cookie_hook = "tkrt_cms-reset";
		this.log = datarelay.log;
		log.info("sessioner init");
	}

	public String createKey(String username, long validity){
		String seed = System.nanoTime()+username;
		Utils.sleep(100);
		String key;
		do{
			key = Hasher.hashWithSalt(seed, Hasher.getSalt());
		} while(keydb.pol(key));
		KeyRecord record = new KeyRecord(username, validity, key);
		keydb.put(key, record.toArray());
		return key;
	}

	public boolean checkKey(String key){
		return keydb.pol(key);
	}

	public KeyRecord[] getKeys() {
		ArrayList<KeyRecord> records = new ArrayList<KeyRecord>();
		for(String[] info : keydb.all()){
			records.add(new KeyRecord(info));
		}
		return records.toArray(new KeyRecord[0]);
	}
	
	public boolean useKey(String key){
		//TODO: failure check
		KeyRecord record = new KeyRecord(keydb.get(key));
		record.used = true;
		keydb.mod(key, record.toArray());
		return true;
	}
	
	public boolean deleteKey(String key){
		return keydb.del(key);
	}

	public void doStuff() {
		if(
				!restoreSession() && 
				!createSession() 
		){
			//fail
			ActionLog.error("mishap at the keymanager");
			return;
		}else{
			log.info("session ok");
			session = datarelay.session;

			// TODO Auto-generated method stub

			doLogin();

		}

		if(session.delete){
			log.info("delete session");
			session.remove();
		}else{
			log.info("store session");
			session.store();
		}

	}


	public boolean restoreSession() {
		log.info("trying to restore session");
		if(!(datarelay.cookie != null && datarelay.cookie.containsKey(cookie_hook)))
			return false;

		log.info("got sesid");
		String sID = datarelay.cookie.get(cookie_hook);
		if(!Session.open(sID))
			return false;

		String ip;
		if((ip = Session.readIp()) == null )
			return false;

		if(!ip.equals(datarelay.env.get("REMOTE_ADDR")))
			return false;

		log.info("ip ok");
		if(System.currentTimeMillis() - Session.readLastAccess() > (4000000) ){
			log.info("expired");
			Session.close();
			Session.remove(sID);
			return false;
		}

		log.info("la ok");
		session = new Session();
		session.readStuff();
		Session.close();
		session.setLastAccess(System.currentTimeMillis());
		datarelay.session = session;
		datarelay.username = session.getUser().getName();
		return true;
	}

	public boolean createSession() {
		log.info("look for key in query string");
		if(!(datarelay.query != null && datarelay.query.size() > 0)){
			return false;
		}

		//TODO:

		//?af0a580ce0d908835e6ea026c78a5f3c9cd0df2b
		// 1234567891123456789212345678931234567894

		String key = datarelay.query.keySet().iterator().next();

		if(key.length() !=  40 || !keydb.pol(key)){
			//TODO: fail
			log.info("key length["+key.length()+"]!=40 || keydb.pol()"+keydb.pol(key));
			return false;
		}

		KeyRecord record = new KeyRecord(keydb.get(key));
		String login_name = record.username;
		Cgicms.group_hook="cms";
		UserLoader loader = new UserLoader(login_name);


		if(!loader.quickCheckUser()){
			log.info("quick authentication failed");
			ActionLog.error("login error");
			datarelay.pagebuilder.addMessage("wrong name / pass");
			return false;
		}

		log.info("quick passed");

		if(!loader.load()){
			log.info("authentication failed");
			ActionLog.error("login error");
			datarelay.pagebuilder.addMessage("wrong name / pass");
			return false;
		}


		User user = loader.getUser();

		session = new Session(datarelay, cookie_hook, user);
		datarelay.session = session;
		datarelay.username = session.getUser().getName();
		datarelay.pagebuilder.setCookie(cookie_hook, session.getId());
		return true;
	}

	public void doLogin() {
		if(datarelay.post != null && datarelay.post.containsKey("name")){

			datarelay.post.get("pass");
			datarelay.post.get("pass2");

		}else{


			String https = datarelay.env.get("HTTPS");
			String uri = datarelay.env.get("REQUEST_URI");
			if(uri == null){
				if(datarelay.env.containsKey("PATH_INFO")){
					uri = datarelay.env.get("PATH_INFO");
				}else{
					uri = "";
				}
			}
			String host = datarelay.env.get("HTTP_HOST");
			if(host == null){
				host = datarelay.script.substring(4);
			}
			if(!datarelay.script.contains("localhost") &&
					https == null ){
				StringBuilder redir = new StringBuilder();
				redir.append("https://"+host+uri);
				datarelay.pagebuilder.setRedirect(redir.toString());
				datarelay.pagebuilder.bake();
			}else{
				datarelay.pagebuilder.setTitle("cms - login");
				datarelay.pagebuilder.addStyle(
						"body{"+
						"font-family: verdana;"+
						"font-size: small;"+
						"min-height:300px;"+
						"background-color:white;"+
						"background-position:center top;"+
						"color:black;"+
						"margin:0px;"+
						"}"+

						"div.boxi a{"+
						"border: 0px solid orange;"+
						"color: black;"+
						"background-color:#dddddd;"+
						"font-size:x-small;"+
						"text-transform:uppercase;"+
						"text-decoration:none;"+
						"}"+

						"div.boxi a:visited{"+
						"color:grey;"+
						"}"+

						"div.boxi a:hover{"+
						"color:white;"+
						"background-color:orange;"+
						"}"+

						"div.boxi {"+
						"border-width: 1px;"+
						"border-style: solid;"+
						"}"+

						"div.boxi {"+
						"width:200px;"+
						"margin: 8% auto;"+
						"text-align:left;"+
						"padding:2px;"+
						"background-color:white;"+
						"}"+

						"div.boxi h4{"+
						"background-color:orange;"+
						"color:white;"+
						"font-size: small;"+
						"text-align:left;"+
						"padding:4px 8px;"+
						"margin:0px;"+
						"}"+

						"div.boxi p{"+
						"font-size:x-small;"+
						"margin: 6px 7px;"+
						"}"+

						"div.boxi a{"+
						"padding:3px 10px;"+
						"margin: 5px 7px;"+
						"margin-top:0px;"+
						"text-align:center;"+
						"display:block;"+
						"}"+

						"div.boxi2 a {"+
						"display:inline;"+
						"margin: 2px;"+
						"}"+

						"h2{"+
						"border-bottom-width:1px;"+
						"border-bottom-style:solid;"+
						"padding-top:20px;"+
						"padding-left:10px;"+
						"margin:05px;"+
						"margin-bottom:0px;"+
						"}"+

						"div.boxi ul{"+
						"	padding:0px;"+
						"	margin:4px;"+
						"	list-style:none;"+
						"	line-height:25px;"+
						"	text-align:right;"+
						"}"+

						"label {"+
						"	font-size:x-small;"+
						"	margin-right:6px;"+
						"}"+

						"input{"+
						"	border: 1px solid black;"+
						"	padding:1px 4px;"+
						"	font-family:monospace;"+
						"	font-size:12px;"+
						"}"+

						"input.button{"+
						"padding:1px 6px;"+
						"margin: 2px 4px;"+
						"margin-right: 0px;"+

						"text-align:center;"+

						"background-color: #eee;"+
						"font-family:verdana;"+
						"font-size:10.5px;"+
						"}"+

						"input.button:hover{"+
						"background-color: #fff8ee;"+
						"}"+

						"form {"+
						"margin: 0px;"+
						"}"
				);

				datarelay.pagebuilder.build(
						"<div class=\"content\" style=\"text-align:center;\">"+
						"<div class=\"boxi\">"+
						"<h4>uusi salasana</h4>"
				);

				String formtarget = (datarelay.script.contains("localhost")?"http":"https")+"://"+host+uri;//(datarelay.script.contains("localhost")?"":"s")+datarelay.script.substring(4)+"?flag";
				datarelay.pagebuilder.build(
						"<form method=\"post\" action=\""+ 
						formtarget +
						//(datarelay.env.containsKey("PATH_INFO") ? formtarget + datarelay.env.get("PATH_INFO") : https ) +
						"\">"+

						"<ul>"+
						"<li><label>nimi</label><input name=\"pass\" disabled=\"disabled\" type=\"text\" value=\""+session.getUser().getName()+"\"/></li>"+
						"<li><label>sana</label><input name=\"pass\" type=\"password\"/></li>"+
						"<li><label>sama</label><input name=\"pass2\" type=\"password\"/></li>"+
						"<li><input class=\"button\" type=\"submit\" value=\"tallenna\"/><!--<input class=\"button\" type=\"reset\" value=\"tyhj‰‰\"/>--></li>"+
						"</ul>"+
						"</form>"+
						"</div>"+

						"<div class=\"boxi\">"+
						//"<h4>Ev‰steet</h4>" +
						"<p>Jos joudut kirjautumaan uudelleen joka toiminnolla, selaimesi ev‰steet eiv‰t ole p‰‰ll‰.</p>" +
						"</div>"+

						"</div>"
				);
				datarelay.pagebuilder.bake();
			}
		}
	}


}


