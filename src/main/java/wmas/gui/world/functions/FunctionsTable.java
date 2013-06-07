package wmas.gui.world.functions;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import wmas.expression.Expression;
import wmas.world.functions.ExpressionFunction;
import wmas.world.functions.WorldFunctionAttribute;

public class FunctionsTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static List<String> emptyList = new LinkedList<String>();
	private static Expression zero = new Expression(0);

	FunctionsTableModel model;

	public FunctionsTable(FunctionWorldAttributeEditor parent,
			List<ExpressionFunction> functions) {
		super(new FunctionsTableModel(parent, functions));
		model = (FunctionsTableModel) (this.getModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		setShowGrid(false);
	}

	private void stopEditing() {
		if (isEditing()) {
			TableCellEditor cellEditor = getCellEditor(getEditingRow(),
					getEditingColumn());
			cellEditor.cancelCellEditing();
		}
	}

	public void setAttribute(WorldFunctionAttribute attr) {
		stopEditing();
		model.setFunctions(attr.getFunctions());
	}

	public void del() {
		model.delete(getSelectedRows());
	}

	public void add() {
		stopEditing();
		ExpressionFunction newFunction = new ExpressionFunction("f", zero,
				emptyList);
		model.add(newFunction);
	}
}
