package envelope;

import javax.swing.table.*;
import javax.mail.*;
import java.text.*;
import java.io.*;
import java.util.ArrayList;

class MessageTableModel extends AbstractTableModel
{
	private String[] columnNames;
	private ArrayList<String[]> data;

	public MessageTableModel()
	{
		this.data = new ArrayList<String[]>();
		this.columnNames = new String[] { "Subject","From", "Attachment","Date" };
	}

	@Override
	public int getRowCount()
	{
		return this.data.size();
	}

	@Override
	public int getColumnCount()
	{
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return this.columnNames[column];
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		return this.data.get(row)[column];
	}

	public void setData(Email email)
	{
		for (int i = 0; i < email.getLocalMessagesCount(); i++)
		{
			try {
				addMessage(email.getMessage(i));
			} catch (MessagingException e) {}
		}
	}

	public void addMessage(Message m)
	{
		// Create the table row to store the message info
		String[] element = new String[getColumnCount()];

		// Subject
		try {
			element[0] = m.getSubject();
		} catch (MessagingException e) {
			element[0] = "";
		}
		
		// From
		try {
			element[1] = convertAddressesToString(m.getFrom());
		} catch (MessagingException e) {
			element[1] = "";
		}

		// Attachment
		if(hasAttachment(m))
		{
			element[2] = "Yes";
		} else {
			element[2] = "";
		}


		// Date
		try {
			element[3] = dateToString(m.getReceivedDate());
		} catch (MessagingException e) {
			element[3] = "";
		}

		// Add to table
		this.data.add(0, element);

		// Update the table
		this.fireTableRowsInserted(this.data.size(), this.data.size());
	}

	private boolean hasAttachment(Message m)
	{
		Multipart multipart = null;
		try {
			if(m.getContent() instanceof Multipart) {
				multipart = (Multipart)m.getContent();
				boolean answer = false;	
				for(int i = 0; i < multipart.getCount(); i++)
				{
					try {
						BodyPart bodyPart = multipart.getBodyPart(i);
						String disposition = bodyPart.getDisposition();
						if(disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE)))
						{
							answer = true;
							break;
						}
					}
					catch (MessagingException e) {}
				}

				return answer;
			} else {
				return false;
			}
		} catch (IOException ex) {
			return false;
		} catch (MessagingException ex) {
			return false;
		}
	
	}

	private String convertAddressesToString(Address[] as)
	{
		if(as.length > 0)
		{
			String returnString = as[0].toString();
			for(int i = 1; i < as.length; i++)
			{
				returnString += "; " + as[i];
			}
			return returnString;
		}
		else
		{
			return "";
		}
	}

	private String dateToString(java.util.Date d)
	{
		if(d == null)
			return "";
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(d);
	}
}
