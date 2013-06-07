package wmas.gui.world.functions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.gui.GuiModificationListener;
import wmas.gui.world.WorldAttributeEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.world.Model;
import wmas.world.World;
import wmas.world.functions.ExpressionFunction;
import wmas.world.functions.WorldFunctionAttribute;
import wmas.xml.XMLEntity;

public class FunctionWorldAttributeEditor extends JPanel implements
		WorldAttributeEditor, GuiModificationListener, ListSelectionListener,
		ActionListener {
	private static final long serialVersionUID = 7876968915794741317L;

	private static final Icon functionIcon = new ImageIcon(
			FunctionWorldAttributeEditor.class
					.getResource("icons/function.gif"));

	private boolean edit = true;
	private Object descr = null;

	private JButton delButton;
	private FunctionsTable table;

	public FunctionWorldAttributeEditor() {
		super(new GridBagLayout());

		table = new FunctionsTable(this, new LinkedList<ExpressionFunction>());
		table.getSelectionModel().addListSelectionListener(this);

		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		JButton b = new JButton("+");
		b.setToolTipText("Add a function");
		b.setActionCommand("add_fun");
		b.addActionListener(this);
		tb.add(b);

		b = new JButton("-");
		b.setToolTipText("Remove selected function(s)");
		b.setActionCommand("del_fun");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		delButton = b;

		tb.setFloatable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		add(tb, c);
		c.weighty = 1;
		add(new JScrollPane(table), c);
	}

	@Override
	public void setWorld(XMLEntity w, boolean isEditable) {
		if (w instanceof World) {
			edit = isEditable;
		} else {
			edit = true;
		}
	}

	@Override
	public boolean canEditObject(Object o) {
		if (o == null || !edit)
			return false;
		if (o instanceof Doublet<?, ?>) {
			if ((((Doublet<?, ?>) o).getFirst() instanceof String)
					&& (((Doublet<?, ?>) o).getSecond() instanceof XMLEntity)) {
				XMLEntity e = (XMLEntity) ((Doublet<?, ?>) o).getSecond();
				String s = (String) ((Doublet<?, ?>) o).getFirst();
				if (e instanceof World) {
					return ((World) e).getAttribute(s) instanceof WorldFunctionAttribute;
				} else if (e instanceof Model) {
					return ((Model) e).getAttribute(s) instanceof WorldFunctionAttribute;
				}
				return false;
			}
		}
		return (o instanceof WorldFunctionAttribute);
	}

	@Override
	public Component getEditor() {
		if (!edit) {
			return null;
		}
		return this;
	}

	@Override
	public Icon getIcon() {
		return functionIcon;
	}

	private WorldObjectTreeModel treeModel = null;

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public String getTitle() {
		if (treeModel == null)
			return "Functions";
		return treeModel.getTitle(descr);
	}

	@Override
	public boolean isModified() {
		if (treeModel == null)
			return false;
		return treeModel.isModified(descr);
	}

	@Override
	public void refresh() {
		if (edit) {
			((FunctionsTableModel) table.getModel()).fireTableDataChanged();
			repaint();
		}
	}

	@Override
	public void setObject(Object o) {
		if (o == null)
			return;
		if (o instanceof Doublet<?, ?>) {
			if ((((Doublet<?, ?>) o).getFirst() instanceof String)
					&& (((Doublet<?, ?>) o).getSecond() instanceof XMLEntity)) {
				XMLEntity e = (XMLEntity) ((Doublet<?, ?>) o).getSecond();
				String s = (String) ((Doublet<?, ?>) o).getFirst();
				if (e instanceof World) {
					if (((World) e).getAttribute(s) instanceof WorldFunctionAttribute) {
						descr = o;
						edit((WorldFunctionAttribute) ((World) e)
								.getAttribute(s));
					}
				} else if (e instanceof Model) {
					if (((Model) e).getAttribute(s) instanceof WorldFunctionAttribute) {
						descr = o;
						edit((WorldFunctionAttribute) ((Model) e)
								.getAttribute(s));
					}
				}
			}
		}
		if (o instanceof WorldFunctionAttribute) {
			descr = o;
			edit((WorldFunctionAttribute) o);
		}
	}

	private void edit(WorldFunctionAttribute attribute) {
		table.setAttribute(attribute);
	}

	private HashSet<GuiModificationListener> guiListeners = new HashSet<GuiModificationListener>();

	public void addGuiModificationListener(GuiModificationListener l) {
		guiListeners.add(l);
	}

	public void removeGuiModificationListener(GuiModificationListener l) {
		guiListeners.remove(l);
	}

	private Object[] expandGuiEvent(Object[] o) {
		Object[] r;
		if (o == null)
			return new Object[] { descr };

		r = new Object[o.length + 1];
		int i = 0;
		r[0] = descr;
		for (i = 0; i < o.length; i++) {
			r[i + 1] = o[i];
		}
		return r;
	}

	@Override
	public void internalChanged(Object[] o) {
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(r);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(r);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		delButton.setEnabled(table.getSelectedRowCount() > 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s.equals("add_fun")) {
			table.add();
		} else if (s.equals("del_fun") && table.getSelectedRowCount() > 0) {
			table.del();
		}
	}

}
