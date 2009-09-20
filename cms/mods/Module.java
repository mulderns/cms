package cms.mods;

import html.CmsElement;
import html.CmsPage;
import html.TextAreaField;

import java.io.File;
import java.util.ArrayList;

import util.ActionLog;
import util.Logger;
import util.Utils;
import cms.Cgicms;
import cms.DataRelay;
import cms.Mailer;
import cms.PageBuilder;
import d2o.FlushingFile;
import d2o.GroupDb;

public class Module {
	protected Logger log;
	protected String hook;
	protected String menu_label;

	protected String script;
	protected String username;

	protected PageBuilder pagebuilder;

	protected DataRelay datarelay;
	protected ArrayList<Action> actions;

	protected String mod,act,ext;

	protected CmsPage page;

	protected class Action{
		String label;
		String action_hook;
		ArrayList<String> fields;

		public Action(String label, String hook) {
			this.label = label;
			this.action_hook = hook;
			fields = new ArrayList<String>();
		}

		public void addField(String fieldname){
			fields.add(fieldname);
		}

		public String[] getFields(){
			return fields.toArray(new String[fields.size()]);
		}

		void execute(){
			CmsElement noImplementionBox = new CmsElement();
			noImplementionBox.addLayer("div","boxi2 medium3");
			noImplementionBox.addTag("h4", mod + "/" + act);
			noImplementionBox.addLayer("div","ingroup filled");
			noImplementionBox.addTag("p", "Virheellinen toiminto ["+act+"]");

			page.setTitle("blank");

			page.addTop(getMenu());
			page.addCenter(noImplementionBox);
			page.addCenter(getActionLinks());

		}
	}

	public void activate(){
		actions = new ArrayList<Action>();
		log = new Logger(hook);
		script = datarelay.script;
		username = datarelay.username;
	}

	protected Module(String hook, DataRelay datarelay){
		this.hook = hook;
		this.datarelay = datarelay;
	}

	protected Module(DataRelay datarelay){
		//this.hook = hook;
		this.datarelay = datarelay;
	}
	
	public String getHook(){
		return hook;
	}

	protected String getMenu_old(){
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"menuu\">\n");
		sb.append("<h4>toiminnot</h4>\n");
		//log.info("modules in loader: "+(datarelay.modules == null?"null":datarelay.modules.size()));
		//long toomuch = System.nanoTime();

		String key;

		for(Module m : datarelay.modules){
			key = m.getHook();
			if(GroupDb.checkAccess(datarelay.session.getUser(), key ,"")){
				sb.append(
						"<a href=\"" + script + "/" + key + "\">" +
						//m.menu_label +
						m.menu_label +
						"</a>\n"
				);
			}
		}
		//pagebuilder.addHidden(" toomuch = "+(System.nanoTime() - toomuch)+" ns"); ;
		sb.append("</div>\n");
		return sb.toString();
	}
	
	protected CmsElement getMenu(){
		CmsElement menu = new CmsElement();
		//StringBuilder sb = new StringBuilder();
		menu.addLayer("div", "menuu3");
		menu.addLayer("div style=\"float:right\"");
		if(GroupDb.checkAccess(datarelay.session.getUser(), "oma" ,"")){
			menu.addTag("a href=\"" + script + "/oma\" class=\"sprited gear\"","menu","Oma");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "logout" ,"")){
			menu.addTag("a href=\"" + script + "/logout\" class=\"sprited exit\"","menu","Lopeta");
		}
		menu.up();
		
		menu.addLayer("div", "center2");
		if(GroupDb.checkAccess(datarelay.session.getUser(), "etusivu" ,"")){
			menu.addTag("a href=\"" + script + "/etusivu\" class=\"sprited star\"","Etusivu");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "viikko" ,"")){
			menu.addTag("a href=\"" + script + "/viikko\" class=\"sprited calendar\"","Tapahtumat");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "sivut" ,"")){
			menu.addTag("a href=\"" + script + "/sivut\" class=\"sprited planet\"","Sivuston hallinta");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "tiedostot" ,"")){
			menu.addTag("a href=\"" + script + "/tiedostot\" class=\"sprited file\"","Tiedostot");
		}
		
		return menu;
	}
	
	protected String getMenuExtra(){
		StringBuilder sb = new StringBuilder();
		if(GroupDb.checkAccess(datarelay.session.getUser(), "hallitus" ,"")){
			sb.append("<a class=\"menu\" href=\"" + script + "/hallitus\">Hallitus</a>\n");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "oikeudet" ,"")){
			sb.append("<a class=\"menu\" href=\"" + script + "/oikeudet\">Oikeuksien hallinta</a>\n");
		}
		if(GroupDb.checkAccess(datarelay.session.getUser(), "maintenance" ,"")){
			sb.append("<a class=\"menu\" href=\"" + script + "/yllapito\">Ylläpito</a>\n");
		}

		return sb.toString();
	}

	protected String getField(String fieldname) {
		return datarelay.post.get(fieldname);
	}

	protected boolean hasField(String fieldname) {
		return datarelay.post.containsKey(fieldname);
	}

	protected boolean hasInput() {
		return datarelay.post != null;
	}

	protected boolean checkFields(String[] fields) {
		log.info("checking fields");
		if(datarelay.post == null){
			log.info("post still null");
			return false;
		}
		for(String field : fields){
			log.info("f["+field+"]");
			if(field == null){
				log.info("@");
				continue;
			}
			if(!datarelay.post.containsKey(field)){
				log.info("post does not contain field: "+field);
				log.info(datarelay.post.toString());
				return false;
			}
		}
		return true;
	}

	protected boolean checkField(String field) {
		if(datarelay.post == null){
			log.info("post still null");
			return false;
		}

		if(!datarelay.post.containsKey(field)){
			log.info("post does not contain field: "+field);
			log.info(datarelay.post.toString());
			return false;
		}

		return true;
	}

	public void execute(){
		pagebuilder = datarelay.pagebuilder;
		page = datarelay.page;
		
		page.addTop(getMenu());
		page.addRight(getMenuExtra());
		page.addRight(genBugreport());
		
		mod = datarelay.mod;
		act = datarelay.act;
		ext = datarelay.ext;
		
		ActionLog.action("["+username+"] "+mod+"/"+act+" ["+ext+"]");
		
		log.info(mod+"/"+act+" ["+ext+"]");
		log.info(hook + " executing");
//		checkTalk();
		checkBug();

		for(Action action : actions){
			if(action!=null){
				if(action.action_hook.equals(act)){
					action.execute();
					pagebuilder.build(page);
					return;
				}
			}
		}

		new Action(null,act).execute();
		pagebuilder.build(page);
	}

	protected CmsElement getActionLinks() {
		CmsElement toiminnot = new CmsElement();
		for( Action action : actions){
			if(action != null){
				if(action.label != null)
					if(GroupDb.checkAccess(datarelay.session.getUser(), hook ,action.action_hook))
						//toiminnot.addTag("a href=\""+script+"/"+hook+"/"+action.action_hook+"\"", action.label);
						toiminnot.addLink(null, "menu", script+"/"+hook+"/"+action.action_hook, action.label);
			}else{
				toiminnot.addSingle("br");
			}
		}
		return toiminnot;
	}

	public String[] getActions(){
		String[] temp = new String[actions.size()];
		int i = 0;
		for(Action action : actions){
			if(action != null)
				temp[i++] = action.action_hook;
		}
		return temp;
	}

	protected void checkTalk(){
		if(hasInput()){
			if(hasField("sanoma")){
				log.info("has talkback");
				if(
						datarelay.session.getUser().getGroups().contains("root") && 
						getField("sanoma").equals("clear")
				){
					log.info("clearing talk");
					clearTalk();

				}else{
					log.info("storing talk");
					storeTalk(getField("sanoma"));
				}
			}
		}
	}

	private void clearTalk() {
		FlushingFile talkfile = new FlushingFile(new File(Cgicms.products_dir,genTalkBackFilename()));
		talkfile.delete();

	}

	protected void storeTalk(String field) {
		if(field.trim().length() > 0){
			FlushingFile talkfile = new FlushingFile(new File(Cgicms.products_dir,genTalkBackFilename()));

			field = Utils.deNormalize(field);
			talkfile.append(datarelay.session.getUser().getName()+" &#62; "+field);
		}else{
			log.fail("talking back failed");
		}
	}

	protected CmsElement genBugreport() {
		CmsElement tools = new CmsElement();
		tools.addFormTop(script+"/"+hook);
	
		tools.addLayer("div", "sidebar");
		tools.addTag("h4", "Palaute");
		tools.addField("bugibugi",null,false,new TextAreaField(-1,4));
		tools.addSingle("input type=\"submit\" value=\"sano\" class=\"list\"" );
		tools.addSingle("input type=\"hidden\" name=\"location\" value=\""+mod+"/"+act+"/"+ext+"\" style=\"display:none;\"" );
		return tools;
	}

	protected void checkBug(){
		if(hasInput()){
			if(hasField("bugibugi")){
				log.info("has bugs");
				storeBug(getField("bugibugi"),getField("location"));
			}
		}
	}

	protected void storeBug(String description, String location) {
		if(location == null)
			location = "no location";
		if(description.trim().length() > 0){
			Mailer.sendMail("Cms", "valtteri.kortesmaa@tut.fi", "[Cms] bug", "["+location+"]"+username+":"+description);
		}else{
			log.fail("storing bug failed");
			ActionLog.error(username+" - storing bug failed");
		}
	}

	protected CmsElement genTalkback() {
		FlushingFile talkfile = new FlushingFile(new File(Cgicms.products_dir, genTalkBackFilename()));

		String[] talk = talkfile.loadAll();

		CmsElement tools = new CmsElement();
		tools.addFormTop(script+"/"+hook);
		tools.addLayer("div", "sidebar");
		tools.addTag("h4", "Pulina");
		
		tools.addLayer("div","pulina");
			for(String s : talk){
			tools.addTag("p",s);
		}
		tools.up();
		tools.addLayer("table");
		tools.addLayer("tr");
		

		
		tools.addLayer("td");
		tools.addContent("<input type=\"text\" name=\"sanoma\" size=\"20\"/>");
		tools.up();
		tools.addLayer("td");
		tools.addContent("<input class=\"list\" type=\"submit\" value=\"sano\" name=\"submit\"/>");
		tools.up();
		
		return tools;
	}

	private String genTalkBackFilename() {
		return "talkback."+ hook + (act.length() > 0 ? "_" + act : "") ;//+ ".talkback";
	}

}

