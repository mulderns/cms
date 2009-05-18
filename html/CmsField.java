package html;

public class CmsField {
	//String tag;
	//boolean twopart;
	String type = "text";
	
	CmsField(){
		//tag = "input";
		//twopart = false;	
	}

/*	public String getTag(){
		return tag;
	}*/
	
	public CmsField(String type) {
		this.type = type;
	}

	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		if(type.equals("submit")){
			sb.append("<div>");
		}
		sb.append("<input");
		if(name != null){
			sb.append(" name=\""+name+"\"");
		}
		if(value != null){
			sb.append(" value=\""+value+"\"");
		}
		sb.append(" type=\""+type+"\" />");
		if(type.equals("submit")){
			sb.append("</div>");
		}
		return sb.toString();
	}
}
