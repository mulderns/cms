package d2o.render;

import java.util.ArrayList;

import util.Utils;

public class MetaKernel {
	//String label;
	String header;
	String[] parts;
	String id;
	ArrayList<MetaKernel> subs;

	public MetaKernel(){

	}

	public MetaKernel(String header) {
		this.header = header;
		parts = header.split(":");
		if(parts.length > 1){
			id = parts[1];
		}
	}

	public String getId() {
		return id;
	}

	public void add(MetaKernel kernel) {
		if(subs == null)
			subs = new ArrayList<MetaKernel>();
		subs.add(kernel);

	}

	public boolean hasChildren() {
		return subs == null;
	}

	public String getType() {
		if(header == null){
			return "dynamic";
		}else if(id == null){
			return "garbage";
		}else{
			return parts[0];
		}
	}

	public String getLabel() {
		if(header==null){
			return id;
		}
		return id;
	}

	public String toString(){
		return toString(0);
	}

	public String toString(int indent){
		StringBuilder sb = new StringBuilder();
		String s = Utils.genSpace(indent);
		sb.append(s+" id["+(id==null?"@null":id)+"]");
		sb.append(" type["+(getType()==null?"@null":getType())+"]");
		sb.append(" header["+(header==null?"@null":header)+"]\n");
		sb.append(s+"  parts["+(parts==null?"@null":parts.length)+"]");
		if(parts!=null){
			for(String p : parts){
				sb.append(" {"+p+"}");
			}
		}
		sb.append("\n");
		sb.append(s+"  subs["+(subs==null?"@null":subs.size())+"]\n");
		if(subs!=null){
			for(MetaKernel m : subs){
				sb.append(s+" {\n"+m.toString(indent+1)+s+" }\n");
			}
		}
		return sb.toString();
	}

}
