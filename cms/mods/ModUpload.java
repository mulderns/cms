package cms.mods;


import html.CmsElement;
import html.FileField;
import http.FormPart;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import util.ActionLog;
import util.Logger;
import cms.DataRelay;
import cms.FileHive;
import d2o.UploadFileRecord;

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
			box.createBox("Tiedostot");
			box.addLayer("table", "table5");

			log.info("hook["+hook+"]");

			FileHive fh = FileHive.getFileHive();

			DateFormat format = DateFormat.getDateInstance();
			Comparator<UploadFileRecord> by_name = new Comparator<UploadFileRecord>(){public int compare(UploadFileRecord o1, UploadFileRecord o2) {
				return o1.filename.compareTo(o2.filename);
			}};
			Comparator<UploadFileRecord> by_user = new Comparator<UploadFileRecord>(){public int compare(UploadFileRecord o1, UploadFileRecord o2) {
				return o1.upload_user.compareTo(o2.upload_user);
			}};
			Comparator<UploadFileRecord> by_date = new Comparator<UploadFileRecord>(){public int compare(UploadFileRecord o1, UploadFileRecord o2) {
				return Long.valueOf(o1.upload_date).compareTo(Long.valueOf(o2.upload_date));
			}};
			Comparator<UploadFileRecord> by_size = new Comparator<UploadFileRecord>(){public int compare(UploadFileRecord o1, UploadFileRecord o2) {
				return Long.valueOf(o1.size).compareTo(Long.valueOf(o2.size));
			}};


			//box.addLayer("tr");
			box.addSingle("colgroup ");
			box.addSingle("colgroup width=\"55\"");
			box.addSingle("colgroup width=\"55\"");
			box.addSingle("colgroup width=\"65\"");
			box.addSingle("colgroup ");
			box.addSingle("colgroup width=\"85\"");
			box.addSingle("colgroup width=\"65\"");
			//box.up();
			
			box.addLayer("tr");
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=name\">Nimi</a>");
			box.addTag("th", "Lataa");
			box.addTag("th", "Tiedot");
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=size\">Koko</a>");
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=date\">Lisätty</a>");
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=user\">Lisääjä</a>");
			box.addTag("th", "Poista");
			box.up();

			List<UploadFileRecord> records = fh.getFileRecords();

			Collections.sort(records, by_name);

			if(datarelay.query.containsKey("sort")){
				String by = datarelay.query.get("sort");
				if(by.equals("name")){
					Collections.sort(records, by_name);
				}else if(by.equals("date")){
					Collections.sort(records, by_date);
				}else if(by.equals("size")){
					Collections.sort(records, by_size);
				}else if(by.equals("user")){
					Collections.sort(records, by_user);
				}
			}
			
			
			DecimalFormat decimal_format = new DecimalFormat("#.#");
			
			final String[] suffixes = {
					"b",
					"Kb",
					"Mb",
					"Gb",
					"Tb"
			};
			boolean parillinen = true;
			
			long total_size = 0;
			
			for(UploadFileRecord record : records){
				parillinen = !parillinen;
				box.addLayer("tr"+(parillinen?" style=\"background-color:#f5f5f5\"":""));
				box.addTag("td style=\"\"", "<a style=\"text-decoration:none;color:#125698\" title=\"avaa\" class=\"\" href=\""+script+"/"+hook+"/download/"+record.filename+"?direct\">"+record.filename+"</a>");
				box.addTag("td", "<a title=\"lataa\" style=\"text-decoration:none;color:#125698\" href=\""+script+"/"+hook+"/download/"+record.filename+"\">&#187;lataa</a>");
				box.addTag("td", "<a title=\"tiedot\" style=\"text-decoration:none;color:#125698\" href=\""+script+"/"+hook+"/prefs/"+record.filename+"\">&#187;tiedot</a>");
				
				int order = 0;
				total_size += record.size;
				double size = record.size;
				while(size >= 100){
					size/=1024;
					order++;
				}
							
				String processed_size = decimal_format.format(size) + " " + suffixes[order];
				
				box.addTag("td style=\"text-align:right\"", processed_size);
				box.addTag("td style=\"padding-left:6px\"", format.format(new Date(record.upload_date)));
				box.addTag("td", record.upload_user);
				box.addTag("td", "<a title=\"poista\" class=\"but\" style=\"margin:0px;padding:0px;\" href=\""+script+"/"+hook+"/delete/"+record.filename+"\">X</a>");
				box.up();
			}
			box.up();

			
			int order = 0;
			double size = total_size;
			while(size >= 1000){
				size/=1024;
				order++;
			}
						
			String processed_size = decimal_format.format(size) + " " + suffixes[order];
			

			box.addTag("p", "total size: "+processed_size);
			
			order = 0;
			size = 167772160 - total_size;
			while(size >= 1000){
				size/=1024;
				order++;
			}
			processed_size = decimal_format.format(size) + " " + suffixes[order];
			
			
			box.addTag("p", "estimated free space: "+ processed_size);
			
			page.setTitle("Tiedostojen huploadaus");
			page.addCenter(box);
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("upload", "upload"){public void execute(){
			if(datarelay.multipart){
				CmsElement box = new CmsElement();
				box.createBox("Tiedoston vastaanotto", "medium4");

				box.addContent("<pre style=\"border:1px dashed #eee;background:#fefefe;color:#eee;\">");
				box.addContent(datarelay.post.toString());
				box.addContent("</pre>");

				StringBuilder sb = new StringBuilder();

				FileHive fh = FileHive.getFileHive();

				if(datarelay.files.length==0){
					sb.append("Virhe: Tiedosto yli 10 megaa, tyhjä, tai jotain muuta");
				}

				for(FormPart p: datarelay.files){
//					if(fh.hasFile(p.getFilename())){
//						sb.append("store failed : file with same name exists in archive");
//					}else{
						if(fh.addFile(username, false, "", p)){
							sb.append("stored file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}else{
							sb.append("storing failed, file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}
					//}
				}

				box.addTag("p", sb.toString());
				box.addTag("a href=\""+script+"/"+hook+"\"","list","Ok");

				page.setTitle("Tiedoston vastaanotto");
				//page.addTop(getMenu());
				page.addCenter(box);
			}else{
				CmsElement box = new CmsElement();
				box.addLayer("form method=\"post\" action=\"" +
						script + "/" + hook +"/"+action_hook+
				"\" enctype=\"multipart/form-data\"");
				box.createBox(null,"medium3");
				box.addTag("p", "tiedosto(je)n (yhteis)koko ei saa ylittää 10mb");
				box.up(2);
				
				box.addLayer("div", "boxi2 medium3");
				box.addTag("h4", "Tiedoston lähetys");
				box.addLayer("div", "ingroup filled");
				box.addLayer("table", "table5");
				box.addSingle("colgroup");
				box.addSingle("colgroup width=\"70\"");

				box.addLayer("tr");
				box.addTag("td","Tiedosto:");
				box.addLayer("td");
				box.addField("tiedosto1", null, true, new FileField());
				box.up(2);
				
				box.addLayer("tr");
				box.addTag("td","Tiedosto:");
				box.addLayer("td");
				box.addField("tiedosto2", null, false, new FileField());
				box.up(2);
				
				box.addLayer("tr");
				box.addTag("td","Tiedosto:");
				box.addLayer("td");
				box.addField("tiedosto3", null, false, new FileField());
				box.up(2);
				
				box.addLayer("tr");
				box.addTag("td colspan=\"2\"", "<input class=\"list\" style=\"width:100%;cursor:pointer;\" type=\"submit\" value=\"lähetä\">");
				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston lähetys");

				page.setTitle("Tiedoston lähetys");
				page.addCenter(box);
			}
		}});

		actions.add(new Action(null, "download"){public void execute(){
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive();
				log.fail("ext["+ ext+"]");
				if(fh.hasFile(ext)){
					String data = null;
					if(datarelay.query.containsKey("direct")){
						data = fh.getFileResponse(ext,false);
					}else{
						data = fh.getFileResponse(ext,true);
					}
					if(data != null){
						pagebuilder.rawSend(data);
						fh.up(ext);
					}else{
						CmsElement box = new CmsElement();
						box.createBox("error");
						box.addTag("p", "tiedostoa ei löytynyt");
						page.addCenter(box);
					}
				}
			}else{
				pagebuilder.setRedirect(script+"/"+hook);
			}
		}});
		actions.add(new Action(null, "delete"){public void execute(){
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive();
				if(fh.hasFile(ext)){
					fh.deleteFile(ext);
					pagebuilder.setRedirect(script+"/"+hook);
				}
			}else{
				pagebuilder.setRedirect(script+"/"+hook);
			}
		}});
		
		actions.add(new Action(null, "prefs"){public void execute(){
			
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive();
				if(fh.hasFile(ext)){
					UploadFileRecord record = fh.getFileRecord(ext);
					page.setTitle("Ominaisuudet - "+record.filename);
					
					CmsElement box = new CmsElement();
					box.createBox("Ominaisuudet");
					
					CmsElement prebox = new CmsElement();
										
					prebox.createBox(record.filename);
					prebox.addLayer("pre style=\"font-size:12.5px\"");
					prebox.addContent("filename : " + record.filename + "\n");
					prebox.addContent("size : " + record.size + "\n");
					prebox.addContent("content_type : " + record.content_type + "\n");
					prebox.addContent("download count : " + record.download_count + "\n");
					prebox.addContent("public access : " + record.public_access + "\n");
					prebox.addContent("access groups : " + record.access_groups + "\n");
					
					page.addCenter(prebox);
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
