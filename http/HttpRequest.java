package http;

import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import cms.DataRelay;

import util.Logger;
//import util.Utils;

/**
 * t‰‰ on semmonen oli joka k‰sittelee querystringin ja 'post':n 
 * sek‰ cookien.
 * 
 * voi olla jotain j‰mi‰ jostain prototyyppi ajoilta joilla ei tee
 * mit‰‰n.
 * 
 * queryn prosessointiin ja varastointiin k‰ytet‰‰n Query-luokkaa
 * postilla on kans samanlainen mutta sen prosessointi hoidetaan
 * vissiin t‰ss‰ suoraa ja vasta varastoidaan sitten itse olioon
 * 
 * 
 * @author Valtteri
 *
 */

public class HttpRequest {
	private Logger log;

	public enum req_type {
		post,
		get,
		unknown
	}

	private Map<String,String> env;

	private Query query;
	private Post post;
	private HashMap<String,String> header;
	private HttpCookie cookie;

	private int contentLength;
	//private String contentType;
	private req_type request_method;
	private boolean multipart;

	private DataRelay datarelay;

	public HttpRequest(DataRelay datarelay){
		log = new Logger("Request");
		this.datarelay = datarelay;
		env = datarelay.env;

		header = new HashMap<String, String>();
		post = new Post();

		multipart = false;

		if(processQuery(env)){
			datarelay.query = query.toHashMap();
			//processPathInfo();
			if(env.containsKey("CONTENT_LENGTH")){
				log.info("contains contentl..");
				try {
					contentLength = Integer.parseInt(env.get("CONTENT_LENGTH"));
				} catch (NumberFormatException nfe){
					log.fail("nfe parsing contentlength:"+nfe);
					contentLength = -1;
				}

			}
			if(env.containsKey("REQUEST_METHOD")){
				log.info("contains reqmeth..");
				request_method = parseReq_type(env.get("REQUEST_METHOD"));
				if(request_method == req_type.post){
					processPost();
					datarelay.multipart = multipart;
					if(!multipart){
						datarelay.post = post.toHashMap();
					}else{
						datarelay.post = new HashMap<String, String>();
					}
				}
			}
			if(env.containsKey("HTTP_COOKIE")){
				log.info("contains cookie..");
				processCookie(env);
				datarelay.cookie = cookie.toHashMap();
			}
		}

	}

	public req_type parseReq_type(String type){
		log.info("parsing request type;");
		if(type.equalsIgnoreCase("post")){
			log.info(" -> post");
			return req_type.post;
		}
		if(type.equalsIgnoreCase("get")){
			log.info(" -> get");
			return req_type.get;
		}
		log.info(" -> unknown");
		return req_type.unknown;
	}

	public boolean hasCookie(){
		log.info("canHasCookie: "+(cookie != null));
		return cookie != null;
	}

	public HttpCookie getCookie(){
		return cookie;
	}

	private void processCookie(Map<String,String> env){
		log.info("mapping cookie");
		cookie = new HttpCookie(env.get("HTTP_COOKIE"), log);
	}

	/** looks for stuff in the $query_string
	 *	creates a Query object to hold the stuff 
	 */
	private boolean processQuery(Map<String,String> env){
		log.info("processing query");
		if(env.containsKey("QUERY_STRING")) {
			log.info(" -> contains querys..");
			query = new Query(env.get("QUERY_STRING"), log);
			return true;
		}
		log.info(" -> no query_sting");
		return false;
	}

	private boolean processPost(){
		try{

			if(env.containsKey("CONTENT_TYPE") &&
					env.get("CONTENT_TYPE").toLowerCase().startsWith("multipart/form-data")){
				multipart = true;
				
			}else{
				final BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
				processNormal(bin);
				bin.close();
			}
			return true;

		}catch(IOException ioe){
			log.fail("post processing failed: "+ioe);
		}
		return false;
	}

	public void processMultiPart(){
		try{
			final BufferedReader bis = new BufferedReader(
					new InputStreamReader(System.in,"ISO-8859-1"));
			if(contentLength < 10000000){
				processMulti(bis);
			}
			bis.close();

			datarelay.post = post.toHashMap();
			datarelay.files = post.getFiles();

		}catch (IOException ioe) {
			log.fail("exception processing multipart post:"+ioe);
		}

	}

	private void processNormal(BufferedReader bin) throws IOException{
		for(String apu; (apu = bin.readLine()) != null;	){
			String[] pairs = apu.split("\\&");
			for(String pair : pairs) {

				String[] values = pair.split("=");
				if(values.length == 2){
					try{
						values[0] = URLDecoder.decode(values[0],"ISO-8859-1");
						values[1] = URLDecoder.decode(values[1],"ISO-8859-1");
						log.info(values[0]+" - "+ values[1]);
						post.addField(values[0],values[1]);
					}catch(UnsupportedEncodingException uee){
						log.fail(""+uee);
					}
				}else{
					log.info("uneven pair:" +pair);
					values[0] = URLDecoder.decode(values[0],"ISO-8859-1");
					post.addField(values[0],"");
					log.info(values[0]+" - ");
				}
			}
		}
	}

	//(old)the infamous multipart/form-data !not testet throughoutly
	//(old)causes some systematic error, but haven't pinpointed it yet 
	//-new implementation, no errors sofar
	private void processMulti(final BufferedReader bis) throws IOException {
		String line = env.get("CONTENT_TYPE");//getContentType();
		final String boundary = line.substring(line.lastIndexOf("boundary=") + 9);

		line = bis.readLine();
		if(line == null || !line.equals("--"+boundary)){
			log.fail("initial boundary not found["+line+"]");
			return;
		}
		FormPart part;
		while(true){
			part = new FormPart();

			//Content-Disposition
			line = bis.readLine();
			if(line == null || !(line.startsWith("Content-Disposition:"))){
				log.fail("Content-Disposition expected ["+line+"]");
				return;
			}
			final String[] fields = line.split(";");

			if(fields.length < 2){
				log.fail("what on earth is that ->["+line+"]");
				return;
			}
			part.contentDisposition = fields[0].substring(20).trim();

			//name
			if(fields[1].trim().startsWith("name")){
				part.setName(fields[1].split("=")[1].replace("\"","").trim()); //jepjep
				log.info(" name="+part.getName());
			}else{
				log.fail("not name["+fields[1]+"]");
				return;
			}

			//filename
			if(fields.length > 2){
				if(fields[2].trim().startsWith("filename")){
					part.setFilename(fields[2].split("=")[1].replace("\"","").trim()); //jepjep
					log.info("filename:"+part.getFilename());
				}else{
					log.fail("not filename["+fields[2]+"]");
					return;
				}
			}

			line = bis.readLine();
			if(line != null && line.length()>0){
				if(!line.startsWith("Content-Type")){
					log.fail("error in headers 2["+line+"]");
					return;
				}
				part.contentType = line.substring(13).trim();
				line = bis.readLine();
				if(line != null && line.length()>0){
					if(!line.startsWith("Content-Transfer-Encoding")){
						log.fail("error in headers 3["+line+"]");
						return;
					}
					part.contentEncoding = line.substring(26).trim();

					line = bis.readLine();
					if(line != null && line.length()>0){
						log.fail("unknown headers["+line+"]");
						return;
					}
				}
			}
			if(part.filename == null){
				part.value = bis.readLine();
				post.addPart(part);
				line = bis.readLine();
				if(line!=null && line.startsWith("--" +boundary)){
					if(line.equals("--" +boundary+"--")){
						return;
					}
					continue;
				}
			}else{
				log.info("file");

				byte[] bytes;
				bytes = readToBarrierBytes(bis,"--"+boundary);
				log.info("bytes read["+bytes.length+"]");
				if(bytes.length == 0){
					log.fail("error reading bytes from file contents");
					return;
				}

				part.bytes = bytes;
				post.addPart(part);

				line = bis.readLine();
				if(line != null){
					if(line.startsWith("--" +boundary)){
						continue;
					}else if(line.equals("--")){
						return;
					}else{
						log.fail("oddities["+line+"]");
						//return;
					}
				}else{
					return;
				}

			}
		}
	}

	private byte[] readToBarrierBytes(BufferedReader bis, String barrier) throws IOException {
		int i = 0;
		int[] iBarrier = new int[barrier.length()];

		for(char c : barrier.toCharArray())
			iBarrier[i++] = c;

		i = 0;
		int last;
		int size = 0;
		ByteBuffer bb = ByteBuffer.allocate(contentLength);
		while((last = bis.read()) > -1){
			bb.put((byte)last);
			size++;
			if(last == iBarrier[i]){
				if(i == iBarrier.length -1){
					byte[] result = new byte[size-iBarrier.length-2];//force truncate CRLF
					bb.rewind();
					bb.get(result);
					return result;
				}
				i++;
			}else{
				i = 0;
			}
		}

		return new byte[0];
	}

	public Query getQuery(){
		return query;
	}

	public String toString(){
		StringBuilder buff = new StringBuilder();
		buff.append("query:"+query+"\n");
		buff.append("cookie:"+cookie+"\n");
		buff.append("contentLength:"+contentLength+"\n");
		//buff.append("contentType:"+contentType+"\n");
		buff.append("request_method:"+request_method+"\n");

		buff.append("header:\n");
		for (String string : header.values()) {
			buff.append(" " + string + "\n");
		}
		return buff.toString();
	}
}

