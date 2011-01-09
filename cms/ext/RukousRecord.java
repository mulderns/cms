package cms.ext;

public class RukousRecord {

	public String time;
	public String text;
	public boolean censored;
	
	public RukousRecord(long time, String text){
		this.time = Long.toString(time);
		this.text = text;
		censored = false;
	}
	
	public RukousRecord(){
		this(System.currentTimeMillis(),"");
	}
	
	public RukousRecord(String[] parts){
		time = parts[0];
		text = parts[1];
		if(parts.length > 2 && parts[2].equals("sensured")){
			censored = true;
		}else{
			censored = false;
		}
	}
	
	public String[] toArray(){
		String[] array = {
			time,
			text,
			(censored?"sensured":"")
		};
		return array; 
	}
	
	public String toString(){
		return "time["+time+"] censored["+(censored?"true":"fals")+"] text["+text+"]";
	}
}
