package envelope;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

class StatusPanel extends JPanel implements ActionListener
{
	private final int DELAY = 3000;

	private final JLabel label;
	private Timer timer;

	public StatusPanel()
	{
		super();
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.label = new JLabel();
		this.label.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(this.getLabel());
		this.timer = new Timer(DELAY, this);
	}	

	public void setWidth(int width)
	{
		this.setPreferredSize(new Dimension(width, 16));
	}

	public void printException(Exception e)
	{
		this.printException(e, "Exception");
	}

	public void printException(Exception e, String prefix)
	{
		this.setText(prefix + ": " + e.getMessage(), false);
	}

	public void setText(String text, boolean clearable)
	{
		if(clearable)
		{
			// Reset timer
			this.timer.restart();
		}
		else
		{
			// Stop the timer
			this.timer.stop();
		}
		
		this.getLabel().setText(text);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Clear the panel
		this.getLabel().setText("");
	}

	private JLabel getLabel()
	{
		return this.label;
	}
}
