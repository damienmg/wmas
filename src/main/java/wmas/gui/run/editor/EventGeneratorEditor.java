package wmas.gui.run.editor;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import wmas.world.events.EventComboBoxModel;
import wmas.world.events.EventGenerator;
import wmas.world.events.EventTableModel;
import wmas.world.events.SimuEvent;

public class EventGeneratorEditor extends JPanel implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private EventTableModel evt;
	private JTable table;
	private JButton addButton;
	private JButton delButton;

	public EventGeneratorEditor() {
		super(new GridBagLayout());
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
		addButton = new JButton("+");
		addButton.setActionCommand("+");
		addButton.setToolTipText("Add an event");
		addButton.addActionListener(this);
		delButton = new JButton("-");
		delButton.setActionCommand("-");
		delButton.setToolTipText("Delete an event");
		delButton.addActionListener(this);
		delButton.setEnabled(false);

		tb.add(addButton);
		tb.add(delButton);
		tb.setFloatable(false);
		add(tb, c);

		evt = new EventTableModel();
		table = new JTable(evt);
		table.setDefaultEditor(SimuEvent.class, new DefaultCellEditor(
				new JComboBox(new EventComboBoxModel())));
		table.setDefaultRenderer(SimuEvent.class,
				new DefaultTableCellRenderer());
		table.getSelectionModel().addListSelectionListener(this);
		c.weighty = 1;
		add(new JScrollPane(table), c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(
					table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}
		if (e.getActionCommand().equals("+")) {
			evt.add();
		} else if (e.getActionCommand().equals("-")) {
			for (int i : table.getSelectedRows()) {
				evt.del(i);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		delButton.setEnabled(table.getSelectedRow() >= 0);
	}

	public void setGenerator(EventGenerator gen) {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(
					table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}
		evt.setGenerator(gen);
	}

	public boolean isModified() {
		return evt.isModified();
	}
}
