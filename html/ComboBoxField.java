package html;


public class ComboBoxField extends CmsField {
	String[] elements;
	String selected;
	
	public ComboBoxField(String[] elements){
		this.elements = elements;
	}
		
	public ComboBoxField(String[] elements, String selected){
		this.elements = elements;
		this.selected = selected;
	}
	
	public ComboBoxField(String[] elements, String selected,String additional){
		this.elements = new String[elements.length+1];
		this.elements[0] = additional;
		for(int i = 0; i < elements.length; i++){
			this.elements[i+1] = elements[i];
		}
		this.selected = selected;
	}
	
	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<select name=\""+name+"\"");

		sb.append("size=\"1\">");
		if(elements != null){
			for(String e: elements){
				if(selected != null){
					if(e.equals(selected)){
						sb.append("\n<option value=\""+e+"\" selected=\"selected\">"+e+"</option>");
						selected = null;
						continue;
					}
				}
				sb.append("\n<option value=\""+e+"\">"+e+"</option>");
				
			}
		}else{
			sb.append("\n<option value=\"\">empty</option>");
		}
		if(selected != null){
			sb.append("\n<option value=\""+selected+"\" selected=\"selected\">"+selected+"</option>");
		}
		sb.append("</select>");
		return sb.toString();
	}
	
}
