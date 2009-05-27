package cms.mods;

import html.CmsElement;
import html.FileField;
import html.HiddenField;
import html.PassField;
import html.SubmitField;
import html.TextAreaField;
import http.FormPart;

import java.io.File;

import util.ActionLog;
import util.Hasher;
import cms.Cgicms;
import cms.DataRelay;
import d2o.FlushingFile;
import d2o.UserDb;
import d2o.pages.BinaryFile;
import d2o.pages.CmsFile;
import d2o.pages.PageDb;
import d2o.pages.TextFile;
import d2o.pages.VirtualPath;
import d2o.render.Renderer;

public class ModOwn extends Module{

	public ModOwn(DataRelay datarelay) {
		super(datarelay);
		hook = "oma";
		menu_label = "Omat tiedot";
	}

	public void activate(){
		super.activate();
		actions.add(new Action(null, ""){public void execute(){
			page.setTitle("Omat tiedot");
			page.addTop(getMenu());
			page.addLeft(getActionLinks().toString());
			page.addRight(genBugreport());
		}});

		actions.add(new Action("Oman naamasivun muokkaus", "naama"){public void execute(){
			FlushingFile naamaprofiilit = new FlushingFile(new File(Cgicms.products_dir,"misc.naamat"));
			String[] data = new String[1];
			data = naamaprofiilit.loadAll();

			if(ext == ""){
				page.setTitle("Valitse naama");

				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium4");
				box.addTag("h4", "Valitse naama");
				box.addLayer("div","side5");

				for(String s : data){
					if(s.length()<2)
						continue;
					String[] palat = util.Csv.decode(s);
					if(palat.length < 2){
						box.addTag("pre", "error");
						continue;
					}
					box.addTag("a href=\""+script+"/"+hook+"/"+action_hook+"/"+palat[1]+"\"", "menu", palat[0]);
				}

				page.addTop(getMenu());
				page.addCenter(box);
				page.addLeft(getActionLinks());

			}else{

				page.setTitle("Oma naama");
				page.addTop(getMenu());

				String titteli = null;//"Puheenjohtaja";//datarelay.session.getUser().getInfo("titteli");
				String filename = null;//"naamat_"+titteli.toLowerCase()+".shtml";
				String clean = null;

				for(String s : data){
					if(s.length()<2)
						continue;
					String[] palat = util.Csv.decode(s);
					if(palat.length < 2){
						pagebuilder.addHidden("error["+s+"]");
						continue;
					}
					if(palat[1].equals(ext)){
						titteli = palat[0];
						filename = "naamat_"+palat[1]+".shtml";
						clean = palat[1];
						break;
					}
				}

				if(titteli == null){
					page.addCenter("<pre>error: no profile["+ext+"] found</pre>");
					return;
				}

				PageDb pdb = PageDb.getDb();
				log.info("filename["+filename+"] ");

				VirtualPath naamapath = VirtualPath.create("/"+filename);
				log.info("path["+naamapath.toString()+"] "+naamapath.getFilename());

				TextFile naamasivu;
				if(!pdb.fileExists(naamapath)){
					log.info("creating file stub for "+ext);
					naamasivu = new TextFile(filename);
					naamasivu.content_type = "text/html";
					naamasivu.parent = "oma_naama";
					naamasivu.setData("");

					if(pdb.addFile("/", naamasivu)!=null){
						page.addCenter("<pre>error: could not add file["+filename+"] to db</pre>");
						return;
					}

				}else{
					naamasivu = (TextFile)pdb.getFileMeta(naamapath);
				}
				if(naamasivu == null){
					TextFile uusi = new TextFile(filename);
					uusi.parent = "oma_naama";
					pdb.addFile(naamapath.getPath(), uusi);
				}

				
				

				if(checkField("_save")||checkField("_preview")){
					CmsElement box = new CmsElement();
					do{
						Renderer r = Renderer.getRenderer();
						CmsFile oldfile = pdb.getFileMeta(naamapath);

						box.addLayer("div","boxi");
						box.addTag("h4", "Tallennus");

						log.info("saving file");

						if(oldfile == null){
							box.addTag("pre","Virhe: could not load oldfile");
							break;
						}

						log.info("got oldfile");
						TextFile file = (TextFile)oldfile;
						file.setData(r.genData(datarelay.post, file));
						file.relativePath = naamapath;

						if(!pdb.updateData(file)){
							box.addTag("pre","error - file data could not be updated");
							break;
						}

						log.info("update data["+file.name+"] successfull");
						ActionLog.action("Updated ["+file.relativePath.getUrl()+"]" );
						if(checkField("_preview")){
							pagebuilder.setRedirect(script+"/"+hook+"/esikatsele/"+ext);
						}else{
							pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook);
						}

					}while(false);
					page.addCenter(box);

				}else{
					CmsElement side = new CmsElement();
					side.addLayer("div","boxi inv col");
					side.addLayer("div","boxi medium");
					side.up();
					side.addLink("upload", script + "/" + hook + "/" + action_hook +"?action=upload" );
					//page.addLeft(side);
					
					CmsElement uploadbox = new CmsElement();
					side.addLayer("div","boxi inv col");
					uploadbox.addLayer("div","boxi2 medium");
					uploadbox.addTag("h4","Uploadaa kuvatiedosto");

					uploadbox.addLayer("div", "ingroup filled");
					uploadbox.addLayer("form method=\"post\" action=\"" +
							script + "/" + hook +"/upload/"+ext+"\" enctype=\"multipart/form-data\"");
					uploadbox.addLayer("table","table5 compact");
					uploadbox.addLayer("tr");
//					uploadbox.addTag("td style=\"text-align:right;\"","Tiedosto");
					uploadbox.addLayer("td");
					uploadbox.addField("file", null, true, new FileField());
					uploadbox.up(3);
					uploadbox.addSingle("input value=\"l‰het‰\" type=\"submit\" class=\"list\"");
					uploadbox.up();
//					page.addCenter(uploadbox);
					page.addLeft(uploadbox);

					CmsElement edit = new CmsElement();
					edit.addLayer("div","boxi medium4");
					edit.addTag("h4", titteli);
					edit.addFormTop(script+"/"+hook+"/"+action_hook+"/"+ext);

					Renderer r = Renderer.getRenderer();
					edit.addContent(r.generateEditPage(naamasivu).toString());

					edit.addLayer("table");
					edit.addLayer("tr");
					edit.addTag("td","<input name=\"_save\" value=\"tallenna\" type=\"submit\" class=\"list\"/>");
					edit.addTag("td","<input name=\"_preview\" value=\"esikatsele\" type=\"submit\" class=\"list\"/>");
					edit.up(2);
					edit.addField("titteli", titteli, true, new HiddenField());
					edit.addField("filename", clean, true, new HiddenField());
					edit.addField("_lastmodified", Long.toString(naamasivu.lastModified), true, new HiddenField());

					page.addCenter(edit);
				}
			}
		}});

		actions.add(new Action(null, "esikatsele"){public void execute(){

			if(ext == ""){
				FlushingFile naamaprofiilit = new FlushingFile(new File(Cgicms.products_dir,"misc.naamat"));
				String[] data = new String[1];
				data = naamaprofiilit.loadAll();
				
				page.setTitle("Valitse naama");

				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium4");
				box.addTag("h4", "Valitse naama");
				box.addLayer("div","side5");

				for(String s : data){
					if(s.length()<2)
						continue;
					String[] palat = util.Csv.decode(s);
					if(palat.length < 2){
						box.addTag("pre", "error");
						continue;
					}
					box.addTag("a href=\""+script+"/"+hook+"/"+action_hook+"/"+palat[1]+"\"", "menu", palat[0]);
				}

				page.addTop(getMenu());
				page.addCenter(box);
				page.addLeft(getActionLinks());

			}else{

				PageDb pdb = PageDb.getDb();
//				String titteli = "Puheenjohtaja";//datarelay.session.getUser().getInfo("titteli");
				String filename = "naamat_"+ext+".shtml";
				VirtualPath naamapath = VirtualPath.create("/"+filename);
				TextFile naamasivu = (TextFile)pdb.getFileMeta(naamapath);
				if(naamasivu == null){
					TextFile uusi = new TextFile(filename);
					uusi.parent = "oma_naama";
					pdb.addFile(naamapath.getPath(), uusi);
				}
				Renderer renderer = Renderer.getRenderer();
				renderer.setUrl(script+"/sivut/preview/");
				String data = renderer.generateHtml(naamasivu);

				pagebuilder.rawSend(data);
			}
		}});

		actions.add(new Action("Profiilien m‰‰ritys", "profiilit"){public void execute(){

			FlushingFile naamaprofiilit = new FlushingFile(new File(Cgicms.products_dir,"misc.naamat"));
			String[] data = new String[1];
			if(checkField("naamat")){
				data[0] = datarelay.post.get("naamat");
				naamaprofiilit.overwrite(data);
			}else{
				data = naamaprofiilit.loadAll();
			}

			StringBuilder sb = new StringBuilder();
			for(String s : data){
				sb.append(s).append("\n");
			}

			//String motd = fh.readFile("misc.motd");


			CmsElement box = new CmsElement();

			box.addFormTop(script + "/" + hook + "/" + action_hook);
			box.addLayer("div","boxi2 medium4");
			box.addTag("h4", "profiilit");
			box.addLayer("div","ingroup filled");
			box.addField("naamat", sb.toString(), true, new TextAreaField(400));
			//			box.addSingle("br");
			box.addField("submit", "submit", true, new SubmitField(true));


			page.setTitle("Profiilien m‰‰ritys");
			page.addTop(getMenu());
			page.addLeft(getActionLinks());
			page.addCenter(box);

		}});

		actions.add(new Action("Vaihda salasana", "salasana"){public void execute(){
			if(!datarelay.env.containsKey("HTTPS")){
				pagebuilder.setRedirect("https"+script.substring(4)+"/"+hook+"/"+action_hook);
			}else{
				//CmsBoxi box = new CmsBoxi("Salasanan vaihto", "medium3");
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi2 medium3");
				box.addTag("h4","Salasanan vaihto");
				box.addLayer("div","ingroup filled");
				
				box.addFormTop("https"+script.substring(4)+"/"+hook+"/"+action_hook);
				box.addContent("<table class=\"table5\"><tr><td>Vanha:</td><td>");
				box.addField("vanha", null, true, new PassField(-1));
				box.addContent("</td></tr><tr><td>Uusi:</td><td>");
				box.addField("uusi", null, true, new PassField(-1));
				box.addContent("</td></tr><tr><td>Uudestaan:</td><td>");
				box.addField("uusi2", null, true, new PassField(-1));
				box.addContent("</td></tr></table");
				box.addContent("<input type=\"submit\" value=\"vaihda\" class=\"list\">");
				box.addContent("</form>");				

				if(checkFields(box.getFields())){
					log.info("fields found");

					String virhe_sanoma = "";

					UserDb udb = UserDb.getDb();
					udb.loadDb();

					String salt = udb.getSalt(username);
					String old_hash = Hasher.hashWithSalt(datarelay.post.get("vanha"), salt);
					String current_hash = udb.getPass(username);//datarelay.session.getUser().getPass();

					if(!old_hash.equals(current_hash)){
						virhe_sanoma = "Vanha Salasana v‰‰rin o["+old_hash+"] c["+current_hash+"]";
					}else if(datarelay.post.get("uusi").length() < 4){				
						virhe_sanoma = "Salasanan tulee olla v‰hint‰‰n 4 merkki‰";
					}else if(!datarelay.post.get("uusi").equals(datarelay.post.get("uusi2"))){
						virhe_sanoma = "uudet salasanat eiv‰t t‰sm‰‰";
					}else if(username.equals(datarelay.post.get("uusi"))){
						virhe_sanoma = "Salasana ei voi olla sama kuin tunnus";
					}else{



						if(udb.hasUser(username)){
							//if(!udb.getPass(username).equals(Hasher.hash(datarelay.post.get("vanha")))){
							if(!udb.getPass(username).equals(old_hash)){
								virhe_sanoma = "vanha salasana v‰‰rin "+udb.getPass(username)+" "+old_hash ;
							}else
								if(!udb.changePass(username, datarelay.post.get("uusi"))){
									ActionLog.error("changePass failed -changes");
									virhe_sanoma = "error error *piip* *puup* (user not found)";
								}else
									if(udb.storeDb() != null){
										ActionLog.error("changePass failed -store");
										virhe_sanoma = "could not store database";
									}
						}
						//virhe_sanoma = datarelay.manager.changePass(username,datarelay.post.get("vanha"), datarelay.post.get("uusi"));
					}
					if(!virhe_sanoma.equals("")){
						ActionLog.action(username + " - failure in changing password");

						//CmsBoxi result = new CmsBoxi("Salasanan vaihto ep‰onnistui");
						CmsElement result = new CmsElement();
						result.addLayer("div","boxi2 medium3");
						result.addTag("h4","Salasanan vaihto ep‰onnistui");
						result.addLayer("div","ingroup filled");
						result.addTag("p","Virhe: " + virhe_sanoma);

						page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
						page.addTop(getMenu());
						page.addCenter(result.toString());
						page.addCenter(box.toString());
						//page.addRight(genTalkback().toString());
						//page.addRight(genBugreport().toString());
					}else{
						ActionLog.action(username + " - changed  own password");

						//CmsBoxi result = new CmsBoxi("Salasanan vaihto onnistui");
						CmsElement result = new CmsElement();
						result.addLayer("div","boxi2 medium3");
						result.addTag("h4","Salasanan vaihto onnistui");
						result.addLayer("div","ingroup filled");
						result.addTag("p","Salasana vaihdettu.");
						result.addLink("muut toiminnot", script + "/" + hook );

						page.setTitle("Omat tiedot");
						page.addTop(getMenu());
						page.addCenter(result.toString());
						//page.addRight(genTalkback().toString());
						//page.addRight(genBugreport().toString());
					}
				}else{
					page.setTitle("K‰ytt‰j‰ kohtaiset hommelit");
					page.addTop(getMenu());
					page.addCenter(box.toString());
				}
			}
		}});
		
		actions.add(new Action(null, "upload"){public void execute(){
			VirtualPath path = VirtualPath.create("/res/");
			
			FlushingFile naamaprofiilit = new FlushingFile(new File(Cgicms.products_dir,"misc.naamat"));
			String[] data = new String[1];
			data = naamaprofiilit.loadAll();

			String titteli = null;
			
			for(String s : data){
				if(s.length()<2)
					continue;
				String[] palat = util.Csv.decode(s);
				if(palat.length < 2){
					pagebuilder.addHidden("error["+s+"]");
					continue;
				}
				if(palat[1].equals(ext)){
					titteli = palat[0];
					break;
				}
			}
			
			if(titteli == null){
				page.addCenter("<pre>error: no profile found</pre>");
				return;
			}
			
			if(datarelay.multipart){

				log.info("got multipart");

				CmsElement box = new CmsElement();
				box.addLayer("div", "boxi2 medium4");
				box.addTag("h4", "Tiedoston vastaanotto");
				box.addLayer("div", "ingroup filled");

				StringBuilder sb = new StringBuilder();
				PageDb pdb = PageDb.getDb();

				if(datarelay.files.length==0){
					log.info("files length == 0");
					sb.append("Virhe: Tiedosto yli 10 megaa, tyhj‰, tai jotain muuta");
				}
				

				for(FormPart p: datarelay.files){
					log.info("form part");

					String postfix = p.getFilename().substring(p.getFilename().lastIndexOf(".")+1);
					String filename = "naamat_"+ext+"."+postfix;

	
					CmsFile file;
					if(p.getContentType().startsWith("text")){
						TextFile temp =  new TextFile(filename);

						temp.setData(new String(p.bytes));
						file = temp;
					}else{
						BinaryFile temp =  new BinaryFile(filename);
						temp.setData(p.bytes);
						file = temp;
					}
					file.content_type = p.getContentType();
					pdb.addFile(path.getPath(), file);

				}

				if(sb.length()>0){
					box.addTag("p", sb.toString());
				}else{
					box.addTag("p", "alles ok");
				}
				//box.addTag("a href=\""+script+"/"+hook+"/file/"+path.getPath()+filename+"\"","list","Ok");
				page.setTitle("Tiedoston vastaanotto");
				page.addTop(getMenu());
				page.addCenter(box.toString());
			}else{

				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium3");
				box.addTag("h4","Uploadaa tiedosto");

				box.addLayer("form method=\"post\" action=\"" +
						script + "/" + hook +"/"+action_hook+"/"+ext+
				"\" enctype=\"multipart/form-data\"");


				box.addLayer("table","compact");
				box.addLayer("tr");
				box.addTag("td style=\"text-align:right;\"","Sijainti");
				box.addTag("td","<a class=\"compact\">/"+path.getPath()+"</a>");
				box.up();

				box.addLayer("tr");
				box.addTag("td style=\"text-align:right;\"","Tiedosto");
				box.addLayer("td");
				box.addField("file", null, true, new FileField());
				box.up(2);

				box.up();

				box.addSingle("input value=\"luo\" type=\"submit\" class=\"list\"");
				box.up();

				page.setTitle("Sivuston hallinta - lis‰‰ tiedosto");
				page.addTop(getMenu());
				page.addCenter(box);
			}
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}
}

