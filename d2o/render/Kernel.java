package d2o.render;

import java.util.ArrayList;

import util.Utils;

public class Kernel {
	String id;
	private ArrayList<Kernel> subs;
	Kernel parent;
	Type type;

	public Kernel(){
		id = "";
		type = Type.code;
	}

	public Kernel(Kernel clone){
		this.id = clone.id;
		this.parent = clone.parent;
		this.type = clone.type;

		if(clone.subs != null){
			subs = new ArrayList<Kernel>(clone.subs.size());
			for(Kernel k: clone.subs){
				Kernel temp = new Kernel(k);
				temp.parent = this;
				subs.add(temp);
			}
		}
	}

	public Kernel(String input, Type type){
		this.type = type;
		id = input;
	}

	public Kernel(Kernel parent, Type type){
		id = "";
		this.parent = parent;
		this.type = type;
	}

	public Kernel(String input, Kernel parent, Type type){
		id = input;
		this.parent = parent;
		this.type = type;
	}

	enum Type{
		data,
		meta,
		code,
		file
	}

	public void add(Kernel kernel) {
		if(subs==null)
			subs = new ArrayList<Kernel>();
		subs.add(kernel);		
	}
	
	public void add(ArrayList<Kernel> children) {
		if(subs==null)
			subs = new ArrayList<Kernel>();
		subs.addAll(children);		
	}

	public void addAll(Kernel data) {
		if(data.subs == null)
			return;
		if(subs == null){
			subs = data.subs;
			return;
		}
		subs.addAll(data.subs);
	}

	public void addFile(Kernel kernel) {
		if(subs==null)
			subs = new ArrayList<Kernel>();
		subs.add(0, kernel);
	}

	public Kernel getData(String dataid) {
		if(subs == null)
			return null;
		for(Kernel k : subs){
			if(k.id.equals(dataid))
				return k;
		}
		return null;
	}

	public ArrayList<Kernel> getCode() {

		if(subs == null)
			return new ArrayList<Kernel>(0);

		ArrayList<Kernel> temp = new ArrayList<Kernel>(subs.size());
		for(Kernel k : subs){
			if (k.type.equals(Kernel.Type.code)){
				temp.add(k);
			}
		}

		return temp;
	}

	public String getParentName() {
		if(subs == null){
			return null;
		}
		Kernel file = subs.get(0);
		if(file.type != null && file.type.equals(Type.file))
			return file.id;
		return null;
	}

	public ArrayList<Kernel> getSubs() {
		if(subs == null)
			return new ArrayList<Kernel>(0);
		return subs;
	}

	public Kernel getFirst() {
		if(subs!=null && subs.size()>0){
			return subs.get(0);
		}
		//return null;
		return new Kernel();
	}

	public ArrayList<Kernel> asList() {
		if(subs == null)
			return new ArrayList<Kernel>(0);
		ArrayList<Kernel> list = new ArrayList<Kernel>();
		for(Kernel k: subs ){
			if(!k.type.equals(Kernel.Type.file)){
				list.add(k);
				if(!k.type.equals(Kernel.Type.meta)){
					list.addAll(k.asList());
				}
			}
		}
		return list;		
	}

	public String toHtml(){
		return toHtml(0);
	}

	public String toHtml(int indent) {
		StringBuilder sb = new StringBuilder();
	
		if(subs == null)
			return "";
	
		//final String space = Utils.genSpace(indent);
		for(Kernel k : subs){
			//sb.append(space + "<!-- ["+k.type.toString()+"] "+(k.subs==null?"@":k.subs.size())+" -->\n");
			sb.append(k.id);
			//sb.append("\n");
			sb.append(k.toHtml(indent+2));
		}
	
		return sb.toString();
	}

	public String toString(){
		return toString(0);
	}

	public String toString(int indent){
		StringBuilder sb = new StringBuilder();
		final String space = Utils.genSpace(indent);
		sb.append(space);
		sb.append("["+(type==null?"@":type)+"] ");
		if(!type.equals(Type.code)){
			sb.append("id["+(id==null?"@":id)+"] ");
		}else{
			sb.append("id["+(id==null?"@":Utils.removeBrakes(id))+"] ");
		}
		if(parent==null)
			sb.append("@ ");
		if(subs != null)
			sb.append("subs["+subs.size()+"]");
		sb.append("\n");
		if(subs!=null && subs.size()>0){
			indent += 2;
	
			for(Kernel k : subs){
				if(k != null)
					sb.append(k.toString(indent));
			}
		}
		return sb.toString();
	}
}
