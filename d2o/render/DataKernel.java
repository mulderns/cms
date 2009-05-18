package d2o.render;

import java.util.ArrayList;
import java.util.Collections;

import util.Utils;

public class DataKernel implements Comparable<DataKernel> {
	String id;
	String data;
	ArrayList<DataKernel> subs;

	DataKernel(String id, String data){
		this.id = id;
		this.data = data;
	}

	DataKernel(String id){
		this.id = id;
		data = "";
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id["+(id==null?"@null":id)+"] data["+(data==null?"@null":data)+"] subs["+(subs==null?"@null":subs.size())+"]");
		return sb.toString();
	}
	
	public String toString2(int indent){
		StringBuilder sb = new StringBuilder();
		String s = Utils.genSpace(indent);
		sb.append(s+" id["+(id==null?"@null":id)+"]");
		sb.append(" data["+(data==null?"@null":data+"]"));
		sb.append(" subs["+(subs==null?"@null":subs.size())+"]\n");
		if(subs!=null){
			for(DataKernel m : subs){
				sb.append(s+" {\n"+m.toString2(indent+1)+s+" }\n");
			}
		}
		return sb.toString();
	}
	
	public void toDataArray(ArrayList<String> buffer){

		if(subs==null){
			buffer.add("«"+id+"»"+data+"«:"+id+"»");
		}else{
			buffer.add("«"+id+"»");
			for(DataKernel dk : subs){
				dk.toDataArray(buffer);
			}
			buffer.add("«:"+id+"»");
		}
	}

	public int compareTo(DataKernel o) {
		return id.compareTo(o.id);
	}

	public void add(DataKernel kernel){
		if(subs == null)
			subs = new ArrayList<DataKernel>();
		subs.add(kernel);
	}

	public void sort() {
		if(subs != null){
			Collections.sort(subs);
		}
	}
	
}
