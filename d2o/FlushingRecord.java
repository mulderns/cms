package d2o;




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
			data = new String[fields.length-1];
			//System.err.println("#### [FlushingRecord] : f.l["+fields.length+"] d.l["+data.length+"]");
			System.arraycopy(fields, 1, data, 0, fields.length-1);
			//data = Arrays.copyOfRange(fields,1,fields.length);
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
