package envelope;

import javax.swing.event.*;
import javax.swing.*;
import javax.mail.*;
import java.io.IOException;

class MessageTableSelectionListener implements ListSelectionListener {
	private final JMimePane messagePane;
	private final Email email;
	private int oldIndex;
	private StatusPanel statusPanel;

	public MessageTableSelectionListener(JMimePane messagePane, Email email, StatusPanel statusPanel)
	{
		this.messagePane = messagePane;
		this.email = email;
		this.oldIndex = 0;
		this.statusPanel = statusPanel;
	}	

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();

		if (!e.getValueIsAdjusting()){
			// Replace the content of the messagePane
			// Work out the selected index, it could either be e.getFirstIndex() or e.getLastIndex()
			int selectedIndex;
			if(e.getFirstIndex() == this.getOldIndex())
			{
				selectedIndex = e.getLastIndex();
				this.setOldIndex(e.getLastIndex());
			} else {
				selectedIndex = e.getFirstIndex();
				this.setOldIndex(e.getFirstIndex());
			}

			try {
				int reverseSelectedIndex = email.getLocalMessagesCount() - selectedIndex;
				this.getMessagePane().setContent(email.getMessage(reverseSelectedIndex));
			}
			catch (MessagingException ex)
			{
				this.statusPanel.printException(ex);
			}
		}
	}

	private int getOldIndex()
	{
		return this.oldIndex;
	}

	private void setOldIndex(int i)
	{
		this.oldIndex = i;
	}

	private JMimePane getMessagePane()
	{
		return this.messagePane;
	}

	private Email getEmail()
	{
		return this.email;
	}
}
