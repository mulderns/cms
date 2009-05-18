package cms.mods;

import util.ActionLog;
import util.Logger;
import html.CmsElement;
import html.FileField;
import http.FormPart;
import cms.Cgicms;
import cms.DataRelay;
import cms.FileHive;

public class ModUpload extends Module {

	public ModUpload(DataRelay datarelay) {
		super(datarelay);
		hook = "tiedostot";
		menu_label = "Tiedostot";
	}

	public void activate(){
		super.activate();

		actions.add(new Action(null, ""){public void execute(){
			CmsElement box = new CmsElement();
			box.addLayer("div", "boxi2 medium4");
			box.addTag("h4", "Tiedostot");
			box.addLayer("div", "ingroup filled");
			box.addLayer("table", "table5");
			box.addSingle("colgroup");
			box.addSingle("colgroup width=\"70\"");
			box.addSingle("colgroup width=\"27\"");

			log.info("hook["+hook+"]");

			FileHive fh = FileHive.getFileHive(Cgicms.uploaded_dir);

			for(String file : fh.getFiles()){
				box.addLayer("tr");
				box.addTag("td", null, "<a title=\"lataa\" class=\"but\" href=\""+script+"/"+hook+"/download/"+file+"\">"+file+"</a>");
				box.addTag("td", null, "&nbsp;");
				box.addTag("td", null,"<a title=\"poista\" class=\"but\" href=\""+script+"/"+hook+"/delete/"+file+"\">X</a>");
				box.up();
			}
			box.up();
			//box.addTag("a href=\""+script+"/"+hook+"/upload"+"\" class=\"list\"", "Upload");
			//uploadBox.addLink("Upload",script+"/"+hook+"/upload");

			page.setTitle("Tiedostojen huploadaus");
			page.addTop(getMenu());
			page.addCenter(box.toString());
			page.addRight(genBugreport());
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("upload", "upload"){public void execute(){
			if(datarelay.multipart){
				//if(datarelay.post != null && datarelay.post.size()>0){

				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston vastaanotto");
				CmsElement box = new CmsElement();
				box.addLayer("div", "boxi2 medium4");
				box.addTag("h4", "Tiedoston vastaanotto");
				box.addLayer("div", "ingroup filled");
				
				box.addContent("<pre style=\"border:1px dashed #eee;background:#fefefe;color:#eee;\">");
				box.addContent(datarelay.post.toString());
				box.addContent("</pre>");


				StringBuilder sb = new StringBuilder();

				FileHive fh = FileHive.getFileHive(Cgicms.uploaded_dir);

				if(datarelay.files.length==0){
					sb.append("Virhe: Tiedosto yli 10 megaa, tyhjä, tai jotain muuta");
				}

				for(FormPart p: datarelay.files){
					if(fh.hasFile(p.getFilename())){
						sb.append("store failed : file with same name exists in archive");
					}else{
						if(fh.storeFile(p)){
							sb.append("stored file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}else{
							sb.append("storing failed, file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}
					}
				}

				box.addTag("p", sb.toString());
				box.addTag("a href=\""+script+"/"+hook+"\"","list","Ok");
				//uploadBox.addLink("Ok",script+"/"+hook);
				page.setTitle("Tiedoston vastaanotto");
				page.addTop(getMenu());
				page.addCenter(box.toString());
			}else{
				CmsElement box = new CmsElement();
				box.addLayer("form method=\"post\" action=\"" +
						script + "/" + hook +"/"+action_hook+
						"\" enctype=\"multipart/form-data\"");
				box.addLayer("div", "boxi2 medium3");
				box.addTag("h4", "Tiedoston lähetys");
				box.addLayer("div", "ingroup filled");
				box.addLayer("table", "table5");
				box.addSingle("colgroup");
				box.addSingle("colgroup width=\"70\"");
				
				box.addLayer("tr");
				box.addTag("td","Tiedosto:");
				box.addLayer("td");
				box.addField("tiedosto", null, true, new FileField());
				box.up(2);
				box.addLayer("tr");
				box.addTag("td colspan=\"2\"", "<input class=\"list\" style=\"width:100%;cursor:pointer;\" type=\"submit\" value=\"lähetä\">");
				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston lähetys");

				page.setTitle("Tiedoston lähetys");
				page.addTop(getMenu());
				page.addCenter(box);
			}
		}});

		actions.add(new Action(null, "download"){public void execute(){
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive(Cgicms.uploaded_dir);
				log.fail("ext["+ ext+"]");
				if(fh.hasFile(ext)){
					String data = fh.getFileData(ext);
					if(data != null){
						pagebuilder.rawSend(data);
					}
				}
			}else{
				pagebuilder.setRedirect(script+"/"+hook);
			}
		}});
		actions.add(new Action(null, "delete"){public void execute(){
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive(Cgicms.uploaded_dir);
				if(fh.hasFile(ext)){
					fh.removeFile(ext);
					pagebuilder.setRedirect(script+"/"+hook);
				}
			}else{
				pagebuilder.setRedirect(script+"/"+hook);
			}
		}});
	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}

	public void servePublic() {
		log = new Logger("tiedostot");
		pagebuilder = datarelay.pagebuilder;
		page = datarelay.page;
		ActionLog.action("[public] getting file ["+datarelay.query+"]");
		
		// validate request
		// check for .. and \\ and //
		// check for file
		// check if public
		
		// put up download page
		// send file
		
		pagebuilder.build(page);
	
	}	
}
