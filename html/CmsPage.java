package html;

import java.util.ArrayList;

public class CmsPage extends Page{
	public ArrayList<String> top;
	public ArrayList<String> left;
	public ArrayList<String> center;
	public ArrayList<String> right;

	public CmsPage(String title) {
		super(title);
		top = new ArrayList<String>();
		left = new ArrayList<String>();
		center = new ArrayList<String>();
		right = new ArrayList<String>();
	}
	
	public void addTop(String stuff){
		top.add(stuff);
	}
	public void addLeft(String stuff) {
		left.add(stuff);
	}
	public void addCenter(String stuff) {
		center.add(stuff);
	}
	public void addRight(String stuff) {
		right.add(stuff);
	}
	
	public void addTop(CmsElement stuff){
		top.add(stuff.toString());
	}
	public void addLeft(CmsElement stuff) {
		left.add(stuff.toString());
	}
	public void addCenter(CmsElement stuff) {
		center.add(stuff.toString());
	}
	public void addRight(CmsElement stuff) {
		right.add(stuff.toString());
	}
	
	public void setTitle(String string) {
		title = string;
	}
	
	public String getBody() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<div class=\"wrapper\"><div class=\"center\">\n");
		
		sb.append("<table class=\"main\">\n<tr>\n<td>");
		for(String unit: left){
			sb.append(unit);
		}
		sb.append("</td>\n<td>\n");
		for(String unit: center){
			sb.append(unit);
		}
		sb.append("</td>\n<td>\n");
		for(String unit: right){
			sb.append(unit);
		}
		sb.append("</td>\n</tr>\n</table>\n</div>\n</div>");
		return sb.toString();
	}
}
