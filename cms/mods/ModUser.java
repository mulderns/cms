package cms.mods;

import html.CmsBoxi;
import html.PassField;
import html.TextField;

import java.util.ArrayList;

import util.ActionLog;
import cms.DataRelay;
import d2o.GroupDb;
import d2o.UserDb;
import d2o.UserDbRecord;


public class ModUser extends Module {

	public ModUser(DataRelay datarelay) {
		super(datarelay);
		hook = "kayttajat";
		menu_label = "K‰ytt‰jien hallinta";
	}

	public void activate(){
		super.activate();

		actions.add(
				new Action(null, ""){
					public void execute(){
						CmsBoxi naytaNimiBox = new CmsBoxi("K‰ytt‰j‰t");
						naytaNimiBox.addTag("<table class=\"def\">");
						log.info("hook["+hook+"]");

						for(String user : UserDb.getNames()){
							naytaNimiBox.addSource(
									"<tr>\n<td>"+user+"</td>\n<td>"+
									"<a class=\"line\" href=\""+script+"/"+hook+"/muokkaa/"+user+"\">muokkaa</a>" +
									" <a class=\"line\" href=\""+script+"/"+hook+"/poista/"+user+"\">poista</a>" +
									"</td>\n<td> "
							);
							for(String s : UserDb.getGroups(user)){

								naytaNimiBox.addSource(s+" ");
							}
							naytaNimiBox.addSource("</td></tr>");
						}
						naytaNimiBox.addTag("</table>");
						naytaNimiBox.addLink("Lis‰‰ k‰ytt‰j‰",script+"/"+hook+"/lisaa");

						page.setTitle("K‰ytt‰jien hallinta");
						page.addTop(getMenu());
						page.addCenter(naytaNimiBox.toString());
					}
				}
		);

		actions.add(
				new Action("Lis‰‰ k‰ytt‰j‰", "lisaa"){
					public void execute(){
						GroupDb gdb = GroupDb.getDb();

						CmsBoxi addUserBox = new CmsBoxi("Lis‰‰ k‰ytt‰j‰");
						addUserBox.addForm(script+"/"+hook+"/"+action_hook);
						addUserBox.addTag("<table class=\"def\">\n<tr><td>");
						addUserBox.addLabel("Tunnus");
						addUserBox.addTag("</td><td>");
						addUserBox.addField("tunnus",null,true,new TextField(20));
						addUserBox.addTag("</td></tr>\n<tr><td>");
						addUserBox.addLabel("Salasana");
						addUserBox.addTag("</td><td>");
						addUserBox.addField("sana",null,true,new PassField(20));
						addUserBox.addTag("</td></tr>\n<tr><td><h3>Ryhm‰t: </h3></td></tr>\n");

						for(String s : gdb.getGroupNames()){
							addUserBox.addTag("<tr><td>"+s+"</td><td>"); 
							addUserBox.addTag("<input name=\""+s+"\" type=\"checkbox\"/>");
							addUserBox.addTag("</td></tr>");
						}
						addUserBox.addTag("</table>");
						addUserBox.addInput(null, "lis‰‰", "submit", null);
						addUserBox.addTag("</form>");

						if(checkFields(addUserBox.getFields())){
							log.info("fields found");
							String virhe_sanoma = "";

							if(datarelay.post.get("sana").length() < 4){				
								virhe_sanoma = "Salasanan tulee olla v‰hint‰‰n 4 merkki‰";
							}else if(datarelay.post.get("sana").equals(datarelay.post.get("tunnus"))){
								virhe_sanoma = "Salasana ei voi olla sama kuin tunnus";
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
								CmsBoxi result = new CmsBoxi("K‰ytt‰j‰n lis‰ys ep‰onnistui");
								result.addP("Virhe: " + virhe_sanoma);
								addUserBox.setFields(datarelay.post);
								page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
								page.addTop(getMenu());
								page.addCenter(result.toString());
								page.addCenter(addUserBox.toString());
								//page.addRight(genBugreport().toString());

							}else{
								ActionLog.action(username + " - added user ["+datarelay.post.get("tunnus")+"]");
								CmsBoxi result = new CmsBoxi("K‰ytt‰j‰n lis‰ys onnistui");
								result.addP("K‰ytt‰j‰ ["+datarelay.post.get("tunnus")+"] list‰ttiin onnistuneesti.");
								result.addLink("lis‰‰ lis‰‰", script + "/" + hook + "/"+action_hook);
								result.addLink("muut toiminnot", script + "/" + hook );

								page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
								page.addTop(getMenu());
								page.addCenter(result.toString());
							}

						}else{
							log.info("fields not found");
							page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
							page.addTop(getMenu());
							page.addCenter(addUserBox.toString());
						}
					}					
				}
		);

		actions.add(
				new Action(null, "poista"){
					public void execute(){
						if(!ext.equals("")){
							if(checkField("deleteit")){
								UserDb udb = UserDb.getDb();
								String result = "";
								if(!udb.removeUser(ext)){
									result = "k‰ytt‰j‰‰ ei lˆytynyt";
								}else{
									udb.storeDb();
								}
								CmsBoxi results = new CmsBoxi("K‰ytt‰jien poisto");
								if(!result.equals("")){
									results.addP("K‰ytt‰j‰n ["+ext+"] ep‰onnistui:" +result);
									ActionLog.action("fail");
									page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
									page.addTop(getMenu());
									page.addCenter(results.toString());
								}else{

									results.addP("K‰ytt‰j‰n ["+ext+"] poisto onnistui.");
									results.addLink("ok", script + "/" + hook + "/" + action_hook);
									ActionLog.action("success");

									page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
									page.addTop(getMenu());
									page.addCenter(results.toString());
								}
							}else{
								boolean found = false;
								for(String user : UserDb.getNames()){
									if(ext.equals(user)){
										found = true;
									}
								}
								if(found){
									CmsBoxi confirmBox = new CmsBoxi("Vahvista poisto");
									confirmBox.addP("Haluatko varmasti poistaa k‰ytt‰j‰n ["+ext+"] ?");
									confirmBox.addForm(script + "/" + hook + "/poista/" + ext);
									confirmBox.addInput("deleteit", null, "checkbox", "Kyll‰");
									confirmBox.addInput(null, "Jatka", "submit", null);
									confirmBox.addTag("</form>");

									page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
									page.addTop(getMenu());
									page.addCenter(confirmBox.toString());
								}else{
									CmsBoxi confirmBox = new CmsBoxi("Virhe");
									confirmBox.addP("k‰ytt‰j‰‰ ["+ext+"] ei lˆytynyt");
									ActionLog.error("fail - not found");
									page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
									page.addTop(getMenu());
									page.addCenter(confirmBox.toString());
								}
							}

						}else{
							CmsBoxi naytaNimiBox = new CmsBoxi("Poista k‰ytt‰j‰");
							naytaNimiBox.addTag("<table class=\"def\">");
							for(String user : UserDb.getNames()){
								naytaNimiBox.addTag(
										"<tr><td>"+user+"</td><td>"+
										"<a class=\"line\" href=\""+
										script+"/"+hook+"/poista/"+user+
										"\">poista</a></td></tr>"
								);
							}
							naytaNimiBox.addTag("</table>");
							naytaNimiBox.addLink("takaisin", script + "/" + hook);

							page.setTitle("K‰ytt‰jien hallinta");
							page.addTop(getMenu());
							page.addCenter(naytaNimiBox.toString());
							page.addCenter(getActionLinks().toString());
						}
					}
				}
		);

		actions.add(
				new Action(null, "muokkaa"){
					public void execute(){
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
									CmsBoxi failBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
									failBox.addP("Muokkaus ep‰onnistui:"+result);
									failBox.addLink("ok", script + "/" + hook + "/" + action_hook + "/"+ext);

									page.setTitle("K‰ytt‰jien hallinta");
									page.addTop(getMenu());
									page.addCenter(failBox.toString());
								}else{
									CmsBoxi successBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
									successBox.addP("Muokkaus onnistui!");
									successBox.addLink("ok", script + "/" + hook);
									page.setTitle("K‰ytt‰jien hallinta");
									page.addTop(getMenu());
									page.addCenter(successBox.toString());
								}

							}else{
								UserDb userdb = UserDb.getDb();
								UserDbRecord user = userdb.getUser(ext);

								GroupDb groupdb = GroupDb.getDb();

								if(user != null){

									CmsBoxi modifyBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
									modifyBox.addForm(script+"/"+hook+"/"+action_hook+"/"+ext);
									modifyBox.addTag("<table class=\"def\">\n<tr><td>");
									modifyBox.addLabel("Tunnus");
									modifyBox.addTag("</td><td>");
									modifyBox.addTag(user.name);
									modifyBox.addTag("</td></tr>\n<tr><td><h3>Ryhm‰t: </h3></td></tr>\n");

									for(String s : groupdb.getGroupNames()){
										modifyBox.addTag("<tr><td>"+s+"</td><td>"); 
										modifyBox.addTag("<input name=\""+s+"\" type=\"checkbox\" ");
										for(String group : user.groups){
											if(s.equals(group)){
												modifyBox.addTag("checked=checked");
												break;
											}
										}
										modifyBox.addTag("/>\n</td></tr>");
									}
									modifyBox.addTag("</table>");
									modifyBox.addInput(null, "lis‰‰", "submit", null);
									modifyBox.addTag("</form>");

									page.setTitle("K‰ytt‰jien hallinta");
									page.addTop(getMenu());
									page.addCenter(modifyBox.toString());
								}
							}
						}else{
							CmsBoxi failBox = new CmsBoxi("Muokkaa k‰ytt‰j‰‰");
							failBox.addP("hupsi");

							page.setTitle("K‰ytt‰jien hallinta");
							page.addTop(getMenu());
							page.addCenter(failBox.toString());
						}
					}
				}
		);

		actions.add(new Action("Resetoi salasana", "resetpass"){public void execute(){
			CmsBoxi naytaNimiBox = new CmsBoxi("Resetoi salasana");
			naytaNimiBox.addTag("<table class=\"def\">");
			for(String user : UserDb.getNames()){
				naytaNimiBox.addTag(
						"<tr><td>"+user+"</td><td>"+
						"<a class=\"line\" href=\""+
						script+"/"+hook+"/poista/"+user+
						"\">poista</a></td></tr>"
				);
			}
			naytaNimiBox.addTag("</table>");
			naytaNimiBox.addLink("takaisin", script + "/" + hook);

			page.setTitle("K‰ytt‰jien hallinta");
			page.addTop(getMenu());
			page.addCenter(naytaNimiBox.toString());
			page.addCenter(getActionLinks().toString());

		}});

	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}