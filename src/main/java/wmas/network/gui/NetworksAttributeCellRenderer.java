package wmas.network.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class NetworksAttributeCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	JCheckBox cb = new JCheckBox();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		boolean grayed = false;
		if (value == null) {
			value = "";
			grayed = true;
		} else if (value instanceof Boolean) {

			if (isSelected) {
				cb.setBackground(table.getSelectionBackground());
				cb.setForeground(table.getSelectionForeground());
			} else {
				cb.setBackground(table.getBackground());
				cb.setForeground(table.getForeground());
			}
			if (hasFocus) {
				if (isSelected) {
					cb.setBorder(UIManager
							.getBorder("Table.focusSelectedCellHighlightBorder"));
				} else {
					cb.setBorder(UIManager
							.getBorder("Table.focusCellHighlightBorder"));
				}
			} else {
				cb.setBorder(noFocusBorder);
			}
			cb.setSelected((Boolean) value);
			cb.setHorizontalAlignment(CENTER);
			cb.setOpaque(isSelected);
			return cb;
		}
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);

		if (grayed) {
			setBackground(Color.LIGHT_GRAY);
			setOpaque(true);
		} else {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				setBackground(table.getBackground());
			}
			setOpaque(isSelected);
		}
		return this;
	}

}
