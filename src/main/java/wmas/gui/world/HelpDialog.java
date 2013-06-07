package wmas.gui.world;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.expression.functions.FunctionList;
import wmas.gui.world.entity.EntityEditor;
import wmas.util.Doublet;

public class HelpDialog extends JDialog {
	Container parent = null;

	public HelpDialog(Dialog owner, String title) {
		super(owner, title + " - Help", false);
		parent = owner;
		construct();
	}

	public HelpDialog(Frame owner, String title) {
		super(owner, title + " - Help", false);
		parent = owner;
		construct();
	}

	private static final long serialVersionUID = 1L;
	private JTabbedPane pane = new JTabbedPane();

	private class AttributeTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		List<String> availablesAttributes = new LinkedList<String>();
		int type;

		public AttributeTableModel(int type) {
			super();
			switch (type) {
			case 0:
				availablesAttributes.addAll(WorldEditor.listAttributes());
				break;
			case 1:
				availablesAttributes.addAll(EntityEditor.listAttributes());
				break;
			case 2:
				availablesAttributes.addAll(BehaviourGraphFactory
						.listBehaviours());
				break;
			}
			this.type = type;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return availablesAttributes.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String attrName = availablesAttributes.get(rowIndex);
			if (columnIndex == 0)
				return attrName;
			switch (type) {
			case 0:
				return WorldEditor.getDescription(attrName);
			case 1:
				return EntityEditor.getDescription(attrName);
			case 2:
				return BehaviourGraphFactory.getDescription(attrName);
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	private class FunctionTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		List<String> availablesAttributes = new LinkedList<String>();
		int type;

		public FunctionTableModel(int type) {
			super();
			switch (type) {
			case 0:
				availablesAttributes.addAll(FunctionList.listPrefix());
				break;
			case 1:
				availablesAttributes.addAll(FunctionList.listInfix());
				break;
			case 2:
				availablesAttributes.addAll(FunctionList.listFunctions());
				break;
			}
			this.type = type;
		}

		@Override
		public int getColumnCount() {
			return type == 2 ? 3 : 2;
		}

		@Override
		public int getRowCount() {
			return availablesAttributes.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String attrName = availablesAttributes.get(rowIndex);
			if (columnIndex == 0)
				return attrName;
			if (columnIndex == 2 || type != 2) {
				return FunctionList.getDescription(attrName);
			}
			Doublet<Integer, Integer> r = FunctionList.getArgsNumber(attrName);
			if (r == null)
				return "";
			if (r.getFirst() == r.getSecond()) {
				return r.getFirst();
			}
			if (r.getSecond() == Integer.MAX_VALUE) {
				return r.getFirst().toString() + " - ∞";
			}
			return r.getFirst() + " - " + r.getSecond();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return type == 2 ? "Function name" : "Operator";
			case 1:
				return type == 2 ? "Number of arguments" : "Description";
			case 2:
				return "Description";
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

	}

	private void construct() {
		JTable bTable = new JTable(new AttributeTableModel(2));
		JTable eTable = new JTable(new AttributeTableModel(1));
		JTable wTable = new JTable(new AttributeTableModel(0));
		JTable unaTable = new JTable(new FunctionTableModel(0));
		JTable binTable = new JTable(new FunctionTableModel(1));
		JTable funTable = new JTable(new FunctionTableModel(2));
		bTable.setTableHeader(null);
		eTable.setTableHeader(null);
		wTable.setTableHeader(null);
		pane.addTab("Help", new JPanel());
		pane.addTab("Available behaviours", new JScrollPane(bTable));
		pane.addTab("Available world attributes", new JScrollPane(wTable));
		pane.addTab("Available entity attributes", new JScrollPane(eTable));
		JTabbedPane pane2 = new JTabbedPane();
		pane2.addTab("Unary operators", new JScrollPane(unaTable));
		pane2.addTab("Binary operators", new JScrollPane(binTable));
		pane2.addTab("Functions", new JScrollPane(funTable));
		pane.addTab("Available functions and operators", pane2);
		add(pane);
		setSize(600, 400);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	public void setVisible(boolean isVisible) {
		if (isVisible && !this.isVisible())
			setLocationRelativeTo(parent);
		super.setVisible(isVisible);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title + " - Help");
	}
}
