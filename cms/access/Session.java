package cms.access;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import util.ActionLog;
import util.Logger;

import cms.Cgicms;
import cms.DataRelay;

public class Session {

	/*public static void remove(String sid) {
		Cgicms.log.info("removing session: " + sid);
		File file = new File(Cgicms.sessions_dir , sid); 
		if(file.delete()){
			ActionLog.system("deleted "+sid);
			Cgicms.log.info(" -> successfully removed: "+file);
		}else{
			ActionLog.error("failed to delete "+sid);
			Cgicms.log.info(" -> failed to remove: "+file);
		}

	}*/

	private Logger log;
	public boolean delete = false;
	public boolean temporary = false;
	
	private boolean changed = false;
	private String session_id;
	private String ip;
	private long lastAccess;
	private String cookie_hook;
	private User user;
	private HashMap<String, String> attributes;

	private ObjectInputStream oin;

	public Session(){
		log = new Logger("session");
	}

	public Session(DataRelay datarelay, String cookie_hook, User user){
		log = new Logger("session");

		session_id = generateID();
		ip = (datarelay.env.containsKey("REMOTE_ADDR") ? 
				datarelay.env.get("REMOTE_ADDR") : "NULL");
		lastAccess = System.currentTimeMillis();
		this.cookie_hook = cookie_hook;
		this.user = user;
		attributes = new HashMap<String, String>(1);
		
		changed = true;
	}

	public String generateID(){
		return Integer.valueOf(new Random(System.nanoTime()).nextInt(1000)).toString() + Long.valueOf(System.currentTimeMillis()).toString().substring(5);
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public HashMap<String,String> getAttributes(){
		return attributes;
	}

	public String getCookie_hook() {
		return cookie_hook;
	}

	public String getId() {
		return session_id;
	}

	public String getIp() {
		return ip;
	}

	public long getLastAccess(){
		return lastAccess;
	}

	public User getUser() {
		return user;
	}

	/** opens the session file to be read by readIp() and readStuff().
	 *  remember to close with close().
	 * 
	 * @param session_id	The session id used to identify the session and as filename
	 * @return				True if opening streams succeeded. False if there was an ioexception.
	 */
	public boolean open(String session_id) {
		File source = new File(Cgicms.sessions_dir, session_id);
		if(source.canRead()){
			Cgicms.log.info("session exists");
			try {
				oin = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream(source)));
				this.session_id = session_id;
				lastAccess = source.lastModified();
				return true;
			} catch (IOException ioe) {
				Cgicms.log.fail(""+ioe);
			}
		}
		Cgicms.log.info("could not open session");
		return false;
	}

	public String readIp() {
		try {
			String temp = (String)oin.readObject();
			Cgicms.log.info("read ip: "+temp);
			ip = temp;
			return temp;
		} catch (ClassNotFoundException cnfe){
			Cgicms.log.fail(""+cnfe);
		} catch (IOException ioe) {
			Cgicms.log.fail(""+ioe);
		}
		Cgicms.log.info("could not read ip");
		return null;
	}

	//@SuppressWarnings("unchecked")
	public void readStuff() {
		try {
			if(oin == null)
				log.info("oin == @null");
			cookie_hook = (String)oin.readObject();
			user = (User)oin.readObject();
			//attributes = (HashMap<String, String>)oin.readObject();

		} catch (ClassNotFoundException cnfe){
			Cgicms.log.fail(""+cnfe);
		} catch (IOException ioe) {
			Cgicms.log.fail(""+ioe);
		}
	}
	
	public boolean close() {
		try {
			oin.close();
			return true;
		} catch (IOException ioe) {
			Cgicms.log.fail(""+ioe);
			Cgicms.log.info("could not close session");
			return false;
		}
	}
	
	public void remove() {
		log.info("removing session: " + session_id);
		File file = new File(Cgicms.sessions_dir , session_id); 
		if(file.delete()){
			//ActionLog.system("deleted "+sId);
			log.info(" -> successfully removed: "+file);
		}else{
			ActionLog.error("failed to delete "+session_id);
			log.info(" -> failed to remove: "+file);
		}
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
		changed = true;
	}

	public void setID(String id) {
		session_id = id;
		changed = true;
	}


	public void setIp(String newip){
		if(newip != null){
			ip = newip;
			changed = true;
		}
	}

	public void setLastAccess(long newTime) {
		File source = new File(Cgicms.sessions_dir, session_id);
		source.setLastModified(newTime);
	}
	
	public void setTemp(boolean temporary) {
		this.temporary = temporary;	
		changed = true;
	}

	public void store() {
		if(temporary || !changed)
			return;

		log.info("storing session: "+ session_id);
		long start = System.currentTimeMillis();
		File session_file = new File(Cgicms.sessions_dir, session_id);
		try {
//			log.info("opening object stream");
			ObjectOutputStream oout = new ObjectOutputStream(new
					BufferedOutputStream(new FileOutputStream(session_file)));

//			log.info("writing ip");
			oout.writeObject(ip);
//			log.info("writing last access");
			//oout.writeLong(lastAccess);
//			log.info("writing all the rest");
			oout.writeObject(cookie_hook);
			oout.writeObject(user);
			oout.writeObject(attributes);
			//oout.writeObject(history);
			oout.close();
			//ActionLog.system("stored session "+sId);
//			log.info("successful write");
			log.info("store took: "+(System.currentTimeMillis()-start)+" ms");
			return;
		}catch (NotSerializableException nse){
			log.fail("could not serialize object:"+nse);
		} catch (IOException ioe) {
			log.fail("error writing session:"+ioe);
		}
		ActionLog.error("store failed "+session_id);
		log.info("store failed");
	}
	public String toString(){
		StringBuilder buff = new StringBuilder();
		buff.append("sessionID: " + session_id + "\n");
		buff.append("lastAccess: " + lastAccess + "\n");
		buff.append("IPAdress: " + ip + "\n");
		buff.append("user: " + user + "\n");
		buff.append("Attributes:\n");
		for(Map.Entry<String, String> entry : attributes.entrySet()){
			buff.append(entry.getKey()+": " + entry.getValue() + "\n");
		}
		buff.append("delete: " + delete + "\n");

		return buff.toString();
	}
}

