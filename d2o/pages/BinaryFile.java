package d2o.pages;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFile extends CmsFile{

	public String header;
	private String data;
	private byte[] databytes;
	private boolean loaded = false;

	public BinaryFile(CmsFile cmsFile) {
		super(cmsFile);
	}

	public BinaryFile(String filename) {
		super(filename);
		type = Type.BINARY;
		content_type="application/octet-stream";
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
		BufferedReader bin = datasource.initRead();
		try{
			int last;
			while((last = bin.read()) > -1){
				sb.append((char)last);
			}
			datasource.endRead(bin);
			data = sb.toString();
			return true;
		}catch (IOException ioe) {
			datasource.endRead(bin);
			return false;
		}

	}

	protected boolean writeData(File dir) {
		try{
			BufferedOutputStream bout =	new BufferedOutputStream(
					new FileOutputStream(
							new File(dir, datafilename), false
					)
			);
			bout.write(databytes);
			bout.close();
			return true;
		}catch(IOException ioe){
			return false;
		}
	}


	public void setData(byte[] bytes) {
		databytes = bytes;		
	}

}
