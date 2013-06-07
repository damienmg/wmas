package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Poly;
import wmas.gui.shapes.AbstractDrawableShape;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.ShapeDrawer;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Line extends AbstractDrawableShape {
	private static Line2D line = new Line2D.Double();

	double x1 = 0, x2 = 10, y1 = 0, y2 = 0;
	boolean arrow = true;
	boolean editable = true;
	Color colour = Color.BLACK;

	public boolean hasArrow() {
		return arrow;
	}

	public double[] getBounds() {
		double[] points = { x1, y1, x2 - x1, y2 - y1 };
		return points;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		double[] r = { (x1 + x2) / 2, (y1 + y2) / 2 };
		return r;
	}

	public double[] getPosition() {
		double[] r = { (x1 + x2) / 2, (y1 + y2) / 2 };
		return r;
	}

	public boolean hasBothResizeableDirections() {
		return false;
	}

	public boolean isLine() {
		return true;
	}

	public boolean isMoveable() {
		return editable;
	}

	public boolean isResizeable() {
		return editable;
	}

	public boolean isSelectable() {
		return editable;
	}

	public void paint(Graphics2D g) {
		if (colour != null || colorized != null) {
			g.setColor(colorized != null ? colorized : colour);
			line.setLine(x1, y1, x2, y2);
			g.draw(line);
			if (arrow)
				ShapeDrawer.drawArrow(x1, y1, x2, y2, g);
		}
	}

	public void setPosition(double x, double y) {
		double dx = x - (x1 + x2) / 2;
		double dy = y - (y1 + y2) / 2;
		if (dx != 0 || dy != 0) {
			x1 += dx;
			x2 += dx;
			y1 += dy;
			y2 += dy;
			this.moved();
		}
	}

	public void setSize(double size) {
		if (size != 0) {
			x2 = x1 + size;
			y2 = y1 + size;
			this.resized();
		}
	}

	public void setSize(double width, double height) {
		if (width != 0 || height != 0) {
			x2 = x1 + width;
			y2 = y1 + height;
			this.resized();
		}
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
		this.changed();
	}

	public void setArrow(boolean arrow) {
		this.arrow = arrow;
		this.changed();
	}

	public double getX1() {
		return x1;
	}

	public void setX1(double x1) {
		if (x1 != this.x1) {
			this.x1 = x1;
			this.moved();
			this.resized();
		}
	}

	public double getX2() {
		return x2;
	}

	public void setX2(double x2) {
		if (x2 != this.x2) {
			this.x2 = x2;
			this.moved();
			this.resized();
		}
	}

	public double getY1() {
		return y1;
	}

	public void setY1(double y1) {
		if (y1 != this.y1) {
			this.y1 = y1;
			this.moved();
			this.resized();
		}
	}

	public double getY2() {
		return y2;
	}

	public void setY2(double y2) {
		if (y2 != this.y2) {
			this.y2 = y2;
			this.moved();
			this.resized();
		}
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public Poly getPoly() {
		return null;
	}

	public Color getColor() {
		return this.colour;
	}

	public void setColor(Color c) {
		this.colour = c;
		this.changed();
	}

	public DrawableShape copyShape() {
		Line r = new Line();
		r.x1 = x1;
		r.x2 = x2;
		r.y1 = y1;
		r.y2 = y2;
		r.editable = editable;
		r.arrow = arrow;
		r.colour = colour;
		return r;
	}

	public String toString() {
		return "Line(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		colour = Util.colorFromString(e.getAttribute("color"));
		x1 = Double.parseDouble(e.getAttribute("x1"));
		y1 = Double.parseDouble(e.getAttribute("y1"));
		x2 = Double.parseDouble(e.getAttribute("x2"));
		y2 = Double.parseDouble(e.getAttribute("y2"));
		arrow = e.hasAttribute("arrow") && e.getAttribute("arrow").equals("1");
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		e.setAttribute("color", Util.colorToString(colour));
		e.setAttribute("x1", Double.toString(x1));
		e.setAttribute("y1", Double.toString(y1));
		e.setAttribute("x2", Double.toString(x2));
		e.setAttribute("y2", Double.toString(y2));
		if (arrow)
			e.setAttribute("arrow", "1");
		return e;
	}

	@Override
	public Color getFillColor() {
		return null;
	}

	@Override
	public boolean hasFillColor() {
		return false;
	}

	@Override
	public void setFillColor(Color c) {
	}
}
