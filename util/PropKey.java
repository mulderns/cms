package util;

public class PropKey {

	public String identifier;
	public String description;
	public String default_value;

	public PropKey(String id, String def, String desc){
		identifier = id;
		default_value = def;
		description = desc;
	}

}
