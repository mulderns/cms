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
	/*
	private ArrayList<Module> loadAllModules() {
		// adding order defines the order of menu items 
		ArrayList<Module> modules = new ArrayList<Module>();
		modules.add(new ModMain(datarelay));
		modules.add(new ModViikko(datarelay));
		modules.add(new ModUser(datarelay));
		modules.add(new ModGroup(datarelay));
		modules.add(new ModMaintenance(datarelay));
		modules.add(new ModOwn(datarelay));
		modules.add(new ModLogout(datarelay));

		datarelay.modules = modules;
		log.info("datarelay.modules.size: "+datarelay.modules.size());
		return modules;
	}
	 */
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
			//if(datarelay.modules != null && datarelay.modules.size() > 0 ){
			//datarelay.modules.get(0).execute();
			new ModMain(datarelay).execute();
			//datarelay.pagebuilder.bake();
			//}
		}else{
			log.info(" ..");

			/** new way
			final User user = datarelay.session.getUser();
			for(String m_hook : datarelay.modules){
				if(m_hook.equals(datarelay.mod)){
					if(GroupDb.checkAccess(
							user,
							datarelay.mod,
							datarelay.act)
					){
						load(m_hook).execute();
						//datarelay.pagebuilder.bake();
						return;
					}else{
						datarelay.pagebuilder.addMessage("denied");
						//datarelay.pagebuilder.bake();
						return;
					}
				}
			}*/

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
							//datarelay.pagebuilder.bake();
							return;
						}else{
							datarelay.pagebuilder.addMessage("oikeutesi eivät riitä tämän toiminnon ajamiseen");
							//datarelay.pagebuilder.bake();
							//getModule(ModMain.hook).execute();
							return;
						}
					}
				}
			}

			datarelay.pagebuilder.addMessage("module not found");
			new ModMain(datarelay).execute();
			//if(datarelay.modules != null && datarelay.modules.size() > 0){
			//	datarelay.modules.get(0).execute();
			//}
			//datarelay.pagebuilder.bake();
		}
	}

	/*
	Module load(String hook){
		if(hook.equals(ModMain.hook))
			return new ModMain(datarelay);
		if(hook.equals(ModViikko.hook))
			return new ModViikko(datarelay);
		if(hook.equals(ModUser.hook))
			return new ModUser(datarelay);
		if(hook.equals(ModGroup.hook))
			return new ModGroup(datarelay);
		if(hook.equals(ModMaintenance.hook))
			return new ModMaintenance(datarelay);
		if(hook.equals(ModOwn.hook))
			return new ModOwn(datarelay);
		if(hook.equals(ModUpload.hook))
			return new ModUpload(datarelay);
		if(hook.equals(ModPages.hook))
			return new ModPages(datarelay);
		if(hook.equals(ModLogout.hook))
			return new ModLogout(datarelay);
		return null;
	}*/

	public String[] getModuleStuff() {
		if(datarelay.modules == null){
			return new String[0];
		}

		ArrayList<String> tempStuff = new ArrayList<String>();
		String hook;

		load_modules();
		for(Module m : datarelay.modules){
			hook = m.getHook();
			//tempStuff.add(hook);
			m.activate();
			for(String action : m.getActions()){
				if(action != null){
					//if(!action.equals("")){
					tempStuff.add(hook+"/"+action);
					//}
				}
			}
		}

		return tempStuff.toArray(new String[tempStuff.size()]);
	}

	/*
	public void load_hooks() {
		datarelay.module_hooks = new ArrayList<String>();
		datarelay.module_labels = new ArrayList<String>();

		datarelay.module_hooks.add(ModMain.hook);
		datarelay.module_labels.add(ModMain.menu_label);

		datarelay.module_hooks.add(ModViikko.hook);
		datarelay.module_labels.add(ModViikko.menu_label);

		datarelay.module_hooks.add(ModUser.hook);
		datarelay.module_labels.add(ModUser.menu_label);

		datarelay.module_hooks.add(ModGroup.hook);
		datarelay.module_labels.add(ModGroup.menu_label);

		datarelay.module_hooks.add(ModMaintenance.hook);
		datarelay.module_labels.add(ModMaintenance.menu_label);

		datarelay.module_hooks.add(ModOwn.hook);
		datarelay.module_labels.add(ModOwn.menu_label);

		datarelay.module_hooks.add(ModUpload.hook);
		datarelay.module_labels.add(ModUpload.menu_label);

		datarelay.module_hooks.add(ModPages.hook);
		datarelay.module_labels.add(ModPages.menu_label);

		datarelay.module_hooks.add(ModLogout.hook);
		datarelay.module_labels.add(ModLogout.menu_label);
	}*/

	public final void load_modules(){
		datarelay.modules = new ArrayList<Module>(12);
		datarelay.modules.add(new ModMain(datarelay));
		datarelay.modules.add(new ModViikko(datarelay));
		datarelay.modules.add(new ModUser(datarelay));
		datarelay.modules.add(new ModGroup(datarelay));
		datarelay.modules.add(new ModMaintenance(datarelay));
		datarelay.modules.add(new ModOwn(datarelay));
		datarelay.modules.add(new ModUpload(datarelay));
		datarelay.modules.add(new ModPages(datarelay));
		datarelay.modules.add(new ModLogout(datarelay));
		//datarelay.modules.add(new ModTest(datarelay));
		datarelay.modules.add(new ModAccess(datarelay));
	}
}

