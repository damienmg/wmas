package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Poly;
import wmas.geometry.PolyDefault;
import wmas.gui.shapes.AbstractDrawableShape;
import wmas.gui.shapes.DrawableShape;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class CrossedSquare extends AbstractDrawableShape {
	private static Rectangle2D rectangle = new Rectangle2D.Double();
	private static Line2D line = new Line2D.Double();

	private double x = 0;
	private double y = 0;
	private Color color = Color.BLACK;
	private double size = 10;
	private boolean editable = true;
	private boolean selectable = true;

	public double[] getBounds() {
		double[] res = { x - size / 2, y - size / 2, size, size };
		return res;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		double[] res = { x, y };
		double s = size / 2;
		if (Math.abs(vx) < Math.abs(vy)) {
			res[0] += vx * s / Math.abs(vy);
			res[1] += vy < 0 ? -s : s;
		} else {
			res[1] += vy * s / Math.abs(vx);
			res[0] += vx < 0 ? -s : s;
		}
		return res;
	}

	public double[] getPosition() {
		double[] res = { x, y };
		return res;
	}

	public boolean hasBothResizeableDirections() {
		return false;
	}

	public boolean isLine() {
		return false;
	}

	public boolean isMoveable() {
		return editable;
	}

	public boolean isResizeable() {
		return editable;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void paint(Graphics2D g) {
		if (color != null) {
			g.setColor(color);
			double diameter = size;
			double delta = diameter / 2;
			rectangle.setRect((x - delta), (y - delta), diameter, diameter);
			g.draw(rectangle);
			if (colorized != null)
				g.setColor(colorized);
			line.setLine((x - delta), (y - delta), (x + delta), (y + delta));
			g.draw(line);
			line.setLine((x - delta), (y + delta), (x + delta), (y - delta));
			g.draw(line);
		}
	}

	public void setPosition(double x, double y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			this.moved();
		}
	}

	public void setSize(double width, double height) {
		if (width != this.size) {
			this.size = width;
			this.resized();
		}
	}

	public void setSize(double size) {
		if (size != this.size) {
			this.size = size;
			this.resized();
		}
	}

	public void setColor(Color color) {
		this.color = color;
		this.changed();
	}

	public Color getColor() {
		return color;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public Poly getPoly() {
		return new PolyDefault(new Rectangle2D.Double(x - size / 2, y - size
				/ 2, size, size));
	}

	public DrawableShape copyShape() {
		CrossedSquare r = new CrossedSquare();
		r.selectable = selectable;
		r.editable = editable;
		r.size = size;
		r.x = x;
		r.y = y;
		r.color = color;
		return r;
	}

	public String toString() {
		return "CrossedSquare(" + x + ", " + y + ", " + size + ")";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		color = Util.colorFromString(e.getAttribute("color"));
		size = Double.parseDouble(e.getAttribute("size"));
		x = Double.parseDouble(e.getAttribute("x"));
		y = Double.parseDouble(e.getAttribute("y"));
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		e.setAttribute("color", Util.colorToString(color));
		e.setAttribute("size", Double.toString(size));
		e.setAttribute("x", Double.toString(x));
		e.setAttribute("y", Double.toString(y));
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
