package generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;

import com.google.common.collect.Lists;

import text.TextAlignment;
import text.TextRenderer;

/**
 * A class that randomly generates names for secret santa from a csv of participants & emails
 * Then emails out the secret santa giftee to the participants with a printable label attached
 * 
 * @author  Tunde Olokesusi
 * @version 1.0
 **/

public class SecretSantaGenerator {
	// set required details
	public static String participantsCSV= "Secret Santa Participants at Gamma.csv";
	public static String secretSantaEmail = "secretsanta.gamma@gmail.com";
	public static String secretSantaPassword = "SantaAtGamma2018";
	public static String secretSantaOverseer = "secretsanta.gamma@gmail.com";
	public static boolean emailMappings = true;
	public static boolean deleteMappings = false;
	public static String purchaseLimit = "£10";
	public static String giftPurchaseDeadline = "Tuesday 11th December";
	public static String giftExchangeDate = "Wednesday 12th December";
	public static boolean emailParticipants = false;

	public static void main(String[] args) throws IOException, EmailException {
		File participantsCSVFile = new File("src/main/resources/"+participantsCSV);
		
		//Check that participants file is present in required folder
		if (participantsCSVFile.exists()) {
			System.out.println("CSV File of participants '"+ participantsCSV
					+ "' exists in the resources (src/main/resources/) folder.");
		}
		else{
			System.out.println("Could not find CSV File of participants '"+ participantsCSV
					+ "' in the resources (src/main/resources/) folder.\n"
					+ "Please make sure the csv file is in the right location");
			return;
		}
		
		FileReader fileReader = new FileReader(new File(participantsCSVFile.toString()));
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
		
		// set embedded image for overseer email
		File imagePath = new File("src/main/resources/");
		String image1 = "SecretSantaImage1.png";
		
		// create the email message for the Secret Santa Overseer
		ImageHtmlEmail email = new ImageHtmlEmail();
		email.setDataSourceResolver(new DataSourceFileResolver(imagePath));
		
		try {
			email.setHostName("smtp.googlemail.com");
			email.setSmtpPort(587);//465);
			email.setAuthentication(secretSantaEmail, secretSantaPassword);
			email.setSSLOnConnect(true);
			email.setFrom(secretSantaEmail,"Santa @Gamma");
			email.addTo(secretSantaOverseer, "Secret Santa Overseer");
//			email.addBcc("tundeolokesusi@gmail.com","Tunde Olokesusi");
			email.setSubject("Secret Santa 2018 Participant Mappings");
			
			// set the html message
			String htmlTemplate ="<html><center><img src=\""+image1+"\"><p><br/>Ho Ho Ho,"
					+ "<br/><br/>Attached is a csv file with the Secret Santa participants and their giftees!"
					+ "<br/>You have been entrusted with these mappings, please don't look at them unless it "
					+ "is absolutely necessary!"
					+ "<br/><br/>Thank You and Merry Christmas,<br/>Santa</p></center></html>";
					
			email.setHtmlMsg(htmlTemplate);
			
			// set the alternative message
			email.setTextMsg("Attached is a csv file with the Secret Santa participants and their giftees!");
			
			// add the csv attachment
			email.attach(attachment);
			
			if(emailMappings) {
				email.send(); // send the email
				System.out.println("Secret Santa mappings csv sent!");
				// delete the mappings csv from local system
				if(deleteMappings) {
					deleteFile("src/main/resources/SecretSantaMappings.csv");
				}
			}
		} catch(EmailException ee) {
			ee.printStackTrace();
		}
		
		// set embedded image for participant email
//		String image2 = "SecretSantaImage2a.png";
		
		// send emails to each participant with the name of their giftee
		List<String> santas = Lists.newArrayList(participants2.keySet().iterator());
		for(String name:santas) {
			String emailAddress = participants1.get(name);
			String giftee = participants2.get(name);
			
			// Secret Santa Label for attahcment
			secretSantaLabel("src/main/resources/SecretSantaTemplate2.png",giftee);
			attachment = new EmailAttachment();
			attachment.setPath("src/main/resources/SecretSantaLabel.png");
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setDescription("Secret Santa Label");
			attachment.setName("SecretSantaLabel.png");
			
			email = new ImageHtmlEmail();
			email.setDataSourceResolver(new DataSourceFileResolver(imagePath));
			
			try {
				email.setHostName("smtp.googlemail.com");
				email.setSmtpPort(587);//465);
				email.setAuthentication(secretSantaEmail, secretSantaPassword);
				email.setSSLOnConnect(true);
				email.setFrom(secretSantaEmail,"Santa @Gamma");
				email.setSubject("Dear Secret Santa");
				// set the html message
				String htmlTemplate = "<html><center><img src=\""+image1+"\"><p><br/><i><font color=\"purple\">Ho Ho Ho " + name + ","
						+ "<br/><br/>This year for Christmas, would you be so kind as to procure a gift for:</font><br/><br/>"
						+ "<font size=\"3\" color=\"white\"><strong>" + giftee + "</strong></font>"
						+ "<br/><br/><font color=\"purple\">This gift does not need to exceed "+ purchaseLimit
						+ "<br/>Please ensure your gift is purchased, wrapped and in the avialble "
						+ "Santa Sacks in the kitchen by " + giftPurchaseDeadline + "."
						+ "<br/>Attached is a label with your Secret Santa giftee's name, "
						+ "which you may choose to print off and attach to their gift."
						+ "<br/>Gifts will be handed out on " + giftExchangeDate + "."
						+ "<br/><br/>Happy Gift Hunting,<br/>Santa</font></p></i></center></html>";
				email.setHtmlMsg(htmlTemplate);
				// set the alternative message
//				email.setTextMsg("Ho Ho Ho " + name + "," + System.lineSeparator() + System.lineSeparator() 
//					+ "You will be getting a Secret Santa gift for:" + System.lineSeparator() + System.lineSeparator() 
//					+ giftee);
				email.addTo(emailAddress,name);
				email.attach(attachment);
				if(emailParticipants) {
					email.send();
					System.out.println("Email sent to " + name + " (" + emailAddress + ")");
				}
			} catch(EmailException ee) {
				ee.printStackTrace();
			}
//			deleteFile("src/main/resources/SecretSantaLabel.png");
		}
		if(emailParticipants) {
			System.out.println("Emails sent to all " + numberOfParticipants + " Secret Santa participants.");
		}
		System.out.println("Labels for all participants have been saved in 'src/main/resources/secretsantalabels/'");
		
		File label = new File("src/main/resources/SecretSantaLabel.png");
		while (label.exists()) {
			deleteFile("src/main/resources/SecretSantaLabel.png");
		}
		
		buffRead.close();
		System.out.println("Have a Merry Christmas!");
	}
	
	public static void deleteFile(String file) { 
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
	
	public static  void secretSantaLabel(String imageFile,String name) throws IOException {
	    final BufferedImage image = ImageIO.read(new File(imageFile));
	    
	    Rectangle bounds = new Rectangle(210, 80, 400, 200);
	    Graphics g = image.getGraphics();
	    g.setFont(new Font("Script MT Bold",Font.BOLD,62));
	    g.setColor(Color.RED);
	    TextRenderer.drawString(g,name,g.getFont(),g.getColor(),bounds,TextAlignment.MIDDLE);
	    g.dispose();

	    ImageIO.write(image, "png", new File("src/main/resources/SecretSantaLabel.png"));
	    ImageIO.write(image, "png", new File("src/main/resources/secretsantalabels/"+name+".png"));
	}
}
