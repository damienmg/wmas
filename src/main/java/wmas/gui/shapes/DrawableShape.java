package wmas.gui.shapes;

import java.awt.Color;
import java.awt.Graphics2D;

import wmas.geometry.Poly;
import wmas.xml.XMLEntity;

public interface DrawableShape extends XMLEntity {

	// Paint the shape on a graphics2D
	public void paint(Graphics2D g);

	// Returns the bounds of the shape
	public double[] getBounds();

	// Returns true if the shape is reduced to a line (in that case getBounds
	// returns the exact shape)
	public boolean isLine();

	// Returns the color of the shape
	public Color getColor();

	// Set the color of the shape
	public void setColor(Color c);

	// Returns wether you can change the filling color
	public boolean hasFillColor();

	// Returns the filling color of the shape
	public Color getFillColor();

	// Set the color of the shape
	public void setFillColor(Color c);

	// Returns the center position of the shape
	public double[] getPosition();

	// Returns the connection point with the given vector
	public double[] getConnectionPoint(double vx, double vy);

	// Returns whether the shape is selectable or not
	public boolean isSelectable();

	// Returns wether the shape is moveable or not
	public boolean isMoveable();

	// Returns wether the shape is resizeable or not
	public boolean isResizeable();

	// Returns wether the shape is resizeable in both direction or not
	public boolean hasBothResizeableDirections();

	// Modifications
	public void setPosition(double x, double y); // Center position

	public void setSize(double size);

	public void setSize(double width, double height);

	// Determine the surface filled by this shape
	public Poly getPoly();

	public DrawableShape copyShape();

	// Listener
	public void addListener(ShapeListener l);

	public void removeListener(ShapeListener l);

	public void resized();

	public void moved();

	public void changed();

	// Colorize
	public void colorize(Color c);
}
