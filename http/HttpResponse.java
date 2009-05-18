package http;
import java.util.ArrayList;


/**
 * nyt en oo ihan varma käyttäiskö toi pageparser
 * tätä mutta vissiin se käyttää.
 * 
 *
 * @author Valtteri
 *
 */

public class HttpResponse {

	private String content_type;
	private String charset;
	private ArrayList<String> cookies;

	private final String linesep = System.getProperty("line.separator");

	public HttpResponse(){
		content_type = "text/html";
		charset = "iso-8859-1";
		cookies = new ArrayList<String>();
	}

	public void setContentType(String type){
		content_type = type;
	}

	public void setCharset(String charset){
		this.charset = charset;
	}

	public String getCharset(){
		return charset;
	}

	public boolean cookieSet(){
		return cookies.size() > 0;
	}

	public void addCookie(String cookie){
		cookies.add(cookie);
	}

	public String toString(){
		StringBuilder buf = new StringBuilder();
		buf.append("Content-type: "+content_type);
		buf.append("; charser="+charset);
		if(cookies.size() > 0){
			for (String cookie : cookies) {
				buf.append(linesep + cookie);
			}
		}
		buf.append(linesep + linesep + linesep);
		return buf.toString();
	}

	public String getCookie() {
		return cookies.get(0);
	}
}
