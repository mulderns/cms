package html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CmsBoxi {

	protected Element main;
	protected Element title;
	protected ArrayList<Element> data;
	protected Stack<Element2> layers;
	protected ArrayList<Field> fields;

	private String action;


	private CmsBoxi(){
		layers = new Stack<Element2>();
		data = new ArrayList<Element>();
		fields = new ArrayList<Field>();
		action = "";
	}

	public CmsBoxi(String title) {
		this(title, "");
	}

	public CmsBoxi(String title, String styleClass) {
		this();

		main = new Element2("div class=\"boxi2 "+styleClass+"\"");
		if(title != null)
			this.title = new Element("<h4>"+title+"</h4>\n");
		//data.add(new Element("<div class=\"ingroup filled\">"));
		addLayer("div class=\"ingroup filled\"");
	}

	public void setTitle(String title, String style_class) {
		this.title = new Element("<h4 class=\""+style_class+"\">"+title+"</h4>");
	}

	public void addLayer(String intag){
		Element2 temp = new Element2(intag);

		if(layers.empty()){
			data.add(temp);
		}
		layers.push(temp);		

	}

	public void upLayer(){
		data.add(layers.pop());
	}

	private void add(Element e){
		if(layers.empty()){
			data.add(e);
		}else{
			layers.peek().add(e);
		}
	}

	public void addBr(){
		add(new Element("<br/>\n"));
	}

	public void addhr(){
		add(new Element("<hr/>\n"));
	}

	public void endForm(){
		add(new Element("</form>\n"));
	}

	public void addLink(String label, String url) {
		add(new Element("<a class=\"list\" href=\""+url+"\">&#187;"+label+"</a>\n"));
	}

	public void addP(String text) {
		add(new Element("<p style=\"font-size:12.5px;margin:4px;\">"+text+"</p>\n"));
	}

	public void addPre(String text) {
		add(new Element("<pre>"+text+"</pre>\n"));
	}

	public void addForm(String action_url) {
		action = action_url;

		add(new Element(new Form()));
	}

	public void addInput(String hook, String value, String type, String label) {
		if(label != null){
			addLabel(label);
		}
		if(type != null){
			Field temp = new Field(hook,value,true,new CmsField(type));
			fields.add(temp);
			add(new Element(temp));
		}
	}

	public void addInputCols(String hook, String value, String cols) {
		Field temp = new Field(hook,value,true, new TextField(Integer.parseInt(cols)));
		fields.add(temp);
		add(new Element(temp));
	}

	public void addField(String name, String value, boolean checked, CmsField ft){
		Field temp = new Field(name,value,checked,ft);
		fields.add(temp);
		add(new Element(temp));
	}

	public void addTag(String string) {
		add(new Element(string+"\n"));
	}

	public void addSource(String string) {
		add(new Element(string));
	}

	public void addLabel(String string) {
		add(new Element("<label>"+string+": </label>\n"));
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append("<!-- ["+main.data+"] -->\n");
		
		sb.append(alku(main.data));
		if(title != null)
			sb.append(title.toString());

		for(Element e : data){
			sb.append(e.toString());
		}


		sb.append(loppu(main.data));
		return sb.toString();
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


	public void setAction(String action_url) {
		action = action_url;
	}

	static class Form{
		public static final String mangle(String action_url){
			return "<form action=\""+action_url+"\" accept-charset=\"ISO-8859-1\" method=\"POST\" target=\"_self\">\n";
		}
	}

	class Element2 extends Element{

		public Element2(String string) {
			super(string);
		}
		public Element2(Form form) {
			super(form);
		}
		public Element2(Field field) {
			super(field);
		}

		ArrayList<Element> subs;

		public void add(Element e){
			if(subs==null)
				subs = new ArrayList<Element>();
			subs.add(e);
		}

		public String toString(){
			StringBuilder sb = new StringBuilder();
sb.append("<!-- e2 s["+(subs==null?"@":subs.size())+"]  -->");
			if(data != null){
				sb.append(alku(data));
				if(subs != null)
					for(Element e : subs)
						sb.append(e.toString());
				sb.append(loppu(data)+"\n");
			}else if(isForm){
					sb.append(Form.mangle(action)+"\n");
			}else {
				sb.append(field2.toString()+"\n");
			}
			return sb.toString();

		}
	}

	class Element{

		protected final String data;
		protected final boolean isForm;
		protected final Field field2;

		public Element(String string) {
			data = string;
			isForm = false;
			field2 = null;
		}

		public Element(Form form) {
			data = null;
			isForm = true;
			field2 = null;
		}

		public Element(Field field) {
			data = null;
			isForm = false;
			field2 = field;
		}

		public String toString(){
			if(data != null)
				return data;
			if(isForm)
				return Form.mangle(action);
			return field2.toString();

		}
	}

	public String wrap(String body, String core) {
		return alku(body) + core + loppu(body);
	}
	private String alku(String defi) {
		return "<" + defi + ">";
	}
	private String loppu(String defi) {
		int end = defi.indexOf(' ');
		return "</" + ((end == -1) ? defi : defi.substring(0, end )) + ">";

	}


}


