package util;

import java.util.ArrayList;


public class Csv {
	public static final String[] decode(final String raw) {
		final ArrayList<String> entities = new ArrayList<String>();
		final int end = raw.length();
		//System.err.println("Csv decode["+raw+"]");
		int state = 0;

		boolean quote_found = false;
		boolean single_found = false;

		int alku = 0;
		char c;

		for (int i = 0; i < end; i++) {
			c = raw.charAt(i);
			if(c == '\'')
				single_found = true;
			if(state == 1){
				switch (c) {
				case ',':
					if (!quote_found && alku != i) { //hmm, tarvitaanko alku != i vertailua? aina true?
						state = 0;
						entities.add((single_found?raw.substring(alku, i).replace("''", "\""):raw.substring(alku, i)));
						alku = i + 1;
					}
					break;
				case '"':
					if (quote_found) {
						state++; //state -> 2
						quote_found = false;
						entities.add((single_found?raw.substring(alku, i).replace("''", "\""):raw.substring(alku, i)));
						alku = i + 1;
					}
				}
			}else if(state == 0){ // true kun ei vielä kirjaimia
				switch (c) {
				case ',':
					//if (alku != i) {
						entities.add("");
						alku = i + 1;
						//} else {
						//	alku++;
						//}
					break;
				case '"':
					state++;
					quote_found = true;
					alku = i + 1;
					break;
				case ' ':
					alku++;
					break;
				default:
					state++;
				}				
			}else{ // '"' jälkeen
				if (c == ',') {
					state = 0;
					alku = i + 1;
				}
			}
			
			if (i + 1 == end) {
				if (state == 0) {
					entities.add("");
				} else if(state==1){
					entities.add((single_found?raw.substring(alku, i + 1).trim().replace("''", "\""):raw.substring(alku, i + 1).trim()));
					//entities.add(raw.substring(alku, i + 1).trim());
				}
			}
		}
		//System.err.println(" ["+Arrays.toString(jep.toArray(new String[jep.size()]))+"]");
		return entities.toArray(new String[entities.size()]);
	}
	
	public static final String encode(final String[] raw) {
		final StringBuilder sbuf = new StringBuilder();

		String entity;
		char c;
		boolean problem_found, worse_found;
		for (int i = 0; i < raw.length; i++) {
			entity = raw[i];
			problem_found = false;
			worse_found = false;
			for (int j = 0; j < entity.length(); j++) {
				c = entity.charAt(j);
				if (c == ',' || c == ' ') {
					problem_found = true;
					//break;
				}else if(c == '"'){
					worse_found = true;
				}
			}
			if(worse_found)
				entity = entity.replace("\"", "''");
			
			if (problem_found) {
				sbuf.append("\"" + entity + "\",");
				continue;
			}
			sbuf.append(entity + ",");
		}

		final int length = sbuf.length();
		if (length > 1) {
			return sbuf.substring(0, length - 1);
		}
		return "";
	}
}
