package d2o;

import java.util.Arrays;

public class UserInfoRecord {
	public String full_name;
	public String phone;
	public String email;

	public String tittle;
	public Boolean hallituksessa;
	public Boolean toimari;
	public String file;

	public UserInfoRecord(String[] parts) {
		if(parts.length == 7){

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
			
		}else{
			full_name = parts[0];
			phone = "parts == " + Integer.toString(parts.length);
			email = Arrays.toString(parts);
			tittle = "error";
			hallituksessa = false;
			toimari = false;
			file = "error";
		}
	}

	public UserInfoRecord(){
		this("","","","",false,false,"");
	}

	public UserInfoRecord(String full_name, String phone, String email,
			String tittle, boolean hallituksessa, boolean toimari, String tiedosto) {
		this.full_name = full_name;			//0
		this.phone = phone;					//1
		this.email = email;					//2
		this.tittle = tittle;				//3
		this.hallituksessa = hallituksessa;	//4
		this.toimari = toimari;				//5
		this.file = tiedosto;				//6
	}

	public String[] toArray() {
		String[] output = { 
				full_name, 
				phone, 
				email, 
				tittle,
				(hallituksessa? "h" : "-"),
				(toimari? "t" : "-"),
				file
		};
		return output;
	}

}
