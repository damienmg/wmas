package wmas.gui.behaviour.simple;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class BehaviourGraphEditor extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private BehaviourGraphFactory parent;
	private BehaviourGraph behaviour;
	private JTextField nameField;
	private JLabel nameLabel;
	private JButton behaviourSelectorButton;

	private void constructPanel() {
		GridBagConstraints c = new GridBagConstraints();
		GridBagLayout layout = (GridBagLayout) getLayout();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0.0;

		nameField = new JTextField("", 10);
		nameLabel = new JLabel("Name:");
		layout.setConstraints(nameField, c);
		layout.setConstraints(nameLabel, c);
		add(nameLabel);
		add(nameField);
		nameField.addActionListener(this);
		nameField.setActionCommand("set_name");

		c.weightx = 0.0;
		JButton b = new JButton("Load file...");
		b.setActionCommand("load");
		b.addActionListener(this);
		layout.setConstraints(b, c);
		add(b);

		behaviourSelectorButton = new JButton("Select behaviour...");
		behaviourSelectorButton.setActionCommand("select");
		behaviourSelectorButton.addActionListener(this);
		layout.setConstraints(behaviourSelectorButton, c);
		add(behaviourSelectorButton);

		b = new JButton("Edit...");
		b.setActionCommand("edit");
		b.addActionListener(this);
		layout.setConstraints(b, c);
		add(b);

		c.weightx = 1.0;
		c.weighty = 1.0;
		JLabel lab = new JLabel();
		layout.setConstraints(lab, c);
		add(lab);
	}

	public BehaviourGraphEditor(BehaviourGraphFactory parent,
			BehaviourGraph behaviour) {
		super(new GridBagLayout());
		constructPanel();
		setOwner(parent, behaviour);
	}

	public void setOwner(BehaviourGraphFactory parent, BehaviourGraph behaviour) {
		this.parent = parent;
		this.behaviour = behaviour;
		if (behaviour != null)
			nameField.setText(behaviour.getName());
		behaviourSelectorButton
				.setVisible(parent.getAvailableBehaviours() != null);
	}

	private JFileChooser chooser = new JFileChooser();

	public void actionPerformed(ActionEvent arg0) {
		if (behaviour == null)
			return;

		if (arg0.getActionCommand().equals("set_name")) {
			behaviour.setName(nameField.getText());
			parent.getSchemeView().repaint();
		} else if (arg0.getActionCommand().equals("load")) {
			int returnVal = chooser.showOpenDialog(parent.getRootView());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					XMLEntity xe = XMLInterpretor.convert(chooser
							.getSelectedFile().getCanonicalPath(), null);
					if (xe instanceof BehaviourGraph) {
						BehaviourGraph g = (BehaviourGraph) xe;
						behaviour.copy(g);
						nameField.setText(behaviour.getName());
						parent.getSchemeView().repaint();
					} else {
						JOptionPane.showMessageDialog(parent.getRootView(),
								"Loading failed!");
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(parent.getRootView(),
							"Loading failed!");
				}
			}
		} else if (arg0.getActionCommand().equals("edit")) {
			parent.loadSubGraph(this.behaviour);
		} else if (arg0.getActionCommand().equals("select")) {
			Object[] possibilities = parent.getAvailableBehaviours().toArray();
			Object s = JOptionPane.showInputDialog(this,
					"Please select the behaviour to add:",
					"Behaviour selection", JOptionPane.PLAIN_MESSAGE, null,
					possibilities, possibilities[0]);
			if ((s != null) && (s instanceof Behaviour)) {
				Behaviour b = (Behaviour) s;
				parent.replaceBehaviour(this.behaviour, b);
			}
		}
	}
}
