package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class ActionLog {
	//private Logger2 log;
	private static final ArrayList<String> buffer = new ArrayList<String>();
	private static File logfile = new File("actionlog");
	private static final byte[] linesep = System.getProperty("line.separator").getBytes();
	private static final Calendar cal = Calendar.getInstance();

	/*
	public ActionLog(File logfile){
		cal = Calendar.getInstance();
		log = new Logger2("ActionLog");
		buffer = new ArrayList<String>();
		this.logfile = logfile;
		linesep = System.getProperty("line.separator").getBytes();
	}
	 */

	public static void log(final String message){
		buffer.add(
				cal.get(Calendar.YEAR) + 
				Utils.addLeading((cal.get(Calendar.MONTH)+1),2) + 
				Utils.addLeading(cal.get(Calendar.DATE),2) + " " +  

				Utils.addLeading(cal.get(Calendar.HOUR_OF_DAY),2) +
				Utils.addLeading(cal.get(Calendar.MINUTE),2) +
				Utils.addLeading(cal.get(Calendar.SECOND),2) + " " +
				message
		);
	}

	public static void exception(final String message){
		log("exc: "+message);
	}
	public static void error(final String message){
		log("err: "+message);
	}
	public static void action(final String message){
		log("act: "+message);
	}
	public static void system(final String message){
		log("sys: "+message);
	}
	public static void time(final String message){
		log("tim: "+message);
	}

	public static void setLogFile(final File _logfile){
		logfile = _logfile;
	}

	public static void write() {
		if(buffer.size() > 0){
			try {
				BufferedOutputStream bfout = new BufferedOutputStream(new FileOutputStream(logfile, true));

				for (String line : buffer) {
					bfout.write(line.getBytes());
					bfout.write(linesep);
				}
				bfout.close();
				//log.info("wrote actionlog successfully");
			} catch (FileNotFoundException fnfe) {
				//log.severe("could not write actionlog:"+fnfe);
			} catch (IOException ioe){
				//log.severe("could not write actionlog:"+ioe);
			}
		}
	}

	public static String getLog() {
		StringBuilder sb = new StringBuilder();
		for (String line : buffer) {
			sb.append(line+"\n");
		}
		return sb.toString();
	}
}
