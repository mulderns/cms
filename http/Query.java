package http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import util.Logger;
import util.Utils;

public class Query {
	private Logger log;
	private String original;
	public HashMap<String,String> fields;
	/*private String[] keys;
	private String[] values;*/

	public Query(){
		original = "";
		fields = new HashMap<String,String>();
		log = new Logger("Query");
	}

	public Query(String fromEnv, Logger hostLog){
		log = hostLog;
		//try {
		//	original = URLDecoder.decode(fromEnv, "ISO-8859-1");
		//} catch (UnsupportedEncodingException e) {
		original = fromEnv;
		//};
		log.info(" query_string["+original+"]");

		//count number of possible fields
		int parts = Utils.countChar('&', original)+1;
		log.info("parts: "+parts);

		//parse fields
		fields = new HashMap<String,String>(parts+2); //:P //wtf?
		for(String pair : original.split("\\&")) {
			String[] individuals = pair.split("=");
			try {
				if(individuals.length == 2){
					fields.put(URLDecoder.decode(individuals[0], "ISO-8859-1"),URLDecoder.decode(individuals[1], "ISO-8859-1"));
					log.info("stored:" + individuals[0] +" - " + individuals[1]);
				}else if (individuals.length == 1){
					fields.put(URLDecoder.decode(individuals[0], "ISO-8859-1"),URLDecoder.decode(individuals[0], "ISO-8859-1"));
					log.info("stored:" + individuals[0] +" + " + individuals[0]);
				}

			} catch (UnsupportedEncodingException e) {


				if(individuals.length == 2){
					fields.put(individuals[0],individuals[1]);
					log.info("stored:" + individuals[0] +" - " + individuals[1]);
				}else if (individuals.length == 1){
					fields.put(individuals[0],individuals[0]);
					log.info("stored:" + individuals[0] +" + " + individuals[0]);
				}
			}
		}
	}

	public String getQueryString(){
		return original;
	}

	public boolean hasField(String fieldName) {
		return fields.containsKey(fieldName);
	}

	public String getField(String fieldName){
		return fields.get(fieldName);
	}

	public String toString(){
		StringBuilder buff = new StringBuilder();
		buff.append("query\n");
		buff.append("original:"+ original+"\n");
		buff.append("values:\n");
		for (String string : fields.values()) {
			buff.append(" " + string + "\n");
		}
		return buff.toString();
	}

	public HashMap<String,String> toHashMap(){
		return fields;
	}
	/*
	public String getFirst() {
		return keys[0];
	}*/
}

