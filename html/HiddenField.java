package html;

public class HiddenField extends CmsField {
	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input name=\""+name+"\"");
		if(value != null){
			sb.append(" value=\""+value+"\"");
		}
		sb.append(" type=\"hidden\" />");
		return sb.toString();
	}
}
