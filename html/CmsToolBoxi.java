package html;

import java.util.ArrayList;

public class CmsToolBoxi extends CmsBoxi{

	//public String tittle;
	//ArrayList<Element> elements;
	//ArrayList<String> fields;

	public CmsToolBoxi(String title) {
		super(title);
		data = new ArrayList<Element>();
		data.add(new Element("<div class=\"toolbar\" >\n"));
		if(title != null)
			data.add(new Element("<h4>"+title+"</h4>\n"));

		//fields = new ArrayList<Field>();
		//this.tittle = super.tittle;
		//elements = super.elements;
		//fields = fields;
	}

	/*
	public String toString(){
		return data.toString()+"</div>\n";		
	}*/
	
	/*
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<div class=\"toolbar\">\n" //id='exp' onClick='expand()'>\n"
		);
		if(title != null){
			sb.append(
					"<h4>"+title+"</h4>\n"
			);
		}

		for(Element e : elements){
			if(e.type.equals(Types.form)){
				sb.append(
						"<form action='"+e.value+"' accept-charset='ISO-8859' method='POST' target='_self'>\n"
				);
			}else if(e.type.equals(Types.input)){
				sb.append("<input name='"+e.hook+"'");
				if(e.value != null)
					sb.append("value='"+e.value+"'");
				sb.append("type='"+e.extra+"'/>\n");
			}else if(e.type.equals(Types.label)){
				sb.append("<label for='"+e.hook+"'>"+e.value+": </label>\n");
			}else if(e.type.equals(Types.tag)){
				sb.append(e.value+"\n");
			}else if(e.type.equals(Types.link)){
				sb.append("<a href=\""+e.value+"\">&#187;"+e.hook+"</a>\n");
			}else if(e.type.equals(Types.paragraph)){
				sb.append("<p>"+e.value+"</p>\n");
			}else if(e.type.equals(Types.code)){
				sb.append("<pre>"+e.value+"</pre>\n");
			}else{
				sb.append("<pre>error</pre>\n");
			}
		}
		sb.append("</div>\n");
		return sb.toString();
	}*/
}
