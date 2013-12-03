package envelope;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.DefaultFolder;
import java.io.IOException;
import javax.activation.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

class Email {
	private final StatusPanel statusPanel;
	private Store store;
	private Folder inbox;
	private ArrayList<Message> messages;
	private final String emailAddress;
	private final String password;
	private String POP3Address;
	private String IMAPAddress;
	private final String SMTPAddress;
	private final String SMTPPort;
	private final ProtocolType protocol;
	private boolean initialised;

	// Constructor
	public Email(StatusPanel statusPanel, String emailAddress, String password) throws UnrecognisedEmailServiceException
	{
		this.statusPanel = statusPanel;
		this.emailAddress = emailAddress;
		this.password = password;
		if(this.getEmailAddress().contains("@gmail.com"))
		{
			this.protocol = ProtocolType.IMAP;
			this.IMAPAddress = "imap.gmail.com";
			this.SMTPAddress = "smtp.gmail.com";
			this.SMTPPort = "587";
		} else {
			throw new UnrecognisedEmailServiceException("Unrecognised email service: " + this.getEmailAddress());
		}

		this.messages = new ArrayList<Message>();
		this.initialised = false;
	}

	public Email(StatusPanel statusPanel, String emailAddress, String password, String address, String smtpAddress, String smtpPort, ProtocolType protocol)
	{
		this.statusPanel = statusPanel;
		this.emailAddress = emailAddress;
		this.password = password;
		this.SMTPAddress = smtpAddress;
		this.SMTPPort = smtpPort;
		this.protocol = protocol;

		switch(protocol){
		case IMAP:
			this.IMAPAddress = address;
			break;
		case POP3:
			this.POP3Address = address;
			break;
		}

		this.messages = new ArrayList<Message>();
		this.initialised = false;
	}

	public void initialise() throws NoSuchProviderException, MessagingException
	{
		// Connect to the mail server and retrieve the mail store	
		statusPanel.setText("Getting store...", false);
		this.store = getConnectedStore();
		statusPanel.setText("Getting store...Done.", true);
		
		// Retrieve the inbox
		statusPanel.setText("Retrieving inbox...", false);
		switch(this.protocol) {
		case IMAP:
			this.inbox = (IMAPFolder)this.getStore().getFolder("inbox");
			break;
		case POP3:
			// POP only downloads the inbox
			// In this example it downloads all folders, see the follwing article for details
			// https://support.google.com/mail/answer/16418?hl=en
			this.inbox = this.getStore().getFolder("INBOX");
			break;
		}
		statusPanel.setText("Retrieving inbox...Done.", true);

		// Check that the inbox is open
		if(!this.getInbox().isOpen())
			this.getInbox().open(Folder.READ_WRITE);

		this.initialised = true;
	}

	public Message getMessage(int msgnum) throws MessagingException
	{
		Message m = this.getInbox().getMessage(msgnum);
		this.setMessage(msgnum, m);
		return m;
	}

	private void setMessage(int i, Message m)
	{
		boolean added = false;
		while (!added)
		{
			try {
				this.messages.set(i - 1, m);
				added = true;
			}
			catch (IndexOutOfBoundsException e)
			{
				this.messages.add(null);
			}
		}
	}

	public int retrieveMessageCount() throws MessagingException
	{
		return this.getInbox().getMessageCount();
	}

	public int getLocalMessagesCount()
	{
		return this.getMessages().size();
	}

	private Store getConnectedStore() throws NoSuchProviderException, MessagingException
	{
		Session session = getRetrieveSession();
		statusPanel.setText("Connecting to the mailbox...", false);
		Store store = null;
		switch(this.protocol)
		{
		case IMAP:
			store = session.getStore("imaps");
			store.connect(getIMAPAddress(), getEmailAddress(), getPassword());
			break;
		case POP3:
			store = session.getStore("pop3s");
			store.connect(getPOP3Address(), getEmailAddress(), getPassword());
			break;
		}
		
		statusPanel.setText("Connecting to the mailbox...Done.", true);
		return store;
	}

	private Session getRetrieveSession()
	{
		// Set some mail properties
		Properties properties = System.getProperties();
		switch(this.protocol) {
		case IMAP:
			properties.setProperty("mail.store.protocol", "imaps");
			break;
		case POP3:
			properties.setProperty("mail.store.protocol", "pop3s");
			break;
		}

		properties.setProperty("mail.user", getEmailAddress());
		properties.setProperty("mail.password", getPassword());

		// Establish a mail session
		Session session = Session.getDefaultInstance(properties);

		return session;
	}

	private Session getSendSession()
	{
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.host", this.getSMTPAddress());
		properties.setProperty("mail.smtp.port", this.getSMTPPort());
		properties.setProperty("mail.password", this.getPassword());

		return Session.getDefaultInstance(properties);
	}

	public void send(EmailMessage messageInfo)
	{
		System.out.println("SEND METHOD");
		try {

			System.out.println("1");

			Session session = this.getSendSession();
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.getEmailAddress()));
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(messageInfo.getTo().getText()));
			message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(messageInfo.getCC().getText()));
			message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(messageInfo.getBCC().getText()));

			System.out.println("2");


			message.setSubject(messageInfo.getSubject().getText());

			Multipart multiPart = new MimeMultipart();
			MimeBodyPart messageText = new MimeBodyPart();
			messageText.setText(messageInfo.getMessage().getText());
			multiPart.addBodyPart(messageText);

			System.out.println("3");


			// Add attachments if they exist
			for(int i = 0; i < messageInfo.getFiles().size(); i++)
			{
				MimeBodyPart attachment = new MimeBodyPart();
				DataSource source = new FileDataSource(messageInfo.getFiles().get(i));
				attachment.setDataHandler(new DataHandler(source));
				attachment.setFileName(messageInfo.getFiles().get(i).getName());
				multiPart.addBodyPart(attachment);
			}

			message.setContent(multiPart);
			message.saveChanges();

			System.out.println("4");


			// Send the message
			Transport tr = session.getTransport("smtp");
			this.statusPanel.setText("Connecting to the server...", false);
			tr.connect(this.getSMTPAddress(), this.getEmailAddress(), this.getPassword());

			this.statusPanel.setText("Sending message...", false);
			tr.sendMessage(message, message.getAllRecipients());
			this.statusPanel.setText("Sending message...Done.", true);

			System.out.println("5");

		}
		catch (NoSuchProviderException e)
		{
			this.statusPanel.printException(e);	
		}
		catch (MessagingException e)
		{
			this.statusPanel.printException(e);
		}
	}

	public int countNewMessages() throws MessagingException
	{
		if(!this.initialised)
		{
			this.initialise();
		}

		return (this.inbox.getMessageCount() - this.getLocalMessagesCount());
	}

	private void updateMessages()
	{
		try {
			this.messages = new ArrayList<Message>(Arrays.asList(this.getInbox().getMessages()));
		}
		catch (MessagingException e)
		{
			this.statusPanel.printException(e);
		}
	}

	private String getEmailAddress()
	{
		return this.emailAddress;
	}

	private String getPassword()
	{
		return this.password;
	}

	private String getPOP3Address()
	{
		return this.POP3Address;
	}

	private String getIMAPAddress()
	{
		return this.IMAPAddress;
	}

	private String getSMTPAddress()
	{
		return this.SMTPAddress;
	}

	private String getSMTPPort()
	{
		return this.SMTPPort;
	}

	private ArrayList<Message> getMessages()
	{
		return this.messages;
	}

	private Store getStore()
	{
		while(this.store == null)
		{
			try
			{
				this.initialise();
			}
			catch (NoSuchProviderException e){}
			catch (MessagingException e){}
		}
		
		return this.store;
	}

	private Folder getInbox()
	{
		while(this.inbox == null)
		{
			try
			{
				this.initialise();
			}
			catch (NoSuchProviderException e){
				this.statusPanel.printException(e);
			} catch (MessagingException e){
				this.statusPanel.printException(e);
			}
		}

		return this.inbox;
	}
}
