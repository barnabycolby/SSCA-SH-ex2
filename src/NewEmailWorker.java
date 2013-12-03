package envelope;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.mail.*;

class NewEmailWorker extends SwingWorker<Void,Void> 
{
	final long DELAY = 10 * 1000;

	private Email email;
	private MessageTableModel messageTableModel;
	private StatusPanel statusPanel;

	public NewEmailWorker(Email email, MessageTableModel messageTableModel, StatusPanel statusPanel)
	{
		this.email = email;
		this.messageTableModel = messageTableModel;
		this.statusPanel = statusPanel;
	}

	@Override
	public Void doInBackground()
	{
		while(true)
		{
			if(this.isCancelled()) {
				return (null);
			}

			this.refreshEmail();
			
			try {
				Thread.sleep(DELAY);
			}
			catch (InterruptedException e)
			{
				// This SwingWorker has been cancelled!
				return (null);
			}
		}
	}

	public NewEmailWorker createIdenticalWorker()
	{
		return new NewEmailWorker(this.email, this.messageTableModel, this.statusPanel);
	}

	private void refreshEmail()
	{
		try {
			if(this.isCancelled())
				return;

			this.statusPanel.setText("Checking for new messages...", false);
	
			int newMessagesCount = email.countNewMessages();
			if(newMessagesCount > 0)
			{
				if(this.isCancelled())
					return;

				// Notify the user
				showNewMessageNotification();

				this.statusPanel.setText("Updating list...", false);
				int totalMessageCount = this.email.retrieveMessageCount();
				for(int i = totalMessageCount - newMessagesCount + 1; i <= totalMessageCount; i++)
				{
					Message m = this.email.getMessage(i);

					this.messageTableModel.addMessage(m);
				}

				if(this.isCancelled())
					return;

				this.statusPanel.setText("Updating list...done.", true);
			}

			if(this.isCancelled())
				return;

			this.statusPanel.setText("Checking for new messages...done.", true);
		}
		catch (NoSuchProviderException e)
		{
			this.statusPanel.printException(e);
		}
		catch (MessagingException e)
		{
			this.statusPanel.printException(e);
		}

		return;
	}

	private void showNewMessageNotification()
	{
		JOptionPane.showMessageDialog(null, "You have new email in your inbox!", "New email!", JOptionPane.PLAIN_MESSAGE);
	}
}
