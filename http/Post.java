package http;

import java.util.ArrayList;
import java.util.HashMap;

public class Post {
	//Logger2 log;
	//public HashMap<String,FormPart> fields;
	public ArrayList<FormPart> fieldes;

	public Post(){
		//log = new Logger2("Post");
		//fields = new HashMap<String,FormPart>();
		fieldes = new ArrayList<FormPart>();
	}

	public void addField(String key, String value) {
		//log.info("adding field:");
		FormPart temp = new FormPart();
		temp.setName(key);
		temp.setValue(value);
		//log.info(" "+key+":"+temp.value);
		fieldes.add(temp);
	}

	public void addPart(FormPart value) {
		//log.info("adding part:");
		//log.info(" "+key+":"+value.toString());
		fieldes.add(value);

	}

	public String getField(String key) {
		for(FormPart p : fieldes){
			if(p.name.equals(key)){
				return p.value;
			}
		}
		return null;
	}

	public FormPart getPart(String key){
		for(FormPart p : fieldes){
			if(p.name.equals(key)){
				return p;
			}
		}
		return null;
	}
/*
	public boolean hasPart(String key) {
		return fields.containsKey(key);
	}
	*/
	public boolean hasField(String string) {
		for(FormPart p : fieldes){
			if(p.name.equals(string)){
				return true;
			}
		}
		return false;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Post has:");
		for (FormPart p : fieldes) {
			sb.append(" ");
			sb.append(p.name);
			sb.append('\n');
		}
		return sb.toString();
	}

	public HashMap<String,String> toHashMap(){
		HashMap<String,String> temp = new   HashMap<String, String>();
		for(FormPart p : fieldes){
			temp.put(p.name, p.value);
		}
		return temp;
	}

	public FormPart[] getFiles() {
		ArrayList<FormPart> temp = new ArrayList<FormPart>();
		for(FormPart p : fieldes){
			if(p.filename != null){
				temp.add(p);
			}
		}
		return temp.toArray(new FormPart[temp.size()]);
	}
	
	/*public HashMap<String, byte[]> getFiles() {
		HashMap<String, byte[]> temp = new HashMap<String, byte[]>();
		for(FormPart p : fieldes){
			if(p.filename != null){
				temp.put(p.filename, p.bytes);
			}
		}
		return null;
	}*/

}
