package wmas.gui.behaviour.physical;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import wmas.behaviour.physical.SpeedAttribute;
import wmas.gui.GuiModificationListener;

public class SpeedAttributeEditor extends JPanel implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private SpeedAttribute attr;
	private JTable attrEditor;
	private JButton delAttr;
	private GuiModificationListener parent;
	private SpeedAttrTableCellRenderer cellRenderer = new SpeedAttrTableCellRenderer();

	public SpeedAttributeEditor(SpeedAttribute attr,
			GuiModificationListener parent) {
		super(new GridBagLayout());
		this.attr = attr;
		this.parent = parent;
		construct();
	}

	public void setAttribute(SpeedAttribute attr, GuiModificationListener parent) {
		this.attr = attr;
		this.parent = parent;
		valueChanged(null);
		((SpeedAttrTableModel) attrEditor.getModel())
				.fireTableStructureChanged();
		attrEditor.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
	}

	class SpeedAttrTableCellRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

		public Component getTableCellRendererComponent(JTable table,
				Object element, boolean isSelected, boolean hasFocus, int row,
				int column) {

			setFont(table.getFont());

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
			if (hasFocus) {
				if (isSelected) {
					setBorder(UIManager
							.getBorder("Table.focusSelectedCellHighlightBorder"));
				} else {
					setBorder(UIManager
							.getBorder("Table.focusCellHighlightBorder"));
				}
			} else {
				setBorder(noFocusBorder);
			}
			if (element instanceof Color) {
				setOpaque(true);
				Color newColor = (Color) element;
				setBackground(newColor);
				setText("");
			} else {
				setOpaque(isSelected);
				setText(element.toString());
			}
			return this;
		}
	}

	private class SpeedAttrTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 2;
		}

		private Color getRowColor(int rowIndex) {
			if (rowIndex == 0)
				return null;
			List<Color> c = new LinkedList<Color>(attr.listSupportedColors());
			c.remove(null);
			if (rowIndex > c.size())
				return null;
			return c.get(rowIndex - 1);
		}

		@Override
		public int getRowCount() {
			int s = attr.listSupportedColors().size();
			if (attr.hasSpeed(null))
				return s;
			return s + 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Color c = getRowColor(rowIndex);
			if (columnIndex == 0) {
				if (rowIndex == 0)
					return "default";
				return c;
			}
			return attr.getSpeed(c);
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Color";
			case 1:
				return "Speed";
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		public void add() {
			Color c = JColorChooser.showDialog(SpeedAttributeEditor.this,
					"Choose speed color", Color.BLACK);
			if (c != null) {
				if (!attr.hasSpeed(c)) {
					attr.addSpeed(c, 1.0);
					fireTableStructureChanged();
					attrEditor.getColumnModel().getColumn(0)
							.setCellRenderer(cellRenderer);
					if (parent != null) {
						parent.internalChanged(new Object[] { attr });
					}
				}
			}
		}

		public void delete() {
			int i = attrEditor.getSelectedRow();
			if (i > 0) {
				Color c = getRowColor(i);
				if (c != null) {
					attr.removeSpeed(c);
					fireTableRowsDeleted(i, i);
					if (parent != null) {
						parent.internalChanged(new Object[] { attr });
					}
				}
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			try {
				if (columnIndex == 1) {
					double v = Double.parseDouble(aValue.toString());
					Color c = getRowColor(rowIndex);
					attr.addSpeed(c, v);
					fireTableCellUpdated(rowIndex, columnIndex);
					if (parent != null) {
						parent.internalChanged(new Object[] { attr });
					}
				}
			} catch (NumberFormatException exn) {
			}
		}
	}

	private void construct() {
		attrEditor = new JTable(new SpeedAttrTableModel());
		JButton addAttr = new JButton("+");
		delAttr = new JButton("-");
		addAttr.setToolTipText("Add speed color");
		delAttr.setToolTipText("Delete speed color");
		addAttr.setActionCommand("+");
		delAttr.setActionCommand("-");
		delAttr.setEnabled(false);
		addAttr.addActionListener(this);
		delAttr.addActionListener(this);
		attrEditor.getSelectionModel().addListSelectionListener(this);
		attrEditor.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		tb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});
		tb.add(addAttr);
		tb.add(delAttr);
		tb.setFloatable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(tb, c);
		c.weighty = 1;
		add(new JScrollPane(attrEditor), c);
		attrEditor.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			((SpeedAttrTableModel) attrEditor.getModel()).add();
		} else if (e.getActionCommand().equals("-")) {
			((SpeedAttrTableModel) attrEditor.getModel()).delete();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		delAttr.setEnabled((attrEditor.getSelectedRow() > 0));
	}
}
