package wmas.behaviour.graph.gui;

import java.awt.FontMetrics;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.gui.GuiModificationListener;
import wmas.util.Doublet;

public class ExpressionConditionEditor extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	List<List<Doublet<Expression, Boolean>>> condition = null;
	List<Integer> indexes = new LinkedList<Integer>();
	int size = 0;
	private GuiModificationListener listener = null;
	private Object[] parent = null;
	private JTable table = null;

	public ExpressionConditionEditor() {
		super();
		table = new JTable(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setShowGrid(false);
		table.setTableHeader(null);
		FontMetrics fm = table.getFontMetrics(table.getFont());
		table.getColumnModel().getColumn(0)
				.setMaxWidth(fm.stringWidth(" ∨ "));
		table.getColumnModel().getColumn(1)
				.setMaxWidth(fm.stringWidth(" ∧ "));
		table.getColumnModel().getColumn(2).setMaxWidth(fm.stringWidth(" ¬ "));
		table.getColumnModel()
				.getColumn(2)
				.setCellEditor(
						new DefaultCellEditor(new JComboBox(new Object[] { "",
								"¬" })));
	}

	public void setOwner(List<List<Doublet<Expression, Boolean>>> condition,
			Object[] parent, GuiModificationListener listener) {
		stopEditing();
		this.listener = listener;
		this.condition = condition;
		this.parent = parent;
		computeSizes();
	}

	private void stopEditing() {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(
					table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}
	}

	private void computeSizes() {
		indexes.clear();
		size = 0;
		if (condition != null) {
			for (List<Doublet<Expression, Boolean>> m : condition) {
				indexes.add(size);
				size += m.size();
			}
		}
		fireTableDataChanged();
	}

	public void del() {
		del(table.getSelectedRow());
	}

	public void del(int index) {
		int disjunction = getDisjunction(index);
		if (disjunction < 0)
			return;
		int lemma = index - indexes.get(disjunction);
		if (condition.get(disjunction).size() == 1) {
			stopEditing();
			condition.remove(disjunction);
			indexes.remove(disjunction);
			size--;
			if (disjunction < indexes.size()) {
				ListIterator<Integer> it = indexes.listIterator(disjunction);
				while (it.hasNext()) {
					int ind = it.next();
					it.set(ind - 1);
				}
			}
		} else {
			stopEditing();
			condition.get(disjunction).remove(lemma);
			size--;
			if (disjunction < indexes.size() - 1) {
				ListIterator<Integer> it = indexes
						.listIterator(disjunction + 1);
				while (it.hasNext()) {
					int ind = it.next();
					it.set(ind - 1);
				}
			}
		}
		fireTableRowsDeleted(index, index);
		if (listener != null) {
			listener.internalChanged(parent);
		}
	}

	public void add() {
		if (condition == null)
			return;
		stopEditing();
		List<Doublet<Expression, Boolean>> newMap = new LinkedList<Doublet<Expression, Boolean>>();
		newMap.add(new Doublet<Expression, Boolean>(new Expression(0), true));
		condition.add(newMap);
		indexes.add(size);
		size++;
		fireTableRowsInserted(size, size);
		if (listener != null) {
			listener.internalChanged(parent);
		}
	}

	public void addLemma() {
		add(getDisjunction(table.getSelectedRow()));
	}

	public void add(int disjunction) {
		if (condition == null)
			return;
		if (disjunction >= 0 && disjunction < condition.size()) {
			List<Doublet<Expression, Boolean>> newMap = condition
					.get(disjunction);
			newMap.add(new Doublet<Expression, Boolean>(new Expression(0), true));
			size++;
			stopEditing();

			int c = 0;
			int index = size;
			if (disjunction < indexes.size() - 1) {
				ListIterator<Integer> it = indexes
						.listIterator(disjunction + 1);
				while (it.hasNext()) {
					int ind = it.next();
					it.set(ind + 1);
					c++;
					if (c == 2)
						index = ind + 1;
				}
			}
			fireTableRowsInserted(index, index);
			if (listener != null) {
				listener.internalChanged(parent);
			}
		}
	}

	public int getDisjunction(int index) {
		if (condition == null)
			return -1;
		if (index >= size || index < 0)
			return -1;
		int k = 0;
		for (Integer i : indexes) {
			if (index < i)
				return k - 1;
			k++;
		}
		return k - 1;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return size;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		int disjunction = getDisjunction(rowIndex);
		if (disjunction < 0)
			return null;
		int lemma = rowIndex - indexes.get(disjunction);
		switch (columnIndex) {
		case 0:
			return (disjunction != 0) && (lemma == 0) ? "∨" : "";
		case 1:
			return lemma == 0 ? "" : "∧";
		case 2:
			return condition.get(disjunction).get(lemma).getSecond() ? ""
					: "¬";
		case 3:
			return condition.get(disjunction).get(lemma).getFirst().toString();
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 1;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex != 2 && columnIndex != 3)
			return;
		int disjunction = getDisjunction(rowIndex);
		if (disjunction < 0)
			return;
		int lemma = rowIndex - indexes.get(disjunction);
		if (columnIndex == 3) {
			try {
				Expression e = new Expression(aValue.toString());
				if (e.toString().equals(
						condition.get(disjunction).get(lemma).toString()))
					return;
				condition.get(disjunction).get(lemma).setFirst(e);
				fireTableCellUpdated(rowIndex, columnIndex);
				if (listener != null) {
					listener.internalChanged(parent);
				}
			} catch (ExpressionParseException e) {
				JOptionPane.showMessageDialog(table, e.getMessage(),
						"Parse error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			boolean newValue = false;
			if (aValue instanceof Boolean)
				newValue = (Boolean) aValue;
			else
				newValue = !(aValue.toString().equals("¬"));
			if (aValue == condition.get(disjunction).get(lemma))
				return;
			condition.get(disjunction).get(lemma).setSecond(newValue);
			fireTableCellUpdated(rowIndex, columnIndex);
			if (listener != null) {
				listener.internalChanged(parent);
			}
		}
	}

	public JTable getTable() {
		return table;
	}
}
