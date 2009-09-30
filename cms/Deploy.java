package cms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import d2o.FlushingFile;

import util.Csv;
import util.Hasher;
import util.Logger;
import util.ProgInfo;
import util.PropKey;

public class Deploy {
	public static final String linesep = System.getProperty("line.separator");

	private final Cgicms host;
	private final Logger log;

	public Deploy(Cgicms host){
		this.host = host;
		log = new Logger("Deploy");
	}

	void checkDirStructure(){
		log.info("checking dirs:");

		for (File dir : Cgicms.directories) {
			if(!dir.exists()){
				log.info(" "+dir.getName()+"..creating");
				dir.mkdir();	
			}else{
				log.info(" "+dir.getName()+"..exists");
			}
		}
	}

	void makeDeploy(){
		checkDirStructure();

		// default values for parameters;
		initDefaultProps();

		try {
			String apu;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(System.in));
			System.out.println("\ngenerate lauchscripts? [yes/no]");
			apu = in.readLine();
			if(apu == null)
				return;
			if(apu.equalsIgnoreCase("yes") || apu.equalsIgnoreCase("y")) {
				System.out.println("\nrunning on linux? [yes/no/maybe]");
				apu = in.readLine();
				if(apu.equalsIgnoreCase("no") || apu.equalsIgnoreCase("n")) {
					writeFile("makemy.bat", true,
							"java -jar Cgicms.jar --serve 1>output.html 2>errors.cgicms" + linesep 
					);
					writeFile("env_vars.bat", true,
							linesep +
							"set QUERY_STRING=\"\"" + linesep +
							"set SERVER_NAME=www.students.tut.fi" + linesep +
							"set SCRIPT_NAME=/cgi-bin/cgiwrap/kortesmv/makemyday.cgi" + linesep +
							"set REQUEST_URI=/cgi-bin/cgiwrap/kortesmv/makemyday.cgi" + linesep +
							"set HTTP_COOKIE=RFID=user%3DMulliSaukko%26pass%3Dsuprasalainen"+ linesep
					);
				} else {
					writeFile("makemyday.cgi", true,
							"#!/bin/sh" + linesep + linesep +
							"cd cgicms" + linesep + 
							"java -jar Cgicms.jar --serve 2> ../errors.cgicms" 
					);
					System.out.println(
							"\n\nWrote makemyday.cgi"+ linesep +
							"Move it to cgi-bin folder" +
							" and check the paths inside!" + linesep +
							"Also check that it has 755 as access modifier, cgiwrap" +
							" requires this."
					);
				}
				System.out.println( linesep +
						"it is adviced to run --conf next!"
				);


			}

			System.out.println("would you like to run --conf now? [yes/no]");
			apu = in.readLine();
			if(apu.equalsIgnoreCase("yes") || apu.equalsIgnoreCase("y")) {
				doConfig();
			}

			System.out.println("would you like to bootstap an user account? [yes/no]");
			apu = in.readLine();
			if(apu == null)
				return;
			if(apu.equalsIgnoreCase("yes") || apu.equalsIgnoreCase("y")) {
				System.out.print("Username:");
				String name = in.readLine();

				System.out.print(linesep + "Password:");
				String pass = in.readLine();

				bootStrapRootUser(name, pass);
			}

		}catch(IOException ioe){
			log.fail(ioe.toString());
		}
	}

	void upgradeUserDB(){
		System.out.println("converting database");

		File input = new File(Cgicms.database_dir, "users.cms");
		//File output = new File(Cgicms.database_dir, "output");

		FlushingFile db = new FlushingFile(input);
		System.out.println("loading file:"+input);
		String[] source_lines = db.loadAll();
		System.out.println("loaded ["+source_lines.length+"] lines");		
		if(input.renameTo(new File(Cgicms.database_dir, "users.pre"))){
			System.out.println(linesep + "renamed old file to users.pre");
		}else{
			System.out.println("could not rename old file");
		}
		File output = new File(Cgicms.database_dir, "users.cms");

		String[] result_lines = new String[source_lines.length];

		result_lines[0] = source_lines[0];

		for(int i = 1;i < source_lines.length; i++){
			String[] parts_old = Csv.decode(source_lines[i]);
			if(parts_old.length < 3){
				System.out.print("error on line ["+i+"]:" +source_lines[i]);
				continue;
			}
			String[] parts_new = new String[parts_old.length+1];
			String salt = Hasher.getSalt();

			parts_new[0] = parts_old[0];
			parts_new[1] = Hasher.legacyHash(parts_old[1], salt);
			parts_new[2] = salt;
			for(int j = 2; j < parts_old.length; j++)
				parts_new[j+1] = parts_old[j];

			result_lines[i] = Csv.encode(parts_new);
		}

		FlushingFile out = new FlushingFile(output);
		out.overwrite(result_lines);
	}

	void bootStrapRootUser(String name, String pass) {
		System.out.println("bootstrapping");
		try{
			File output = new File(Cgicms.database_dir, "users.cms");
			if(output.exists()){
				if(output.renameTo(new File(Cgicms.database_dir, "users.bak"))){
					System.out.println(linesep + "renamed old file to users.bak");
				}
				output = new File(Cgicms.database_dir, "users.cms");
			}
			FileWriter fout = new FileWriter(output);
			String salt = Hasher.getSalt();
			fout.write("\""+name+"\""+linesep);
			fout.write("\""+name+"\",\""+Hasher.hashWithSalt(pass, salt)+"\",\""+salt+"\",\"root\""+linesep);
			fout.close();
			System.out.println("wrote "+output);

			// groups
			output = new File(Cgicms.database_dir, "groups.cms");
			if(output.exists()){
				if(output.renameTo(new File(Cgicms.database_dir, "groups.bak"))){
					System.out.println(linesep + "renamed old file to groups.bak");
				}
				output = new File(Cgicms.database_dir, "groups.cms");
			}
			fout = new FileWriter(output);
			fout.write("root,all"+linesep);
			fout.close();
			System.out.println("wrote "+output);

			System.out.println("Bootstrap successfull!");
		}catch (IOException ioe){
			log.fail(ioe.toString());
		}
	}

	void doConfig(){
		// prints program infos and does premilinary checking
		System.out.println(ProgInfo.prg_info + linesep+"version " + ProgInfo.prg_ver);
		System.out.println("checking directory struckture...");
		checkDirStructure();
		initDefaultProps();


		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(System.in));
			String apu;

			System.out.print(
					linesep+
					linesep+
					"########################################################"+linesep+
					"# Configuring CGICMS                                   #"+linesep+
					"########################################################"+linesep
			);

			for (int i = 0; i < Cgicms.property_keys.length; i++) {
				System.out.println(linesep);
				System.out.println("PROPERTY: "+Cgicms.property_keys[i].identifier+linesep);
				System.out.println("DESCRIPTION: "+Cgicms.property_keys[i].description+linesep);
				System.out.println("DEFAULT: "+Cgicms.property_keys[i].default_value+linesep);
				System.out.print(">");
				apu = in.readLine();
				if(apu == null)
					return;
				if(apu.length() < 1){
					System.out.println(linesep+"no input -> using default");
					host.main_props.put(Cgicms.property_keys[i].identifier, Cgicms.property_keys[i].default_value);
				}else{
					System.out.println(linesep+"storing '"+apu+"'");
					host.main_props.put(Cgicms.property_keys[i].identifier, apu);
				}
			}

			//Save collected properties to properties_file
			try {
				BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(Cgicms.main_props_file, false), "ISO-8859-1"));
				String[] temp = new String[2];

				for(Map.Entry<String, String> entry : host.main_props.entrySet()) {
					temp[0] = entry.getKey();
					temp[1] = entry.getValue();

					bout.write(Csv.encode(temp));
					bout.write(linesep);
				}
				bout.close();
			} catch (IOException ioe) {
				log.fail("ioexception writing :" + ioe);
			}

		}catch (IOException ioe){
			log.fail("Exception while doing config: "+ioe);
		}
	}

	void initDefaultProps(){
		// initialize propertykeys with default values
		//Properties temp = new Properties();
		HashMap<String, String> temp = new HashMap<String, String>();
		for (PropKey key : Cgicms.property_keys) {
			temp.put(key.identifier, key.default_value);			
		}
		host.main_props = temp;
	}

	/*
	void generateHash(String string) {
		System.out.println("");
		System.out.println("Hashing: ["+string+"]");
		if((string = Hasher.hash(string)) != null){
			System.out.println(" -> ["+string+"]");
		}else{
			System.out.println("ERROR: Hashing failed");
		}
	}*/

	public boolean writeFile(String fileName, boolean overwrite, String content ){
		log.info("writing file:"+fileName);

		try{
			File output = new File(Cgicms.settings_dir.getParentFile(), fileName);
			if(output.exists() && !overwrite){
				log.info(" not overwriting");
				return false;
			}
			FileWriter fout = new FileWriter(output);
			fout.write(content);
			fout.close();

		}catch (IOException ioe){
			log.fail(ioe.toString());
			return false;
		}

		return true;
	}

	void dumpEnv(String fname) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry :  System.getenv().entrySet()) {
			sb.append(entry.getKey() +" -> "+ entry.getValue()+"\n");
		}
		writeFile(fname, true, sb.toString());
	}
}
