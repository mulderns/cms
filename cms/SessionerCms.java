package cms;

import util.ActionLog;
import util.Hasher;
import util.Logger;
import cms.access.Session;
import cms.access.User;
import cms.access.UserLoader;

public class SessionerCms {
	Session session;
	DataRelay datarelay;
	String cookie_hook;
	Logger log;

	public SessionerCms(DataRelay datarelay) {
		this.datarelay = datarelay;
		cookie_hook = "tkrt_cms";
		this.log = datarelay.log;
		log.info("sessioner init");
	}

	public Session getSession(){
		return session;
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
		log.info("looking for login info in post");
		if(!(datarelay.post != null && datarelay.post.containsKey("name"))){
			return false;
		}

		log.info("login name found");
		String login_name = datarelay.post.get("name");
		String login_pass;
		if((login_pass = datarelay.post.get("pass")) == null){
			return false;
		}
		log.info("login pass found");
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

		if((login_pass = Hasher.hashWithSalt(login_pass, loader.getSalt()))== null ){
			return false;
		}

		if(!loader.getPass().equals(login_pass)){
			log.info("authentication failed");
			ActionLog.error("login error");
			datarelay.pagebuilder.addMessage("wrong name / pass");
			return false;
		}

		log.info("auth passed");
		//loader.loadGroups();
		//loader.loadInfo();
		User user = loader.getUser();
		
		session = new Session(datarelay, cookie_hook, user);
		datarelay.session = session;
		datarelay.username = session.getUser().getName();
		datarelay.pagebuilder.setCookie(cookie_hook, session.getId());
		return true;
	}

	public void doLogin() {
		//System.err.println("err3");
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
					"<h4>login</h4>"
			);

			String formtarget = (datarelay.script.contains("localhost")?"http":"https")+"://"+host+uri;//(datarelay.script.contains("localhost")?"":"s")+datarelay.script.substring(4)+"?flag";
			datarelay.pagebuilder.build(
					"<form method=\"post\" action=\""+ 
					formtarget +
					//(datarelay.env.containsKey("PATH_INFO") ? formtarget + datarelay.env.get("PATH_INFO") : https ) +
					"\">"+

					"<ul>"+
					"<li><label>nimi</label><input name=\"name\" type=\"text\"/></li>"+
					"<li><label>sana</label><input name=\"pass\" type=\"password\"/></li>"+
					"<li><input class=\"button\" type=\"submit\" value=\"kirjaudu\"/><input class=\"button\" type=\"reset\" value=\"tyhj‰‰\"/></li>"+
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

