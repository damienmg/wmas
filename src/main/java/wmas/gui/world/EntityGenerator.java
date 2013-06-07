package wmas.gui.world;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import wmas.world.EntityInterface;
import wmas.world.EntityReference;

public class EntityGenerator extends JDialog {

	private static final long serialVersionUID = 1L;

	WorldEditorPanel parentPanel;
	EntityInterface orig;

	JSpinner nbEntity = new JSpinner(new SpinnerNumberModel(1, 1,
			Integer.MAX_VALUE, 5));
	JTextField x0 = new JFormattedTextField(NumberFormat.getInstance());
	JTextField y0 = new JFormattedTextField(NumberFormat.getInstance());
	JTextField dx = new JFormattedTextField(NumberFormat.getInstance());
	JTextField dy = new JFormattedTextField(NumberFormat.getInstance());
	JTextField maxDistance = new JFormattedTextField(NumberFormat.getInstance());
	JCheckBox number = new JCheckBox(
			"Add an automatic number at the end of entity name?");
	JCheckBox zigzag = new JCheckBox("All entities on the same line?");
	JButton runButton = new JButton("Generate!");
	JButton cancelButton = new JButton("Cancel!");

	private boolean copy = true;

	private void generateEntityList(int nb, double x0, double y0, double dx,
			double dy, double maxDistance) {
		double x = x0;
		double y = y0;
		double d0 = Math.sqrt(dx * dx + dy * dy);
		double d = 0;
		boolean turn = false;
		for (int i = 0; i < nb; i++) {
			EntityInterface e = null;
			if (copy)
				e = orig.copy();
			else
				e = new EntityReference(orig);
			if (number.isSelected())
				e.setName(e.getName() + (i + 1));
			e.setPosition(x, y);
			parentPanel.add(e);
			d += d0;
			if (d >= maxDistance) {
				if (!zigzag.isSelected()) {
					if (turn) {
						x += -dy;
						y += dx;
					} else {
						x += dy;
						y += -dx;
					}
				}
				turn = !turn;
				dx = -dx;
				dy = -dy;
				d = 0;
			} else {
				x += dx;
				y += dy;
			}
		}
	}

	private void generate() {
		generateEntityList((Integer) nbEntity.getValue(),
				Double.parseDouble(x0.getText()),
				Double.parseDouble(y0.getText()),
				Double.parseDouble(dx.getText()),
				Double.parseDouble(dy.getText()),
				Double.parseDouble(maxDistance.getText()));
	}

	public static EntityGenerator getInstance(WorldEditorPanel parent) {
		Container owner = parent.parentFrame();
		if (owner == null) {
			return new EntityGenerator(parent);
		} else if (owner instanceof Frame) {
			return new EntityGenerator((Frame) owner, parent);
		} else if (owner instanceof Dialog) {
			return new EntityGenerator((Dialog) owner, parent);
		} else {
			return new EntityGenerator(parent);
		}
	}

	private EntityGenerator(WorldEditorPanel parent) {
		super();
		this.setTitle(parent.getTitle() + " - Add several entities");
		this.parentPanel = parent;
		construct();
	}

	private EntityGenerator(Frame owner, WorldEditorPanel parent) {
		super(owner, parent.getTitle() + " - Add several entities", true);
		this.parentPanel = parent;
		construct();
	}

	private EntityGenerator(Dialog owner, WorldEditorPanel parent) {
		super(owner, parent.getTitle() + " - Add several entities", true);
		this.parentPanel = parent;
		construct();
	}

	private void construct() {
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generate();
				setVisible(false);
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getRootPane().setLayout(new GridBagLayout());
		getRootPane().removeAll();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		getRootPane().add(new JLabel("Number of entity to generate: "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(nbEntity, c);

		c.gridwidth = 1;
		getRootPane().add(new JLabel("Orignal position: "), c);
		getRootPane().add(x0, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(y0, c);

		c.weightx = 1;
		c.gridwidth = 1;
		getRootPane().add(new JLabel("Deplacement vector: "), c);
		getRootPane().add(dx, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(dy, c);

		c.weightx = 1;
		c.gridwidth = 1;
		getRootPane().add(new JLabel("Maximum distance to cover: "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(maxDistance, c);

		getRootPane().add(number, c);
		getRootPane().add(zigzag, c);

		c.gridwidth = 1;
		getRootPane().add(new JLabel(), c);
		c.gridwidth = 1;
		c.weightx = 0;
		getRootPane().add(cancelButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(runButton, c);

		pack();
		setResizable(false);
	}

	public void display(EntityInterface e, double x, double y, boolean copy) {
		this.orig = e;
		this.x0.setText(Double.toString(x));
		this.y0.setText(Double.toString(y));
		double[] d = (e.getDrawingShape() != null ? e.getDrawingShape()
				.getBounds() : null);
		double delta = 20;
		if (d != null)
			delta = Math.max(d[2], d[3]);
		this.dy.setText(Double.toString(delta));
		this.dx.setText("0");
		this.number.setSelected(true);
		this.maxDistance.setText(Double.toString(20 * delta));
		this.nbEntity.setValue(100);
		this.copy = copy;
		setLocationRelativeTo(parentPanel);
		setVisible(true);
	}

}
