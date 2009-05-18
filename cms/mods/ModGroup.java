package cms.mods;

import html.CmsBoxi;

import java.util.ArrayList;

import util.ActionLog;
import cms.DataRelay;
import d2o.GroupDb;

public class ModGroup extends Module {
	
	public ModGroup(DataRelay datarelay) {
		super(datarelay);
		hook = "ryhmat";
		menu_label = "Ryhmien hallinta";
	}

	public void activate(){
		super.activate();
		actions.add(
				new Action(null, ""){
					public void execute(){
						page.setTitle("Ryhmien hallinta");
						page.addTop(getMenu());
						page.addCenter(getActionLinks().toString());
						//page.addRight(genTalkback().toString());
					}
				}
		);
		actions.add(
				new Action("Tulosta moduulit", "tulosta_mod"){
					public void execute(){
						ActionLog.action("success");
						CmsBoxi viewModulesBox = new CmsBoxi("Moduulit");
						viewModulesBox.addForm(script+"/"+hook+"/tulosta_mod");
						viewModulesBox.addTag("<pre>\n");
						for(String hook : datarelay.loader.getModuleStuff()){
							viewModulesBox.addTag(hook);
						}
						viewModulesBox.addTag("</pre>");
						viewModulesBox.addTag("</form>");

						page.setTitle("Ryhmä juttuja");
						page.addTop(getMenu());
						page.addCenter(viewModulesBox.toString());
					}
				}
		);
		actions.add(
				new Action("Lisää ryhmä", "lisaa_ryhma"){
					public void execute(){

						if(checkField("r_nimi")){
							log.info(" fields ok");
							ArrayList<String> modules = new ArrayList<String>();
							for(String field : datarelay.post.keySet()){
								if(field.equals("r_nimi"))
									continue;
								modules.add(field);
							}
							ArrayList<String> res = new ArrayList<String>();
							//String result = "";
							GroupDb gdb = GroupDb.getDb();
							if(!gdb.loadDb()){
								res.add("could not load groupdb");
							}else{
								if(!gdb.addGroup(datarelay.post.get("r_nimi"))){
									//ActionLog.error("addGroup failed");
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
							//String result = gdb.addGroup(datarelay.post.get("r_nimi"), modules.toArray(new String[0]));

							CmsBoxi resultBox = new CmsBoxi("Ryhmän lisäys");
							if(res.size() > 0){
								resultBox.addP("Virhe:");
								for(String s : res){
									resultBox.addPre("["+s+"]");
								}
								ActionLog.action("group add fail");
							}else{
								resultBox.addP("Ryhmän ["+datarelay.post.get("r_nimi")+"] lisäys onnistui.");
								ActionLog.action("group add success");
							}

							page.setTitle("Ryhmä juttuja");
							page.addTop(getMenu());
							page.addCenter(resultBox.toString());
						}else{
							CmsBoxi addGroupBox = new CmsBoxi("Lisää ryhmä");
							addGroupBox.addForm(script+"/"+hook+"/lisaa_ryhma");
							addGroupBox.addInput("r_nimi", null, "text", "Nimi");
							addGroupBox.addInput(null, null, null, "Käyttöoikeus");
							addGroupBox.addTag("<table class=\"def\">\n");
							for(String hook : datarelay.loader.getModuleStuff()){
								addGroupBox.addTag("<tr><td>"+hook+"</td><td><input name=\""+hook+"\" type=\"checkbox\"/></td></tr>");
							}
							addGroupBox.addTag("</table>");
							addGroupBox.addTag("<input type=\"submit\" value=\"käsittele\"/>");
							addGroupBox.addTag("</form>");

							page.setTitle("Ryhmä juttuja");
							page.addTop(getMenu());
							page.addCenter(addGroupBox.toString());
						}
					}
				}
		);
		actions.add(
				new Action("Muokkaa oikeuksia", "muokkaa"){
					public void execute(){

						if(datarelay.post != null){
							log.info("muokkausta tapahtuu");
							CmsBoxi resultBox = new CmsBoxi("Ryhmien muokkaus");
							ArrayList<String> result = new ArrayList<String>();
							GroupDb gdb = GroupDb.getDb();
							gdb.loadDb();
							gdb.reset();

							for(String raw : datarelay.post.keySet()){
								log.info("["+raw+"]->");
								int st;
								String mod = "";
								String act = "";
								String group;
								if((st = raw.indexOf(':')) != -1){
									group = raw.substring(0, st);
									int nd;
									if((nd = raw.indexOf('/'))!= -1 ){
										mod = raw.substring(st + 1, nd);
										act = raw.substring(nd+1);
									}else{
										mod = raw.substring(st + 1);
									}
									log.info(" g["+group+"] m["+mod+"] a["+act+"]");
									result.add(group+":"+mod+"/"+act+(gdb.push(group,mod,act) ? " ok" : " fail"));
								}else{
									log.info(" fail[:]");
								}
							}

							result.add((gdb.storeDb()?"Store success":"Store failure"));
							resultBox.addTag("<p> näin:<br/>\n");
							for(String s: result){
								resultBox.addTag(s+"<br/>\n");
							}
							resultBox.addTag("</p>\n");

							page.setTitle("Ryhmä juttuja");
							page.addTop(getMenu());
							page.addCenter(resultBox.toString());
						}else{
							CmsBoxi changeAllBox = new CmsBoxi("Muokkaa oikeuksia");
							changeAllBox.addForm(script+"/"+hook+"/muokkaa");

							changeAllBox.addTag("<table class=\"def\">\n<tr>\n<td>&nbsp;</td>");
							GroupDb gdb = GroupDb.getDb();
							String[] groups = gdb.getGroupNames();
							int num_groups = groups.length;
							for(String hook : groups){
								changeAllBox.addTag("<td>"+hook+"</td>");
							}
							changeAllBox.addTag("</tr>\n");
							for(String module : datarelay.loader.getModuleStuff()){
								changeAllBox.addTag("<tr><td>"+module+"</td>");
								for(int i = 0; i < num_groups; i++){
									changeAllBox.addSource(
											"<td><input name=\""+groups[i]+":"+module+
											"\" type=\"checkbox\" "
									);

									if(module.contains("/")){
										if(module.indexOf('/') != module.length()-2){
											String act = module.substring(module.indexOf('/')+1);
											String mod = module.substring(0,module.indexOf('/'));
											changeAllBox.addSource((gdb.fastCanAccess(groups[i], mod, act)?"checked=\"checked\"":""));
										}else{
											String mod = module.substring(0,module.indexOf('/'));
											changeAllBox.addSource((gdb.fastCanAccess(groups[i], mod, "")?"checked=\"checked\"":""));
										}
									}else{
										//return group_db.fastCanAccess(group, module, "");
										changeAllBox.addSource((gdb.fastCanAccess(groups[i], mod, "")?"checked=\"checked\"":""));
									}
									changeAllBox.addSource(" /></td>\n");
								}
								changeAllBox.addTag("</tr>\n");
							}
							changeAllBox.addTag("</table>");
							changeAllBox.addTag("<input type=\"submit\" value=\"käsittele\"/>");
							changeAllBox.addTag("</form>");

							page.setTitle("Ryhmä juttuja");
							page.addTop(getMenu());
							page.addCenter(changeAllBox.toString());
						}
					}
				}
		);
		actions.add(
				new Action("Näytä ryhmät", "nayta"){
					public void execute(){

						CmsBoxi viewModulesBox = new CmsBoxi("Moduulit");
						viewModulesBox.addForm(script+"/"+hook+"/tulosta_mod");
						viewModulesBox.addTag("<pre>\n");
						GroupDb gdb = GroupDb.getDb();
						for(String hook : gdb.getGroupNames()){
							viewModulesBox.addTag(hook);
						}
						viewModulesBox.addTag("</pre>");
						viewModulesBox.addTag("</form>");

						page.setTitle("Ryhmä juttuja");
						page.addTop(getMenu());
						page.addCenter(viewModulesBox.toString());
					}
				}
		);
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}

