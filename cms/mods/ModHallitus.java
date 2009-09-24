package cms.mods;

import html.CmsElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cms.DataRelay;
import d2o.UserDb;
import d2o.UserInfoRecord;

public class ModHallitus extends Module {

	public ModHallitus(DataRelay datarelay) {
		super(datarelay);
		hook = "hallitus";
		menu_label = "Hallitus";
	}

	public void activate(){
		super.activate();

		actions.add(new Action(null,""){public void execute(){
			CmsElement etu = new CmsElement();
			etu.createBox("hm", "medium3");

			page.setTitle("Etusivu");
			//page.addCenter(etu);
			page.addLeft(getActionLinks());
			pagebuilder.setRedirect(script+"/"+hook+"/"+"yhteys");
		}});

		actions.add(new Action("Yhteystiedot","yhteys"){public void execute(){
			CmsElement box = new CmsElement();
			box.createBox("Yhteystiedot");

			UserDb udb = UserDb.getDb();
			ArrayList<UserInfoRecord> infos = new ArrayList<UserInfoRecord>();
			for(String name: UserDb.getNames()){
				UserInfoRecord record = udb.getUserInfo(name);
				if(record != null){
					infos.add(record);
				}else{
					log.fail("user info not loaded for ["+name+"]");
				}
			}

			Collections.sort(infos, new Comparator<UserInfoRecord>(){
				public int compare(UserInfoRecord o1, UserInfoRecord o2) {
					if(o1.hallituksessa && !o2.hallituksessa)
						return -1;
					if(!o1.hallituksessa && o2.hallituksessa)
						return 1;
					if(!o1.toimari && o2.toimari)
						return -1;
					if(o1.toimari && !o2.toimari)
						return 1;
					
					return o1.full_name.compareTo(o2.full_name);
				}
			}); 

			box.addLayer("table","table5");
			box.addLayer("tr");
			box.addTag("th", "Nimi");
			box.addTag("th", "Titteli");
			box.addTag("th", "Puh");
			box.addTag("th", "Sähköposti");
			box.up();
			
			for(UserInfoRecord record : infos){
				box.addLayer("tr");
				box.addTag("td", record.full_name);
				box.addTag("td", record.tittle);
				box.addTag("td", record.phone);
				box.addTag("td", 
						"<a href=\"mailto:"+record.email+"\">"+record.email+"</a>");
				box.up();
			}
			box.up();
			
			page.setTitle("Yhteystiedot");
			page.addCenter(box);
			page.addLeft(getActionLinks());
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}


