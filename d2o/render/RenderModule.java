package d2o.render;

import java.util.TreeSet;

import d2o.pages.VirtualPath;

import html.CmsElement;

public class RenderModule {
	public String hook;
	public Type type;

	public RenderModule(String hook){
		this.hook = hook;
	}

	void init(){
		type = Type.local;
	}

	enum Type{
		local,
		external
	}
	
	public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid) {}
	public void render(Kernel metakernel, Kernel cloud) {}
	public void substitute(Kernel metakernel, VirtualPath path) {}
	
	protected DataKernel extractData(TreeSet<DataKernel> cloud, String id, String parentid){
		//System.err.println("#### extracting data: id["+id+"]");
		DataKernel kernel = new DataKernel("");
		if(parentid.length() > 0){
			parentid = parentid.substring(0, parentid.length()-1);
		}
		int i = parentid.lastIndexOf('.');
		if(i != -1)
			parentid = parentid.substring(i+1);
		//System.err.println("pid["+parentid+"] ");
		if(parentid.length() > 0){
			for(DataKernel dk : cloud){
				//System.err.print(" ?["+dk.id+"]");
				if(dk.id.equals(parentid) && dk.subs != null){
					//System.err.println(" +");
					//System.err.println("  id["+id+"] ");
					for(DataKernel d2k : dk.subs){
						//System.err.print("   ?"+d2k.id+"");
						if(d2k.id.equals(id)){
							//System.err.println(" +");
							//dk.subs.remove(d2k);
							//System.err.println("");
							return d2k;
						}
						//System.err.println(" -");
					}
				}
				//System.err.println(" -");
			}
		}else{
			for(DataKernel d2k : cloud){
				//System.err.print("   ?"+d2k.id+"");
				if(d2k.id.equals(id)){
					//System.err.println(" +");
					//cloud.remove(d2k);
					//System.err.println("");
					return d2k;
				}
				//System.err.println(" -");
			}
		}
		//System.err.println("");
		return kernel;
	}


}
