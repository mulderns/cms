package http;

import java.util.HashMap;

import util.Logger;

/**
 * keksi -- eikun eväste
 * 
 * joskus ajattelin että cookieen vois varastoida
 * jotain kenttiä mutta aika turhaa kun ne varas-
 * toidaan ehkä sinne sessioniin
 * 
 * @author Valtteri
 *
 */

public class HttpCookie {
	String raw_cookie;
	Logger log;
	HashMap<String,String> values;

	public HttpCookie(){
		raw_cookie = "";
		log = new Logger("HttpCookie");
		values = new HashMap<String,String>();
	}

	public HttpCookie(String fromEnv, Logger hostLog){
		log = hostLog;
		raw_cookie = fromEnv;
		log.info("cookie:"+raw_cookie);
		//example (w/out '['): [RFID=user%3Dfail%26pass%3Dfail; CMSes=1203238995979]
		values = new HashMap<String,String>();
		String[] temp;
		if(raw_cookie.contains(";")){
			log.info(" -> multiple cookies");
			String[] raws = raw_cookie.split(";");		

			for (String string : raws) {
				temp = string.trim().split("=");
				if(temp.length == 2){
					log.info("cookiefield: "+temp[0]+" - "+ temp[1]);
					values.put(temp[0], temp[1]);
				}else{
					log.info("stupid cookie split failed - 1");
				}
			}
		}else{
			temp = raw_cookie.split("=");
			if(temp.length==2){
				log.info("cookiefield: "+temp[0]+" - "+ temp[1]);
				values.put(temp[0], temp[1]);
			}else{
				log.info("stupid cookie split failed - 2");
			}
		}
	}

	public String toString(){
		return raw_cookie;
	}

	public boolean hasField(String fieldName) {
		log.info("has:"+fieldName+"\n -> "+values.containsKey(fieldName));
		return values.containsKey(fieldName);
	}

	public String getField(String fieldName) {
		return values.get(fieldName);
	}

	public HashMap<String, String> toHashMap() {
		return values;
	}


}

