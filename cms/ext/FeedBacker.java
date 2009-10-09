package cms.ext;

import cms.DataRelay;
import cms.Mailer;
import cms.PageBuilder;
/*import util.ActionLog;
import util.Utils;*/

public class FeedBacker {
	public FeedBacker(DataRelay datarelay){
		final PageBuilder pagebuilder = datarelay.pagebuilder;

		pagebuilder.setTitle("Tampereen kristityt teekkarit - palaute");
		pagebuilder.addHeadTag(
				"<link rel=\"stylesheet\" href=\"" +
				"http://www.students.tut.fi/~tkrt/" +
				"style.css\" type=\"text/css\" />"
		);
		pagebuilder.addHeadTag(
				"<link rel=\"shortcut icon\" href=\"http://www.students.tut.fi/~tkrt/res/favicon.png\"/>"
		);
		pagebuilder.build(
				"<div class=\"wrapper\">" +
				"<div class=\"topbar\">" +
				"	<div class=\"center\">" +
				"	<a href=\"http://www.students.tut.fi/~tkrt/index.shtml\"><img class=\"logo\" src=\"http://www.students.tut.fi/~tkrt/res/tkrt_logo-2.png\" width=\"100\" height=\"100\" alt=\"TKrT\" /></a>" +
				"		<div class=\"navi\">" +
				"		" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/index.shtml\">nyt</a>" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/aina.shtml\">aina</a>" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/toiminta.shtml\">tkrt</a>" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/naamat.shtml\">naamat</a>" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/rukous.shtml\">rukous</a>" +
				"			<a href=\"http://www.students.tut.fi/~tkrt/eng/index.shtml\" style=\"margin-left:20px;margin-right:0px;\">english</a>" +
				"			" +
				"		</div>" +
				"	</div>" +
				"</div>" +
				"<div class=\"location\">"+
				"<div class=\"center\">" +
				"Palaute" +
				"</div>" +
				"</div>" +
				"<div class=\"content\">" +
				"<div class=\"center\">" +
				"<div class=\"mono\">"
		);

		if(datarelay.post != null &&
				(
						datarelay.post.containsKey("sivu") ||
						datarelay.post.containsKey("palaute_teksti")
				)
		){
			final String palaute = 
				(datarelay.post.containsKey("sivu") ? datarelay.post.get("sivu"):"general") +
				"\n  " +
				(datarelay.post.containsKey("palaute_teksti") ? datarelay.post.get("palaute_teksti"): "no feedback") +
				"\n-----\n\n";

			Mailer.sendMail("Cms", "valtteri.kortesmaa@tut.fi", "[Cms] palautetta", palaute);
			/*
			if(!Utils.appendFile("logbooks/feedback.txt",palaute)){
				ActionLog.error("appending failed");
				ActionLog.log("feedb - " + datarelay.post.get("sivu"));
				ActionLog.log("feedb + " + datarelay.post.get("palaute_teksti"));
			}*/

			pagebuilder.build("<h4>Kiitos palautteesta!</h4>");


		}else{

			pagebuilder.build(
					"<h1>Anna palautetta sivuista</h1>"	+
					"<p>" +
					"Voit antaa palautetta koko sivustosta tai tietystä sivusta." +
					"</p>"		
			);

			pagebuilder.build(
					"<form method=\"post\" action=\"http://www.students.tut.fi/cgi-bin/cgiwrap/tkrt/palaute.cgi\" name=\"palaute\">"
			);

			String referer;
			if((referer = System.getenv("HTTP_REFERER")) == null){
				referer = " ";
			}

			pagebuilder.build(
					"<label for=\"sivu\">Sivu: </label>" +
					"<input type=\"text\" name=\"sivu\" value=\""+referer+"\" size=\"60\"/>"
			);

			pagebuilder.build(
					"<textarea rows=\"12\" cols=\"50\" name=\"palaute_teksti\"></textarea>" +
					"<br /><br />" +
					"<input value=\"L&auml;het&auml;\" type=\"submit\" />" +
					"</form>"
			);
		}

		pagebuilder.build(
				"</div>"+
				"</div>"+
				"</div>"+
				"</div>" +
				"<div class=\"footer\">" +
				"<p>Copyright: TKrT &nbsp;&nbsp;&nbsp;" +
				"<a href=\"http://www.students.tut.fi/cgi-bin/cgiwrap/kortesmv/palaute.cgi\">&#187;Anna palautetta</a>" +
				"</p>" +
				"</div>" 
		);

		pagebuilder.bake();
	}
}
