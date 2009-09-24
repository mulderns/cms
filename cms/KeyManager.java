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
import d2o.UserDb;

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
		log.info("KeyManager init");
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
		if(!keydb.pol(key)){
			return false;
		}

		KeyRecord record = new KeyRecord(keydb.get(key));
		if(record.used)
			return false;
		record.used = true;
		return keydb.mod(key, record.toArray());
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
			datarelay.pagebuilder.setTitle("cms - reset password");
			datarelay.pagebuilder.addHeadTag(
					"<link type=\"text/css\" href=\""+
					datarelay.res + "login.css\" rel=\"stylesheet\"/>"
			);
			datarelay.pagebuilder.build(
					"<div class=\"content\" style=\"text-align:center;\">"+
					"<div class=\"boxi\">"+

					"<h4 style=\"background-color:#D44232\">error</h4>"+
					"<p>no valid key found</p>" +

					"</div>"+
					"</div>"
			);
			datarelay.pagebuilder.bake();
			
			ActionLog.error("mishap at the keymanager");
			return;
		}else{
			log.info("session ok");
			session = datarelay.session;

			// TODO Auto-generated method stub

			doLogin();

		}

		log.info(" but now - session.delete["+session.delete+"]");
		
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
		if(!(datarelay.cookie != null && datarelay.cookie.containsKey(cookie_hook))){
			log.info("cookie == "+(datarelay.cookie==null?"@null":"found")+", or no content");
			return false;
		}

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
			log.info("null || empty");
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
		if(record.used){
			log.info("key used");
			return false;
		}
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


		String path = datarelay.env.get("SCRIPT_NAME"); 
		if(path == null){
			log.fail("scrip_name not found in env");
			ActionLog.error("scrip_name not found in env");
			return false;
		}

		User user = loader.getUser();

		session = new Session(datarelay, cookie_hook, user);
		datarelay.session = session;
		datarelay.username = session.getUser().getName();
		datarelay.pagebuilder.setCookie(cookie_hook, session.getId(), path);

		log.info("validate key:" + useKey(key));
		return true;
	}

	public void doLogin() {
		if(datarelay.post != null && datarelay.post.containsKey("pass")){

			String pass = datarelay.post.get("pass");
			String pass2 = datarelay.post.get("pass2");

			//TODO:check for coherence;


			datarelay.pagebuilder.setTitle("cms - reset password");
			datarelay.pagebuilder.addHeadTag(
					"<link type=\"text/css\" href=\""+
					datarelay.res + "login.css\" rel=\"stylesheet\"/>"
			);
			

			UserDb udb = UserDb.getDb();
			log.info("evaluate password");
			String message = udb.evaluatePassword(datarelay.username, pass, pass2);
			if(message != null){
				datarelay.pagebuilder.build(
						"<div class=\"content\" style=\"text-align:center;\">"+
						"<div class=\"boxi\">"+

						"<h4 style=\"background-color:#D44232\">error</h4>"+
						"<p>"+message+"</p>" +

						"</div>"+
						"</div>"
				);
				
				datarelay.pagebuilder.bake();
				return;
			}
			
			
			udb.loadDb();
			log.info("change password for ["+datarelay.username+"] -> ["+pass+"]");
			if(udb.changePass(datarelay.username, pass)){
				if(udb.storeDb()==null){


					datarelay.pagebuilder.build(
							"<div class=\"content\" style=\"text-align:center;\">"+
							"<div class=\"boxi\">"+

							"<h4 style=\"background-color:#62d432\">Success!</h4>"+
							"<p>salasana vaihdettu.</p>" +
							"<a href=\""+datarelay.script+"\">&#187; kirjaudu</a>"+

							"</div>"+
							"</div>"
					);

					session.delete = true;
					log.info(" session.delete -> true");
					log.info(" "+session.delete);
					
				}else{
					datarelay.pagebuilder.build(
							"<div class=\"content\" style=\"text-align:center;\">"+
							"<div class=\"boxi\">"+

							"<h4 style=\"background-color:#D44232\">error</h4>"+
							"<p>couldn't store new password</p>" +

							"</div>"+
							"</div>"
					);
				}

			}else{
				datarelay.pagebuilder.build(
						"<div class=\"content\" style=\"text-align:center;\">"+
						"<div class=\"boxi\">"+

						"<h4 style=\"background-color:#D44232\">error</h4>"+
						"<p>couldn't change password</p>" +

						"</div>"+
						"</div>"
				);
			}

			datarelay.pagebuilder.bake();
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
				datarelay.pagebuilder.setTitle("cms - reset password");
				datarelay.pagebuilder.addHeadTag(
						"<link type=\"text/css\" href=\""+
						datarelay.res + "login.css\" rel=\"stylesheet\"/>"
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


