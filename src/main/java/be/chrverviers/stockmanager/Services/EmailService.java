package be.chrverviers.stockmanager.Services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;

@Service
@EnableScheduling
public class EmailService {
	
    @Autowired
    Environment env;
    
    @Autowired
    InterventionRepository interventionRepo;
    
	@Autowired
	private JavaMailSender emailSender;
	
    @Value("${stockmanager.mail.host}")
    private String host;
    
    @Value("${stockmanager.mail.port}")
    private String port;
    
    @Value("${stockmanager.mail.email}")
    private String email;
    
    @Value("${stockmanager.production}")
    private Boolean isProd;
    
    @Value("${stockmanager.mail.username}")
    private String username;
    
    @Value("${stockmanager.mail.password}")
    private String password;
    
    @Value("${stockmanager.mail.helpline}")
    private String helpLine;
    
    @Value("${stockmanager.mail.helpline.replyas}")
    private String helpLineDisplay;
    
    @Value("${stockmanager.mail.debug.helpline}")
    private String debugHelpLine;
    
    @Value("${stockmanager.mail.debug.helpline.replyas}")
    private String debugHelpLineDisplay;
    
    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    Folder emailFolder;
    
    Store store;
    
    Properties properties = new Properties();

    @PostConstruct
    void setup() {
		logger.info("Setting up IMAP mail connection");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.username", username);
        properties.put("mail.imap.password", password);
        //properties.put("mail.pop3.starttls.enable", "true");
        Session emailSession = Session.getDefaultInstance(properties);
        Store store = null;
        try {
            store = emailSession.getStore("imap");
            store.connect(host, username, password);
            emailFolder = store.getFolder("Inbox");
            logger.info("The application has successfully connected to the mailbox");
        } catch (MessagingException e) {
            logger.error(String.format("The application has failed to establish a connection to the mailbox with following exception: \n%s", e.getMessage()));
        }
    }
   
    @Scheduled(fixedRate = 5000)
    synchronized void read() throws MessagingException, IOException {
    	//The write permission is important  because we won't be able to set the message as READ otherwise
    	if(!emailFolder.isOpen()) {
    		emailFolder.open(Folder.READ_WRITE);
    	}
        try {
        	//We basically fetch all unread messages
            Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
	        for (int i = 0; i < messages.length; i++) {
	            logger.info("New unread message received. Checking if it should be parsed");
	            Message message = messages[i];
	            //Set a regex to verify it comes from a user with a CHR email
	            Pattern pattern = Pattern.compile("<.*@chrverviers.be>");
	            //Get it the email
	            Matcher matcher = pattern.matcher(message.getFrom()[0].toString());
	            //Try to find match the text and the regex
	            boolean match = matcher.find();
	            //If no match, we set the message as read and we go to the next message
	            if(!match){
		            logger.info("Message doesn't come from the HelpLine. Ignoring it...");
		        	message.setFlag(Flag.SEEN, true);
		            continue;
	            }
	            //If there is a match, we verify that the mail is coming from the helpline, or we log the address of the funny guy that tried weird things on us
	            //The 'helpline' email should be different for testing purposes as we might break or disrupt the Jira otherwise
	            if(!String.format("<%s>", isProd ? helpLineDisplay : debugHelpLineDisplay).equals(matcher.group(0))){
		            logger.info(String.format("The message was sent by a CHRV User [%s] but doesn't come from the HelpLine. Ignoring it...", message.getFrom()[0].toString()));
		        	message.setFlag(Flag.SEEN, true);
		            continue;
	            }
	            //If it comes from the helpline, we parse the mail
	            logger.info("Message is coming from the helpline. Parsing it...");
	            String ticket = "";
	            String intervention = "";
	            try {
		            //We extract the ticket and original intervention values with a regex matcher. If it fails, it's ok we'll catch those exceptions
		        	//We have to remove all spaces because for some reason the Jira's real value will be like # CHRV-2 9340: instead of # CHRV-29340:
	            	//And the encoding is weird too. So, simple fix: remove spaces.
	            	String subject = message.getSubject().replace(" ", ""); 
            		ticket = this.extractTicketNumber(subject);
            		intervention = this.extractInterventionId(subject);
	            } catch(IllegalStateException e) {
	            	//In both cases, it means something was wrong with the informations in the mail, so we set it as READ so that we don't fetch it again next time
		        	message.setFlag(Flag.SEEN, true);
	                logger.error(String.format("The application has to update intervention.\n This should be because no match was found for ticket number and/or intervention id.\n It failed with following exception: \n%s", e.getMessage()));
	                continue;
	            } catch(IndexOutOfBoundsException e) {
		        	message.setFlag(Flag.SEEN, true);
	                logger.error(String.format("The application has to update intervention.\n It failed with following exception: \n%s", e.getMessage()));
	                continue;
	            }
	        	//If we're here then it means everything seems good in the mail.
	            //We try to fetch the intervention that match the id found in the mail
	        	Intervention inter = interventionRepo.findById(Integer.parseInt(intervention)).orElse(null);
	        	if(inter != null && inter.getId()==Integer.parseInt(intervention)) {
	        		//If we find it, we update it.
	        		inter.setTicketNumber(ticket);
	        		interventionRepo.save(inter, inter.getId());
	        	} else {
	        		//If we don't find it, then it means there was an error with the id when sending the mail.
		        	message.setFlag(Flag.SEEN, true);
		            logger.info(String.format("Message successfully parsed and ticket: [%s] received, but the intervention id: [%s] received did not match any intervention from the database thus the message was ignored.", ticket, intervention));
	        	}
	        	//This is important because otherwise the message won't be set as READ unless we access it's content
	        	message.setFlag(Flag.SEEN, true);
	            logger.info(String.format("Message successfully parsed and ticket number: [%s] added to intervention: %s", ticket, inter));
	        }
 
        } finally {
        	//This is also important because if we don't close the resource, trying to open the folder on the next call will give us a nice big ol' stacktrace
        	try {
                if(emailFolder.isOpen()) {
                	emailFolder.close();
                }
        	} catch (NoSuchMethodError e) {
        		//This is a weird Websphere bug. Don't really know what to do to fix it
        		//It doesn't happen when launching the application outside the Websphere server.
        	}
        }
    }
    
    public void sendMail(Intervention intervention) {
        logger.info(String.format("Sending ticket generation mail for intervention: %s", intervention));
    	this.sendMail(intervention, false, false);
    }
    
    public void sendMail(Intervention intervention, Boolean ignoreShouldSendMailHelpline) {
        logger.info(String.format("This mail will ignore the 'ShouldSendMailHelpline' property of the InterventionType", intervention));
    	this.sendMail(intervention, ignoreShouldSendMailHelpline, false);
    }
    
	public void sendMail(Intervention intervention, Boolean ignoreShouldSendMailHelpline, Boolean ignoreShouldSendMailUser) {
			
		if(!intervention.getType().getShouldSendMailHelpline() && !ignoreShouldSendMailHelpline &&  !ignoreShouldSendMailUser) {
			return;
		}
		
		//We get a SimpleMailMessage that will be built based on the SMTP server informations in the application.properties
		SimpleMailMessage message = new SimpleMailMessage();
		//We set the message to be originating from the API's email
		message.setFrom(email);
        //The 'helpline' email should be different for testing purposes as we might break or disrupt the Jira otherwise
		if((intervention.getType().getShouldSendMailUser() || ignoreShouldSendMailUser)) {
			if(intervention.getNotifier()!=null && intervention.getNotifier().getEmail() != null)
				message.setTo(isProd?helpLine:debugHelpLine, intervention.getNotifier() != null ? intervention.getNotifier().getEmail():null);
			else
				message.setTo(isProd?helpLine:debugHelpLine);
		} else {
			message.setTo(isProd?helpLine:debugHelpLine);
		}
		
		//We set the message's informations
		message.setSubject("[Stockmanager] Opération #"+intervention.getId()+"#");
		message.setText(
			"Une nouvelle opération à été créée: "+
			"\nType: "+intervention.getType().getName()+
			"\nUtilisateur: "+intervention.getUser().getFirstname()+" "+intervention.getUser().getLastname()+
			"\nBénéficiare: "+ String.format("%s %s", 
				intervention.getNotifier() != null && intervention.getNotifier().getFirstname() != null ? intervention.getNotifier().getFirstname(): "aucun",
				intervention.getNotifier() != null && intervention.getNotifier().getLastname() != null ? intervention.getNotifier().getLastname(): "") +
			"\nMachine: "+String.format("%s | %s", intervention.getItem().getName(), intervention.getItem().getReference())+
			"\nEndroit prévu: "+String.format("%s ->  %s", intervention.getRoom().getUnit().getName(), intervention.getRoom().getName())+
			"\nDate prévue: "+displayDate(fromDate(intervention.getExpectedDate()))+
			"\nDescription de l'intervention:\n"+intervention.getDescription()
		);
		
		Runnable runnable = () -> { 
			this.emailSender.send(message);
	        logger.info(String.format("The ticket generation mail for intervention: %s has been sent successfully", intervention));
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	private String extractTicketNumber(String from) {
		//Set the regex
        Pattern pattern = Pattern.compile("#\\w*-\\w*:");
        //Set the text we will check
        Matcher matcher = pattern.matcher(from);
        //Try to find something
        boolean match = matcher.find();
        //Return what we found after removing the delimiters from it. If nothing this will throw an error that will needs to be catched
        return matcher.group(0).replaceAll("[^a-zA-Z0-9-]", "");
	}
	
	//Same as above but different regex
	private String extractInterventionId(String from) {
        Pattern pattern = Pattern.compile("#\\d*#");
        Matcher matcher = pattern.matcher(from);
        boolean match = matcher.find();
        return matcher.group(0).replaceAll("[^0-9]", "");
	}
	
    private String displayDate(LocalDate from) {
    	DateTimeFormatter fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
    	return StringUtils.capitalize(fmt.format(from));
    }
	
    private LocalDate fromDate(Date from) {
    	//For some reason, A Java.util.Date can actually be a java.sql.Date
    	//And the java.sql.Date can't use the toInstant() method 
        try {
            return from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        } catch (UnsupportedOperationException e) {
        	Date date = new Date(from.getTime());
        	return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
}