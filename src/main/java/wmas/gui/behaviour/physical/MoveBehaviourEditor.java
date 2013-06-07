package wmas.gui.behaviour.physical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import wmas.behaviour.physical.MoveBehaviour;
import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.geometry.Path;
import wmas.gui.GuiModificationListener;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.SchemeViewListener;
import wmas.gui.shapes.ShapeListener;
import wmas.gui.shapes.editors.PathTable;
import wmas.gui.shapes.elements.Cross;
import wmas.gui.shapes.elements.DrawablePath;
import wmas.gui.shapes.elements.Line;

public class MoveBehaviourEditor extends JPanel implements ActionListener,
		ShapeListener, SchemeViewListener {
	private static final long serialVersionUID = 1L;

	private MoveBehaviour behaviour;

	// Generics
	private JLabel speedLabel;
	private JTextField speedField;
	private JCheckBox infiniteSpeed;
	private JComboBox typeChooser;
	protected JButton colorLabel;
	protected ImageIcon colorIcon;
	protected JLabel colorLabelLabel;

	// Relative and Absolute
	private JLabel xLabel;
	private JTextField xField;
	private JLabel yLabel;
	private JTextField yField;
	// Path
	private JLabel pathLabel;
	private PathTable pathTable;
	// Cover
	private JLabel coverLabel;
	private JComboBox coverShapeList;

	private JLabel padLabel;
	// edit on worldView
	private DrawablePath drawPath;
	private Cross drawPoint;
	private Line drawArrow;
	private SchemeView worldView;

	private GuiModificationListener parent;

	public MoveBehaviourEditor(MoveBehaviour behaviour,
			GuiModificationListener parent, SchemeView worldView) {
		super(new GridBagLayout());
		this.behaviour = behaviour;
		this.parent = parent;
		this.worldView = worldView;
		if (this.worldView != null)
			this.worldView.addListener(this);
		construct();
		transfer();
	}

	public void setBehaviour(MoveBehaviour behaviour,
			GuiModificationListener parent, SchemeView worldView) {
		if (this.worldView != null
				&& this.worldView.contains(behaviour.getToCover())) {
			this.worldView.remove(behaviour.getToCover());
			this.worldView.remove(drawPath);
			this.worldView.remove(drawPoint);
			this.worldView.remove(drawArrow);
		}
		this.behaviour = behaviour;
		this.parent = parent;
		if (this.worldView != worldView) {
			if (this.worldView != null)
				this.worldView.removeListener(this);
			this.worldView = worldView;
			if (this.worldView != null)
				this.worldView.addListener(this);
		}
		transfer();
	}

	private void setColorButton() {
		Color c = null;
		if (behaviour.getColoring() != null) {
			c = behaviour.getColoring();
		}
		Image i = colorIcon.getImage();
		Graphics g = i.getGraphics();
		if (c == null) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(
					AlphaComposite.CLEAR, 0.0f));
		} else
			g.setColor(c);
		g.fillRect(0, 0, 20, 20);
		g.dispose();
		i.flush();
		repaint();
	}

	private void transferPath() {
		if (behaviour.getPath() == null) {
			behaviour.setPath(new Path());
		}
		pathTable.setParent(worldView);
		pathTable.setPath(behaviour.getPath());
		drawPath.setPath(behaviour.getPath());
		xLabel.setVisible(false);
		xField.setVisible(false);
		yLabel.setVisible(false);
		yField.setVisible(false);
		pathLabel.setVisible(true);
		pathTable.setVisible(true);
		coverLabel.setVisible(false);
		coverShapeList.setVisible(false);
		padLabel.setVisible(false);
		if (worldView != null) {
			worldView.remove(behaviour.getToCover());
			worldView.remove(drawArrow);
			worldView.remove(drawPoint);
			worldView.add(drawPath);
			worldView.select(drawPath);
			worldView.repaint();
		}
	}

	private void transferAbsolute() {
		xLabel.setText("x = ");
		yLabel.setText("y = ");
		xField.setText(behaviour.getX().toString());
		yField.setText(behaviour.getY().toString());
		drawPoint.setPosition(behaviour.getX().getNumber().doubleValue(),
				behaviour.getY().getNumber().doubleValue());

		xLabel.setVisible(true);
		xField.setVisible(true);
		yLabel.setVisible(true);
		yField.setVisible(true);
		pathLabel.setVisible(false);
		pathTable.setVisible(false);
		coverLabel.setVisible(false);
		coverShapeList.setVisible(false);
		padLabel.setVisible(true);
		if (worldView != null) {
			worldView.remove(behaviour.getToCover());
			worldView.remove(drawPath);
			worldView.remove(drawArrow);
			worldView.add(drawPoint);
			worldView.select(drawPoint);
			worldView.repaint();
		}
	}

	private void transferRelative() {
		xLabel.setText("dx = ");
		yLabel.setText("dy = ");
		xField.setText(behaviour.getX().toString());
		yField.setText(behaviour.getY().toString());
		drawArrow.setSize(behaviour.getX().getNumber().doubleValue(), behaviour
				.getY().getNumber().doubleValue());

		xLabel.setVisible(true);
		xField.setVisible(true);
		yLabel.setVisible(true);
		yField.setVisible(true);
		pathLabel.setVisible(false);
		pathTable.setVisible(false);
		coverLabel.setVisible(false);
		coverShapeList.setVisible(false);
		padLabel.setVisible(true);
		if (worldView != null) {
			worldView.remove(behaviour.getToCover());
			worldView.remove(drawPath);
			worldView.remove(drawPoint);
			worldView.add(drawArrow);
			worldView.select(drawArrow);
			worldView.repaint();
		}
	}

	private void transferCover() {
		coverShapeList.removeAllItems();
		if (worldView != null) {
			worldView.remove(drawArrow);
			worldView.remove(drawPath);
			worldView.remove(drawPoint);
			worldView.add(behaviour.getToCover());
			worldView.select(behaviour.getToCover());
			for (DrawableShape ds : worldView.listShapes()) {
				if (!ds.isLine() && !(ds instanceof DrawablePath))
					coverShapeList.addItem(ds);
			}
		} else {
			coverShapeList.addItem(behaviour.getToCover());
		}
		coverShapeList.setSelectedItem(behaviour.getToCover());
		xLabel.setVisible(false);
		xField.setVisible(false);
		yLabel.setVisible(false);
		yField.setVisible(false);
		pathLabel.setVisible(false);
		pathTable.setVisible(false);
		coverLabel.setVisible(true);
		coverShapeList.setVisible(true);
		padLabel.setVisible(true);
		if (worldView != null) {
			worldView.repaint();
		}
	}

	private void transfer() {
		if (behaviour != null) {
			if (behaviour.getSpeed() == null) {
				infiniteSpeed.setSelected(true);
				speedField.setEnabled(false);
				speedField.setText("");
			} else {
				infiniteSpeed.setSelected(false);
				speedField.setEnabled(true);
				speedField.setText(behaviour.getSpeed().toString());
			}
			typeChooser.setSelectedIndex(behaviour.getType());
			setColorButton();
			switch (behaviour.getType()) {
			case MoveBehaviour.MOVE_TYPE_ABSOLUTE:
				transferAbsolute();
				break;
			case MoveBehaviour.MOVE_TYPE_RELATIVE:
				transferRelative();
				break;
			case MoveBehaviour.MOVE_TYPE_PATH:
				transferPath();
				break;
			case MoveBehaviour.MOVE_TYPE_COVER:
				transferCover();
				break;
			}
		}
	}

	private void construct() {
		Image i = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		colorIcon = new ImageIcon(i);
		colorLabel = new JButton(colorIcon);
		colorLabel.setActionCommand("color");
		colorLabel.addActionListener(this);
		colorLabelLabel = new JLabel("Coloring:");
		padLabel = new JLabel();

		speedLabel = new JLabel("Speed:");
		speedField = new JTextField();
		infiniteSpeed = new JCheckBox("Instant movement?");
		speedField.setActionCommand("speed");
		speedField.addActionListener(this);
		infiniteSpeed.setActionCommand("inf_speed");
		infiniteSpeed.addActionListener(this);
		typeChooser = new JComboBox(new Object[] { "Absolute", "Relative",
				"Path", "Cover" });
		typeChooser.setActionCommand("type");
		typeChooser.addActionListener(this);
		xLabel = new JLabel("x = ");
		yLabel = new JLabel("y = ");
		xField = new JTextField();
		xField.setActionCommand("x");
		xField.addActionListener(this);
		yField = new JTextField();
		yField.setActionCommand("y");
		yField.addActionListener(this);
		pathLabel = new JLabel("Path:");
		pathTable = new PathTable();
		pathTable.setActionCommand("path");
		pathTable.addActionListener(this);
		coverLabel = new JLabel("Shape to cover:");
		coverShapeList = new JComboBox();
		coverShapeList.setActionCommand("cover");
		coverShapeList.addActionListener(this);

		drawPoint = new Cross();
		drawPoint.setSize(10);
		drawPoint.setColor(Color.BLACK);
		drawPoint.addListener(this);
		drawPath = new DrawablePath();
		drawPath.setColor(Color.BLACK);
		drawPath.addListener(this);
		drawArrow = new Line();
		drawArrow.setPosition(10, 10);
		drawArrow.setArrow(true);
		drawArrow.setColor(Color.BLACK);
		drawArrow.addListener(this);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(speedLabel, c);
		add(speedField, c);
		add(infiniteSpeed, c);
		add(typeChooser, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		add(colorLabelLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(colorLabel, c);

		c.gridwidth = 1;
		c.weightx = 0;
		add(xLabel, c);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(xField, c);
		c.weightx = 0;
		c.gridwidth = 1;
		add(yLabel, c);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(yField, c);
		add(coverLabel, c);
		add(coverShapeList, c);
		add(pathLabel, c);
		c.weighty = 1;
		add(pathTable, c);
		add(padLabel, c);
	}

	private void updated() {
		switch (behaviour.getType()) {
		case MoveBehaviour.MOVE_TYPE_ABSOLUTE:
			transferAbsolute();
			break;
		case MoveBehaviour.MOVE_TYPE_RELATIVE:
			transferRelative();
			break;
		case MoveBehaviour.MOVE_TYPE_PATH:
			transferPath();
			break;
		case MoveBehaviour.MOVE_TYPE_COVER:
			transferCover();
			break;
		}
		if (parent != null)
			parent.representationChanged(new Object[] { behaviour });
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("type")) {
			behaviour.setType(typeChooser.getSelectedIndex());
			updated();
		} else if (cmd.equals("speed")) {
			try {
				behaviour.setSpeed(new Expression(speedField.getText()));
				updated();
			} catch (ExpressionParseException e1) {
				speedField.setText(behaviour.getSpeed().toString());
			}
		} else if (cmd.equals("inf_speed")) {
			if (infiniteSpeed.isSelected()) {
				behaviour.setSpeed(null);
				speedField.setText("");
				speedField.setEnabled(false);
			} else {
				if (behaviour.getSpeed() == null)
					behaviour.setSpeed(new Expression(10000));
				speedField.setText(behaviour.getSpeed().toString());
				speedField.setEnabled(true);
			}
		} else if (cmd.equals("color")) {
			Color c = behaviour.getColoring();
			Color newColor = JColorChooser.showDialog(this, "Choose coloring",
					c);
			if (newColor != null) {
				if (newColor.equals(Color.WHITE))
					newColor = null;
				behaviour.setColoring(newColor);
				setColorButton();
				if (parent != null)
					parent.representationChanged(new Object[] { behaviour });
			}
		} else if (cmd.equals("x")) {
			try {
				behaviour.setX(new Expression(xField.getText()));
				updated();
			} catch (ExpressionParseException exn) {
				JOptionPane.showMessageDialog(this, exn.getMessage(),
						"Parse error", JOptionPane.NO_OPTION);
				xField.setText(behaviour.getX().toString());
			}
		} else if (cmd.equals("y")) {
			try {
				behaviour.setY(new Expression(yField.getText()));
				updated();
			} catch (ExpressionParseException exn) {
				JOptionPane.showMessageDialog(this, exn.getMessage(),
						"Parse error", JOptionPane.NO_OPTION);
				yField.setText(behaviour.getY().toString());
			}
		} else if (cmd.equals("path")) {
			if (worldView != null)
				worldView.repaint();
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		} else if (cmd.equals("cover")) {
			if (worldView != null) {
				worldView.remove(this.behaviour.getToCover());
			}
			this.behaviour.setToCover(((DrawableShape) coverShapeList
					.getSelectedItem()).copyShape());
			transferCover();
			if (worldView != null)
				worldView.repaint();
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		}
	}

	@Override
	public void changed(DrawableShape s) {
		resized(s);
	}

	@Override
	public void moved(DrawableShape s) {
		if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_ABSOLUTE) {
			double[] p = s.getPosition();
			if (p != null) {
				if (behaviour.getX().getNumber().doubleValue() != p[2]
						&& behaviour.getY().getNumber().doubleValue() != p[3]) {
					behaviour.setX(new Expression(p[0]));
					behaviour.setY(new Expression(p[1]));
					xField.setText(behaviour.getX().toString());
					yField.setText(behaviour.getY().toString());
					if (parent != null)
						parent.representationChanged(new Object[] { behaviour });
				}
			}
		} else if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_PATH) {
			pathTable.refresh();
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		}
	}

	@Override
	public void resized(DrawableShape s) {
		if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_RELATIVE) {
			double[] p = s.getBounds();
			if (p != null) {
				if (behaviour.getX().getNumber().doubleValue() != p[2]
						&& behaviour.getY().getNumber().doubleValue() != p[3]) {
					behaviour.setX(new Expression(p[2]));
					behaviour.setY(new Expression(p[3]));
					xField.setText(behaviour.getX().toString());
					yField.setText(behaviour.getY().toString());
					if (parent != null)
						parent.representationChanged(new Object[] { behaviour });
				}
			}
		} else if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_ABSOLUTE) {
			drawPoint.setSize(10);
		} else if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_PATH) {
			pathTable.refresh();
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		} else {
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		}
	}

	public void click(DrawableShape s) {
	}

	public void mouseMoved(double x, double y) {
	}

	public void mouseOut() {
	}

	public boolean remove(DrawableShape s) {
		if (s == behaviour.getToCover() || s == drawArrow || s == drawPath
				|| s == drawPoint)
			return false;
		return true;
	}

	public void select(DrawableShape s) {
		if (behaviour.getType() == MoveBehaviour.MOVE_TYPE_COVER && !s.isLine()
				&& !(s instanceof DrawablePath)) {
			if (worldView != null) {
				worldView.remove(this.behaviour.getToCover());
			}
			this.behaviour.setToCover(s.copyShape());
			transferCover();
			if (worldView != null)
				worldView.repaint();
			if (parent != null)
				parent.representationChanged(new Object[] { behaviour });
		}
	}
}
