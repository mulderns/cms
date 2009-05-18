package d2o.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import util.Logger;

public class Tree {
	private static final Logger log = new Logger("Tree");
	public static final Tree parseTree(final String line){
		Tree tree = new Tree("root");
		char ati;
		int alku = 0;
		final int length = line.length();

		for(int i = 0; i < length; i++){
			ati = line.charAt(i);
			if(ati == '('){
				tree.addChild(line.substring(alku, i));
				tree = tree.getLast();
				alku = i+1;
			}else if(ati == ')'){
				if(alku <= i-1){
					tree.addChild(line.substring(alku, i));
				}
				if(!tree.hasParent())
					return null;
				tree = tree.getParent();
				alku = i+1;

			}else if(ati == ','){
				if(alku <= i-1){
					tree.addChild(line.substring(alku, i));
				}
				alku = i+1;
			}
			if(i + 1 == length){
				if(alku <= i){
					tree.addChild(line.substring(alku));
				}
			}
		}
		return tree.getRoot();
	}
	public String name;
	ArrayList<Tree> subs;

	Tree parent;

	public Tree(String name){
		this.name = name;
	}

	public Tree(String name, Tree parent){
		this.name = name;
		this.parent = parent;
	}

	public Tree(Tree child, Tree parent) {
		this.name = child.name;
		this.subs = child.subs;
		this.parent = parent;
	}

	public boolean addChild(String name){
		if(subs == null){
			subs = new ArrayList<Tree>();
			subs.add(new Tree(name,this));
			return true;
		}
		for(Tree t : subs){
			if( t.name.equals(name))
				return false;
		}
		subs.add(new Tree(name,this));
		return true;
	}

	public boolean addChildNode(Tree child){
		if(subs == null){
			subs = new ArrayList<Tree>();
			subs.add(new Tree(child,this));
			return true;
		}
		for(Tree t : subs){
			if( t.name.equals(name))
				return false;
		}
		subs.add(new Tree(child,this));
		return true;
	}

	public ArrayList<String> asList(ArrayList<String> target) {
		target.add(name);
		if(subs != null){
			for(Tree child:subs){
				child.asList(target);
			}
		}
		return target;		
	}

	public boolean deepRemove(String name2) {
		//System.err.print("#### dr ### ["+name2+"] from ["+this.name+"]");
		if(this.name.contentEquals(name2)){
			//System.err.print(" !");
		}
		if(hasChild(name2)){
			//System.err.print(" found");
			if(removeChild(name2)){
				//System.err.println(" success");
				return true;
			}
			//System.err.println(" error");
			return false;
		}else{
			//System.err.print(" not found");
		}
		
		for(String cname : list()){
				if(getChild(cname).deepRemove(name2))
					return true;
		}
		return false;		
	}

	public Tree getChild(String name){
		if(subs == null)
			return null;
		for(Tree s: subs){
			if(s.name.equals(name))
				return s;
		}
		return null;
	}

	public ArrayList<String> getChildArray() {
		ArrayList<String> temp = new ArrayList<String>(5);
		temp.add(name);
		if(subs == null)
			return temp;
		for(Tree t: subs)
			temp.addAll(t.getChildArray());
		return temp;
	}

	public Tree getLast(){
		if(subs.size()==0){
			return null;
		}
		return subs.get(subs.size()-1);
	}

	public Tree getNode(String name) {
		if(subs == null){
			return null;
		}
		for(Tree node: subs){
			if(node.name.contentEquals(name)){
				return node;
			}else{
				Tree node2 = node.getNode(name);
				if(node2 != null)
					return node2;
			}
		}
		return null;
	}

/*
	public boolean pull(Path path) {
		//log.info(" %["+name+"] -["+path.length+"]");
		if(subs == null){
			log.info("  ¤subs");
			log.severe("pull hit a childless node");
			return false;
		}
		if(path.length() > 0){
			for(Tree s: subs){
				if(s.name.equals(path.getRoot().toString())){

				}
			}
		}
		return false;*/
		/*
		if(path.length > start+1){
			log.info("  >");
			for(Tree s: subs){
				if(s.name.equals(path[start])){ 
					log.info("   ["+path[start]+"] found");
					return s.pull(path, start+1);
				}
			}
			log.info("   ["+path[start]+"] not found");
			return false;
		}else{
			log.info("  -");
			if(removeChild(path[start])){
				log.info("success fully removed child node["+path[start]+"]");
				return true;
			}
			log.severe("could not remove child node["+path[start]+"]");
			return false; 
		}*/
	//}

	public Tree getParent(){
		//log.info(" /"+parent);
		return parent;
	}

	public Tree getRoot(){
		Tree temp_parent = this;
		for(int i = 0; i < 300; i++){ //after 300 assume in infinite loop :P
			if(temp_parent.hasParent()){
				temp_parent = temp_parent.getParent();
			}else{
				return temp_parent;
			}
		}
		return null;
	}

	public Tree getTree(VirtualPath path) {
		//log.info(" <["+name+"] <["+path+"]");		
		if(path.length() > 1){
			//log.info("b");
			if(subs == null)
				return null;
			String node = path.getPath();
			for(Tree s: subs){
				if(s.name.contentEquals(node)){
					return s.getChild(path.up().getPath());
				}
			}
			return null;
		}else{
			//log.info("a");
			//log.info(" name["+path.getPath()+"]");
			if(path.getPath().equals("")){ //TODO: not going to work
				return this;
			}
			return getChild(path.getPath());
		}
	}

	public boolean hasChild(String string) {
		if(subs == null){
			//System.err.print(" subs==null ");
			return false;
		}
		for(Tree s: subs){
			//System.err.print(".["+s.name+"]");
			if(s.name.contentEquals(string)){
				//System.err.print(" %");
				return true;
			}else{
				//System.err.print("-");
			}
		}
		//System.err.print(" \\");
		return false;

	}


	public boolean hasChildren() {
		return (subs != null);
	}

	public boolean hasParent() {
		return (parent != null);
	}

	
	public String[] list() {
		if(subs == null)
			return new String[0];
		//return subs.toArray(new String[subs.size()]);
		String[] children = new String[subs.size()];
		int i = 0;
		for(Tree tree : subs)
			children[i++] = tree.name;
		return children;
	}

	public boolean pull(String[] path, int start) {
		log.info(" %["+name+"] -["+path.length+"]");
		if(subs == null){
			//log.info("  ¤subs");
			//log.severe("pull hit a childless node");
			return false;
		}
		if(path.length > start+1){
			//log.info("  >");
			for(Tree s: subs){
				if(s.name.equals(path[start])){ 
					log.info("   ["+path[start]+"] found");
					return s.pull(path, start+1);
				}
			}
			//log.info("   ["+path[start]+"] not found");
			return false;
		}else{
			//log.info("  -");
			if(removeChild(path[start])){
				//log.info("success fully removed child node["+path[start]+"]");
				return true;
			}
			log.fail("could not remove child node["+path[start]+"]");
			return false; 
		}
	}

	public boolean push(String[] path, int start){
		//log.info(" >>["+name+"] -["+path.length+"]");
		if(subs == null){
			//log.info("  @subs");
			subs = new ArrayList<Tree>();
		}
		if(path.length > start+1){
			//log.info("  >");
			for(Tree s: subs){
				if(s.name.equals(path[start])){ 
					//log.info("   ["+path[start]+"] found");
					return s.push(path, start+1);
				}
			}
			//log.info("   ["+path[start]+"] not found");
			return false;
		}else{
			//log.info("  +");
			addChild(path[start]);
			return true; 
		}
	}

	/*public String toString(){
		StringBuilder sb = new StringBuilder();
		Tree temp = this;
		for(int i = 0; i < 100; i++){
			sb.append(temp.name);
			if(temp.subs != null){
				sb.append("(");
				temp = temp.getFirst();
			}
		}
		return sb.toString();
	}*/	

	/*
	public String toString(){
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		System.out.println("s["+name+"]-"+subs.size());
		for(Tree t : subs){
			sb.append(","+t.name);
		}
		sb.append(linesep);
		for(Tree t : subs){
			sb.append(t.toString(name));
		}
		return sb.toString();
	}
	public String toString(String parent){
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(parent+"/"+name);
		System.out.println("p["+parent+"] s["+name+"]-"+subs.size());
		System.out.println(name+"-"+subs.size());
		for(Tree t : subs){
			sb.append(","+t.name);
		}
		sb.append(linesep);
		for(Tree t : subs){
			sb.append(t.toString(parent+"/"+name));
		}
		return sb.toString();
	}*/

	public boolean push(String[] path, String name){
		if(subs == null)
			subs = new ArrayList<Tree>();
		//System.out.println(name+"-pushing["+path[0]+"]+"+path.length);
		if(path.length > 1){
			for(Tree s: subs){
				if(s.name.equals(path[1])){
					return s.push(Arrays.copyOfRange(path, 1, path.length), name);
				}
			}
			//Tree temp = new Tree(path[1]);
			//subs.add(temp);
			//return temp.push(Arrays.copyOfRange(path, 1, path.length), name);
			return false;
		}else{
			addChild(name);
			return true; 
		}
	}

	public boolean removeChild(String name){
		if(subs == null){
			//System.err.println("#### rc ## subs == null");
			return false;
		}

		int i = 0;
		for(Tree s: subs){
			if(s.name.equals(name)){
				subs.remove(i);
				//System.err.println("#### rc ## success");
				return true;
			}
			//System.err.println("#### rc ## ["+s.name+"] != ["+name+"] ");
			i++;
		}
		//System.err.println("#### rc ## fail");
		return false;
	}

	/**

<li>base</li>
<ul>
<li>index</li>
<li>semi parsing</li><ul><li>full parsing</li></ul>
<li>naama</li>
</ul>

	 */
/*
	public String rootToHtml(String path){ // 0

		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\""+path+"\">root</a>\n");
		String link2;
		if(subs != null){
			sb.append("<ul>");
			for(Iterator<Tree> i = subs.iterator(); i.hasNext();){
				Tree t = i.next();
				link2 = path+"/"+t.name;
				sb.append("<a href=\""+link2+"/\">"+t.name+"</a>\n");
				if(t.hasChildren()){
					sb.append("<ul>"+t.toHtml(link2)+"</ul>\n");
				}
			}
			sb.append("</ul>");
		}

		return sb.toString();
	}

	public String toHtml(String link){ // self
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();

		String link2;
		for(Iterator<Tree> i = subs.iterator(); i.hasNext();){
			Tree t = i.next();
			link2 = link+"/"+t.name;
			sb.append("<a href=\""+link2+"/\">"+t.name+"</a>\n");
			if(t.hasChildren()){
				sb.append("<ul>"+t.toHtml(link2)+"</ul>\n");
			}
		}

		return sb.toString();
	}

	
	public String toHtml2(String link){ //0
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();

		//String link2;
		for(Iterator<Tree> i = subs.iterator(); i.hasNext();){
			Tree t = i.next();
			//link2 = link+"/"+t.name;
			sb.append("<li><a href=\""+link+"/"+t.name+"?type=template\">"+t.name+"</a></li>");
			if(t.hasChildren()){
				sb.append("<ul>"+t.toHtml(link)+"</ul>");
			}
		}

		return sb.toString();
	}

	public String toTempHtml2(String link){ // pagedb.getTempHtml
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();

		//String link2;
		for(Iterator<Tree> i = subs.iterator(); i.hasNext();){
			Tree t = i.next();
			//link2 = link+"/"+t.name;
			sb.append("<li><a href=\""+link+"/"+t.name+"?type=template\">"+t.name+"</a></li>");
			if(t.hasChildren()){
				sb.append("<ul>"+t.toHtml(link)+"</ul>");
			}
		}

		return sb.toString();
	}*/
	
	public String toString(){
		if(subs == null)
			return "";
		StringBuilder sb = new StringBuilder();

		//sb.append(name);
		//System.out.println("s["+name+"]-"+subs.size());

		for(Iterator<Tree> i = subs.iterator(); i.hasNext();){
			Tree t = i.next();
			sb.append(t.name);
			if(t.hasChildren()){
				sb.append("("+t.toString()+")");
			}else if(i.hasNext()){
				sb.append(",");
			}
		}
		/*for(Tree t : subs){
			sb.append(t.name);
			if(t.hasChildren()){
				sb.append("("+t.toString()+")");
			}else{
				sb.append(",");
			}
		}*/
		return sb.toString();
	}
}
