package envelope;

import java.awt.event.*;

class RefreshListener implements ActionListener
{
	private NewEmailWorker newEmailWorker;

	public RefreshListener(NewEmailWorker newEmailWorker)
	{
		this.newEmailWorker = newEmailWorker;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		NewEmailWorker nextEmailWorker = this.newEmailWorker.createIdenticalWorker();
		this.newEmailWorker.cancel(false);
		nextEmailWorker.execute();
		this.newEmailWorker = nextEmailWorker;
	}
}
