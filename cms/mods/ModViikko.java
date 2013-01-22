package cms.mods;

import html.CheckBoxField;
import html.CmsElement;
import html.TextAreaField;
import html.TextField;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import util.ActionLog;
import util.Utils;
import cms.DataRelay;
import cms.FileOps;
import cms.Mailer;
import d2o.ViikkoDb;
import d2o.ViikkoEntry;

public class ModViikko extends Module {
	private CmsElement forma;
	private CmsElement help;

	public ModViikko(DataRelay datarelay) {
		super(datarelay);
		hook = "viikko";
		menu_label = "Viikko-ohjelma";
	}

	public void activate(){
		super.activate();

		forma = new CmsElement();
		forma.createBox("Tiivistelm‰", "medium3");
		forma.addLayer("table", "table5");
		forma.addSingle("colgroup width=\"70\"");
		forma.addSingle("colgroup");
		forma.addLayer("tr");
		forma.addLayer("td style=\"text-align:right;\"");
		forma.addTag("label","<b>Otsikko</b>:");
		forma.up();
		forma.addLayer("td");
		forma.addField("otsikko", null, true, new TextField(22));
		forma.up(2);


		forma.addLayer("tr");
		forma.addTag("td style=\"text-align:right;\"", "<label><b>P‰iv‰</b>:</label>");
		forma.addLayer("td");
		forma.addField("paiva", null, true, new TextField(2));
		forma.addContent(" ");
		forma.addField("kuu", Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1), true, new TextField(2));
		forma.addContent(" ");
		forma.addField("vuosi", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), true, new TextField(4));
		forma.up(2);

		forma.addLayer("tr");
		forma.addTag("td style=\"text-align:right;\"", "<label>Kello:</label>");
		forma.addLayer("td");
		forma.addField("aika", "00:00", false, new TextField(4));
		forma.up(2);

		forma.addLayer("tr");
		forma.addTag("td style=\"text-align:right;\"", "<label>Paikka:</label>");
		forma.addLayer("td");
		forma.addField("paikka", null, false, new TextField(22));
		forma.up(2);

		forma.addLayer("tr");
		forma.addTag("td style=\"text-align:right;text-align: right; vertical-align: top;\"", "<label>Lis‰tiedot:</label>");
		forma.addLayer("td");
		forma.addField("lisa","", false, new TextAreaField(20,4));
		forma.up(2);

		forma.up(); //table

		forma.addSingle("input value=\"Tallenna\" type=\"submit\" class=\"list\" style=\"cursor:pointer;text-align:center;\"");

		forma.up(); //ingroup
		forma.up(); //boxi

		forma.addLayer("div","boxi2 medium4");
		forma.addContent("<h4 style=\"vertical-align:bottom;\" class=\"nohi black\">Lis‰‰ viesti viikkopostiin:");
		forma.addField("mailiin", null, false, new CheckBoxField());
		forma.addContent("</h4>");
		forma.addLayer("div", "ingroup filled");

		forma.addLayer("table", "table5");
		forma.addSingle("colgroup width=\"70\"");
		forma.addSingle("colgroup");
		forma.addLayer("tr");
		forma.addLayer("td colspan=\"2\"");
		forma.addField("posti", null, false, new TextAreaField(-1,8));

		forma.up(); //td
		forma.up(); //tr
		forma.up(); //table
		forma.up(); //ingroup
		forma.up(); //boxi


		forma.addSingle("br");

		forma.addLayer("div","boxi2 medium3");
		forma.addTag("h4","nohi","Lis‰asetukset");
		forma.addLayer("table", "table5");
		forma.addSingle("colgroup width=\"33\"");
		forma.addSingle("colgroup width=\"49\"");
		forma.addSingle("colgroup width=\"184\"");

		forma.addLayer("tr");
		forma.addTag("td","<label>Iso:</label>");
		forma.addLayer("td");
		forma.addField("etukateen", null, false, new CheckBoxField());
		forma.up();
		forma.addTag("td","<label>Automaattisesti toistuva:</label>");
		forma.addLayer("td");
		forma.addField("auto", null, false, new CheckBoxField());

		help = new CmsElement();
		help.addLayer("dl","help");
		help.addTag("h4", "Ohjeita");
		help.addTag("p", "Ensimm‰inen laatikko ker‰‰ tapahtumasta tiivistetyt perustiedot, "+
				"jotka n‰kyv‰t sivuilla olevassa viikko-ohjelmassa, sek‰ " +
		"viikkopostissa jos n‰in halutaan.");
		help.addTag("p","Toisessa laatikossa oleva viesti tulee pelk‰st‰‰n viikkopostiin "+
		"ja voi olla pidempi ja yksityis kohtaisempi.");
		help.addTag("h4", "Kent‰t");

		help.addTag("dt", "Otsikko");
		help.addTag("dd","<b>Pakollinen kentt‰.</b>");
		help.addTag("dd","Tapahtumaa kuvaava yleisotsikko.");
		help.addTag("dd","esim. kappeli-ilta, lenkki, kahvitus.");

		help.addTag("dt", "P‰iv‰");
		help.addTag("dd","<b>Pakollinen kentt‰.</b>");
		help.addTag("dd","Ekaan laatikkoon p‰iv‰, tokaan kuu ja kolmanteen vuosi.");
		help.addTag("dd","Tapahtumia ei voi lis‰t‰ menneisyyteen.");

		help.addTag("dt", "Kello");
		help.addTag("dd","Jos tapahtumalle ei halua kellon aikaa, kelpaa tyhj‰ kentt‰ tai 00:00");

		help.addTag("dt", "Paikka");
		help.addTag("dd","Miss‰ tapahtuu.");

		help.addTag("dt", "Lis‰tiedot");
		help.addTag("dd","Muita huomioon otettavia tietoja tiivistettyn‰.");
		help.addTag("dd","esim. kappeli-illan aihe / puhuja.");

		help.addTag("dt", "Lis‰‰ viesti viikkopostiin");
		help.addTag("dd","Rasti t‰h‰n jos haluat tapahtuman n‰kyv‰n viikkopostissa.");
		help.addTag("dd","Alla olevaan laatikkoon voi kirjoittaa yksityiskohtaisen viestin joka n‰kyy vain viikkopostissa");
		help.addTag("dt", "Iso");
		help.addTag("dd","Jos tapahtumasta tulisi ilmoittaa jo paria viikkoa etuk‰teen "+
		"laita rasti t‰h‰n kohtaan.");
		help.addTag("dd","Tapahtuma n‰kyy sek‰ sivuilla ett‰ viikko postissa(jos valittu) alkaen kahta viikkoa ennen tapahtuman ajankohtaa ");
		help.addTag("dt", "Automaattisesti toistuva");
		help.addTag("dd","Tapahtuma lis‰t‰‰n viikko-ohjelmaan ja postiin(jos valittu) automaattisesti joka viikko.");

		actions.add(new Action(null, ""){public void execute(){
			CmsElement tap = new CmsElement();

			ViikkoDb db = new ViikkoDb();
			db.checkDb();
			int last_week = 999;
			int current_week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)+datarelay.week_fix;

			for(ViikkoEntry ve : db.getUserEntries("all")){
				if(ve.getWeek() != last_week){
					tap.up(3);
					last_week = ve.getWeek();

					tap.addLayer("div", "boxi2 medium3");
					tap.addTag("h4", (last_week==current_week?null:(last_week==(current_week+1)?"nohi black":"nohi")), "Vko "+ve.getWeek());
					tap.addLayer("div", "ingroup filled");
					tap.addLayer("table", "table5");
					tap.addSingle("colgroup width=\"70\"");
					tap.addSingle("colgroup");
					tap.addSingle("colgroup width=\"27\"");
				}

				tap.addLayer("tr");
				tap.addTag("td", null, ve.getDayName()+" "+ ve.day+"."+ve.month+".");
				tap.addTag("td", null, "<a title=\"muokkaa\" class=\"but\" href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">"+ve.otsikko+"</a>");
				tap.addTag("td", null,"<a title=\"poista\" class=\"but\" href=\""+script+"/"+hook+"/poista/"+ve.id +"\">X</a>");
				tap.up();
			}
			tap.up(3);

			tap.addSingle("br");

			tap.addLayer("div", "boxi2 medium3");

			tap.addTag("h4", null, "Automaattiset");
			tap.addLayer("div", "ingroup filled");
			tap.addLayer("table", "table5");
			tap.addSingle("colgroup width=\"33\"");
			tap.addSingle("colgroup width=\"27\"");
			tap.addSingle("colgroup");
			tap.addSingle("colgroup width=\"27\"");

			for(ViikkoEntry ve : db.getUserEntries("auto")){
				tap.addLayer("tr");
				tap.addTag("td", null, ve.getDayName());
				tap.addTag("td", null, "<a title=\""+(ve.enabled?"disable":"enable")+"\" class=\"but\" href=\""+script+"/"+hook+"/toggle/"+ve.id +
						"\">"+(ve.enabled?"-":"+")+"</a>");
				tap.addTag("td", null, "<a title=\"muokkaa\" class=\"but"+(ve.enabled?"":" unselected")+"\" href=\""+script+"/"+hook+"/muokkaa/"+ve.id+
						"\">"+ve.otsikko+"</a>");
				tap.addTag("td", null,"<a title=\"poista\" class=\"but\" href=\""+script+"/"+hook+"/poista/"+ve.id+"\">X</a>");
				tap.up();
			}
			tap.up(3);


			page.setTitle("Tapahtumat");
			page.addCenter(tap);
			page.addLeft(getActionLinks());

		}});


		actions.add(new Action(null, "toggle"){public void execute(){
			if(!ext.equals("")){
				ViikkoDb db = new ViikkoDb();
				ViikkoEntry ve = db.loadEntry(ext);
				if(ve != null){
					ActionLog.action("Toggle["+ext+"]");
					ve.setEnabled(ve.enabled ? false : true);
					db.removeEntry(ext);
					db.addEntry(ve);
					pagebuilder.setRedirect(script + "/" +hook+"/");
				}
			}
		}});

		actions.add(new Action("Lis‰‰ tapahtuma", "lisaa"){public void execute(){
			log.info("/lisaa");

			if(checkFields(forma.getFields())){
				log.info(" fields ok");
				ArrayList<String> results = new ArrayList<String>();
				String res = "";

				if(!datarelay.post.containsKey("otsikko")){
					results.add("Tapahtumalla ei ole otsikkoa");
				}else{
					String hmm = datarelay.post.get("otsikko");
					if(hmm.length()<1){
						results.add("Tapahtumalla ei ole otsikkoa");
					}
				}
				ViikkoEntry temp = new ViikkoEntry(datarelay.post.get("otsikko"), username);


				if(datarelay.post.containsKey("auto")){
					temp.setAuto(true);
				}

				if((res = temp.setVuosi(datarelay.post.get("vuosi"))) != null)
					results.add(res);
				if((res = temp.setKuukausi(datarelay.post.get("kuu"))) != null)
					results.add(res);
				if((res = temp.setPaiva(datarelay.post.get("paiva"))) != null)
					results.add(res);
				if(datarelay.post.containsKey("aika"))
					if((res = temp.setAika(datarelay.post.get("aika"))) != null)
						results.add(res);

				if(datarelay.post.containsKey("lisa")){
					if((res = temp.setLisa(datarelay.post.get("lisa"))) != null){
						results.add(res);
					}
				}

				if(datarelay.post.containsKey("posti")){
					if((res = temp.setPosti(datarelay.post.get("posti"))) != null){
						results.add(res);
					}else{
						int charcount;
						if((charcount = temp.getTekstiCharCount()) > 1000 ){
							results.add("Tekstin maksimipituus 1000 merkki‰, t‰m‰n hetkinen ["+charcount+"]");
						}
					}
				}

				if(datarelay.post.containsKey("paikka"))
					temp.setPaikka(datarelay.post.get("paikka"));

				if(datarelay.post.containsKey("mailiin"))
					temp.setMailiin(true);

				if(datarelay.post.containsKey("etukateen"))
					temp.setEtukateen(true);
				ViikkoDb vdb = new ViikkoDb();
				if(results.size() == 0){
					if((res = vdb.addEntry(temp)) != null)
						results.add(res);
				}

				if(results.size() == 0){
					String prev = genPreview(temp);
					if(!temp.auto && datarelay.post.containsKey("pohjaksi")){
						temp.setPohjaksi(true);
						vdb.addEntry(temp);
					}
					ActionLog.action(username + " - added happening ["+datarelay.post.get("otsikko")+"]");

					CmsElement result = new CmsElement();
					result.createBox("Tapahtuman lis‰ys onnistui", "medium3");
					result.addTag("a href=\""+script + "/" + hook+"\"","list", "Ok");
					result.addTag("a href=\""+script + "/" + hook + "/" + action_hook+"\"","list", "Lis‰‰ lis‰‰");
					result.up(2);
					result.addSingle("br");
					result.addContent(prev);

					page.setTitle("Lis‰‰ tapahtuma");
					page.addCenter(result);
					page.addCenter(prev);

				}else{
					ActionLog.action(username + " - failed to add happening ["+datarelay.post.get("otsikko")+"]");
					CmsElement result = new CmsElement();
					result.createBox("Virhe", "medium3");
					result.addTag("p","Tapahtuman lis‰ys ep‰onnistui:");
					result.addContent("<pre style=\"font-size:12.5px\">");
					for(String s : results){
						result.addContent(chop(s,35)+"\n\n");
					}
					result.addContent("</pre>");
					forma.setFields(datarelay.post);

					page.setTitle("Lis‰‰ tapahtuma");
					page.addLeft(getActionLinks());
					page.addCenter(result);
					forma.addFormTop(script+"/"+hook+"/"+action_hook);
					page.addCenter(forma);

				}
			}else{
				log.info("fields not found");
				page.setTitle("Lis‰‰ tapahtuma");

				forma.addFormTop(script+"/"+hook+"/"+action_hook);

				page.addCenter(forma);
				page.addLeft(help);
				page.addRight(genMonth(0));
				page.addRight(genMonth(1));


			}
		}});

		actions.add(null);

		actions.add(new Action(null, "muokkaa"){public void execute(){
			log.info("/muokkaa ["+ext+"]");
			if(ext != ""){
				if(checkFields(forma.getFields())){
					log.info(" required fields found");
					ArrayList<String> results = new ArrayList<String>();
					if(!datarelay.post.containsKey("otsikko")){
						results.add("Tapahtumalla ei ole otsikkoa");
					}else{
						String hmm = datarelay.post.get("otsikko");
						if(hmm.length()<1){
							results.add("Tapahtumalla ei ole otsikkoa");
						}
					}

					ViikkoEntry temp = new ViikkoEntry(datarelay.post.get("otsikko"), username);
					String res = "";

					if(datarelay.post.containsKey("auto")){
						temp.setAuto(true);
					}else if(datarelay.post.containsKey("pohjaksi")){
						temp.setPohjaksi(true);
					}

					if((res = temp.setVuosi(datarelay.post.get("vuosi"))) != null)
						results.add(res);
					if((res = temp.setKuukausi(datarelay.post.get("kuu"))) != null)
						results.add(res);
					if((res = temp.setPaiva(datarelay.post.get("paiva"))) != null)
						results.add(res);
					if(datarelay.post.containsKey("aika"))
						if((res = temp.setAika(datarelay.post.get("aika"))) != null)
							results.add(res);
					if(datarelay.post.containsKey("lisa")){
						if((res = temp.setLisa(datarelay.post.get("lisa"))) != null){
							results.add(res);
						}
					}

					if(datarelay.post.containsKey("posti")){
						if((res = temp.setPosti(datarelay.post.get("posti"))) != null){
							results.add(res);
						}else{
							int charcount;
							if((charcount = temp.getTekstiCharCount()) > 1000 ){
								results.add("Tekstin maksimipituus 1000 merkki‰, t‰m‰n hetkinen ["+charcount+"]");
							}
						}
					}

					if(datarelay.post.containsKey("paikka"))
						temp.setPaikka(datarelay.post.get("paikka"));

					if(datarelay.post.containsKey("mailiin"))
						temp.setMailiin(true);

					if(datarelay.post.containsKey("etukateen"))
						temp.setEtukateen(true);

					if(datarelay.post.containsKey("pohjaksi"))
						temp.setPohjaksi(true);

					if(datarelay.post.containsKey("auto"))
						temp.setAuto(true);

					ViikkoDb vdb = new ViikkoDb();

					if(results.size() == 0){
						if(temp.auto){
							ViikkoEntry autocheck = vdb.loadEntry(ext);
							if(autocheck != null && autocheck.enabled){
								temp.setEnabled(true);
							}
						}

						if((res = vdb.removeEntry(ext))!=null){
							results.add("error removing the old entry:"+res);
							log.fail("couldnt remove ["+ext+"]");
						}else{
							if((res = vdb.addEntry(temp)) != null){
								results.add(res);
							}
						}
					}
					/** alku */
					log.info(" errors["+results.size()+"]");
					if(results.size() == 0){
						ActionLog.action(username + " - modified happening ["+datarelay.post.get("otsikko")+"]");
						String prev = genPreview(temp);

						CmsElement result = new CmsElement();
						result.createBox("Tapahtuman muokkaus onnistui", "medium3");

						result.addTag("a href=\""+script + "/" + hook+"\"","list", "Ok");
						result.addTag("a href=\""+script + "/" + hook +"/esikatsele"+"\"","list", "Esikatseluun");
						result.up(2);
						result.addSingle("br");
						result.addContent(prev);

						page.setTitle("Muokkaa tapahtumaa");
						page.addCenter(result);

					}else{
						ActionLog.action(username + " - failed to modify happening ["+datarelay.post.get("otsikko")+"]");

						CmsElement result = new CmsElement();
						result.createBox("Tapahtuman muokkaus ep‰onnistui", "medium3");
						result.addTag("p","sill‰: ");
						result.addLayer("pre");
						for(String s : results){
							result.addContent(s+"\n");
						}
						result.up();
						forma.setFields(datarelay.post);
						forma.addFormTop(script + "/" + hook + "/muokkaa/"+ext);

						result.addLink("muut toiminnot", script + "/" + hook );
						page.setTitle("Lis‰‰ tapahtuma");

						page.addCenter(result);
						page.addCenter(forma);

					}

				}else{
					ViikkoDb db = new ViikkoDb();
					ViikkoEntry ve = db.loadEntry(ext);	


					if(ve != null){
						{
							HashMap<String, String> temp = new HashMap<String, String>(11);
							temp.put("otsikko", ve.otsikko);
							temp.put("auto", (ve.auto? "on":"off"));
							if(ve.auto){
								ve.genAutoWeek(1);
							}
							temp.put("kuu", Integer.toString(ve.month));
							temp.put("paiva", Integer.toString(ve.day));

							temp.put("vuosi", Integer.toString(ve.year));
							temp.put("aika", ve.hour+":"+Utils.addLeading(ve.minute,2));
							temp.put("paikka", ve.paikka);

							{
								StringBuilder sb = new StringBuilder();
								for(String s:ve.yhteenveto){
									sb.append(s+"\n");
								}
								temp.put("lisa", sb.toString());
								sb = new StringBuilder();
								for(String s:ve.teksti){
									sb.append(s+"\n");
								}
								temp.put("posti", sb.toString());
							}

							temp.put("mailiin", (ve.mailiin? "on":"off"));
							temp.put("etukateen", (ve.etukateen? "on":"off"));
							temp.put("pohjaksi", (ve.pohjaksi? "on":"off"));

							forma.setFields(temp);
							forma.addFormTop(script + "/" + hook + "/muokkaa/"+ext);
						}
						page.addCenter(forma.toString());
						page.addLeft(help);
					}else{
						CmsElement box = new CmsElement();
						box.addLayer("div","boxi2 medium3");
						box.addTag("h4","Muokkaus sivu");
						box.addLayer("div","ingroup filled");
						box.addTag("p","tapahtumaa ei lˆytynyt id["+ext+"]");
						page.addCenter(box);
					}				

					page.setTitle("Muokkaa tapahtumaa");

					page.addRight(genMonth(0));
					page.addRight(genMonth(1));

				}
			}else{
				CmsElement box = new CmsElement();
				box.createBox("Muokkaus sivu", "medium3");
				box.addLayer("div","boxi2 medium3");
				box.addTag("h4","Muokkaus sivu");
				box.addLayer("div","ingroup filled");
				page.setTitle("Viikkoohjelma");
				page.addCenter(box);
				page.addCenter(getActionLinks());
			}
		}});

		actions.add(new Action(null, "poista"){public void execute(){
			if(ext != ""){
				if(checkField("doit")){
					ViikkoDb db = new ViikkoDb();

					CmsElement box = new CmsElement();
					box.createBox("Muokkaus sivu", "medium3");
					
					String res;
					if((res = db.removeEntry(ext)) == null){
						box.addTag("p","tapahtuma tuhottu");
						box.addLink("Ok", script +"/"+ hook);
						ActionLog.action(username+" - deleted tapahtuma ["+ext+"]");
						pagebuilder.setRedirect(script+"/"+hook+"/");
					}else{
						box.addTag("p","poisto ep‰onnistui id["+ext+"]:"+res);
						ActionLog.action(username+" - tapahtuma deletion failed ["+ext+"]");
					}				

					page.setTitle("Viikkoohjelma");
					page.addCenter(box);
					page.addCenter(getActionLinks());
				}else{
					ViikkoDb db = new ViikkoDb();
					ViikkoEntry ve;

					CmsElement poista = new CmsElement();

					poista.addLayer("div","boxi2 medium3");
					poista.addTag("h4",null,"Poista tapahtuma");
					poista.addLayer("div", "ingroup filled");


					if((ve = db.loadEntry(ext)) == null){
						poista.addTag("p", "tapahtumaa ["+ext+"] ei lˆydy!");
						ActionLog.action(username+" - unable to deleted tapahtuma["+ext+"], not found");
					}else{
						poista.addFormTop(script+"/"+hook+"/"+action_hook+"/"+ext);

						poista.addLayer("table", "table5");
						poista.addLayer("tr");
						poista.addLayer("td");
						poista.addTag("p","Tuhotaanko ["+ve.otsikko+"]");
						poista.up();

						poista.addLayer("td");
						poista.addTag("label", "Kyll‰ ");
						poista.up();
						poista.addLayer("td");
						poista.addField("doit", "kyll‰", false, new CheckBoxField());
						poista.up();
						poista.up();
						poista.up();
						poista.addSingle("input class=\"list\" type=\"submit\" value=\"Jatka\"");
					}

					page.setTitle("Viikkoohjelma");
					page.addCenter(poista);
				}
			}else{
				CmsElement box = new CmsElement();
				box.createBox("Muokkaus sivu", "medium3");
				page.setTitle("Viikkoohjelma");
				page.addCenter(box);
				page.addCenter(getActionLinks());
			}
		}});

		actions.add(new Action("Esikatsele", "esikatsele"){public void execute(){
			if(!ext.equals("")){
				if(ext.length() > 0 && ext.length() < 3){
					try{
						int offset = Integer.parseInt(ext);
						page.addTop(getMenu());
						page.addLeft(getActionLinks().toString());
						page.addCenter("<div class=\"right\">");
						page.addCenter(getViikkoHtml(offset,true));
						page.addCenter("</div>");

						CmsElement boxb = new CmsElement();
						boxb.createBox(null, "medium3");
						
						boxb.addLayer("p style=\"font-size:9.5px;\"");
						boxb.addContent(getViikkoMail(offset,true));

						page.setTitle("Viikkoohjelma - esikatselu ");

						page.addCenter(boxb);

					}catch (NumberFormatException nfe) {
						pagebuilder.addMessage("virhe viikon offsetiss‰ ["+ext+"]");
					}
				}else{
					ViikkoDb db = new ViikkoDb();
					ViikkoEntry ve = db.loadEntry(ext);
					if(ve != null){
						StringBuilder p = new StringBuilder();
						p.append("<div class=\"right\">");
						p.append(genWeekHtmlString(ve,true));
						p.append("</div>");
						page.addTop(getMenu());
						page.addLeft(getActionLinks().toString());
						page.setTitle("esikatselu");
						page.addCenter(p.toString());
					}else{
						CmsElement esikatselu = new CmsElement();
						esikatselu.createBox("error", "medium3");
						
						esikatselu.addTag("p","Virhe tapahtumaa["+ext+"] ei saatu ladattua");
						page.setTitle("esikatselu");
						page.addLeft(getActionLinks());
						page.addCenter(esikatselu);
					}
				}
			}else{
				CmsElement esi = new CmsElement();

				esi.addLayer("div style=\"float:left;\"","right");
				esi.addContent(getViikkoHtml(0,true));
				esi.up();
				esi.addLayer("div style=\"float:left;\"","right");
				esi.addContent(getViikkoHtml(1,true));
				esi.up();

				esi.addSingle("br");
				esi.addLayer("div style=\"clear:both;font-size:12.5px;\"","ingroup");
				esi.addLayer("pre");
				esi.addContent(getViikkoMail(0,true));
				esi.up(2);

				esi.addLayer("div style=\"clear:both;font-size:12.5px;\"","ingroup");
				esi.addLayer("pre");
				esi.addContent(getViikkoMail(1,true));


				page.setTitle("Esikatselu");
				page.addLeft(getActionLinks());
				page.addCenter(esi);
			}
		}});



		actions.add(new Action("L‰het‰ s‰hkˆposti", "posti"){public void execute(){
			if(ext == ""){
				CmsElement boxi = new CmsElement();
				boxi.createBox("Postin l‰hetys", "medium3");

				boxi.addTag("a href=\""+script+"/"+hook+"/"+action_hook+"/0"+"\" class=\"list\"", "T‰m‰ viikko");
				boxi.addTag("a href=\""+script+"/"+hook+"/"+action_hook+"/1"+"\" class=\"list\"", "Ensiviikko");

				page.setTitle("Viikkopostin l‰hetys");
				page.addLeft(getActionLinks());
				page.addCenter(boxi);
			}else{
				if(checkField("saaja")){
					String result;
					if(ext.equals("0")){
						result = Mailer.sendMail(
								"TKrT tiedotus <tkrt@students.cc.tut.fi>",
								datarelay.post.get("saaja"),
								"[TKrT] TKrT:ll‰ tapahtuu",
								getViikkoMail(0,false)
						);
					}else if(ext.equals("1")){
						result = Mailer.sendMail(
								"TKrT tiedotus <tkrt@students.cc.tut.fi>",
								datarelay.post.get("saaja"),
								"[TKrT] TKrT:ll‰ tapahtuu",
								getViikkoMail(1,false)
						);
					}else{
						result = "error";
					}

					if(result == null){
						//CmsBoxi boxi = new CmsBoxi("Postin l‰hetys");
						CmsElement boxi = new CmsElement();
						boxi.createBox("Postin l‰hetys", "medium3");
						
						boxi.addTag("p","Posti l‰hetettiin onnistuneesti ["+datarelay.post.get("saaja")+"]");
						boxi.addLink("Ok", script +"/"+ hook);

						page.setTitle("Cms");
						page.addCenter(boxi);
					}else{
						CmsElement boxi = new CmsElement();
						boxi.createBox("Postin l‰hetys", "medium3");
						boxi.addTag("p","Virhe:"+result);
						boxi.addLink("Ok", script +"/"+ hook);

						page.setTitle("Cms");
						page.addCenter(boxi);
					}
				}else{
					CmsElement boxi = new CmsElement();
					boxi.addLayer("div", "boxi2 medium3");
					int week = 0;
					if(ext.equals("0")){
						boxi.addTag("h4", "T‰m‰ viikko");
						week = 0;
					}else if(ext.equals("1")){
						boxi.addTag("h4", "Ensiviikko");
						week = 1;
					}else{
						boxi.addTag("h4", "Error");
						week = 7;
					}

					boxi.addLayer("div","ingroup filled");
					boxi.addFormTop(script + "/" + hook + "/" + action_hook+"/"+week);
					boxi.addTag("p", "Osoite johon posti l‰hetet‰‰n");
					boxi.addField("saaja", null, true, new TextField(30));
					boxi.addSingle("input type=\"submit\" value=\"l‰het‰\" class=\"list\"");
					boxi.up(2);
					boxi.addSingle("br");
					boxi.addLayer("div", "boxi2");
					boxi.addLayer("div", "ingroup filled");
					boxi.addLayer("pre style=\"font-size:12.5px\"");
					boxi.addContent(getViikkoMail(week, false));
					page.setTitle("Viikko-ohjelma");

					page.addCenter(boxi);
				}
			}
		}});

		actions.add(new Action("P‰ivit‰ sivut", "julkaise"){public void execute(){
			if(checkField("flag")){
				FileOps.write(new File(datarelay.target,"s_viikko_gen.html"), getViikkoHtml(false), false);
				
				FileOps.write(new File(datarelay.target,"s_tuleva.html"), getTulevaHtml(), false);
				
				CmsElement box = new CmsElement();
				box.createBox("Sivujen p‰ivitys", "medium3");
				box.addTag("p","sivut p‰ivitetty");
				box.addLink("Ok", script+"/"+hook);

				page.setTitle("Viikkoohjelma");
				page.addCenter(box);
				page.addLeft(getActionLinks());

			}else{
				CmsElement box = new CmsElement();
				box.addFormTop(script + "/" + hook + "/" + action_hook);
				box.createBox("Sivujen p‰ivitys", "medium3");

				box.addLayer("table", "table5");
				box.addSingle("colgroup");
				box.addSingle("colgroup width=\"30\"");
				box.addSingle("colgroup width=\"70\"");

				box.addLayer("tr");
				box.addTag("td","do you really want this?");
				box.addLayer("td style=\"text-align:right;\"");
				box.addTag("label","yes");
				box.up();
				box.addLayer("td");
				box.addField("flag", null, true, new CheckBoxField(false));
				box.up();
				box.addTag("td", "&nbsp;");
				box.up();
				box.addLayer("tr");
				box.addTag("td colspan=\"3\"","<input value=\"do it\" type=\"submit\" class=\"list\" style=\"width:100%\"");

				page.setTitle("Viikkoohjelma");
				page.addCenter(box);
				page.addCenter("<div class=\"right\">");
				page.addCenter(getViikkoHtml(false));
				page.addCenter("</div>");
				
				page.addCenter("<div class=\"right\">");
				page.addCenter(getTulevaHtml());
				page.addCenter("</div>");
								
				page.addLeft(getActionLinks());
			}
		}});

		actions.add(null);

		actions.add(new Action("Clear old", "clearold"){public void execute(){
			new ViikkoDb().removeOld();

			CmsElement boxi = new CmsElement();
			boxi.createBox("jep", "medium3");
			boxi.addLink("Ok", script +"/"+ hook);

			page.setTitle("Cms");
			page.addCenter(boxi);
		}});


	}

	private String genWeekHtmlString(List<ViikkoEntry> week, boolean preview){
		//ArrayList<String> strings = new ArrayList<String>();
		StringBuilder strings = new StringBuilder();
		int previous_day = 0;
		strings.append("<ul>");
		for(ViikkoEntry ve: week){
			if(previous_day != ve.day){
				if( previous_day != 0){
					strings.append("</dl></li>\n");
				}
				previous_day = ve.day;
				strings.append("<li>\n");
				strings.append("<h4>"+ve.getDayName()+" - "+ve.day+"."+ve.month+".</h4>\n");
				strings.append("<dl>\n");
			}
			strings.append("<dt>"+ve.otsikko);
			if(preview)
				strings.append(" <a href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">&#187;m</a>");
			strings.append("</dt>\n");
			if(ve.hour != 0)
				strings.append("<dd>Klo "+ve.hour+":"+Utils.addLeading(ve.minute,2)+"</dd>\n");
			if(ve.paikka.length() > 0)
				strings.append("<dd>"+ve.paikka+"</dd>\n");
			for(String s:ve.yhteenveto)
				strings.append("<dd>"+s+"</dd>\n");
		}

		if(previous_day != 0){
			strings.append("</dl></li>");
		}else{
			log.info("temp paiva == null");
		}
		strings.append("</ul>");
		return strings.toString();
	}
	private String genWeekHtmlString(ViikkoEntry ve, boolean preview){
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>\n");
		sb.append("<li>\n");
		sb.append("<h4>"+ve.getDayName()+" - "+ve.day+"."+ve.month+".</h4>\n");
		sb.append("<dl>\n");

		sb.append("<dt>"+ve.otsikko);
		if(preview)
			sb.append(" <a href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">&#187;m</a>");
		sb.append("</dt>\n");
		if(ve.hour != 0)
			sb.append("<dd>Klo "+ve.hour+":"+Utils.addLeading(ve.minute,2)+"</dd>\n");
		if(ve.paikka.length() > 0)
			sb.append("<dd>"+ve.paikka+"</dd>\n");
		for(String s:ve.yhteenveto)
			sb.append("<dd>"+s+"</dd>\n");

		sb.append("</dl></li>");
		sb.append("</ul>\n");
		return sb.toString();
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}

	private String genMonth(int offset) {
		Calendar k = Calendar.getInstance();
		k.setFirstDayOfWeek(Calendar.MONDAY);
		if(offset != 0){
			k.set(k.get(Calendar.YEAR), k.get(Calendar.MONTH)+offset, 1);
		}
		int daymonth = k.get(Calendar.DAY_OF_MONTH);
		int dayweek = k.get(Calendar.DAY_OF_WEEK);
		int lastday = k.getActualMaximum(Calendar.DAY_OF_MONTH);

		String[] months = {
				"Tammi",
				"Helmi",
				"Maalis",
				"Huhti",
				"Touko",
				"Kes‰",
				"Hein‰",
				"Elo",
				"Syys",
				"Loka",
				"Marras",
				"Joulu",
		};

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"sidebar\">\n");
		sb.append("<h4>"+months[k.get(Calendar.MONTH)]+"kuu</h4>");
		sb.append("<table class=\"def\" style=\"background-color:white;text-align:right;font-size:9.5px;font-family:verdana;border:1px solid black;margin:10 auto; padding:0px\"><tr>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">MA</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">TI</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">KE</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">TO</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">PE</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">LA</td>\n");
		sb.append(" <td style=\"background-color:#00B3E0;color:white;padding:1px 2px;text-align:center;\">SU</td>\n");
		sb.append("</tr><tr>\n");

		int daycount = 0;
		for(int i = 0 - (dayweek-2);(daymonth+i < lastday+1); i++){
			if(!(i > 0)){
				//write empty cell <td>&nbsp;</td>
				if(i == 0){
					if(offset == 0){
						sb.append(" <td style=\"background-color:orange;padding:1px 2px;\">"+(daymonth)+"</td>\n");
					}else{
						sb.append(" <td style=\"background-color:#ACE8FD;padding:1px 2px;\">"+(daymonth)+"</td>\n");
					}
				}else{
					sb.append(" <td style=\"padding:1px 2px;\">&nbsp;</td>\n");
				}
			}else{
				//write  <td>day+i</td>
				sb.append(" <td style=\"background-color:#ACE8FD;padding:1px 2px;\">"+(daymonth+i)+"</td>\n");
			}
			if(++daycount == 7){
				daycount = 0;
				//write new row </tr><tr>
				sb.append("</tr><tr>\n");
			}
		}
		sb.append("</tr></table></div>\n");
		return sb.toString();
	}

	private String getViikkoMail(int offset, boolean preview){
		ViikkoDb db = new ViikkoDb();

		ArrayList<ViikkoEntry> stweekmail = 
			new ArrayList<ViikkoEntry>(Arrays.asList(db.getWeek(offset,true,false)));

		List<ViikkoEntry> ndweekmail = Arrays.asList(db.getWeek(offset+1,true,true));

		for(ViikkoEntry ve :db.getUserEntries("auto")){
			if(ve.enabled){
				if(ve.mailiin){			
					ve.genAutoWeek(offset);
					stweekmail.add(ve);
				}
			}
		}

		Collections.sort(stweekmail);
		Collections.sort(ndweekmail);

		final String lf = "\n";//(preview?"<br/>\n":"\n");
		final String delimit = "*************************************************************************";

		StringBuilder sb = new StringBuilder();
		sb.append("TKrT:n tulevaa toimintaa"+lf);
		sb.append("\nVko "+db.getWeekNumber(offset)+lf); 
		int oldday = 0;
		for(ViikkoEntry ve: stweekmail){
			if(oldday != ve.day){
				sb.append(ve.getDayName()+" ");
				oldday = ve.day;
			}else{
				sb.append("   ");
			}
			sb.append(ve.day+"."+ve.month+". ");
			sb.append(ve.otsikko);
			if(!(ve.hour==0&&ve.minute==0))
				sb.append(" "+ve.hour+":"+Utils.addLeading(ve.minute,2));
			if(ve.paikka != null && ve.paikka.length() != 0)
				sb.append(" "+ve.paikka);
			sb.append(lf);
		}
		sb.append(lf);
		if(ndweekmail.size()>0){
			sb.append(lf+"Vko "+db.getWeekNumber(offset+1)+lf);
			for(ViikkoEntry ve: ndweekmail){
				sb.append(ve.day+"."+ve.month+". ");
				sb.append(ve.otsikko);
				if(!(ve.hour==0&&ve.minute==0))
					sb.append(" "+ve.hour+":"+Utils.addLeading(ve.minute,2));
				if(ve.paikka != null && ve.paikka.length() != 0)
					sb.append(" "+ve.paikka);
				sb.append(lf);
			}
			sb.append(lf);
		}

		sb.append(delimit+lf); //*******

		for(ViikkoEntry ve: stweekmail){
			if(ve.yhteenveto.size()<1 && ve.teksti.size()<1){
				continue;
			}else{
				pagebuilder.addHidden("yhteen["+ve.yhteenveto.size()+"] ["+ve.otsikko+"]");
				pagebuilder.addHidden("teksti["+ve.teksti.size()+"]");
			}

			sb.append(ve.otsikko);

			if(preview)
				sb.append(" <a href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">&#187;m</a>");

			sb.append(lf+lf+ve.getDayName()+" "+ve.day+"."+ve.month+". ");

			if(!(ve.hour==0&&ve.minute==0))
				sb.append("Klo "+ve.hour+":"+Utils.addLeading(ve.minute,2));
			if(ve.paikka != null && ve.paikka.length() != 0)
				sb.append(lf+ve.paikka);
			if(ve.yhteenveto.size()>0)
				for(String s: ve.yhteenveto)
					if(s.length()>0){sb.append(lf+chop(s,73));}

			sb.append(lf+lf);
			if(ve.teksti.size()>0){
				for(String rivi : ve.teksti)
					sb.append(chop(rivi,73)+lf);
				sb.append(lf);
			}
			sb.append(delimit+lf);
		}
		for(ViikkoEntry ve: ndweekmail){
			sb.append(ve.otsikko);
			if(preview)
				sb.append(" <a href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">&#187;m</a>");
			sb.append(lf+ve.getDayName()+" "+ve.day+"."+ve.month+". ");
			if(!(ve.hour==0&&ve.minute==0))
				sb.append("Klo "+ve.hour+":"+Utils.addLeading(ve.minute,2));
			if(ve.paikka != null && ve.paikka.length() != 0)
				sb.append(lf+ve.paikka);

			sb.append(lf+lf);
			for(String rivi : ve.teksti)
				sb.append(rivi+lf);
			sb.append(lf);
			sb.append(delimit+lf);
		}
		return sb.toString();
	}

	private String chop(String rivi, final int line_size) {
		StringBuilder sb = new StringBuilder(rivi);
		int current_line_size = 0;
		int last_space = 0;
		for(int i = 0; i < sb.length(); i++){
			if(sb.charAt(i) == ' ')
				last_space = i;

			if(current_line_size == line_size){
				sb.insert(last_space, '\n');
				sb.deleteCharAt(last_space+1);
				current_line_size = i - last_space;				

			}else{
				current_line_size++;
			}
		}
		return sb.toString();
	}

	private String getViikkoHtml(int offset, boolean preview){
		ViikkoDb db = new ViikkoDb();

		List<ViikkoEntry> stweek;

		stweek = new ArrayList<ViikkoEntry>(Arrays.asList(db.getWeek(offset,false,false)));

		for(ViikkoEntry ve :db.getUserEntries("auto")){
			if(ve.enabled){
				ve.genAutoWeek(offset);
				stweek.add(ve);
			}
		}

		Collections.sort(stweek);

		StringBuilder viikko = new StringBuilder();

		if(stweek.size() > 0){
			viikko.append(genWeekHtmlString(stweek,preview)+"\n");
		}
		return viikko.toString();
	}

	private String getTulevaHtml(){
		ViikkoDb db = new ViikkoDb();
		CmsElement tap = new CmsElement();

		//ArrayList<List<ViikkoEntry>> weeks;
		List<ViikkoEntry> week;
		
		int viikon_numero = 0;
		log.fail("getting tuleva html...");
		for(int i = 0; i < 2; i++){
			log.fail(" i ["+i+"] ");
			week = new ArrayList<ViikkoEntry>(Arrays.asList(db.getWeek(i, false,false)));
			log.fail(" week.size() >"+week.size());
			
			//Collections.sort(week);
			
			if(week.size() > 0){
				viikon_numero = week.get(0).getWeek();
			}else{
				viikon_numero++;
			}
			log.fail(" viikon_numero["+viikon_numero+"]");
			
			if(week.size() < 1)
				continue;
			
			
			for(ViikkoEntry ve :db.getUserEntries("auto")){
				if(ve.enabled){
					ve.genAutoWeek(i);
					week.add(ve);
				}
			}

			
			Collections.sort(week);
		
			tap.addLayer("div", "boxi2 medium3");
			tap.addTag("h4", "Vko "+viikon_numero);
			tap.addLayer("div", "ingroup filled");
			tap.addLayer("table", "table5");
			tap.addSingle("colgroup width=\"90\"");
			tap.addSingle("colgroup width=\"65\"");
			tap.addSingle("colgroup width=\"160\"");
			tap.addSingle("colgroup");

			for(ViikkoEntry ve : week){
				tap.addLayer("tr");
				tap.addTag("td", null, ve.getDayName()+" "+ ve.day+"."+ve.month+".");
				tap.addTag("td", null, (ve.hour == 0 && ve.minute == 0 ? "&nbsp;" : Utils.addLeading(ve.hour,2) +":" + Utils.addLeading(ve.minute, 2)));
				tap.addTag("td", null, ve.otsikko);
				tap.addTag("td", null, ve.paikka);
				tap.up();
			}

			tap.up(3);
			
		}
		
		return tap.toString();
	
	}
	
	private String getViikkoHtml(boolean preview){
		ViikkoDb db = new ViikkoDb();
		Calendar k = Calendar.getInstance();
		k.setFirstDayOfWeek(Calendar.MONDAY);
		int weekday = k.get(Calendar.DAY_OF_WEEK);

		List<ViikkoEntry> stweek;
		List<ViikkoEntry> ndweek;
		List<ViikkoEntry> rdweek;

		if(weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY){
			stweek = Arrays.asList(db.getWeek(0,false,false));
			ndweek = new ArrayList<ViikkoEntry>(Arrays.asList(db.getWeek(1,false,false)));
			rdweek = Arrays.asList(db.getWeek(2,false,true));

			for(ViikkoEntry ve :db.getUserEntries("auto")){
				if(ve.enabled){
					ve.genAutoWeek(1);
					ndweek.add(ve);
				}
			}

			Collections.sort(stweek);
			Collections.sort(ndweek);
			Collections.sort(rdweek);

			Calendar tester = Calendar.getInstance();
			int dow;
			boolean found = false;
			for(int i = 0; i < stweek.size(); i++){
				ViikkoEntry entry = stweek.get(i);
				tester.clear();
				tester.setFirstDayOfWeek(Calendar.MONDAY);
				tester.set(entry.year, entry.month-1, entry.day);
				dow = tester.get(Calendar.DAY_OF_WEEK);
				if(dow == Calendar.SATURDAY || dow == Calendar.SUNDAY){
					stweek = stweek.subList(i, stweek.size());
					found = true;
					break;
				}
			}
			if(!found){
				stweek = new ArrayList<ViikkoEntry>();
			}

		}else{
			stweek = new ArrayList<ViikkoEntry>(Arrays.asList(db.getWeek(0,false,false)));
			ndweek = Arrays.asList(db.getWeek(1,false,true));
			rdweek = Arrays.asList(db.getWeek(2,false,true));

			for(ViikkoEntry ve :db.getUserEntries("auto")){
				if(ve.enabled){
					ve.genAutoWeek(0);
					stweek.add(ve);
				}
			}

			Collections.sort(stweek);
			Collections.sort(ndweek);
			Collections.sort(rdweek);
		}

		StringBuilder viikko = new StringBuilder();

		if(stweek.size() > 0){
			viikko.append(genWeekHtmlString(stweek,preview)+"\n");
		}

		if(ndweek.size() > 0){
			viikko.append(genWeekHtmlString(ndweek,preview)+"\n");
		}

		if(rdweek.size() > 0){
			viikko.append(genWeekHtmlString(rdweek,preview)+"\n");
		}
		return viikko.toString();

	}

	private String genPreview(ViikkoEntry ve){
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"right\" style=\"margin:10px auto;\">");
		sb.append(genWeekHtmlString(ve,true));
		sb.append("</div>");
		String lf = "\n";
		if(ve.mailiin){
			sb.append("<div class=\"ingroup medium4\">");
			sb.append("<pre style=\"font-size:12.5px;\">");

			sb.append(ve.day+"."+ve.month+". ");
			sb.append(ve.otsikko);
			if(!(ve.hour==0&&ve.minute==0))
				sb.append(" "+ve.hour+":"+Utils.addLeading(ve.minute,2));
			if(ve.paikka != null && ve.paikka.length() != 0)
				sb.append(" "+ve.paikka);
			sb.append(lf);			

			if(ve.yhteenveto.size()>0 && ve.teksti.size()>0){
				sb.append("\n************************************\n");

				sb.append(ve.otsikko);

				sb.append(" <a href=\""+script+"/"+hook+"/muokkaa/"+ve.id+"\">&#187;m</a>");

				sb.append(lf+lf+ve.getDayName()+" "+ve.day+"."+ve.month+". ");

				if(!(ve.hour==0&&ve.minute==0))
					sb.append("Klo "+ve.hour+":"+Utils.addLeading(ve.minute,2));
				if(ve.paikka != null && ve.paikka.length() != 0)
					sb.append(lf+ve.paikka);
				if(ve.yhteenveto.size()>0)
					for(String s: ve.yhteenveto)
						if(s.length()>0){sb.append(lf+s);}

				sb.append(lf+lf);
				if(ve.teksti.size()>0){
					for(String rivi : ve.teksti)
						sb.append(chop(rivi,47)+lf);
					sb.append(lf);
				}

			}
			sb.append("</p>");
			sb.append("</div>\n");
		}
		return sb.toString();
	}

	public boolean blind_update(){
		ActionLog.action("updating week, blind");
		
		return FileOps.write(new File(datarelay.target,"s_viikko_gen.html"), getViikkoHtml(false), false)
		&&
		FileOps.write(new File(datarelay.target,"s_tuleva.html"), getTulevaHtml(), false);
	}
}

