package html;

public class SubmitField extends CmsField {

	boolean disguise = false;

	public SubmitField(){

	}

	public SubmitField(boolean disguise){
		this.disguise = disguise;
	}

	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div><input name=\""+name+"\"");
		if(value != null){
			sb.append(" value=\""+value+"\"");
		}
		sb.append(" type=\"submit\"");
		
		if(disguise){
			sb.append(" class=\"list\"");
		}
		
		sb.append(" /></div>");
		return sb.toString();
	}
}
