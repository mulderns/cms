package d2o.pages;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import util.Logger;
import util.TriState;
import d2o.FlushingFile;

public class Tempdex {
	private final FlushingFile index_file;
	private File root_dir;
	public TriState state;

	private Logger log;
	Tree temp_tree;

	public Tempdex(File root_dir){
		this.root_dir = root_dir;
		index_file = new FlushingFile(new File(root_dir,"index.tempdex"));
		state = new TriState();
		log = new Logger("Tempdex");
	}

	public String load(){
		if(!state.open){
			String[] lines = index_file.loadAll();
			if(lines.length > 0){
				if((temp_tree = Tree.parseTree(lines[0]))== null){
					return "error parsing temp tree";
				}
			}else{
				temp_tree = new Tree("root");
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
			lines[0] = temp_tree.toString();
			String result = index_file.overwrite(lines);
			if ( result == null){
				return true;
			}
			log.fail(result);
			return false;
		}
		log.fail("will not store: state not open");
		return false;
	}


	public String[] getTemplateNames() {
		ArrayList<String> templates = temp_tree.asList(new ArrayList<String>());
		templates.remove("root");
		return templates.toArray(new String[templates.size()]);

	}

	public boolean templateExists(String name) {
		for(String node : temp_tree.getChildArray()){
			if(node.equals(name))
				return true;
		}
		return false;
	}


	public String createTempEntry(String filename) {
		if(templateExists(filename))
			return "name in use allready["+filename+"]";
		if(!temp_tree.addChild(filename)){
			return "could not add template to index";
		}
		return null;
	}

	public String createTempEntry(TemplateFile file) {

		if(templateExists(file.name))
			return "name in use allready["+file.name+"]";
		if(!temp_tree.addChild(file.name)){
			return "could not add template to index";
		}
		return null;
	}


	public TemplateFile getTemplate(String name) {
		for(String node : temp_tree.getChildArray()){
			if(node.equals(name)){
				TemplateFile file = new TemplateFile(name);
				if(!file.load(root_dir)){
					log.fail("template load failed");
					return null;
				}
				//log.info("template data length["+file.data.length+"]");
				return file;
			}
		}
		return null;
	}

	public TemplateFile getTemplateMeta(String name) {
		for(String node : temp_tree.getChildArray()){
			if(node.equals(name)){
				TemplateFile file = new TemplateFile(name);

				if(!file.loadMeta2(root_dir)){ //TODO:change
					log.fail("template load failed");
					return null;
				}

				return file;
			}
		}
		return null;
	}

	public String removeTemplate(String name){
		TemplateFile tfile;
		if((tfile = getTemplate(name))!=null){
			if(!tfile.delete(root_dir)){
				return "could not remove template files";
			}

			System.err.println("");
			System.err.println("");
			System.err.println(temp_tree.toString());
			System.err.println("");
			System.err.println("");
			System.err.println(temp_tree.hasChildren());
			for(String s : temp_tree.getChildArray()){
				System.err.println(s);
			}
			System.err.println("");
			System.err.println("");

			if(!temp_tree.deepRemove(name)){
				return "template could not be removed from index";
			}
			return null;
		}
		return ("template not found");
	}

	public File getDir() {
		return root_dir;
	}

	public void clean(){
		log.info("running tempcleaner...");
		if(!state.open)
			load();
		if(!state.open){
			log.fail("clean could not load tempdex");
			return;
		}

		ArrayList<String> temp_list = temp_tree.asList(new ArrayList<String>());

		TreeSet<String> temp_set = new TreeSet<String>(temp_list);

		TreeSet<String> files = new TreeSet<String>(Arrays.asList(root_dir.list(
				new FilenameFilter(){public boolean accept(File dir, String name){
					if(name.startsWith("temp.meta.")||name.startsWith("temp.data.")){
						return true;
					}
					return false;	
				}}
		)));

		// put aside all complete records
		log.info("check complete files...");
		for(String temp : temp_list){
			log.info(temp);
			if(files.contains("temp.meta."+temp)&&files.contains("temp.data."+temp)){
				files.remove("temp.meta."+temp);
				files.remove("temp.data."+temp);
				temp_set.remove(temp);
				log.info("..ok");
			}
		}

		//from remaining, remove those with only meta data (regardless of index)
		log.info("remove abandoned meta files...");
		for(String f : files.toArray(new String[0])){
			log.info(f);
			if(f.startsWith("temp.meta.")){
				files.remove(f);
				if(new File(root_dir, f).delete()){
					log.info("..del");					
				}else{
					log.fail("..fail");
				}

			}
		}

		//remove void index entries (no data); possibly combine this and previous check
		log.info("remove void index entries...");
		for(String tempname : temp_set.toArray(new String[0])){
			log.info(tempname);
			if(!new File(root_dir, "temp.data."+tempname).exists()){
				if(temp_tree.deepRemove(tempname)){
					log.info("..del");
				}else{
					log.fail("..fail");
				}				
				temp_set.remove(tempname);
			}
		}

		//add missing "temp.data." files to index
		log.info("add missing data to index...");
		for(String dfile : files.toArray(new String[0])){
			String name = dfile.substring(10);
			createTempEntry(name);

			FlushingFile meta = new FlushingFile(new File(root_dir,"temp.meta."+name));
			meta.append("file,"+name);
			log.info(dfile+"..recover");
		}

		log.info("store ["+store()+"]");
	}



	public boolean rename(String oldname, String newname) {
		Tree node = temp_tree.getNode(oldname);

		if(node == null)
			return false;
		
		node.name = newname;

		File oldfile = new File(root_dir, "temp.meta."+oldname);
		File newfile = new File(root_dir, "temp.meta."+newname);
		oldfile.renameTo(newfile);

		oldfile = new File(root_dir, "temp.data."+oldname);
		newfile = new File(root_dir, "temp.data."+newname);
		oldfile.renameTo(newfile);

		log.info(" updating metadata");
		TemplateFile meta = getTemplateMeta(newname);
		meta.name = newname;
		if(!updateTemplateMeta(meta)){
			log.fail("failed metadata update ["+newname+"]");
		}

		if(node.hasChildren()){
			log.info("updating child metadata");
			for(Tree child : node.subs){
				log.info(" ["+child.name+"]");
				meta = getTemplateMeta(child.name);
				meta.parent = newname;
				if(!updateTemplateMeta(meta)){
					log.fail("failed child meta update ["+child.name+"]");
				}
			}
		}

		return true;
	}

	private boolean updateTemplateMeta(TemplateFile meta) {
		return CmsFile.storeMeta(meta, root_dir);
	}

	public String updateParent(TemplateFile tfile) {
		if(tfile.parent != null){
			log.info("file has parent ["+tfile.parent+"]");
			Tree parentnode;
			if((parentnode = temp_tree.getNode(tfile.name)) != null){
				if( (parentnode = parentnode.getParent()) != null ){
					Tree newparentnode = temp_tree.getNode(tfile.parent);
					if(newparentnode != null){
						Tree node = temp_tree.getNode(tfile.name);
						if(!parentnode.removeChild(tfile.name)){
							log.fail("could not remove child from parent");
						}
						if(!newparentnode.addChildNode(node)){
							log.fail("could not add child to new parent");
						}
						if(store()){
							log.info("store success");
						}else{
							log.fail("store failed");
							return "store failed";
						}
						return null;
					}else{
						log.fail("new parentnode not found");
					}
				}else{
					log.fail("old parentnode not found");
				}
			}else{
				log.fail("node not found");
				return "index out of sync erro#¤\"%! ";
			}
		}else{
			log.info("no parent to update");
			return null;
		}
		log.fail("what gives?");
		return "";
	}

}
