package d2o.pages;

import java.io.File;
import java.util.ArrayList;

import d2o.FlushingFile;
import d2o.render.MetaKernel;
import d2o.render.Renderer;

import util.Csv;

public class TemplateFile extends CmsFile {

	public String data;
	private String[] fields;

	public TemplateFile(String filename){
		super(filename);
		metafilename = "temp.meta."+filename;
		datafilename = "temp.data."+filename;
		data = "";
	}


	private String mineFields() {
		ArrayList<MetaKernel> fields = Renderer.mineMeta(data);
		if(fields != null){
			StringBuilder sb = new StringBuilder();
			for(MetaKernel kernel: fields){
				sb.append(kernel.getType());
				sb.append(',');
			}
			if(sb.length()>0)
				sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		}
		return "";
	}

	private boolean parseMeta(String[] lines) {
		if(lines == null || lines.length < 1)
			return false;
		String[] pieces = Csv.decode(lines[0]);

		//discard first piece; 'temp'

		if(pieces.length > 1)
			parent = (pieces[1].equals("null")||pieces[1].equals("none")?null:pieces[1]);
		if(lines.length > 1){
			fields = Csv.decode(lines[1]);
		}

		return true;
	}


	private boolean parseData(String[] lines) {
		if(lines == null)
			return false;
		StringBuilder sb = new StringBuilder();
		for(String line: lines)
			sb.append(line).append("\n");
		data = sb.toString();
		return true;
	}

	@Override
	public String[] getMeta(){
		String[] metas = {
				type.toString(),
				(parent==null?"none":parent),
				content_type
		};

		String[] temp = {
				util.Csv.encode(metas),
				mineFields()
		};
		return temp;
	}

	@Override
	public String getData() {
		return data;
	}


	public boolean load(File source_dir){
		FlushingFile metasource = new FlushingFile(new File(source_dir, metafilename));
		FlushingFile datasource = new FlushingFile(new File(source_dir, datafilename));
		if( !( parseMeta(metasource.loadAll()) && parseData(datasource.loadAll()) ) ){
			return false;
		}

		return true;
	}

	public String[] getFields() {
		if(fields != null)
			return fields;
		return new String[0];
	}


	public void setData(String[] lines) {
		if(lines == null)
			return;
		StringBuilder sb = new StringBuilder();
		for(String line: lines)
			sb.append(line).append("\n");
		data = sb.toString();
		//loaded = true;
	}
}
