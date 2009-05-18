package d2o.pages;


import java.io.BufferedReader;
import java.io.IOException;

public class TextFile extends CmsFile {

	private String data;
	private boolean loaded = false;

	public TextFile(CmsFile cmsFile){
		super(cmsFile);
	}
	
	public TextFile(String filename){
		super(filename);
		metafilename = "page.meta."+filename;
		datafilename = "page.data."+filename;
		data = "";
	}


	@Override
	public String getData() {
		if(!loaded)
			if(!loadData()){
				return null;
			}else{
				loaded = true;
			}
		return data;
	}

	private boolean loadData() {
		StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader bin = datasource.initRead();
		try{
			while((line = bin.readLine())!=null){
				sb.append(line).append("\n");
			}
			datasource.endRead(bin);
			data = sb.toString();
			return true;
		}catch (IOException ioe) {
			datasource.endRead(bin);
			return false;
		}
	}

	public void setData(String[] lines) {
		if(lines == null)
			return;
		StringBuilder sb = new StringBuilder();
		for(String line: lines)
			sb.append(line).append("\n");
		data = sb.toString();
		loaded = true;
	}

	public void setData(String line) {
		loaded = true;
		data = line;
	}

}
