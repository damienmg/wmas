package wmas.gui.shapes.editors;

import javax.swing.table.AbstractTableModel;

import wmas.geometry.Path;
import wmas.geometry.Point;

public class PathTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private Path p = null;
	PathTable parent = null;

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return p == null ? 0 : p.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (p == null)
			return null;
		if (rowIndex < 0 || rowIndex >= p.size())
			return null;

		Point point = p.get(rowIndex);
		return columnIndex == 0 ? point.x : point.y;
	}

	public Path getPath() {
		return p;
	}

	public void setPath(Path p) {
		this.p = p;
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int column) {
		return column == 0 ? "x" : "y";
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			if (p == null)
				return;
			if (rowIndex < 0 || rowIndex >= p.size())
				return;
			Point point = p.get(rowIndex);
			if (columnIndex == 0)
				point.x = Double.parseDouble(aValue.toString());
			else if (columnIndex == 1)
				point.y = Double.parseDouble(aValue.toString());
			fireTableCellUpdated(rowIndex, columnIndex);
			if (parent != null)
				parent.action();
		} catch (NumberFormatException exn) {
		}
	}

	public void add() {
		if (p == null)
			return;
		p.add(new Point());
		fireTableRowsInserted(p.size() - 1, p.size() - 1);
		parent.action();
	}

	public void delete(int index) {
		if (p == null)
			return;
		if (index < 0 || index >= p.size())
			return;
		p.remove(index);
		fireTableRowsDeleted(index, index);
		if (parent != null)
			parent.action();
	}
}
