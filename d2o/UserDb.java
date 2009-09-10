package d2o;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cms.Cgicms;

import util.Csv;
import util.Hasher;
import util.TriState;

public class UserDb {
	//private static final String linesep = System.getProperty("line.separator");

	private ArrayList<String> quickusers;
	private ArrayList<UserDbRecord> users;

	private File source;
	private FlushingFile dbfile;
	private long lmod;

	private TriState state;
	private static boolean created = false;
	private static UserDb present;

	public static UserDb getDb() {
		if(created){
			return present;
		}
		created = true;
		return new UserDb(Cgicms.group_hook);
	}

	private UserDb(String group_hook){
		quickusers = new ArrayList<String>();
		users = new ArrayList<UserDbRecord>();

		source = new File(Cgicms.database_dir,"users."+group_hook);
		dbfile = new FlushingFile(source);

		state = new TriState();
		setPresent(this);
	}

	private static void setPresent(UserDb userDb) {
		present = userDb;
	}

	public boolean addUser(String name, String pass, String[] groups){
		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				return false;
			}
		}
		String salt = Hasher.getSalt();
		users.add(new UserDbRecord(name,Hasher.hashWithSalt(pass, salt),salt,groups));
		quickusers.add(name);
		return true;
	}

	public boolean cleanDb(){
		if(quickusers.size() == users.size()){
			return false;
		}else if(quickusers.size() > users.size()){
			for(Iterator<String> iter = quickusers.iterator();iter.hasNext();){
				boolean found = false;
				String name = iter.next();
				for(UserDbRecord user : users){
					if(user.name.equals(name)){
						found = true;
						break;
					}
				}
				if(!found){
					iter.remove();
				}
			}
		}else if(quickusers.size() < users.size()){
			boolean found;
			for(UserDbRecord user : users){
				found = false;
				for(Iterator<String> iter = quickusers.iterator();iter.hasNext();){
					if(user.name.equals(iter.next())){
						found = true;
						break;
					}
				}
				if(!found){
					quickusers.add(user.name);
				}
			}
		}
		return true;
	}


	public boolean loadDb(){
		if(state.pure){
			lmod = source.lastModified();
			//System.err.println("loading userdb");
			//String[] raw = cms.FileHive.getFileHive().readFile2Array(source);
			String[] raw = dbfile.loadAll();
			//System.err.println("lines received ["+raw.length+"]");
			if(raw == null || raw.length < 2){
				return false;
			}
			for(String user : Csv.decode(raw[0])){
				quickusers.add(user);
			}
			for(int i = 1; i < raw.length; i++){
				//System.err.println("* "+raw[i]);
				users.add(new UserDbRecord(Csv.decode(raw[i])));

			}
			state.touch();
		}
		return true;
	}

	public String storeDb() {
		if(!state.open)
			loadDb();
		if(state.open){
			if(lmod != source.lastModified()){
				return "will not store, db has changed on disk after loading";
			}
			ArrayList<String> buffer = new ArrayList<String>();

			buffer.add(Csv.encode(quickusers.toArray(new String[quickusers.size()])));
			for(UserDbRecord user : users){
				buffer.add(Csv.encode(user.toArray()));
			}

			return dbfile.overwrite(buffer.toArray(new String[buffer.size()]));

			/**
			StringBuilder sb =  new StringBuilder();
			sb.append(Csv.encode(quickusers.toArray(new String[0]))+linesep);
			for(UserDbRecord user : users){
				sb.append(Csv.encode(user.toArray())+linesep);
			}
			if(cms.FileHive.getFileHive().storeFile(source, sb.toString())){
				return null;
			}*/
		}
		return "db state not favourable["+state+"]";
	}

	/*
	public boolean storeDb() {
		if(lmod != source.lastModified()){
			return false;
		}
		try{
			BufferedWriter bout = new BufferedWriter(new FileWriter(source));
			bout.write(Csv.encode(quickusers.toArray(new String[0]))+linesep);
			for(User user : users){
				bout.write(Csv.encode(user.toArray())+linesep);
			}
			bout.close();
		}catch(IOException ioe){
			return false;
		}
		return true;
	}
	 */
	public boolean removeUser(String name) {
		if(!state.open){
			loadDb();
		}
		if(state.open){
			for(UserDbRecord user : users){
				if(user.name.equals(name)){
					users.remove(user);
					for(Iterator<String> iter = quickusers.iterator();iter.hasNext();){

						if( iter.next().equals(name)){
							iter.remove();
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean addUserToGroups(String name, String[] groups) {
		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				int length = user.groups.length-1;
				user.groups = Arrays.copyOf(user.groups, user.groups.length+groups.length);
				for(int i = 0; i < groups.length; i++){
					user.groups[i+length] = groups[i];
				}
				return true;
			}
		}
		return false;
	}

	public String getNamesString() {
		StringBuilder sb = new StringBuilder();
		for (String s : quickusers){
			sb.append(s).append(",");
		}
		return sb.toString();
	}

	public boolean changePass(String name, String pass) {
		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				user.salt = Hasher.getSalt();
				user.pass_hash = Hasher.hashWithSalt(pass, user.salt);
				return true;
			}
		}
		return false;
	}

	public String getPass(String name) {
		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				return user.pass_hash;
			}
		}
		return null;
	}


	public String getSalt(String name) {
		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				return user.salt;
			}
		}
		return null;
	}

	public boolean hasUser(String name) {
		if(!state.open)
			loadDb();

		for(UserDbRecord user : users){
			if(user.name.equals(name)){
				return true;
			}
		}
		return false;
	}

	public static String[] getNames() {
		UserDb udb = UserDb.getDb();

		if(!udb.state.open){
			udb.loadDb();
		}
		return udb.quickusers.toArray(new String[udb.quickusers.size()]);
	}

	public static String[] getGroups(String username) {
		UserDb udb = UserDb.getDb();

		if(!udb.state.open){
			udb.loadDb();
		}

		for(UserDbRecord user: udb.users){
			if(user.name.equals(username)){
				return user.groups;
			}
		}
		return new String[0];
	}

	public UserDbRecord getUser(String username) {
		if(state.pure){
			loadDb();
		}
		if(state.open){
			for(UserDbRecord user: users){
				if(user.name.equals(username)){
					return user;
				}
			}
		}
		return null;
	}





	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(UserDbRecord ur: users){
			sb.append("n["+ur.name+"]\n");
			for(String g : ur.groups)
				sb.append(" "+g+"\n");
		}
		return sb.toString();

	}

	/** Gets user info for user 'name' from file if
	 *  available.
	 * @param name
	 * @return User info in UserInfoRecord object
	 */
	public UserInfoRecord getUserInfo(String name){
		
	}
	
	public boolean saveUserInfo(String name, UserInfoRecord record){
		
	}
	
}


