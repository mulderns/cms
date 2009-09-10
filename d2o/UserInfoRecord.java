package d2o;

public class UserInfoRecord {
	public String full_name;
	public String phone;
	public String email;
	
	public String tittle;
	public Boolean hallituksessa;
	

	UserInfoRecord(String[] parts){
		full_name = parts[0];
		phone = parts[1];
		email = parts[2];
		tittle = parts[3];
		if(parts[4].equals("h")){
			hallituksessa = true;
		}else if(parts[4].equals("t")){
			hallituksessa = false;
		}else{
			hallituksessa = null;
		}
	}

	public UserInfoRecord(String full_name, String phone, String email, String tittle, Boolean hallituksessa) {
		this.full_name = full_name;
		this.phone = phone;
		this.email = email;
		this.tittle = tittle;
		this.hallituksessa = hallituksessa;
	}

	public String[] toArray() {
			String[] output = {
					full_name,
					phone,
					email,
					tittle,
					(hallituksessa==null?"-":(hallituksessa?"h":"t"))
			};
			return output;
	}

}