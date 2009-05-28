package html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CmsElement {

	String intag;
	String style_class;
	Type type;
	ArrayList<CmsElement> children;
	Stack<CmsElement> layers;
	ArrayList<Field> fields;


	public CmsElement(CmsElement clone){
		intag = clone.intag;
		style_class = clone.style_class;
		type = clone.type;
		children = new ArrayList<CmsElement>(clone.children);
		layers = new Stack<CmsElement>();
		layers.addAll(clone.layers);
		
		fields = new ArrayList<Field>(clone.fields);
	}
	
	public CmsElement(){
		this(Type.normal);
		fields = new ArrayList<Field>();
	}

	CmsElement(Type type){
		this.type = type;
		//intag = "";
		if(type.equals(Type.single)||type.equals(Type.content)){

		}else{
			children = new ArrayList<CmsElement>();
			layers = new Stack<CmsElement>();
			layers.push(this);
		}

	}

	public CmsElement(String intag, String style_class) {
		this();
		this.intag = intag;
		this.style_class = style_class;
	}

	public CmsElement(Type type, String data) {
		this(type);
		this.intag = data;
	}

	public void addForm(String action) {
		CmsElement temp = new CmsElement(Type.form, action);
		//layers.add(arg0, arg1)
		layers.peek().add(temp);
		layers.push(temp);
	}
	
	public void addFormTop(String action_url) {
		//CmsElement temp = new CmsElement(, null);
		this.intag = "form action=\""+action_url+"\" accept-charset=\"ISO-8859-1\" method=\"POST\" target=\"_self\"";
	}

	private void add(CmsElement element) {
		children.add(element);		
	}
	
	public void addElement(CmsElement element){
		layers.peek().add(element);
	}
	
	public void addElementOpen(CmsElement element){
		layers.peek().add(element);
		
		Stack<CmsElement> a = element.layers;
		for(CmsElement e : a){
			layers.push(e);
		}
		
		
	}

	public void addLayer(String intag, String style_class) {
		CmsElement temp = new CmsElement(intag, style_class);
		layers.peek().add(temp);
		layers.push(temp);
	}


	public void addLink(String intag, String style_class, String href, String label) {
		CmsElement temp = new CmsElement("a "+intag+" href=\""+href+"\"", style_class);
		temp.addSource(label);
		layers.peek().add(temp);
	}
	
	public void addLink(String label, String url) {
		addLink(null,"list",url,label);		
	}
	
	public void addTag(String intag, String style_class, String content) {
		CmsElement temp = new CmsElement(intag, style_class);
		temp.addSource(content);
		layers.peek().add(temp);
	}
	
	public void addTag(String intag, String content) {
		CmsElement temp = new CmsElement(intag, style_class);
		temp.addSource(content);
		layers.peek().add(temp);
	}

	private void addSource(String content) {
		layers.peek().add(new CmsElement(Type.content, content));
	}
	
	public void addContent(String content) {
		addSource(content);		
	}

	public void addSingle(String intag) {
		layers.peek().add(new CmsElement(Type.single, intag));
	}

	public void addLayer(String intag) {
		addLayer(intag, null);
	}

	public void up() {
		if(layers.size() > 1)
			layers.pop();
	}
	
	public void up(int i) {
		while(i-- > 0)
			if(layers.size() > 1)
				layers.pop();
	}
	

	public void addField(String name, String value, boolean checked,
			CmsField fieldt) {
		Field temp = new Field(name, value, checked, fieldt);
		layers.peek().add(temp);
		fields.add(temp);
	}

	public String[] getFields() {
		ArrayList<String> allfields = new ArrayList<String>();

		for(Field f : fields){
			if(f.required){
				allfields.add(f.name);
			}
		}

		return allfields.toArray(new String[allfields.size()]);
	}

	public void setFields(HashMap<String, String> post) {
		for(Map.Entry<String, String> entry: post.entrySet())
			for(Field f : fields){
				if(f.name != null){
					if(f.name.equals(entry.getKey())){
						f.value=entry.getValue();
						break;
					}
				}
			}

	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		toString(sb,"");
		return sb.toString();
	}
	public StringBuilder toString(StringBuilder sb, String space){
		
		if(type.equals(Type.normal)){
			if(intag == null){
				for(CmsElement e : children){
					e.toString(sb,space+" ");
				}
			}else{
				sb.append(space +"<"+intag);
				if(style_class != null)
					sb.append(" class=\""+style_class+"\"");
				sb.append(">");

				for(CmsElement e : children){
					e.toString(sb,space+" ");
				}
				int end = intag.indexOf(' ');
				sb.append("</" + ((end == -1) ? intag : intag.substring(0, end )) + ">\n");
			}

		}else if(type.equals(Type.single)){
			sb.append(space +"<"+intag);
			if(style_class != null)
				sb.append(" class=\""+style_class+"\"");
			sb.append("/>\n");

		}else if(type.equals(Type.content)){
			sb.append(intag);
		}
		return sb;
	}


	enum Type{
		form,
		field,
		single,
		normal,
		content
	}


	public void createBox(String tittle) {
		createBox(tittle,"");
	}
	public void createBox(String tittle, String style_class) {
		addLayer("div", "boxi2 "+style_class);
		if(tittle != null)
			addTag("h4", null, tittle);
		addLayer("div", "ingroup filled");
	}





}