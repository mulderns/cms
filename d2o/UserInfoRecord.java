package d2o;

public class UserInfoRecord {
	public String full_name;
	public String phone;
	public String email;

	public String tittle;
	public Boolean hallituksessa;
	public Boolean toimari;
	public String file;

	UserInfoRecord(String[] parts) {
		full_name = parts[0];
		phone = parts[1];
		email = parts[2];
		tittle = parts[3];
		if (parts[4].equals("h")) {
			hallituksessa = true;
		} else{
			hallituksessa = false;
		}
		if(parts[5].equals("t")){
			toimari = true;
		}else{
			toimari = false;
		}
		file = parts[6];
	}

	public UserInfoRecord(){
		this("","","","",false,false,"");
	}

	public UserInfoRecord(String full_name, String phone, String email,
			String tittle, boolean hallituksessa, boolean toimari, String tiedosto) {
		this.full_name = full_name;
		this.phone = phone;
		this.email = email;
		this.tittle = tittle;
		this.hallituksessa = hallituksessa;
		this.toimari = toimari;
		this.file = tiedosto;
	}

	public String[] toArray() {
		String[] output = { full_name, phone, email, tittle,
				(hallituksessa? "h" : "-"),
				(toimari? "t" : "-"),
				file };
		return output;
	}

}
