package cms.ext;

import java.util.ArrayList;

import cms.DataRelay;
import cms.Mailer;
import cms.PageBuilder;
import d2o.FlushingDb;
import util.ActionLog;

public class Help {

	FlushingDb rukousdb;

	private static final String source = "rukousdb";

	public Help(DataRelay datarelay){
		final PageBuilder pagebuilder = datarelay.pagebuilder;


		if(datarelay.post != null &&
				(
						datarelay.post.containsKey("rukousaihe")
				)
		){
			RukousRecord record = new RukousRecord(System.currentTimeMillis(),stripNewlines(datarelay.post.get("rukousaihe")));

			//create an entry in the rukous database
			rukousdb = new FlushingDb(source);

			if(!rukousdb.put(record.time, record.toArray())){
				pagebuilder.addMessage("db error");

			}else{

				ActionLog.log("rukouspyyntö vastaanotettu");
				pagebuilder.setRedirect("http://www.students.tut.fi/~tkrt/rukous_lahetetty.shtml");
			}
		}else{
			pagebuilder.setRedirect("http://www.students.tut.fi/~tkrt/rukous.shtml");
		}



		/*
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
		 */
		pagebuilder.bake();
	}

	private String stripNewlines(String string) {
		return string.replace("\r", ";").replace("\n", ";"); 
	}

	public boolean blind_send() {
		// compose the rukous mail

		//  read records
		FlushingDb rukousdb = new FlushingDb("rukousdb");

		ArrayList<RukousRecord> censor_list = new ArrayList<RukousRecord>();
		ArrayList<RukousRecord> requests = new ArrayList<RukousRecord>();
		ArrayList<RukousRecord> remove_list= new ArrayList<RukousRecord>();

		for(String[] data : rukousdb.all()){
			RukousRecord record = new RukousRecord(data);
			record.text = record.text.trim();
			if(record.censored){
				censor_list.add(record);
			}else{
				requests.add(record);
			}
		}

		//  sensure
		for(RukousRecord record : requests){
			for(RukousRecord cencor : censor_list){	
				if(record.text.equalsIgnoreCase(cencor.text)){
					remove_list.add(record);
					break;
				}
			}
		}

		requests.removeAll(remove_list);

		// send the rukous mail to hallitus list
		StringBuffer viesti = new StringBuffer();
		viesti.append("Viikon rukouspyynnöt:\n");
		viesti.append("\n-----\n");
		for(RukousRecord record : requests){
			viesti.append(record.text);
			viesti.append("\n\n-----\n\n");
		}

		viesti.append("\n-----\n\n");

		ActionLog.log("Rukouspyynnöt");
		//Mailer mailer = new Mailer();
		//ActionLog.log(Mailer.sendMail("TKrT rukouspalvelu", "tkrt-hallitus@tut.fi", "TKrT:n rukouspalvelun rukouspyynnöt", viesti));
		ActionLog.log(Mailer.sendMail("TKrT rukouspalvelu", "valtteri.kortesmaa@tut.fi", "TKrT:n rukouspalvelun rukouspyynnöt", viesti.toString()));		

		// remove old requests from db
		//  or add only the censor_list
		rukousdb.rst(true);
		for(RukousRecord record : censor_list){
			rukousdb.put(record.time, record.toArray());
		}

		return true;
	}
}
