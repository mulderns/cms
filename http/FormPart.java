package http;

/**
 * tää toimii jonkin näköisenä avustajana tuossa 
 * multipart/form-datan parsetuksessa. muuhun tästä 
 * ei sitten vissiin oo
 * 
 * @author Valtteri
 *
 */

public class FormPart {
	String contentDisposition;
	String contentType;
	String name;
	String filename;
	//ArrayList<FormField> data;
	String contentEncoding;
	String value;
	public byte[] bytes;

	FormPart(){
		contentDisposition = "default";
		contentType = "default";
		contentEncoding = "text/plain";
		name = "default";
		filename = null;
		//data = new ArrayList<FormField>();
	}

	public String getContentType(){
		return contentType;
	}
	public String getContentEncoding(){
		return contentEncoding;
	}

	public void setName(String newname) {
		name = newname;
	}
	public void setValue(String value){
		this.value = value;
	}
/*
	public void addField(FormField formField) {
		data.add(formField);
	}
*/
	public void setFilename(String filename) {
		this.filename = filename;

	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public String getValue() {
		return value;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("FormPart has:");
		sb.append(
				" contentDisposition: "+
				(contentDisposition==null? "null" : contentDisposition)
		);
		sb.append('\n');
		sb.append(
				" contentType: "+
				(contentType==null? "null" : contentType)
		);
		sb.append('\n');
		sb.append(
				" name: "+
				(name==null? "null" : name)
		);
		sb.append('\n');
		sb.append(
				" filename: "+
				(filename==null? "null" : filename)
		);
		sb.append('\n');
		sb.append(
				" contentEncoding: "+
				(contentEncoding==null? "null" : contentEncoding)
		);
		sb.append('\n');
		sb.append(
				" value: "+
				(value==null? "null" : value)
		);
		sb.append('\n');
		sb.append(" data:");
		sb.append('\n');
		/*for (FormField ffield : data) {
			sb.append("  ");
			sb.append(ffield);
			sb.append('\n');
		}*/

		return sb.toString();
	}

	public String getContentDisposition() {
		return contentDisposition;
	}
}
