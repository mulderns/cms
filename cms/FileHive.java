package cms;

import http.FormPart;
import http.HttpRequest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import util.Csv;
import util.Logger;
import util.Utils;
import d2o.FlushingFile;

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
	private static final String linesep = System.getProperty("line.separator");
	private static final byte[] linesep_bytes = linesep.getBytes();
	private static boolean initiated = false;
	private static FileHive current;

	public static FileHive getFileHive() {
		if (initiated) {
			return current;
		} else {
			initiated = true;
			current = new FileHive(".");
			return current;
		}
	}

	public static FileHive getFileHive(File hive_dir) {
		if (initiated) {
			current.hive_dir = hive_dir;
			current.index_file = new File(hive_dir, "index");
			return current;
		} else {
			initiated = true;
			current = new FileHive(hive_dir);
			current.index_file = new File(hive_dir, "index");
			return current;
		}
	}

	private Logger log;
	private File hive_dir;
	private File index_file;

	private static final String prefix = "file";

	private FileHive(File hive_dir) {
		log = new Logger("FileHive");

		this.hive_dir = hive_dir;
		index_file = new File(this.hive_dir, "index");
		log.info("init hive[" + hive_dir.getName() + "]");
	}

	private FileHive(String hive_dir) {
		this(new File(hive_dir));
	}

	private void addToIndex(String targetfile, String realname) {
		log.info("adding to index: " + targetfile + " -> " + realname);

		try {
			if (!index_file.exists())
				index_file.createNewFile();
			BufferedOutputStream bout = new BufferedOutputStream(
					new FileOutputStream(index_file, true));
			String[] line = { targetfile, realname };
			bout.write(Csv.encode(line).getBytes());
			bout.write(linesep_bytes);
			bout.close();
			log.info(" -> success");
		} catch (IOException ioe) {
			log.fail("addToIndex failed:" + ioe);
		}
	}

	private String extractPartMeta(FormPart part) {
		StringBuilder sb = new StringBuilder();
		sb.append(part.getFilename());
		sb.append('\n');
		sb.append(part.getContentType());
		sb.append('\n');
		sb.append(part.getContentEncoding());
		sb.append('\n');
		sb.append(part.getContentDisposition());
		sb.append('\n');

		return sb.toString();
	}

	private String generateFileName() {
		log.info("generating filename");
		try {
			if (!index_file.exists()) {
				index_file.createNewFile();
			}
			BufferedReader bin = new BufferedReader(new FileReader(index_file));

			String line;
			String[] temp;
			int postfix = 0;
			String newname;
			newname = prefix + Utils.addLeading(postfix, 4);
			while ((line = bin.readLine()) != null) {
				temp = Csv.decode(line);
				if (!temp[0].equals(newname)
						&& new File(hive_dir, newname).createNewFile()) {
					log.info(" ->" + newname);
					break;
				} else {
					postfix++;
					newname = prefix + Utils.addLeading(postfix, 4);
				}
			}
			bin.close();
			return newname;
		} catch (FileNotFoundException e) {
			log.fail("getFiles failed: " + e);
		} catch (IOException ioe) {
			log.fail("getFiles failed: " + ioe);
		}
		return null;

	}

	private String genFileName() {
		return generateFileName();
	}

	public String getActionLog() {
		log.info("getting actionlog");
		try {
			BufferedReader bin = new BufferedReader(new FileReader(new File(
					Cgicms.logbooks_dir, "actionlog")));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bin.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			bin.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			log.fail("getActionLog failed: " + e);
		} catch (IOException ioe) {
			log.fail("getActionLog failed: " + ioe);
		}
		return null;
	}

	private String getData(String file) {
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

	public String getFileData(String filename) {
		String stored_name;
		if ((stored_name = getFileName(filename)) != null) {
			String[] meta = getMeta(stored_name);
			String content_type = meta[1];
			log.info("Content-Type:" + content_type);
			log.info(filename + " -> " + stored_name);
			String data = getData(stored_name);
			log.info("Content-Length: " + data.length());
			StringBuilder sb = new StringBuilder("Content-Type: " + content_type);
			sb.append("\nContent-Disposition: attachment; filename=\""
					+ filename + "\"");
			sb.append('\n');
			sb.append('\n');
			sb.append(data);
			return sb.toString();
		} else {
			return null;
		}
	}

	private String getFileName(String file) {
		ArrayList<String> files = readIndex();
		for (String line : files) {
			String[] parts = Csv.decode(line);
			if (parts[1].equals(file)) {
				return parts[0];
			}
		}
		return null;
	}

	private String[] getMeta(String file) {
		log.info("reading metadata: " + file);
		try {
			BufferedReader bin = new BufferedReader(new FileReader(new File(
					hive_dir, file + ".meta")));
			String line;
			String[] datas;
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			while ((line = bin.readLine()) != null) {
				sb.append(line);
				sb.append("\",\"");
			}
			sb.append('"');
			datas = Csv.decode(sb.toString());
			bin.close();
			log.info(" -> success");
			return datas;
		} catch (IOException ioe) {
			log.fail("getMeta failed:" + ioe);
		}
		return null;
	}

	public boolean hasFile(String filename) {
		if (getFileName(filename) != null) {
			return true;
		}
		return false;
	}

	private ArrayList<String> readIndex() {

		try {
			if (!index_file.exists()) {
				index_file.createNewFile();
			}
			log.info("reading index hf[" + hive_dir.getAbsolutePath() + "] i["
					+ index_file.getCanonicalPath() + "]");
			BufferedReader bin = new BufferedReader(new FileReader(index_file));
			String line;
			ArrayList<String> temp = new ArrayList<String>();
			while ((line = bin.readLine()) != null) {
				temp.add(line);
			}
			bin.close();
			log.info(" -> success");
			return temp;
		} catch (IOException ioe) {
			log.fail("readIndex failed:" + ioe);
		}
		return null;

	}

	public void removeFile(String realname) {
		log.info("removing file and index entry from uploads");
		String stored_name = getFileName(realname);
		File target = new File(hive_dir, stored_name);
		target.delete();
		target = new File(hive_dir, stored_name + ".meta");
		target.delete();
		removeFromIndex(realname);
	}

	private void removeFromIndex(String realname) {
		FlushingFile flush = new FlushingFile(index_file);
		ArrayList<String> buffer = new ArrayList<String>();
		for (String s : flush.loadAll()) {
			if (Csv.decode(s)[1].compareTo(realname) == 0) {
				continue;
			}
			buffer.add(s);
		}
		flush.overwrite(buffer.toArray(new String[buffer.size()]));
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
		if (file.length() > 0) {
			String storefname;
			if ((storefname = getFileName(file)) != null) {
				String[] meta = getMeta(storefname);
				String ctype = meta[1];
				log.info("Ctype:" + ctype);
				log.info(file + " -> " + storefname);
				String data = getData(storefname);
				try {
					log.info("Clen: " + data.length());
					StringBuilder sb = new StringBuilder("Content-Type: "
							+ ctype);
					sb.append('\n');
					sb.append('\n');
					BufferedWriter bout = new BufferedWriter(
							new OutputStreamWriter(System.out, "ISO-8859-1"));
					sb.append(data);
					bout.write(sb.toString());
					bout.close();
					return true;
				} catch (Exception e) {
					log.fail("exception: " + e);
				}
				/*
				Content-Type: audio/mpeg
				Content-Length: 5779683
				 */
			}
		}
		return false;
	}

	public boolean storeFile(FormPart part) {
		log.info("storing form part");
		String targetfile = genFileName();

		if (FileOps.write(new File(hive_dir, targetfile), part.bytes, false)) {
			if (FileOps.write(new File(hive_dir, targetfile + ".meta"),
					extractPartMeta(part), false)) {
				addToIndex(targetfile, part.getFilename());
				return true;
			}
		}
		return false;
	}

	public ArrayList<String> getFiles() {
		//read index
		log.info("getting filelist");
		try {
			BufferedReader bin = new BufferedReader(new FileReader(index_file));
			ArrayList<String> files = new ArrayList<String>();
			String line;
			while ((line = bin.readLine()) != null) {
				files.add(Csv.decode(line)[1]);
			}
			bin.close();
			return files;
		} catch (FileNotFoundException e) {
			log.fail("getFiles failed: " + e);
		} catch (IOException ioe) {
			log.fail("getFiles failed: " + ioe);
		}
		return new ArrayList<String>();
	}

}
