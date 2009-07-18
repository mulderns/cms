package cms.mods;

import html.CmsElement;
import html.FileField;
import html.SubmitField;
import html.TextAreaField;
import http.FormPart;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import util.ActionLog;
import util.Csv;
import util.Utils;
import cms.Cgicms;
import cms.DataRelay;
import cms.FileHive;
import cms.FileOps;
import d2o.FlushingFile;
import d2o.UserDb;


public class ModMaintenance extends Module {

	public ModMaintenance(DataRelay datarelay) {
		super(datarelay);
		hook = "yllapito";
		menu_label = "Ylläpito";
	}

	public void activate(){
		super.activate();
		actions.add(new Action(null, ""){public void execute(){
			page.setTitle("Ylläpito");
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("Puhdista vanhat istunnot", "cleanup"){public void execute(){

			log.info("doing cleanup");

			CmsElement result = new CmsElement();
			result.createBox("Cleanup","medium3");
			result.addTag("pre",clearSessions());

			page.setTitle("Maintenance");
			page.addCenter(result);
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("MotD", "motd"){public void execute(){

			FlushingFile motd = new FlushingFile(new File(Cgicms.products_dir,"misc.motd"));
			String[] data;
			if(checkField("motd")){
				data = datarelay.post.get("motd").split("\n");
				motd.overwrite(data);

			}else{
				data = motd.loadAll();
			}

			StringBuilder sb = new StringBuilder();
			for(String s : data){
				sb.append(s).append("\n");
			}

			CmsElement box = new CmsElement();
			box.createBox("Message of the day","medium3");
			box.addFormTop(script + "/" + hook + "/" + action_hook);
			box.addField("motd", sb.toString(), true, new TextAreaField(50,10));
			box.addContent("<br/>");
			box.addField(null,"submit", false, new SubmitField(true));

			page.setTitle("Maintenance - Message of the day");
			page.addCenter(box);
			page.addLeft(getActionLinks());

		}});

		actions.add(null);

		actions.add(new Action("näytä logi", "viewlog"){public void execute(){
			log.info("viewing log");

			CmsElement result = new CmsElement();
			result.createBox("Logi");

			result.addTag("pre style=\"font-size:12.5px\"",getActionLog());
			//result.addLink("lataa logi", script + "/" + hook + "/downlog");
			//result.addLink("arkistoi logi", script + "/" + hook + "/archlog");
			//result.addLink("muut toiminnot", script + "/" + hook );

			page.setTitle("Maintenance - Logi");
			page.addCenter(result);
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("lataa logi", "downlog"){public void execute(){
			log.info("downloading log");

			StringBuilder sb = new StringBuilder("Content-Type: text/plain");
			sb.append("\nContent-Disposition: attachment; filename=\"ActionLog.txt\"");
			sb.append('\n');
			sb.append('\n');
			sb.append(getActionLog());
			pagebuilder.rawSend(sb.toString());
		}});

		actions.add(new Action("arkistoi logi", "archlog"){public void execute(){
			log.info("archiving log");
			FileOps.archive(new File(Cgicms.logbooks_dir,"actionlog"));

			CmsElement result = new CmsElement();
			result.createBox("Logi", "medium3");
			result.addTag("p","joo");
			result.addLink("ok", script + "/" + hook );

			page.setTitle("Maintenance - Logi");
			page.addCenter(result);
		}});

		actions.add(null);

		actions.add(new Action("näytä kävijät", "viewaccess"){public void execute(){
			log.info("viewing log");



			FlushingFile ff = new FlushingFile(new File(Cgicms.products_dir,"misc.botlist"));

			if(ext.contentEquals("bots")){
				if(checkField("botlist")){
					String data = datarelay.post.get("botlist");
					ff.overwrite(data);
				}
			}


			String[] botlist = ff.loadAll();
			if(botlist == null){
				botlist = new String[0];
			}

			CmsElement prototable = new CmsElement();
			prototable.addLayer("table style=\"font-size:8.5px\"","table5");
			prototable.addSingle("colgroup width=\"20\"");
			prototable.addSingle("colgroup width=\"50\"");
			prototable.addSingle("colgroup width=\"200\"");
			prototable.addSingle("colgroup width=\"120\"");
			prototable.addSingle("colgroup");

			CmsElement result = new CmsElement();
			result.createBox("Kävijälogi");
			result.addElementOpen(new CmsElement(prototable));

			boolean parillinen = true;
			int week = 0;
			int kavija = 0;

			HashSet<String> day_bots = new HashSet<String>();
			HashSet<String> day_visitors = new HashSet<String>();

			ArrayList<Paiva> days = new ArrayList<Paiva>();

			Calendar cal = Calendar.getInstance();
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			page.clear();
			try{
				for(String line : FileOps.readToArray(new File("..","ticker.info.dat"))){
					String[] fields = Csv.decode(line);

					cal.setTimeInMillis(Long.parseLong(fields[0]));
					int old_week = week;
					if((week = cal.get(Calendar.DAY_OF_YEAR))!= old_week){

						days.add(new Paiva(old_week,day_visitors.size(),day_bots.size()));

						if(day_bots.size() > 0){
							result.addContent("<tr style=\"background-color:#eee\"><td colspan=\"6\">bots["+day_bots.size()+"]: ");
							for(String s : day_bots){
								result.addContent(s+" , ");
							}
							day_bots.clear();
							result.addContent(
									"</td></tr>"
							);
						}
						result.up(3);
						result.addContent("<h4>"+
								Utils.addLeading(cal.get(Calendar.DATE),2) + "." +
								Utils.addLeading((cal.get(Calendar.MONTH)+1),2) + "."		
								+"</h4>"
						);
						result.addLayer("div","ingroup filled");
						result.addElementOpen(new CmsElement(prototable));
						kavija = 1;
						day_visitors.clear();
					}

					boolean skip = false;
					for(String s : botlist){
						if(s.length()>0){
							if(fields[3].endsWith(s)){
								skip = true;
								day_bots.add(s);
								break;
							}
						}
					}
					if(skip)
						continue;

					if(day_visitors.contains(fields[2]))
						continue;

					day_visitors.add(fields[2]);

					parillinen = !parillinen;
					result.addContent(
							"<tr"+ (parillinen ? " style=\"background-color:lightYellow\"":"")+"><td>"+(kavija++)+"</td><td>"+
							Utils.addLeading(cal.get(Calendar.HOUR_OF_DAY),2) + ":" +
							Utils.addLeading(cal.get(Calendar.MINUTE),2)+
							"</td><td>"+fields[1]+"</td>"
							/*<td>"+fields[2]+"</td>"*/+
							"<td>"+truncate(fields[3]) +
							//"<div style=\"height:10px\">"+truncate(fields[3])+"</div>"+
							"</td><td>"+
							"<div style=\"overflow:hidden;height:12px\">"+fields[4]+"</div>"+
							"</td></tr>\n"
					);

				}
			}catch (Exception e) {
				page.addCenter("<pre>");
				page.addCenter("exception occurred: "+e+"\n");
				for(StackTraceElement trc: e.getStackTrace()){
					page.addCenter(" "+trc.toString()+"\n");
				}
				//				result.addLayer("pre");
				//				result.addContent("exception occurred: "+e+"\n");
				//				for(StackTraceElement trc: e.getStackTrace()){
				//					result.addContent(" "+trc.toString()+"\n");
				//				}
			}


			page.addTop(result);
			page.addTop("<br/>");

			if(days.size() > 0){
				days.remove(0);
			}

			CmsElement graphs = new CmsElement();
			graphs.createBox("Graphs");
			graphs.addLayer("table style=\"font-size:8.5px\"","table5");
			graphs.addLayer("tr");
			for(Paiva p : days){
				graphs.addTag("td style=\"vertical-align:bottom;\"",
						"<div style=\"" +
						"background-color:#458372;width:5px;float:right;" +
						"height:"+p.visitors +"px;"+
						"\"></div>"
				);
				graphs.addTag("td style=\"vertical-align:bottom;\"",
						"<div style=\"" +
						"background-color:#d79742;width:5px;" +
						"height:"+p.bots +"px;" +
						"font-size:0px;"+
						"\"></div>"
				);
			}
			graphs.up();

			graphs.addLayer("tr");
			for(Paiva p : days){
				graphs.addTag("td colspan=\"2\" style=\"background-color:#fafafa;text-align:center\"",
						Integer.toString(p.visitors)
				);

			}

			page.addTop(graphs);

			CmsElement stuff = new CmsElement();
			stuff.createBox("Botlist","medium3");

			stuff.addFormTop(script+"/"+hook+"/"+action_hook+"/bots");
			StringBuilder bots = new StringBuilder();
			for(String s : botlist)
				bots.append(s).append("\n");
			stuff.addField("botlist", bots.toString(), true, new TextAreaField(200));
			stuff.addField("submit","submit",false,new SubmitField(true));

			page.addCenter(stuff);

			page.setTitle("Maintenance - Logi");

		}

		private String truncate(String source) {
			return source.substring(source.lastIndexOf('.', source.lastIndexOf('.')-1)+1);
		}});

		actions.add(null);

		actions.add(new Action("puhdista käyttäjäkanta", "cleanuserdb"){public void execute(){
			CmsElement result = new CmsElement();
			result.createBox("puhdistus","medium3");
			UserDb udb = UserDb.getDb();
			result.addTag("pre",(udb.cleanDb()?"cleaned some":"allready clean"));
			//result.addLink("muut toiminnot", script + "/" + hook );

			page.setTitle("Maintenance - UserDb");
			page.addCenter(result);
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("Target Practice", "target"){public void execute(){

			CmsElement box = new CmsElement();
			box.createBox("Target Practice","medium4");

			File target = new File(datarelay.target,"target.practice");
			box.addTag("p","Creating file["+datarelay.target+"] - ["+target.getAbsolutePath()+"]");
			String result = "";
			try{
				if(target.createNewFile()){
					result = "created ok";
				}else{
					if(target.exists()){
						result = "target exists";
					}else{
						result = "could not create";
					}
				}
			}catch (IOException ioe) {
				result = "error: "+ioe;
			}

			box.addTag("p",result);

			page.setTitle("Maintenance - Target Practice");
			page.addCenter(box);
			page.addLeft(getActionLinks());

		}});

		actions.add(new Action("Varmuuskopioi asetukset", "backupsettings"){public void execute(){
			log.info("doing backup");

			CmsElement result = new CmsElement();
			result.createBox("Backup", "medium3");

			backupSettings();
			result.addTag("pre","backupSettings()");
			//result.addLink("muut toiminnot", script + "/" + hook );

			page.setTitle("Maintenance");
			page.addCenter(result);
			page.addLeft(getActionLinks());
		}});

		actions.add(null);

		actions.add(new Action("päivitä", "update"){public void execute(){
			log.info("updating svn repository");

			CmsElement result = new CmsElement();
			result.createBox("Päivitys","medium4");

			String r = updateRepository();
			Utils.sleep(2000);
			result.addTag("pre",r);
			//result.addLink("muut toiminnot", script + "/" + hook );

			page.setTitle("Maintenance - Logi");
			page.addCenter(result);
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("Build info", "build"){public void execute(){
			ClassLoader cl = this.getClass().getClassLoader();
			java.io.InputStream in = cl.getResourceAsStream("extra/cgicms.build.number");
			StringBuilder sb = new StringBuilder();
			try{
				int i = 0;
				while((i = in.read())!= -1){
					sb.append((char)i);
				}
				in.close();
			}catch (IOException e) {
				sb.append(" - IOException ["+e+"] -");
			}

			page.addCenter("<pre>"+sb.toString()+"</pre>");
			page.addLeft(getActionLinks());
		}});

		actions.add(new Action("Upload update", "uploadate"){public void execute(){
			if(datarelay.multipart){


				if(datarelay.files.length==0){
					page.addCenter("error:no file");
					page.addLeft(getActionLinks());
				}

				if(datarelay.files.length==1){
					FormPart p = datarelay.files[0];
					if(!p.getFilename().equals("Cgicms.jar")){
						page.addCenter("error:name");
						page.addLeft(getActionLinks());
						return;
					}
					log.info("writing jar");
					if(FileOps.write(new File("Cgicms.jar"), p.bytes, false)){
						log.info("wrote jar");
						page.addCenter("wrote jar");
						page.addLeft(getActionLinks());
					}else{
						page.addCenter("error:could not write jar");
						log.fail("jar could not be written");
						page.addLeft(getActionLinks());
					}
				}
			}else{
				CmsElement box = new CmsElement();
				box.addLayer("form method=\"post\" action=\"" +
						script + "/" + hook +"/"+action_hook+
				"\" enctype=\"multipart/form-data\"");
				box.addLayer("div", "boxi2 medium3");
				box.addTag("h4", "Tiedoston lähetys");
				box.addLayer("div", "ingroup filled");
				box.addLayer("table", "table5");
				box.addSingle("colgroup");
				box.addSingle("colgroup width=\"70\"");

				box.addLayer("tr");
				box.addTag("td","Tiedosto:");
				box.addLayer("td");
				box.addField("tiedosto", null, true, new FileField());
				box.up(2);
				box.addLayer("tr");
				box.addTag("td colspan=\"2\"", "<input class=\"list\" style=\"width:100%;cursor:pointer;\" type=\"submit\" value=\"lähetä\">");
				//CmsBoxi uploadBox = new CmsBoxi("Tiedoston lähetys");

				page.setTitle("Tiedoston lähetys");
				page.addCenter(box);
				page.addLeft(getActionLinks());
			}
		}});

	}

	@Override
	public void execute(){
		activate();
		super.execute();
	}

	private String updateRepository() {
		StringBuilder sb = new StringBuilder();
		try{
			Process update = Runtime.getRuntime().exec("sh paivita_sivut.sh");
			BufferedInputStream bes = new BufferedInputStream(update.getErrorStream());
			BufferedInputStream bis = new BufferedInputStream(update.getInputStream());
			int read;
			while((read = bes.read()) != -1){
				sb.append((char)read);
			}
			while((read = bis.read()) != -1){
				sb.append((char)read);
			}
			bis.close();
			bes.close();
			sb.append("\n process exit value: ").append(update.exitValue());
		}catch(IOException ioe){
			sb.append(ioe.toString());
		}catch(Exception e){
			sb.append(e.toString());
		}
		return sb.toString();
	}

	private String getActionLog() {
		FileHive filehive = FileHive.getFileHive(Cgicms.database_dir);
		String actionlog = filehive.getActionLog();
		return actionlog;		
	}

	private String clearSessions(){
		ActionLog.action("Cleaning old sessions");
		log.info("aloitetaan puhdistus");		

		File sessiondir = new File("..","sessions");
		if(sessiondir.isDirectory() && sessiondir.canRead()){
			log.info("can read session dir");
		}else{
			log.fail("cannot read session dir");
			return "cannot read session dir";
		}

		File[] sessions = sessiondir.listFiles();
		StringBuilder sb = new StringBuilder();

		for ( File session_file : sessions ){
			sb.append("id["+session_file.getName()+"]\n");
			long age = System.currentTimeMillis() - session_file.lastModified();
			sb.append(" - age["+age+"]ms\n");
			sb.append(" - "+Utils.longTimeToString(age)+"\n");
			if( age > 4000000 ){ //66min?
				log.info("deleting..");
				sb.append(" - deleting..");
				if(session_file.delete()){
					log.info("   ok");
					sb.append("ok");
				}else{
					log.info("   fail");
					sb.append("fail");
				}
			}else{
				sb.append(" - fresh enough");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void backupSettings() {
		for(File f: Cgicms.database_dir.listFiles()){
			if(f.isFile()){
				String[] temp = FileOps.readToArray(f);
				String filename = f.getName();
				FileOps.archive(f);
				FileOps.write(new File(Cgicms.database_dir,filename), temp, false);
			}
		}
	}
}

class Paiva{
	int date;
	int visitors;
	int bots;

	Paiva(int date, int visitors, int bots){
		this.date = date;
		this.visitors = visitors;
		this.bots = bots;
	}
}

