package d2o;


public class UploadFileRecord {
	public String filename;
	public String stored_name;
	public long size;

	public String content_type;
	public String content_encoding;
	public String content_disposition;

	public String upload_user;
	public long upload_date;

	public int download_count;

	public boolean public_access;
	public String access_groups;

	public UploadFileRecord(){
		filename = "untitled";
		stored_name = "none";
		size = 0;

		content_type = "";
		content_encoding = "";
		content_disposition = "";

		upload_user = "";
		upload_date = 0;

		download_count = 0;

		public_access = false;
		access_groups = null;
	}
	
	public UploadFileRecord(String[] parts) {
		if(parts.length == 11){
			filename = parts[0];
			stored_name= parts[1];
			size = Long.parseLong(parts[2]);

			content_type= parts[3];
			content_encoding= parts[4];
			content_disposition= parts[5];

			upload_user= parts[6];
			upload_date= Long.parseLong(parts[7]);

			download_count = Integer.parseInt(parts[8]);

			public_access = Boolean.parseBoolean(parts[9]);
			access_groups = parts[10];
			
		}else{
			filename = "error";
		}
	}
	
	public String[] toArray() {
		String[] output = { 

				filename,
				stored_name,
				Long.toString(size),

				content_type,
				content_encoding,
				content_disposition,

				upload_user,
				Long.toString(upload_date),

				Integer.toString(download_count),

				Boolean.toString(public_access),
				access_groups

		};
		return output;
	}

}
