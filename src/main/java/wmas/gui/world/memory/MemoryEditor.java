package wmas.gui.world.memory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wmas.gui.GuiModificationListener;
import wmas.world.memory.Memory;

public class MemoryEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private GuiModificationListener parent;
	private Memory mem;
	private JTextField field;

	private void constructPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JLabel("Memory size:"), c);
		field = new JTextField("");
		add(field, c);
		c.weighty = 1;
		add(new JLabel(), c);
		field.addActionListener(this);
		field.setActionCommand("set_value");
	}

	public MemoryEditor(Memory mem, GuiModificationListener parent) {
		super(new GridBagLayout());
		constructPanel();
		setOwner(mem, parent);
	}

	public void setOwner(Memory mem, GuiModificationListener parent) {
		this.parent = parent;
		this.mem = mem;
		if (mem != null)
			field.setText(mem.getMemorySize());
	}

	public void actionPerformed(ActionEvent arg0) {
		if (mem == null)
			return;

		if (arg0.getActionCommand().equals("set_value")) {
			if (mem.setMemorySize(field.getText())) {
				if (this.parent != null) {
					this.parent.representationChanged(new Object[] { mem });
				}
			}
			field.setText(mem.getMemorySize());
		}
	}
}
