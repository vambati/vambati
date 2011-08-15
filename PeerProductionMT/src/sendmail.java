import channels.P2PServer;
import channels.mail.MailInterface;
import mturk.*; 

public class sendmail {

	public static void main(String args[]){
		String file = args[0];
		String to = "vamshi.ambati@gmail.com";
	      // Send mail 
        MailInterface mi = new MailInterface(); 
        mi.sendAttachmentMail(to,file);
	}
}
