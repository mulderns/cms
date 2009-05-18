package util;

import java.io.Serializable;
import java.util.ArrayList;
// version 0.2c
/**
 * tämä on tämmönen mun tyylin koodauksessa tärkeä loggeri
 * jota käytän yleensä kertomaan mitä ohjelmassa tapahtuu
 * 
 * logi tulee ainakin tällähetkellä errorstreamiin ja sen
 * oon pistäny sieltä käynnistävästä skriptistä ohjautumaan
 * tiedostoon.
 * 
 * @author Valtteri
 */

public class Logger implements Serializable{

	private static final long serialVersionUID = -2165973834570820436L;

	private static boolean enableSystemOut = false;
	private static final boolean leakSevere = true;
	
	private final ArrayList<LogEvent> events;
	private final String parent;

	public Logger(){
		this(" ");
	}

	public Logger(String parent){
		this.parent = parent;
		events = new ArrayList<LogEvent>();
		events.add(new LogEvent(parent, LogEvent.eventType.meta, "Startinglog.."));
		//meta("# Logging - "+ parent +" ###");
	}

	public Logger(Logger clone) {
		//enableSystemOut = clone.enableSystemOut;
		//clone.events.
		events = new ArrayList<LogEvent>();
		events.addAll(clone.events);
		parent = new String(clone.parent);
	}

	private final void log(LogEvent.eventType type, String message){
		final LogEvent spur = new LogEvent(parent, type, message);
		events.add(spur);
		if(enableSystemOut){
			/*System.err.println(""); // to get right end of line char
			System.err.print(spur);*/
			System.err.println(spur);
		}else if(leakSevere && type.equals(LogEvent.eventType.fail)){
			System.err.println(spur);
		}
	}

	public final void meta(final String message){
		log(LogEvent.eventType.meta, message);
	}

	public final void info(final String message){
		log(LogEvent.eventType.info, message);
	}

	public final void fail(final String message){
		log(LogEvent.eventType.fail, message);

		//output("log:severe:"+message);

	}

	public final void hard(final String message){
		log(LogEvent.eventType.hard, message);
	}

	public static final void setEnableSystemOut(final Boolean maybe){
		enableSystemOut = maybe;
	}

	public final String getLog(){
		final StringBuilder apu = new StringBuilder();
		for (LogEvent event : events) {
			apu.append(event).append("\n");
		}
		return apu.toString();
	}

	public final String getByType(LogEvent.eventType type){
		final StringBuilder apu = new StringBuilder();
		for (LogEvent event : events) {
			if(event.type == type){
				apu.append(event).append("\n");
			}
		}
		return apu.toString();
	}

	public final String getLast() {
		return events.get(events.size()-1).message;
	}
}

