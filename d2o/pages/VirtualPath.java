package d2o.pages;

import util.Utils;


public class VirtualPath {

	private String[] blocks;
	private String filename;
	//private int pointer;
	private int low;
	private int high;
	private boolean file;


	VirtualPath(String ext){
		//pointer = 0;
		low = 0;
		//System.err.print("#### vp ext["+ext+"] -> ");
		ext = Utils.urlDecode(ext);
		//System.err.println("["+ext+"]");
		
		if(ext.startsWith("/"))
			ext = ext.substring(1);

		if(ext.startsWith("./"))
			ext = ext.substring(2);
		
		if(ext.length()==0){
			blocks = new String[0];
			high = 0;
			file = false;
			filename = "";
		}else{
			
			file = (ext.charAt(ext.length() - 1) != '/');
			
			if(file){
				int last = ext.lastIndexOf('/');
				if(last != -1){
					filename = ext.substring(last+1);
					//ext = ext.substring(0, last);
				}else{
					filename = ext;
					ext = "";
				}
				blocks = ext.split("/");
				high = blocks.length-1;
			}else{
				filename = "";
				blocks = ext.split("/");
				high = blocks.length;
			}

		}
		//System.err.print("####    path["+getPath()+"] ");
		//System.err.print(" url["+getUrl(true)+"] ");
		//System.err.print(" filename["+filename+"] ");
		//System.err.println("file["+file+"]");
		
	}

	public VirtualPath(VirtualPath vpath) {
		blocks = vpath.blocks;
		filename = vpath.filename;
		low = 0;
		high = blocks.length;
		file = vpath.file;
	}

	public int length(){
		return (file ? high - low + 1 : high - low);
	}

	public VirtualPath down(){
		if(high >= low ){
			high--;
			return this;
		}
		return null;
	}

	public VirtualPath up(){

		if(low < high){
			low++;
			return this;
		}
		return null;

		/*
		if(file){
			if(low + 1 < high){
				low++;
				return true;
			}
		}else{
			if(low + 1 < high - 1){
				low++;
				return true;
			}
		}
		return false;*/

	}

	public static boolean validate(String ext) {
		if(
				ext.contains("..") ||
				ext.contains("//") ||
				ext.contains("\\")  //yes single '\'
		)
			return false;
		return true;
	}

	public static VirtualPath create(String ext) {
		if(validate(ext))
			return new VirtualPath(ext);
		return null;
	}

	public String getUrl(){
		if(blocks.length==0){
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		
		for(int i = low; i < high; i++){
			sb.append(blocks[i]+"/");
		}
		
		if(file)
			sb.append(filename);
		return sb.toString();
	}

	public String getUrl(boolean encode){
		if(blocks.length==0){
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("/");
		
		//System.err.println("#### blocks : l["+low+"] h["+high+"]");
		for(int i = low; i < high; i++){
			//System.err.println("####      b ["+blocks[i]+"]");
			sb.append(
					(
							encode ?
								Utils.urlEncode(blocks[i])
							:
								blocks[i]
					)
					+"/"
			);
		}
		if(file)
			sb.append(filename);
		return sb.toString();
	}

	public String getPath() {
		//System.err.println("##### blocks ["+blocks.length+"]");
		if(blocks.length==0)
			return "";

		StringBuilder sb = new StringBuilder();
		//System.err.println("##### low ["+low+"] high["+high+"]");
		for(int i = low; i < high; i++){
			sb.append(blocks[i]+"/");
			//System.err.println("##### append ["+blocks[i]+"]");
		}
		//System.err.println("##### returns ["+sb.toString()+"]");
		return sb.toString();
	}

	public String getFilename() {
		return filename;/*
		if(blocks.length==0)
			return "";

		return blocks[high-1];*/
	}

	public boolean setFile(String name){
		filename = name;
		if(file){
			return true;
		}
		file = true;
		return false;
	}

	public String getRootName() {
		if(blocks.length > low)
			return blocks[low];
		return "";
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String s: blocks){
			sb.append(s);
		}
		return sb.toString();
	}

	public String getPathTo(VirtualPath to) {
		
		return null;
	}

}
