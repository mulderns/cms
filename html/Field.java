package html;



public class Field extends CmsElement{

	boolean required;
	String name;
	String value;
	CmsField fieldtype;
	
	

	public Field(String name, String value, boolean required, CmsField ft) {
		this.name = name;
		this.value = value;
		this.required = required;
		this.fieldtype = ft;
		type = CmsElement.Type.field;
	}

	public String toString(){
		return fieldtype.produce(name,value);
	}
	
	@Override
	public StringBuilder toString(StringBuilder sb, String space){
		sb.append(fieldtype.produce(name, value));
		return sb;
	}
	
}