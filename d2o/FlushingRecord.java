package d2o;

import java.util.Arrays;


public class FlushingRecord {
	public String id;
	public String[] data;

	public FlushingRecord(String id, String[] data){
		this.id = id;
		this.data = data;
	}

	public FlushingRecord(String[] fields){
		id = fields[0];
		if(fields.length > 1){
			data = Arrays.copyOfRange(fields,1,fields.length);
		}

	}

	public String[] toArray() {
		String[] array = new String[data.length+1];
		array[0] = id;
		System.arraycopy(data, 0, array, 1, data.length);
		return array;

		//		ArrayList<String> stack = new ArrayList<String>() ;
		//		stack.add(id);
		//		if(data != null){
		//			for(String s: data)
		//				stack.add(s);
		//		}
		//		return stack.toArray(new String[stack.size()]);
	}

}
