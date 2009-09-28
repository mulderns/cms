package cms;

import java.util.ArrayList;

//import cms.access.Session;
import cms.mods.*;
import d2o.GroupDb;

import util.Logger;


public class ModuleLoader {
	Logger log;
	DataRelay datarelay;

	ModuleLoader(DataRelay datarelay){
		this.datarelay = datarelay;
		log = new Logger("ModuleLoader");
		log.meta("Init");

		String path_info = datarelay.env.get("PATH_INFO");
		if(path_info != null &&	path_info.length() > 1){
			if(path_info.indexOf("/", 1) == -1){
				datarelay.mod = path_info.substring(1);
			}else{
				int i = path_info.indexOf("/", 1);
				datarelay.mod = path_info.substring(1, i);
				if(path_info.indexOf("/", i+1) == -1){
					datarelay.act = path_info.substring(i+1);
				}else{
					int j = path_info.indexOf("/",i+1);
					datarelay.act = path_info.substring(i+1, j);
					datarelay.ext = path_info.substring(j+1);
				}
			}
			log.info(
					" mod["+(datarelay.mod == null?"null_":datarelay.mod)+"]"+
					" act["+(datarelay.act == null?"null_":datarelay.act)+"]"+
					" ext["+(datarelay.ext == null?"null_":datarelay.ext)+"]"
			);
		}
	}

	public Module getModule(String hook){
		for(Module m : datarelay.modules){
			if(m.getHook().equals(hook))
				return m;
		}
		return null;
	}

	public void execute() {
		log.info("executing");

		if(datarelay.mod.equals("")){
			log.info(" ..default");
			new ModMain(datarelay).execute();
		}else{
			log.info(" ..");

			// old way
			if(datarelay.modules != null){
				for(Module m : datarelay.modules){
					if(m.getHook().equals(datarelay.mod)){
						if(GroupDb.checkAccess(
								datarelay.session.getUser(),
								datarelay.mod,
								datarelay.act)
						){
							m.execute();
							return;
						}else{
							datarelay.pagebuilder.addMessage("oikeutesi eivät riitä tämän toiminnon ajamiseen");
							return;
						}
					}
				}
			}
			datarelay.pagebuilder.addMessage("module not found");
			new ModMain(datarelay).execute();
		}
	}

	public String[] getModuleStuff() {
		if(datarelay.modules == null){
			return new String[0];
		}

		ArrayList<String> tempStuff = new ArrayList<String>();
		String hook;

		load_modules();
		for(Module m : datarelay.modules){
			hook = m.getHook();

			m.activate();
			for(String action : m.getActions()){
				if(action != null){
					tempStuff.add(hook+"/"+action);
				}
			}
		}
		return tempStuff.toArray(new String[tempStuff.size()]);
	}


	public final void load_modules(){
		datarelay.modules = new ArrayList<Module>(12);
		datarelay.modules.add(new ModMain(datarelay));
		datarelay.modules.add(new ModViikko(datarelay));
		datarelay.modules.add(new ModMaintenance(datarelay));
		datarelay.modules.add(new ModOwn(datarelay));
		datarelay.modules.add(new ModUpload(datarelay));
		datarelay.modules.add(new ModPages(datarelay));
		datarelay.modules.add(new ModLogout(datarelay));
		datarelay.modules.add(new ModAccess(datarelay));
		datarelay.modules.add(new ModHallitus(datarelay));
	}
}

