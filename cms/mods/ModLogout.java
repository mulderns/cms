package cms.mods;

import html.CmsElement;
import cms.DataRelay;

public class ModLogout extends Module {

	public ModLogout(DataRelay datarelay) {
		super(datarelay);
		hook = "logout";
		menu_label = "Kirjaudu ulos";
	}

	public void activate(){
		super.activate();
		actions.add(new Action(null,""){public void execute(){
			if(checkField("doit")){
				datarelay.pagebuilder.setCookie(datarelay.session.getCookie_hook(), "welcomeagain");
				datarelay.session.delete = true;
				log.info("Logged out");

				//CmsBoxi goodbye = new CmsBoxi("Uloskirjaus", "medium3");
				CmsElement goodbye = new CmsElement();
				goodbye.addLayer("div","boxi2 medium3");
				goodbye.addTag("h4","Uloskirjaus");
				goodbye.addLayer("div","ingroup filled");
				
				goodbye.addTag("p","Sinut on kirjailtu ulos");
				goodbye.addLink("Kirjaudu sis‰‰n", datarelay.script );

				page.setTitle("cms - logged out");
				page.addCenter(goodbye.toString());
				//datarelay.pagebuilder.build(page);
			}else{
				//CmsBoxi goodbye = new CmsBoxi("Uloskirjaus", "medium3");
				CmsElement goodbye = new CmsElement();
				goodbye.addLayer("div","boxi2 medium3");
				goodbye.addTag("h4","Uloskirjaus");
				goodbye.addLayer("div","ingroup filled");
				
				goodbye.addTag("p","Olet kirjautumassa ulos");
				goodbye.addFormTop(datarelay.script + "/" + hook);
				goodbye.addContent("<input type=\"submit\" value=\"kyll‰\" name=\"doit\" class=\"list\"/>");
				//goodbye.addInput("doit", "Kyll‰!", "submit", null);
				//goodbye.addTag("</form");

				page.setTitle("cms - logging out");
				page.addCenter(goodbye.toString());
				//datarelay.pagebuilder.build(page);
			}
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}

}
