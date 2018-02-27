package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.google.common.collect.Lists;

public class RandomNameGenerator {

	public static void main(String[] args) throws IOException, EmailException {
		FileReader fileReader = new FileReader(new File("src/main/resources/TestData.csv"));
		BufferedReader buffRead = new BufferedReader(fileReader);
		String participant;
		Map<String, String> participants1 = new HashMap<String, String>();
		while((participant = buffRead.readLine())!=null) {
			String[] details = participant.split(",");
			participants1.put(details[0], details[1]);
		}
		int item;
		List<String> name1 = Lists.newArrayList(participants1.keySet().iterator());
		List<String> name2 = Lists.newArrayList(participants1.keySet().iterator());
		Map<String, String> participants2 = new HashMap<String, String>();
		for(String name:name1) {
			item = new Random().nextInt(name2.size());
			String giftee = name2.get(item);
			while (giftee.equals(name)) { //Cannot select own name
				item = new Random().nextInt(name2.size());
				giftee = name2.get(item);
			}
			participants2.put(name, giftee);
			name2.remove(giftee);
		}
		
		List<String> santas = Lists.newArrayList(participants2.keySet().iterator());
		for(String name:santas) {
			String emailAddress = participants1.get(name);
			String giftee = participants2.get(name);
			Email email = new SimpleEmail();
			email.setHostName("smtp.googlemail.com");
			email.setSmtpPort(465);
			email.setAuthenticator(new DefaultAuthenticator("tundeolokesusi@gmail.com", "cyzrukqzbhtnrgub"));
			email.setSSLOnConnect(true);
			email.setFrom("tundeolokesusi@gmail.com");
			email.setSubject("Secret Santa 2018");
			email.setMsg("Ho Ho Ho " + name + "," + System.lineSeparator() + System.lineSeparator() 
				+ "You will be getting a Secret Santa Gift for:" + System.lineSeparator() + System.lineSeparator() 
				+ giftee);
			email.addTo(emailAddress);
			email.send();
			System.out.println("Email sent to " + name + " (" + emailAddress + ")");
		}
		buffRead.close();
	}
}
