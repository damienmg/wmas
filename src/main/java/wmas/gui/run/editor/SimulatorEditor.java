package wmas.gui.run.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import wmas.world.Simulator;

public class SimulatorEditor extends JTabbedPane implements ActionListener,
		ChangeListener {
	private static final long serialVersionUID = 1L;

	private Simulator simu = null;

	private JSpinner nbRuns;
	private JTextField precision;

	private ReportDescriptionEditor reportEditor;
	private EventReportDescriptionEditor eventReportEditor;
	private EventGeneratorEditor eventEditor;
	boolean modified;

	public SimulatorEditor() {
		construct();
		transfer();
	}

	private void transfer() {
		if (simu != null) {
			nbRuns.setValue(simu.getNbRuns());
			precision.setText(Double.toString(simu.getPrecision()));
			eventEditor.setGenerator(simu.getEvents());
			eventReportEditor.setDescr(simu.getReporter().getDescription());
			reportEditor.setReportDescription(simu.getReporter()
					.getDescription());
		} else {
			eventEditor.setGenerator(null);
			eventReportEditor.setDescr(null);
			reportEditor.setReportDescription(null);
		}
	}

	private void construct() {
		reportEditor = new ReportDescriptionEditor();
		eventReportEditor = new EventReportDescriptionEditor();
		eventEditor = new EventGeneratorEditor();
		nbRuns = new JSpinner(
				new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		precision = new JTextField();
		precision.addActionListener(this);
		nbRuns.getModel().addChangeListener(this);

		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 0;
		JLabel l1 = new JLabel("Precision: ");
		p.add(l1, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(precision, c);
		JLabel l2 = new JLabel("Number of executions: ");
		c.gridwidth = 1;

		p.add(l2, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(nbRuns, c);

		addTab("General configuration", p);
		addTab("Generated events", eventEditor);
		addTab("Reported events", eventReportEditor);
		addTab("Reported data", reportEditor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (simu != null) {
			try {
				simu.setPrecision(Double.parseDouble(precision.getText()));
				modified = true;
			} catch (NumberFormatException exn) {
				precision.setText(Double.toString(simu.getPrecision()));
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (simu != null) {
			if (nbRuns.getValue() instanceof Integer) {
				simu.setNbRuns((Integer) nbRuns.getValue());
				modified = true;
			} else
				nbRuns.setValue(simu.getNbRuns());
		}
	}

	public void setSimu(Simulator simu) {
		modified = false;
		this.simu = simu;
		transfer();
	}

	public boolean isModified() {
		return modified || reportEditor.isModified()
				|| eventReportEditor.isModified() || eventEditor.isModified();
	}
}
