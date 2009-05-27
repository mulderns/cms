package cms.mods;

import html.CheckBoxField;
import html.CmsElement;
import html.PassField;
import html.SubmitField;
import html.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import util.ActionLog;
import cms.DataRelay;
import d2o.GroupDb;
import d2o.UserDb;
import d2o.UserDbRecord;

public class ModAccess extends Module {

	public ModAccess(DataRelay datarelay) {
		super(datarelay);
		hook = "oikeudet";
		menu_label = "Oikeuksien hallinta";
	}

	public void activate(){
		super.activate();

		actions.add(new Action(null,""){public void execute(){
			GroupDb gdb = GroupDb.getDb();

			String [] groups = gdb.getGroupNames();

			CmsElement box = new CmsElement();

			box.addLayer("div", "boxi2");
			box.addTag("h4", "Oikeudet");
			box.addLayer("div", "ingroup filled");
			box.addLayer("table", "table5");
			box.addSingle("colgroup width=\"120\"");
			box.addLayer("tr");
			box.addTag("td","&nbsp;");
			for(String group: groups){
				box.addTag("td", "<a title=\"muokkaa\" class=\"but\" href=\""+script+"/"+hook+"/muokkaa_r/"+group+"\">"+group+"</a>");
			}
			box.up();
			ArrayList<String> users  = new ArrayList<String>(Arrays.asList(UserDb.getNames()));
			Collections.sort(users);
			for(String user : users){
				String[] belongs = UserDb.getGroups(user);
				box.addLayer("tr");
				box.addTag("td", "<a title=\"muokkaa\" class=\"but\" href=\""+script+"/"+hook+"/muokkaa_k/"+user+"\">"+user+"</a>");
				for(String group: groups){
					box.addLayer("td style=\"text-align:center;font-size:14px;background-color:#fafafa;\"");
					boolean found = false;
					for(String b:belongs){
						if(b.equals(group)){
							found = true;
							box.addContent("&#215;");
							break;
						}
					}
					if(!found)
						box.addContent("&nbsp;");
					box.up();
				}
				//box.addTag("td", "<a title=\"poista\" class=\"but\" href=\""+script+"/"+hook+"/poista_k/"+user +"\">X</a>");
				box.up();
			}

			page.setTitle("Oikeuksien hallinta");
			page.addTop(getMenu());
			page.addRight(getMenuExtra());
			page.addLeft(getActionLinks());
			page.addCenter(box);
		}});

		actions.add(new Action("Lis‰‰ k‰ytt‰j‰","lisaa_k"){public void execute(){
			GroupDb gdb = GroupDb.getDb();

			CmsElement box = new CmsElement();
			box.addFormTop(script+"/"+hook+"/"+action_hook);
			box.addLayer("div","boxi2 medium3");
			box.addTag("h4", "Lis‰‰ k‰ytt‰j‰");
			box.addLayer("div","ingroup filled");
			box.addLayer("table", "def");
			box.addLayer("tr");
			box.addTag("td","Tunnus:");
			box.addLayer("td");
			box.addField("tunnus",null,true,new TextField(20));
			box.up(2);

			box.addLayer("tr");
			box.addTag("td","Salasana:");
			box.addLayer("td");
			box.addField("sana",null,true,new PassField(20));
			box.up(2);

			box.addLayer("tr");
			box.addTag("td","Uudestaan:");
			box.addLayer("td");
			box.addField("sana2",null,true,new PassField(20));
			box.up(2);
			box.addLayer("tr");
			box.addTag("td","<h3>Ryhm‰t: </h3>");
			box.up();

			for(String s : gdb.getGroupNames()){
				box.addContent("<tr><td>"+s+"</td><td>"); 
				box.addContent("<input name=\""+s+"\" type=\"checkbox\"/>");
				box.addContent("</td></tr>");
			}
			box.up();
			box.addField("sub", "Lis‰‰", false, new SubmitField(true));


			if(checkFields(box.getFields())){
				log.info("fields found");
				String virhe_sanoma = "";

				if(datarelay.post.get("sana").length() < 4){				
					virhe_sanoma = "Salasanan tulee olla v‰hint‰‰n 4 merkki‰";
				}else if(datarelay.post.get("sana").equals(datarelay.post.get("tunnus"))){
					virhe_sanoma = "Salasana ei voi olla sama kuin tunnus";
				}else if(!datarelay.post.get("sana").equals(datarelay.post.get("sana2"))){
					virhe_sanoma = "ensim‰inen ja toinen salasana eroavat toisistaan";
				}else{
					UserDb udb = UserDb.getDb();
					ArrayList<String> groups = new ArrayList<String>();
					for(String key :datarelay.post.keySet()){
						if(!key.equals("tunnus") && !key.equals("sana"))
							if(gdb.groupExists(key))
								groups.add(key);
					}
					if(!udb.addUser(datarelay.post.get("tunnus"), datarelay.post.get("sana"), groups.toArray(new String[groups.size()]))){
						virhe_sanoma = "lis‰ys ep‰onnistui";
					}else{
						String temp;
						if((temp = udb.storeDb())!=null){
							virhe_sanoma = temp;
						}

					}
				}
				if(!virhe_sanoma.equals("")){
					ActionLog.action(username + " - failure in adding user ["+datarelay.post.get("tunnus")+"]");
					//CmsBoxi result = new CmsBoxi("K‰ytt‰j‰n lis‰ys ep‰onnistui");
					CmsElement result = new CmsElement();
					result.addLayer("div","boxi2 medium3");
					result.addTag("h4","K‰ytt‰j‰n lis‰ys ep‰onnistui");
					result.addLayer("div","ingroup filled");
					result.addTag("p","Virhe: " + virhe_sanoma);
					box.setFields(datarelay.post);
					page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
					page.addTop(getMenu());
					page.addCenter(result);
					page.addCenter(box);

				}else{
					ActionLog.action(username + " - added user ["+datarelay.post.get("tunnus")+"]");
					//CmsBoxi result = new CmsBoxi("K‰ytt‰j‰n lis‰ys onnistui");
					CmsElement result = new CmsElement();
					result.addLayer("div","boxi2 medium3");
					result.addTag("h4","K‰ytt‰j‰n lis‰ys onnistui");
					result.addLayer("div","ingroup filled");
					result.addTag("p","K‰ytt‰j‰ ["+datarelay.post.get("tunnus")+"] list‰ttiin onnistuneesti.");
					result.addLink("lis‰‰ lis‰‰", script + "/" + hook + "/"+action_hook);
					result.addLink("muut toiminnot", script + "/" + hook );

					pagebuilder.setRedirect(script + "/" + hook + "/");

					page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
					page.addTop(getMenu());
					page.addCenter(result.toString());
				}

			}else{
				log.info("fields not found");
				page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
				page.addTop(getMenu());
				page.addCenter(box.toString());
			}
		}});

		actions.add(new Action("Poista k‰ytt‰ji‰","poista_k"){public void execute(){
			page.setTitle("Poista k‰ytt‰ji‰");
			page.addTop(getMenu());
			page.addLeft(getActionLinks());
			page.addRight(getMenuExtra());

			CmsElement nimilista = new CmsElement();
			nimilista.addFormTop(script + "/" + hook + "/" + action_hook);
			nimilista.addLayer("div","boxi2 medium3");
			nimilista.addTag("h4", "Valitse poistettavat");
			nimilista.addLayer("div","ingroup filled");
			nimilista.addLayer("table","table5");
			for(String user : UserDb.getNames()){
				nimilista.addContent("<tr><td>"+user+"</td><td>");
				nimilista.addField("del_"+user, null, true, new CheckBoxField());
				nimilista.addContent("</td></tr>");
			}

			nimilista.addContent("<tr><td><h5 style=\"margin:0\">Confirm:</h5></td><td>");
			nimilista.addField("confirm", "confirm", true, new CheckBoxField());
			nimilista.addContent("</td></tr>");
			nimilista.up();
			nimilista.addField("submit", "poista", false, new SubmitField(true));

			if(checkField("confirm")){
				log.info("poistetaan k‰ytt‰ji‰");
				UserDb udb = UserDb.getDb();
				ArrayList<String> results = new ArrayList<String>();

				for(Map.Entry<String, String> e : datarelay.post.entrySet()){
					log.info(e.getKey());
					if(!e.getKey().startsWith("del_"))
						continue;
					String name = e.getKey().substring(e.getKey().indexOf('_')+1);
					log.info(" >"+name);
					if(!udb.removeUser(name)){
						results.add("["+name+"]...ei lˆytynyt tai saatu poistettua");
						log.fail(" ei poistettu");
					}else{
						results.add("["+name+"]...poistettu");
						log.info(" poistettu");
					}
				}

				udb.storeDb();
				CmsElement box = new CmsElement();
				box.addLayer("pre");
				for(String s: results)
					box.addContent(s+"\n");
				page.addCenter(box);
				return;
				
			}				

			
			page.addCenter(nimilista);

		}});

		actions.add(null);

		actions.add(new Action("Lis‰‰ ryhm‰","lisaa_r"){public void execute(){
			if(checkField("r_nimi")){

				log.info(" fields ok");

				ArrayList<String> modules = new ArrayList<String>();
				for(String field : datarelay.post.keySet()){
					if(field.equals("r_nimi"))
						continue;
					modules.add(field);
				}
				ArrayList<String> res = new ArrayList<String>();

				GroupDb gdb = GroupDb.getDb();
				if(!gdb.loadDb()){
					res.add("could not load groupdb");
				}else{
					if(!gdb.addGroup(datarelay.post.get("r_nimi"))){
						res.add("Groupname["+datarelay.post.get("r_nimi")+"] in use allready");
					}else{
						if(modules.size() > 0){

							if(!gdb.addGroupModule(datarelay.post.get("r_nimi"), modules.toArray(new String[modules.size()]))){
								res.add("failed in adding modules to group");
							}
						}
						if(!gdb.storeDb()){
							res.add("could not store db");
						}
					}
				}

				//CmsBoxi resultBox = new CmsBoxi("Ryhm‰n lis‰ys");
				CmsElement resultBox = new CmsElement();
				resultBox.addLayer("div","boxi2 medium3");
				resultBox.addTag("h4","Ryhm‰n lis‰ys");
				resultBox.addLayer("div","ingroup filled");
				
				if(res.size() > 0){
					resultBox.addTag("p","Virhe:");
					
					for(String s : res){
						resultBox.addTag("pre","["+s+"]");
					}
					ActionLog.action("group add fail");
				}else{
					resultBox.addTag("p","Ryhm‰n ["+datarelay.post.get("r_nimi")+"] lis‰ys onnistui.");
					ActionLog.action("group add success");
					pagebuilder.setRedirect(script+"/"+hook+"/");
				}

				page.setTitle("Ryhm‰ juttuja");
				page.addTop(getMenu());
				page.addCenter(resultBox.toString());
			}else{
				CmsElement box = new CmsElement();
				box.addLayer("div", "boxi2 medium4");
				box.addFormTop(script+"/"+hook+"/"+action_hook);
				box.addTag("h4", "Uusi ryhm‰");
				box.addLayer("div", "ingroup filled");
				box.addTag("label","Ryhm‰n nimi:");
				box.addField("r_nimi", null, false, new TextField());

				box.addLayer("div", "ingroup");
				box.addLayer("table", "table5");
				box.addSingle("colgroup width=\"150\"");
				box.addSingle("colgroup width=\"20\"");
				box.addSingle("colgroup");
				box.addLayer("tr");
				box.addTag("td colspan=\"2\"", "K‰yttˆoikeus:");
				box.addTag("td", "&nbsp;");
				box.up();
				for(String hook : datarelay.loader.getModuleStuff()){
					box.addLayer("tr");
					box.addTag("td", hook);
					box.addLayer("td");
					box.addField(hook, null, false, new CheckBoxField());
					box.up(2);
				}
				box.up(2);
				box.addContent("<input class=\"list\" type=\"submit\" value=\"k‰sittele\"/>");

				page.setTitle("Ryhm‰ juttuja");
				page.addTop(getMenu());
				page.addCenter(box);
			}

		}});

		actions.add(new Action("Poista ryhm‰","poista_r"){public void execute(){
		}});

		actions.add(new Action(null,"muokkaa_k"){public void execute(){
			if(!ext.equals("")){
				if(datarelay.post != null && datarelay.post.size() != 0){
					GroupDb groupdb = GroupDb.getDb();
					UserDb userdb = UserDb.getDb();
					ArrayList<String> groups = new ArrayList<String>();
					for(String s : datarelay.post.keySet()){
						if(groupdb.groupExists(s)){
							groups.add(s);
						}
					}
					userdb.getUser(ext).groups = groups.toArray(new String[groups.size()]);
					String result = userdb.storeDb();
					if(result != null){
						//CmsBoxi failBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
						CmsElement failBox = new CmsElement();
						failBox.addLayer("div","boxi2 medium3");
						failBox.addTag("h4","Muokkaa k‰ytt‰j‰‰");
						failBox.addLayer("div","ingroup filled");
						failBox.addTag("p","Muokkaus ep‰onnistui:"+result);
						failBox.addLink("ok", script + "/" + hook + "/" + action_hook + "/"+ext);

						page.setTitle("K‰ytt‰jien hallinta");
						page.addTop(getMenu());
						page.addCenter(failBox);
					}else{
						//						CmsBoxi successBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
						//						successBox.addP("Muokkaus onnistui!");
						//						successBox.addLink("ok", script + "/" + hook);
						//						page.setTitle("K‰ytt‰jien hallinta");
						//						page.addTop(getMenu());
						//						page.addCenter(successBox.toString());
						pagebuilder.setRedirect(script + "/" + hook);
					}

				}else{
					UserDb userdb = UserDb.getDb();
					UserDbRecord user = userdb.getUser(ext);

					GroupDb groupdb = GroupDb.getDb();

					if(user != null){

						//CmsBoxi modifyBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
						CmsElement modifyBox = new CmsElement();
						modifyBox.addLayer("div","boxi2 medium3");
						modifyBox.addTag("h4","Muokkaa k‰ytt‰j‰‰");
						modifyBox.addLayer("div","ingroup filled");
						modifyBox.addFormTop(script+"/"+hook+"/"+action_hook+"/"+ext);
						modifyBox.addContent("<table class=\"def\">\n<tr><td>");
						modifyBox.addTag("label","Tunnus:");
						modifyBox.addContent("</td><td>");
						modifyBox.addContent(user.name);
						modifyBox.addContent("</td></tr>\n<tr><td><h3>Ryhm‰t: </h3></td></tr>\n");

						for(String s : groupdb.getGroupNames()){
							modifyBox.addContent("<tr><td>"+s+"</td><td>"); 
							modifyBox.addContent("<input name=\""+s+"\" type=\"checkbox\" ");
							for(String group : user.groups){
								if(s.equals(group)){
									modifyBox.addContent("checked=checked");
									break;
								}
							}
							modifyBox.addContent("/>\n</td></tr>");
						}
						modifyBox.addContent("</table>");
						//modifyBox.addInput(null, "lis‰‰", "submit", null);
						modifyBox.addField(null, "lis‰‰", false, new SubmitField(true));
						modifyBox.addContent("</form>");

						page.setTitle("K‰ytt‰jien hallinta");
						page.addTop(getMenu());
						page.addCenter(modifyBox.toString());
					}
				}
			}else{
				//CmsBoxi failBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
				CmsElement failBox = new CmsElement();
				failBox.addLayer("div","boxi2 medium3");
				failBox.addTag("h4","Muokkaa k‰ytt‰j‰‰");
				failBox.addLayer("div","ingroup filled");
				failBox.addTag("p","hupsi");

				page.setTitle("K‰ytt‰jien hallinta");
				page.addTop(getMenu());
				page.addCenter(failBox.toString());
			}
		}});

		actions.add(new Action(null,"muokkaa_r"){public void execute(){
			page.setTitle("muokkaa ryhm‰‰ - "+ext );			
			page.addTop(getMenu());
			page.addRight(getMenuExtra());

			GroupDb gdb = GroupDb.getDb();

			if(!gdb.groupExists(ext)){
				page.addCenter("no such group");
				return;
			}

			if(checkField("sub")){
				log.info("muokkausta tapahtuu");
				//CmsBoxi resultBox = new CmsBoxi("Ryhmien muokkaus");
				CmsElement resultBox = new CmsElement();
				resultBox.addLayer("div","boxi2 medium3");
				resultBox.addTag("h4","Ryhmien muokkaus");
				resultBox.addLayer("div","ingroup filled");
				ArrayList<String> result = new ArrayList<String>();

				gdb.reset(ext);

				for(String field : datarelay.post.keySet()){
					log.info("["+field+"]->");

					String mod = "";
					String act = "";

					int limit;
					if((limit = field.indexOf('/'))!= -1 ){
						mod = field.substring(0, limit);
						act = field.substring(limit+1);
					}else{
						mod = field.substring(0);
					}
					log.info(" m["+mod+"] a["+act+"]");
					result.add(mod+"/"+act+(gdb.push(ext,mod,act) ? " ok" : " fail"));
				}

				if(gdb.storeDb()){
					pagebuilder.setRedirect(script+"/"+hook);
					return;
				}


				resultBox.addContent("<p> n‰in:<br/>\n");
				for(String s: result){
					resultBox.addContent(s+"<br/>\n");
				}
				resultBox.addContent(" store ep‰onnistui</p>\n");
				page.addCenter(resultBox.toString());
				return;
			}

			CmsElement box = new CmsElement();
			box.addFormTop(script + "/" + hook + "/" +action_hook+"/"+ext);
			box.addLayer("div", "boxi2 medium3");
			box.addTag("h4", ext);
			box.addLayer("div", "ingroup filled");
			box.addLayer("table","table5");

			for(String module : datarelay.loader.getModuleStuff()){
				box.addContent("<tr><td>"+module+"</td>");
				box.addContent(
						"<td><input name=\""+module+
						"\" type=\"checkbox\" "
				);

				if(module.contains("/")){
					if(module.indexOf('/') != module.length()-2){
						String act = module.substring(module.indexOf('/')+1);
						String mod = module.substring(0,module.indexOf('/'));
						box.addContent((gdb.fastCanAccess(ext, mod, act)?"checked=\"checked\"":""));
					}else{
						String mod = module.substring(0,module.indexOf('/'));
						box.addContent((gdb.fastCanAccess(ext, mod, "")?"checked=\"checked\"":""));
					}
				}else{
					//return group_db.fastCanAccess(group, module, "");
					box.addContent((gdb.fastCanAccess(ext, mod, "")?"checked=\"checked\"":""));
				}
				box.addContent(" /></td>\n");
				box.addContent("</tr>\n");
			}

			box.up();
			box.addField("sub", "k‰sittele", false, new SubmitField(true));

			page.addCenter(box);
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}
