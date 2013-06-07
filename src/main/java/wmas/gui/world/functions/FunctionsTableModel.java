package wmas.gui.world.functions;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.expression.functions.FunctionList;
import wmas.world.functions.ExpressionFunction;

public class FunctionsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	public static final String functionRegex = "^\\s*"
			+ FunctionList.identRegex + "\\s*\\(\\s*("
			+ FunctionList.identRegex + "\\s*)?(,\\s*"
			+ FunctionList.identRegex + "\\s*)*\\)\\s*$";
	private List<ExpressionFunction> functions;
	private FunctionWorldAttributeEditor parent;

	public FunctionsTableModel(FunctionWorldAttributeEditor parent,
			List<ExpressionFunction> functions) {
		this.parent = parent;
		this.functions = functions;
	}

	public void setFunctions(List<ExpressionFunction> functions) {
		this.functions = functions;
		this.fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return functions.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex > functions.size()
				|| (columnIndex != 0 && columnIndex != 1))
			return null;
		ExpressionFunction f = functions.get(rowIndex);
		if (f == null)
			return null;
		if (columnIndex == 0)
			return f.getName() + "("
					+ ExpressionFunction.listToString(f.getParameters()) + ")";
		return f.getExpression().toString();
	}

	public void add(ExpressionFunction newFunction) {
		functions.add(newFunction);
		this.fireTableRowsInserted(functions.size() - 1, functions.size());
		this.parent.internalChanged(new Object[] { newFunction });
	}

	static LinkedList<ExpressionFunction> removing = new LinkedList<ExpressionFunction>();

	public void delete(int[] is) {
		removing.clear();
		for (int i : is) {
			removing.add(functions.get(i));
		}
		functions.removeAll(removing);
		for (ExpressionFunction f : removing)
			f.unregister();
		removing.clear();
		this.fireTableDataChanged();
		this.parent.internalChanged(new Object[] {});
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex > functions.size()
				|| (columnIndex != 0 && columnIndex != 1))
			return;
		ExpressionFunction f = functions.get(rowIndex);
		if (f == null)
			return;

		if (columnIndex == 0) {
			String r = aValue.toString();
			r = r.trim();
			if (!r.matches(functionRegex)) {
				JOptionPane.showMessageDialog(this.parent,
						"Invalid function format!", "Invalid input",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int c = r.indexOf('(');
			String name = r.substring(0, c).trim();
			String oldname = f.getName();
			String oldPars = f.getParameters().toString();
			String parameters = r.substring(c + 1).trim();
			if (parameters.charAt(parameters.length() - 1) != ')') {
				JOptionPane.showMessageDialog(this.parent,
						"Invalid function format!", "Invalid input",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			parameters = parameters.substring(0, parameters.length() - 1)
					.trim();
			try {
				f.setFunction(name, parameters);
				this.parent.internalChanged(new Object[] { f });
			} catch (Exception exn) {
				JOptionPane.showMessageDialog(this.parent,
						"Invalid function format:\n" + exn.getMessage(),
						"Invalid input", JOptionPane.ERROR_MESSAGE);
				try {
					f.setFunction(oldname, oldPars);
				} catch (Exception ex) {
				}
			}
		} else {
			try {
				f.setExpression(new Expression(aValue.toString()));
				this.parent.internalChanged(new Object[] { f });
			} catch (ExpressionParseException exn) {
				JOptionPane.showMessageDialog(this.parent,
						"Invalid function format:\n" + exn.getMessage(),
						"Invalid input", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public String getColumnName(int column) {
		return column == 0 ? "Name" : "Value";
	}
}
