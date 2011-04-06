package d2o.pages;

import java.io.File;
import java.util.ArrayList;

import util.Csv;
import d2o.FlushingFile;

public class CmsFile implements Comparable<CmsFile>{

	public Type type;
	public String name;
	public String parent;
	public String content_type;
		
	public boolean modified;
	public long lastModified;
	
	public VirtualPath relativePath;

	public IndexRecord record;

	protected String metafilename;
	protected String datafilename;	

	public FlushingFile datasource;
		
	protected String[] extra_meta;
	
	public CmsFile(String name){
		type = Type.TEXT;
		this.name = name;
		content_type = "text/plain";
		metafilename = "page.meta."+name;
		datafilename = "page.data."+name;
	}

	public CmsFile(CmsFile oldfile) {
		type = oldfile.type;
		name = oldfile.name;
		parent = oldfile.parent;
		content_type = oldfile.content_type;
		
		modified = oldfile.modified;
		lastModified = oldfile.lastModified;
		
		relativePath = oldfile.relativePath;
		
		metafilename = oldfile.metafilename;
		datafilename = oldfile.datafilename;

		datasource = oldfile.datasource;
		
	}

	public String[] getMeta() {
		String[] metas = {
				type.toString(),
				(parent==null?"none":parent),
				content_type
		};
		
		ArrayList<String> metalines = new ArrayList<String>();
		metalines.add(util.Csv.encode(metas));
		for(String extra : getExtraMeta()){
			metalines.add(extra);
		}		
		return metalines.toArray(new String[metalines.size()]);
	}

	protected String[] getExtraMeta(){
		if(extra_meta != null)
			return extra_meta;
		return new String[0];
	}
	
	public String getData() {
		return "generic data"; 
	}
	
	public int compareTo(CmsFile arg0) {
		return name.compareTo(arg0.name);
	}

	public enum Type {
		TEXT,
		BINARY,
		DUMMY,
		TEMP		
	}

	public static boolean storeMeta(CmsFile file, File dir) {
		return file.writeMeta(dir);
	}
	
	public static boolean storeData(CmsFile file, File dir) {
		return file.writeData(dir);
	}

	public boolean loadMeta(File source_dir) {
		FlushingFile metasource = new FlushingFile(new File(source_dir, metafilename));
		if(!parseMeta(metasource.loadAll())){
			return false;
		}
		datasource = new FlushingFile(new File(source_dir, datafilename));
		return true;
	}
	
	public boolean loadMeta2(File source_dir) {
		FlushingFile metasource = new FlushingFile(new File(source_dir, metafilename));
		if(!parseMeta(metasource.loadAll())){
			return false;
		}
		datasource = new FlushingFile(new File(source_dir, datafilename));
		return true;
	}

	private boolean parseMeta(String[] lines) {
		//type,parent,content_type
		//extra
		
		//BINARY,none,text/html
		
		//System.err.println("#### [CmsFile] - parsing meta...");
		if(lines == null || lines.length < 1)
			return false;
		
		String[] pieces = Csv.decode(lines[0]);
		if(pieces.length > 0){
			try{
				type = Type.valueOf(pieces[0]);
			}catch(IllegalArgumentException iae){
				type = Type.DUMMY;
			}
		}
		if(pieces.length > 1)
			parent = (pieces[1].equals("none")?null:pieces[1]);
		if(pieces.length > 2){
			content_type = pieces[2];
		}
		
		if(lines.length >1){
			extra_meta = new String[lines.length-1];
			for(int i = 1; i< lines.length; i++){
				extra_meta[i-1] = lines[i];
			}
		}

		return true;
	}
	
	
	protected boolean writeMeta(File dir) {
		FlushingFile metaflush = new FlushingFile(new File(dir, metafilename));
		String result = metaflush.overwrite(getMeta());
		if(result == null)
			return true;
		return false;	
	}

	
	protected boolean writeData(File dir) {
		FlushingFile dataflush = new FlushingFile(new File(dir, datafilename));
		String result =  dataflush.overwrite(getData());
		//System.err.println("#### [CmsFile] - writeData: "+result);
		
		if(result == null)
			return true;
		return false;
	}

	public boolean delete(File dir){
		FlushingFile dataflush = new FlushingFile(new File(dir, datafilename));
		String result = dataflush.delete();
		if(result == null){
			FlushingFile metaflush = new FlushingFile(new File(dir, metafilename));
			result = metaflush.delete();
		}
		
		if(result == null)
			return true;

		return false;
	}

	public boolean rename(String uusinimi, VirtualPath path, File sdir) {
		name = uusinimi;
		modified = true;

		File dir = new File(sdir, path.getPath());
		
		File old_meta = new File(dir,metafilename);
		File old_data = new File(dir,datafilename);

		File metafile = new File(dir,"page.meta."+uusinimi);
		File datafile = new File(dir,"page.data."+uusinimi);

		if(old_meta.renameTo(metafile) && old_data.renameTo(datafile)){
			return true;
		}
		return false;
	}

	public static CmsFile loadFile(VirtualPath path, File sdir) {
		CmsFile temp = new CmsFile(path.getFilename());
		//System.err.println("#### [CmsFile] - loadFile: ");
		//System.err.println("####           - meta:"+temp.metafilename);
		if(!temp.loadMeta(new File(sdir, path.getPath()))){
			//System.err.println("####           + [null]");
			return null;
		}
		
		temp.relativePath = path;
		
		switch (temp.type) {
		case TEXT:
			//System.err.println("####           + [TextFile]");
			return new TextFile(temp);
			
		case BINARY:
			//System.err.println("####           + [BinaryFile]");
			return new BinaryFile(temp);

		default :
			//System.err.println("####           + [DummyFile]");
		return new DummyFile(temp);
		}
	}
}
