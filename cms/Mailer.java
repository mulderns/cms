package cms;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class Mailer {

	public static String sendMail(String from, String to, String subject, String message){
		String sendmail = "/usr/sbin/sendmail -t";
		//String reply_to = "Reply-to: filtsu@hotmail.com\n";

		/**
		my $sendmail = "/usr/sbin/sendmail -t";
	 	my $reply_to = "Reply-to: foo\@bar.org\n";
		my $subject = "Subject: Confirmation of your submission\n";
		my $content = "Thanks for your submission.";
		my $to = $query->param('send_to')."\n";
		my $file = "subscribers.txt";

		open(SENDMAIL, "|$sendmail") or die "Cannot open $sendmail: $!";
		print SENDMAIL $reply_to;
		print SENDMAIL $subject;
		print SENDMAIL $send_to;
		print SENDMAIL "Content-type: text/plain\n\n";
		print SENDMAIL $content;
		close(SENDMAIL); 
		 */

		try{
			BufferedInputStream bin,berr;
			//BufferedOutputStream bout;
			BufferedWriter bout;
			
			//StringBuilder output = new StringBuilder();
			StringBuilder input = new StringBuilder();

			//input.append("Reply-to: " + reply_to + "\n");
			//input.append("From: TKrT tiedotus <tkrt@students.cc.tut.fi>\n");
			input.append("From: "+from+"\n");
			input.append("Subject: " + subject + "\n");
			input.append("To: " + to + "\n");
			input.append("Content-type: text/plain\n\n");
			input.append(message);

			long start = System.currentTimeMillis();
			Process mail = Runtime.getRuntime().exec(sendmail);
			//bout = new BufferedOutputStream(
			try{
			bout = new BufferedWriter(
					new OutputStreamWriter(
							mail.getOutputStream(),
							"ISO-8859-1"
					)
			);
			}catch(UnsupportedEncodingException uee){
				return "encoding not supported:"+uee;
			}
			bin = new BufferedInputStream(mail.getInputStream());
			berr = new BufferedInputStream(mail.getErrorStream());

			//int red;
			//while(true){
			bout.write(input.toString());
			//while((red = bin.read())!=-1){

			//}

			//}
			bout.close();

			int read;
			StringBuilder sbin = new StringBuilder();
			StringBuilder sberr = new StringBuilder();
			while((read = berr.read()) != -1){
				sberr.append((char)read);
			}
			while((read = bin.read()) != -1){
				sbin.append((char)read);
			}
			bin.close();
			berr.close();

			if(sberr.length()>0 || sbin.length()>0){
				FileOps.write(new File("../logbooks","main-err"), sbin.toString()+sberr.toString(), true);
				//fh.appendFile("main-err", sbin.toString()+sberr.toString());
			}
			while(System.currentTimeMillis() - start < 5000){
				try{
					if(mail.exitValue() != 0){
						return "failed: output["+sbin.toString()+"]\n error["+sberr.toString()+"]";
					}else{
						return null;
					}
				}catch(IllegalThreadStateException itse){
					
				}
			}
			mail.destroy();
			return "force killed after timeout (5 sec)";

		}catch(IOException ioe){
			return "ioe:"+ioe;
		}

		//return "confused=?null";
	}

}
