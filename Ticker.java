import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class Ticker {

	public static void main(String[] args){
		System.out.println("Content-type:text/plain\n\n\n");
		System.out.println(".");

		String[] output = {
				String.valueOf(System.currentTimeMillis()),
				System.getenv("DOCUMENT_URI"),
				System.getenv("REMOTE_ADDR"),
				System.getenv("REMOTE_HOST"),
				System.getenv("HTTP_USER_AGENT"),
		};

		try{
			BufferedWriter bout =
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(
										new File("..","ticker.info.dat"), true
								), "ISO-8859-1"
						)
				);
			bout.write(encode(output)+"\n");
			bout.close();
		}catch(IOException ioe){
			
		}
	}
	public static final String encode(final String[] raw) {
		final StringBuilder sbuf = new StringBuilder();

		String entity;
		char c;
		boolean problem_found, worse_found;
		for (int i = 0; i < raw.length; i++) {
			if(raw[i] == null){
				sbuf.append("\"\",");//raw[i] = "";
				continue;
			}
			
			entity = raw[i];
			
			problem_found = false;
			worse_found = false;
			for (int j = 0; j < entity.length(); j++) {
				c = entity.charAt(j);
				if (c == ',' || c == ' ') {
					problem_found = true;
					//break;
				}else if(c == '"'){
					worse_found = true;
				}
			}
			if(worse_found)
				entity = entity.replace("\"", "''");
			
			if (problem_found) {
				sbuf.append("\"" + entity + "\",");
			}else{
				sbuf.append(entity + ",");
			}
		}

		final int length = sbuf.length();
		if (length > 1) {
			return sbuf.substring(0, length - 1);
		}
		return "";
	}
}
