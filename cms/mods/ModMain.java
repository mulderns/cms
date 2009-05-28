package cms.mods;

import html.CmsElement;

import java.io.File;

import cms.Cgicms;
import cms.DataRelay;
import d2o.FlushingFile;

public class ModMain extends Module {

	public ModMain(DataRelay datarelay) {
		super(datarelay);
		hook = "etusivu";
		menu_label = "Etusivu";
	}

	public void activate(){
		super.activate();
		actions.add(new Action(null,""){public void execute(){
			
			CmsElement etu = new CmsElement();
			etu.createBox("Messages from the admin", "medium3");
			
			FlushingFile motd = new FlushingFile(new File(Cgicms.products_dir,"misc.motd"));
			
			for(String line : motd.loadAll()){
				etu.addTag("p", null, line);
			}
			etu.up(2);
			etu.addSingle("br");
			etu.createBox("Huom! - Ev‰steet", "medium4");
			etu.addTag("p", null, 
					"Jos joudut kirjautumaan uudelleen joka toiminnolla,"+
					" selaimesi ev‰steet eiv‰t ole p‰‰ll‰."
			);
			etu.up(2);
			
			etu.createBox("Beta","medium4");
			etu.addTag("p", null, 
					"Palautetta ja bugi raportteja voi antaa joillakin sivuilla oikealla n‰kyvist‰" +
					" bokseista."
			);
			etu.up(2);

			page.setTitle("Etusivu");
			page.addCenter(etu);

		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}


