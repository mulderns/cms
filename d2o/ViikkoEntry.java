package d2o;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;

import util.Csv;
import util.Logger;
import util.Utils;

public class ViikkoEntry implements Comparable<ViikkoEntry>{
	public String otsikko, paiva, vuosi, paikka;
	public ArrayList<String> yhteenveto;
	public ArrayList<String> teksti;

	public boolean mailiin;
	public boolean etukateen;
	public boolean pohjaksi;
	public boolean auto;
	public boolean enabled;

	Logger log;

	public String user;
	String jatto_aika;
	public String id;

	// 2008.08.13,14:30,Otsikko,paikka,aika,yhteenveto,teksti
	public int year;
	public int month;
	public int day;	
	public int hour;
	public int minute;

	public int week;
	//public String day_name

	public ViikkoEntry(String otsikko, String username) {
		log = new Logger("ViikkoEntry");
		this.otsikko = otsikko;
		//aika = "";
		paikka = "";
		yhteenveto = new ArrayList<String>();
		teksti = new ArrayList<String>();

		mailiin = false;
		etukateen = false;
		enabled = false;

		user = username;

		Calendar k = Calendar.getInstance();
		jatto_aika = 
			Utils.addLeading(k.get(Calendar.MONTH)+1,2) +
			Utils.addLeading(k.get(Calendar.DATE),2) +
			Utils.addLeading(k.get(Calendar.HOUR_OF_DAY),2) +
			Utils.addLeading(k.get(Calendar.MINUTE),2) +
			Utils.addLeading(k.get(Calendar.SECOND),2);
	}

	public ViikkoEntry(String otsikko, String username, String jatto) {
		log = new Logger("ViikkoEntry");
		this.otsikko = otsikko;
		//aika = "";
		paikka = "";
		yhteenveto = new ArrayList<String>();
		teksti = new ArrayList<String>();

		mailiin = false;
		etukateen = false;
		enabled = false;

		user = username;

		jatto_aika = jatto;
	}

	public ViikkoEntry(String[] fields) {
		log = new Logger("ViikkoEntry");
		id = fields[0];
		year = Integer.parseInt(fields[1]);
		month = Integer.parseInt(fields[2]);
		day = Integer.parseInt(fields[3]);
		//log.severe(" day["+day+"]");
		hour = Integer.parseInt(fields[4]);
		minute = Integer.parseInt(fields[5]);
		mailiin = Boolean.parseBoolean(fields[6]);
		etukateen = Boolean.parseBoolean(fields[7]);
		otsikko = fields[8];
		paikka = fields[9];
		yhteenveto = new ArrayList<String>();
		teksti = new ArrayList<String>();

		complete();

		for(int i = 10 ; i < fields.length; i++){
			if(fields[i].startsWith("y]")){
				yhteenveto.add(fields[i].substring(2));
			}else if(fields[i].startsWith("t]")){
				teksti.add(fields[i].substring(2));
			}else if(fields[i].startsWith("e]")){
				enabled=true;
			}else{
				log.fail("weeirds");
				break;
			}
		}
		/*if(yhteenveto.size()==0)
			yhteenveto.add(""); //??*/
	}

	private void complete() {
		if(id != null){
			String[] ids = id.split("-");
			if(ids.length != 3){
				return;
			}
			jatto_aika = ids[1];
			user= ids[2];
			if(ids[0].equals("automatic")){
				auto = true;
				//log.severe("+auto["+auto+"] day["+day+"]");

			}else if(ids[0].equals("template")){
				pohjaksi = true;
			}
		}

		Calendar experim = Calendar.getInstance();
		experim.setFirstDayOfWeek(Calendar.MONDAY);
		experim.set(year,month-1,day);
		week = experim.get(Calendar.WEEK_OF_YEAR);
	}

	public void genAutoWeek(int offset){
		if(auto){
			Calendar experim = Calendar.getInstance();
			experim.setFirstDayOfWeek(Calendar.MONDAY);
			experim.add(Calendar.WEEK_OF_YEAR, offset);
			experim.set(Calendar.DAY_OF_WEEK, day);
			day = experim.get(Calendar.DAY_OF_MONTH);
			month = experim.get(Calendar.MONTH)+1;
			year = experim.get(Calendar.YEAR);
			auto = false;
		}
	}

	public String setKuukausi(String kuukausi){
		kuukausi = kuukausi.trim();
		if(kuukausi.length()==0){
			return "tapahtuma ei sisällä kuukautta";
		}

		try{
			month = Integer.parseInt(kuukausi);
		}catch(NumberFormatException nfe){
			log.fail("month nfe:"+nfe);
			return "kuukautta ei pystytty tulkitsemaan numeroksi "+ nfe;
		}
		return null;
	}

	public String setPaiva(String paiva) {
		//System.err.println("#### viikkoentry ["+paiva+"]");
		paiva = paiva.trim();
		if(paiva.length()==0){
			return "tapahtuma ei sisällä päivää";
		}

		try{
			day = Integer.parseInt(paiva);
		}catch(NumberFormatException nfe){
			log.fail("day1 nfe:"+nfe);
			return "päivää ei pystytty tulkitsemaan numeroksi " + nfe;
		}


		Calendar experim = Calendar.getInstance();
		int curm = experim.get(Calendar.MONTH)+1;
		int curd = experim.get(Calendar.DAY_OF_MONTH);
		experim.clear();
		experim.setLenient(false);
		experim.setFirstDayOfWeek(Calendar.MONDAY);
		try{
			experim.set(year, month-1, day);

			day = experim.get(Calendar.DAY_OF_MONTH);
			month = experim.get(Calendar.MONTH)+1;

			year = experim.get(Calendar.YEAR);
		}catch(Exception e){
			log.fail("exception while validating fields d["+day+"] m["+month+"] y["+year+"] : \n"+e);
			return "virheellinen päivämäärä (ehkä menneisyydessä)";
		}
		log.info("["+month+"]<["+curm+"]   ["+day+"]<["+curd+"]");
		if(	month < curm ||	(month == curm && day < curd)) {
			return "tapahtumia ei voi lisätä menneisyyteen";
		}

		if(auto){
			day = experim.get(Calendar.DAY_OF_WEEK);
		}else{
		}

		return null;
	}

	public String setVuosi(String vuosi) {
		try{
			year = Integer.parseInt(vuosi);
		}catch(NumberFormatException nfe){
			log.fail("year nfe:"+nfe);
			return "vuotta ei pystytty tulkitsemaan numeroksi "+nfe;
		}
		int year_now = Calendar.getInstance().get(Calendar.YEAR);
		if(year < year_now)
			return "tapahtumia ei voi lisätä menneisyyteen";
		return null;
	}

	public String setAika(String aika){
		//14:20
		//15.60
		//14,56
		if(aika.length()>0){
			String[] temp = new String[0];

			if(aika.indexOf(':')!= -1){
				temp = aika.split(":");
			}else if(aika.indexOf('.')!= -1){
				temp = aika.split("\\.");
			}else if(aika.indexOf(',')!= -1){
				temp = aika.split(",");
			}
			if(temp.length != 2){
				return "tarkista ajan formaatti hh:mm";
			}
			try{
				hour = Integer.parseInt(temp[0]);
				minute = Integer.parseInt(temp[1]);
			}catch(NumberFormatException nfe){
				log.fail("time nfe:"+nfe);
				return "kellon aikaa ei pystytty tulkitsemaan numeroksi " + nfe;
			}
			if(hour < 0 || hour > 23 || minute < 0 || minute > 59 )
				return "kellon ajan täytyy olla väliltä 00:00 - 23:59";
		}
		return null;

		/**	this.aika = aika;
	log.info("setting aika ["+aika+"]");
		//pp.kk.vvvv,tt:mm
		String[] fields = aika.split(",");
		if(fields.length != 2)
			return false;
	log.info("1");
		String[] date = fields[0].split("\\.");
		if(date.length != 3)
			return false;
	log.info("2");
		String[] time = fields[1].split(":");
		if(date.length != 2)
			return false;
	log.info("3");
		try{
			day = Integer.parseInt(date[0]);
			month = Integer.parseInt(date[1])-1;
			year = Integer.parseInt(date[2]);

			hour = Integer.parseInt(time[0]);
			minute = Integer.parseInt(date[1]);
		}catch(NumberFormatException nfe){
		log.severe("nfe:"+nfe);
			return false;
		}
	log.info("4");

		Calendar experim = Calendar.getInstance();
		experim.clear();
		experim.setLenient(false);
		try{
			experim.set(year, month, day, hour, minute);
			day = experim.get(Calendar.DAY_OF_MONTH);
			month = experim.get(Calendar.MONTH)+1;
			year = experim.get(Calendar.YEAR);

			hour = experim.get(Calendar.MINUTE);
			minute = experim.get(Calendar.HOUR);
		}catch(Exception e){
		log.severe("exception while validating fields");
			return false;
		}
	log.info("5");
		return true;

		 */
	}

	public void setPaikka(String paikka){
		this.paikka = paikka;
	}

	public String setPosti(String teksti) {
		if(teksti == null)
			return null;

		BufferedReader bstr = new BufferedReader(new StringReader(teksti));
		String line; 

		try{
			while((line = bstr.readLine()) != null){
				log.info("teksti line");
				this.teksti.add(line);
			}
			bstr.close();
		}catch(IOException ioe){
			log.info("ioe:"+ioe);
			return "io exception:"+ioe;
		}

		return null;
	}

	public String setLisa(String teksti) {
		if(teksti == null)
			return null;

		BufferedReader bstr = new BufferedReader(new StringReader(teksti));
		String line;

		try{
			while((line = bstr.readLine()) != null){
				if(line.trim().length() > 0){
					yhteenveto.add(line);
					log.info("yhteenveto line");
				}else{
					log.info("empty line");
				}
			}
			bstr.close();
		}catch(IOException ioe){
			log.info("ioe:"+ioe);
			return "io exception:"+ioe;
		}
		return null;
	}

	/*public String setTeksti(String teksti) {
		//leipäteksti 500-600 merkkiä (spaces included)
		if(teksti == null)
			return null;
		BufferedReader bstr = new BufferedReader(new StringReader(teksti));
		String line;
		//int charcount = 0;
		try{
			while((line = bstr.readLine()) != null){
				if(line.trim().length() > 0){
					yhteenveto.add(line);
					log.info("yhteenveto line");
				}else{
					log.info("empty line");
					break;
				}
			}
			while((line = bstr.readLine()) != null){
				//charcount += line.length();
				log.info("teksti line");
				this.teksti.add(line);
			}
			bstr.close();
		}catch(IOException ioe){
			log.info("ioe:"+ioe);
			return "io exception:"+ioe;
		}

		return null;
	}*/

	public int getTekstiCharCount(){
		int count = 0;
		for(String s : teksti){
			count += s.length();
		}
		return count;
	}

	private void genId(){
		// jatto + käyttaja? ehkä
		// paiva + jatto? 
		// paiva + käyttaja?

		// so the id should be something that can be used to easily find the entry 
		// and it should be unique. 

		//that would mean paiva + unique somefing. 
		//ultimate would be paiva + jatto + kayttaja;

		//20080828-0824134130-mullis

		if(pohjaksi){
			id = "template-" + jatto_aika + "-" + user;
		}else if(auto){
			id = "automatic-" + jatto_aika +	"-" + user;
		}else{
			id =
				year + 
				Utils.addLeading(month,2) +
				Utils.addLeading(day,2) +
				"-" +
				jatto_aika +
				"-" +
				user;
		}
	}
	/*
	public String genTempId(){
		return "template-"+jatto_aika+"-"+user;
	}


	public String genAutoId() {
		return "automatic-"+jatto_aika+"-"+user;
	}
	 */
	public String[] fieldsToArray(){
		genId();
		ArrayList<String> stack = new ArrayList<String>() ;
		stack.add(id);
		stack.add(Integer.toString(year));
		stack.add(Utils.addLeading(month,2));
		stack.add(Utils.addLeading(day,2));
		stack.add(Utils.addLeading(hour,2));
		stack.add(Utils.addLeading(minute,2));
		stack.add(mailiin?"true":"false");
		stack.add(etukateen?"true":"false");
		stack.add(otsikko);
		stack.add(paikka);
		for(String yht : yhteenveto){
			stack.add("y]"+yht);
		}
		for(String teks : teksti){
			stack.add("t]"+teks);
		}
		if(enabled){
			stack.add("e]");
		}
		return stack.toArray(new String[0]);
	}

	public void setMailiin(boolean b) {
		mailiin = b;		
	}

	public void setEtukateen(boolean b) {
		etukateen = b;		
	}

	public void setPohjaksi(boolean b) {
		pohjaksi = b;		
	}
	public void setEnabled(boolean b) {
		enabled = b;		
	}
	public void setAuto(boolean b) {
		auto = b;		
		//log.severe(" auto["+auto+"]");
	}

	public int compareTo(ViikkoEntry o) {
		if(auto){
			if(o.auto){
				if(day > o.day){
					return 1;
				}
				if(day < o.day){
					return -1;
				}

				if(hour > o.hour)
					return 1;
				if(hour < o.hour)
					return -1;
				if(minute > o.minute)
					return 1;
				if(minute < o.minute)
					return -1;

				return otsikko.compareTo(o.otsikko);
			}
			return -1;
		}
		if(o.auto)
			return 1;
		if(year > o.year)
			return 1;
		if(year < o.year)
			return -1;
		if(month > o.month)
			return 1;
		if(month < o.month)
			return -1;
		if(day > o.day)
			return 1;
		if(day < o.day)
			return -1;
		if(hour > o.hour)
			return 1;
		if(hour < o.hour)
			return -1;
		if(minute > o.minute)
			return 1;
		if(minute < o.minute)
			return -1;
		return otsikko.compareTo(o.otsikko);
	}

	public String getDayName(){
		int dayofweek = -1;
		//log.severe("auto["+auto+"] day["+day+"]");
		if(auto){
			dayofweek = day;
		}else{
			Calendar experim = Calendar.getInstance();
			experim.clear();
			experim.setLenient(false);
			experim.setFirstDayOfWeek(Calendar.MONDAY);
			try {
				experim.set(year, month-1, day);
				dayofweek = experim.get(Calendar.DAY_OF_WEEK);
			}catch(Exception e){
				log.fail("exception while validating fields d["+day+"] m["+month+"] y["+year+"]");
				return " ";
			}
		}
		//log.severe("dayofweek["+dayofweek+"]");
		switch (dayofweek) {
		case Calendar.SUNDAY:
			return "Su";

		case Calendar.MONDAY:
			return "Ma";

		case Calendar.TUESDAY:
			return "Ti";

		case Calendar.WEDNESDAY:
			return "Ke";

		case Calendar.THURSDAY:
			return "To";

		case Calendar.FRIDAY:
			return "Pe";

		case Calendar.SATURDAY:
			return "La";

		default:
			return "--";
		}

	}

	public int getWeek(){
		return week;
	}

	public String toString(){
		return Csv.encode(fieldsToArray());
	}

}



