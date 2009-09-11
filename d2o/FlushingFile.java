package d2o;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import util.ActionLog;
import util.Logger;
import util.Utils;

public class FlushingFile {
	private static final String linesep = System.getProperty("line.separator");
	private final Logger log;

	private final File flush_file;
	private long last_modified;
	//private long original_modified;

	//private ArrayList<FlushingRecord> records;

	/*public FlushingDb2(String filename){
		log = new Logger2("FlushingDb["+filename+"]");
		db_file = new File(filename);
	}*/

	public FlushingFile(File file) {
		log = new Logger("FlushingFile[" + file.getName() + "]");
		flush_file = file;
	}

	private final boolean aquireLock(final File file) throws IOException {
		File lock = new File(flush_file.getParentFile(),flush_file.getName() + ".lock");

		int trys = 5;
		while (true) {
			if (trys < 1) {
				if(lock.exists()){
					if((System.currentTimeMillis() - lock.lastModified()) > 5000){
						if(lock.delete()){
							if(lock.createNewFile()){
								return true;
							}
						}
					}
				}

				ActionLog.error("aquireLock: out of trys");
				return false;
			}
			if (!lock.createNewFile()) {
				Utils.sleep(100);
				trys--;
			} else {
				return true;
			}
		}
	}

	private final boolean removeLock(final File file) {
		final File lock = new File(flush_file.getParentFile(),flush_file.getName() + ".lock");

		int trys = 5;
		while (true) {
			if (trys < 1) {
				ActionLog.error("removeLock: out of trys");
				return false;
			}
			if (!lock.delete()) {
				if(!lock.exists()){
					return true;
				}
				Utils.sleep(100);
				trys--;
			} else {
				return true;
			}
		}
	}

	public final String append(final String line) {
		try {
			if (!flush_file.exists()) {
				log.info("creating new file");
				if (!flush_file.createNewFile()) {
					return "could not create new file["
					+ flush_file.getAbsolutePath() + "]";
				}
			}
			if (!aquireLock(flush_file))
				return "could not aquire file lock";

			BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(flush_file, true), "ISO-8859-1"));
			bout.write(line);
			bout.write(linesep);
			bout.close();

			if (!removeLock(flush_file)) {
				log.fail("could not remove lock for ["
						+ flush_file.getAbsolutePath() + "]");
			}
			return null;

		} catch (IOException ioe) {
			log.fail("ioexception appending :" + ioe);
		}

		return "append failed";
	}

	public final String append(final String line, long last_modified) {
		//TODO:
		return append(line);
	}
	
	public final String overwrite(final String[] lines) {
		if(lines == null)
			return "null data to write";
		try {
			if (!flush_file.exists()) {
				log.info("creating new file");
				if (!flush_file.createNewFile()) {
					return "could not create new file["
					+ flush_file.getAbsolutePath() + "]";
				}
			}
			if (!aquireLock(flush_file))
				return "could not aquire file lock";

			//System.err.println("#### flushwriting ["+lines.length+"] lines");

			BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(flush_file, false), "ISO-8859-1"));
			for (String line : lines) {
				//System.err.println("# ["+line+"]");
				bout.write(line);
				bout.write(linesep);
			}
			bout.close();

			if (!removeLock(flush_file)) {
				log.fail("could not remove lock for ["
						+ flush_file.getAbsolutePath() + "]");
			}
			return null;

		} catch (IOException ioe) {
			log.fail("ioexception appending :" + ioe);
		}

		return "overwrite failed";
	}

	public String overwrite(String[] lines, long lmod) {
		//TODO:
		return overwrite(lines);
		
	}
	
	public final String overwrite(final String line) {
		if(line == null)
			return "null data to write";
		try {
			if (!flush_file.exists()) {
				log.info("creating new file");
				if (!flush_file.createNewFile()) {
					return "could not create new file["
					+ flush_file.getAbsolutePath() + "]";
				}
			}
			if (!aquireLock(flush_file))
				return "could not aquire file lock";


			BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(flush_file, false), "ISO-8859-1"));
			bout.write(line);
			bout.close();

			if (!removeLock(flush_file)) {
				log.fail("could not remove lock for ["
						+ flush_file.getAbsolutePath() + "]");
			}
			return null;

		} catch (IOException ioe) {
			log.fail("ioexception appending :" + ioe);
		}

		return "overwrite failed";
	}

	public final String[] loadAll() {
		final ArrayList<String> temp = new ArrayList<String>();

		final File lock = new File(flush_file.getName() + ".lock");

		int trys = 5;
		try {
			if (!flush_file.exists()) {
				log.fail("l_entries: file does not exist ["
						+ flush_file.getName() + "]");
				flush_file.createNewFile();
				return new String[0];
			}

			while (true) {
				if (trys < 1) {
					ActionLog.error("out of trys");
					return null;
				}
				if (lock.exists()) {
					Utils.sleep(100);
					trys--;
				} else {
					break;
				}
			}

			last_modified = flush_file.lastModified();
			
			BufferedReader bin = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(flush_file), "ISO-8859-1")
			);
			String line;
			while ((line = bin.readLine()) != null) {
				temp.add(line);
			}
			bin.close();
			return temp.toArray(new String[temp.size()]);

		} catch (IOException ioe) {
			log.fail("failed to load file :" + ioe);
		}

		return null;
	}

	public String delete() {
		try {
			if (!flush_file.exists()) {
				return "nothing to delete ["
				+ flush_file.getAbsolutePath() + "]";
			}

			if (!aquireLock(flush_file))
				return "could not aquire file lock";

			flush_file.delete();

			if (!removeLock(flush_file)) {
				log.fail("could not remove lock for ["
						+ flush_file.getAbsolutePath() + "]");
			}
			return null;

		} catch (IOException ioe) {
			log.fail("ioexception appending :" + ioe);
		}

		return "delete failed";

	}

	public final BufferedReader initRead() {
		try {
			if (!flush_file.exists()) {
				log.info("creating new file");
				if (!flush_file.createNewFile()) {
					log.fail("could not create new file["
							+ flush_file.getAbsolutePath() + "]");
					return null;
				}
			}
			if (!aquireLock(flush_file)){
				log.fail("could not aquire file lock");
				return null;
			}

			BufferedReader bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(flush_file), "ISO-8859-1"));
			return bin;

		} catch (IOException ioe) {
			log.fail("failed to initialize read on file :" + ioe);
		}
		log.fail("init not successfull ["+flush_file.getName()+"]");
		return null;
	}

	public final boolean endRead(BufferedReader bin){
		try{
			bin.close();
			if (!removeLock(flush_file)) {
				log.fail("could not remove lock for ["
						+ flush_file.getAbsolutePath() + "]");
				return false;
			}
			return true;
		}catch (IOException ioe) {
			log.fail("ioexception while closing stream");
		}
		return false;
	}
	
	public final long getLastModified(){
		return last_modified;
	}


}
