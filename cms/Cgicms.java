package cms;

import html.CmsPage2;
import http.HttpRequest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import util.ActionLog;
import util.Csv;
import util.Logger;
import util.ProgInfo;
import util.PropKey;
import util.Utils;
import cms.access.Session;
import cms.ext.FeedBacker;
import cms.ext.Help;
import cms.mods.ModUpload;
import cms.mods.ModViikko;

/**
 *  t‰‰ on nyt se ite p‰‰ juttu jossa on mahtavia
 *  algoritmeja ja funktioita.
 */

public class Cgicms {

	public static final File settings_dir = new File("..", "settings");
	public static final File sessions_dir = new File("..", "sessions");
	public static final File database_dir = new File("..", "database");
	public static final File logbooks_dir = new File("..", "logbooks");
	public static final File uploaded_dir = new File("..", "uploaded");
	public static final File products_dir = new File("..", "products");
	public static final File archives_dir = new File("..", "archives");
	//material, elements, frontend

	static final File[] directories = {
		settings_dir, // settings for cgicms and modules
		sessions_dir, // stored (user)sessions
		database_dir, // users, groups, etc.
		logbooks_dir, // logged actions
		uploaded_dir, // uploaded files
		products_dir, // module spinoffs
		archives_dir  // backupping
	};

	static final File main_props_file = new File(settings_dir, "properties");

	final static PropKey[] property_keys = {
			new PropKey(
					"res_root",
					"http://www.students.tut.fi/~kortesmv/cms/",
					"url to external resources such as images and stylesheets"
			),
			new PropKey(
					"script_file",
					"http://www.students.tut.fi/cgi-bin/cgiwrap/kortesmv/makemyday.cgi",
					"url of the script file, which launces cms."
			),
			new PropKey(
					"relative_target_path",
					"..\\..\\",
					"(relative)path from cms to where you would like final pages to be placed(public_html?)"
			)
	};

	public static final Logger log = new Logger("___");

	public static DataRelay datarelay;
	public HttpRequest request;
	public PageBuilder pagebuilder;
	public Session session;
	public FileHive filehive;
	public ModuleLoader loader;

	public static String group_hook;
	private long t_begin, t_init, t_request, t_restore, t_execute;
	private long t_begin_nano, t_init_nano, t_request_nano, t_restore_nano, t_execute_nano;

	HashMap<String,String> main_props;

	Cgicms(){
		log.meta("CGICMS");
		t_begin = System.currentTimeMillis();
		t_begin_nano = System.nanoTime();
		ActionLog.setLogFile(new File(logbooks_dir, "actionlog"));
	}

	public static void main(String[] args){
		try{
			System.setErr(
					new PrintStream(
							new BufferedOutputStream(
									new FileOutputStream(new File("..","err"),true)
							)
							, true 
							,"ISO-8859-1" 
					)
			);
			new Cgicms().doCommand(args);
		}catch(Exception e){
			e.printStackTrace();
			StringBuilder sb = new StringBuilder();
			sb.append("Exception ["+e.getCause()+"] ["+e.toString()+"]");
			sb.append("\n");
			for(StackTraceElement ste: e.getStackTrace()){
				sb.append(ste.toString());
				sb.append("\n");
			}
			ActionLog.exception(sb.toString());
			ActionLog.write();
			Mailer.sendMail("cms", "valtteri.kortesmaa@tut.fi", "[Cms] exception", 
					sb.toString()	
			);
		}
	}


	/** Does all the real work(or commanding it)
	 * @param args
	 */
	private void doCommand(String[] args){
		List<String> arguments = Arrays.asList(args);
		Collections.sort(arguments);

		// we determine possible required actions from 'args'

		if(Collections.binarySearch(arguments, "--serve") >= 0){
			// --serve is called to proceed with cgi-processing (usually results in
			//         doing something(storing, login, editing) and pringint a web page

			// checking properties
			long t_temp = System.currentTimeMillis();

			if((main_props = loadProperties(main_props_file)) == null){
				log.fail("loading properties failed");
				return;
			}
			if(!checkProperties(main_props)){
				log.fail("not all properties set in properties file");
				return;
			}
			if(main_props.get("script_file").contains("localhost")){
				Logger.setEnableSystemOut(true);
			}

			t_init = System.currentTimeMillis();
			t_init_nano = System.nanoTime();
			log.info("not-properties load+check took: "+(t_init - t_temp)+" ms");

			datarelay = new DataRelay();
			datarelay.script = getScriptFile();
			datarelay.target = getRelativePath();
			datarelay.res = getResRoot();
			datarelay.env = new HashMap<String,String>(System.getenv());
			group_hook = "cms";
			
			// read input from cgi-environment
			request = new HttpRequest(datarelay);//initHttpRequest();
			t_request = System.currentTimeMillis();
			t_request_nano = System.nanoTime();

			//Deploy deploy = new Deploy(this);
			//deploy.dumpEnv("cms.env.dump");

			//authentication & sessions
			log.info("proceeding to sessions");
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;

			pagebuilder.addHeadTag(	
					"<link rel=\"shortcut icon\" href=\""+
					main_props.get("res_root")
					+"./res/favicon.png\"/>"
			);


			// >>> watch out! bumpy road ahead <<<

			/** first check if requester has a valid session open */
			SessionerCms sessioner = new SessionerCms(datarelay);
			if(
					!sessioner.restoreSession() && //restore from cookie
					!sessioner.createSession() //create new from received login
			){
				sessioner.doLogin(); // send login page
				t_restore = System.currentTimeMillis();
				t_restore_nano = System.nanoTime();

			}else{
				log.info("has session");
				t_restore = System.currentTimeMillis();
				t_restore_nano = System.nanoTime();
				session = datarelay.session;

				if(datarelay.multipart)
					request.processMultiPart();

				datarelay.page = new CmsPage2(null);
				datarelay.page.addHead("<link rel=\"stylesheet\" href=\"" +
						main_props.get("res_root") +"style2.css\" type=\"text/css\" />");

				loader = new ModuleLoader(datarelay);
				datarelay.loader = loader;
				t_temp = System.nanoTime();
				loader.load_modules();
				pagebuilder.addHidden(" load modules = "+((System.nanoTime()-t_temp)/1000000)+" ms");
				
				log.info("user-"+session.getUser().toString());
				
				long apu = System.nanoTime();
				loader.execute(); // get the inputs, do the outputs
				pagebuilder.addHidden(" execute module = "+((System.nanoTime()-apu)/1000000)+" ms");
				
				pagebuilder.bake();

				if(session.delete){
					log.info("delete session");
					session.remove();
				}else{
					log.info("store session");
					session.store();
				}
			}

			t_execute = System.currentTimeMillis();
			t_execute_nano = System.nanoTime();
			log.info("run took: "+(System.currentTimeMillis()-t_begin)+" ms");
			ActionLog.time(
					"i "+Utils.addLeading((int)(t_init - t_begin), 4) +" "+
					"r "+Utils.addLeading((int)(t_request - t_init), 4) +" "+
					"l "+Utils.addLeading((int)(t_restore - t_request), 4) +" "+
					"e "+Utils.addLeading((int)(t_execute - t_restore), 4) +" "+
					"t "+Utils.addLeading((int)(System.currentTimeMillis() - t_begin), 4));
		
			ActionLog.time(
					"i "+Utils.addLeading((int)(t_init_nano - t_begin_nano), 10).substring(0, 4) +" "+
					"r "+Utils.addLeading((int)(t_request_nano - t_init_nano), 10).substring(0, 4) +" "+
					"l "+Utils.addLeading((int)(t_restore_nano - t_request_nano), 10).substring(0, 4) +" "+
					"e "+Utils.addLeading((int)(t_execute_nano - t_restore_nano), 10).substring(0, 4) +" "+
					"t "+Utils.addLeading((int)(System.nanoTime() - t_begin_nano), 10).substring(0, 4));
			ActionLog.write();
			log.info("____________________________________________");

		}else if(Collections.binarySearch(arguments, "--publicfile") >= 0){
			datarelay = new DataRelay();
			datarelay.env = new HashMap<String,String>(System.getenv());
			request = new HttpRequest(datarelay);//initHttpRequest();
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;
			ModUpload upload = new ModUpload(datarelay);
			upload.servePublic();
			
		}else if(Collections.binarySearch(arguments, "--servefile") >= 0){
			datarelay = new DataRelay();
			datarelay.env = new HashMap<String,String>(System.getenv());
			request = new HttpRequest(datarelay);
			filehive = FileHive.getFileHive();

			//dumpEnv("env.file.dump");

			//TODO: --servefile
			SessionerCms sessioner = new SessionerCms(datarelay);
			session = sessioner.getSession();

			if(session == null){
				//session = new Session(request.getIp(),new User("guest","guest"));
			}
			if(!filehive.sendFile(request)){
				pagebuilder = new PageBuilder(this);
				loadProperties(main_props_file);
				pagebuilder.addHeadTag(
						"<link rel=\"stylesheet\" href=\"" +
						main_props.get("res_root") +
						"style.css\" type=\"text/css\" />"
				);
				pagebuilder.addMessage("download failed");
				pagebuilder.bake();
			}

		}else if(Collections.binarySearch(arguments, "--feedb") >= 0){

			// read input from cgi-environment
			datarelay = new DataRelay();
			datarelay.env = new HashMap<String,String>(System.getenv());
			request = new HttpRequest(datarelay);//initHttpRequest();

			//dumpEnv("env.feedb.dump");

			//authentication & sessions
			log.info("proceeding to feedback");
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;
			new FeedBacker(datarelay);
			ActionLog.write();

		}else if(Collections.binarySearch(arguments, "--update") >= 0){
			
			/** § */
			if((main_props = loadProperties(main_props_file)) == null){
				log.fail("loading properties failed");
				return;
			}
			if(!checkProperties(main_props)){
				log.fail("not all properties set in properties file");
				return;
			}
			datarelay = new DataRelay();
			datarelay.script = getScriptFile();
			datarelay.target = getRelativePath();
			datarelay.res = getResRoot();
			datarelay.env = new HashMap<String,String>(System.getenv());
			group_hook = "cms";
			
			// read input from cgi-environment
			request = new HttpRequest(datarelay);//initHttpRequest();
			
			/** § */
			
			// read input from cgi-environment


			//authentication & sessions
			log.info("proceeding to update week");
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;
			
			if(!new ModViikko(datarelay).blind_update()){
				ActionLog.error("update failed");
			}else{
				ActionLog.log("kicker updated week successfully");
			}
			
			ActionLog.write();

		}else if(Collections.binarySearch(arguments, "--reset") >= 0){
			if((main_props = loadProperties(main_props_file)) == null){
				log.fail("loading properties failed");
				return;
			}
			if(!checkProperties(main_props)){
				log.fail("not all properties set in properties file");
				return;
			}
			if(main_props.get("script_file").contains("localhost")){
				Logger.setEnableSystemOut(true);
			}

			datarelay = new DataRelay();
			datarelay.script = getScriptFile();
			//datarelay.target = getRelativePath();
			datarelay.res = getResRoot();
			datarelay.env = new HashMap<String,String>(System.getenv());
			group_hook = "cms";
			request = new HttpRequest(datarelay);//initHttpRequest();
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;

			pagebuilder.addHeadTag(	
					"<link rel=\"shortcut icon\" href=\""+
					main_props.get("res_root")
					+"./res/favicon.png\"/>"
			);

			log.info("proceeding to sessions");
		
			//authentication & sessions
			log.info("validating key");

			KeyManager keymanager = new KeyManager(datarelay);
			keymanager.doStuff();
			
			ActionLog.write();
			//Deploy deploy = new Deploy(this);
			//deploy.dumpEnv("env.dump");
			log.info("____________________________________________");
			
		}else if(Collections.binarySearch(arguments, "--apua") >= 0){

			// read input from cgi-environment
			datarelay = new DataRelay();
			datarelay.env = new HashMap<String,String>(System.getenv());
			request = new HttpRequest(datarelay);//initHttpRequest();

			//dumpEnv("env.feedb.dump");

			//authentication & sessions
			log.info("proceeding to help");
			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;
			new Help(datarelay);
			ActionLog.write();

		/*}else if(Collections.binarySearch(arguments, "--poll") >= 0){
			//dumpEnv("env.poll.dump");

			// read input from cgi-environment
			datarelay = new DataRelay();
			datarelay.env = new HashMap<String,String>(System.getenv());
			request = new HttpRequest(datarelay);//initHttpRequest();

			pagebuilder = new PageBuilder(this);
			datarelay.pagebuilder = pagebuilder;			

			//authentication & sessions
			log.info("proceeding to poll");
			//Sessioner sessioner =
			//new SessionerPoll(datarelay);

			new Poller(datarelay);

			ActionLog.write();*/

		}else if((Collections.binarySearch(arguments, "--conf") >= 0)){
			// --conf runs configuration wizard to set parameters/locations
			new Deploy(this).doConfig();
			return;

		}else if(Collections.binarySearch(arguments, "--check") >= 0){
			// --check might confirm the correct dir/file structure and/or other stuff
			Deploy deploy = new Deploy(this);
			deploy.checkDirStructure();
			//checkModules();
			deploy.initDefaultProps();
			checkProperties(main_props);
			return;

		}else if(Collections.binarySearch(arguments, "--deploy") >= 0){
			// --deploy helps test the system
			new Deploy(this).makeDeploy();
			return;

		/*}else if(Collections.binarySearch(arguments, "--hash") >= 0){
			// --hash prints out the hash of args[1]
			new Deploy(this).generateHash(args[1]);
			return;
*/
		}else if(Collections.binarySearch(arguments, "--bootstrap") >= 0){
			// --hash prints out the hash of args[1]
			new Deploy(this).bootStrapRootUser("mullis", "mallis");
			//bootSmack("")
			return;
			
		}else if(Collections.binarySearch(arguments, "--convert") >= 0){
			// --hash prints out the hash of args[1]
			new Deploy(this).upgradeUserDB();
			return;

		}else if(
				(Collections.binarySearch(arguments, "--help") >= 0) ||
				(Collections.binarySearch(arguments, "-h") >= 0) 
		){
			// --help and -h print out the instructions on command line syntax and options
			System.out.println(ProgInfo.prg_info + ". Version "+ProgInfo.prg_ver+", "+ProgInfo.prg_date );
			System.out.println(ProgInfo.prg_help);
		}else{
			System.out.println("no parameter found -> doing nothing");
		}
	}


	/** Loads Properties from 'source'
	 * @param source
	 */

	private HashMap<String, String> loadProperties(File source){
		log.info("loading properties from:"+source);

		if(!source.canRead()){
			log.fail("could not read properties from: "+source.getPath());
			return null;
		}

		final HashMap<String,String> temp = new  HashMap<String,String>();

		try {

			BufferedReader bin = new BufferedReader(new InputStreamReader(
					new FileInputStream(source), "ISO-8859-1"));
			String line;
			String[] things;
			while ((line = bin.readLine()) != null) {
				things = Csv.decode(line);
				if(things.length == 2){
					temp.put(things[0], things[1]);	
				}else if(things.length == 1){
					temp.put(things[0], "");
				}
			}
			bin.close();
			return temp;

		} catch (IOException ioe) {
			log.fail("ioexception appending :" + ioe);
		}

		return null;

	}

	/** Checks to see if loaded properties 'joo' contains all
	 *  needed properties.
	 *  
	 * @param properties
	 * properties
	 */
	private boolean checkProperties(HashMap<String,String> properties){
		//check to see if all keys are defined in properties;
		log.info("checking property keys");
		for (PropKey key : property_keys) {
			if(!properties.containsKey(key.identifier)){
				return false;
			}
		}
		return true;
	}

	final public String getScriptFile() {
		if(main_props != null)
			return main_props.get("script_file");
		return "";
	}

	final public String getRelativePath() {
		if(main_props != null)
			return main_props.get("relative_target_path");
		return "";
	}

	final public String getResRoot() {
		if(main_props != null)
			return main_props.get("res_root");
		return "";
	}

}


