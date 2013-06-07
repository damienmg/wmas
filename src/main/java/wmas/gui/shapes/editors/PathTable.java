package wmas.gui.shapes.editors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.geometry.Path;

public class PathTable extends JPanel implements ListSelectionListener,
		ActionListener {
	private static final long serialVersionUID = 1L;

	private JTable table;
	private PathTableModel model;
	private Component parent;
	private JButton addButton;
	private JButton delButton;
	private String aCommand = "";
	private Set<ActionListener> listeners = new HashSet<ActionListener>();

	public PathTable() {
		super(new GridBagLayout());
		construct();
	}

	public void setPath(Path p) {
		model.setPath(p);
		valueChanged(null);
		if (parent != null)
			parent.repaint();
	}

	public void setParent(Component parent) {
		this.parent = parent;
		if (parent != null)
			parent.repaint();
	}

	private void construct() {
		model = new PathTableModel();
		model.parent = this;
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		addButton = new JButton("+");
		delButton = new JButton("-");
		addButton.setToolTipText("Add a point to the path");
		delButton.setToolTipText("Delete selected point from the path");
		addButton.setActionCommand("+");
		delButton.setActionCommand("-");
		addButton.addActionListener(this);
		delButton.addActionListener(this);
		delButton.setEnabled(false);

		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		tb.setFloatable(false);
		tb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});
		tb.add(addButton);
		tb.add(delButton);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0;
		add(tb, c);
		c.weighty = 1;
		add(new JScrollPane(table), c);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		delButton.setEnabled(table.getSelectedRow() >= 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			model.add();
		} else if (e.getActionCommand().equals("-")) {
			int index = table.getSelectedRow();
			if (index >= 0) {
				model.delete(index);
			}
		}
	}

	public void setActionCommand(String string) {
		this.aCommand = string;
	}

	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}

	void action() {
		if (parent != null)
			parent.repaint();
		ActionEvent e = new ActionEvent(this, 0, aCommand);
		for (ActionListener l : listeners)
			l.actionPerformed(e);
	}

	public void refresh() {
		model.fireTableDataChanged();
	}
}
