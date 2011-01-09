package cms.mods;

import html.CheckBoxField;
import html.CmsElement;
import html.ComboBoxField;
import html.FileField;
import html.HiddenField;
import html.SubmitField;
import html.TextField;
import http.FormPart;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;

import util.ActionLog;
import util.Utils;
import cms.DataRelay;
import cms.FileOps;
import d2o.pages.BinaryFile;
import d2o.pages.CmsFile;
import d2o.pages.IndexRecord;
import d2o.pages.PageDb;
import d2o.pages.TemplateFile;
import d2o.pages.TextFile;
import d2o.pages.Tree;
import d2o.pages.VirtualPath;
import d2o.render.Renderer;

public class ModPages extends Module {

	public ModPages(DataRelay datarelay) {
		super(datarelay);
		hook = "sivut";
		menu_label = "Sivuston hallinta";
	}

	public void activate(){
		super.activate();

		actions.add(new Action(null, ""){ public void execute(){
			PageDb pdb = PageDb.getDb();
			pdb.runDbCheck();
			pagebuilder.setRedirect(script+"/"+hook+"/hallitse/");
		}});

		actions.add(new Action("Hallitse", "hallitse"){	public void execute(){
			page.setTitle("Sivuston hallinta");

			CmsElement side = new CmsElement();
			side.addLayer("div", "boxi inv col");
			PageDb pdb = PageDb.getDb();
			VirtualPath path = VirtualPath.create(ext);

			if(pdb.dirExists(path.getPath())){
				final String link = script+"/"+hook;
				Tree tree = pdb.getFolderTree();

				CmsElement folders = new CmsElement();
				folders.addLayer("div","boxi folder");
				folders.addTag("h4","Navi");
				folders.addLayer("ul","folder");

				folders.addContent(genFolderTreeHtml(tree,path));
				folders.up();
				side.addElement(folders);

				CmsElement temps = new CmsElement();
				temps.addLayer("div", "boxi temp");
				temps.addTag("h4","Mallit");
				temps.addLayer("ul", "temp");

				temps.addContent(genTempHtml(pdb.getTempTree(),script+"/"+hook+"/template"));
				temps.addContent("<hr style=\"background-color:#ccc\" />");
				temps.addContent("<a href=\""+script+"/"+hook+"/addtemp"+"\">[+] lis‰‰ malli</a>");
				temps.up();
				side.addElement(temps);

				final String url_path = "/"+ path.getPath();

				CmsElement folderview = new CmsElement();
				folderview.addLayer("div", "boxi medium3 fview2");
				folderview.addTag("h4","/"+ext);

				folderview.addLayer("table","files");
				folderview.addLayer("tr");
				folderview.addTag("th","Tiedoston nimi");
				folderview.addTag("th","Status");
				folderview.up();

				for(IndexRecord record: pdb.getFileRecords(path.getPath())){
					folderview.addLayer("tr");
					folderview.addTag("td","<a href=\""+link+"/file"+url_path+record.filename+"\">"+
							record.filename+
					"</a>");
					folderview.addTag("td",Character.toString(record.status));
					folderview.up();
				}

				folderview.up();
				folderview.addLink(null, "list", link+"/addfile"+url_path, "[+] add a file" );

				page.addLeft(side);
				page.addCenter(folderview);
			}else{
				CmsElement error = new CmsElement();
				error.addLayer("div","boxi medium");
				error.addTag("h4","Virhe");
				error.addTag("p","Hakemistoa ["+ext+"] ei lˆytynyt");
				error.addLink("Ok",script+"/"+hook+"/"+action_hook+"/");
				page.addCenter(error);
			}
		}});

		actions.add(new Action(null, "addtemp"){public void execute(){
			PageDb pdb = PageDb.getDb();

			CmsElement box = new CmsElement();
			box.addLayer("div","boxi medium");
			box.addTag("h4","Uusi malli");
			box.addFormTop(script + "/" + hook + "/" +action_hook +"/"+ext);

			box.addLayer("table", "compact");
			box.addLayer("tr");
			box.addTag("td style=\"text-align:right;\"","Nimi");
			box.addLayer("td");
			box.addField("filename", null, true, new TextField(20));
			box.up(2);
			box.addLayer("tr");
			box.addTag("td style=\"text-align:right;\"","Parent");
			box.addLayer("td");
			box.addField("parent", null, true, new ComboBoxField(pdb.getTemplateNames(),"[new]"));
			box.up(3);
			box.addField("luo", "luo", false, new SubmitField());

			/** older */

			if(checkFields(box.getFields())){

				String result;
				if((result = pdb.createTemplate(datarelay.post.get("filename"))) == null){
					box = new CmsElement();//CmsBoxi("done","medium3");
					box.addLayer("div","boxi medium3");
					box.addTag("h4","done");
					//TODO: make less horrible
					box.addTag("p","template created");
					if(!datarelay.post.get("parent").contentEquals("[new]")){
						if(pdb.getTemplate(datarelay.post.get("parent"))!=null){
							TemplateFile tfile = pdb.getTemplate(datarelay.post.get("filename"));
							tfile.parent = datarelay.post.get("parent");
							pdb.updateTemplate(tfile);
						}
					}
					box.addLink("continue", script+ "/"+ hook + "/template/"+datarelay.post.get("filename"));
					pagebuilder.setRedirect(script+ "/"+ hook + "/template/"+datarelay.post.get("filename"));
				}else{
					box = new CmsElement();//CmsBoxi("error", "medium");
					box.addLayer("div","boxi medium");
					box.addTag("h4","error");
					box.addTag("p","encountered an error ["+result+"]");
				}

			}else{
				// "no" input -> print default box;
			}

			page.setTitle("Sivuston hallinta - lis‰‰ malli");
			//page.addTop(getMenu());
			page.addCenter(box);
		}});


		actions.add(new Action(null, "tempchangeparent"){public void execute(){
			page.addCenter("not implemented");
		}});

		actions.add(new Action(null, "tempdelete"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);

			//CmsBoxi box = new CmsBoxi("Poistetaan malli ["+path.getFilename()+"]", "medium");
			CmsElement box = new CmsElement();
			box.addLayer("div","boxi medium");
			box.addTag("h4","Poistetaan malli ["+path.getFilename()+"]");
			box.addFormTop(script+"/"+hook+"/"+action_hook +"/"+ path.getFilename());
			box.addContent("Really? ");
			box.addField("really", null, false, new CheckBoxField());
			box.addField("action", "delete", true, new SubmitField(true));
			box.up();
			if(datarelay.post != null && datarelay.post.get("really") != null){
				PageDb pdb = PageDb.getDb();

				box = new CmsElement();//CmsBoxi("Poistetaan malli ["+path.getFilename()+"]", "medium3");
				box.addLayer("div","boxi medium3");
				box.addTag("h4","Poistetaan malli ["+path.getFilename()+"]");
				if(!pdb.removeTemplate(path.getFilename())){
					box.addTag("p","failed");
					box.addLink("Ok", script +"/"+ hook + "/hallitse");
				}
				else{
					box.addTag("p","Success!");
					box.addLink("Ok", script +"/"+ hook + "/hallitse"+path.down().getPath());
					//pagebuilder.setRedirect(script +"/"+ hook + "/hallitse"+path.down().getPath());
				}
			}
			page.addCenter(box);

		}});

		actions.add(new Action(null, "temprename"){public void execute(){
			page.setTitle("Rename [/"+ext+"]");
			//page.addTop(getMenu());

			VirtualPath path = VirtualPath.create(ext);
			PageDb pdb = PageDb.getDb();

			CmsElement box = new CmsElement();
			box.addLayer("div", "boxi medium3");
			box.addTag("h4","Vaihda nime‰");
			box.addFormTop(script + "/" + hook + "/" + action_hook +"/"+ path.getUrl());

			box.addTag("label","Uusi nimi");
			box.addField("uusinimi", path.getFilename(), true, new TextField(27));
			box.addField("ok", "Muuta", true, new SubmitField());

			box.up();
			if(checkFields(box.getFields())){
				if(!pdb.renameTemplate(path.getFilename(), datarelay.post.get("uusinimi"))){
					pagebuilder.addMessage("error: could not rename template");
				}else{
					pagebuilder.setRedirect(script + "/" + hook + "/template/" + path.getPath()+"/"+datarelay.post.get("uusinimi"));
				}
			}
			page.addCenter(box);
		}});

		actions.add(new Action(null, "template"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);

			page.setTitle("Sivuston hallinta - malli ["+path.getFilename()+"]");
			//page.addTop(getMenu());

			if(ext.equals("")){
				CmsElement box = new CmsElement();
				box.addLayer("div", "boxi medium");
				box.addTag("h4","Hmm");
				box.addTag("p","pondering... please wait");
				box.addLink("to the ruleings", script + "/" + hook + "/hallitse");
				page.addCenter(box);

			}else{
				log.info("process the template stuff");
				PageDb pdb = PageDb.getDb();

				Entry<String, String> command = null;
				if(datarelay.query != null && datarelay.query.size() > 0){
					for(Entry<String, String> c: datarelay.query.entrySet()){
						if(
								c.getKey().equals("add") ||
								c.getKey().equals("up") ||
								c.getKey().equals("del")
						){
							command = c;
							break;
						}						
					}
				}

				if(command != null){
					//pass command to renderer
					log.info("dynamic operation ["+datarelay.query.keySet().toArray(new String[0])[0]+"]");
					//CmsBoxi box = new CmsBoxi("dynamic operation");
					CmsElement box = new CmsElement();
					box.addLayer("div","boxi");
					box.addTag("h4","dynamic operation");

					Renderer renderer = Renderer.getRenderer();
					TemplateFile template = pdb.getTemplate(path.getFilename());


					if(template != null){
						log.info("got oldfile");
						template.setData(renderer.dynData(command.getKey(), command.getValue(), template));
						log.info("oldfile.data " +(template.data != null));

						if(pdb.updateTemplate(template)){
							ActionLog.action("Updated t["+path.getUrl()+"]" );
							pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl(true));
						}else{
							box.addTag("pre","error - file data could not be updated");
							page.addCenter(box);
						}
					}

				}else if(checkField("_lastmodified")){ // or _save
					Renderer r = Renderer.getRenderer();
					TemplateFile oldfile = pdb.getTemplate(path.getFilename());

					CmsElement box = new CmsElement();
					box.addLayer("div","boxi");
					box.addTag("h4","Tallennus");

					log.info((""));

					if(oldfile != null){
						log.info("got oldfile");

						if(checkFields(r.getFields(oldfile)))
							log.info("saving template");

						oldfile.setData(r.postToCmsData(datarelay.post, oldfile));
						log.info("oldfile.data" +(oldfile.data != null));

						if(pdb.updateTemplate(oldfile)){
							ActionLog.action("Updated t["+path.getUrl()+"]" );
							pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl(true));
						}else{
							box.addTag("pre","error - file data could not be updated");
						}

					}else{
						box.addTag("pre","nulllll, could not load oldfile");
					}
					page.addCenter(box.toString());
				}else{ //command == null

					log.info("displaying template ["+path.getFilename()+"]");

					if(path.length() > 0){

						TemplateFile tfile = pdb.getTemplate(path.getFilename());

						if(tfile == null){
							CmsElement box = new CmsElement();
							box.addLayer("div","boxi");
							box.addTag("h4","Virhe");
							box.addTag("pre","could not load file ["+path.getFilename()+"]");
							page.addCenter(box.toString());
						}else{

							CmsElement box = new CmsElement();
							box.addLayer("div", "boxi inv col");

							box.addLayer("div", "boxi medium");
							box.addTag("h4", "Ominaisuudet");
							box.addLayer("table", "compact");
							box.addSingle("colgroup width=\"33\"");
							box.addSingle("colgroup");
							box.addLayer("tr");
							box.addTag("td style=\"text-align:right;\"", "nimi");
							box.addTag("td", "<a class=\"compact select \" href=\"" +
									script + "/" + hook + "/temprename"+ path.getUrl(true) +
									"\">"+tfile.name+"</a>");
							box.up();
							box.addLayer("tr");
							box.addTag("td style=\"text-align:right;\"", "malli");
							box.addTag("td", "<a class=\"compact select \" href=\"" +
									script + "/" + hook + "/tempchangeparent" + path.getUrl(true) +
									"\">"+(tfile.parent==null ? "none" : tfile.parent)+"</a>");
							box.up(2);

							box.addLink("upload", script + "/" + hook + "/tempupload" + path.getUrl(true));
							box.addLink("poista", script + "/" + hook + "/tempdelete" + path.getUrl(true));
							box.addSingle("hr");
							box.addLink("hallintaan", script + "/" + hook + "/hallitse"+path.getPath());

							page.addLeft(box);

							CmsElement edit = new CmsElement();
							edit.addLayer("div style=\"margin-left:50px;\"","boxi2");
							edit.addTag("h4", "<span style=\"font-weight:normal;color:#000\">"+path.getPath() +"</span>"+path.getFilename());

							edit.addFormTop(script+"/"+hook+"/"+action_hook+path.getUrl(true));

							/** so, in here we would make it so that the data is interpreted
							 *  and turned into proper elements on the page.
							 */
							Renderer r = Renderer.getRenderer();
							edit.addContent(r.generateEditPage(tfile).toString());

							edit.addLayer("table");
							edit.addLayer("tr");

							edit.addTag("td","<input name=\"_save\" value=\"tallenna\" type=\"submit\" class=\"list\"/>");
							edit.addTag("td","<input name=\"_preview\" value=\"esikatsele\" type=\"submit\" class=\"list\"/>");
							edit.up(2);
							edit.addField("_lastmodified", Long.toString(tfile.lastModified), true, new HiddenField());

							page.addCenter(edit);

						}
					}else{
						pagebuilder.addMessage("parts == 0 ["+path.getUrl()+"]");
					}
				}
			}
		}});

		actions.add(new Action(null, "delete"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);
			PageDb pdb = PageDb.getDb();

			if(datarelay.query.containsKey("type")){
				String type = datarelay.query.get("type");
				if(type.equals("directory")){

					CmsElement box = new CmsElement();
					box.addLayer("div","boxi medium");
					box.addTag("h4","Poistetaan kansio [/"+path.getPath()+"]");
					box.addFormTop(script+"/"+hook+"/"+action_hook+"/"+path.getPath()+"?type=directory");
					box.addContent("Really? ");
					box.addField("really", null, false, new CheckBoxField());
					box.addField("action", "delete", true, new SubmitField(true));
					box.up();
					if(datarelay.post != null && datarelay.post.get("really") != null){
						box = new CmsElement();//new CmsBoxi("Poistetaan kansio [/"+ext+"]", "medium3");
						box.addLayer("div","boxi medium3");
						box.addTag("h4","Poistetaan kansio [/"+path.getPath()+"]");

						if(!pdb.removeDir(path.getPath())){
							box.addTag("p","could not remove dir");
							box.addLink("Ok", script +"/"+ hook + "/hallitse");
							page.addCenter(box);
							return;
						}
						if(!pdb.store()){
							box.addTag("p","could not store db");
							box.addLink("Ok", script +"/"+ hook + "/hallitse");
							page.addCenter(box);
							return;
						}
						box.addTag("p","Success!");
						box.addLink("Ok", script +"/"+ hook + "/hallitse/"+path.down().getPath());
						pagebuilder.setRedirect(script +"/"+ hook + "/hallitse/"+path.down().getPath());
					}
					page.addCenter(box);

					return;
				}
			}

		}});


		actions.add(new Action(null, "rename"){public void execute(){
			page.setTitle("rename file - "+ext);
			VirtualPath path = VirtualPath.create(ext);
			PageDb pdb = PageDb.getDb();

			if(datarelay.query.containsKey("type")){
				String type = datarelay.query.get("type");
				if(type.equals("directory")){
					//TODO:rename directory
				}
			}

			if(path.length() > 0){

				if(!pdb.fileExists(path)){
					CmsElement box = new CmsElement();
					box.addLayer("div","boxi");
					box.addTag("h4", "Virhe");

					box.addTag("pre","file not found");
					page.addCenter(box);

				}else{
					CmsFile file = pdb.getFileMeta(path);
					CmsElement box = new CmsElement();
					box.createBox("Vaihda nime‰", "medium3");
					box.addFormTop(script + "/" + hook + "/" + action_hook +"/"+ path.getUrl());

					box.addTag("label","Uusi nimi");
					box.addField("uusinimi", file.name, true, new TextField(27));
					box.addField("ok", "Muuta", true, new SubmitField(true));

					if(checkFields(box.getFields())){
						String result;
						if((result = pdb.rename(path, datarelay.post.get("uusinimi"))) != null){
							pagebuilder.addMessage("error: "+result);
						}else{
							pagebuilder.setRedirect(script + "/" + hook + "/" + "file/" + path.getPath()+"/"+datarelay.post.get("uusinimi"));
						}
					}else{
						page.addCenter(box);
					}
				}
			}
		}});

		actions.add(new Action(null, "file"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);

			page.setTitle("Sivuston hallinta - tiedosto ["+path.getUrl()+"]");
			//page.addTop(getMenu());

			if(ext.equals("")){
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium");
				box.addTag("h4", "Hmm");
				box.addTag("p","No file specified.");
				box.addLink("Hallintaan", script + "/" + hook + "/hallitse");
				page.addCenter(box);
				return;
			}

			log.info("process the file stuff");
			PageDb pdb = PageDb.getDb();

			Entry<String, String> command = null;
			if(datarelay.query != null && datarelay.query.size() > 0){
				for(Entry<String, String> c: datarelay.query.entrySet()){
					if(
							c.getKey().equals("add") ||
							c.getKey().equals("up") ||
							c.getKey().equals("del")
					){
						command = c;
						break;
					}						
				}
			}

			if(command != null){
				//pass command to renderer
				log.info("dynamic operation ["+datarelay.query.keySet().toArray(new String[0])[0]+"]");
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi");
				box.addTag("h4", "dynamic operation");

				Renderer r = Renderer.getRenderer();

				CmsFile genericfile = pdb.getFileMeta(path);

				if(genericfile != null){
					log.info("got genericfile");
					TextFile file = (TextFile)genericfile;
					file.setData(r.dynData(command.getKey(), command.getValue(), file));
					//log.info("ile.data " +(file.data != null));
					log.info("got datas:");
					//log.info(Arrays.toString(file.data));

					if(pdb.updateData(file)){
						ActionLog.action("Updated t["+path.getUrl()+"]" );
						pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl(true));
					}else{
						box.addTag("pre","error - file data could not be updated");
						page.addCenter(box.toString());
					}
				}
				return;

			}
			if(checkField("_save")){
				Renderer r = Renderer.getRenderer();
				CmsFile oldfile = pdb.getFileMeta(path);
				//if(checkFields(r.getFields(oldfile)))
				//	log.info("saving file");

				CmsElement box = new CmsElement();
				box.addLayer("div","boxi");
				box.addTag("h4", "Tallennus");

				log.info((""));

				if(oldfile != null){
					//oldfile.datasource.endRead(oldfile.bin);
					log.info("got oldfile");
					TextFile file = (TextFile)oldfile;
					log.info("setting data");
					file.setData(r.postToCmsData(datarelay.post, file));

					file.relativePath = path;
					if(pdb.updateData(file)){
						log.info("update data["+file.name+"] successfull");
						ActionLog.action("Updated ["+file.relativePath.getUrl()+"]" );
						pdb.setStatus(path, 'm');
						pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl());
					}else{
						box.addTag("pre","error - file data could not be updated");
					}

				}else{
					box.addTag("pre","nulllll, could not load oldfile");
				}
				page.addCenter(box.toString());
				return;

			}

			CmsFile file = pdb.getFileMeta(path);

			if(file == null){
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi");
				box.addTag("h4", "Virhe");
				box.addTag("pre","file not found ( file == null )");
				page.addCenter(box.toString());
				return;
			}

			if(datarelay.query.containsKey("action")){
				String action = datarelay.query.get("action");

				CmsElement box = new CmsElement();
				box.addLayer("div","boxi");

				if(action.equals("settxt")){
					file.type = CmsFile.Type.TEXT;
					pdb.updateMeta(file);
					pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl());
				}else if(action.equals("setres")){
					file.type = CmsFile.Type.BINARY;
					pdb.updateMeta(file);
					pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl());
				}else if(action.equals("setdum")){
					file.type = CmsFile.Type.DUMMY;
					pdb.updateMeta(file);
					pagebuilder.setRedirect(script+"/"+hook+"/"+action_hook+path.getUrl());
				}else if(action.equals("upload")){
					//TODO:file internal content upload
					box = new CmsElement();
					box.addLayer("div","boxi medium");
					box.addTag("h4", "L‰het‰ tiedosto");
					box.addFormTop(script + "/" + hook + "/" + action_hook + path.getUrl()+
					"?action=upload");
					box.up();
				}else if(action.equals("delete")){
					box = new CmsElement();
					box.addLayer("div","boxi medium");
					box.addTag("h4", "Tuhoa tiedosto");
					box.addFormTop(script + "/" + hook + "/" + action_hook + path.getUrl(true)+ "?action=delete");
					box.addField("confirm", null, true, new CheckBoxField());
					box.addField("sub", "confirm", false, new SubmitField());
					if(checkField("confirm")){
						pdb.removeFile(path.getPath(), file.name);
						pagebuilder.setRedirect(script+"/"+hook+"/hallitse/"+path.getPath());
					}
				}else if(action.equals("content")){
					box = new CmsElement();
					box.addLayer("div","boxi2 medium3");
					box.addTag("h4", "Vaihda mime-type‰");
					box.addLayer("div","ingroup filled");
					box.addFormTop(script + "/" + hook + "/" + action_hook + path.getUrl(true)+ "?action=content");
					box.addField("content_type", file.content_type, true, new TextField());
					box.addField("sub", "tallenna", false, new SubmitField(true));
					if(checkField("content_type")){
						file.content_type = datarelay.post.get("content_type");
						pdb.updateMeta(file);
						pagebuilder.setRedirect(script + "/" + hook + "/" + action_hook +path.getUrl());
					}
				}else if(action.equals("parent")){
					box = new CmsElement();
					box.addLayer("div","boxi2 medium3");
					box.addTag("h4", "vaihda mallia");
					box.addLayer("div","ingroup filled");
					box.addFormTop(script + "/" + hook + "/" + action_hook + path.getUrl(true)+ "?action=parent");
					box.addField("parent", file.content_type, true, new ComboBoxField(pdb.getTemplateNames(),(file.parent != null ? file.parent:"none"),"none"));
					box.addField("sub", "tallenna", false, new SubmitField(true));
					if(checkField("parent")){
						file.parent = datarelay.post.get("parent");
						pdb.updateMeta(file);
						pagebuilder.setRedirect(script + "/" + hook + "/" + action_hook +path.getUrl());
					}
				}else {
					box = new CmsElement();
					box.addLayer("div","boxi medium");
					box.addTag("h4", "error, unknown action");
				}
				page.addCenter(box.toString());
				return;
			}


			CmsElement side = new CmsElement();
			side.addLayer("div","boxi inv col");
			side.addLayer("div style=\"margin:0 0 10px;\"","boxi2 medium");
			side.addTag("h4", "Ominaisuudet");
			side.addLayer("div","ingroup filled");
			side.addLayer("table","compact");
			side.addSingle("colgroup width=\"33\"");
			side.addSingle("colgroup");
			side.addLayer("tr");
			side.addTag("td style=\"text-align:right;\"","nimi");
			side.addTag(
					"td","<a class=\"compact select \" href=\"" +
					script + "/" + hook + "/" + "rename" +    path.getUrl(true)+
					"\">"+file.name+"</a>");
			side.up();

			if(file.type.equals(CmsFile.Type.TEXT)){
				side.addLayer("tr");
				side.addTag("td style=\"text-align:right;\"","<a href=\""+script+"/"+hook+"/template/"+file.parent+"\">malli</a>");
				side.addTag(
						"td","<a class=\"compact select \" href=\"" +
						script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=parent" +
						"\">"+(file.parent==null ? "none" : file.parent)+"</a>"
				);
				side.up();
			}

			side.addLayer("tr");
			side.addTag("td style=\"text-align:right;\"","sijainti");
			side.addTag("td","<a class=\"compact select \" href=\"" +
					script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=move" +
					"\">/"+file.relativePath.getPath()+"</a>");
			side.up();

			side.addLayer("tr");
			side.addTag("td style=\"text-align:right;\"","content");
			side.addTag("td","<a class=\"compact select \" href=\"" +
					script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=content" +
					"\">"+file.content_type+"</a>");
			side.up();


			side.addLayer("tr");
			side.addTag("td style=\"text-align:right;line-height:1.8;\"","tyyppi");
			side.addLayer("td");
			side.addContent("<a class=\"compact inline " +
					(file.type.equals(CmsFile.Type.TEXT) ?
							"selected\"" : "select\" href=\"" +
							script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=settxt" +
					"\"")+">txt</a>");
			side.addContent("<a class=\"compact inline " +
					(file.type.equals(CmsFile.Type.BINARY) ?
							"selected\"" : "select\" href=\"" +
							script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=setres" +
					"\"")+">bin</a>");
			side.addContent("<a class=\"compact inline " +
					(file.type.equals(CmsFile.Type.DUMMY) ?
							"selected\"" : "select\" href=\"" +
							script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=setdum" +
					"\"")+">dum</a>");
			side.up();

			side.up(3);
			side.addLayer("div","ingroup filled");
			side.addLink("upload", script + "/" + hook + "/" + action_hook +  path.getUrl(true)+ "?action=upload" );
			side.addLink("poista", script + "/" + hook + "/" + action_hook +   path.getUrl(true)+ "?action=delete" );
			side.up();
			side.addLayer("div","ingroup filled");
			side.addLink("preview", script + "/" + hook + "/preview" +  path.getUrl(true) );
			side.addLink("publish", script + "/" + hook + "/render"  +   path.getUrl(true) );

			side.up();
			side.addLayer("div","ingroup filled");
			side.addLink("hallintaan", script + "/" + hook + "/hallitse/"+ path.getPath());
			page.addLeft(side);


			CmsElement edit = new CmsElement();
			edit.addLayer("div style=\"margin-left:50px\"","boxi2");
			edit.addTag("h4", "<span style=\"font-weight:normal;color:#000\">"+ path.getPath() +"</span>"+path.getFilename());
			edit.addFormTop(script+"/"+hook+"/"+action_hook+ path.getUrl(true));

			if(file.type.equals(CmsFile.Type.TEXT)){

				Renderer r = Renderer.getRenderer();
				edit.addContent(r.generateEditPage(file).toString());

			}else if(file.type.equals(CmsFile.Type.BINARY)){
				if(file.content_type.startsWith("image")){
					edit.addSingle("img src=\""+script+"/"+hook+"/preview"+path.getUrl()+"\" style=\"background-color:#999\"");
				}else{
					edit.addLayer("div","ingroup filled");
					edit.addLink("Download", script+"/"+hook+"/download"+path.getUrl());
				}
			}
			if(file.type.equals(CmsFile.Type.TEXT)){
				edit.addLayer("table");
				edit.addLayer("tr");
				edit.addTag("td","<input name=\"_save\" value=\"tallenna\" type=\"submit\" class=\"list\"/>");
				//				edit.addTag("td","<input name=\"_preview\" value=\"esikatsele\" type=\"submit\" class=\"list\"/>");
				edit.up(2);
				edit.addField("_lastmodified", Long.toString(file.lastModified), true, new HiddenField());
			}

			page.addCenter(edit);


		}});

		actions.add(new Action(null, "folder"){	public void execute(){
			page.setTitle("Sivuston hallinta - kansio [/"+ext+"]");
			//page.addTop(getMenu());

			if(ext.equals("")){
				//CmsBoxi box = new CmsBoxi("Hmm", "medium");
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium");
				box.addTag("h4","hmm");
				box.addTag("p","t‰‰ olis se juuri kansio [/]");
				box.addTag("p","mut sit‰ ei voi poistaa :P");
				page.addCenter(box);
			}else{
				PageDb pdb = PageDb.getDb();
				//CmsBoxi box = new CmsBoxi("Muokkaa kansiota [/"+ext+"]", "medium");
				CmsElement box = new CmsElement();
				box.addLayer("div","boxi medium");
				box.addTag("h4","Muokkaa kansiota [/"+ext+"]");

				if(pdb.dirExists(ext)){
					VirtualPath path = VirtualPath.create(ext);
					box.addLink("delete", script +"/"+ hook + "/delete/"+path.getPath()+"?type=directory");

				}else{
					box.addTag("p","no folder ["+ext+"] found");
					box.addLink("Clean dir tree", script +"/"+ hook + "/cleandirs");
					box.addLink("Ok", script +"/"+ hook + "/hallitse");
				}
				page.addCenter(box);
			}
		}});

		actions.add(new Action(null, "addfold"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);
			CmsElement box = new CmsElement();
			box.addLayer("div","boxi2 medium3");
			//box.addTag("h4","Lis‰‰ kansio");
			box.addTag("h4","Lis‰‰ kansioon <span style=\"color:black;font-weight:normal;\">/"+path.getPath()+"</span>");

			box.addLayer("div","ingroup filled");

			if(checkField("folder_name")){
				//checks
				ArrayList<String> results = new ArrayList<String>();
				PageDb pdb = PageDb.getDb();
				String folder = datarelay.post.get("folder_name");
				if(checkFolder(folder)){
					if(pdb.dirExists(path.getPath())){
//						String result;
						if(!pdb.createDir(path.getPath(), folder)){
							box.addTag("p", "could not create directory");
							page.addCenter(box);
							return;
						}
						if(!pdb.store()){
							box.addTag("p", "could not store db");
							page.addCenter(box);
							return;
						}
					}else{
						results.add("invalid parent directory");
					}
				}else{
					results.add("invalid folder name ["+datarelay.post.get("folder_name")+"]");
				}

				if(results.size() == 0){
					pagebuilder.setRedirect( script + "/" + hook +"/hallitse/"+path.getPath()+folder);
				}else{
					box.addTag("p","Kansion lis‰ys ep‰onnistui:");

					for(String s: results)
						box.addTag("pre",s);
					box.addLink("puhdista kansiot", script + "/" + hook + "/cleandirs");
				}
			}else {
				//box = new CmsBoxi("Lis‰‰ kansio");
				box.addFormTop(script + "/" + hook + "/" + action_hook + "/" + path.getPath());
				box.addLayer("table","table5 compact");
				box.addLayer("tr");
				box.addTag("td style=\"text-align:right;\"","Uusi kansio:");
				box.addLayer("td");
				box.addField("folder_name", null, true, new TextField(20));
				box.up(3);

				box.addSingle("input class=\"list\" type=\"submit\" value=\"lis‰‰\"");
			}
			page.setTitle("Sivuston hallinta - lis‰‰ kansio");
			//page.addTop(getMenu());
			page.addCenter(box);
		}});


		actions.add(new Action(null, "addfile"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);

			PageDb pdb = PageDb.getDb();

			CmsElement box = new CmsElement();
			box.addLayer("div","boxi2 medium3");
			box.addTag("h4","Uusi tiedosto <span style=\"color:black;font-weight:normal;\">/"+path.getPath()+"</span>");
			box.addFormTop(script + "/" + hook + "/" +action_hook +"/"+ext+"?"+Utils.mapToQueryString(datarelay.query));
			box.addLayer("div", "ingroup filled");
			box.addLayer("table","table5 compact");
			box.addLayer("tr");
			box.addTag("td style=\"text-align:right;\"","Nimi");
			box.addLayer("td");
			box.addField("filename", null, true, new TextField(20));
			box.up(2);
			box.addLayer("tr");
			box.addTag("td style=\"text-align:right;\"","Template");
			box.addLayer("td");
			box.addField("temp", null, true,new ComboBoxField(pdb.getTemplateNames(),"-"));
			box.up(3);
			box.addSingle("input value=\"luo\" type=\"submit\" class=\"list\"");
			box.up(2);

			if(checkFields(box.getFields())){
				if(checkFilename(datarelay.post.get("filename"))){
					TextFile file = new TextFile(datarelay.post.get("filename"));

					String temp = datarelay.post.get("temp");
					if((temp!=null) && !temp.equals("-")){
						file.parent = temp;
					}else{
						log.info("no parent assigned");
					}

					file.setData("");

					String result = "";
					if(pdb.addFile(ext, file)){
						pagebuilder.setRedirect(script +"/"+ hook + "/file/"+path.getPath()+file.name);

					}else{
						box = new CmsElement();
						box.addLayer("div","boxi medium");
						box.addTag("h4","errors o_O");
						box.addTag("pre",result);
					}

				}
			}else{
				pagebuilder.addHidden(" errors in fields");
			}
			page.setTitle("Sivuston hallinta - lis‰‰ tiedosto");
			//page.addTop(getMenu());
			page.addCenter(box);

			CmsElement uploadbox = new CmsElement();
			uploadbox.addLayer("div","boxi2 medium3");
			uploadbox.addTag("h4","Uploadaa tiedosto");

			uploadbox.addLayer("div", "ingroup filled");
			uploadbox.addLayer("form method=\"post\" action=\"" +
					script + "/" + hook +"/upload/"+ext+"\" enctype=\"multipart/form-data\"");
			uploadbox.addLayer("table","table5 compact");
			uploadbox.addLayer("tr");
			uploadbox.addTag("td style=\"text-align:right;\"","Tiedosto");
			uploadbox.addLayer("td");
			uploadbox.addField("file", null, true, new FileField());
			uploadbox.up(3);
			uploadbox.addSingle("input value=\"l‰het‰\" type=\"submit\" class=\"list\"");
			uploadbox.up();
			page.addCenter(uploadbox);
		}});

		actions.add(new Action(null, "preview"){public void execute(){

			VirtualPath path = VirtualPath.create(ext);
			PageDb pdb = PageDb.getDb();

			// check path
			// get file
			CmsFile file = pdb.getFileMeta(path);

			if(file == null){
				// error;
				log.fail("could not acuire file for preview :(");
				return;
			}

			// according to type,
			if(file.type.equals(CmsFile.Type.BINARY)){
				//  binary
				BinaryFile rfile = (BinaryFile)file;
				//rfile.getData();

				log.info("BINARY file preview");
				StringBuilder sb = new StringBuilder("Content-Type: "+rfile.content_type);
				sb.append('\n');
				sb.append('\n');
				sb.append(rfile.getData());

				pagebuilder.rawSend(sb.toString());


			}else if(file.type.equals(CmsFile.Type.TEXT)){
				//  pagefile
				//   render der der

				Renderer renderer = Renderer.getRenderer();
				renderer.setUrl(script+"/"+hook+"/preview/");
				String data = renderer.generateHtml((TextFile)file);
				data = data.replace("</html>", "" +
						"<div style=\"" +
						"border:2px solid red;" +
						"background-color:white;"+//#f3e4a0;" +
						"position:fixed;" +
						"top:20px;" +
						"left:20px;" +
						//						"width:50px;" +
						//						"height:30px;" +
						"padding:4px;" +
						"\">" +
						"" +
						"<a style=\"" +
						"color:blue;" +
						"\"" +						
						"href=\""+script+"/"+hook+"/file/"+ext+"\">&#187;back</a>" +
						"" +
						"" +
				"</html>");
				pagebuilder.rawSend(data);
			}

		}});


		actions.add(new Action(null, "render"){
			public void execute(){
								
				if(ext.length()==0){
					CmsElement box = new CmsElement();
					box.createBox("Render all pages");
					box.addFormTop(script+"/"+hook+"/"+action_hook);
					box.addTag("label", "force rendering of all pages");
					box.addField("force", "force", false, new CheckBoxField());
					box.addField("render", "render", true, new SubmitField());
					
					
					if(checkFields(box.getFields())){
						log.info("render all");
						page.setTitle("Rendering all pages");
						renderDir("/", checkField("force"));
					}else {
						page.addCenter(box);
					}
					

				}else{
					renderFile(ext, true);
				}
			}
		
			private void renderDir(String _path, boolean force){
				VirtualPath path = VirtualPath.create(_path);
				log.info("rendering dir ["+path.getUrl()+"] ("+_path+")");
				
				PageDb pdb = PageDb.getDb();

				String[] files = pdb.getFileNameList(path.getUrl());
				page.addCenter("<h5>"+path.getUrl()+"</h5>");
				page.addCenter("<pre>\n");
				for(String filename : files){
					page.addCenter(filename +" ");
					page.addCenter((renderFile(path.getUrl()+"/"+filename, force)?"..ok":"..fail"));
					page.addCenter("\n");
				}
				page.addCenter("</pre>");

				String[] dirs = pdb.getDirList(path.getUrl()+"/");
				for(String dir: dirs){
					renderDir(path.getUrl()+"/"+dir, force);
				}

			}

			private boolean renderFile(String ext, boolean force) {
				log.info("rendering file ["+ext+"]");
				VirtualPath path = VirtualPath.create(ext);
				PageDb pdb = PageDb.getDb();

				CmsFile file = pdb.getFileMeta(path);

				if(file == null){
					log.fail("could not acuire file for preview :(");
					return false;
				}

				if(!force && pdb.getStatus(path) == '.'){
					page.addCenter("..no changes");
					return true;
				}
				
				// according to type,
				if(file.type.equals(CmsFile.Type.BINARY)){
					//copy file content to target
					try{
						File target_path = new File(datarelay.target,path.getPath());

						if(!target_path.exists())
							target_path.mkdirs();

						File target = new File(target_path,file.name);
						BinaryFile rfile = (BinaryFile)file;

						//if(!FileOps.write(target, rfile.getData(),false)){
						if(!FileOps.copy(rfile.datasource.getFile(), target, true)){
							page.addCenter("..could not write the file");
							pdb.setStatus(path,'o');
							return false;
						}else{
							pdb.setStatus(path,'.');
							//page.addCenter("ok");
							return true;
						}
					}catch (Exception e){
						pdb.setStatus(path,'x');
						page.addCenter("exception:"+e);
						return false;
					}


				}else if(file.type.equals(CmsFile.Type.TEXT)){
					//log.info("pagefile");
										
					File target_path = new File(datarelay.target,path.getPath());
					if(!target_path.exists())
						target_path.mkdirs();
					File target = new File(target_path,file.name);
					
					Renderer renderer = Renderer.getRenderer();

					String data = renderer.generateHtml((TextFile)file,path.getPath());
					if(data == null){
						page.addCenter("data == null");
						return false;
					}
						
					String[] ds = new String[1];
					ds[0] = data;
					//File target_path = new File(datarelay.target);
					if(!FileOps.write(target, ds , false)){
						page.addCenter("could not write to target");
						return false;
					}
					
					pdb.setStatus(path,'.');
					pdb.saveRendered(path);
					
					//page.addCenter("ok");
					return true;
				}
				return false;
			}
			
		});

		actions.add(new Action("Puhdista", "cleandirs"){public void execute(){
			PageDb pdb = PageDb.getDb();
			pdb.runDirCleaner();
			pdb.runTempCleaner();
			pdb.runPageCleaner();
			pdb.store();
			pagebuilder.setRedirect(script+"/"+hook);
		}});
		
		actions.add(new Action("Rebuild", "rebuild"){public void execute(){
			PageDb pdb = PageDb.getDb();
			pdb.deleteIndexes();
			pagebuilder.setRedirect(script+"/"+hook+"/cleandirs/");
		}});
		
		actions.add(new Action("Revert to last rendered", "revert"){public void execute(){
			PageDb pdb = PageDb.getDb();
			pdb.revertChanges();
			//pagebuilder.setRedirect(script+"/"+hook+"/cleandirs/");
			pagebuilder.setRedirect(script+"/"+hook);
		}});
		
		actions.add(new Action("Scan target", "scan"){public void execute(){
			
			CmsElement box = new CmsElement();
			box.createBox("target looks like this");
			
			File target_dir = new File(datarelay.target);
			
			ArrayList<File> root_list = new ArrayList<File>(Arrays.asList(target_dir.listFiles()));
			Collections.sort(root_list);
			
			
			PageDb pdb = PageDb.getDb();
			
			box.addLayer("pre style=\"font-size:12.5px\"");
			
			for(File f : root_list){
				if(f.isFile()){
					box.addContent(
							(pdb.fileExists(
									VirtualPath.create("/"+f.getName())
									)?"+ ":"- ")+f.getName()+"\n");
				}else {
					
					box.addContent("d"+
							(pdb.dirExists(
									"/"+f.getName()
									)?"+ ":"- ")+f.getName()+"\n");
		
				}
			}
			page.addCenter(box);
			page.addLeft(getActionLinks());
		}});
		

		actions.add(new Action(null, "upload"){public void execute(){
			VirtualPath path = VirtualPath.create(ext);

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
				String filename = "";
				for(FormPart p: datarelay.files){
					log.info("form part");

					filename = p.getFilename();
					String prefix = "page.data.";

					HashSet<String> files = new HashSet<String>(Arrays.asList(pdb.getDirList(path.getPath())));

					boolean done = true;

					if(files.contains(prefix+filename)){
						done = false;
						for(int i = 0; i< 100; i++){
							if(files.contains(prefix+filename+"_"+i))
								continue;
							done = true;
							p.setName(prefix+filename+"_"+i);
							break;
						}
					}
					if(!done){
						log.fail("could not find a suitable filename");
						break;
					}

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
					pagebuilder.setRedirect(script+"/"+hook+"/file/"+path.getPath()+filename);
				}
				//box.addTag("a href=\""+script+"/"+hook+"/file/"+path.getPath()+filename+"\"","list","Ok");
				page.setTitle("Tiedoston vastaanotto");
				//page.addTop(getMenu());
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
				//page.addTop(getMenu());
				page.addCenter(box);
			}
		}});
	}

	protected boolean checkFolder(String string) {
		char c;
		boolean dot = false;
		for (int i = 0; i < string.length(); i++) {
			c = string.charAt(i);
			log.info("c[" + c + "]");

			if (Character.isLetter(c))
				continue;
			log.info(" !letter");

			if (Character.isDigit(c))
				continue;
			log.info(" !digit");

			if (c == '-')
				continue;
			log.info(" !-");

			if (c == '_')
				continue;
			log.info(" !_");

			if (c == '.') {
				dot = true;
				continue;
			}

			log.info(" !.");

			return false;
		}

		if (dot) {
			if (string.contains(".."))
				return false;
		}

		return true;
	}

	private String genFolderTreeHtml(Tree tree, VirtualPath vpath) {
		StringBuilder sb = new StringBuilder();
		vpath = new VirtualPath(vpath);
		if(vpath.length()==0){
			sb.append("<table style=\"margin-top:2px;\"><tr class=\"selected\">" +
					"<td><li>root</li></td>" +
					"<td width=\"26px\"><a href=\""+script+"/"+hook+"/addfold/"+"\" class=\"but2\" title=\"new dir\">+</a></td>" +
					//"<td width=\"26px\"><a href=\"#\" class=\"but2\" title=\"delete\">-</a></td>" +
			"</tr></table>\n");
		}else{
			sb.append("<a href=\""+script+"/"+hook+"/hallitse/"+"\">root</a>\n");
		}

		if(tree.hasChildren()){
			sb.append("<ul>\n");
			for(String childname: tree.list()){
				Tree child = tree.getChild(childname);
				sb.append(genFolderTreeHtml2(child, vpath, "/"));
			}
			sb.append("</ul>\n");
		}
		return sb.toString();
	}

	private String genFolderTreeHtml2(Tree tree, VirtualPath vpath, String link) {
		StringBuilder sb = new StringBuilder();
		link = link+Utils.urlEncode(tree.name)+"/";

		if(vpath != null){
			if(tree.name.contentEquals(vpath.getRootName())){
				if(vpath.length()<2){
					sb.append(
							"<table><tr class=\"selected\">" +
							"<td><li>"+tree.name+"</li></td>" +
							"<td width=\"26px\"><a href=\""+script+"/"+hook+"/addfold"+link+"\" class=\"but2\" title=\"new dir\">+</a></td>" +
							"<td width=\"26px\"><a href=\""+script+"/"+hook+"/delete"+link+"?type=directory\" class=\"but2\" title=\"delete\">-</a></td>" +
							"</tr></table>\n"
					);
				}else{
					sb.append("<a href=\""+script+"/"+hook+"/hallitse"+link+"\">"+tree.name+"</a>\n");
				}
				vpath.up();
			}else{
				sb.append("<a href=\""+script+"/"+hook+"/hallitse"+link+"\">"+tree.name+"</a>\n");
			}
		}else{
			sb.append("<a href=\""+script+"/"+hook+"/hallitse"+link+"\">"+tree.name+"</a>\n");
		}

		if(tree.hasChildren()){
			sb.append("<ul>\n");
			for(String childname: tree.list()){
				Tree child = tree.getChild(childname);
				sb.append(genFolderTreeHtml2(child, vpath, link));
			}
			sb.append("</ul>\n");
		}
		return sb.toString();
	}

	private String genTempHtml(Tree tree, String link) {
		StringBuilder sb = new StringBuilder();

		if(tree.hasParent()){
			sb.append("<a href=\""+link+"/"+Utils.urlEncode(tree.name)+"\">"+tree.name+"</a>\n");

			if(tree.hasChildren()){
				sb.append("<ul>\n");
				for(String childname: tree.list()){
					Tree child = tree.getChild(childname);
					sb.append(genTempHtml(child, link));
				}
				sb.append("</ul>\n");
			}
		}else{
			if(tree.hasChildren()){
				//sb.append("<ul>\n");
				for(String childname: tree.list()){
					Tree child = tree.getChild(childname);
					sb.append(genTempHtml(child, link));
				}
				//sb.append("</ul>\n");
			}
		}
		return sb.toString();
	}

	private boolean checkFilename(String string) {
		char c;
		boolean dot = false;
		for (int i = 0; i < string.length(); i++) {
			c = string.charAt(i);
			log.info("c[" + c + "]");

			if (Character.isLetter(c))
				continue;
			log.info(" !letter");

			if (Character.isDigit(c))
				continue;
			log.info(" !digit");

			if (c == '-')
				continue;
			log.info(" !-");

			if (c == '_')
				continue;
			log.info(" !_");

			if (c == '.') {
				dot = true;
				continue;
			}

			log.info(" !.");

			return false;
		}

		if (dot) {
			if (string.contains(".."))
				return false;
		}

		return true;
	}

	@Override
	public void execute() {
		activate();
		super.execute();
	}
}
