package d2o.pages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/*
import cms.Cgicms;
import cms.FileHive;
 */
import d2o.FlushingFile;

import util.Csv;
import util.TriState;

public class IndexFile {
	private FlushingFile ff;
	private File file;
	File dir;
	private ArrayList<IndexRecord> records;
	boolean changed;

	private TriState state;

	public IndexFile(File dir){
		this.dir = dir;
		file = new File(dir, "index");
		ff = new FlushingFile(file);
		changed = false;
		state = new TriState();
	}

	/*
	public IndexFile(Path path){
		file = path.getFile();
		ff = new FlushingFile(file);
		changed = false;
		state = new TriState();
	}*/

	//filename,modified

	public ArrayList<IndexRecord> getRecords() {
		return records;
	}
	
	public String loadRecords(){
		if(state.open)
			return "will not load, state open ["+state+"]";

		records = new ArrayList<IndexRecord>();
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException ioe){
				return "failed to create file["+file+"]";
			}
		}

		String[] data = ff.loadAll();
		if(data == null)
			return "could not load database";
		for(String s: data){
			IndexRecord r;
			if((r = parseRecord(s)) != null){
				records.add(r);
			}
		}

		changed = false;
		state.reset();
		state.touch();

		Collections.sort(
				records,

				new Comparator<IndexRecord>(){
					public int compare(IndexRecord arg0, IndexRecord arg1) {

						if(arg0.filename.equals(arg1.filename))
							return arg0.filename.compareTo(arg1.filename);
						return 0;
					}
				}
		);
		return null;
	}

	//new which creates the entry and stores index.
	public String addRecord(CmsFile file){
		for(IndexRecord ir : records){
			if(ir.filename.equals(file.name)){
				return "filename in use";
			}
		}
		IndexRecord ir = new IndexRecord(file.name);
		ir.modified = true;
		records.add(ir);
		changed = true;
		return null;
	}

/*
	public String updateFile(CmsFile file) {
		System.err.println("#### - indexfile updating file");
		for(IndexRecord ir : records){
			if(ir.filename.equals(file.name)){
				System.err.println("#### - found record");
				//ir.group = file.group;
				ir.modified = true;
				//records.add(ir);
				changed = true;
				
				if(CmsFile.storeMeta(file, dir)){
					if(CmsFile.storeData(file, dir)){
						return null;
					}else{
						file.delete(dir);
						return "could not write data ["+file.name+"]";
					}
				}else{
					return "could not write meta ["+file.name+"]";
				}

			}
		}	
		return "file not found";
	}
*/
	public String removeRecord(String filename){
		if(!state.open){
			loadRecords();
		}
		if(!state.open){
			return "state not open["+state+"]";
		}
		for(IndexRecord ir : records){
			if(ir.filename.equals(filename)){
				records.remove(ir);
				changed = true;
				return null;
			}
		}
		return "not found["+filename+"] in ["+file+"] size["+records.size()+"]";
	}

	public String storeRecords(){
		if(state.open){
			if(changed){
				String[] temp = new String[records.size()];
				Iterator<IndexRecord> iter = records.iterator();
				for(int i = 0; i < temp.length; i++) {
					temp[i] = iter.next().toString();
				}

				String result = ff.overwrite(temp);
				if(result == null){
					state.touch();
					return null;
				}
				return result;
			}
			return "will not store: nothing changed";
		}
		return "will not store: state not open";
	}

	private IndexRecord parseRecord(String line){
		final String[] stuff = Csv.decode(line);
		if(stuff.length == 2){
			IndexRecord ir = new IndexRecord(stuff[0]);
			ir.dir = dir;
			if(stuff[1].equals("+")){
				ir.modified = true;
			}
			return ir;
		}else{
			System.err.println("oddity["+stuff.length+"]");
			return null;
		}
	}

	public boolean fileExists(String filename) {
		for(IndexRecord ir:records){
			if(ir.filename.equals(filename))
				return true;
		}
		return false;
	}

	public IndexRecord getRecord(String string) {
		for(IndexRecord ir:records){
			if(ir.filename.equals(string))
				return ir;
		}
		return null;
	}


	public void renameRecord(String name, String uusinimi) {
		IndexRecord record = getRecord(name);
		record.filename = uusinimi;
		changed = true;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(dir.getPath());
		sb.append("-");
		for(IndexRecord ir:records){
			sb.append(ir.filename);
			sb.append(",");
		}
		return sb.toString();
	}

	public void clean() {
		if(!state.open)
			loadRecords();
		
		//remove duplicates from records
		HashSet<String> found_pool = new HashSet<String>();
		ArrayList<IndexRecord> delete_these = new ArrayList<IndexRecord>();
		for(IndexRecord record : records){
			System.err.println("#### record - ["+record.filename+"]");
			if(found_pool.contains(record.filename)){
				System.err.println(" -> delete_this");
				delete_these.add(record);
			}else{
				System.err.println(" -> unique");
				found_pool.add(record.filename);
			}
		}
		records.removeAll(delete_these);
		
		HashSet<String> file_list = new HashSet<String>(Arrays.asList(dir.list(new FilenameFilter(){
			public boolean accept(File dir, String name) {return (name.startsWith("page.")?true:false);}
		})));
		
		HashSet<IndexRecord> false_in_index = new HashSet<IndexRecord>();
		HashSet<String> missing_data = new HashSet<String>();
		HashSet<String> missing_meta = new HashSet<String>();
		HashSet<String> complete_records = new HashSet<String>();

		//collect complete records separate partial records		
		for(IndexRecord record : records){
			System.err.println("#### - record ["+record.filename+"]");
			if(!file_list.contains("page.data."+record.filename)){
				System.err.println("      not found in file list [page.data."+record.filename+"]");
				false_in_index.add(record);
				if(file_list.contains("page.meta."+record.filename)){
					System.err.println("       removing meta [page.meta."+record.filename+"]");
					missing_data.add("page.meta."+record.filename);
				}
			}else{
				if(file_list.contains("page.meta."+record.filename)){
					complete_records.add("page.meta."+record.filename);
					complete_records.add("page.data."+record.filename);
				}else{
					System.err.println("       orphaned data [page.data."+record.filename+"]");
					missing_meta.add("page.data."+record.filename);
				}
			}
		}

		System.err.println("#### - get missing full files");
		records.removeAll(false_in_index);
		file_list.removeAll(missing_data);
		file_list.removeAll(complete_records);

		//import missing full files to index
		HashSet<String> complete_nonindexed = new HashSet<String>();
		for(String s : file_list){
			System.err.println("#### - ["+s+"]");
			if(s.startsWith("page.data.")){
				if(file_list.contains("page.meta"+s.substring(9))){
					System.err.println("#### - complete");
					complete_nonindexed.add(s);
					complete_nonindexed.add("page.meta"+s.substring(9));
					continue;
				}
				System.err.println("#### - incomplete");
			}
		}
		
		file_list.removeAll(complete_nonindexed);
		//add full (and partial) files to index
		System.err.println("#### - add full files to index");
		for(String s : complete_nonindexed){
			if(s.startsWith("page.data.")){
				System.err.println("#### - "+s);
				try{
					IndexRecord ir = new IndexRecord(s.substring(10));
					//FlushingFile flush = new FlushingFile(new File(dir,"page.meta."+ir.filename));
					//String[] meta = Csv.decode(flush.loadAll()[0]);
					//ir.parent = meta[2];
					ir.modified = true;
					records.add(ir);
					changed = true;
				}catch(Exception e){
					System.err.println(e);
				}
			}
		}

		System.err.println("#### - removing rest");
		//remove actual file that are in incomplete and in file_list(residue)
		for(String s:missing_data){
			System.err.println("#### - "+s);
			File f = new File(dir,s);
			if(!f.delete()){
				System.err.println("    error could not delete "+s);
			}
		}

		for(String s:file_list){
			System.err.println("#### - "+s);
			File f = new File(dir,s);
			if(!f.delete()){
				System.err.println("    error could not delete "+s);
			}
		}
		changed = true;
		System.err.println("soring : "+storeRecords());
	}



}


