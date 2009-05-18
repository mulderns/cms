package cms.access;

import java.io.Serializable;
import java.util.ArrayList;

/** changes not done: User.java
 * all usernames to firstline for quick check;
 */

public class User implements Serializable{
	private static final long serialVersionUID = -7929861620622731162L;
	private String name;
	private String password;
	private String salt;
	private ArrayList<String> groups;
	//private HashMap<String,String> info;

	private User(){
	}

	static User create(String[] values){
		if(values.length < 3)
			return null;

		User user = new User();
		user.name = values[0];
		user.password = values[1];
		user.salt = values[2];

		if(values.length > 3){
			user.groups = new ArrayList<String>(values.length-3);
			for(int i = 3; i < values.length; i++)
				user.groups.add(values[i]);
		}		
		
		return user;
	}

	public String getName() {
		return name;
	}

	public String getPass() {
		return password;
	}

	public ArrayList<String> getGroups() {
		return groups;
	}

	/*public void setName(String name) {
		this.name = name;		
	}*/

	public String getSalt() {
		return salt;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("id: ");
		sb.append(name);
		sb.append("\ngroups:");

		for (String group : groups) {
			sb.append("\n ");
			sb.append(group);
		}
		return sb.toString();
	}
}
