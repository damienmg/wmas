package wmas.reports;

import java.awt.Container;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import wmas.util.Doublet;

public class ReportDescriptionTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<Doublet<String, String>> wholeList = new LinkedList<Doublet<String, String>>();

	private ReportDescription desc = null;
	private boolean modified = false;

	public ReportDescriptionTableModel() {
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return wholeList.size();
	}

	public void add(Container parent) {
		if (desc != null) {
			Object[] o = ReportDescription.possibleReport.keySet().toArray();
			String s = (String) JOptionPane.showInputDialog(parent,
					"Select value to report:", "Add value to report",
					JOptionPane.PLAIN_MESSAGE, null, o, o[0]);
			if (s != null) {
				modified = true;
				String arg = "x";
				if (desc.storedReport.containsKey(s)) {
					while (desc.storedReport.get(s).containsKey(arg))
						arg += "x";
				} else
					desc.storedReport.put(s, new HashMap<String, Boolean>());
				desc.storedReport.get(s).put(arg, false);
				refresh();
			}
		}
	}

	public void remove(int[] ids) {
		List<Doublet<String, String>> toRemove = new LinkedList<Doublet<String, String>>();
		for (int i : ids) {
			toRemove.add(wholeList.get(i));
		}
		for (Doublet<String, String> s : toRemove) {
			if (desc.storedReport.containsKey(s.getFirst())) {
				desc.storedReport.get(s.getFirst()).remove(s.getSecond());
				if (desc.storedReport.get(s.getFirst()).isEmpty())
					desc.storedReport.remove(s.getFirst());
			}

		}
		if (toRemove.size() > 0) {
			modified = true;
			refresh();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Doublet<String, String> d = wholeList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return d.getFirst();
		case 1:
			return ReportDescription.possibleReport.get(d.getFirst());
		case 2:
			return d.getSecond();
		case 3:
			return desc.storedReport.get(d.getFirst()).get(d.getSecond());
		}
		return null;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Report identifier";
		case 1:
			return "Cumulative?";
		case 2:
			return "Report parameter";
		case 3:
			return "Report all values?";
		}
		return super.getColumnName(column);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Doublet<String, String> d = wholeList.get(rowIndex);
		switch (columnIndex) {
		case 2:
			if (!desc.storedReport.get(d.getFirst()).containsKey(
					aValue.toString())
					&& !d.getSecond().equals(aValue.toString())) {
				desc.storedReport.get(d.getFirst()).put(aValue.toString(),
						desc.storedReport.get(d.getFirst()).get(d.getSecond()));
				desc.storedReport.get(d.getFirst()).remove(d.getSecond());
				d.setSecond(aValue.toString());
				modified = true;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
			break;
		case 3:
			if (aValue instanceof Boolean) {
				desc.storedReport.get(d.getFirst()).put(d.getSecond(),
						(Boolean) aValue);
				modified = true;
			}
			break;
		}
	}

	public void setDesc(ReportDescription desc) {
		this.desc = desc;
		modified = false;
		refresh();
	}

	private void refresh() {
		wholeList.clear();
		if (desc != null) {
			for (String s : desc.storedReport.keySet()) {
				for (String s2 : desc.storedReport.get(s).keySet()) {
					wholeList.add(new Doublet<String, String>(s, s2));
				}
			}
		}
		fireTableDataChanged();
	}

	public boolean isModified() {
		return modified;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		case 2:
			return String.class;
		case 3:
			return Boolean.class;
		}
		return super.getColumnClass(column);
	}
}
