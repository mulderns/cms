package cms.ext;

import cms.DataRelay;
import cms.Mailer;
import cms.PageBuilder;
import util.ActionLog;

public class Help {
	public Help(DataRelay datarelay){
		final PageBuilder pagebuilder = datarelay.pagebuilder;

		if(datarelay.post != null &&
				(
						datarelay.post.containsKey("nimi") &&
						datarelay.post.containsKey("yhteys") &&
						datarelay.post.containsKey("pyynto")
				)
		){
			final String viesti = 
				datarelay.post.get("nimi") + "\n  " +
				datarelay.post.get("yhteys") + "\n  " +
				datarelay.post.get("pyynto") + "\n  " +
				"\n-----\n\n";
			ActionLog.log("apua");
			//Mailer mailer = new Mailer();
			ActionLog.log(Mailer.sendMail("apua", "markus.karjalainen@tut.fi", "apua-pyyntö", viesti));
			//Mailer.sendMail("apua", "valtteri.kortesmaa@tut.fi", "apua-pyyntö", viesti);
			pagebuilder.setRedirect("http://www.students.tut.fi/~tkrt/apua_lahetetty.shtml");
		}else{
			pagebuilder.setRedirect("http://www.students.tut.fi/~tkrt/apua_apua.shtml");
		}

		pagebuilder.bake();
	}
}
