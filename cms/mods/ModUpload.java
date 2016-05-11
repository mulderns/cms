package cms.mods;


import html.CmsElement;
import html.ComboBoxField;
import html.FileField;
import html.SubmitField;
import html.TextAreaField;
import html.TextField;
import http.FormPart;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import util.ActionLog;
import util.Logger;
import cms.Cgicms;
import cms.DataRelay;
import cms.FileHive;
import d2o.FlushingFile;
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

			DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT,Locale.UK);
			
			
			Comparator<UploadFileRecord> by_name = new Comparator<UploadFileRecord>(){public int compare(UploadFileRecord o1, UploadFileRecord o2) {
				return o1.filename.toLowerCase().compareTo(o2.filename.toLowerCase());
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
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=date\">Lis‰tty</a>");
			box.addTag("th", "<a class=\"but\" href=\""+script+"/"+hook+"/"+action_hook+"?sort=user\">Lis‰‰j‰</a>");
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
			for(String category : getFileCategories()){
				if(category.equals("-")){
					category = "";
				}else{
					box.addContent("<tr><td colspan=\"7\" style=\"font-weight:bold;padding-top:5px;border-bottom:1px solid black;\">"+category+"</td></tr>");
				}
				
				for(UploadFileRecord record : records){
					if(record.category!= null && !record.category.equals(category))
						continue;
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
					sb.append("Virhe: Tiedosto yli 10 megaa, tyhj‰, tai jotain muuta");
				}

				log.info("category ?");
				String category = "";
				if(datarelay.post.containsKey("category")){
					log.info(" found in post");
					String pCategory = datarelay.post.get("category");
					for(String c : getFileCategories()){
						if(!c.equals(pCategory))
							continue;
						category = pCategory;
						log.info(" match found in db");
						break;
					}
				}
				
				for(FormPart p: datarelay.files){
//					if(fh.hasFile(p.getFilename())){
//						sb.append("store failed : file with same name exists in archive");
//					}else{
						if(fh.addFile(username, false, "", category, p)){
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
				box.addTag("p", "tiedosto(je)n (yhteis)koko ei saa ylitt‰‰ 10mb");
				box.up(2);
				
				box.addLayer("div", "boxi2 medium3");
				box.addTag("h4", "Tiedoston l‰hetys");
				box.addLayer("div", "ingroup filled");
				box.addLayer("table", "table5");
				box.addSingle("colgroup");
				box.addSingle("colgroup width=\"70\"");

				box.addLayer("tr");
				box.addLayer("td colspan=\"2\"");
				box.addContent("Kategoria: ");
				box.addField("category", "-", true, new ComboBoxField(getFileCategories(),"-"));
				box.up(2);
				
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
				box.addTag("td colspan=\"2\"", "<input class=\"list\" style=\"width:100%;cursor:pointer;\" type=\"submit\" value=\"l‰het‰\">");
				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston l‰hetys");

				page.setTitle("Tiedoston l‰hetys");
				page.addCenter(box);
			}
		}});

		actions.add(new Action("categories", "categories"){public void execute(){
			FlushingFile categoryfile = new FlushingFile(new File(Cgicms.products_dir,"misc.categories"));
			String[] data;
			if(checkField("categories")){
				data = datarelay.post.get("categories").split(",");
				for(String d : data)
					d = d.trim();
				categoryfile.overwrite(util.Csv.encode(data));

			}else{
				try{
					data = util.Csv.decode(categoryfile.loadAll()[0]);
				}catch(ArrayIndexOutOfBoundsException e){
					data = new String[1];
					data[0] = "-";					
				}
			}

			StringBuilder sb = new StringBuilder();
			for(String s : data){
				sb.append(s).append(",");
			}
			
			CmsElement box = new CmsElement();
			box.createBox("Categories","medium3");
			box.addFormTop(script + "/" + hook + "/" + action_hook);
			box.addField("categories", sb.substring(0, sb.length()-1), true, new TextAreaField(30,10));
			box.addContent("<br/>");
			box.addField(null,"submit", false, new SubmitField(true));

			page.setTitle("Edit file categories");
			page.addCenter(box);
			page.addLeft(getActionLinks());
		}});
		
		actions.add(new Action(null, "download"){public void execute(){
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive();
				log.info("ext["+ ext+"]");
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
						box.addTag("p", "tiedostoa ei lˆytynyt");
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
			log.info("prefs->");
			if(!ext.equals("")){
				FileHive fh = FileHive.getFileHive();
				log.info(" file["+ext+"]");
				if(fh.hasFile(ext)){
					log.info("  found");
					UploadFileRecord record = fh.getFileRecord(ext);
					page.setTitle("Ominaisuudet - "+record.filename);
					boolean doupdate = false;
					
					if(checkField("filename") || checkField("category")){
						log.info(" update something");
						if(datarelay.post.containsKey("filename")){
							String newname = datarelay.post.get("filename");
							if(!record.filename.equals(newname) ){
								log.info("  name -> ["+newname+"]");
								if(!fh.hasFile(newname)){
									fh.renameFile(record.filename, newname);
									record = fh.getFileRecord(newname);
									record.filename = newname;
									doupdate = true;
								}else{
									log.info("   filename in use");
									pagebuilder.addMessage("filename in use");
								}
							}
						}
						if(datarelay.post.containsKey("category")){
							String newcategory = datarelay.post.get("category");
							if(newcategory.equals("-"))
								newcategory = "";
							
							if(!record.filename.equals(newcategory)){
								log.info("  category");
								if(newcategory.equals(""))
									newcategory = "-";
								
								for(String c : getFileCategories()){
									if(!c.equals(newcategory))
										continue;
									if(newcategory.equals("-"))
										newcategory = "";
									record.category = newcategory;
									log.info("   match found in db");
									doupdate = true;
									break;
								}
								
								
								//if(getFileCategories())
								
							}
						}
						if(doupdate)
							fh.updateFileRecord(record);
						
						
					}					
					
					CmsElement box = new CmsElement();
					box.createBox(record.filename,"medium4");
					box.addFormTop(script+"/"+hook+"/"+action_hook+"/"+record.filename);
					box.addLayer("table","table5");
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "Filename:");
					box.addLayer("td");
					box.addField("filename", record.filename, false, new TextField(30));
					box.up(2);
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "size:");
					box.addTag("td", ""+record.size);
					box.up();
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "content_type:");
					box.addTag("td", record.content_type);
					box.up();
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "download count:");
					box.addTag("td", ""+record.download_count);
					box.up();
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "public access:");
					box.addTag("td", ""+record.public_access);
					box.up();
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "category:");
					box.addLayer("td");
					box.addField("category", null, false, new ComboBoxField(getFileCategories(), (record.category.equals("")?"-":record.category)));
					//box.addTag("td", record.category);
					box.up();
					
					box.addLayer("tr");
					box.addTag("td style=\"text-align:right;\"", "access groups:");
					box.addTag("td", record.access_groups);
					box.up();
					
					box.addLayer("tr");
					box.addLayer("td colspan=\"2\"");
					box.addField(null,"submit",false,new SubmitField(true));
					
//					CmsElement prebox = new CmsElement();
//
//					prebox.createBox(record.filename);
//					prebox.addLayer("pre style=\"font-size:12.5px\"");
//					prebox.addContent("filename : " + record.filename + "\n");
//					prebox.addContent("size : " + record.size + "\n");
//					prebox.addContent("content_type : " + record.content_type + "\n");
//					prebox.addContent("download count : " + record.download_count + "\n");
//					prebox.addContent("public access : " + record.public_access + "\n");
//					prebox.addContent("category : " + record.category + "\n");
//					prebox.addContent("access groups : " + record.access_groups + "\n");

					page.addCenter(box);
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

	private String[] getFileCategories() {
		try{
			FlushingFile categoryfile = new FlushingFile(new File(Cgicms.products_dir,"misc.categories"));
			String[] data;
			data = util.Csv.decode(categoryfile.loadAll()[0]);

			return data;
		}catch (NullPointerException e){
			return new String[]{"-"};
		}catch (ArrayIndexOutOfBoundsException e){
			return new String[]{"-"};
		}
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
