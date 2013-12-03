package envelope;

import java.util.Properties;
import javax.mail.*;
import com.sun.mail.imap.IMAPFolder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import javax.swing.border.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

class Envelope {
	private static Email obtainEmail(StatusPanel statusPanel)
	{
		Email email = null;
		JTextField emailAddressField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		try {
			JPanel inputPanel = new JPanel();
			inputPanel.setLayout(new GridLayout(2,1));
			inputPanel.add(new JLabel("Email:"));
			inputPanel.add(emailAddressField);
			inputPanel.add(Box.createHorizontalStrut(15));
			inputPanel.add(new JLabel("Password:"));
			inputPanel.add(passwordField);

			int result = JOptionPane.showConfirmDialog(null, inputPanel, "Please enter your email address and password", JOptionPane.OK_CANCEL_OPTION);
			if(result == JOptionPane.OK_OPTION)
			{
				email = new Email(statusPanel, emailAddressField.getText(), passwordField.getText());
			} else {
				return;
			}
		}
		catch (UnrecognisedEmailServiceException e)
		{
			JComboBox protocolField = new JComboBox(ProtocolType.values());
			JTextField incomingAddressField = new JTextField();
			JTextField smtpAddressField = new JTextField();
			JTextField smtpPortField = new JTextField();

			JPanel inputPanel = new JPanel();
			inputPanel.setLayout(new GridLayout(4,1));
			inputPanel.add(new JLabel("Incoming Mail Address:"));
			inputPanel.add(incomingAddressField);
			inputPanel.add(Box.createHorizontalStrut(15));
			inputPanel.add(new JLabel("Incoming Protocol:"));
			inputPanel.add(protocolField);
			inputPanel.add(Box.createHorizontalStrut(15));
			inputPanel.add(new JLabel("SMTP Address:"));
			inputPanel.add(smtpAddressField);
			inputPanel.add(Box.createHorizontalStrut(15));
			inputPanel.add(new JLabel("SMTP Port:"));
			inputPanel.add(smtpPortField);

			int result = JOptionPane.showConfirmDialog(null, inputPanel, "Please enter the mail server details", JOptionPane.OK_CANCEL_OPTION);
			if(result == JOptionPane.OK_OPTION)
			{
				email = new Email(statusPanel, emailAddressField.getText(), passwordField.getText(), incomingAddressField.getText(), smtpAddressField.getText(), smtpPortField.getText(), (ProtocolType)protocolField.getSelectedItem());
			} else {
				return null;
			}
		}

		return email;
	}

	private static JMenuBar createEnvelopeMenu(JFrame frame, final Email email, final MessageTableModel messageTableModel, final NewEmailWorker newEmailWorker, final StatusPanel statusPanel)
	{
		JMenuBar menuBar = new JMenuBar();
		JButton compose = new JButton("Compose Mail");
		compose.addActionListener(new ComposeMailListener(frame, email));
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(new RefreshListener(newEmailWorker));

		menuBar.add(compose);
		menuBar.add(refresh);

		return menuBar;
	}

	private static void createGUI(Email email, MessageTableModel messageTableModel, NewEmailWorker newEmailWorker, StatusPanel statusPanel)
	{
		// Create the frame
		JFrame frame = new JFrame("Envelope");

		// Set StatusPanel width to width of screen
		statusPanel.setWidth(frame.getWidth());

		// Set the layout
		BorderLayout layout = new BorderLayout();
		
		// Set frame size
		frame.setSize(500, 500);

		// Create a JPanel to hold the messageList and messagePane
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2,1));

		// Create JTextPane to hold the message
		JMimePane messagePane = new JMimePane(statusPanel);

		// Create JTable to hold subjects
		JTable messageTable = new JTable(messageTableModel);

		ListSelectionModel messageTableSelectionModel = messageTable.getSelectionModel();
		messageTableSelectionModel.addListSelectionListener(new MessageTableSelectionListener(messagePane, email, statusPanel));

		// Create the JMenuBar
		JMenuBar menu = createEnvelopeMenu(frame, email, messageTableModel, newEmailWorker, statusPanel);

		// Create the contacts pane
		JPanel contactsPanel = new JPanel();
		contactsPanel.add(new JLabel("Contacts"));

		// Frame configuration
		mainPanel.add(new JScrollPane(messageTable));
		mainPanel.add(new JScrollPane(messagePane));
		frame.setLayout(layout);
		frame.setJMenuBar(menu);
		frame.add(contactsPanel, BorderLayout.WEST);
		frame.add(mainPanel, BorderLayout.CENTER);
		frame.add(statusPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/*
	  ================== Bonus Features ==================
	  - Added support for message content types other than
            text/plain i.e. Multipart, text/html
	  - Changed the subject list to show additional info
	    including from, attachment and date fields
	  - Added ability to input any email address and
	    password, and in some cases automatically
	    determine server connection settings
	  - Added support for receiving and downloading
	    attachments
	  - Added support for POP3
	  - Added ability to refresh messages
	  ====================================================
	*/
	public static void main(String[] args) throws InterruptedException, InvocationTargetException
	{
		// Create the email object, so that we can pass it to the GUI Components
		final StatusPanel statusPanel = new StatusPanel();
		final MessageTableModel messageTableModel = new MessageTableModel();
		final Email email = obtainEmail(statusPanel);
		if(email == null) {
			JOptionPane.showMessageDialog(null, "An unknown error occurred.", "Unknown Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		final NewEmailWorker newEmailWorker = new NewEmailWorker(email, messageTableModel, statusPanel);

		// Create the GUI
		javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			public void run()
				{
					createGUI(email, messageTableModel, newEmailWorker, statusPanel);
				}
		});

		// New email checking thread
		// Will also fill the table for the first time
		newEmailWorker.execute();
	}
}
