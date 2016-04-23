import java.util.*;  
import javax.mail.*;  
import javax.mail.internet.*;  
import javax.activation.*;  
import javax.swing.JOptionPane;

class SendEmail{  
	public void send(String subject,String msg){  

		String user="alertgfs@gmail.com";
		String password="GFSgroup8";
		String[] sendTo=new String[3];
		sendTo[0]="aishwarypramanik@gmail.com";
		sendTo[1]="shaileshrvajpayee@gmail.com";
		int i;
		for(i=0;sendTo[i]!=null;i++)
		{
			System.out.println("Sending mail to"+sendTo[i]);
			String to=sendTo[i];//change accordingly  
			//1) get the session object     
			Properties props = System.getProperties();
			props.put("mail.smtp.auth", true);
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getDefaultInstance(props,  
					new javax.mail.Authenticator() {  
				protected PasswordAuthentication getPasswordAuthentication() {  
					return new PasswordAuthentication(user,password);  
				}  
			});  

			//2) compose message     
			try{  
				MimeMessage message = new MimeMessage(session);  
				message.setFrom(new InternetAddress(user));  
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));  
				message.setSubject(subject);  

				//3) create MimeBodyPart object and set your message text     
				BodyPart messageBodyPart1 = new MimeBodyPart();  
				messageBodyPart1.setText(msg);  

				//5) create Multipart object and add MimeBodyPart objects to this object      
				Multipart multipart = new MimeMultipart();  
				multipart.addBodyPart(messageBodyPart1);  

				message.setContent(multipart );
				//7) send message  
				Transport.send(message);  

				//JOptionPane.showMessageDialog(null, "Mail sent to "+sendTo[i]);
			}catch (MessagingException ex) {
				System.out.print(ex);

			}  
		}  

	}
}  