package cms.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import util.Csv;
import cms.Cgicms;

public class UserLoader {
	private String user_name;
	private File source;
	private BufferedReader bin;
	private User user;

	public UserLoader(String login_name){
		user_name = login_name;
		source = new File(Cgicms.database_dir, "users."+Cgicms.group_hook);
	}

	public boolean quickCheckUser() {
		if(!source.canRead()){
			Cgicms.log.fail("can't read file:"+source.getAbsolutePath());
			return false;
		}
		try {
			bin = new BufferedReader(new FileReader(source));
			String line = bin.readLine();
			if(line != null) {
				String[] temp = Csv.decode(line);
				for (String string : temp) {
					if(string.equals(user_name))
						return true;
				}
			}
			bin.close();
			Cgicms.log.fail("quick check returns false");
			return false;

		} catch (IOException ioe) {
			Cgicms.log.fail("user load could not complete: "+ioe);
		}
		return false;
	}

	public boolean load() {
		try {
			String line;
			while ((line = bin.readLine()) != null) {
				String[] values = Csv.decode(line);
				if(values[0].equals(user_name)){
					bin.close();
					user = User.create(values);
					return user != null;
				}
			}
			bin.close();
		} catch (IOException ioe) {
			Cgicms.log.fail("user load could not complete: "+ioe);
		}
		return false;
	}

	public String getSalt() {
		return user.getSalt();
	}

	public String getPass() {
		return user.getPass();
	}

	/*public void loadGroups() {
		ArrayList<String> groups = user.getGroups();
		groups = new ArrayList<String>();
		for(String group : UserDb.getGroups(user_name)){
			groups.add(group);
		}
	}*/

	/*public void loadInfo() {
		FlushingFile infofile = new FlushingFile(
				new File(Cgicms.database_dir,Cgicms.group_hook+"-"+user_name+".info")
		);
		user.info = new HashMap<String, String>();
		for(String line: infofile.loadAll()){
			String[] parts = util.Csv.decode(line);
			if(parts.length==2){
				user.info.put(parts[0], parts[1]);
			}
		}
	}*/

	public User getUser() {
		return user;
	}

}
