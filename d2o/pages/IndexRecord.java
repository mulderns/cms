package d2o.pages;

import java.io.File;

public class IndexRecord{
	public String filename;
	//public String parent;
	//public CmsFile.Type type;
	public boolean modified;
	public File dir;

	public IndexRecord(String filename) {
		this.filename = filename;
		modified = false;
		//type = CmsFile.Type.TEXT;
	}

	public String toString(){
		String[] fields = {
				filename,
				//type.toString(),
				//(parent==null?"none":parent),
				(modified?"+":"-")
		};
		return util.Csv.encode(fields);		
	}
}
