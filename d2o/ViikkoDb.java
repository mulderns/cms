package d2o;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import util.ActionLog;
import util.Csv;
import util.Logger;
import util.Utils;
import cms.Cgicms;
import cms.FileOps;

public class ViikkoDb {
	Logger log;

	File db_file;

	public ViikkoDb(){
		log = new Logger("ViikkoDb");
		db_file = new File(Cgicms.products_dir,"viikkodb");

	}

	private String genFilename(ViikkoEntry ve){
		if(ve.auto){
			return "automatic.hap";
		}else if(ve.pohjaksi){
			return "templates.hap";
		}

		Calendar experim = Calendar.getInstance();
		experim.clear();
		try{
			experim.set(ve.year, ve.month-1, ve.day);
			experim.setFirstDayOfWeek(Calendar.MONDAY);
			return genFilename(ve.year,experim.get(Calendar.WEEK_OF_YEAR));
		}catch(Exception e){
			return null;
		}
	}

	private String genFilename(String id){
		log.info("generating filename from id");
		int year, month, day;

		if(id.startsWith("automatic")){
			return "automatic.hap";
		}else if(id.startsWith("template")){
			return "templates.hap";
		}else{

			try{
				year = Integer.parseInt(id.substring(0,4));
				month = Integer.parseInt(id.substring(4,6));
				day = Integer.parseInt(id.substring(6,8));
				log.info("d["+day+"] m["+month+"] y["+year+"]");
			}catch(NumberFormatException nfe){
				log.fail("day2 nfe:"+nfe);
				log.info("numberformat exception:"+nfe);
				return null;
			}

			Calendar experim = Calendar.getInstance();
			experim.clear();
			try{
				experim.set(year, month-1, day);
				experim.setFirstDayOfWeek(Calendar.MONDAY);
				log.info("generated filename from id ["+id+"] -> ["+year + Utils.addLeading(experim.get(Calendar.WEEK_OF_YEAR),2) + ".hap"+"]");
				return genFilename(year,experim.get(Calendar.WEEK_OF_YEAR));

			}catch(Exception e){
				log.info("date ill formed");
				return null;
			}
		}
	}

	private String genFilename(int year, int week){
		return year + Utils.addLeading(week,2) + ".hap";
	}

	public String addEntry(ViikkoEntry ve){
		FlushingFile dest = new FlushingFile(new File(db_file, genFilename(ve)));
		return dest.append(Csv.encode(ve.fieldsToArray()));
	}

	public String removeEntry(String id){
		String filename = genFilename(id);
		if(filename == null){
			return "can't generate filename from id";
		}

		FlushingFile dest = new FlushingFile(new File(db_file, filename));
		String[] lines = dest.loadAll();
		log.info("read lines["+lines.length+"]");
		ArrayList<String> store = new ArrayList<String>(lines.length);
		boolean found = false;
		for(String line : lines){
			if(line != null){
				ViikkoEntry ve = new ViikkoEntry(Csv.decode(line));
				log.info("/ ve["+ve.id+"]");
				log.info("  id["+id+"]");
				if(!ve.id.equals(id)){
					log.info(" -");
					store.add(line);
				}else{
					log.info(" +");
					found = true;
				}
			}
		}
		if(found){
			return dest.overwrite(store.toArray(new String[store.size()]));
		}else{
			log.fail("can't remove, id not found");
			return "id not found";
		}

	}

	public ArrayList<ViikkoEntry> getUserEntries(String username) {
		ArrayList<ViikkoEntry> array = new ArrayList<ViikkoEntry>();
		if(username.equals("all")){
				for(File _file : FileOps.getFiles(db_file,"hap")){
				String file = _file.getName();
				if(!(file.equals("templates.hap") || file.equals("automatic.hap"))){
					for(ViikkoEntry ve : loadEntries(file,false,false)){
						array.add(ve);
					}
				}
			}
		}else if(username.equals("auto")){
			for(File _file : FileOps.getFiles(db_file,"hap")){
				String file = _file.getName();
				if(file.equals("automatic.hap")){
					for(ViikkoEntry ve : loadEntries(file,false,false)){
						array.add(ve);
					}
				}
			}
		}else{
			for(File _file : FileOps.getFiles(db_file,"hap")){
				String file = _file.getName();
				for(ViikkoEntry ve : loadEntries(file,false,false)){
					if(ve != null && ve.user.equals(username)){
						log.fail("wrong users gotten");
					}
				}
			}

		}
		Collections.sort(array);
		return array;		
	}

	private ArrayList<ViikkoEntry> loadEntries(String filename, boolean vain_mailiin, boolean vain_etukateen) {
		ArrayList<ViikkoEntry> temp = new ArrayList<ViikkoEntry>();
		FlushingFile dest = new FlushingFile(new File(db_file, filename));
		String[] lines = dest.loadAll();

		for(String line : lines){
			if(line != null){
				ViikkoEntry ve = new ViikkoEntry(Csv.decode(line));
				if(vain_mailiin){
					if(vain_etukateen){
						if(ve.mailiin&&ve.etukateen)
							temp.add(ve);
					}else{
						if(ve.mailiin)
							temp.add(ve);
					}
				}else if(vain_etukateen){
					if(ve.etukateen){
						temp.add(ve);
					}
				}else{
					temp.add(ve);
				}
				log.info(ve.otsikko);
			}
		}
		return temp;
	}



	public ViikkoEntry loadEntry(String id) {
		ViikkoEntry temp;

		String filename = genFilename(id);
		if(filename == null){
			return null;
		}

		FlushingFile dest = new FlushingFile(new File(db_file, filename));
		String[] lines = dest.loadAll();

		for(String line : lines){
			temp = new ViikkoEntry(Csv.decode(line));
			if(temp.id.equals(id)){
				log.info(temp.otsikko);
				return temp;
			}
		}
		return null;
	}


	public ViikkoEntry[] getWeek(int offset, boolean vain_mailiin, boolean vain_etukateen) {
		//generate filenames this week and next;
		Calendar now = Calendar.getInstance();
		now.setFirstDayOfWeek(Calendar.MONDAY);
		now.add(Calendar.WEEK_OF_YEAR, offset);
		int week = now.get(Calendar.WEEK_OF_YEAR);
		int year = now.get(Calendar.YEAR);

		ArrayList<ViikkoEntry> array = loadEntries(genFilename(year,week), vain_mailiin, vain_etukateen);
		if(array != null){
			return array.toArray(new ViikkoEntry[array.size()]);
		}else{
			return new ViikkoEntry[0];
		}
	}

	public int getWeekNumber(int offset) {
		Calendar now = Calendar.getInstance();
		now.setFirstDayOfWeek(Calendar.MONDAY);
		now.add(Calendar.WEEK_OF_YEAR, offset);
		return now.get(Calendar.WEEK_OF_YEAR);
	}

	public void removeOld() {
		Calendar now = Calendar.getInstance();
		now.setFirstDayOfWeek(Calendar.MONDAY);
		int cur_year = now.get(Calendar.YEAR);
		int cur_week = now.get(Calendar.WEEK_OF_YEAR);

		//FileHive fh = FileHive.getFileHive(db_file);
		for(File _file : FileOps.getFiles(db_file,"hap")){
			String file = _file.getName();
			if(file.length() > 4){
				int year = 0;
				int week = 0; 
				try{
					year = Integer.parseInt(file.substring(0, 4));
					week = Integer.parseInt(file.substring(4, file.indexOf('.')));
				}catch (NumberFormatException nfe) {
					log.info("error parsing file name:"+nfe);
					continue;
				}
				log.info("["+year+"]<["+cur_year+"]   ["+week+"]<["+cur_week+"]");
				if(year < cur_year || week < cur_week)
					FileOps.archive(_file);
			}

		}
	}

	/*
	public String addTemplate(ViikkoEntry ve) {
		File dest = new File(db_file, "templates.hap");
		File lock = new File(db_file, dest.getName()+".lock");
		FileHive fh = FileHive.getFileHive(db_file);
		int trys = 5;
		try{
			if(dest.exists()){
				while(trys > 0){
					if(lock.createNewFile()){
						ve.id = ve.genTempId();
						ve.day = 0;
						ve.month = 0;
						ve.year = 0;
						fh.appendFileIso(dest.getName(), Csv.encode(ve.fieldsToArray()));
						if(!lock.delete())
							log.info("could not delete ["+lock.getName()+"]");
						ActionLog.system("add trys ["+trys+"]");
						return null;
					}else{
						Utils.sleep(100);
						trys--;
					}
				}
				ActionLog.error("out of trys");
				return "out of tryes";
			}else{
				log.info("creating new file");
				fh.appendFileIso(dest.getName(), Csv.encode(ve.fieldsToArray()));
				return null;
			}
		}catch (IOException ioe) {
			log.severe("ioexception adding entry:"+ioe);
		}

		return "add failed";
	}*/
	/*
	public String removeTemlate(String id){
		// generate right filename
		String filename = "templates.hap";
		if(filename == null){
			return "can't generate filename from id";
		}

		// lock file
		File dest = new File(db_file, filename);
		File lock = new File(db_file, filename+".lock");

		FileHive fh = FileHive.getFileHive(db_file);
		int trys = 5;
		boolean found = false;
		try{
			if(dest.exists()){
				while(trys > 0){
					if(lock.createNewFile()){
						ActionLog.system("remove trys ["+trys+"]");
						String[] lines = fh.readFileIsoToArray(dest.getName());
						log.info("read lines["+lines.length+"]");
						ArrayList<String> store = new ArrayList<String>(lines.length);
						for(String line : lines){
							if(line != null){
								ViikkoEntry ve = new ViikkoEntry(Csv.decode(line));
								log.info("/ ve["+ve.id+"]");
								log.info("  id["+id+"]");
								if(!ve.id.equals(id)){
									log.info(" -");
									store.add(line);
								}else{
									log.info(" +");
									found = true;
								}
							}
						}
						if(found){
							boolean isit = fh.storeFile(dest.getName(), store);
							log.info("stored successfully " + isit);
							lock.delete();
							return null;
						}else{
							log.info("id not found");
							if(!lock.delete())
								log.severe("could not delete["+lock.getName()+"]");
							return "id not found";
						}
					}else{
						Utils.sleep(100);
						trys--;
					}
				}
				ActionLog.error("out of trys");
				log.severe("out of trys");
				return "db timed out";
			}else{
				log.severe("file does not exist ["+dest.getName()+"]");
				return "record file not found";
			}
		}catch (IOException ioe) {
			log.severe("exception occurred:"+ioe);
			return "io exception";
		}
	}*/
	/*
	public ViikkoEntry loadTemplate(String id) {
		ViikkoEntry temp;

		//String filename = genFilename(id);
		File dest = new File(db_file, "templates.hap");
		File lock = new File(db_file, dest.getName()+".lock");

		FileHive fh = FileHive.getFileHive(db_file);
		int trys = 5;
		if(dest.exists()){
			while(trys > 0){
				if(!lock.exists()){
					ActionLog.system("read trys ["+trys+"]");
					String[] lines = fh.readFileIsoToArray(dest.getName());
					for(String line : lines){
						temp = new ViikkoEntry(Csv.decode(line));
						if(temp.id.equals(id)){
							log.info(temp.otsikko);
							return temp;
						}
					}
					return null;
				}else{
					Utils.sleep(100);
					trys--;
				}
			}
			ActionLog.error("out of trys");
			log.severe("out of trys");
		}else{
			log.severe("file does not exist ["+dest.getName()+"]");
		}

		return null;
	}

	public ViikkoEntry[] loadTemplates() {
		ArrayList<ViikkoEntry> temp = new ArrayList<ViikkoEntry>();

		File dest = new File(db_file, "templates.hap");
		File lock = new File(db_file, dest.getName()+".lock");

		//		FileHive fh = FileHive.getFileHive(db_file);
		int trys = 5;
		if(dest.exists()){
			while(trys > 0){
				if(!lock.exists()){
					if(trys < 5)
						ActionLog.system("read trys ["+trys+"]");
					String[] lines = FileOps.readToArray(new File(db_file,dest.getName()));
					//					String[] lines = fh.readFileToArrayIso(dest.getName());
					for(String line : lines){
						if(line != null){
							//temp.add(new ViikkoEntry(Csv.decode(line)));
							ViikkoEntry ve = new ViikkoEntry(Csv.decode(line));
							temp.add(ve);
							//log.info(ve.otsikko);
						}
					}					
					return temp.toArray(new ViikkoEntry[temp.size()]);
				}else{
					Utils.sleep(100);
					trys--;
				}
			}
			ActionLog.error("out of trys");
			log.fail("out of trys");
		}else{
			log.fail("load_temp: file does not exist ["+dest.getName()+"]");
		}

		return new ViikkoEntry[0];
	}*/

	public void checkDb() {
		if(!db_file.exists()){
			if(!db_file.mkdir()){
				ActionLog.error("could not create viikkodb dir");
				log.fail("could not create viikkodb dir");
			}
		}
	}

}

