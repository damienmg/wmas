package wmas.gui.world.simple;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.elements.Circle;
import wmas.gui.shapes.elements.CrossedSquare;
import wmas.gui.shapes.elements.Diamond;
import wmas.gui.shapes.elements.Rectangle;
import wmas.gui.world.entity.EntityShapeFactory;

public class SimpleEntityShapeFactory implements EntityShapeFactory {

	private ImageIcon[] icons;

	public SimpleEntityShapeFactory() {
		icons = new ImageIcon[4];
		icons[0] = new ImageIcon(new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB));
		icons[1] = new ImageIcon(new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB));
		icons[2] = new ImageIcon(new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB));
		icons[3] = new ImageIcon(new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB));

		Graphics2D g1 = (Graphics2D) icons[0].getImage().getGraphics();
		Graphics2D g2 = (Graphics2D) icons[1].getImage().getGraphics();
		Graphics2D g3 = (Graphics2D) icons[2].getImage().getGraphics();
		Graphics2D g4 = (Graphics2D) icons[3].getImage().getGraphics();

		Circle c = new Circle();
		c.setSize(15);
		c.setPosition(7, 7);
		c.paint(g1);

		CrossedSquare s = new CrossedSquare();
		s.setSize(14);
		s.setPosition(7, 7);
		s.paint(g2);

		Diamond d = new Diamond();
		d.setSize(14);
		d.setPosition(7, 7);
		d.paint(g3);

		Rectangle r = new Rectangle();
		r.setSize(14);
		r.setPosition(7, 7);
		r.paint(g4);
	}

	public int getNbShape() {
		return 4;
	}

	public DrawableShape getShape(int shapeNb) {
		switch (shapeNb) {
		case 0:
			return new Circle();
		case 1:
			return new CrossedSquare();
		case 2:
			return new Diamond();
		case 3:
			return new Rectangle();
		}
		return null;
	}

	public Icon getShapeIcon(int shapeNb) {
		return icons[shapeNb];
	}

	public int getShapeIndex(DrawableShape s) {
		if (s instanceof Circle)
			return 0;
		if (s instanceof CrossedSquare)
			return 1;
		if (s instanceof Diamond)
			return 2;
		if (s instanceof Rectangle)
			return 3;
		return -1;
	}

	public String getShapeName(int shapeNb) {
		switch (shapeNb) {
		case 0:
			return "Circle";
		case 1:
			return "Crossed square";
		case 2:
			return "Diamond";
		case 3:
			return "Rectangle";
		}
		return null;
	}

}
