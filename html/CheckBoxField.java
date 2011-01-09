package html;

public class CheckBoxField extends CmsField {
	boolean checked;
	
	public CheckBoxField(boolean checked){
		//super();
		this.checked = checked;
	}
	public CheckBoxField(){
		this(false);
	}
	
	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input name=\""+name+"\"");
		if(value != null){
			if(value.equals("on") ){
				sb.append(" checked=\"checked\"");
			}
		}else if(checked){
			sb.append(" checked=\"checked\"");
		}
		sb.append(" type=\"checkbox\" />");
		return sb.toString();
	}
	
}