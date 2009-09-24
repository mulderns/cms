package d2o;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.Csv;
import util.Logger;
import cms.Cgicms;

public class FlushingDb {
	Logger log;

	private File source;
	private FlushingFile dbfile;
	private long lmod;

	//private ArrayList<FlushingRecord> records;
	//private HashSet<String> quick_list;

	private HashMap<String, FlushingRecord> records;

	private boolean loaded;

	public FlushingDb(String filename) {
		log = new Logger("FlushingDb[" + filename + "]");

		source = new File(Cgicms.database_dir, filename);
		dbfile = new FlushingFile(source);

		loaded = false;
	}

	public boolean add(String key, String[] data) {
		if (key == null)
			throw new NullPointerException("key is null");
		if (data == null)
			throw new NullPointerException("data is null");

		if (pol(key)) {
			log.info("key in use [" + key + "]");
			return false;
		}

		FlushingRecord record = new FlushingRecord(key, data);
		records.put(key, record);

		return store(record);
	}
	
	public boolean put(String key, String[] data) {
		if (key == null)
			throw new NullPointerException("key is null");
		if (data == null)
			throw new NullPointerException("data is null");

		if (!loaded)
			if(!load()){
				log.fail("could not load dbfile["+source+"]");
				return false;
			}
		
		FlushingRecord record = new FlushingRecord(key, data);
		records.put(key, record);

		return store();

	}

	public boolean mod(String key, String[] data) {
		if (key == null)
			throw new NullPointerException("key is null");
		if (data == null)
			throw new NullPointerException("data is null");

		if (!pol(key)) {
			log.info("key not found [" + key + "]");
			return false;
		}

		//TODO:
		FlushingRecord record = new FlushingRecord(key, data);
		records.put(key, record);
		
		return store();
	}

	public String[] get(String key) {
		if (key == null)
			throw new NullPointerException("key is null");

		if (!loaded)
			if(!load()){
				log.fail("could not load dbfile["+source+"]");
				return null;
			}

		return records.get(key).data;
	}

	/**
	 * 
	 * @param key	The key to be search for.
	 * @return		True if records contain such key. False if loading is not successfull
	 * 				or key is not found.
	 */
	public boolean pol(String key) {
		if (!loaded)
			if(!load()){
				log.fail("could not load dbfile["+source+"]");
				return false;
			}

		return records.containsKey(key);
	}

	public boolean del(String key) {
		if (key == null)
			throw new NullPointerException("key is null");

		if (!pol(key)) {
			log.info("key not found [" + key + "]");
			return false;
		}

		
		//TODO:
		if(records.remove(key)==null){
			log.fail("record not found");
			return false;
		}
		return store();
	}

	public List<String[]> all(){
		if (!loaded)
			if(!load()){
				log.fail("could not load dbfile["+source+"]");
				return null;
			}
		
		ArrayList<String[]> all = new ArrayList<String[]>();
		for(FlushingRecord r :records.values()){
			all.add(r.data);
		}
		return all;
	}
	
	private boolean load() {
		if (loaded)
			return false;

		records = new HashMap<String, FlushingRecord>();
		
		for (String recline : dbfile.loadAll()) {
			FlushingRecord rec = new FlushingRecord(Csv.decode(recline));
			records.put(rec.id, rec);
		}

		lmod = dbfile.getLastModified();
		loaded = true;
		
		//log.info("loaded userinfo:");
		//for(FlushingRecord r : records.values()){
		//	log.info(Arrays.toString(r.data));
		//}
		return true;
	}

	private boolean store() {
		String[] lines = new String[records.size()];
		int i = 0;
		for (FlushingRecord r : records.values()) {
			lines[i++] = Csv.encode(r.toArray());
		}

		return (dbfile.overwrite(lines, lmod) == null);
	}

	private boolean store(FlushingRecord record) {
		if (record == null)
			throw new NullPointerException("record is null");
		return (dbfile.append(Csv.encode(record.toArray()), lmod) == null);
	}

}
