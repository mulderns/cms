package util;

/**
 * tää on tämmönen missä vois olla tommosia epäoleellisia
 * tiedon palasia, jos jaksaa päivittää. 
 * 
 * versio on ainakin ihan keinotekoinnen :)
 * 
 * @author Administrator
 *
 */

public class ProgInfo {

	public static final String prg_ver = "0.1.11";
	public static final String prg_date = "20.09.2008";
	public static final String prg_info = 
		"\nCommon Gateway Interface Content Management system for" +
		" TKrT-webpages.";
	public static final String prg_help = 
		"usage: java -jar Cgicms.jar [SWITCHES]\n" +
		"\n" +

		"[SWITCHES]\n" +
		" --conf            use to create properties.xml so the program can run\n" +
		" --check           does a check of files and folders or something...\n" +
		" --deploy          helps in deploying cgicms to server by writing launch\n" +
		"                   scripts, dirstructures, configuring, and bootstrapping\n" +
		"                   root account\n" +
		" --hash %1         outputs a SHA-1 hash of %1\n" +
		" --help            displays this help\n" +
		" --serve           the main purpose of the program, invoke from a script;\n" +
		"                   expects cgi-environment\n" +
		" --servefile       fetches a file from filehive (not implemented)\n" +


		"Examles:\n" +
		"\n" +
		"java -jar Cgicms.jar --deploy\n"+

		"";
}
