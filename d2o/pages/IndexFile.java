package d2o.pages;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import d2o.FlushingDb;

public class IndexFile {
	private FlushingDb indexdb;
	public File dir;
	public File file;

	public IndexFile(File dir) {
		this.dir = dir;
		file = new File(dir, "index");
		indexdb = new FlushingDb(file);
	}

	public List<String[]> getRecords() {
		return indexdb.all();
	}

	public boolean addRecord(CmsFile file) {
		if (indexdb.pol(file.name)) {
			return false;
		}

		IndexRecord record = new IndexRecord(file.name, "");

		if (!indexdb.add(record.filename, record.toArray())) {
			return false;
		}

		return true;
	}

	public String removeRecord(String filename) {
		if (!indexdb.pol(filename)) {
			return "file not found";
		}

		if (!indexdb.del(filename)) {
			return "could not delete record";
		}
		return null;

	}

	public boolean fileExists(String filename) {
		return indexdb.pol(filename);
	}

	public boolean setStatus(String name, char status){
		if (!indexdb.pol(name)) {
			return false;
		}
		IndexRecord record = new IndexRecord(indexdb.get(name));
		indexdb.del(name);
		record.status = status;
		return indexdb.add(name, record.toArray());
	}
	
	public IndexRecord getRecord(String filename) {
		if (!indexdb.pol(filename)) {
			return null;
		}
		return new IndexRecord(indexdb.get(filename));
	}

	public boolean renameRecord(String name, String uusinimi) {
		if (!indexdb.pol(name)) {
			return false;
		}

		if (indexdb.pol(uusinimi)) {
			return false;
		}

		IndexRecord record = new IndexRecord(indexdb.get(name));
		indexdb.del(name);
		record.filename = uusinimi;
		return indexdb.add(uusinimi, record.toArray());
	}

	public void clean() {

		//remove duplicates from records
		HashSet<String> found_pool = new HashSet<String>();
		ArrayList<IndexRecord> delete_these = new ArrayList<IndexRecord>();
		for (String[] raw : indexdb.all()) {
			IndexRecord record = new IndexRecord(raw);
			if(record.status == 'e')
				continue;
			System.err.println("#### record - [" + record.filename + "]");
			if (found_pool.contains(record.filename)) {
				System.err.println(" -> delete_this");
				delete_these.add(record);
			} else {
				System.err.println(" -> unique");
				found_pool.add(record.filename);
			}
		}
		for (IndexRecord target : delete_these) {
			indexdb.del(target.filename);
		}

		HashSet<String> file_list = new HashSet<String>(Arrays.asList(dir
				.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return (name.startsWith("page.") ? true : false);
					}
				})));

		HashSet<IndexRecord> false_in_index = new HashSet<IndexRecord>();
		HashSet<String> missing_data = new HashSet<String>();
		HashSet<String> missing_meta = new HashSet<String>();
		HashSet<String> complete_records = new HashSet<String>();

		//collect complete records separate partial records		
		for (String[] raw : indexdb.all()) {
			IndexRecord record = new IndexRecord(raw);
			if(record.status == 'e')
				continue;
			System.err.println("#### - record [" + record.filename + "]");
			if (!file_list.contains("page.data." + record.filename)) {
				System.err.println("      not found in file list [page.data."
						+ record.filename + "]");
				false_in_index.add(record);
				if (file_list.contains("page.meta." + record.filename)) {
					System.err.println("       removing meta [page.meta."
							+ record.filename + "]");
					missing_data.add("page.meta." + record.filename);
				}
			} else {
				if (file_list.contains("page.meta." + record.filename)) {
					complete_records.add("page.meta." + record.filename);
					complete_records.add("page.data." + record.filename);
				} else {
					System.err.println("       orphaned data [page.data."
							+ record.filename + "]");
					missing_meta.add("page.data." + record.filename);
				}
			}
		}

		System.err.println("#### - get missing full files");

		for (IndexRecord target : false_in_index) {
			indexdb.del(target.filename);
		}

		file_list.removeAll(missing_data);
		file_list.removeAll(complete_records);

		//import missing full files to index
		HashSet<String> complete_nonindexed = new HashSet<String>();
		for (String s : file_list) {
			System.err.println("#### - [" + s + "]");
			if (s.startsWith("page.data.")) {
				if (file_list.contains("page.meta" + s.substring(9))) {
					System.err.println("#### - complete");
					complete_nonindexed.add(s);
					complete_nonindexed.add("page.meta" + s.substring(9));
					continue;
				}
				System.err.println("#### - incomplete");
			}
		}

		file_list.removeAll(complete_nonindexed);
		//add full (and partial) files to index
		System.err.println("#### - add full files to index");
		for (String s : complete_nonindexed) {
			if (s.startsWith("page.data.")) {
				System.err.println("#### - " + s);
				try {
					IndexRecord ir = new IndexRecord(s.substring(10));
					indexdb.add(ir.filename, ir.toArray());

				} catch (Exception e) {
					System.err.println(e);
				}
			}
		}

		System.err.println("#### - removing rest");
		//remove actual file that are in incomplete and in file_list(residue)
		for (String s : missing_data) {
			System.err.println("#### - " + s);
			File f = new File(dir, s);
			if (!f.delete()) {
				System.err.println("    error could not delete " + s);
			}
		}

		for (String s : file_list) {
			System.err.println("#### - " + s);
			File f = new File(dir, s);
			if (!f.delete()) {
				System.err.println("    error could not delete " + s);
			}
		}
	}
}
