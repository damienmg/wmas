package wmas.gui.behaviour.simple;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.behaviour.simple.WaitBehaviour;
import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;

public class WaitBehaviourEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private BehaviourGraphFactory parent;
	private WaitBehaviour behaviour;
	private JTextField durationField;
	private JLabel durationLabel;

	private void constructPanel() {
		GridBagConstraints c = new GridBagConstraints();
		GridBagLayout layout = (GridBagLayout) getLayout();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.0;

		durationField = new JTextField("1.0", 10);
		durationLabel = new JLabel("Duration:");
		layout.setConstraints(durationField, c);
		layout.setConstraints(durationLabel, c);
		add(durationLabel);
		add(durationField);
		durationField.addActionListener(this);
		durationField.setActionCommand("sleep_time");
		c.weighty = 1.0;
		JLabel lab = new JLabel();
		layout.setConstraints(lab, c);
		add(lab);
	}

	public WaitBehaviourEditor(BehaviourGraphFactory parent,
			WaitBehaviour behaviour) {
		super(new GridBagLayout());
		constructPanel();
		setOwner(parent, behaviour);
	}

	public void setOwner(BehaviourGraphFactory parent, WaitBehaviour behaviour) {
		this.parent = parent;
		this.behaviour = behaviour;
		if (behaviour != null)
			durationField.setText(behaviour.getWaitingTime().toString());
	}

	public void actionPerformed(ActionEvent arg0) {
		if (behaviour == null)
			return;

		if (arg0.getActionCommand().equals("sleep_time")) {
			try {
				behaviour
						.setWaitingTime(new Expression(durationField.getText()));
				if (this.parent != null) {
					this.parent.representationChanged(new Object[] { this });
					this.parent.getSchemeView().repaint();
				}
			} catch (ExpressionParseException exn) {
				JOptionPane.showMessageDialog(this, exn.getMessage(),
						"Invalid expression", JOptionPane.ERROR_MESSAGE);
				durationField.setText(behaviour.getWaitingTime().toString());
			}
		}
	}
}
