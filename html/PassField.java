package html;

public class PassField extends CmsField {
		int cols;
		
		public PassField(int cols){
			//super();
			this.cols = cols;
		}
		public String produce(String name, String value) {
			StringBuilder sb = new StringBuilder();
			sb.append("<input name=\""+name+"\"");
			if(value != null){
				sb.append(" value=\""+value+"\"");
			}
			sb.append("size=\""+cols+"\" type=\"password\" />");
			return sb.toString();
		}
		
	}