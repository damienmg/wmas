package wmas.gui.behaviour.physical;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wmas.behaviour.physical.DropBehaviour;
import wmas.behaviour.physical.LiftBehaviour;
import wmas.gui.GuiModificationListener;

public class DropLiftBehaviourEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private GuiModificationListener parent;
	private DropBehaviour dropBehaviour = null;
	private LiftBehaviour liftBehaviour = null;
	private JTextField field;
	private JCheckBox allField;

	private void constructPanel() {
		allField = new JCheckBox("Drop all?");
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(allField, c);
		add(new JLabel("Entity name prefix:"), c);
		field = new JTextField("");
		add(field, c);
		c.weighty = 1;
		add(new JLabel(), c);
		field.addActionListener(this);
		field.setActionCommand("set_value");
		allField.addActionListener(this);
		allField.setActionCommand("drop_all");
	}

	private void transfer() {
		allField.setVisible(dropBehaviour != null);
		field.setEnabled(dropBehaviour == null || !dropBehaviour.isDropAll());
		allField.setSelected(dropBehaviour != null && dropBehaviour.isDropAll());
		field.setText(dropBehaviour != null ? dropBehaviour.getNamePattern()
				: liftBehaviour.getNamePattern());
	}

	public DropLiftBehaviourEditor(LiftBehaviour behaviour,
			GuiModificationListener parent) {
		super(new GridBagLayout());
		constructPanel();
		this.parent = parent;
		this.liftBehaviour = behaviour;
		this.dropBehaviour = null;
		transfer();
	}

	public DropLiftBehaviourEditor(DropBehaviour behaviour,
			GuiModificationListener parent) {
		super(new GridBagLayout());
		constructPanel();
		this.parent = parent;
		this.liftBehaviour = null;
		this.dropBehaviour = behaviour;
		transfer();
	}

	public void setOwner(LiftBehaviour behaviour, GuiModificationListener parent) {
		this.parent = parent;
		this.liftBehaviour = behaviour;
		this.dropBehaviour = null;
		transfer();
	}

	public void setOwner(DropBehaviour behaviour, GuiModificationListener parent) {
		this.parent = parent;
		this.liftBehaviour = null;
		this.dropBehaviour = behaviour;
		transfer();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (dropBehaviour == null || liftBehaviour == null)
			return;
		if (liftBehaviour != null) {
			if (arg0.getActionCommand().equals("set_value")) {
				liftBehaviour.setNamePattern(field.toString());
				if (this.parent != null) {
					this.parent
							.representationChanged(new Object[] { liftBehaviour });
				}
			}
		} else if (dropBehaviour != null) {
			if (arg0.getActionCommand().equals("set_value")) {
				dropBehaviour.setNamePattern(field.toString());
				if (this.parent != null) {
					this.parent
							.representationChanged(new Object[] { dropBehaviour });
				}
			} else if (arg0.getActionCommand().equals("drop_all")) {
				dropBehaviour.setDropAll(allField.isSelected());
				field.setEnabled(!dropBehaviour.isDropAll());
				if (this.parent != null) {
					this.parent
							.representationChanged(new Object[] { dropBehaviour });
				}
			}
		}
	}
}
