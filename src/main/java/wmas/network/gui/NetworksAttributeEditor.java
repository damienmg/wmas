package wmas.network.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import wmas.gui.GuiModificationListener;
import wmas.network.NetworksAttribute;
import wmas.network.NetworksAttribute.NetworkAttribute;
import wmas.network.NetworksDescription;

public class NetworksAttributeEditor extends JScrollPane implements TableModel,
		MouseListener {
	private static final long serialVersionUID = 1L;

	private NetworksDescription description = null;
	private NetworksAttribute attribute = null;
	private GuiModificationListener parent = null;

	private JTable table = null;

	public NetworksAttributeEditor() {
		super();
		table = new JTable(this);
		getViewport().setView(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(false);
		table.addMouseListener(this);
		NetworksAttributeCellRenderer cellRenderer = new NetworksAttributeCellRenderer();
		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
		table.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
	}

	public void setParent(NetworksDescription descr, NetworksAttribute attr,
			GuiModificationListener parent) {
		this.description = descr;
		this.attribute = attr;
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
			return Double.class;
		case 2:
			return Integer.class;
		case 3:
			return Boolean.class;
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Network index";
		case 1:
			return "Communication range";
		case 2:
			return "Window size";
		case 3:
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
		if (description == null || attribute == null)
			return null;
		if (columnIndex == 0) {
			return rowIndex;
		}
		if (!attribute.hasNetwork(rowIndex))
			return null;
		NetworkAttribute na = attribute.getSupportedNetwork(rowIndex);
		switch (columnIndex) {
		case 1:
			return na.range == 0 ? "∞" : na.range;
		case 2:
			return na.window;
		case 3:
			return description.isReaderNetwork(rowIndex) ? na.reader : null;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (description == null || attribute == null)
			return false;
		if (columnIndex == 0) {
			return false;
		}
		if (!attribute.hasNetwork(rowIndex))
			return false;
		return (columnIndex != 3) || (description.isReaderNetwork(rowIndex));
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (description == null
				|| attribute == null
				|| columnIndex == 0
				|| !attribute.hasNetwork(rowIndex)
				|| ((columnIndex == 3) && !(description
						.isReaderNetwork(rowIndex)))) {
			return;
		}
		NetworkAttribute na = attribute.getSupportedNetwork(rowIndex);
		switch (columnIndex) {
		case 1:
			if (aValue.toString().equals("∞"))
				na.range = 0;
			else {
				try {
					na.range = Double.parseDouble(aValue.toString());
				} catch (NumberFormatException ex) {
					return;
				}
			}
			cellChanged(rowIndex, columnIndex);
			break;
		case 2:
			try {
				na.window = Integer.parseInt(aValue.toString());
			} catch (NumberFormatException ex) {
				return;
			}
			cellChanged(rowIndex, columnIndex);
			break;
		case 3:
			na.reader = (aValue instanceof Boolean) && ((Boolean) aValue);
			cellChanged(rowIndex, columnIndex);
			break;
		}
	}

	public void refresh() {
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

	void rowChanged(int row) {
		parent.internalChanged(null);
		TableModelEvent e = new TableModelEvent(this, row);
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

	@Override
	public void mouseClicked(MouseEvent e) {
		if (attribute == null || description == null)
			return;
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			int row = table.rowAtPoint(e.getPoint());
			int column = table.columnAtPoint(e.getPoint());
			if (row >= 0 && column == 0) {
				if (attribute.hasNetwork(row))
					attribute.delNetworkAttribute(row);
				else
					attribute.addNetworkAttribute(row, description);
				rowChanged(row);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
