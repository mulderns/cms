package html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CmsElement {
	String intag;
	String style_class;
	Type type;

	CmsElement parent;
	CmsElement current;

	ArrayList<CmsElement> children;
	ArrayList<Field> fields;


	public CmsElement(CmsElement clone){
		intag = clone.intag;
		style_class = clone.style_class;
		type = clone.type;

		parent = null;
		
		if(clone.current == clone)
			current = this;

		if(clone.children != null){
			children = new ArrayList<CmsElement>(clone.children.size());
			for(CmsElement e: clone.children){
				CmsElement temp = new CmsElement(e);
				temp.parent = this;
				if(temp.current != null){
					current = temp.current;
				}
				children.add(temp);
			}
		}
		
		if(clone.fields != null){
			fields = new ArrayList<Field>(clone.fields);
		}

	}

	public CmsElement(){
		this(Type.normal);
		current = this;
		fields = new ArrayList<Field>(0);
	}

	CmsElement(Type type){
		this.type = type;
		if(!(type.equals(Type.single)||type.equals(Type.content))){
			children = new ArrayList<CmsElement>();
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
		current.add(temp);
		current = temp;
	}

	public void addFormTop(String action_url) {
		this.intag = "form action=\""+action_url+"\" accept-charset=\"ISO-8859-1\" method=\"POST\" target=\"_self\"";
	}

	private void add(CmsElement element) {
		children.add(element);
		element.parent = this;
	}

	public void addElement(CmsElement element){
		current.add(element);
	}

	public void addElementOpen(CmsElement element){
		current.add(element);
		if(element.current != null){
			current = element.current;
		}else{
			System.err.println("element.current == @null");
		}
	}

	public void addLayer(String intag, String style_class) {
		CmsElement temp = new CmsElement(intag, style_class);
		current.add(temp);
		if(current != this)
			current.current = null;
		current = temp;
		temp.current = current;
	}


	public void addLink(String intag, String style_class, String href, String label) {
		CmsElement temp = new CmsElement("a "+intag+" href=\""+href+"\"", style_class);
		temp.addSource(label);
		current.add(temp);
	}

	public void addLink(String label, String url) {
		addLink(null,"list",url,label);		
	}

	public void addTag(String intag, String style_class, String content) {
		CmsElement temp = new CmsElement(intag, style_class);
		temp.addSource(content);
		current.add(temp);
	}

	public void addTag(String intag, String content) {
		CmsElement temp = new CmsElement(intag, style_class);
		temp.addSource(content);
		current.add(temp);
	}

	private void addSource(String content) {
		current.add(new CmsElement(Type.content, content));
	}

	public void addContent(String content) {
		addSource(content);		
	}

	public void addSingle(String intag) {
		current.add(new CmsElement(Type.single, intag));
	}

	public void addLayer(String intag) {
		addLayer(intag, null);
	}

	public void up() {
		if(current.parent != null)
			current = current.parent;
	}

	public void up(int i) {
		while(i-- > 0)
			if(current.parent != null)
				current = current.parent;
	}


	public void addField(String name, String value, boolean checked,
			CmsField fieldt) {
		Field temp = new Field(name, value, checked, fieldt);
		current.add(temp);
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