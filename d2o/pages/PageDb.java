package d2o.pages;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

	
	public boolean createDir(String path, String name){
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return false;
			}
		}

		if(!checkDirSyntax(path, name)){
			log.fail("dir syntax fail");
			return false;
		}

		if(!treedex.pingDir(path)){
			log.fail("parent dir doesn't exist");
			return false;
		}

		if(treedex.addDir(path,name)){
			File temp = new File(sdir,path);
			temp = new File(temp,name);
			if(temp.mkdir()){
				actions.append("+d,"+path+"/"+name+","+Cgicms.datarelay.username);
				return true;
			}
			log.fail( "could not create directory["+temp.getAbsolutePath()+"]");
			return false;
		}else{
			log.fail( "could not add directory to db");
			return false;
		}
	}

	public boolean removeDir(String path) {
		if(!treedex.state.open){
			if(treedex.load() != null){
				log.fail("could not load treedex");
				return false;
			}
		}

		if(!checkDirSyntax(path, "")){
			log.fail("dir syntax fail");
			return false;
		}

		if(path.length() == 0){
			log.info("can't delete root folder");
			return false;
		}

		File temp = new File(sdir,path);

		if(temp.exists() && temp.isDirectory()){
			for(File file : temp.listFiles()){
				if(!file.delete()){
					log.fail("could not remove ["+file.getAbsolutePath()+"]");
					return false;
				}
			}
			if(temp.delete()){
				if(treedex.deleteDir(path)){
					actions.append("-d,"+path+","+Cgicms.datarelay.username);
					return true;
				}
				log.fail("could not remove directory from db");
				
			}else{
				log.fail("could not remove directory["+temp.getAbsolutePath()+"]");
			}
		}else{
			log.fail("directory ["+temp+"] doesn't exist or is not a directory");
		}
		
		return false;
	}

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

	public boolean addFile(String path, CmsFile file){
		if(!checkDirSyntax(path, "")){
			log.fail("dir syntaxerror");
			return false;
		}
		if(!checkFileSyntax(file.name)){
			log.fail("name syntax error");
			return false;
		}

		IndexFile index = getIndex(path);
		if( !index.addRecord(file))
			return false;

		if(!CmsFile.storeMeta(file, index.dir))
			return false;
		if(!CmsFile.storeData(file, index.dir))
			return false;

		actions.append("+f,"+path+"/"+file.name+","+Cgicms.datarelay.username);
		return true;
	}

	public CmsFile getFileMeta(VirtualPath path){
		log.info("getFileMeta...");

		if(path.getFilename().equals("")){
			log.fail("no file specified");
			return null;
		}
		log.info("load File ["+path.getUrl()+"]");
		return CmsFile.loadFile(path,sdir);
	}

	private IndexFile getIndex(String path) {
		IndexFile index = new IndexFile(new File(sdir, path));
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

		ArrayList<String> temp = new ArrayList<String>();

		for(String[] raw : index.getRecords()){
			IndexRecord record = new IndexRecord(raw);
			temp.add(record.filename);
			log.info("record >> "+record);
		}
		Collections.sort(temp);
		return temp.toArray(new String[0]);
	}

	public IndexRecord[] getFileRecords(String path) {
		log.info("getting filename list for ["+path+"]");
		if(!checkDirSyntax(path, "")){
			log.info("invalid dir syntax");
			return null;
		}

		File dir = new File(sdir,path);
		IndexFile index = new IndexFile(dir);

		ArrayList<IndexRecord> temp = new ArrayList<IndexRecord>();

		for(String[] raw : index.getRecords()){
			IndexRecord record = new IndexRecord(raw);
			temp.add(record);
		}
		Collections.sort(temp,new Comparator<IndexRecord>(){
			public int compare(IndexRecord o1, IndexRecord o2) {
				return o1.filename.compareTo(o2.filename);
			}
		});
		return temp.toArray(new IndexRecord[0]);
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
			return null;
		}
		return "error while renaming the file";
	}

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

	public boolean fileExists(VirtualPath vpath) {
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
			File dir = new File(sdir,sb.toString());
			File index = new File(dir,"index");
			if(index.exists()){
				log.info(" 3");
				IndexFile dex = new IndexFile(dir);

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

	public String removeFile(String path, String name){
		checkDirSyntax(path, name);

		IndexFile index = new IndexFile(new File(sdir,path));
		String result;

		if( (result = index.removeRecord(name)) != null)
			return result;

		File dir = new File(sdir,path);
		File meta = new File(dir,"page.meta."+name);
		File data = new File(dir,"page.data."+name);

		StringBuilder errors = new StringBuilder();

		if(!FileOps.archive(data)){
			errors.append("could not remove data["+data.getAbsolutePath()+"]\n");
		}

		if(!FileOps.archive(meta)){
			errors.append("could not remove meta["+meta.getAbsolutePath()+"]");
		}
		if(errors.length() > 0){
			return errors.toString();
		}
		return null;
	}
	
	public boolean checkDirSyntax(String path, String name) {
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

	public boolean checkFileSyntax(String name) {
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
	}

	public boolean updateMeta(CmsFile file) {
		return CmsFile.storeMeta(file, sdir);	
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

	public void deleteIndexes() {
		getIndex("/").file.delete();
		for(String dir : getDirList("/")){
			getIndex(dir).file.delete();
		}
		
	}

}

