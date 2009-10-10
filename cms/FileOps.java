package cms;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import util.Logger;
import util.Utils;

/**
append
 string
 string[]

write
 string
 string[]
 byte[]

read
 string
 string[]
 byte[]

manage
 delete
 exists
 archive
 get files
  by extension

 */

public class FileOps {
	private static final String linesep = System.getProperty("line.separator");
	//	private static final byte[] linesep_bytes = linesep.getBytes();
	private static final Logger log = new Logger("FileOps");

	public static String read(File file){
		StringBuilder lines = new StringBuilder();
		try{
			BufferedReader bin =
				new BufferedReader(
						new InputStreamReader(
								new FileInputStream(
										file
								), "ISO-8859-1"
						)
				);
			String line;
			while((line = bin.readLine())!= null){
				lines.append(line).append("\n");
			}
			bin.close();
			//log.info(" ->success");
			return lines.toString();
		}catch(IOException ioe){
			log.fail("read File failed:"+ioe);
		}
		return null;
	}

	public static String[] readToArray(File file){
		ArrayList<String> lines = new ArrayList<String>();
		try{
			BufferedReader bin =
				new BufferedReader(
						new InputStreamReader(
								new FileInputStream(
										file
								), "ISO-8859-1"
						)
				);
			String line;
			while((line = bin.readLine())!= null){
				lines.add(line);
			}
			bin.close();
			//log.info(" ->success");
			return lines.toArray(new String[0]);
		}catch(IOException ioe){
			log.fail("readToArray failed:"+ioe);
		}
		return null;
	}

	/**Warning this is unimplemented
	 * 
	 * @param file
	 * @return
	 */
	public byte[] readBytes(File file){log.fail("unimplemented");return null;}

	public static boolean write(File file, String line, boolean append){
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										file, append
								), "ISO-8859-1"
						)
				);
			bout.write(line);
			bout.write(linesep);
			bout.close();
			return true;
		}catch(IOException ioe){
			log.fail("write line failed:"+ioe);
		}
		return false;
	}
	public static boolean write(File file, String[] lines, boolean append){
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										file, append
								), "ISO-8859-1"
						)
				);
			for(String s: lines){
				bout.write(s);
				bout.newLine();				
			}
			bout.close();
			return true;
		}catch(IOException ioe){
			log.fail("write lines failed:"+ioe);
		}
		return false;


	}
	public static boolean write(File file, byte[] bytes, boolean append){
		try{
			BufferedOutputStream bout =	new BufferedOutputStream(
					new FileOutputStream(
							file, append
					)
			);
			bout.write(bytes);
			bout.close();
			return true;
		}catch(IOException ioe){
			log.fail("write bytes failed:"+ioe);
		}
		return false;
	}

	//	public boolean delete(File file){}
	//	public boolean exists(File file){}
	public static boolean archive(File file, boolean overwrite){
		if(file.exists()){
			File archive = Cgicms.archives_dir;
			final String originalname = file.getName();

			String[] files = archive.list(
					new FilenameFilter(){public boolean accept(File dir, String name) {
						//return name.startsWith(originalname);
						return name.matches(originalname.replace(".", "\\.")+"_[0-9]{3}");
					}}
			);
			log.info("archive["+file.getName()+"]");
			log.info(Arrays.toString(files));

			String finalname = originalname+"_000";

			if(files.length > 0){
				int[] suffixes = new int[files.length];
				int i = 0;
				for(String name:files){
					suffixes[i++] = Integer.parseInt(name.substring(name.lastIndexOf('_')+1));
				}

				Arrays.sort(suffixes);
				for(int j = 0; j < 999; j++){
					if(Arrays.binarySearch(suffixes, j) < 0){
						finalname = originalname+"_"+Utils.addLeading(j, 3);
						break;
					}
				}
			}
			log.info("finalname = "+finalname);

			File dest = new File(archive,finalname);

			if(!dest.exists()){
				if(file.renameTo(dest)){
					log.info("archive success["+originalname+"]");
					return true;
				}else{
					log.info("archive failed["+originalname+"]");
					return false;
				}
			}else{
				if(overwrite){
					dest.delete();
					if(file.renameTo(dest)){
						log.info("archive success["+originalname+"]");
						return true;
					}else{
						log.info("archive failed["+originalname+"]");
						return false;
					}
				}else{
					log.info("file exists");
					log.fail("archive failed["+originalname+"] -> too many files in archive");
				}
			}
		}else{
			log.fail("no file["+file.getName()+"] found");
		}
		return false;
	}

	public static File[] getFiles(File dir, final String extension) { 
		return dir.listFiles(
				new FilenameFilter(){public boolean accept(File dir, String name){
					return (name.contains("." + extension));
				}}
		);
	}

	public static boolean copy(File source, File target, boolean overwrite) {
		if(!overwrite && target.exists()){
			log.info("target exists");
			return false;
		}
		try{
			final FileInputStream fin = new FileInputStream(source);
			final FileOutputStream fout = new FileOutputStream(target);
			final byte[] buffer = new byte[2048];
			int c;

			while ((c = fin.read(buffer)) != -1){
				if(c != buffer.length){
					byte[] buffer2 = new byte[c];
					System.arraycopy(buffer, 0, buffer2, 0, c);
					fout.write(buffer2);
					break;
				}
				fout.write(buffer);
			}

			fin.close();
			fout.close();
		}catch (IOException ioe) {
			log.fail("IOException copying file:"+ioe);
			return false;
		}
		return true;
	}
}
/*
	public static void appendFile(String fileName, String line) {
		log.info("appending file["+fileName+"] hive["+hive_dir+"]");
		try {
			BufferedOutputStream bfout = new BufferedOutputStream(
					new FileOutputStream(new File(hive_dir,fileName), true));

			bfout.write(line.getBytes());
			bfout.write(linesep_bytes);

			bfout.close();
			log.info("wrote actionlog successfully");
		} catch (FileNotFoundException fnfe) {
			log.fail("could not write actionlog:"+fnfe);
		} catch (IOException ioe){
			log.fail("could not write actionlog:"+ioe);
		}
	}

	public boolean appendFileIso(String filename, String data) {
		log.info("storing file:"+filename);
		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										new File(hive_dir, filename), true
								), "ISO-8859-1"
						)
				);
			bout.write(data);
			bout.write(linesep);
			bout.close();
			log.info(" ->success");
			return true;
		}catch(IOException ioe){
			log.fail("appendFileIso failed:"+ioe);
		}
		return false;
	}


	public boolean fileExists(String filename) {
		File supposed = new File(hive_dir,filename);
		return supposed.exists();
	}

	public ArrayList<String> getFiles() {
		//read index
		log.info("getting filelist");
		try {
			BufferedReader bin = new BufferedReader(new FileReader(index));
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

	public String[] getFiles(final String extension) {
		String[] files = hive_dir.list(
				new FilenameFilter(){
					public boolean accept(File dir, String name){
						if(name.contains("."+extension))
							return true;
						return false;
					}
				}
		);
		return files;
	}
}*/
