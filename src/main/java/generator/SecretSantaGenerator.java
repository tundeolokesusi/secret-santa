package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.google.common.collect.Lists;

public class SecretSantaGenerator {

	public static void main(String[] args) throws IOException, EmailException {
		FileReader fileReader = new FileReader(new File("src/main/resources/Secret Santa Participants.csv"));
		BufferedReader buffRead = new BufferedReader(fileReader);
		String participant;
		Map<String, String> participants1 = new HashMap<String, String>();
		while((participant = buffRead.readLine())!=null) {
			String[] details = participant.split(",");
			participants1.put(details[0], details[1]);
		}
		
		//For the secret santa mappings csv file 
		PrintWriter pw = null;
		try {
		    pw = new PrintWriter(new File("src/main/resources/SecretSantaMappings.csv"));
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		String ColumnNamesList = "Secret Santa,Giftee";
		builder.append(ColumnNamesList +"\n");
		
		int numberOfParticipants = participants1.size();
		
		int item;
		List<String> name1 = Lists.newArrayList(participants1.keySet().iterator());
		List<String> name2 = Lists.newArrayList(participants1.keySet().iterator());
		Map<String, String> participants2 = new HashMap<String, String>();
		String participantsMapping = "";
		for(String name:name1) {
			item = new Random().nextInt(name2.size());
			String giftee = name2.get(item);
			while (giftee.equals(name)) { //Cannot select own name
				item = new Random().nextInt(name2.size());
				giftee = name2.get(item);
			}
			participants2.put(name, giftee);
			name2.remove(giftee);
			participantsMapping = participantsMapping 
					+ name + "," + giftee + System.lineSeparator();
		}
		
		builder.append(participantsMapping);
		pw.write(builder.toString());
		pw.close();
		System.out.println("Secret Santa mappings csv created!");
		
		// Create the attachment
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath("src/main/resources/SecretSantaMappings.csv");
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Secret Santa Mappings csv");
		attachment.setName("SecretSantaMappings.csv");
		
		// set Email details
		String secretSantaEmail = "secretsanta.gamma@gmail.com";
		String secretSantaPassword = "SantaAtGamma2018";
		
		// create the email message for the Secret Santa Overseer
		String secretSantaemail = "secretsanta.gamma@gmail.com";
		HtmlEmail email = new HtmlEmail();
		email.setHostName("smtp.googlemail.com");
		email.setSmtpPort(465);
//		email.setAuthenticator(new DefaultAuthenticator(secretSantaEmail, secretSantaPassword));
		email.setAuthentication(secretSantaEmail, secretSantaPassword);
		email.setSSLOnConnect(true);
		email.setFrom(secretSantaEmail,"Gamma's Santa");
		email.addTo(secretSantaemail, "Secret Santa Overseer");
		email.setSubject("Secret Santa 2018 Participant Mappings");
		
		// set embedded image for overseer email
		String cid1 = email.embed(new File("src/main/resources/SecretSantaImage1.png"));
		
		// set the html message
		email.setHtmlMsg("<html><img src=\"cid:"+cid1+"\"><p>" +
		"\nAttached is a csv file with the Secret Santa participants and their giftees!</p></html>");
		
		// set the alternative message
		email.setTextMsg("Attached is a csv file with the Secret Santa participants and their giftees!");
		
		// add the csv attachment
		email.attach(attachment);
		
		// send the email
		email.send();
		System.out.println("Secret Santa mappings csv sent!");
		
		// delete the mappings csv
		deleteFile("src/main/resources/SecretSantaMappings.csv");
		
		// set embedded image for participant email
		String cid2 = email.embed(new File("src/main/resources/SecretSantaImage2a.png"));
		
		// send emails to each participant with the name of their giftee
		List<String> santas = Lists.newArrayList(participants2.keySet().iterator());
		for(String name:santas) {
			String emailAddress = participants1.get(name);
			String giftee = participants2.get(name);
			email = new HtmlEmail();
			email.setHostName("smtp.googlemail.com");
			email.setSmtpPort(465);
			email.setAuthentication(secretSantaEmail, secretSantaPassword);
			email.setSSLOnConnect(true);
			email.setFrom(secretSantaEmail,"Gamma's Santa");
			email.setSubject("Secret Santa 2018");
			// set the html message
			email.setHtmlMsg("<html><img src=\"cid:"+cid2+"\"><p>\nHo Ho Ho " + name + ","
					+ "\n\nYou will be getting a Secret Santa gift for:\n\n"
					+ giftee + "</p></html>");
			// set the alternative message
			email.setTextMsg("Ho Ho Ho " + name + "," + System.lineSeparator() + System.lineSeparator() 
				+ "You will be getting a Secret Santa gift for:" + System.lineSeparator() + System.lineSeparator() 
				+ giftee);
			email.addTo(emailAddress,name);
			email.send();
			System.out.println("Email sent to " + name + " (" + emailAddress + ")");
		}
		
		System.out.println("Emails sent to all " + numberOfParticipants + " Secret Santa participants.");
		buffRead.close();
	}
	
	public static void deleteFile(String file) 
    { 
        try
        { 
        	System.out.println("Attempting to delete '" + file + "'");
        	Files.deleteIfExists(Paths.get(file)); 
        } 
        catch(NoSuchFileException e) 
        { 
            System.out.println("No such file/directory exists"); 
        } 
        catch(DirectoryNotEmptyException e) 
        { 
            System.out.println("Directory is not empty."); 
        } 
        catch(IOException e) 
        { 
            System.out.println("Invalid permissions."); 
        } 
          
        System.out.println("File deletion successful."); 
    } 
}
