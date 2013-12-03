package envelope;

import javax.swing.*;

class SendEmailWorker extends SwingWorker<Void,Void>
{
	private Email email;
	private EmailMessage emailMessage;

	public SendEmailWorker(Email email, EmailMessage emailMessage)
	{
		this.email = email;
		this.emailMessage = emailMessage;
	}

	@Override
	public Void doInBackground()
	{
		// Send the email
		this.email.send(this.emailMessage);
		
		return (null);
	}
}
