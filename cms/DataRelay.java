package cms;

import html.CmsPage;
import http.FormPart;

import java.util.ArrayList;
import java.util.HashMap;

import util.Logger;
import cms.access.Session;
import cms.mods.Module;

public class DataRelay {
	public HashMap<String, String> env;
	public HashMap<String, String> query;
	public HashMap<String, String> post;
	public HashMap<String, String> cookie;

	public boolean multipart;
	public FormPart[] files;
	public ArrayList<Module> modules;
	public CmsPage page;
	
	public String username;
	public String mod="",act="",ext="";
	public String script;
	public String target;
	public String res;

	public int week_fix;
	
	public PageBuilder pagebuilder;
	public Session session;
	public Logger log = new Logger("datarelay");
	public ModuleLoader loader;
}
