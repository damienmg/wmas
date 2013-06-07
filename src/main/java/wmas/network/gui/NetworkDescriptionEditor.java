package wmas.network.gui;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import wmas.gui.GuiModificationListener;
import wmas.network.NetworksDescription;
import wmas.network.NetworksDescription.NetworkDescription;

public class NetworkDescriptionEditor extends JPanel implements TableModel,
		ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private NetworksDescription description = null;
	private GuiModificationListener parent = null;

	private JTable table = null;
	private JToolBar tb = null;
	private JButton addButton = null;
	private JButton delButton = null;
	private DefaultListModel messageList = null;
	private JToolBar msgTb = null;
	private JButton msgAddButton = null;
	private JButton msgDelButton = null;
	private JList msgList = null;

	public NetworkDescriptionEditor() {
		super(new GridBagLayout());
		table = new JTable(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(false);
		messageList = new DefaultListModel();
		msgList = new JList(messageList);
		msgList.addListSelectionListener(this);
		tb = new JToolBar();
		tb.add(new JLabel("List of network configurations"));
		tb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});
		addButton = new JButton("+");
		addButton.setActionCommand("+");
		addButton.addActionListener(this);
		delButton = new JButton("-");
		delButton.setActionCommand("-");
		delButton.addActionListener(this);
		tb.add(addButton);
		tb.add(delButton);
		tb.setFloatable(false);

		msgTb = new JToolBar();
		msgTb.add(new JLabel("List of available messages"));
		msgTb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});
		msgAddButton = new JButton("+");
		msgAddButton.setActionCommand("m+");
		msgAddButton.addActionListener(this);
		msgDelButton = new JButton("-");
		msgDelButton.setActionCommand("m-");
		msgDelButton.addActionListener(this);
		msgTb.add(msgAddButton);
		msgTb.add(msgDelButton);
		msgTb.setFloatable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 0;
		add(tb, c);
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(msgTb, c);
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
		add(new JScrollPane(table), c);
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JScrollPane(msgList), c);
	}

	public void setParent(NetworksDescription descr,
			GuiModificationListener parent) {
		this.description = descr;
		this.parent = parent;
		refresh();
	}

	private HashSet<TableModelListener> listeners = new HashSet<TableModelListener>();

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 3:
			return Long.class;
		case 4:
			return Double.class;
		case 5:
			return Boolean.class;
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Network index";
		case 1:
			return "Default communication range";
		case 2:
			return "Default window size";
		case 3:
			return "Bandwidth (byte/second)";
		case 4:
			return "Delay";
		case 5:
			return "Reader?";
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return description == null ? 0 : description.getNbNetworks();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (description == null)
			return null;
		if (columnIndex == 0) {
			return rowIndex;
		}
		NetworkDescription net = description.getNetwork(rowIndex);
		if (net != null) {
			switch (columnIndex) {
			case 1:
				return net.defaultRange == 0 ? "∞" : net.defaultRange;
			case 2:
				return net.defaultWindow;
			case 3:
				return net.bandwidth;
			case 4:
				return net.delay;
			case 5:
				return net.reader;
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (description != null && columnIndex != 0);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (description == null || columnIndex == 0) {
			return;
		}
		NetworkDescription net = description.getNetwork(rowIndex);
		if (net != null) {
			switch (columnIndex) {
			case 1:
				if (aValue.toString().equals("∞"))
					net.defaultRange = 0;
				else {
					try {
						net.defaultRange = Double
								.parseDouble(aValue.toString());
					} catch (NumberFormatException ex) {
						return;
					}
				}
				cellChanged(rowIndex, columnIndex);
				break;
			case 2:
				try {
					net.defaultWindow = Integer.parseInt(aValue.toString());
				} catch (NumberFormatException ex) {
					return;
				}
				cellChanged(rowIndex, columnIndex);
				break;
			case 3:
				try {
					net.bandwidth = Long.parseLong(aValue.toString());
				} catch (NumberFormatException ex) {
					return;
				}
				cellChanged(rowIndex, columnIndex);
				break;
			case 4:
				try {
					net.delay = Double.parseDouble(aValue.toString());
				} catch (NumberFormatException ex) {
					return;
				}
				cellChanged(rowIndex, columnIndex);
				break;
			case 5:
				net.reader = (aValue instanceof Boolean) && ((Boolean) aValue);
				cellChanged(rowIndex, columnIndex);
				break;
			}
		}
	}

	public void refresh() {
		delButton.setEnabled(description != null
				&& description.getNbNetworks() > 0);
		msgDelButton.setEnabled(description != null
				&& !msgList.isSelectionEmpty());
		messageList.clear();
		if (description != null) {
			int i = 0;
			for (String s : description.listMessageTypes()) {
				messageList.add(i, s);
				i++;
			}
		}
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(
					table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}
		TableModelEvent e = new TableModelEvent(this);
		for (TableModelListener l : listeners) {
			l.tableChanged(e);
		}
	}

	void cellChanged(int row, int column) {
		parent.internalChanged(null);
		TableModelEvent e = new TableModelEvent(this, row, column);
		for (TableModelListener l : listeners) {
			l.tableChanged(e);
		}
	}

	void rowAdded() {
		parent.internalChanged(null);
		TableModelEvent e = new TableModelEvent(this);
		for (TableModelListener l : listeners) {
			l.tableChanged(e);
		}
	}

	void rowDeleted() {
		parent.internalChanged(null);
		TableModelEvent e = new TableModelEvent(this);
		for (TableModelListener l : listeners) {
			l.tableChanged(e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (description == null)
			return;
		if (e.getActionCommand().equals("+")) {
			description.addNetwork();
			delButton.setEnabled(true);
			rowAdded();
		} else if (e.getActionCommand().equals("-")) {
			description.deleteNetwork();
			delButton.setEnabled(description.getNbNetworks() > 0);
			rowDeleted();
		} else if (e.getActionCommand().equals("m+")) {
			String msg = JOptionPane.showInputDialog(this,
					"Enter the name of the new message",
					"Network message creation", JOptionPane.OK_CANCEL_OPTION);
			if (msg != null && !msg.isEmpty()) {
				if (description.addMessageType(msg)) {
					parent.internalChanged(null);
					messageList.add(messageList.getSize(), msg);
				} else
					JOptionPane
							.showMessageDialog(
									this,
									"The name of the message is already a function name!",
									"Network message creation error",
									JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals("m-")) {
			if (!msgList.isSelectionEmpty()) {
				for (Object o : msgList.getSelectedValues()) {
					description.removeMessageType(o.toString());
				}
				messageList.clear();
				int i = 0;
				for (String s : description.listMessageTypes()) {
					messageList.add(i, s);
					i++;
				}
				parent.internalChanged(null);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		msgDelButton.setEnabled(description != null
				&& !msgList.isSelectionEmpty());
	}
}
