package d2o.pages;

import java.io.File;
import java.util.Iterator;

import d2o.FlushingFile;

import util.Logger;
import util.TriState;

public class Treedex {
	private final FlushingFile index_file;
	private final File root_dir;
	public TriState state;

	private Logger log;

	Tree dir_tree;

	//private String dir_orig;

	public Treedex(File root_dir){
		this.root_dir = root_dir;
		index_file = new FlushingFile(new File(root_dir,"index.treedex"));
		state = new TriState();
		log = new Logger("Treedex");
	}

	public String load(){
		if(!state.open){
			String[] lines = index_file.loadAll();
			if(lines.length > 0){
				//dir_orig = lines[0];
				if((dir_tree = Tree.parseTree(lines[0]))== null){
					return "error parsing dir tree";
				}
			}else{
				//dir_orig = "";
				dir_tree = new Tree("root");
			}
			state.reset();
			state.touch();
			return null;
		}
		return "will not load: state open";
	}

	public boolean store() {
		if(state.open){
			String[] lines = new String[1];
			lines[0] = dir_tree.toString();
			return (index_file.overwrite(lines) == null);
		}
		log.fail("will not store: state not open");
		return false;
	}

	public boolean addDir(String path, String name){
		Tree temp;
		if((temp = getTree(path)) != null){
			if(temp.addChild(name)){
				return true;
			}			
		}
		return false;
	}

	public boolean deleteDir(String path) {
		Tree temp;
		if((temp = getTree(path)) != null){
			String name = temp.name;
			temp = temp.parent;
			if(temp != null && temp.removeChild(name)){
				return true;
			}
		}
		return false;
	}

	public boolean deleteDir(String path, String name) {
		Tree temp;
		if((temp = getTree(path)) != null){
			return temp.removeChild(name);
		}
		return false;
	}

	public Tree getTree(String path){
		String[] paths = path.split("/");
		Tree temp = dir_tree;
		if(path.equals(""))
			return temp;
		for(int i = 0; i < paths.length; i++){
			if( (temp = temp.getChild(paths[i])) == null)
				return null;
		}
		return temp;
	}

	public File getFolder(String path){
		//TODO: treedex getFolder(String path)
		return null;
	}
	
	public IndexFile getIndexFile(String path){
		//TODO: IndexFile getIndexFile(String path)
		
		File temp = new File(root_dir,path);
		if(temp.exists() && temp.isDirectory()){
			File index = new File(temp,"index");
			if(index.exists()){
				IndexFile indexfile = new IndexFile(temp);
				return indexfile;
			}
		}
		
		return new IndexFile(temp);
	}
	
	
	public String[] getFolderNamesList(String path) {
		Tree temp = getTree(path);
		log.fail("treedex-getfolders["+path+"]:"+(temp==null?"null":temp.toString()));
		if (temp == null || temp.subs == null){
			log.fail("treedex-getfolders:returning empty");
			return new String[0];
		}

		String[] arr = new String[temp.subs.size()];
		Iterator<Tree> iter = temp.subs.iterator();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = iter.next().name; 
		}
		return arr;

	}

	public boolean pingDir(String path) {
		log.info("pinging ["+path+"]");
		
		if(path.length()<1)
			return true;
		
		if(path.startsWith("/")){
			if(path.length() == 1)
				return true;
			path = path.substring(1);
		}
		
		String[] paths = path.split("/");
		Tree temp = dir_tree;
		log.info("paths["+paths.length+"]");
		if(paths.length > 0){
			for(int i = 0; i < paths.length; i++){
				if( (temp = temp.getChild(paths[i])) == null)
					return false;
			}
			return true;
		}
		return false;
	}

	public void cleanDirs(){
		if(state.pure)
			load();
		Tree tree = new Tree("root");
		for(File f : root_dir.listFiles()){
			if(f.isDirectory()){
				tree.addChild(f.getName());
				cleanDirs(f,tree.getLast());
			}
		}
		dir_tree = tree;
		log.info(dir_tree.toString());
	}

	private void cleanDirs(File dir,Tree tree){
		for(File f : dir.listFiles()){
			if(f.isDirectory()){
				tree.addChild(f.getName());
				cleanDirs(f,tree.getLast());
			}
		}
	}

}
