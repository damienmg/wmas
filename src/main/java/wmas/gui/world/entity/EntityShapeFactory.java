package wmas.gui.world.entity;

import javax.swing.Icon;

import wmas.gui.shapes.DrawableShape;

public interface EntityShapeFactory {
	int getNbShape();

	DrawableShape getShape(int shapeNb);

	int getShapeIndex(DrawableShape s);

	String getShapeName(int shapeNb);

	Icon getShapeIcon(int shapeNb);
}
