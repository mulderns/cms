package util;

import java.io.Serializable;

public class LogEvent implements Serializable{

	enum eventType {
		info,
		meta,
		fail,
		hard
	}

	private static final long serialVersionUID = -1761598914407997912L;
	String parent;
	eventType type;
	String message;

	public LogEvent(eventType type, String message){
		this.parent = "--";
		this.type = type;
		this.message = message;
	}

	public LogEvent(String parent, eventType type, String message){
		this.parent = parent;
		this.type = type;
		this.message = message;
	}

	public LogEvent(LogEvent clone) {
		parent = new String(clone.parent);
		type = clone.type;
		message = new String(clone.message);
	}

	public String toString(){
		//return parent +"- "+ type +" :"+ message;
		return type+" ["+parent+"] : " + message;
	}
}

