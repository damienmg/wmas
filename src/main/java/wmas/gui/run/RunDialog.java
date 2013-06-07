package wmas.gui.run;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import wmas.gui.shapes.SchemeView;
import wmas.gui.world.WorldEditor;
import wmas.world.Simulator;
import wmas.world.UpdateManager;
import wmas.world.World;

public class RunDialog extends JDialog implements WindowListener,
		ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	JProgressBar progressBar;
	JLabel progressLabel;
	JProgressBar timeBar;
	JLabel timeLabel;

	class SimulatorTask extends SwingWorker<Void, double[]> implements
			UpdateManager {
		Simulator simulation;

		SimulatorTask(Simulator simu) {
			this.simulation = simu;
		}

		double[] values = new double[4];
		int lastP = -1;

		public void update() {
			if (simulation != null) {
				values[0] = simulation.getRunNumber() - 1;
				values[1] = simulation.getNbRuns();
				values[2] = simulation.getInternalTime();
				values[3] = simulation.predictDuration();
				double progress = values[0] / values[1];
				if (!Double.isInfinite(values[3]))
					progress += values[2] / (values[3] * values[1]);
				setProgress((int) (progress * 100));
				if (this.isCancelled())
					simulation.Kill();
				publish(values);
			}
		}

		public Void doInBackground() {
			if (simulation != null) {
				simulation.setNoTimer(true);
				simulation.setRealTime(false);
				try {
					simulation.Run(this);
				} catch (Exception exn) {
					exn.printStackTrace();
				}
			}
			if (simulation.getRunNumber() == simulation.getNbRuns())
				setProgress(100);
			return null;
		}

		public void done() {
			String t = title + " - Reports";
			if (f != null)
				t = title + " - [" + f.getAbsolutePath() + "] Reports";
			new ReportDialog(RunDialog.this, t, simulation.getReporter());
			close();
		}

		public void addWorldButton(Component comp) {
		}

		public SchemeView getWorldView() {
			return null;
		}

		public void prepareReset() {
		}

		public void reset() {
		}

		public void terminated() {
		}

		protected void process(List<double[]> values) {
			if (values.isEmpty())
				return;
			double[] v = values.get(values.size() - 1);
			RunDialog.this.update((int) v[0], (int) v[1], v[2], v[3]);
		}
	}

	protected void update(int run, int nbRun, double time, double maxTime) {
		if (maxTime == 0 || Double.isInfinite(maxTime)) {
			timeBar.setIndeterminate(true);
		} else {
			timeBar.setIndeterminate(false);
			timeBar.setValue((int) (time * 100 / maxTime));
		}
		progressLabel.setText("Execution " + (run + 1) + " of " + nbRun);
		timeLabel.setText("t = " + time);
	}

	private RunDialog() {
		super();
		getRootPane().setLayout(new GridBagLayout());
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		timeBar = new JProgressBar(0, 100);
		timeBar.setValue(0);
		timeBar.setStringPainted(true);
		progressLabel = new JLabel("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		timeLabel = new JLabel("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		progressLabel.setHorizontalTextPosition(JLabel.CENTER);
		timeLabel.setHorizontalTextPosition(JLabel.CENTER);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(timeLabel, c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		getRootPane().add(timeBar, c);
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0;
		getRootPane().add(progressLabel, c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		getRootPane().add(progressBar, c);
		c.fill = GridBagConstraints.CENTER;
		JButton but = new JButton("Cancel");
		but.addActionListener(this);
		getRootPane().add(but, c);
		getRootPane()
				.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		pack();
		setResizable(false);
		addWindowListener(this);
	}

	private void reset() {
		timeBar.setIndeterminate(true);
		progressBar.setIndeterminate(false);
		timeBar.setValue(0);
		progressBar.setValue(0);
		timeLabel.setText("");
		progressLabel.setText("");
	}

	private SimulatorTask task = null;

	private void execute(Simulator simu) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		reset();
		if (simu != null) {
			update(0, simu.getNbRuns(), 0, Double.POSITIVE_INFINITY);
		}
		task = (new SimulatorTask(simu));
		task.addPropertyChangeListener(this);
		task.execute();
	}

	public void run(Simulator simu) {
		setVisible(true);
		setLocationRelativeTo(null);
		execute(simu);
	}

	private static RunDialog dialog = null;
	private static File f;
	private static String title;
	private static World world;

	public static void run(String t, File f, World w, String s) {
		Simulator simu = w.getSimulator(s);
		RunDialog.world = w;
		if (simu != null) {
			simu.setWorld(w);
			String title = t + " - Execution of " + s;
			if (f != null)
				title = t + " - [" + f.getAbsolutePath() + "] " + title;
			RunDialog.f = f;
			RunDialog.title = t;
			if (dialog == null)
				dialog = new RunDialog();
			dialog.setTitle(title);
			dialog.run(simu);
		}
	}

	public void close() {
		if (task != null) {
			SimulatorTask task2 = task;
			task = null;
			if (!task2.cancel(true)) {
				this.dispose();
				if (f != null)
					WorldEditor.run(title, f);
				else
					WorldEditor.run(title, world);
			}
		} else {
			this.dispose();
			if (f != null)
				WorldEditor.run(title, f);
			else
				WorldEditor.run(title, world);
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		close();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		close();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (task != null) {
			int progress = task.getProgress();
			progressBar.setValue(progress);
		}
	}

}
