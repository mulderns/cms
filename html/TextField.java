package html;

public class TextField extends CmsField {
	int cols;
	boolean enabled = true;
	
	
	public TextField(int cols){
		//super();
		this.cols = cols;
	}
	
	public TextField(int cols, boolean enabled){
		this.cols = cols;
		this.enabled = enabled;
	}
	
	public TextField() {
		cols = 0;
	}

	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input name=\""+name+"\"");
		if(value != null){
			sb.append(" value=\""+value+"\"");
		}
		if(!enabled)
			sb.append(" disabled=\"disabled\"");
		
		if(cols > 0){
			sb.append(" size=\""+cols+"\"");
		}
		sb.append(" type=\"text\" />");
		return sb.toString();
	}
	
}