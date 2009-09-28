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
import d2o.FileRecord;

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
			Comparator<FileRecord> by_name = new Comparator<FileRecord>(){public int compare(FileRecord o1, FileRecord o2) {
				return o1.filename.compareTo(o2.filename);
			}};
			Comparator<FileRecord> by_user = new Comparator<FileRecord>(){public int compare(FileRecord o1, FileRecord o2) {
				return o1.upload_user.compareTo(o2.upload_user);
			}};
			Comparator<FileRecord> by_date = new Comparator<FileRecord>(){public int compare(FileRecord o1, FileRecord o2) {
				return new Long(o1.upload_date).compareTo(new Long(o2.upload_date));
			}};
			Comparator<FileRecord> by_size = new Comparator<FileRecord>(){public int compare(FileRecord o1, FileRecord o2) {
				return new Long(o1.size).compareTo(new Long(o2.size));
			}};


			//box.addLayer("tr");
			box.addSingle("colgroup ");
			box.addSingle("colgroup width=\"65\"");
			box.addSingle("colgroup width=\"65\"");
			box.addSingle("colgroup width=\"75\"");
			box.addSingle("colgroup width=\"85\"");
			box.addSingle("colgroup width=\"65\"");
			//box.up();
			
			box.addLayer("tr");
			box.addTag("th", "<a href=\""+script+"/"+hook+"/"+action_hook+"?sort=name\">Tiedosto</a>");
			box.addTag("th", "Lataa");
			box.addTag("th", "<a href=\""+script+"/"+hook+"/"+action_hook+"?sort=size\">Koko</a>");
			box.addTag("th", "<a href=\""+script+"/"+hook+"/"+action_hook+"?sort=date\">Lis�tty</a>");
			box.addTag("th", "<a href=\""+script+"/"+hook+"/"+action_hook+"?sort=user\">Lis��j�</a>");
			box.addTag("th", "Poista");
			box.up();

			List<FileRecord> records = fh.getFileRecords();

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
			
			for(FileRecord record : records){
				box.addLayer("tr");
				box.addTag("td", "<a title=\"lataa\" class=\"\" href=\""+script+"/"+hook+"/download/"+record.filename+"?direct\">"+record.filename+"</a>");
				box.addTag("td", "<a title=\"lataa\" class=\"but\" href=\""+script+"/"+hook+"/download/"+record.filename+"\">lataa</a>");
				
				int order = 0;
				double size = record.size;
				while(size >= 100){
					size/=1024;
					order++;
				}
							
				String processed_size = decimal_format.format(size) + " " + suffixes[order];
				
				box.addTag("td style=\"text-align:right\"", processed_size);
				box.addTag("td style=\"padding-left:6px\"", format.format(new Date(record.upload_date)));
				box.addTag("td", record.upload_user);
				box.addTag("td", "<a title=\"poista\" class=\"but\" href=\""+script+"/"+hook+"/delete/"+record.filename+"\">X</a>");
				box.up();
			}
			box.up();

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
					sb.append("Virhe: Tiedosto yli 10 megaa, tyhj�, tai jotain muuta");
				}

				for(FormPart p: datarelay.files){
					if(fh.hasFile(p.getFilename())){
						sb.append("store failed : file with same name exists in archive");
					}else{
						if(fh.addFile(username, false, "", p)){
							sb.append("stored file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}else{
							sb.append("storing failed, file["+p.getFilename()+"] type["+p.getContentType()+"] size["+p.bytes.length+"]bytes\n");
						}
					}
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
				box.addLayer("div", "boxi2 medium3");
				box.addTag("h4", "Tiedoston l�hetys");
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
				box.addTag("td colspan=\"2\"", "<input class=\"list\" style=\"width:100%;cursor:pointer;\" type=\"submit\" value=\"l�het�\">");
				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston l�hetys");

				page.setTitle("Tiedoston l�hetys");
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
						box.addTag("p", "tiedostoa ei l�ytynyt");
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
