package cms;

import http.FormPart;
import http.HttpRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.Logger;
import util.Utils;
import d2o.FileRecord;
import d2o.FlushingDb;

/**
 * index
 add file
 rename file
 remove file

store from part

send file

genfilename

 */

public class FileHive {
	private static boolean initiated = false;
	private static FileHive current;

	private Logger log;
	private File hive_dir;
	private String index_file;
	private FlushingDb filedb;

	private static final String prefix = "file";

	public static FileHive getFileHive() {
		if (initiated) {
			return current;
		} else {
			initiated = true;
			current = new FileHive();
			return current;
		}
	}

	private FileHive() {
		log = new Logger("FileHive");

		hive_dir = Cgicms.uploaded_dir;
		index_file = "file_index";
		filedb = new FlushingDb(index_file);

		log.info("init hive[" + hive_dir.getName() + "]");
	}

	public boolean addFile(String user, boolean public_access,
			String access_groups, FormPart part) {
		FileRecord record = new FileRecord();

		//TODO: fill in the details

		record.filename = part.getFilename();

		if (filedb.pol(record.filename)) {
			log.fail("file [" + record.filename + "] allready in db");
			//TODO: autorename;

			return false;
		}

		record.stored_name = genNewStoredName();
		record.size = part.bytes.length;

		record.content_type = part.getContentType();
		record.content_encoding = part.getContentEncoding();
		record.content_disposition = part.getContentDisposition();

		record.upload_user = user;
		record.upload_date = System.currentTimeMillis();

		record.download_count = 0;

		record.public_access = public_access;
		record.access_groups = (access_groups == null ? "" : access_groups);

		if (!filedb.add(record.filename, record.toArray())) {
			log.fail("could not add record to db");
			return false;
		}

		if (!FileOps.write(new File(hive_dir, record.stored_name), part.bytes,
				false)) {
			log.fail("could not write file content to disk");
			return false;
		}

		return true;
	}

	public boolean deleteFile(String filename) {
		log.info("removing file and index entry from uploads");

		if (!filedb.pol(filename)) {
			log.fail("file not found in index");
			return false;
		}

		FileRecord record = new FileRecord(filedb.get(filename));

		File target = new File(hive_dir, record.stored_name);

		if (target.exists()) {
			if (!target.delete()) {
				log.fail("could not delete target file");
				return false;
			}
		} else {
			log.info("target file does not exist. removing index + meta");
		}

		if (filedb.del(filename)) {
			log.fail("could not remove file from index");
			return false;
		}

		return true;
	}

	public String getFileResponse(String filename) {
		if (!filedb.pol(filename))
			return null;

		FileRecord record = new FileRecord(filedb.get(filename));

		String data = getFileContents(record.stored_name);
		StringBuilder sb = new StringBuilder("Content-Type: "
				+ record.content_type);
		sb.append("\nContent-Disposition: attachment; filename=\""
				+ record.filename + "\"");
		sb.append('\n');
		sb.append('\n');
		sb.append(data);
		return sb.toString();
	}

	public List<FileRecord> getFileRecords() {
		ArrayList<FileRecord> records = new ArrayList<FileRecord>();
		for(String[] raw : filedb.all()){
			records.add(new FileRecord(raw));
		}
		return records;
	}

	public boolean hasFile(String filename) {
		return filedb.pol(filename);
	}

	private String genNewStoredName() {
		HashSet<String> names = new HashSet<String>();
		for (String[] raw : filedb.all()) {
			FileRecord record = new FileRecord(raw);
			names.add(record.stored_name);
		}

		for (int postfix = 0; postfix < 999; postfix++) {
			if (!names.contains(prefix + Utils.addLeading(postfix, 4))) {
				return prefix + Utils.addLeading(postfix, 4);
			}
		}
		log.fail("could not find suitable name for storing the file");
		return null;
	}

	private String getFileContents(String file) {
		try {
			BufferedReader bin = new BufferedReader(
					new InputStreamReader(new FileInputStream(new File(
							hive_dir, file)), "ISO-8859-1"));

			StringBuilder sbuf = new StringBuilder();
			int last;

			while ((last = bin.read()) > -1) {
				sbuf.append((char) last);
			}
			bin.close();

			return sbuf.toString();
		} catch (IOException ioe) {
			log.fail("error reading data: " + ioe);
		}
		return null;
	}

	/**
	 * sendFile is used for encapsulating a file within a httprequest
	 * and used only in the --servefile operation mode of cms
	 * 
	 * @param request	existing request which will serve this file
	 * @return			true if file is loaded without exceptions, false if there is\
	 *                  no PATH_INFO or it is erroneous, or if ioexception occurs
	 */
	public boolean sendFile(HttpRequest request) {
		log.info("sendFile()...");
		String pathi;
		if ((pathi = System.getenv("PATH_INFO")) == null) {
			return false;
		}
		log.info("pathi: " + pathi);
		pathi = pathi.substring(1, pathi.length()); // -> /scan.txt
		log.info(" ->" + pathi);
		if (pathi.contains("/")) {
			return false;
		}
		String file = pathi;

		String data = getFileResponse(file);

		try {
			BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
					System.out, "ISO-8859-1"));
			bout.write(data);
			bout.close();

		} catch (IOException ioe) {
			log.fail("error while sending raw data: " + ioe);
			return false;
		}
		return true;
	}

}
