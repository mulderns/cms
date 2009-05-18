package html;

public class TextAreaField extends CmsField{
	int cols;
	int rows;

	public TextAreaField(int cols, int rows){
		//super();
		this.cols = cols;
		this.rows = rows;
		//tag = "textarea";
		//	twopart = true;
	}

	public TextAreaField(int height){
		//super();
		cols = 0;
		this.rows = height;
		//tag = "textarea";
		//	twopart = true;
	}
	
	public TextAreaField(String height){
		this(Integer.parseInt(height));
	}

	public String produce(String name, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<textarea name=\""+name+"\" ");

		if(cols == -1){
			sb.append("style=\"width:97%\" ");
			sb.append("rows=\""+rows+"\">");
		}else if(cols != 0){
			sb.append("cols=\""+cols+"\" ");
			sb.append("rows=\""+rows+"\">");
		}else{
			sb.append("style=\"height:"+rows+"px;\">");
		}
		if(value != null){
			sb.append(value);
		}
		sb.append("</textarea>");
		return sb.toString();
	}

}
