package wmas.gui.run.editor;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.reports.ReportDescription;
import wmas.reports.ReportDescriptionTableModel;

public class ReportDescriptionEditor extends JPanel implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private ReportDescriptionTableModel tableModel = new ReportDescriptionTableModel();
	private JButton addButton = new JButton("+");
	private JButton delButton = new JButton("-");
	private JTable table;

	public ReportDescriptionEditor() {
		super(new GridBagLayout());
		construct();
	}

	private void construct() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		JToolBar tb = new JToolBar();
		tb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});

		addButton.setToolTipText("Add a value to report");
		delButton.setToolTipText("Remove selected value to report");
		addButton.setActionCommand("+");
		delButton.setActionCommand("-");
		addButton.addActionListener(this);
		delButton.addActionListener(this);
		delButton.setEnabled(false);
		tb.add(addButton);
		tb.add(delButton);
		tb.setFloatable(false);
		add(tb, c);

		table = new JTable(tableModel);
		table.getSelectionModel().addListSelectionListener(this);
		c.weighty = 1;
		add(new JScrollPane(table), c);
	}

	public void setReportDescription(ReportDescription e) {
		tableModel.setDesc(e);
		valueChanged(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			tableModel.add(this);
		} else if (e.getActionCommand().equals("-")) {
			tableModel.remove(table.getSelectedRows());
		}

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		delButton.setEnabled(table.getSelectedRow() >= 0);
	}

	public boolean isModified() {
		return tableModel.isModified();
	}
}
