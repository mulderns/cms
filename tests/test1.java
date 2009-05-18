package tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class test1 {

	@Test
	public void testDecodeSimple() {
		//fail("Not yet implemented");
		String[] expected = {
				"hei",
				"he i",
				" moi",
				" mo i",
				" moi ",
				" mo i ",
				"hei,",
				",",
				"h'ei'"
		};
		
		String input = "hei,\"he i\",\" moi\",\" mo i\",\" moi \",\" mo i \",\"hei,\",\",\",\"h'ei'\"";
		String[] result = util.Csv.decode(input);
		assertTrue(expected.equals(result));
	}
	
	
	@Test
	public void testDecode() {
		//fail("Not yet implemented");
		String[] expected = {
				"hei",
				"he i",
				" moi",
				" mo i",
				" moi ",
				" mo i ",
				"hei,",
				",",
				"h\"ei\"",
				"h'ei'"
		};
		
		String input = "hei,\"he i\",\" moi\",\" mo i\",\" moi \",\" mo i \",\"hei,\",\",\",\"h''ei''\",\"h'ei'\"";
		String[] result = util.Csv.decode(input);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testEncodeStringArray() {
		//fail("Not yet implemented");
		String[] input = {
				"hei",
				"he i",
				" moi",
				" mo i",
				" moi ",
				" mo i ",
				"hei,",
				",",
				"h\"ei\"",
				"h'ei'"
		};
		
		String expected = "hei,\"he i\",\" moi\",\" mo i\",\" moi \",\" mo i \",\"hei,\",\",\",\"h''ei''\",\"h'ei'\"";
		String result = util.Csv.encode(input);
		assertTrue(expected.equals(result));
	}

	@Test 
	public void loopbackTest() {
		String[] input = {
				"hei",
				"",
				"hei",
				" ",
				"hei",
				"he i",
				" moi",
				" mo i",
				" moi ",
				" mo i ",
				"hei,",
				",",
				"h\"ei\"",
				" hei \"sinä\"",
				" hei \"sinä\" minä",
				"hei\" "
		};
		long start = System.nanoTime();
		String[] result = util.Csv.decode(util.Csv.encode(input));
		System.err.println(util.Utils.nanoTimeToString(System.nanoTime()-start));
		//assertEquals(input.length, result.length);
		System.err.println(util.Csv.encode(input));
		System.err.println(Arrays.toString(result));
		assertArrayEquals(input, result);
	}
}
