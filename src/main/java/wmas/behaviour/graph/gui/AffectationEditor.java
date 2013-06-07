package wmas.behaviour.graph.gui;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.gui.GuiModificationListener;
import wmas.util.Doublet;

public class AffectationEditor extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	List<Doublet<Expression, Expression>> affectations = null;
	Object[] parent = null;
	private GuiModificationListener listener = null;
	private JTable table = null;

	public AffectationEditor() {
		super();
		table = new JTable(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
	}

	public void setOwner(List<Doublet<Expression, Expression>> affectations,
			Object[] parent, GuiModificationListener listener) {
		stopEditing();
		this.listener = listener;
		this.parent = parent;
		this.affectations = affectations;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return affectations.size();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public String getColumnName(int column) {
		return column == 0 ? "Variable to set" : "Value";
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= affectations.size())
			return "";
		Doublet<Expression, Expression> d = affectations.get(rowIndex);
		return columnIndex == 0 ? d.getFirst().toString() : d.getSecond()
				.toString();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			String s = aValue.toString();
			Doublet<Expression, Expression> d;
			if (rowIndex < affectations.size()) {
				d = affectations.get(rowIndex);
			} else {
				d = new Doublet<Expression, Expression>();
				d.setFirst(new Expression("a"));
				if (columnIndex == 0)
					d.setSecond(new Expression(0));
			}
			Expression e = new Expression(s);
			if (columnIndex == 0) {
				if (!e.isLeftValue()) {
					JOptionPane.showMessageDialog(table, "The expression '" + s
							+ "' is not a lvalue!", "Invalid lvalue",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (e.toString().equals(d.getFirst().toString()))
					return;
				d.setFirst(e);
			} else {
				if (e.toString().equals(d.getSecond().toString()))
					return;
				d.setSecond(new Expression(s));
			}
			fireTableCellUpdated(rowIndex, columnIndex);
			if (listener != null) {
				listener.internalChanged(parent);
			}

		} catch (ExpressionParseException exn) {
			JOptionPane.showMessageDialog(table, exn.getMessage(),
					"Invalid expression", JOptionPane.ERROR_MESSAGE);
		}
	}

	public JTable getTable() {
		return table;
	}

	public void add() {
		try {
			affectations.add(new Doublet<Expression, Expression>(
					new Expression("a"), new Expression(0)));
			stopEditing();
			fireTableRowsInserted(affectations.size(), affectations.size());
			if (listener != null) {
				listener.internalChanged(parent);
			}
		} catch (ExpressionParseException e) {
		}
	}

	public void del() {
		int i = table.getSelectedRow();
		if (i >= 0) {
			stopEditing();
			affectations.remove(i);
			fireTableRowsDeleted(i, i);
			if (listener != null) {
				listener.internalChanged(parent);
			}
		}
	}

	public void down() {
		int i = table.getSelectedRow();
		if (i >= 0 && i < affectations.size() - 1) {
			stopEditing();
			Doublet<Expression, Expression> o = affectations.remove(i);
			affectations.add(i + 1, o);
			fireTableRowsUpdated(i, i + 1);
			if (listener != null) {
				listener.internalChanged(parent);
			}
		}
	}

	public void up() {
		int i = table.getSelectedRow();
		if (i > 0 && i < affectations.size()) {
			stopEditing();
			Doublet<Expression, Expression> o = affectations.remove(i);
			affectations.add(i - 1, o);
			fireTableRowsUpdated(i - 1, i);
			if (listener != null) {
				listener.internalChanged(parent);
			}
		}
	}

	private void stopEditing() {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(
					table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}
	}

}
