package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * t‰t‰ voi vissiin k‰ytt‰‰ k‰tev‰sti koodaamaan merkkej‰
 * niin ett‰ ne n‰kyy html sivulla normaalisti.
 * eli ett‰ '<' n‰kyy selaimen renderˆim‰ll‰ sivulla '<':na
 * 
 * @author Administrator
 *
 */

public final class Utils {

	public static final String mapToQueryString(HashMap<String, String> map){
		StringBuilder sb = new StringBuilder();
		Entry<String, String> entry;

		for (Iterator<Entry<String, String>> iter = map.entrySet().iterator(); iter.hasNext();){
			entry = iter.next();
			if(!entry.getKey().equals(""))
				sb.append(entry.getKey()+"="+entry.getValue()+"&");
		}

		return sb.toString();
	}


	public static String urlEncode(String text) {
		try{
			return URLEncoder.encode(text, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static String urlDecode(String text) {
		try{
			return URLDecoder.decode(text, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static String urlDecodeText(final String in){
		//StringBuilder out = new StringBuilder(in.length());
		final StringBuilder out = new StringBuilder();
		char ch;
		for(int i = 0 ; i < in.length(); i++){
			ch = in.charAt(i);
			if(ch == '+'){
				ch = ' ';
				out.append(ch);
			}else if (ch == '%'){
				out.append("&#");
				out.append(Integer.parseInt(in.substring(i+1,i+3), 16));
				out.append(';');
				i+=2;
			}else{
				out.append(ch);
			}
		}
		return new String(out);
	}

	public static String addLeading(final int source, final int digits) {
		final StringBuilder temp = new StringBuilder();
		final String base = Integer.toString(source);
		for(int i = base.length(); i < digits ; i++){
			temp.append("0");
		}
		temp.append(base);
		return temp.toString();
	}

	public static int countChar(final char c, final String raw) {
		int count = 0;
		for(char d : raw.toCharArray()){
			if(d == c)
				count++;
		}
		return count;
	}

	//@SuppressWarnings("static-access")
	public static boolean sleep(final long time){
		try {
			//Thread.currentThread();
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	public static boolean appendFile(String fileName, String content){
		try{
			File output = new File(fileName);
			BufferedOutputStream bos = 
				new BufferedOutputStream(new FileOutputStream(output, true));
			char[] chars = content.toCharArray();
			for(char c : chars){
				bos.write((int)c);
			}
			bos.close();
		}catch (FileNotFoundException fnfe){
			return false;
		}catch (IOException ioe){
			return false;
		}
		return true;
	}

	public static String reverseLines(String lines) {
		String[] reverse = lines.split("\n");
		StringBuilder sb = new StringBuilder();
		for(int i = reverse.length - 1; i >= 0; i--){
			sb.append(reverse[i]).append("<br/>");
		}
		return sb.toString();
	}

	public static String deNormalize(String field) {
		try{
			return urlDecodeText(URLEncoder.encode(field, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
		}
		return "denom";
	}

	public static String[] reverseLines(String[] lines) {
		String[] reverse = new String[lines.length];
		final int length = lines.length;
		final int temp = length-1;
		for(int i = 0; i < length; i++){
			reverse[i] = lines[temp - i];
		}
		return reverse;
	}

	public static String longTimeToString(long time){

		long vuosi = 31536000000L;
		long kuukausi = 2592000000L;
		long paiva = 86400000L;
		long tunti = 3600000L;
		long minuutti = 60000;
		long sekuntti = 1000;


		long years=0, months=0, days=0, hours=0, minutes=0, seconds=0;

		while(time >= vuosi){
			time -= vuosi;
			years++;
		}
		while(time >= kuukausi){
			time -= kuukausi;
			months++;
		}
		while(time >= paiva){
			time -= paiva;
			days++;
		}
		while(time >= tunti){
			time -= tunti;
			hours++;
		}
		while(time >= minuutti){
			time -= minuutti;
			minutes++;
		}
		while(time >= sekuntti){
			time -= sekuntti;
			seconds++;
		}

		StringBuilder sb = new StringBuilder();
		if(years > 0)
			sb.append(years+"y ");
		if(months > 0)
			sb.append(months+"m ");
		if(days > 0)
			sb.append(days+"d ");
		if(hours > 0)
			sb.append(hours+"h ");
		if(minutes > 0)
			sb.append(minutes+"min ");
		if(seconds > 0)
			sb.append(seconds+"s ");
		if(time > 0){
			sb.append(time+"ms");
		}

		return sb.toString();
	}

	public static String nanoTimeToString(long time){
		long micro = 1000L;
		long milli = 1000000L;
		long sec = 1000000000L;

		long secs=0, millis=0, micros=0;

		while(time >= sec){
			time -= sec;
			secs++;
		}

		while(time >= milli){
			time -= milli;
			millis++;
		}

		while(time >= micro){
			time -= micro;
			micros++;
		}

		StringBuilder sb = new StringBuilder();
		if(secs > 0)
			sb.append(secs+"s ");
		if(millis > 0)
			sb.append(millis+"ms ");
		if(micros > 0)
			sb.append(micros+"us ");
		if(time > 0){
			sb.append(time+"ns");
		}

		return sb.toString();
	}


	public static final String genSpace(int i) {
		if(i <= 0)
			return "";
		if(i >= 500)
			return "";
		StringBuilder sb = new StringBuilder(i);
		while(i-- > 0)
			sb.append(" ");
		return sb.toString();
	}


	public static String removeBrakes(String data) {
		if(data == null)
			return null;

		StringBuilder sb = new StringBuilder();

		int last = 0;
		for(int i = 0 ; i < data.length(); i++){
			if(
					data.charAt(i) == '\n' || 
					data.charAt(i) == '\r' ||
					data.charAt(i) == '\f' 
			){
				sb.append(data.substring(last, i)).append(" ");
				last = i+1;
			}

			if(last < data.length()-1)
				sb.append(data.substring(last));
		}
		return sb.toString();
	}
}