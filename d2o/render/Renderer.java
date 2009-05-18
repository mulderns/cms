package d2o.render;

import html.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import d2o.pages.CmsFile;
import d2o.pages.PageDb;
import d2o.pages.TemplateFile;
import d2o.pages.TextFile;
import d2o.pages.VirtualPath;

import util.Logger;

import util.Utils;

public class Renderer {
	private static boolean created = false;
	private static Renderer present;

	PageDb pdb;
	private static Logger log;

	HashMap<String, RenderModule> modules;
	HashMap<String, RenderModule> locals;

	//private boolean preview;
	String url;

	public static Renderer getRenderer() {
		if(!created){
			created = true;
			present = new Renderer();
		}
		return present;
	}

	private Renderer(){
		pdb = PageDb.getDb();
		log = new Logger("Renderer");
		initModules();
	}

	private void initModules(){
		locals = new HashMap<String, RenderModule>();

		RenderModule module;

		module = new RenderModule("a"){
			void init(){type = Type.local;}
			public void substitute(Kernel metakernel, VirtualPath from) {
				metakernel.type = Kernel.Type.code;

				//String original = metakernel.id;
				//VirtualPath to = VirtualPath.create(original);
				//metakernel.id = "<!-- anchor "+metakernel.id+"-->";// from.getPathTo(to);
				//String local_url = null;
				String[] parts = metakernel.id.split(":");
				if(url != null){
					if (parts[1].startsWith("./")){
						parts[1] = parts[1].substring(2);
					}
				}

				log.info("a - > "+(url==null?"@":url));
				log.info("m - > "+parts[1]);
				metakernel.id = (url==null?"":url)+parts[1];

				//metakernel.id = "http://localhost:8080/cgi-bin/cms-2/Cgicms.exe/sivut/preview"+metakernel.id.split(":")[1];
			}
		};
		locals.put(module.hook, module);

		module = new RenderModule("ssi"){ //TODO: convert to using generateHtml(PageFile file)
			void init(){type = Type.local;}

			public void substitute(Kernel metakernel, VirtualPath from) {
				metakernel.type = Kernel.Type.code;
				PageDb pdb = PageDb.getDb();

				String[] parts = metakernel.id.split(":");
				String target = parts[1];

				if(url!=null){
					TextFile file = (TextFile)pdb.getFileMeta(VirtualPath.create(target));
					if(file != null){
						Kernel structure = getFile(file);
						log.info("apuva : "+structure.toString());
						structure = infuse(structure, new Kernel(), from);
						metakernel.id = "";
						metakernel.add(new Kernel(structure.toHtml(),Kernel.Type.code));
						log.info("apuva : "+structure.toHtml());
					}else{
						metakernel.id = "";
						metakernel.add(new Kernel("<!-- file not found: "+target+" -->",Kernel.Type.code));
					}

					/*
				if(file == null){
					log.fail("ssi fetch file null");
					metakernel.id = "<!-- ssi error ["+VirtualPath.create(target).getUrl()+"] -->";
				}else{
					metakernel.id = "";
					for(String s: file.getData()){
						metakernel.add(new Kernel(s,Kernel.Type.code));
					}
				}*/
				}else{
					metakernel.id = "";
					metakernel.add(new Kernel("<!--#include virtual=\""+target+"\" -->",Kernel.Type.code));

				}
				metakernel.type = Kernel.Type.code;
			}
		};
		locals.put(module.hook, module);


		modules = new HashMap<String, RenderModule>();

		module = new RenderModule("line"){
			void init(){type = Type.external;}

			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");

				brew.addContent(m.getLabel()+":");
				brew.addField(parentid+m.id, Utils.deNormalize(extractData(cloud, m.id, parentid).data), true, new TextField());
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				String[] parts = metakernel.id.split(":");
				String id = parts[1];
				Kernel data = cloud.getData(id);
				if(data != null){
					log.info("data found");
					metakernel.id = "";
					metakernel.add(data.getSubs());
					//metakernel.id = data.getFirst().id; //"";// data.id;
					//log.info(">"+metakernel.id+"<");

					//metakernel.addAll(data);
				}else{
					log.info("could not get data from cloud");
					metakernel.id = "<!-- null line -->";
				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);

		module = new RenderModule("area"){
			void init(){type = Type.external;}
			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");

				int height = 200;

				//				String[] elements = new String[0];
				if(m.parts != null && m.parts.length > 2){
					height = Integer.parseInt(m.parts[2]);
				}

				brew.addContent(m.getLabel()+":");
				brew.addField(parentid+m.id, extractData(cloud, m.id, parentid).data, true, new TextAreaField(height));
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				String[] parts = metakernel.id.split(":");
				String id = parts[1];
				Kernel data = cloud.getData(id);
				if(data != null){
					//log.info("data found");
					metakernel.id = "";// data.id;
					metakernel.addAll(data);
				}else{
					log.info("could not get data from cloud");
					id = "<!-- null area -->";
				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);

		module = new RenderModule("parsing_area"){
			void init(){type = Type.external;}
			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");
				int height = 200;

				if(m.parts != null && m.parts.length > 2){
					height = Integer.parseInt(m.parts[2]);
				}

				brew.addContent(m.getLabel()+":");
				brew.addField(parentid+m.id, extractData(cloud, m.id, parentid).data, true, new TextAreaField(height));
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				String[] parts = metakernel.id.split(":");
				String id = parts[1];
				Kernel data = cloud.getData(id);


				if(data != null){
					//log.info("data found");
					metakernel.id = "";// data.id;
					data.id = data.id.replace("\n", "<br/>");
					for(Kernel k: data.asList()){

						k.id = k.id.replace("\n", "<br/>");

					}

					metakernel.addAll(data);
				}else{
					log.info("could not get data from cloud");
					id = "<!-- null area -->";
				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);

		module = new RenderModule("dynamic"){
			void init(){type = Type.external;}

			private TreeSet<DataKernel> extractDynData(TreeSet<DataKernel> cloud, String id, String parentid){
				log.info("extracting dyndata... id["+id+"] pid["+parentid+"]");

				//first get to the right stage
				ArrayList<DataKernel> buffer = new ArrayList<DataKernel>(cloud);

				int i = 0;
				int j = 0;
				i = parentid.indexOf('.');
				String curid;
				while(i != -1){
					curid = parentid.substring(j,i);
					for(DataKernel dk : buffer){
						if(dk.id.startsWith((curid))){
							if(dk.subs == null)
								dk.subs = new ArrayList<DataKernel>();
							buffer = dk.subs;
							continue;
						}
					}
					j = i+1;
					i = parentid.indexOf('.',j);
				}
				TreeSet<DataKernel> kernels = new TreeSet<DataKernel>();
				for(DataKernel dk : buffer){
					if(dk.id.startsWith((id+"-"))){
						kernels.add(dk);
						continue;
					}
				}
				buffer.removeAll(kernels);
				return kernels;
			}

			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");

				TreeSet<DataKernel> datas = extractDynData(cloud, m.id, parentid);

				if(datas != null){
					for(DataKernel data : datas){
						brew.addLayer("div","ingroup");
						brew.addSingle("input type=\"hidden\" name=\""+parentid+data.id+"\" value=\"\" style=\"display:none\"");
						brew.addTag("h3","<a class=\"add\" title=\"poista\" href=\"?del="+parentid+data.id+"\">X</a><a class=\"add\" title=\"shift up\" href=\"?up="+parentid+data.id+"\">&#94;</a>"+data.id);

						for(MetaKernel subm: m.subs){
							if(modules.containsKey(subm.getType())){
								modules.get(subm.getType()).synthesize(subm, datas, brew, parentid+data.id+".");
							}else{
								log.fail(" invalid key/type/module ["+subm.getType()+"]");
							}
						}
						brew.up();
					}
				}
				brew.addLink(null, "ingroup", "?add="+parentid+m.getLabel(), "[+] "+m.id);
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				log.info("rendering dynamic ["+metakernel.id+"]");
				Kernel proto = new Kernel(metakernel);

				ArrayList<Kernel> datas = new ArrayList<Kernel>();

				for(Kernel k : cloud.getSubs()){
					if(k.id.startsWith(metakernel.id)){
						//log.info(" data["+k.id+"]");
						datas.add(k);
					}
				}
				metakernel.getSubs().clear();
				metakernel.id = "";

				//log.info("proto : " +proto.debug(0));				

				log.info("processing");
				for(Kernel k : datas){
					//log.info("["+k.id+"]");
					for(Kernel protosub:proto.getSubs()){
						if(protosub.type.equals(Kernel.Type.code)){
							//log.info(" [code] "+ protosub.id);

							metakernel.add(new Kernel(protosub.id,Kernel.Type.code));

						}else if(protosub.type.equals(Kernel.Type.meta)){
							//log.info(" [meta] ["+protosub.id+"]");
							RenderModule m;
							if(protosub.id.indexOf(':') == -1){
								m = modules.get("dynamic");
							}else{
								m = modules.get(protosub.id.split(":")[0]);
							}
							if(m!= null){
								Kernel temp = new Kernel(protosub);
								m.render(temp, k);
								metakernel.add(temp);
							}
						}
					}

				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);

		module = new RenderModule("drop"){
			void init(){type = Type.external;}

			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");

				String[] elements = new String[0];
				if(m.parts != null && m.parts.length > 2){
					elements = m.parts[2].split(",");
				}
				brew.addContent(m.getLabel()+":");
				String value = extractData(cloud, m.id, parentid).data;
				brew.addField(parentid+m.id, value, true, new ComboBoxField(elements, value));
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				String[] parts = metakernel.id.split(":");
				String id = parts[1];
				Kernel data = cloud.getData(id);
				if(data != null){
					//log.info("data found");
					metakernel.id = "";// data.id;
					metakernel.addAll(data);
				}else{
					log.info("could not get data from cloud");
					id = "<!-- null drop -->";
				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);

		module = new RenderModule("hidden"){
			void init(){type = Type.external;}

			@Override
			public void synthesize(MetaKernel m, TreeSet<DataKernel> cloud, CmsElement brew, String parentid){
				log.info("synthesizing ["+hook+"] id["+m.id+"]");

				brew.addContent("<!-- " +m.getLabel()+ "-->");
				//brew.addField(parentid+m.id, extractData(cloud, m.id, parentid).data, true, new TextField());
			}

			@Override
			public void render(Kernel metakernel, Kernel cloud){
				log.info("render hidden");
				String[] parts = metakernel.id.split(":");
				String id = parts[1];
				Kernel data = cloud.getData(id);
				if(data != null){
					log.info("data found");
					metakernel.id = data.getFirst().id; //"";// data.id;
					//log.info(">"+metakernel.id+"<");
					//metakernel.addAll(data);
				}else{
					log.info("could not get data from cloud");
					metakernel.id = "<!-- hidden null line -->";
				}
				metakernel.type = Kernel.Type.code;
			}
		};
		modules.put(module.hook, module);
	}


	public String[] getFields(CmsFile file){
		if(file.parent == null){
			String[] fields = new String[1];
			fields[0] = "data";
			return fields;
		}else{
			return pdb.getTemplate(file.parent).getFields();
		}
	}

	public CmsElement generateEditPage(CmsFile file){
		log.info("gen edit");

		/** needs 
		 * - the file to be edited
		 * - maybe html elements
		 * - access to parent templates
		 */


		CmsElement edit = new CmsElement();
		edit.addLayer("div","ingroup filled");

		if(file.parent == null){ 	//source edit
			log.info("source edit");

			log.info("reading data [");
			StringBuilder data = new StringBuilder();
			data.append(file.getData());

			log.info("            ]");

			edit.addField("data", data.toString(), true, new TextAreaField(500));

		}else{
			ArrayList<DataKernel> cloud = mineData(file.getData());
			if(cloud != null){
				log.info("mined data");
				int i = 0;
				for(DataKernel data: cloud){
					log.info((i++)+": " + data.toString());
				}
			}
			log.info("getting parent["+file.name+"] -> ["+file.parent+"]");
			TemplateFile parent = pdb.getTemplate(file.parent);
			if(parent == null){
				log.fail("parent template could not be found ["+file.parent+"]");
				edit.addTag("pre","parent template could not be found ["+file.parent+"]");
				edit.addLayer("pre");

				edit.addContent(file.getData());
				edit.up();

				return edit;
			}
			ArrayList<MetaKernel> storm = mineMeta(parent.getData());
			//fill parent with data and produce elements
			edit = brew(storm, cloud);
		}

		return edit;
	}


	public String generateHtml(TextFile file) {
		log.info("Generating html...");

		if(file == null){
			log.fail("file is null");
			return null;
		}

		VirtualPath path = (file.relativePath==null?VirtualPath.create(""):file.relativePath);
		String parent;

		// gut == abstract sectionize
		// infuse == synthetize meta tags to code using cloud data;

		// gut the file
		log.info("1st get gutted file");
		Kernel structure = getFile(file);
		// infuse the file -> cloud
		log.info("gutts: "+structure.toString());

		log.info("1st infuse");
		Kernel cloud = infuse(structure, new Kernel(), path);
		log.info("fusion: "+cloud.toString());
		// if file has parent
		parent = structure.getParentName();

		while(parent != null && !parent.equals("null")){
			log.info("gut lap");
			// gut the parent
			structure = getCachedTemplate(parent);
			log.info("gutts: "+structure.toString());
			// infuse cloud and parent -> new cloud
			cloud = infuse(structure, cloud, path);
			log.info("fusion: "+cloud.toString());
			// if parent has parent
			parent = structure.getParentName();
		}

		StringBuilder sb = new StringBuilder();

		if(url!=null){
			if(parent!=null){
				sb.append("Content-Type: text/html; charset=iso-8859-1;\n\n");
			}else{
				sb.append("Content-Type: "+file.content_type+"; charset=iso-8859-1;\n\n");
			}
			log.info("content type: ["+file.content_type+"] ->"+sb.toString());
		}

		//final String linesep = System.getProperty("line.separator");
		/*for(Kernel kernel : cloud.subs){
			sb.append(kernel.toString());
			sb.append(linesep);
		}*/

		if(cloud != null){
			sb.append(cloud.toHtml());
		}else{
			sb.append("<html><body><pre>null</pre></body></html>");
			log.fail("cloud == @null");
		}
		return sb.toString();

	}


	private Kernel infuse(Kernel structure, Kernel cloud, VirtualPath path) {
		log.info("Infusing...");
		long start = System.nanoTime();

		if(structure == null){
			log.info("no structure");
			return new Kernel();
		}
		if(cloud == null){
			log.info("cloud == @");
			cloud = new Kernel();
		}

		Kernel mesosphere = new Kernel();

		//ArrayList<Kernel> hello = structure.asList();
		//log.info("as list:"+hello.size()+"");

		for(Kernel k : structure.getSubs()){
			if(k.type.equals(Kernel.Type.code)){
				log.info("add [code]");
				mesosphere.add(k);
			}else if(k.type.equals(Kernel.Type.data)){
				log.info("add [data] "+k.id);
				mesosphere.add(k);
				for(Kernel l : k.getSubs()){

					if(l.type.equals(Kernel.Type.meta)){
						log.info("  [fuse] "+k.id);
						metaFuse(l,cloud,path);
					}
				}
			}else if(k.type.equals(Kernel.Type.meta)){
				log.info("add [meta] "+k.id);
				mesosphere.add(metaFuse(k,cloud,path));
			} 
		}

		//log.info("path:"+path.getUrl());
		//log.info("cloud:"+cloud.toString());
		//log.info("structure:"+structure.toString());
		//log.info("mesosphere:"+mesosphere.toString());

		log.info("infuse took: " + Utils.nanoTimeToString((System.nanoTime() - start)));
		return mesosphere;
	}


	private Kernel metaFuse(Kernel metakernel, Kernel cloud, VirtualPath path) {
		log.info("Metafuse...");

		String[] parts = metakernel.id.split(":");
		String type = "";
		if(parts.length == 1){
			type = "dynamic";
		}else if (parts.length > 1){
			type = parts[0];
		}

		log.info("type ["+type+"]");
		log.info(metakernel.id);
		if(modules.containsKey(type)){
			//log.info(" module found");
			modules.get(type).render(metakernel, cloud);
		}else{
			if(locals.containsKey(type)){
				locals.get(type).substitute(metakernel, path);
				//log.info(" module found in locals");
			}else{
				log.fail(" invalid key/type/module ["+type+"]");
			}
		}
		return metakernel;
	}


	private CmsElement brew(ArrayList<MetaKernel> storm, ArrayList<DataKernel> _cloud) {
		log.info("storm is brewing... s["+(storm==null?"null":storm.size())+"] c["+(_cloud==null?"null":_cloud.size())+"]");
		TreeSet<DataKernel> cloud;
		if(_cloud != null){
			cloud = new TreeSet<DataKernel>(_cloud);
		}else{
			cloud = new TreeSet<DataKernel>();
		}

		CmsElement brew = new CmsElement();
		brew.addLayer("div","ingroup filled");

		if(storm != null){
			for(MetaKernel m: storm){
				log.info("["+m.getType()+"]");
				if(modules.containsKey(m.getType())){
					log.info(" module found");
					modules.get(m.getType()).synthesize(m, cloud, brew, "");
				}else{
					log.fail(" invalid key/type/module ["+m.getType()+"]");
				}
			}
		}else{
			log.info("storm == null");
		}
		return brew;
	}


	private Kernel getFile(TextFile file) {
		//Kernel data = gut(file.bin);
		if(file != null){
			BufferedReader bin = file.datasource.initRead();
			if(file.parent!=null){
				if(!file.parent.equals("null")){
					Kernel data = dataGut2(bin);
					file.datasource.endRead(bin);
					while(data.parent != null)
						data = data.parent;
					data.addFile(new Kernel((file.parent==null?"null":file.parent),Kernel.Type.file));
					return data;
				}
			}
			//Kernel data = straight(file.bin);
			Kernel data = dataGut2(bin);
			file.datasource.endRead(bin);
			return data;
		}
		return null;
	}

	private HashMap<String, Kernel> templateCache; 
	private Kernel getCachedTemplate(String filename){
		if(templateCache == null){
			templateCache = new HashMap<String, Kernel>();
		}

		Kernel cached = templateCache.get(filename);
		if(cached == null){

			TemplateFile temp = pdb.getTemplateMeta(filename);
			if(temp == null){
				return null;
			}


			//cached = gut(temp.bin);
			BufferedReader bin = temp.datasource.initRead();
			cached = dataGut2(bin);
			temp.datasource.endRead(bin);

			while(cached.parent != null)
				cached = cached.parent;

			cached.addFile(new Kernel((temp.parent==null?"null":temp.parent),Kernel.Type.file));
			templateCache.put(temp.name, cached);
		}

		return cached;

	}

	/*
	private Kernel gut(BufferedReader reader) {
		long start = System.nanoTime();

		char c;
		int i;
		Kernel open = new Kernel();

		StringBuilder buffer = new StringBuilder();

		try{
			while((i = reader.read()) != -1){
				//log.info("[gt]               ["+c+"]");
				c = (char)i;
				switch (c) {

				case '{':
					log.info("[gt] {");
					if(buffer.length()>0){
						log.info("[gt] +code -> [dg]");
						open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
						buffer.setLength(0);
					}
					open = dataGut(open, reader);
					log.info("^");
					break;

				case '}':
					log.info("[gt] malform - } ");
					buffer.setLength(0);
					break;

				case '[':
					log.info("[gt] [");
					if(buffer.length() > 0){
						log.info("[gt] +code -> [mg]");
						open.subs.add(new Kernel(buffer.toString(),open, Kernel.Type.code));
						buffer.setLength(0);
					}
					open = metaGut(open, reader);
					log.info("^");
					break;

				case ']':
					log.info("[gt] malform - ] ");
					buffer.setLength(0);
					break;
				default:
					buffer.append(c);
				}
			}

			log.info("[gt] end of stream");
			if(buffer.length() > 0){
				log.info("[gt] +code");
				open.subs.add(new Kernel(buffer.toString(),open, Kernel.Type.code));
				buffer.setLength(0);
			}
		}catch (IOException ioe) {
			log.fail("IOException occured while gutting: "+ioe);
		}

		log.info("gut took " + Utils.nanoTimeToString((System.nanoTime() - start)));
		return open;
	}*/


	/*HashMap<String, TemplateFile> templateCache;
	private CmsFile getCachedTemplate(String filename) {
		if(templateCache == null){
			templateCache = new HashMap<String, TemplateFile>();
		}

		TemplateFile cached = templateCache.get(filename);
		if(cached == null){
			cached = pdb.getTemplate(filename);
			templateCache.put(cached.name, cached);
		}

		return cached;
	}*/

	/*
	private Kernel dataGut(Kernel _open, BufferedReader reader) throws IOException{
		char c;
		int i;
		StringBuilder buffer = new StringBuilder();

		boolean openbrace = true;
		boolean doublepoint = false;

		Kernel open = new Kernel();

		while((i = reader.read()) != -1){
			c = (char)i;
			switch (c) {

			case '{':
				log.info("[dg] {");
				if(!openbrace){
					openbrace = true;
					if(buffer.length()>0){
						log.info("[dg] +code");
						//log.info("["+buffer.toString()+"]");

						open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.code));

						//open.data = buffer.toString();
						buffer.setLength(0);
					}
					//dataGut(open, reader);
				}else{
					log.fail("[dg] malform x!p");
					buffer.setLength(0);

				}
				break;

			case '}':
				log.info("[dg] }");
				if(openbrace){
					openbrace = false;
					if(doublepoint){
						doublepoint = false;
						final String id = buffer.toString();
						buffer.setLength(0);
						if(open.id.equals(id)){
							log.info("[dg] $["+id+"]");
							open = open.parent;
							if(open.parent == null){
								log.info("[dg] <");
								_open.subs.add(open.subs.get(0));
								return _open;
							}
						}else{
							log.fail("[dg] malform o!l - o["+open.id+"] l["+id+"]");
							buffer.setLength(0);
						}
					}else{
						log.info("[dg] +dkernel ["+buffer.toString()+"]");
						Kernel temp = new Kernel(buffer.toString(), open, Kernel.Type.data);
						open.subs.add(temp);
						open = temp;
						buffer.setLength(0);
					}
					//open.subs.add(new Kernel(buffer.toString(), open, Kernel.Type.data));
					//buffer.setLength(0);
				}else{
					log.fail("[dg] malform }!o");
					buffer.setLength(0);
				}
				break;

			case ':':
				if(openbrace){
					log.info("[dg] :");
					doublepoint = true;
					if(buffer.length() == 0){

					}else{
						buffer.append(c);
					}
				}
				break;

			case '[':
				log.info("[dg] [");
				if(buffer.length() > 0){
					log.info("[dg] +code");
					open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
					buffer.setLength(0);
				}
				log.info("[dg] > [mg]");
				open = metaGut(open, reader);
				break;

			case ']':
				log.info("[dg] malform - ] ");
				buffer.setLength(0);
				break;
			default:
				buffer.append(c);
			}

		}
		log.info("end of stream");
		return open;
	}*/


	private Kernel metaGut(Kernel open, BufferedReader reader) throws IOException{
		char c;
		int i;
		StringBuilder buffer = new StringBuilder();

		boolean openbrace = true;
		boolean doublepoint = false;
		boolean end = false;

		while((i = reader.read()) != -1){
			c = (char)i;
			switch (c) {

			case '[':
				log.info("[mg] [");
				if(!openbrace){
					openbrace = true;
					if(buffer.length()>0){
						log.info("[mg] >data");
						//open.data = buffer.toString();
						open.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
						buffer.setLength(0);
					}
				}else{
					log.fail("[mg] malform x!p");
				}
				break;

			case ']':
				log.info("[mg] ]");
				if(openbrace){
					openbrace = false;
					if(doublepoint){
						doublepoint = false;
						if(end){ // end to dynamic
							end = false;
							final String id = buffer.toString();
							buffer.setLength(0);
							if(open.id.equals(id)){
								log.info("[mg] $");
								open = open.parent;
								if(!open.type.equals(Kernel.Type.meta)){
									log.info("[mg] <");
									return open;
								}
							}else{
								log.fail("[mg] malform o!l - o["+open.id+"] l["+id+"]");
							}
						}else{ // normal tag
							log.info("[mg] +normal ");
							//open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.meta));
							open.add(new Kernel(buffer.toString(),open,Kernel.Type.meta));
							if(!open.type.equals(Kernel.Type.meta))
								return open;
							buffer.setLength(0);
						}
					}else{ // dynamic
						log.info("[mg] +dynamic");
						Kernel temp = new Kernel(buffer.toString(), open, Kernel.Type.meta);
						//open.subs.add(temp);
						open.add(temp);
						open = temp;
						buffer.setLength(0);
					}
				}else{
					log.fail("[mg] malform }!o");
				}
				break;

			case ':':
				log.info("[mg] :");
				if(openbrace){
					doublepoint = true;
					if(buffer.length() == 0){
						log.info("[mg] =$");
						end = true;
					}else{
						buffer.append(c);
					}
				}
				break;

			default:
				buffer.append(c);
			}

		}
		log.info("end of stream");
		return open;
	}


	public static ArrayList<MetaKernel> mineMeta(String data) {
		long start = System.nanoTime();
		if(log == null){
			log = new Logger("Renderer");
		}
		log.info("Mining meta...");
		if(data == null)
			return new ArrayList<MetaKernel>();

		ArrayList<MetaKernel> storm = new ArrayList<MetaKernel>();

		Stack<MetaKernel> parents = new Stack<MetaKernel>();

		MetaKernel kernel = new MetaKernel("~");

		int prev = 0;
		int alku = 0;
		String intag = "";
		for(String line : data.split("\n")){
			//log.info(line);
			prev = 0;
			while((alku = line.indexOf('[', prev)) != -1){
				//log.info("  [");
				//log.info(Utils.genSpace(alku)+"^");
				if((prev = line.indexOf(']', alku)) != -1){
					//log.info("  ]");
					//log.info(Utils.genSpace(prev)+"^");

					intag = line.substring(alku+1, prev);
					//log.info("  >"+intag+"<");

					switch (intag.indexOf(':')) {

					case -1: //dynaaminen
						//log.info("  d");
						kernel = new MetaKernel();
						kernel.id = intag;
						parents.add(kernel);
						break;

					case 0:	//lopputagi (dynaamiselle)
						//log.info("  /d");
						if(!parents.empty()){
							kernel = parents.pop();
							if(parents.empty()){
								storm.add(kernel);
							}else{
								parents.peek().add(kernel);
							}
						}else{
							log.fail("malform - empty parent");
							log.fail(line);
							log.fail(Utils.genSpace(prev)+"^");
							return null;
						}
						break;

					default : //normi tagit
						//log.info("  n");
						kernel = new MetaKernel(intag);
					if(!parents.empty()){
						//log.info("  -> ["+parents.peek().id+"]");
						parents.peek().add(kernel);
					}else{
						//log.info("  -> root");
						storm.add(kernel);
					}
					}
				}else{
					log.fail("malform");
					log.fail(line);
					log.fail(Utils.genSpace(prev)+"^");
					return null;
				}
			}
		}
		HashSet<String> done = new HashSet<String>(storm.size());
		ArrayList<MetaKernel> clean = new ArrayList<MetaKernel>(storm.size());
		for(MetaKernel k : storm ){
			if(!done.contains(k.id)){
				clean.add(k);
				done.add(k.id);
			}
		}

		log.info("mineMeta took " + Utils.nanoTimeToString((System.nanoTime() - start)));
		return clean;//storm;
	}

	/*
	static ArrayList<MetaKernel> mineMeta2(String[] data) {
		long start = System.nanoTime();
		if(log == null){
			log = new Logger("Renderer");
		}
		log.info("Mining meta2...");
		if(data == null)
			return new ArrayList<MetaKernel>();

		ArrayList<MetaKernel> storm = new ArrayList<MetaKernel>();

		boolean openbrace = false;
		//boolean opencontent = false;
		boolean doublepoint = false;
		boolean end = false;

		Stack<MetaKernel> open = new Stack<MetaKernel>();

		int alku = 0;

		MetaKernel kernel = new MetaKernel("~"); 

		StringBuilder buffer = new StringBuilder();
		for(int l = 0; l < data.length; l++){

			log.info(data[l]);

			for(int i = 0; i < data[l].length(); i++){
				switch (data[l].charAt(i)) {
				case '[':
					if(!openbrace){
						openbrace = true;
						//if(opencontent){
						//	opencontent = false;
						if(buffer.length()>0){ // contents (non meta)
							MetaKernel mk = new MetaKernel();
							mk.id = null;
							mk.header = buffer.toString() + data[l].substring(alku, i);
							log.info("~");
							if(open.empty()){
								storm.add(mk);
								log.info("+|");
							}else{
								open.peek().add(mk);
								log.info("+<");
							}
							buffer = new StringBuilder();
						}else{ // contents (non meta)
							MetaKernel mk = new MetaKernel();
							mk.id = null;
							mk.header = data[l].substring(alku, i);
							log.info("~");
							if(open.empty()){
								storm.add(mk);
								log.info("+|");
							}else{
								open.peek().add(mk);
								log.info("+<");
							}
						}
						//cloud.add(kernel);
						alku = i+1;
						log.info(Utils.genSpace(i)+">");
						//}else{
						//	alku = i+1;
						//	log.info(Utils.genSpace(i)+"/");
						//}
					}else{
						log.fail("malform x!p");
						log.fail(data[l]);
						log.fail(Utils.genSpace(i)+"^");
						return null;
					}
					break;

				case ']':
					if(openbrace){
						openbrace = false;

						if(doublepoint){ // non dynamic or end to dynamic
							doublepoint = false;
							log.info(Utils.genSpace(i)+"\\");
							if(end){  // end to dynamic
								end = false;
								log.info("$");

								if(!open.empty() && open.peek().id.equals(data[l].substring(alku, i))){
									MetaKernel temp = open.pop();
									if(open.empty()){
										storm.add(temp);
										log.info("+|");
									}else{
										open.peek().add(temp);
										log.info("+<");
									}
									alku = i+1;
								}else{
									log.fail("malform o!l - o["+open.peek()+"] l["+data[l].substring(alku, i)+"]");
									log.fail(data[l]);
									log.fail(Utils.genSpace(i)+"^");
									return null;
								}

							}else{ // non dynamic
								log.info("¤ "+data[l].substring(alku, i));
								MetaKernel temp = new MetaKernel(data[l].substring(alku, i));
								if(open.empty()){
									storm.add(temp);
									log.info("+|");
								}else{
									open.peek().add(temp);
									log.info("+<");
								}
								alku = i+1;									

							}

						}else{ //dynamic

							//if(!opencontent){
							//	opencontent = true;
							log.info(Utils.genSpace(i)+"(");
							kernel = new MetaKernel(data[l].substring(alku, i));
							open.push(kernel);
							alku = i+1;
							//}else{
							//	log.fail("malform c!o - o["+open.peek()+"] l["+data[l].substring(alku, i)+"]");
							//	log.fail(data[l]);
							//	log.fail(Utils.genSpace(i)+"^");
							//	return null;
							//}
						}

					}else{
						log.fail("malform e!o");
						log.fail(data[l]);
						log.fail(Utils.genSpace(i)+"^");
						return null;
					}
					break;
				case ':':
					if(openbrace){
						doublepoint = true;
						if(alku == i){
							alku++;
							end = true;
						}
					}
					break;

				default:
					break;
				}


			}

			if(data[l].length()-1 > alku){
				log.info("\\");
				buffer.append(data[l].substring(alku));
				buffer.append("\n");
			}
			alku = 0;

		}

		log.info("mineMeta2 took " + Utils.nanoTimeToString((System.nanoTime() - start)));
		return storm;
	}*/


	public static ArrayList<DataKernel> mineData(String data){
		long start = System.nanoTime();
		log.info("Mining data...");
		ArrayList<DataKernel> cloud = new ArrayList<DataKernel>();

		boolean openbrace = false;
		boolean opencontent = false;
		boolean doublepoint = false;

		Stack<DataKernel> open = new Stack<DataKernel>();

		int alku = 0;

		DataKernel kernel = new DataKernel("~"); 

		StringBuilder buffer = new StringBuilder();

		for(int i = 0; i < data.length(); i++){
			switch (data.charAt(i)) {
			case '«':
				if(!openbrace){
					openbrace = true;
					if(opencontent){
						opencontent = false;
						if(buffer.length()>0){
							open.peek().data = buffer.toString() + data.substring(alku, i);
							buffer = new StringBuilder();
						}else{
							open.peek().data = /*buffer.toString() +*/ data.substring(alku, i);
						}
						//cloud.add(kernel);
						alku = i+1;
						//log.info(Utils.genSpace(i)+")");
					}else{
						alku = i+1;
						//log.info(Utils.genSpace(i)+"/");
					}
				}else{
					log.fail("malform x!p");
					log.fail(data);
					log.fail(Utils.genSpace(i)+"^");
					return null;
				}
				break;

			case '»':
				if(openbrace){
					openbrace = false;
					if(doublepoint){
						doublepoint = false;
						//log.info(Utils.genSpace(i)+"\\");
						if(open.peek().id.equals(data.substring(alku, i))){
							DataKernel temp = open.pop();
							if(open.empty()){
								cloud.add(temp);
							}else{
								open.peek().add(temp);
							}
							alku = i+1;
						}else{
							log.fail("malform o!l - o["+open.peek()+"] l["+data.substring(alku, i)+"]");
							log.fail(data);
							log.fail(Utils.genSpace(i)+"^");
							return null;
						}

					}else{
						if(!opencontent){
							opencontent = true;
							//log.info(Utils.genSpace(i)+"(");
							kernel = new DataKernel(data.substring(alku, i));
							open.push(kernel);
							alku = i+1;
						}else{
							log.fail("malform c!o - o["+open.peek()+"] l["+data.substring(alku, i)+"]");
							log.fail(data);
							log.fail(Utils.genSpace(i)+"^");
							return null;
						}
					}

				}else{
					log.fail("malform e!o");
					log.fail(data);
					log.fail(Utils.genSpace(i)+"^");
					return null;
				}
				break;
			case ':':
				if(openbrace){
					doublepoint = true;
					if(alku == i){
						alku++;
					}else{
						log.fail("malform :!1st");
						log.fail(data);
						log.fail(Utils.genSpace(i)+"^");
						return null;
					}
				}
				break;

			default:
				break;
			}
		}

		log.info("mineData took " + Utils.nanoTimeToString((System.nanoTime() - start)));
		return cloud;
	}


	public String[] genData(HashMap<String, String> post, CmsFile file) {
		log.info("Generating data...");
		if(file.parent == null){
			log.info(" plain source");
			String[] temp = new String[1];
			temp[0] = post.get("data");
			return temp;
		}else{
			log.info(" has parent");
			ArrayList<DataKernel> buffer2 = new ArrayList<DataKernel>();

			log.info("processing post entries");
			HashMap<String, String> post_clean = new HashMap<String, String>(post);
			post_clean.remove("_lastmodified");
			post_clean.remove("_save");
			post_clean.remove("_preview");

			for(Entry<String,String> entry: post_clean.entrySet()){
				log.info("entry ["+entry.getKey()+"] ["+entry.getValue()+"]");

				ArrayList<DataKernel> buffer3 = buffer2;
				int k = 0;
				int j = entry.getKey().indexOf(".", k);
				String key = null; 

				while(j != -1){
					key = entry.getKey().substring(k, j);
					boolean found = false;
					for(DataKernel dk : buffer3){
						if(dk.id.equals(key)){
							found = true;
							if(dk.subs == null){
								dk.subs = new ArrayList<DataKernel>();
							}
							buffer3 = dk.subs;
							break;
						}
					}

					if(!found){
						DataKernel temp = new DataKernel(key);
						buffer3.add(temp);
						temp.subs = new ArrayList<DataKernel>();
						buffer3 = temp.subs;
						k = j+1;
						j = entry.getKey().indexOf(".", k);
						while(j != -1){
							key = entry.getKey().substring(k, j);
							temp = new DataKernel(key);
							temp.subs = new ArrayList<DataKernel>();
							buffer3.add(temp);
							buffer3 = temp.subs;
							k = j+1;
							j = entry.getKey().indexOf(".", k);
						}
						break;
					}

					k = j+1;
					j = entry.getKey().indexOf(".", k);
				}

				if(key == null){
					key = entry.getKey();
				}else{
					key = (k != 0 ? entry.getKey().substring(k) : entry.getKey());
				}

				boolean found = false;
				for(DataKernel dk : buffer3){
					if(dk.id.equals(key)){
						found = true;
						break;
					}
				}
				if(!found){
					buffer3.add(new DataKernel(key, entry.getValue().replace("»", "&#187;").replace("«", "&#171")));
				}
			}

			//log.info("sort and extract string data");
			ArrayList<String> buffer = new ArrayList<String>();
			Collections.sort(buffer2);

			for(DataKernel dk : buffer2){
				//log.info("> "+dk.toString2(0));
				dk.sort();
				dk.toDataArray(buffer);
			}
			return buffer.toArray(new String[buffer.size()]);
		}
	}

	private boolean checkMeta(String[] path, CmsFile file){
		log.info("check for corresponding meta...");
		TemplateFile parent = pdb.getTemplate(file.parent);
		if(parent == null){
			log.fail("parent template could not be found ["+file.parent+"]");
			return false;
		}

		ArrayList<MetaKernel> storm = mineMeta(parent.getData());

		int i = 0;
		boolean gotit = false;
		while(!gotit){
			//log.info("lap");
			String mid = path[i];
			int j = mid.indexOf('-');
			if(j != -1){
				mid = mid.substring(0, j);
			}
			//log.info("looking for ["+mid+"]");
			boolean found = false;

			for(MetaKernel mk : storm){
				//log.info("?"+mk.id);
				if(mk.id.equals(mid)){
					found = true;
					i++;
					if(path.length > i){
						if(mk.subs != null){
							//log.info("ping");
							storm = mk.subs;
							continue;
						}else{
							log.fail("out of subs");
							break;
						}
					}else{
						//log.info("pong");
						gotit = true;
						//theone = mk;
						break;
					}
				}
			}
			if(!found){
				log.fail("id["+mid+"] not found");
				break;
			}
		}
		if(!gotit){
			return false;
		}
		return true;

	}

	private ArrayList<DataKernel> checkData(String[] path, ArrayList<DataKernel> datas, CmsFile file) {
		//log.info("check for corresponding data...");

		ArrayList<DataKernel> siblings = new ArrayList<DataKernel>(datas);

		int i = 0;
		boolean found = false;
		while(path.length > i+1){
			found = false;
			//log.info("looking for ["+path[i]+"]");
			for(DataKernel dk: siblings){
				//log.info(" ?"+dk.id);
				if(dk.id.equals(path[i])){
					//log.info("  +");

					if(dk.subs == null){
						dk.subs = new ArrayList<DataKernel>();
					}
					siblings = dk.subs;
					found = true;
					break;
				}
			}
			if(!found){
				log.fail("not found ["+path[i]+"]");
				break;
			}
			i++;
		}
		//log.info("i["+i+"]");
		if(i < 1){
			//log.info("s = d");
			siblings = datas;
		}
		return siblings;
	}

	public String[] dynData(String command, String data, CmsFile file) {
		log.info("Dynamic operation...");

		if(file.parent == null){
			log.fail(" no parent, dynamic operation not possible");
			return null;
		}

		log.info(" has parent");

		if(command.equals("add")){
			log.info("add");

			String[] path = data.split("\\.");
			//log.info("path["+data+"] -> ["+path.length+"]");

			if(!checkMeta(path, file)){
				log.fail("metaerror");
				return null;
			}

			ArrayList<DataKernel> datas = mineData(file.getData());
			ArrayList<DataKernel> siblings = checkData(path, datas, file);


			// get the first available number used to create unique id
			//log.info("numstuff");
			int num = 1;
			TreeSet<Integer> ints = new TreeSet<Integer>();
			String target = path[path.length-1];
			for(DataKernel dk: siblings){
				if(dk.id.startsWith(target)){
					//log.info("delim["+dk.id+"]");
					int delim = dk.id.indexOf('-');
					if(delim == -1){
						//log.info(" no delim");
					}else{
						int k = Integer.parseInt(dk.id.substring(delim+1));
						//log.info(" k["+k+"]");
						ints.add(Integer.valueOf(k));
					}
				}
			}
			for(Integer iik : ints){
				if(iik.equals(num)){
					num++;
				}else{break;}
			}
			//log.info("assigned id: "+path[path.length-1]+"-"+num);

			/*if(parent == null){
				log.info("no parent -> adding to root datas");
				datas.add(new DataKernel(path[path.length-1]+"-"+num));
			}else{
				log.info("has parent -> adding to ["+parent.id+"]");
				parent.add(new DataKernel(path[path.length-1]+"-"+num));
			}*/

			siblings.add(new DataKernel(path[path.length-1]+"-"+num));

			//for(DataKernel dk : siblings){
			//log.info(">"+dk.id);
			//}

			//for(DataKernel dk : datas){
			//log.info(">"+dk.id);
			//}

			// form the data from all this
			ArrayList<String> buffer = new ArrayList<String>();
			for(DataKernel dk : datas){
				dk.toDataArray(buffer);
			}

			log.info("</add>");
			return buffer.toArray(new String[buffer.size()]);



		}else if(command.equals("up")){
			log.info("up");

			String[] path = data.split("\\.");
			//log.info("path["+data+"] -> ["+path.length+"]");

			if(!checkMeta(path, file)){
				log.fail("metaerror");
				return null;
			}

			ArrayList<DataKernel> datas = mineData(file.getData());
			ArrayList<DataKernel> siblings = checkData(path, datas, file);

			// find the right element
			// check the element number
			// if bigger than 1
			//  rename to 0
			//  rename previous to this
			//  rename this to previous

			String target = path[path.length-1];

			for(DataKernel dk: siblings){
				if(dk.id.equals(target)){
					//log.info("target found");
					int delim = dk.id.indexOf('-');
					if(delim == -1){
						//log.info(" no delim");
					}else{
						int k = Integer.parseInt(dk.id.substring(delim+1));
						//log.info(" k["+k+"]");
						if( k > 1 ){
							String prev = dk.id.substring(0, delim) +"-"+ (k-1);

							for(DataKernel d2k: siblings){
								if(d2k.id.equals(prev)){
									//log.info("prev found");
									d2k.id = dk.id;
									dk.id = prev;

									break;
								}
							}
							log.fail("inconsistent data");
							//move up in this case
							dk.id = prev;


						}else{
							//log.info("first element allready");
						}

					}
					break;
				}
			}

			// form the data from all this
			ArrayList<String> buffer = new ArrayList<String>();

			Collections.sort(datas);

			for(DataKernel dk : datas){
				dk.sort();
				dk.toDataArray(buffer);
			}

			log.info("</up>");
			return buffer.toArray(new String[buffer.size()]);

		}else if(command.equals("del")){
			log.info("del");

			String[] path = data.split("\\.");
			//log.info("path["+data+"] -> ["+path.length+"]");

			if(!checkMeta(path, file)){
				log.fail("metaerror");
				return null;
			}

			ArrayList<DataKernel> datas = mineData(file.getData());
			ArrayList<DataKernel> siblings = checkData(path, datas, file);

			Collections.sort(siblings);

			String target = path[path.length-1];
			DataKernel dmw = null;
			boolean found = false;
			int k = 0;
			for(DataKernel dk: siblings){
				if(found){
					if(dk.id.startsWith(target)){
						dk.id = target+"-"+(k++);
					}

				}else if(dk.id.equals(target)){
					//log.info("target found");
					found = true;
					dmw = dk;
					k = Integer.parseInt(dk.id.substring(dk.id.indexOf('-')+1));
					//log.info(" k["+k+"]");
					target = target.substring(0,target.indexOf('-'));
				}

			}
			if(found){
				siblings.remove(dmw);
			}

			// form the data from all this
			ArrayList<String> buffer = new ArrayList<String>();

			Collections.sort(datas);

			for(DataKernel dk : datas){
				dk.sort();
				dk.toDataArray(buffer);
			}

			log.info("</del>");
			return buffer.toArray(new String[buffer.size()]);


		}else{
			log.fail("unknown command");
			return null;
		}
	}


	private Kernel dataGut2(BufferedReader reader){
		log.info("dataGut2");

		if(reader == null){
			log.fail("reader == null");
			return null;
		}

		long start = System.nanoTime();
		try{
			char c;
			int i;
			StringBuilder buffer = new StringBuilder();

			boolean openbrace = false;
			boolean doublepoint = false;

			Kernel open = new Kernel();

			while((i = reader.read()) != -1){
				c = (char)i;
				switch (c) {

				case '«':
					log.info("[dg] {");
					if(!openbrace){
						openbrace = true;
						if(buffer.length()>0){
							log.info("[dg] +code");
							//log.info("["+buffer.toString()+"]");

							//open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
							open.add(new Kernel(buffer.toString(),open,Kernel.Type.code));

							//open.data = buffer.toString();
							buffer.setLength(0);
						}
						//dataGut(open, reader);
					}else{
						log.fail("[dg] malform x!p");
						buffer.setLength(0);

					}
					break;

				case '»':
					log.info("[dg] }");
					if(openbrace){
						openbrace = false;
						if(doublepoint){
							doublepoint = false;
							final String id = buffer.toString();
							buffer.setLength(0);
							if(open.id.equals(id)){
								log.info("[dg] $["+id+"]");

								if(open.parent == null){
									log.fail("[dg] malform p=@ - o["+open.id+"]");
								}else{
									log.info("[dg] <");
									open = open.parent;
								}

								/**open = open.parent;
							if(open.parent == null){
								log.info("[dg] <");
								_open.subs.add(open.subs.get(0));
								return _open;
							}*/
							}else{
								log.fail("[dg] malform o!l - o["+open.id+"] l["+id+"]");
								buffer.setLength(0);
							}
						}else{
							log.info("[dg] +dkernel ["+buffer.toString()+"]");
							Kernel temp = new Kernel(buffer.toString(), open, Kernel.Type.data);
							//open.subs.add(temp);
							open.add(temp);
							open = temp;
							buffer.setLength(0);
						}
						//open.subs.add(new Kernel(buffer.toString(), open, Kernel.Type.data));
						//buffer.setLength(0);
					}else{
						log.fail("[dg] malform }!o");
						buffer.setLength(0);
					}
					break;

				case ':':
					if(openbrace){
						log.info("[dg] :");
						doublepoint = true;
						if(buffer.length() == 0){

						}else{
							buffer.append(c);
						}
					}else{
						buffer.append(c);
					}
					break;

				case '[':
					log.info("[dg] [");
					if(buffer.length() > 0){
						log.info("[dg] +code");
						//open.subs.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
						open.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
						buffer.setLength(0);
					}
					log.info("[dg] > [mg]");
					open = metaGut(open, reader);
					break;

				case ']':
					log.info("[dg] malform - ] ");
					buffer.setLength(0);
					break;

					/*case '\n':
				//case '\c':
				case '\r':
					if(buffer.length() >0){
						open.add(new Kernel(buffer.toString(),Kernel.Type.code));
						buffer.setLength(0);
					}
					break;*/
				default:
					//log.info("["+c+"]");
					buffer.append(c);
				}
			}
			log.info("end of stream");
			if(buffer.length() > 0)
				open.add(new Kernel(buffer.toString(),open,Kernel.Type.code));
			log.info("gut2 took " + Utils.nanoTimeToString((System.nanoTime() - start)));
			return open;
		}catch (IOException ioe) {
			log.fail("IOexception while gutting : "+ ioe);
		}
		return null;
	}

	public void setUrl(String url){
		this.url = url;
	}

}


/* mitä muutoksia tapahtunut
 * -poistetut
 * -muokatut
 * -uudet tiedostot
 */

/* pre render
 * 
 * -poista poistetut
 * 
 */

/* renderöinti
 * 
 * -res files copy to target
 * 
 * -check if based on a template
 * - no then just copy contents to target 
 * - yes,
 *   aply data to template
 *    -check if template has a template
 *     aply data to template
 *      -jne / etc
 *  -write file   
 * 
 */

/*preview
 * produce page as string
 * 
 */

