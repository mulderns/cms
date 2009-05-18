package cms.mods;

import java.io.File;

import html.CmsBoxi;
import html.CmsElement;
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
			etu.addLayer("div", "boxi2 medium3");
			etu.addTag("h4", null, "Messages from the admin");
			etu.addLayer("div", "ingroup filled");
			
			FlushingFile motd = new FlushingFile(new File(Cgicms.products_dir,"misc.motd"));
			
			for(String line : motd.loadAll()){
				etu.addTag("p", null, line);
			}
			etu.up(2);
			etu.addSingle("br");
			etu.addLayer("div", "boxi2 medium4");
			etu.addTag("h4", null, "Huom! - Ev‰steet");
			etu.addLayer("div", "ingroup filled");
			etu.addTag("p", null, 
					"Jos joudut kirjautumaan uudelleen joka toiminnolla,"+
					" selaimesi ev‰steet eiv‰t ole p‰‰ll‰."
			);
			etu.up(2);
			
			etu.addLayer("div", "boxi2 medium4");
			etu.addTag("h4", null, "Beta");
			etu.addLayer("div", "ingroup filled");
			etu.addTag("p", null, 
					"Palautetta ja bugi raportteja voi antaa joillakin sivuilla oikealla n‰kyvist‰" +
					" bokseista."
			);
			etu.up(2);



			//CmsBoxi esittely = new CmsBoxi("Tour de vesibajatso");
			//esittely.addParagraph("Lyhyt toimintoihin perehdytys");
			//esittely.addLink("Esittely",script+"/"+hook+"/esittely");

			page.setTitle("Etusivu");
			page.addTop(getMenu());
			page.addCenter(etu.toString());
			//page.addRight(esittely.toString());
			page.addRight(getMenuExtra());
			//page.addRight(genTalkback());
			page.addRight(genBugreport());
		}});

		actions.add(new Action(null,"esittely"){public void execute(){
			CmsBoxi esittely = new CmsBoxi("Tour de vesibajatso");
			esittely.addTag("<h3>Linkit</h3>");
			esittely.addP(
					"Vesibajatso on suunniteltu siten ett‰ eri toimintoihin p‰‰see " +
			"helposti linkkien avulla.");
			esittely.addP(
					"Voit laittaa selaimeen kirjanmerkin usein k‰ytettyyn " +
					"toimintoon ja k‰ytt‰‰ sit‰ sivuille tuloon. K‰ytt‰j‰ tunnuksen ja " +
					"salasanan j‰lkeen olet kyseisell‰ sivulla." +
					"" 
			);

			esittely.addTag("<h3>Salasanan vaihto</h3>");
			esittely.addP(
					"Salasanan saa vaihdettua 'Omat tiedot'-sivulta"
			);

			page.setTitle("Etusivu");
			page.addTop(getMenu());
			page.addCenter(esittely.toString());
			//page.addRight(genTalkback().toString());
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}


