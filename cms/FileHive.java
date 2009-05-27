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
	private static FileHive spot;

	public static FileHive getFileHive(){
		if(initiated){
			return spot;
		}else{
			initiated = true;
			spot = new FileHive(".");
			return spot;
		}
	}
	public static FileHive getFileHive(File hive_dir){
		if(initiated){
			spot.hive_dir = hive_dir;
			spot.index_file = new File(hive_dir, "index");
			return spot;
		}else{
			initiated = true;
			spot = new FileHive(hive_dir);
			spot.index_file = new File(hive_dir, "index");
			return spot;
		}
	}
	private Logger log;
	private File hive_dir;
	private File index_file;

	private static final String prefix = "file";

	private FileHive(File hive_dir){
		log = new Logger("FileHive");

		this.hive_dir = hive_dir;
		index_file = new File(this.hive_dir, "index");
		log.info("init hive["+hive_dir.getName()+"]");
	}

	private FileHive(String hive_dir){
		this(new File(hive_dir));
	}

	private void addToIndex(String targetfile, String realname) {
		log.info("adding to index: "+targetfile+" -> "+realname);

		try{
			if(!index_file.exists())
				index_file.createNewFile();
			BufferedOutputStream bout =
				new BufferedOutputStream(
						new FileOutputStream(
								index_file, true
						)
				);
			String[] line = {targetfile, realname};			
			bout.write(Csv.encode(line).getBytes());
			bout.write(linesep_bytes);
			bout.close();
			log.info(" -> success");
			//return true;
		}catch(IOException ioe){
			log.fail("addToIndex failed:"+ioe);
		}
		//return false;
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
			if(!index_file.exists()){
				index_file.createNewFile();
			}
			BufferedReader bin = new BufferedReader(new FileReader(index_file));

			String line;
			String[] temp;
			int postfix = 0;
			String newname;
			newname = prefix + Utils.addLeading(postfix, 4);
			while((line = bin.readLine())!=null){
				temp = Csv.decode(line);
				if(!temp[0].equals(newname) && new File(hive_dir, newname).createNewFile()){
					log.info(" ->"+newname);
					break;
				}else{
					postfix++;
					newname = prefix + Utils.addLeading(postfix, 4);
				}
			}
			bin.close();
			return newname;
		} catch (FileNotFoundException e) {
			log.fail("getFiles failed: "+e);
		} catch (IOException ioe){
			log.fail("getFiles failed: "+ioe);
		}
		return null;

	}

	private String genFileName() {
		return generateFileName();
	}

	public String getActionLog() {
		log.info("getting actionlog");
		try {
			BufferedReader bin = new BufferedReader(new FileReader(new File(Cgicms.logbooks_dir, "actionlog")));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = bin.readLine())!=null){
				sb.append(line);
				sb.append('\n');
			}
			bin.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			log.fail("getActionLog failed: "+e);
		} catch (IOException ioe){
			log.fail("getActionLog failed: "+ioe);
		}
		return null;
	}

	private String getData(String file) {
		try{
			BufferedReader bin = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(
									new File(hive_dir, file)
							), "ISO-8859-1"
					)
			);

			StringBuilder sbuf = new StringBuilder();
			int last;

			while((last = bin.read()) > -1){
				sbuf.append((char)last);
			}
			bin.close();

			return sbuf.toString();
		}catch (IOException ioe) {
			log.fail("error reading data: "+ioe);
		}
		return null;
	}

	/*
	public String getData(File file) {

		try{
			BufferedReader bin =
				new BufferedReader(
						new InputStreamReader(
								new FileInputStream(
										file
								), "ISO-8859-1"
						)
				);
			//byte[] store = new byte[file.length()];

			StringBuilder sbuf = new StringBuilder();
			int last;

			while((last = bin.read()) > -1){
				sbuf.append((char)last);
//				if(last == 13 || last == 10){
//					return sbuf.toString();
//				}
			}
			bin.close();

			return sbuf.toString();
		}catch (IOException ioe) {
			log.fail("error reading data: "+ioe);
		}
		return null;
	}*/

	public String getFileData(String filename) {
		String storefname;
		if((storefname = getFileName(filename)) != null){
			String[] meta = getMeta(storefname);
			String ctype = meta[1];
			//String cenc = meta[2];
			log.info("Ctype:"+ctype);
			log.info(filename+" -> "+storefname);
			String data = getData(storefname);
			//try{
			/*System.out.println("Content-Length: "+data.length);*/
			log.info("Clen: "+data.length());
			StringBuilder sb = new StringBuilder("Content-Type: "+ctype);
			sb.append("\nContent-Disposition: attachment; filename=\""+filename+"\"");
			sb.append('\n');
			sb.append('\n');
			/*BufferedWriter bout = new BufferedWriter(
						new OutputStreamWriter(System.out,"ISO-8859-1"));*/
			sb.append(data);
			//bout.write(sb.toString());
			return sb.toString();
		}else{
			return null;
		}
	}

	private String getFileName(String file) {
		ArrayList<String> files = readIndex();
		for (String line : files) {
			String[] parts = Csv.decode(line); 
			if(parts[1].equals(file)){
				return parts[0];
			}
		}
		return null;
	}

	private String[] getMeta(String file) {
		log.info("reading metadata: "+file);
		try{
			BufferedReader bin =
				new BufferedReader(
						new FileReader(
								new File(hive_dir, file+".meta")
						)
				);
			String line;
			String[] datas;
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			while((line = bin.readLine())!=null){
				sb.append(line);
				sb.append("\",\"");
			}
			sb.append('"');
			datas = Csv.decode(sb.toString());
			bin.close();
			log.info(" -> success");
			return datas;
		}catch(IOException ioe){
			log.fail("getMeta failed:"+ioe);
		}
		return null;
	}

	/*
	public String readFile(String filename) {
		File source = new File(hive_dir,filename);
		if(source.canRead()){
			StringBuilder sb = new StringBuilder();
			try {
				BufferedReader bin = new BufferedReader(new FileReader(source));
				String line;
				while((line = bin.readLine())!= null){
					sb.append(line).append("\n");
				}
				bin.close();
			} catch (IOException ioe) {
				Cgicms.log.fail("exception while quick checking name: "+ioe);
				return null;
			}
			return sb.toString(); 
		} 
		Cgicms.log.fail("quick check could not read user file:"+source);
		return null;
	}*/


	/*
	public String[] readFile2Array(File filename) {
		if(filename.canRead()){
			ArrayList<String> sb = new ArrayList<String>();
			try {
				BufferedReader bin = new BufferedReader(new FileReader(filename));
				String line;
				log.info("reading file");
				while((line = bin.readLine())!= null){
					sb.add(line);
					//log.info("read["+line+"]");
				}
				log.info("closing file");
				bin.close();
			} catch (IOException ioe) {
				Cgicms.log.fail("exception while quick checking name: "+ioe);
				return null;
			}
			return sb.toArray(new String[0]); 
		} 
		Cgicms.log.fail("quick check could not read user file:"+filename.getAbsolutePath());
		return null;
	}*/

	/*
	public String[] readFile2Array(String filename) {
		File source = new File(hive_dir,filename);
		if(source.canRead()){
			ArrayList<String> sb = new ArrayList<String>();
			try {
				BufferedReader bin = new BufferedReader(new FileReader(source));
				String line;
				while((line = bin.readLine())!= null){
					sb.add(line);
				}
				bin.close();
			} catch (IOException ioe) {
				Cgicms.log.fail("exception while quick checking name: "+ioe);
				return null;
			}
			return sb.toArray(new String[0]); 
		} 
		Cgicms.log.fail("quick check could not read user file:"+source);
		return null;
	}*/

	public boolean hasFile(String filename) {
		if(getFileName(filename) != null){
			return true;
		}
		return false;
	}

	

	private ArrayList<String> readIndex(){

		try{
			if(!index_file.exists()){
				index_file.createNewFile();
			}
			log.info("reading index hf["+hive_dir.getAbsolutePath()+"] i["+index_file.getCanonicalPath()+"]");
			BufferedReader bin =
				new BufferedReader(
						new FileReader(
								index_file
						)
				);
			String line;
			ArrayList<String> temp = new ArrayList<String>();
			while((line = bin.readLine())!=null){
				temp.add(line);
			}
			bin.close();
			log.info(" -> success");
			return temp;
		}catch(IOException ioe){
			log.fail("readIndex failed:"+ioe);
		}
		return null;

	}

	public void removeFile(String realname){
		log.info("removing file and index entry from uploads");
		String storefname = getFileName(realname);
		File late = new File(hive_dir, storefname);
		late.delete();
		late = new File(hive_dir, storefname+".meta");
		late.delete();
		removeFromIndex(realname);
	}

	private void removeFromIndex(String realname){
		FlushingFile ff = new FlushingFile(index_file);
		ArrayList<String> buffer = new ArrayList<String>();
		for(String s: ff.loadAll()){
			if(Csv.decode(s)[1].compareTo(realname) == 0){
				continue;
			}
			buffer.add(s);
		}
		ff.overwrite(buffer.toArray(new String[buffer.size()]));
	}

	/*
	public boolean writeFile(FormPart part){
		log.info("storing form part");
		if(storeFile(part.getFilename(),part.bytes)){
			return true;
		}
		return false;
	}*/

	public boolean sendFile(HttpRequest request) {
		log.info("sendFile()...");
		String pathi;
		if((pathi= System.getenv("PATH_INFO"))==null){
			return false;
		}
		log.info("pathi: "+pathi);
		pathi = pathi.substring(1, pathi.length()); // -> /scan.txt
		log.info(" ->"+pathi);
		if(pathi.contains("/")){
			return false;
		}
		String file = pathi;
		if(file.length() > 0){
			String storefname;
			if((storefname = getFileName(file)) != null){
				String[] meta = getMeta(storefname);
				String ctype = meta[1];
				//String cenc = meta[2];
				log.info("Ctype:"+ctype);
				log.info(file+" -> "+storefname);
				String data = getData(storefname);
				try{
					/*System.out.println("Content-Length: "+data.length);*/
					log.info("Clen: "+data.length());
					StringBuilder sb = new StringBuilder("Content-Type: "+ctype);
					sb.append('\n');
					sb.append('\n');
					BufferedWriter bout = new BufferedWriter(
							new OutputStreamWriter(System.out,"ISO-8859-1"));
					sb.append(data);
					bout.write(sb.toString());
					bout.close();
					return true;
				}catch (Exception e) {
					log.fail("exception: "+e);
				}
				/*
				Content-Type: audio/mpeg
				Content-Length: 5779683
				 */
			}
		}
		return false;
	}

	/*public boolean storeTargetFile(String filename, String data) {
		log.info("storing file to target dir:"+filename);
		File target = new File()
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										filename, false
								), "ISO-8859-1"
						)
				);
			bout.write(data);
			log.info("write["+data+"]");
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.severe("storing upload failed:"+ioe);
		}
		return false;

	}*/

	/*
	public boolean storeFile(File file, String data) {
		log.info("storing file:"+file);
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										file, false
								), "ISO-8859-1"
						)
				);
			bout.write(data);
			//log.info("write["+data+"]");
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.fail("storeFile failed:"+ioe);
		}
		return false;
	}*/

	public boolean storeFile(FormPart part) {
		log.info("storing form part");
		String targetfile = genFileName();

		if(FileOps.write(new File(hive_dir,targetfile), part.bytes, false)){
				//storeFile(targetfile,part.bytes)){
			if(FileOps.write(new File(hive_dir,targetfile+".meta"), extractPartMeta(part), false)){
					//storeFile((targetfile+".meta"),extractPartMeta(part))){
				addToIndex(targetfile, part.getFilename());
				return true;
			}
		}
		return false;
	}

	/*
	public boolean storeFile(String filename, ArrayList<String> data) {
		log.info("storing file:"+filename);
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										new File(hive_dir, filename), false
								), "ISO-8859-1"
						)
				);
			for(String line : data){
				bout.write(line+linesep);
			}
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.fail("storeFile failed:"+ioe);
		}
		return false;

	}*/
	
	/*
	public boolean storeFile(String filename, byte[] filecontent){
		log.info("storing file:"+filename);

		try{
			BufferedOutputStream bout =	new BufferedOutputStream(
					new FileOutputStream(
							new File(hive_dir, filename), false
					)
			);
			bout.write(filecontent);
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.fail("storeFile failed:"+ioe);
		}
		return false;
	}
	*/
	
	/*
	public boolean storeFile(String filename, String data) {
		log.info("storing file:"+filename);
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										new File(hive_dir, filename), false
								), "ISO-8859-1"
						)
				);
			bout.write(data);
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.fail("storeFile failed:"+ioe);
		}
		return false;
	}*/

	/*
	public void delete(File file) {
		file.delete();		
	}*/
	public ArrayList<String> getFiles() {
		//read index
		log.info("getting filelist");
		try {
			BufferedReader bin = new BufferedReader(new FileReader(index_file));
			ArrayList<String> files = new ArrayList<String>();
			String line;
			while((line = bin.readLine())!=null){
				files.add(Csv.decode(line)[1]);
			}
			bin.close();
			return files;
		} catch (FileNotFoundException e) {
			log.fail("getFiles failed: "+e);
		} catch (IOException ioe){
			log.fail("getFiles failed: "+ioe);
		}
		return new ArrayList<String>();
	}
	
}

/**
public boolean archive(File file, boolean overwrite) {
		String filename = file.getName();
		//File archived = new File(hive_dir,filename);
		if(file.exists()){
			File dest;// = new File(Cgicms.archives_dir,file);
			if(overwrite){
				dest = new File(Cgicms.archives_dir,filename);
				if(dest.exists()){
					if(!dest.delete()){
						log.fail("could not delete destination file");
						return false;
					}
				}
				file.renameTo(dest);
				log.info("archive success["+filename+"] (overwrite)");
				return true;
			}

			for(int i = 0; i < 1000;i++){
				dest = new File(
						Cgicms.archives_dir,
						filename+"."+Utils.addLeading(i, 3)
				);

				if(!dest.exists()){
					if(file.renameTo(dest)){
						log.info("archive success["+filename+"]");
						return true;
					}else{
						log.info("archive failed["+filename+"]");
						return false;
					}
				}
			}
			log.fail("archive failed["+filename+"] -> too many files in archive");
		}else{
			log.fail("no file["+filename+"] found");
		}
		return false;
	}
	// TODO: change to non-iterative renaming. instead look up the
	// last file and so on.
	public void archive(String file, boolean overwrite) {
		File archived = new File(hive_dir,file);
		if(archived.exists()){
			File dest;// = new File(Cgicms.archives_dir,file);
			if(overwrite){
				dest = new File(Cgicms.archives_dir,file);
				if(dest.exists()){
					if(!dest.delete()){
						log.fail("could not delete destination file");
						return;
					}
				}
				archived.renameTo(dest);
				log.info("archive success["+file+"] (overwrite)");
				return;
			}

			for(int i = 0; i < 1000;i++){
				dest = new File(
						Cgicms.archives_dir,
						file+"."+Utils.addLeading(i, 3)
				);

				if(!dest.exists()){
					if(archived.renameTo(dest)){
						log.info("archive success["+file+"]");
					}else{
						log.info("archive failed["+file+"]");
					}
					return;
				}else{

				}
			}
			log.fail("archive failed["+file+"] -> too many files in archive");
		}else{
			log.fail("no file["+file+"] found");
		}
	}

	public void archiveCopy(File file, boolean overwrite) {
		if(file.exists()){
			File dest;// = new File(Cgicms.archives_dir,file);
			if(overwrite){
				dest = new File(Cgicms.archives_dir, file.getName());
				if(dest.exists()){
					if(!dest.delete()){
						log.fail("could not delete destination file");
						return;
					}
				}

				FileOps.write(dest, FileOps.readToArray(file), false);
				log.info("archive success["+file+"] (overwrite)");
				return;
			}

			for(int i = 0; i < 1000;i++){
				dest = new File(
						Cgicms.archives_dir,
						file.getName()+"."+Utils.addLeading(i, 3)
				);

				if(!dest.exists()){
					FileOps.write(dest, FileOps.readToArray(file), false);
					return;
				}
			}
			log.fail("archive failed["+file+"] -> too many files in archive");
		}else{
			log.fail("cant archive, no file["+file+"] found");
		}
	}
*/