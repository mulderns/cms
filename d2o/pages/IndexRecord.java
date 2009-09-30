package d2o.pages;

public class IndexRecord {
	public String filename;
	public char	status;
	public String path;

	public IndexRecord(String filename, String path){
		this.filename = filename;
		this.path = path;
		status = 'n';
	}

	public IndexRecord(String filename){
		this.filename = filename;
		path = "";
		status = 'n';
	}

	public IndexRecord(String[] parts){
		if(parts.length != 3){
			filename = "error";
			status = 'e';
			path = "";
		}else{
			filename = parts[0];
			status = parts[1].charAt(0);
			path = parts[2];
		}
	}

	public String[] toArray(){
		String[] fields = {
				filename,
				Character.toString(status),
				path
		};
		return fields;
	}
}
