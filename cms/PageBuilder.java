package cms;

import html.Page;
import http.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import util.Logger;
import util.TriState;
import util.Utils;

/**
 * Tämän luokan tämän hetkinen tehtävä on toimia
 * jonkin näköisenä bufferina johon tulostettava
 * sivu kootaan.
 * 
 * Tähän oon ehkä koodannu jotain hämärää merkistön
 * oikeisuus koodia, no lähinnä toi bwriter joka
 * sitten sylkee ne sivut lopulta toivottavasti
 * oikeassa muodossa ja sulkee System.out:n.
 *  
 * @author Valtteri
 *
 */

public class PageBuilder {

	//hardcoded facts :)
	//private final String http_header = "Content-Type: text/html; charset=iso-8859-1;\n\n";
	/*private final String doctype = 
		"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
	 */

	private Logger log;
	//private final Cgicms host;

	private BufferedWriter bwriter;
	private TriState out_state;

	private HttpResponse response;
	private String encoding;

	private String docTitle;
	private ArrayList<String> headTags;
	private ArrayList<String> styles;
	private ArrayList<String> page;
	private ArrayList<String> messages;
	private ArrayList<String> hidden;

	private boolean redirect = false;
	private String redirect_location;

	private String script;//,res_root;


	public static TriState state = new TriState();

	public PageBuilder(Cgicms host){
		log = new Logger("PageBuilder");
		out_state = new TriState();

		script = host.getScriptFile();

		response = new HttpResponse();
		encoding = "ISO-8859-1";

		docTitle = "untitled";
		headTags = new ArrayList<String>();
		styles = new ArrayList<String>();
		page = new ArrayList<String>();
		messages = new ArrayList<String>();
		hidden = new ArrayList<String>();
	}

	private boolean openOutputStream(){
		if(out_state.pure){
			try{
				bwriter	= new BufferedWriter(
						new OutputStreamWriter(System.out , encoding)
				);
				out_state.touch();
				return true;
			}catch(IOException ioe){
				log.fail("error opening stream: " + ioe);
			}
		}
		return false;
	}

	public void setEncoding( String encoding){
		this.encoding = encoding;
	}

	public void setHeaderCharset(String charset){
		response.setCharset(charset);
	}

	public void setTitle(String title){
		docTitle = title;
	}

	public void addHeadTag(String tag){
		headTags.add(tag);
	}

	public void bake(){
		log.info("proceeding to bake");
		long _start = System.currentTimeMillis();
		if(out_state.pure){
			if(openOutputStream()){
				log.info("baking e["+encoding+"] rcs["+response.getCharset()+"]");
				try{
					if(redirect){
						bwriter.write("Location: "+redirect_location+"\n\n");
						bwriter.close();
						out_state.touch();
						return;
					}

					//bwriter.write(superPlug(response.toString()));
					bwriter.write(response.toString());
					bwriter.write(genHead());
					bwriter.write(alku("body"));

					if(messages.size()>0){
						//TODO: messages from disk cache
						//bwriter.write(superPlug("<div style=\"margin:10px auto;width:300px;padding:4px;font-size:11px;background-color:#eee;border:2px dashed #ddd;\">"));
						bwriter.write("<div style=\"margin:10px auto;width:300px;padding:4px;font-size:11px;background-color:#eee;border:2px dashed #ddd;\">");
						for (String line : messages) {
							//bwriter.write(wrap("pre", superPlug(line)));
							bwriter.write(wrap("pre", line));
						}
						bwriter.write("</div>");
					}
					for (String line : page) {
						//bwriter.write(superPlug(line)+"\n");
						bwriter.write(line+"\n");
					}
					if(hidden.size() > 0){
						bwriter.write("<!-- \n");
						for (String hid : hidden) {
							bwriter.write(hid+"\n");
						}
						bwriter.write(" -->\n");
					}
					//bwriter.write(page.toString());
					bwriter.write(getFoot());
					bwriter.close();
					out_state.touch();
					log.info(" -> done");
					log.info("took "+ (System.currentTimeMillis() - _start) + " ms");
				}catch (IOException ioe) {
					log.fail("error while baking: " + ioe);
				}
			}
		}
	}

	public void buildCode(String text){
		if(out_state.pure){
			try{
				page.add(wrap("pre",
						Utils.urlDecodeText((URLEncoder.encode(text, "ISO-8859-1")))
				)+"\n");
			}catch(UnsupportedEncodingException uee){
				log.fail("PageParser: unsupported encoding:"+uee);
			}	
		}else{
			log.fail("state is not pure!");
		}
	}

	void buildTag(String tag){
		if(out_state.pure){
			build(alku(tag));
		}else{
			log.fail("state is not pure!");
		}
	}

	void buildEnd(String tag){
		if(out_state.pure){
			build(loppu(tag));
		}else{
			log.fail("state is not pure!");
		}

	}

	public void build(String text){
		page.add(text);
	}

	private String getFoot(){
		return "</body>\n</html>\n";
	}

	private String genHead(){
		StringBuilder buf = new StringBuilder();

		buf.append(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"+
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
		);

		buf.append(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"fi\">\n" +
				"<head><title> "+ docTitle +" </title>\n");
		for (String tag : headTags) {
			buf.append(tag+"\n");
		}
		if(styles.size()>0){
			buf.append("<style type=\"text/css\">");
			for (String style : styles){
				buf.append(style);
			}
			buf.append("</style>\n");
		}
		buf.append("</head>\n");
		return buf.toString();
	}

	public void addStyle(String style){
		styles.add(style);
	}


	public void setCookie(String cookie_hook, String data) {
		response.addCookie(
				"Set-cookie: "+cookie_hook+"="+data+"; path="+script.substring(script.indexOf('/',8))+"; HttpOnly"
		);
	}

	// sisällytettyjä html-tageja "staattisesti" tuottava funktio
	// esmes: <html><body></br></body></html> saadaan kutsumalla
	//        wrap("html", wrap("body", "</br>"));
	public static String wrap(String body, String core) {
		return alku(body) + core + loppu(body);
	}
	private static String alku(String defi) {
		return "<" + defi + ">";
	}
	private static String loppu(String defi) {
		int end = defi.indexOf(' ');
		return "</" + ((end == -1) ? defi : defi.substring(0, end )) + ">";
	}

	public void addMessage(String message) {
		messages.add(message);
	}
	public void addHidden(String message) {
		hidden.add(message);
	}

	public void build(Page page) {
		setTitle(page.getTitle());
		for(String htag : page.getHeadTags()){
			addHeadTag(htag);
		}
		build(page.getBody());
	}

	public void clear() {
		headTags = new ArrayList<String>();
		styles = new ArrayList<String>();
		page = new ArrayList<String>();
		messages = new ArrayList<String>();
		hidden = new ArrayList<String>();		
	}

	public void setRedirect(String location_url){
		redirect = true;
		redirect_location = location_url;
	}

	public void clearRedirect(){
		redirect = false;
		redirect_location = "";
	}

	public void rawSend(String data) {
		log.info("raw sending");
		if(data == null){
			log.fail("raw send data == null");
			return;
		}
		if(out_state.pure){
			try{
				BufferedWriter bout = new BufferedWriter(
						new OutputStreamWriter(System.out,"ISO-8859-1"));
				if(response.cookieSet()){
					bout.write(response.getCookie());
				}
				bout.write(data);
				bout.close();
				out_state.touch();
			}catch (IOException ioe) {
				log.fail("error while sending raw data: " + ioe);
			}
		}
	}
}



