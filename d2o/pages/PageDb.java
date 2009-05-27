package d2o.pages;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import util.ActionLog;
import util.Logger;
import cms.Cgicms;
import cms.FileOps;
import d2o.FlushingFile;

public class PageDb {
	private Logger log;

	private static boolean created = false;
	private static PageDb present;

	private FlushingFile actions;

	private static final File sdir = new File(Cgicms.products_dir,"pagedb");

	private Treedex treedex;
	private Tempdex tempdex;


	public static PageDb getDb() {
		if(!created){
			created = true;
			present = new PageDb();
		}
		return present;
	}

	private PageDb(){
		log = new Logger("PageDb");
		treedex = new Treedex(sdir);
		tempdex = new Tempdex(sdir);
		actions = new FlushingFile(new File(sdir, "actions"));
	}

	public String createDir(String path, String name){
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return "could not load treedex";
			}
		}

		if(!checkDirSyntax(path, name))
			return "dirsyntaxerror";

		if(!treedex.pingDir(path)){
			return "parent dir doesn't exist";
		}

		if(treedex.addDir(path,name)){
			File temp = new File(sdir,path);
			temp = new File(temp,name);
			if(temp.mkdir()){
				actions.append("+d,"+path+"/"+name+","+Cgicms.datarelay.username);
				return null;
			}
			return "could not create directory["+temp.getAbsolutePath()+"]";
		}else{
			return "could not add directory to db";
		}
	}

	public String removeDir(String path) {
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return "could not load treedex";
			}
		}

		if(!checkDirSyntax(path, ""))
			return "dirsyntaxerror";

		if(path.length() == 0)
			return "can't delete root folder";

		File temp = new File(sdir,path);

		if(temp.exists() && temp.isDirectory()){
			for(File file : temp.listFiles()){
				if(!file.delete()){
					return "could not remove ["+file.getAbsolutePath()+"]";
				}
			}
			if(temp.delete()){
				if(treedex.deleteDir(path)){
					actions.append("-d,"+path+","+Cgicms.datarelay.username);
					return null;
				}
				return "could not remove directory from db";
			}else{
				return "could not remove directory["+temp.getAbsolutePath()+"]";
			}
		}else{
			return "directory ["+temp+"] doesn't exist or is not a directory";
		}
	}


	/*
	private String getFolderHtml(String path) {
		if(!treedex.state.open){
			treedex.load();
		}
		return treedex.dir_tree.rootToHtml(path);
	}*/

	/*
	private boolean pathCheck(String path) {
		char c;
		boolean dot = false;
		boolean slash = false;

		for(int i = 0; i < path.length(); i++){
			c = path.charAt(i);
			if(Character.isLetter(c))
				continue;
			if(Character.isDigit(c))
				continue;
			if(c == '-')
				continue;
			if(c == '_')
				continue;
			if(c == '.'){
				dot = true;
				continue;
			}
			if(c == '/'){
				slash = true;
				continue;
			}
			return false;
		}

		if(dot){
			if(path.contains(".."))
				return false;
		}
		if(slash){
			if(path.contains("//"))
				return false;
		}
		return true;
	}*/



	public boolean dirExists(String path) {
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return false;
			}
		}

		if(path.length() == 0){
			return true;
		}

		if(treedex.pingDir(path)){
			File temp = new File(sdir,path);
			if(temp.exists() && temp.isDirectory()){
				return true;
			}
		}
		return false;
	}

	public String[] getDirList(String path) {
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return null;
			}
		}
		Tree temp = treedex.getTree(path);
		if(temp == null){
			return new String[0];
		}
		return temp.list();
	}

	public Tree getFolderTree() {
		if(!treedex.state.open){
			if(treedex.load()!= null){
				log.fail("could not load treedex");
				return null;
			}
		}
		return treedex.dir_tree;
	}

	public String addFile(String path, CmsFile file){
		if(!checkDirSyntax(path, ""))
			return "dir syntaxerror";
		if(!checkFileSyntax(file.name))
			return "name syntax error";

		IndexFile index = getIndex(path);
		String result;
		if( (result = index.addRecord(file)) != null)
			return result;

		if(!CmsFile.storeMeta(file, index.dir))
			return "failed to store meta";
		if(!CmsFile.storeData(file, index.dir))
			return "failed to store data";
		if((result = index.storeRecords())!=null)
			return result;

		actions.append("+f,"+path+"/"+file.name+","+Cgicms.datarelay.username);
		return null;
	}

	public CmsFile getFileMeta(VirtualPath path){
		log.info("getFileMeta...");

		//chech request
		if(path.getFilename().equals("")){
			log.fail("no file specified");
			return null;
		}

		//load file meta

		//load metafile
		log.info("load File ["+path.getUrl()+"]");
		return CmsFile.loadFile(path,sdir);

		//determine file type
		//create appropriate file (txt,bin,etc..)
		//make sure meta is filled in


		//if file doesn't exist
		// return null

		/**
		log.info("loading index ["+path.getPath()+"]");
		IndexFile indexfile = treedex.getIndexFile(path.getPath());
		if(indexfile == null){
			log.fail("index file could not be retrieved ["+path.getPath()+"]");
			return null;
		}

		if(indexfile.load() == null){
			if(indexfile.fileExists(path.getFilename())){
				log.info("file exists");
				CmsFile temp = indexfile.getFile(path.getFilename());
				temp.relativePath = path;
				if(temp.loadMeta2(indexfile.dir)){
					log.info("meta load success");
					log.info(" content_type : "+temp.content_type);
					return temp;					
				}
				log.fail("meta load failed");
				return null;
			}else{
				log.info("file doesn't exist? ["+path.getFilename()+"]");
				log.info(" in: "+ indexfile.toString());
			}
		}else{
			log.fail("could not load index ["+indexfile.file+"]");
		}

		return null;*/
	}

	/*
	public CmsFile[] getFileList(String path) {
		File dir = new File(sdir,path);
		IndexFile index = new IndexFile(dir);
		String result = index.load();
		if(result != null){
			log.fail("index did not load["+result+"]");
		}else{
			log.info("index loaded well or something");
		}
		return index.getFileList();
	}*/

	private IndexFile getIndex(String path) {
		IndexFile index = new IndexFile(new File(sdir, path));
		String result;
		if((result = index.loadRecords())!= null){
			log.fail("could not load index file: "+ result);
			return null;
		}
		return index;
	}

	public String[] getFileNameList(String path) {
		log.info("getting filename list for ["+path+"]");
		if(!checkDirSyntax(path, "")){
			log.info("invalid dir syntax");
			return null;
		}

		File dir = new File(sdir,path);
		IndexFile index = new IndexFile(dir);
		if(index.loadRecords()!=null)
			log.fail("index not loaded because idont know");
		log.info("loaded records");

		ArrayList<String> temp = new ArrayList<String>();

		for(IndexRecord ir: index.getRecords()){
			temp.add(ir.filename);
			log.info("record >> "+ir);
		}
		Collections.sort(temp);
		return temp.toArray(new String[0]);
	}

	public String rename(VirtualPath path, String uusinimi) {
		IndexFile index = getIndex(path.getPath());
		if(index == null){
			return "could not load index";
		}

		if(index.getRecord(uusinimi)!=null){
			return "name already in use";
		}

		index.renameRecord(path.getFilename(),uusinimi);

		if(CmsFile.loadFile(path, sdir).rename(uusinimi,sdir)){
			index.storeRecords();
			return null;
		}
		return "error while renaming the file";
	}

	/**
	public String rename(CmsFile file, String uusinimi) {
		IndexFile index = getIndex(file.relativePath.getPath());
		if(index == null){
			return "could not load index";
		}

		if(index.getRecord(uusinimi)!=null){
			return "name already in use";
		}

		index.rename(file.name,uusinimi);
		CmsFile.loadFile(path, sdir).rename();
		index.store();
		return null;
	}*/

	public String createTemplate(String filename){
		String result;
		if(!tempdex.state.open){
			if((result = tempdex.load()) != null){
				log.fail("could not load tempdex");
				return "could not load tempdex: "+result;
			}
		}

		if(!checkFileSyntax(filename))
			return "name syntax error";

		if( (result = tempdex.createTempEntry(filename)) != null)
			return result;
		TemplateFile temp = new TemplateFile(filename);

		if(!CmsFile.storeMeta(temp, tempdex.getDir()))
			return "failed to store meta";

		if(!CmsFile.storeData(temp, tempdex.getDir()))
			return "failed to store data";

		if(!tempdex.store())
			return "db store failed";

		return null;
	}


	public String createTemplate(TemplateFile file){
		String result;
		if(!tempdex.state.open){
			if((result = tempdex.load()) != null){
				log.fail("could not load tempdex");
				return "could not load tempdex: "+result;
			}
		}

		if(!checkFileSyntax(file.name))
			return "name syntax error";

		if( (result = tempdex.createTempEntry(file)) != null)
			return result;

		if(!CmsFile.storeMeta(file, tempdex.getDir()))
			return "failed to store meta";
		if(!CmsFile.storeData(file, tempdex.getDir()))
			return "failed to store data";

		if(!tempdex.store())
			return "db store failed";

		return null;
	}

	public boolean removeTemplate(String name){
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return false;
			}
		}

		if(!tempdex.templateExists(name)){
			log.fail("template ["+name+"] not found in index");
			return false;			
		}

		String result = tempdex.removeTemplate(name);
		if(result != null){
			log.fail(result);
			return false;
		}

		tempdex.store();

		return true;

	}

	public boolean renameTemplate(String oldname, String newname){
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return false;
			}
		}

		if(!tempdex.templateExists(oldname)){
			log.fail("template ["+oldname+"] not found in index");
			return false;			
		}

		if(tempdex.templateExists(newname)){
			log.fail("template ["+newname+"] allready in use index");
			return false;			
		}

		tempdex.rename(oldname,newname);

		if(!tempdex.store()){
			return false;
		}

		return true;

	}

	public TemplateFile getTemplate(String name){
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return null;
			}
		}

		if(tempdex.templateExists(name)){
			return tempdex.getTemplate(name);
		}else{
			log.fail("template["+name+"]not found in index");
			return null;
		}

	}

	public TemplateFile getTemplateMeta(String name){
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return null;
			}
		}

		if(tempdex.templateExists(name)){
			return tempdex.getTemplateMeta(name);
		}else{
			log.fail("template["+name+"]not found in index");
			return null;
		}

	}

	/*
	public String getTempHtml(String path) {
		if(!tempdex.state.open){
			tempdex.load();
		}
		return tempdex.temp_tree.toTempHtml2(path);
	}*/

	public boolean updateTemplate(TemplateFile tfile) {
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("update could not load tempdex");
				return false;
			}
		}

		if(!checkFileSyntax(tfile.name)){
			log.fail("filename syntax error");
			return false ;
		}


		String result = tempdex.updateParent(tfile);
		if(result != null){
			log.fail("updateTemplate: "+result);
			return false;
		}

		if(!CmsFile.storeMeta(tfile, tempdex.getDir())){
			log.fail("failed to store meta");
			return false;
		}

		if(!CmsFile.storeData(tfile, tempdex.getDir())){
			log.fail("failed to store data");
			return false;
		}

		if(!tempdex.store()){
			log.fail("db store failed");
			return false;
		}
		return true;
	}


	/*
	public TemplateFile getTemplate(String name) {
		if(tempdex.state.pure){
			if(tempdex.load() != null){
				log.fail("could not load treedex");
				return null;
			}
		}
		log.info("getting template ["+tempdex.temp_tree.toString()+"]");
		return tempdex.getTemplate(name);
	 */

	/*
		boolean found = false;
		for(String s: tempdex.getTemplateNames()){
			if(s.equals(name)){
				found = true;
				break;
			}
		}

		if(!found)
			return null;

		FileHive fh = FileHive.getFileHive();
		File meta = new File(sdir,"temp.meta."+name);
		File data = new File(sdir,"temp.data."+name);

		TemplateFile file = new TemplateFile();
		file.parseMeta(fh.readFileToArrayIso(meta));
		file.parseData(fh.readFileToArrayIso(data));
		return file;
	 */
	//}


	public boolean fileExists(VirtualPath vpath) {
		//public boolean fileExists(String path) {

		String path = vpath.getUrl();

		log.info("checking if file exists["+path+"]");

		if(path.equals("")){
			log.info("no file specified");
			return false;
		}

		if(path.startsWith("/"))
			path = path.substring(1);

		String[] parts = path.split("/");

		log.info(" 0");
		if(parts.length > 0){
			StringBuilder sb = new StringBuilder();
			log.info(" 1");
			for(int i = 0; i < parts.length -1; i++){
				sb.append(parts[i]+"/");
			}
			log.info(" 2");
			//			if(dirExists(sb.toString())){
			File dir = new File(sdir,sb.toString());
			File index = new File(dir,"index");
			if(index.exists()){
				log.info(" 3");
				IndexFile dex = new IndexFile(dir);
				if(dex.loadRecords()==null){
					log.info(" 4");
					if(dex.fileExists(parts[parts.length-1])){
						log.info(" 5");
						
						File file = new File(dir,"page.meta."+parts[parts.length-1]);
						log.info(" "+ file.getAbsolutePath());
						if(file.exists()){
							log.info(" 6");
							log.info(" the file exists");
							return true;
						}						
					}
				}
			}
			//			}
		}
		log.info(" the file doesn't exist");
		return false;
	}

	public String[] getTemplateNames() {
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return null;
			}
		}

		String[] temp = tempdex.getTemplateNames();
		if(temp == null){
			return new String[0];
		}
		return temp;
	}

	/*
	private String getFolderHtml(String path) {
		if(!treedex.state.open){
			treedex.load();
		}
		return treedex.dir_tree.rootToHtml(path);
	}*/

	/*
	private boolean pathCheck(String path) {
		char c;
		boolean dot = false;
		boolean slash = false;

		for(int i = 0; i < path.length(); i++){
			c = path.charAt(i);
			if(Character.isLetter(c))
				continue;
			if(Character.isDigit(c))
				continue;
			if(c == '-')
				continue;
			if(c == '_')
				continue;
			if(c == '.'){
				dot = true;
				continue;
			}
			if(c == '/'){
				slash = true;
				continue;
			}
			return false;
		}

		if(dot){
			if(path.contains(".."))
				return false;
		}
		if(slash){
			if(path.contains("//"))
				return false;
		}
		return true;
	}*/

	public String removeFile(String path, String name){
		//path = cleanDir(path);
		checkDirSyntax(path, name);

		IndexFile index = new IndexFile(new File(sdir,path));
		String result;

		if( (result = index.loadRecords()) != null)
			return result;
		if( (result = index.removeRecord(name)) != null)
			return result;

		//FileHive fh = FileHive.getFileHive();
		File dir = new File(sdir,path);
		File meta = new File(dir,"page.meta."+name);
		File data = new File(dir,"page.data."+name);

		StringBuilder errors = new StringBuilder();

		if(!FileOps.archive(data)){
			errors.append("could not remove data["+data.getAbsolutePath()+"]\n");
		}
		if( (result = index.storeRecords()) != null){
			errors.append(result+"\n");
		}
		if(!FileOps.archive(meta)){
			errors.append("could not remove meta["+meta.getAbsolutePath()+"]");
		}
		if(errors.length() > 0){
			return errors.toString();
		}
		return null;
	}

	public String addGroup(String path, String name){
		if(!checkDirSyntax(path, name))
			return "syntaxerror";

		File temp = new File(sdir,path);
		temp = new File(temp, "index.groups");

		FlushingFile groups = new FlushingFile(temp);
		boolean found = false;
		for(String s : groups.loadAll())
			if(s.equals(name))
				found = true;

		if(found)
			return "group exists allready";

		return groups.append(name);
	}

	/*
	private String getFolderView(String ext, String link) {
		log.info("getFolderView["+ext+"]");
		ext = cleanDir(ext);
		File dir = new File(sdir,ext);
		if(!dir.exists()){
			log.severe("directory doesn't exist:"+dir.getAbsolutePath());
			return null;
		}

		StringBuilder sb = new StringBuilder();
		//boolean first = true;

		log.info("dirs from treedex:");
		String[] folders = treedex.getFolders(ext);
		log.info(Arrays.toString(folders));

		sb.append("<dl class=\"folder\">");
		if(folders.length > 0){
			sb.append("<dt><a href=\""+link+"/addfold/"+ext+"\" class=\"add\">[+]</a></dt>");
			for(String s : folders){
				//if(first){first = false;}
				sb.append("<dt><a href=\""+link+"/hallitse/"+ext+(ext==""?"":"/")+s+"\">"+s+"</a></dt>");
			}
		}else{
			sb.append("<dt><a href=\""+link+"/addfold/"+ext+"\">[+] lis‰‰ kansio</a></dt>");
		}
		sb.append("</dl>");


		IndexFile index = new IndexFile(dir);
		String result = index.load();
		log.info("index.load:"+result);
		log.info("index.size:"+(index.records==null?"null":index.records.size()));

		log.info("groups from index:");
		FileGroup[] groups = index.getGroups();
		log.info(Arrays.toString(groups));
		boolean root = false;
		if(groups.length > 0){
			for(FileGroup fg : groups){
				if(fg.groupname.equals("-")){
					root = true;
					sb.append("<dl class=\"filegroup root\">");
					sb.append("<dt><a href=\""+link+"/addfile/"+ext+"?group="+fg.groupname+"\" class=\"add\">[+]</dt>");
				}else{
					sb.append("<dl class=\"filegroup\">");
					sb.append("<dt><a href=\""+link+"/addfile/"+ext+"?group="+fg.groupname+"\" class=\"add\">[+]</dt>");
				}
				for(CmsFile ir : fg.files){
					sb.append("<dd><a href=\""+link+"/file/"+ext+(ext==""?"":"/")+ir.name+"\">"+ir.name+"</dd>");
				}
				sb.append("</dl>");
			}

		}
		if(!root){
			sb.append("<dl class=\"filegroup root\">");
			sb.append("<dt><a href=\""+link+"/addfile/"+ext+"\">[+] lis‰‰ tiedosto</dt>");
			sb.append("</dl>");
		}
		sb.append("<dl class=\"filegroup\">");
		sb.append("<dt><a href=\""+link+"/addgroup/"+ext+"\">[+] lis‰‰ ryhm‰</dt>");
		sb.append("</dl>");


		return sb.toString();
	}*/

	/*
	private String[] getGroups(String path) {
		if(treedex.state.pure){
			if(treedex.load() != null){
				log.severe("could not load treedex");
				return null;
			}
		}
		if(!pathCheck(path)){
			log.severe("malicious path detected");
			return null;
		}
		File source_dir;
		if(path.length()== 0){
			source_dir = sdir;
		}else{
			source_dir = new File(sdir,path);
			if(!source_dir.isDirectory()){
				log.severe("error in paht["+source_dir.getAbsolutePath()+"]");
				return null;
			}
		}
		ArrayList<String> groups = new ArrayList<String>(10);
		groups.add("-");
		//see as to what groups can be found in said location;
		//read groupsfile(s)		

		return groups.toArray(new String[groups.size()]);

	}*/

	public String removeGroup(String path, String name){
		if(!checkDirSyntax(path, name)){
			return "syntaxerror";
		}

		File temp = new File(sdir,path);
		temp = new File(temp, "groups");

		FlushingFile groups = new FlushingFile(temp);
		boolean found = false;
		String[] lines = groups.loadAll();
		ArrayList<String> newLines = new ArrayList<String>(lines.length);
		for(String s : lines)
			if(s.equals(name)){
				found = true;
			}else{
				newLines.add(s);
			}

		if(found){
			return groups.overwrite(newLines.toArray(new String[newLines.size()]));
		}
		return "no such group found";
	}

	/*
	private String getFolderView(String ext, String link) {
		log.info("getFolderView["+ext+"]");
		ext = cleanDir(ext);
		File dir = new File(sdir,ext);
		if(!dir.exists()){
			log.severe("directory doesn't exist:"+dir.getAbsolutePath());
			return null;
		}

		StringBuilder sb = new StringBuilder();
		//boolean first = true;

		log.info("dirs from treedex:");
		String[] folders = treedex.getFolders(ext);
		log.info(Arrays.toString(folders));

		sb.append("<dl class=\"folder\">");
		if(folders.length > 0){
			sb.append("<dt><a href=\""+link+"/addfold/"+ext+"\" class=\"add\">[+]</a></dt>");
			for(String s : folders){
				//if(first){first = false;}
				sb.append("<dt><a href=\""+link+"/hallitse/"+ext+(ext==""?"":"/")+s+"\">"+s+"</a></dt>");
			}
		}else{
			sb.append("<dt><a href=\""+link+"/addfold/"+ext+"\">[+] lis‰‰ kansio</a></dt>");
		}
		sb.append("</dl>");


		IndexFile index = new IndexFile(dir);
		String result = index.load();
		log.info("index.load:"+result);
		log.info("index.size:"+(index.records==null?"null":index.records.size()));

		log.info("groups from index:");
		FileGroup[] groups = index.getGroups();
		log.info(Arrays.toString(groups));
		boolean root = false;
		if(groups.length > 0){
			for(FileGroup fg : groups){
				if(fg.groupname.equals("-")){
					root = true;
					sb.append("<dl class=\"filegroup root\">");
					sb.append("<dt><a href=\""+link+"/addfile/"+ext+"?group="+fg.groupname+"\" class=\"add\">[+]</dt>");
				}else{
					sb.append("<dl class=\"filegroup\">");
					sb.append("<dt><a href=\""+link+"/addfile/"+ext+"?group="+fg.groupname+"\" class=\"add\">[+]</dt>");
				}
				for(CmsFile ir : fg.files){
					sb.append("<dd><a href=\""+link+"/file/"+ext+(ext==""?"":"/")+ir.name+"\">"+ir.name+"</dd>");
				}
				sb.append("</dl>");
			}

		}
		if(!root){
			sb.append("<dl class=\"filegroup root\">");
			sb.append("<dt><a href=\""+link+"/addfile/"+ext+"\">[+] lis‰‰ tiedosto</dt>");
			sb.append("</dl>");
		}
		sb.append("<dl class=\"filegroup\">");
		sb.append("<dt><a href=\""+link+"/addgroup/"+ext+"\">[+] lis‰‰ ryhm‰</dt>");
		sb.append("</dl>");


		return sb.toString();
	}*/

	/*
	private String[] getGroups(String path) {
		if(treedex.state.pure){
			if(treedex.load() != null){
				log.severe("could not load treedex");
				return null;
			}
		}
		if(!pathCheck(path)){
			log.severe("malicious path detected");
			return null;
		}
		File source_dir;
		if(path.length()== 0){
			source_dir = sdir;
		}else{
			source_dir = new File(sdir,path);
			if(!source_dir.isDirectory()){
				log.severe("error in paht["+source_dir.getAbsolutePath()+"]");
				return null;
			}
		}
		ArrayList<String> groups = new ArrayList<String>(10);
		groups.add("-");
		//see as to what groups can be found in said location;
		//read groupsfile(s)		

		return groups.toArray(new String[groups.size()]);

	}*/

	/*
	public String[] getGroups2(String path){
		if(!checkDirSyntax(path, "")){
			return null;
		}
		File temp = new File(sdir,path);
		temp = new File(temp, "index.groups");
		FlushingFile groups = new FlushingFile(temp);
		return groups.loadAll();
	}
	 */
	private boolean checkDirSyntax(String path, String name) {
		if(
				path.contains("..") ||
				path.contains("//") ||
				path.contains("\\") ||

				name.contains(".") ||
				name.contains("/") ||
				name.contains("\\") 
		) 
			return false;

		return true;
	}

	private boolean checkFileSyntax(String name) {
		char c;
		boolean dot = false;
		for(int i = 0; i < name.length(); i++){
			c = name.charAt(i);
			//log.info("c["+c+"]");

			if(Character.isLetter(c))
				continue;
			//log.info(" !letter");

			if(Character.isDigit(c))
				continue;
			//log.info(" !digit");

			if(c == '-')
				continue;
			//log.info(" !-");

			if(c == '_')
				continue;
			//log.info(" !_");

			if(c == '.'){
				dot = true;
				continue;
			}
			//log.info(" !.");

			return false;
		}

		if(dot){
			if(name.contains(".."))
				return false;
		}

		return true;
	}

	public void runDbCheck() {
		if(!sdir.exists()){
			if(!sdir.mkdir()){
				ActionLog.error("could not create pagedb dir");
				log.fail("could not create pagedb dir");
			}
		}
	}

	public void runDirCleaner() {
		treedex.cleanDirs();	
	}

	public void runTempCleaner() {
		tempdex.clean();		
	}

	public void runPageCleaner() {
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return;
			}
		}
		log.info("Running Page Cleaner...");
		cleanIndex("");

	}
	private void cleanIndex(String path){
		log.info(" top ["+path+"]");
		//clean this
		IndexFile index = treedex.getIndexFile(path);
		index.clean();

		//clean subdirs
		String[] folders = treedex.getFolderNamesList(path);
		for(String sub : folders){
			cleanIndex(path+"/"+sub);
		}
	}

	public boolean updateData(CmsFile file) {
		log.info("pagedb updating data");
		return CmsFile.storeData(file, sdir);
		/*IndexFile index = getIndex(file.relativePath.getPath());
		if(index==null)
			return false;

		if(index.fileExists(file.name)){
			log.info(" file exits");
			String result;
			if((result = index.updateFile(file))!=null){
				log.fail("could not update data: "+ result);
				return false;
			}

			if((result = index.storeRecords())== null)
				return true;
			log.fail("could not store index: "+result);
			return false;
		}
		log.fail("file doesn't exist in index");
		return false;*/
	}

	public boolean updateMeta(CmsFile file) { //TODO: no need for index in updating meta unless renaming..
		return CmsFile.storeMeta(file, sdir);	

		/*IndexFile index = getIndex(file.relativePath.getPath());
		if(index==null)
			return false;
		if(index.fileExists(file.name)){
			String result;
			if((result = index.updateFile(file))!=null){
				log.fail("could not update meta: "+ result);
				return false;
			}
			//index.getRecord(file.name).type = file.type;
			index.changed = true;
			if((result = index.storeRecords())== null)
				return true;
			log.fail("could not store index: "+result);
			return false;
		}
		log.fail("file doesn't exist in index");
		return false;*/

	}

	public boolean store(){
		if(treedex.state.open){
			log.info("treedex state open");
			return treedex.store();
		}
		log.info("treedex state not open");
		//TODO: add tempdex store to this
		return false;
	}

	public Tree getTempTree() {
		if(!tempdex.state.open){
			if(tempdex.load() != null){
				log.fail("could not load tempdex");
				return null;
			}
		}
		return tempdex.temp_tree;
	}

	public File getDir(VirtualPath path) {
		return new File(sdir,path.getPath());
	}

}

