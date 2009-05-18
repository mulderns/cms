package d2o;

public class UserDbRecord {
	public String name;
	public String pass_hash;
	public String salt;
	public String[] groups = {"notinit"};

	UserDbRecord(String[] parts){
		if(parts.length < 3){
			name = "error";
			pass_hash = "error";
			salt = "error";
			groups = new String[1];
			groups[0] = "error";
		}else{
			name = parts[0];
			pass_hash = parts[1];
			salt = parts[2];
			if(parts.length > 3){
				groups = new String[parts.length-3];
				for(int i = 3; i < parts.length; i++){
					groups[i-3] = parts[i];
				}
			}else{
				groups = new String[1];
				groups[0] = "error";
			}
		}
	}

	public UserDbRecord(String name, String hash, String salt, String[] groups) {
		this.name = name;
		pass_hash = hash;
		this.salt = salt;
		this.groups = groups;
	}

	public String[] toArray() {
		if(groups == null){
			String[] output = new String[4];
			output[0] = name;
			output[1] = pass_hash;
			output[2] = salt;
			output[3] = "none";
			return output;
		}else{
			String[] output = new String[groups.length+3];

			output[0] = name;
			output[1] = pass_hash;
			output[2] = salt;
			for(int i = 0; i < groups.length; i++){
				output[i+3] = groups[i];
			}
			return output;
		}
	}

}
