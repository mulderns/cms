package html;

public class CmsPage2 extends CmsPage {

	public CmsPage2(String title) {
		super(title);

	}
	
	@Override
	public String getBody() {
		StringBuilder sb = new StringBuilder();

		sb.append("<div class=\"wrapper\">\n");

		for(String unit: top){
			sb.append(unit);
		}
		
		sb.append("<div class=\"side4\">");
		for(String unit: right){
			sb.append(unit);
		}
		sb.append("</div>");

		if(left.size() > 0){
			sb.append("<div class=\"side5\">");
			for(String unit: left){
				sb.append(unit);
			}
			sb.append("</div>");
		}
		

		sb.append("<div class=\"content\">");
		for(String unit: center){
			sb.append(unit);
		}
		sb.append("</div>");



		sb.append("</div>");

		return sb.toString();
	}

}
