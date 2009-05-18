package html;

public class FileField extends CmsField {
	
	public FileField(){
		//super();
	}
	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input name=\""+name+"\"");
		if(value != null){
			sb.append(" value=\""+value+"\"");
		}
		sb.append("type=\"file\" />");
		return sb.toString();
	}
	
}