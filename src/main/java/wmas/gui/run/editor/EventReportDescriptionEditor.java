package wmas.gui.run.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.reports.ReportDescription;

public class EventReportDescriptionEditor extends JPanel implements
		ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private JList availables;
	private JList accepted;

	private JButton addButton;
	private JButton delButton;

	private ReportDescription descr = null;
	private boolean modified = true;

	public EventReportDescriptionEditor() {
		super(new GridBagLayout());
		construct();
	}

	private void construct() {
		availables = new JList(new DefaultListModel());
		accepted = new JList(new DefaultListModel());
		addButton = new JButton(">");
		delButton = new JButton("<");
		addButton.setActionCommand("+");
		delButton.setActionCommand("-");
		addButton.setEnabled(false);
		delButton.setEnabled(false);
		addButton.addActionListener(this);
		delButton.addActionListener(this);

		availables.addListSelectionListener(this);
		accepted.addListSelectionListener(this);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		add(new JLabel("Availables events:"), c);
		c.weightx = 0;
		add(new JLabel(""), c);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JLabel("Events to log:"), c);

		JPanel p = new JPanel(new GridBagLayout());
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.SOUTH;
		p.add(addButton, c);
		c.fill = GridBagConstraints.NORTH;
		p.add(delButton, c);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(availables), c);
		c.weightx = 0;
		add(p, c);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JScrollPane(accepted), c);

		transfer();
	}

	private void transfer() {
		((DefaultListModel) (availables.getModel())).clear();
		((DefaultListModel) (accepted.getModel())).clear();
		if (descr != null) {
			Set<String> ev = descr.listEvent();
			for (String s : ev) {
				((DefaultListModel) (accepted.getModel())).addElement(s);
			}
			for (String s : ReportDescription.listPossibleEvents()) {
				if (!ev.contains(s))
					((DefaultListModel) (availables.getModel())).addElement(s);
			}
		}
		valueChanged(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			for (Object o : availables.getSelectedValues()) {
				descr.listEvent().add(o.toString());
				modified = true;
			}
			transfer();
		} else if (e.getActionCommand().equals("-")) {
			for (Object o : accepted.getSelectedValues()) {
				descr.listEvent().remove(o.toString());
				modified = true;
			}
			transfer();
		}

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		addButton.setEnabled(availables.getSelectedIndex() >= 0);
		delButton.setEnabled(accepted.getSelectedIndex() >= 0);
	}

	public void setDescr(ReportDescription descr) {
		this.descr = descr;
		modified = false;
		transfer();
	}

	public boolean isModified() {
		return modified;
	}
}
