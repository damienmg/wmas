package wmas.behaviour.graph;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.behaviour.graph.gui.AffectationEditor;
import wmas.behaviour.graph.gui.ExpressionConditionEditor;
import wmas.gui.GuiModificationListener;

class GraphTransitionEditor extends JSplitPane implements ActionListener,
		GuiModificationListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private GraphTransition transition;

	private AffectationEditor affectations;
	private ExpressionConditionEditor conditions;

	private JButton upButton;
	private JButton downButton;
	private JButton delButton;

	private JButton delCondButton;
	private JButton addLemmaButton;

	private JPanel constructConditionPanel() {
		conditions = new ExpressionConditionEditor();

		conditions.getTable().getSelectionModel()
				.addListSelectionListener(this);

		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		JButton b = new JButton("∨ ...");
		b.setToolTipText("Add a conjunctive clause");
		b.setActionCommand("add_conj");
		b.addActionListener(this);
		tb.add(b);

		b = new JButton("∧ ...");
		b.setToolTipText("Add a clause");
		b.setActionCommand("add_cond");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		addLemmaButton = b;

		b = new JButton("-");
		b.setToolTipText("Remove selected clause");
		b.setActionCommand("del_cond");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		delCondButton = b;

		tb.setFloatable(false);

		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		pane.add(tb, c);
		c.weighty = 1;
		pane.add(new JScrollPane(conditions.getTable()), c);
		return pane;
	}

	private JPanel constructAffectationPanel() {
		affectations = new AffectationEditor();

		affectations.getTable().getSelectionModel()
				.addListSelectionListener(this);

		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		JButton b = new JButton("+");
		b.setToolTipText("Add an affectation");
		b.setActionCommand("add_affect");
		b.addActionListener(this);
		tb.add(b);

		b = new JButton("-");
		b.setToolTipText("Remove selected affectation");
		b.setActionCommand("del_affect");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		delButton = b;

		tb.add(new JSeparator() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
			}
		});
		b = new JButton("v");
		b.setToolTipText("Move selected affectation up");
		b.setActionCommand("down_affect");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		downButton = b;

		b = new JButton("^");
		b.setToolTipText("Move selected affectation down");
		b.setActionCommand("up_affect");
		b.addActionListener(this);
		tb.add(b);
		b.setEnabled(false);
		upButton = b;

		tb.setFloatable(false);

		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		pane.add(tb, c);
		c.weighty = 1;
		pane.add(new JScrollPane(affectations.getTable()), c);
		return pane;
	}

	private void changedGrayed() {
		int i = affectations.getTable().getSelectedRow();
		delButton.setEnabled(i >= 0);
		upButton.setEnabled(i > 0);
		downButton.setEnabled(i >= 0 && i < transition.affectations.size() - 1);
		i = conditions.getTable().getSelectedRow();
		delCondButton.setEnabled(i >= 0);
		addLemmaButton.setEnabled(i >= 0);
	}

	private void construct() {
		setRightComponent(constructAffectationPanel());
		setLeftComponent(constructConditionPanel());
		setDividerLocation(200);
	}

	public GraphTransitionEditor() {
		super(JSplitPane.VERTICAL_SPLIT);
		construct();
	}

	public void setCondition(GraphTransition cond) {
		this.transition = cond;
		if (cond != null) {
			Object[] p = new Object[] { cond };
			this.conditions.setOwner(cond.condition, p, this);
			this.affectations.setOwner(cond.affectations, p, this);
		} else {
			this.conditions.setOwner(null, null, this);
			this.affectations.setOwner(null, null, this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("add_affect")) {
			affectations.add();
		} else if (e.getActionCommand().equals("del_affect")) {
			affectations.del();
		} else if (e.getActionCommand().equals("down_affect")) {
			affectations.down();
		} else if (e.getActionCommand().equals("up_affect")) {
			affectations.up();
		} else if (e.getActionCommand().equals("del_cond")) {
			conditions.del();
		} else if (e.getActionCommand().equals("add_conj")) {
			conditions.add();
		} else if (e.getActionCommand().equals("add_cond")) {
			conditions.addLemma();
		}
	}

	private HashSet<GuiModificationListener> guiListeners = new HashSet<GuiModificationListener>();

	public void addGuiModificationListener(GuiModificationListener l) {
		guiListeners.add(l);
	}

	public void removeGuiModificationListener(GuiModificationListener l) {
		guiListeners.remove(l);
	}

	@Override
	public void internalChanged(Object[] o) {
		changedGrayed();
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(o);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		changedGrayed();
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(o);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		changedGrayed();
	}
}
