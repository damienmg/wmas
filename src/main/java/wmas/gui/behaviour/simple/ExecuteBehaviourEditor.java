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
import wmas.behaviour.simple.ExecuteBehaviour;
import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;

public class ExecuteBehaviourEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private BehaviourGraphFactory parent;
	private ExecuteBehaviour behaviour;
	private JTextField behaviourField;
	private JLabel behaviourLabel;

	private void constructPanel() {
		GridBagConstraints c = new GridBagConstraints();
		GridBagLayout layout = (GridBagLayout) getLayout();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.0;

		behaviourField = new JTextField("1.0", 10);
		behaviourLabel = new JLabel("Behaviour:");
		layout.setConstraints(behaviourField, c);
		layout.setConstraints(behaviourLabel, c);
		add(behaviourLabel);
		add(behaviourField);
		behaviourField.addActionListener(this);
		behaviourField.setActionCommand("expr");
		c.weighty = 1.0;
		JLabel lab = new JLabel();
		layout.setConstraints(lab, c);
		add(lab);
	}

	public ExecuteBehaviourEditor(BehaviourGraphFactory parent,
			ExecuteBehaviour behaviour) {
		super(new GridBagLayout());
		constructPanel();
		setOwner(parent, behaviour);
	}

	public void setOwner(BehaviourGraphFactory parent,
			ExecuteBehaviour behaviour) {
		this.parent = parent;
		this.behaviour = behaviour;
		if (behaviour != null)
			behaviourField.setText(behaviour.getBehaviourExpression()
					.toString());
	}

	public void actionPerformed(ActionEvent arg0) {
		if (behaviour == null)
			return;

		if (arg0.getActionCommand().equals("expr")) {
			try {
				behaviour.setBehaviourExpression(new Expression(behaviourField
						.getText()));
				if (this.parent != null) {
					this.parent.representationChanged(new Object[] { this });
					this.parent.getSchemeView().repaint();
				}
			} catch (ExpressionParseException exn) {
				JOptionPane.showMessageDialog(this, exn.getMessage(),
						"Invalid expression", JOptionPane.ERROR_MESSAGE);
				behaviourField.setText(behaviour.getBehaviourExpression()
						.toString());
			}
		}
	}
}
