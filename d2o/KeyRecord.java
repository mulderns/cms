package d2o;

public class KeyRecord {

	public String username;
	public long validity;
	public String key;
	public boolean used;
	
	public KeyRecord(){
		this("",0,"");
	}

	public KeyRecord(String username, long validity, String key){
		this.username = username;
		this.validity = validity;
		this.key = key;
		used = false;
	}
	
	public KeyRecord(String[] parts){
		key = parts[0];
		validity = Long.parseLong(parts[1]);
		if(parts[2].equals("-")){
			used = false;
		}else {
			used = true;
		}
		username = parts[3];
	}
	
	public String[] toArray(){
		String[] array = {
			key,
			Long.toString(validity),
			(used?"+":"-"),
			username
		};
		return array; 
	}
}