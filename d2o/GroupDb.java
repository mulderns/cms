package d2o;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import util.Logger;
import util.TriState;
import cms.Cgicms;
import cms.access.User;

public class GroupDb {
	private final String linesep = System.getProperty("line.separator");

	private File source;
	private FlushingFile dbfile;
	private long lmod;

	private TriState state;
	private static boolean created = false;
	private static GroupDb present;

	Logger log = new Logger("GroupDb");

	ArrayList<GroupRule> groups;

	public static GroupDb getDb(){
		if(created){
			return present;
		}
		created = true;
		return new GroupDb(Cgicms.group_hook);
	}

	private GroupDb(){
		log.info("init");
		source = new File(Cgicms.database_dir,"groups");
		dbfile = new FlushingFile(source);
		groups = new ArrayList<GroupRule>();
		state = new TriState();
		setPresent(this);
	}

	private GroupDb(String group_hook){
		log.info("init");
		source = new File(Cgicms.database_dir,"groups."+group_hook);
		dbfile = new FlushingFile(source);
		groups = new ArrayList<GroupRule>();
		state = new TriState();
		setPresent(this);
	}

	private static void setPresent(GroupDb groupDb) {
		present = groupDb;

	}

	private GroupDb(File source) {
		log.info("init");
		this.source = source;
		dbfile = new FlushingFile(source);
		groups = new ArrayList<GroupRule>();
	}

	public boolean loadDb(){
		log.info("loading db");
		long start = System.nanoTime();
		if(!state.open){
			lmod = source.lastModified();
			//String[] raw = cms.FileHive.getFileHive().readFile2Array(source);
			String[] raw = dbfile.loadAll();
			if(raw == null){
				log.fail("read returned too little (null)");
				return false;
			}
			GroupRule rg = new GroupRule("init");
			log.info("loading proceeds:");
			for(String s : raw){
				if(s.length() > 0){
					if(s.charAt(0) == ':'){
						rg = new GroupRule(s.substring(1));
						groups.add(rg);
						//log.info("new group ["+rg.group_name+"]");
					}else{
						//log.info(" rule");
						rg.addModuleAction(s);
					}
				}else{
					//log.info("0");
				}
			}

			state.touch();
			log.info(" load successfull");
			//log.info( this.toString());
			long stop = System.nanoTime();
			log.info("gdb load ["+(stop - start)+"]ns");
			buildFast();
			log.info("gdb index build ["+(System.nanoTime() - stop)+"]ns");
			//Collections.sort(groups);
			return true;
		}else{
			log.info(" allready loaded");
			return true;
		}
	}

	public boolean storeDb() {
		if(!state.open || lmod != source.lastModified()){
			log.fail("gdb state not open ["+state+"]");
			return false;
		}
		//StringBuilder sb =  new StringBuilder();
		ArrayList<String> buffer = new ArrayList<String>();
		for(GroupRule rule : groups){
			buffer.add(":"+rule.group_name);
			for(ModuleRule a : rule.modules){
				buffer.add(a.name);
				for(String s : a.actions){
					buffer.add(a.name+"/"+s);
				}
			}
		}
		//log.info("Storing:"+linesep+sb.toString());

		String result = dbfile.overwrite(buffer.toArray(new String[0]));
		if(result == null)
			return true;
		/*if(cms.FileHive.getFileHive().storeFile(source, sb.toString())){
			state.touch();
			return true;
		}*/
		return false;
	}

	public boolean addRule(String group_name, String[] stuff){
		if(!state.open){
			return false;
		}
		for (GroupRule rule : groups){
			if(rule.group_name.equals(group_name)){
				return false;
			}
		}
		groups.add(new GroupRule(group_name,stuff));
		return true;
	}

	public boolean changeRule(String group_name, String[] stuff){
		if(!state.open){
			return false;
		}
		for (GroupRule rule : groups){
			if(rule.group_name.equals(group_name)){
				rule.changeStuff(stuff);
				return true;
			}
		}
		return false;
	}

	public String[] getGroupNames() {
		if(!state.open){
			loadDb();
			//return new String[0];
		}
		String[] temp = new String[groups.size()];
		int i = 0;
		for (GroupRule rule : groups){
			temp[i++] = rule.group_name;
		}
		return temp;
	}

	public String[] getGroupNames(boolean sort) {
//		ArrayList<String> groups = new ArrayList<String>(Arrays.asList(getGroupNames()));
//		Collections.sort(groups, new Comparator<String>(){
//			public int compare(String o1, String o2) {
//				return o1.compareTo(o2);
//			}			
//		});
//		return groups.toArray(new String[0]);
		ArrayList<String> groups = new ArrayList<String>(Arrays.asList(getGroupNames()));
		Collections.sort(groups);
		return groups.toArray(new String[0]);
	}
	
	public String toString(){
		StringBuilder sb =  new StringBuilder();
		for(GroupRule rule : groups){
			sb.append(":" + rule.group_name + linesep);
			for(ModuleRule a : rule.modules){
				sb.append(a.name+linesep);
				if(a.actions == null){
					sb.append(" null"+linesep);
				}else{
					for(String s : a.actions){
						sb.append(a.name+"/"+s+linesep);
					}
				}
			}
		}
		return sb.toString();
	}

	public boolean groupHasAccess(String groupname, String modulename) {
		for(GroupRule rule : groups){
			if(rule.group_name.equals(groupname)){
				for(ModuleRule a : rule.modules){
					if(a.name.equals(modulename)){
						return true;
					}
				}
			}
		}
		return false;
	}

	HashMap<String, HashMap<String, HashSet<String>>> fastdb;

	public void buildFast(){
		fastdb = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, HashSet<String>> temp_action;// = new HashMap<String, HashSet<String>>();
		HashSet<String> temp_group;// = new HashSet<String>();

		for(GroupRule rule : groups){
			//log.info("r");
			for(ModuleRule module : rule.modules){
				//log.info(" m");
				if(fastdb.containsKey(module.name)){
					//log.info("  e");

					if(fastdb.get(module.name).containsKey("")){
						fastdb.get(module.name).get("").add(rule.group_name);
						//log.info("  ["+module.name+"] [] ["+rule.group_name+"]");
					}else{
						temp_group = new HashSet<String>();
						temp_group.add(rule.group_name);
						fastdb.get(module.name).put("", temp_group);
						//log.info("  ["+module.name+"] [] ["+rule.group_name+"]");
					}
				}else{
					//log.info("  n");
					temp_group = new HashSet<String>();
					temp_group.add(rule.group_name);
					temp_action = new HashMap<String, HashSet<String>>();
					temp_action.put("", temp_group);
					fastdb.put(module.name, temp_action);
					//log.info("  ["+module.name+"] ["+temp_action+"] ["+rule.group_name+"]");
				}
				for(String action : module.actions){
					//	log.info("   a");
					if(fastdb.get(module.name).containsKey(action)){
						//log.info("    e");
						fastdb.get(module.name).get(action).add(rule.group_name);
						//log.info("  ["+module.name+"] ["+action+"] ["+rule.group_name+"]");
					}else{
						//log.info("    n");
						temp_group = new HashSet<String>();
						temp_group.add(rule.group_name);
						fastdb.get(module.name).put(action, temp_group);
						//log.info("  ["+module.name+"] ["+action+"] ["+rule.group_name+"]");
					}
				}
			}
		}
	}

	public boolean fastCanAccess(String group, String module, String action){
		//System.err.print("\n ########## g["+group+"] m["+module+"] a["+action+"]");
		//long start = System.nanoTime();
		HashMap<String, HashSet<String>> slow;
		if((slow = fastdb.get(module)) == null) {
			//System.err.print("\n ##########   no such module ["+module+"]");
			//System.err.println((System.nanoTime()-start));
			return false;
		}
		//System.err.print("\n ##########  1");
		HashSet<String> dead;
		if((dead = slow.get(action)) == null){
			//System.err.println((System.nanoTime()-start));
			return false;
		}
		//System.err.print("\n ##########   group");
		if(dead.contains(group)){
			//System.err.println((System.nanoTime()-start));
			return true;
		}
		//System.err.print("\n ##########    fail");
		//System.err.println((System.nanoTime()-start));
		return false;
	}

	public String fastDebug(){
		StringBuilder sb = new StringBuilder();

		for(String mod : fastdb.keySet()){
			for(String act : fastdb.get(mod).keySet()){
				for(String gro : fastdb.get(mod).get(act)){
					sb.append(mod + "/" + act + " - " + gro + linesep);
				}
			}
		}
		return sb.toString();
	}

	public ArrayList<String> getAccess(String group) {
		ArrayList<String> access = new ArrayList<String>();
		for(GroupRule rule : groups){
			if(rule.group_name.equals(group)){
				for(ModuleRule mod: rule.modules){
					access.add(mod.name);
					for(String action : mod.actions){
						access.add(mod+"/"+action);
					}
				}
				return access;
			}
		}
		/*if(Collections.binarySearch(groups, new RuleGroup(group)) >= 0){
			ArrayList<String> access = new ArrayList<String>();
			access.add(e)
		}*/
		return access;
	}

	public boolean addGroup(String name) {
		for(GroupRule group : groups){
			if(group.group_name.equals(name)){
				return false;
			}
		}
		groups.add(new GroupRule(name));
		return true;
	}

	public boolean addGroupModule(String name, String[] modules) {
		for(GroupRule group : groups){
			if(group.group_name.equals(name)){
				for(String raw:modules){
					group.addModuleAction(raw);
				}
				return true;
			}
		}
		return false;
	}

	public boolean push(String group, String mod, String act) {
		log.info("pushing");
		for(GroupRule rule : groups){
			if(rule.group_name.equals(group)){
				for(ModuleRule module : rule.modules){
					if(module.name.equals(mod)){
						for(String action : module.actions){
							if(action.equals(act)){
								return true;
							}
						}
					}
				}
				rule.addModuleAction(mod+"/"+act);
				return true;
			}
		}
		GroupRule temp = new GroupRule(group);
		temp.addModuleAction(mod+"/"+act);
		groups.add(temp);
		return true;
	}

	public boolean reset(String group) {
		log.info("resetting rules on ["+group+"]");
		//ArrayList<GroupRule> temp = new ArrayList<GroupRule>();
		//boolean found;
		
		for(GroupRule rule : groups){
			if(rule.group_name.equals(group)){
				groups.remove(rule);
				return true;
			}
		}
		
		return false;
		
	}
	
	public void reset() {
		log.info("resetting db");
		ArrayList<GroupRule> temp = new ArrayList<GroupRule>();
		boolean found;
		for(GroupRule rule : groups){
			found = false;
			for(GroupRule r : temp){
				if(r.group_name.equals(rule.group_name)){
					log.info(" - allready preserved");
					found = true;
					continue;	
				}
			}
			if(!found){
				log.info(" + preserving group label");
				temp.add(new GroupRule(rule.group_name));
			}
		}
		groups = temp;
		log.info(" preserved ["+groups.size()+"]");
	}

	public boolean groupExists(String s) {
		if(!state.open)
			loadDb();
		for(GroupRule group : groups){
			if(group.group_name.equals(s))
				return true;
		}

		return false;
	}

	public static boolean checkAccess(User user, String mod, String act) {
		//log.info("access check.. ug["+user.getGroups().get(0)+"] m["+mod+"] a["+act+"]");
		ArrayList<String> groups = user.getGroups();
		//System.err.println(Arrays.toString(groups.toArray(new String[0])));
		Collections.sort(groups);
		if(Collections.binarySearch(groups, "root") >= 0){
			//log.info("root");
			return true;
		}
		GroupDb gdb = GroupDb.getDb();

		if(!gdb.state.open){
			gdb.loadDb();
		}
		if(gdb.fastCanAccess("everyone", mod, act)){
			//log.info("everyone");
			return true;
		}
		for(String s : groups){
			if( gdb.fastCanAccess(s, mod, act)){
				//log.info("group["+s+"]");
				return true;
			}
		}
		//log.info("denied");
		return false;

	}
}

class GroupRule{
	String group_name;
	ArrayList<ModuleRule> modules;

	public GroupRule(String group_name, String[] stuff) {
		this.group_name = group_name;
		modules = new ArrayList<ModuleRule>();

		ModuleRule workk = new ModuleRule("init");
		for(String s : stuff){
			if(s.contains("/")){
				workk.addAction(s);
			}else{
				workk = new ModuleRule(s);
				modules.add(workk);
			}
		}
	}

	public GroupRule(String group_name) {
		this.group_name = group_name;
		modules = new ArrayList<ModuleRule>();
	}

	public void changeStuff(String[] stuff) {
		modules = new ArrayList<ModuleRule>();
		ModuleRule workk = new ModuleRule("init");

		for(String s : stuff){
			if(s.contains("/")){
				workk.addAction(s);
			}else{
				workk = new ModuleRule(s);
				modules.add(workk);
			}
		}		
	}

	public void addModuleAction(String s) {
		if(s.contains("/")){
			String act = s.substring(s.indexOf('/')+1);
			String mod = s.substring(0,s.indexOf('/'));
			//log.info("  module["+mod+"] action["+act+"]");
			for(ModuleRule module : modules){
				if(module.name.equals(mod)){
					module.addAction(act);
					//log.info("   found");
					return;
				}
			}
			ModuleRule temp = new ModuleRule(mod);
			temp.addAction(act);
			modules.add(temp);
		}else{
			//log.info("  new module ["+s+"]");
			modules.add(new ModuleRule(s));
		}
	}
}

class ModuleRule{
	String name;
	ArrayList<String> actions;

	public ModuleRule(String module_name) {
		this.name = module_name;
		actions = new ArrayList<String>();
	}

	public void addAction(String s) {
		actions.add(s);
	}
}
