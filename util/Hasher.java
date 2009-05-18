package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *  cms:n swichi '--hash' käyttää tätä jostain syystä.
 *  ehkä vois laittaa sen käyttään tätä myös tuossa
 *  lähetetyn salasanan hashaamisessa, jonka se tällä
 *  hetkellä tekee itse
 *   
 * @author Valtte
 */

public class Hasher {

	public static String getSalt(){
		Random rand = new Random(System.nanoTime());
		return Long.toString(rand.nextLong());
	}
	
	public static String hashWithSalt(final String input, final String salt){
		String legacy = hash(input);
		
		for(int i = 0; i < 4; i++){
			legacy = hash(legacy+salt);
		}
		return legacy;
	}
	
	public static String legacyHash(String legacy, final String salt){
		for(int i = 0; i < 4; i++){
			legacy = hash(legacy+salt);
		}
		return legacy;
	}
	
	private static String hash(final String input){
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			return convertHash(md.digest(input.getBytes()));

		} catch (NoSuchAlgorithmException e) {
			System.err.println("can't get message digest: "+e);
		}
		return null;
	}

	private static String convertHash(final byte[] digest) {
		final StringBuilder sb = new StringBuilder();
		//stupid mess
		for (byte b : digest) {
			String temp = Integer.toHexString((int)b);
			int length = temp.length();
			if(length==2){
				sb.append(temp);
			}else if(length > 2){
				sb.append(temp.substring(length-2, length));
			}else if(length == 1){
				sb.append("0").append(temp);
			}
		}
		return sb.toString();
	}
}




