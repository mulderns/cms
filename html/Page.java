package html;

import java.util.ArrayList;

public class Page {
	public String title;
	public ArrayList<String> head;
	
	public Page(String title) {
		if(title == null){
			title = "untitled";
		}else{
			this.title = title;
		}
		head = new ArrayList<String>();
	}
	
	public void setTitle(String string) {
		title = string;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void addHead(String string){
		head.add(string);
	}
	
	public String[] getHeadTags() {
		return head.toArray(new String[head.size()]);
	}
	
	public String getBody() {
		return "none";
	}
}
