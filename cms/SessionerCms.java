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
		Session session = new Session();
		if(!session.open(sID))
			return false;
		
		String ip;
		if((ip = session.readIp()) == null )
			return false;

		if(!ip.equals(datarelay.env.get("REMOTE_ADDR")))
			return false;
		
		log.info("ip ok");
		if(System.currentTimeMillis() - session.getLastAccess() > (4000000) ){
			log.info("expired");
			session.close();
			Session.remove(sID);
			return false;
		}

		log.info("la ok");
		//session = new Session();
		session.readStuff();
		session.close();
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
			datarelay.pagebuilder.addHeadTag(
					"<link type=\"text/css\" href=\""+
					datarelay.res + "login.css\" rel=\"stylesheet\"/>"
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

