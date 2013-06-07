package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
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

public class Circle extends AbstractDrawableShape {
	private static Ellipse2D oval = new Ellipse2D.Double();

	private boolean selectable = true;
	private boolean editable = true;
	private double size = 10;
	private double x = 0, y = 0;
	private Color lineColour = Color.BLACK;
	private Color fillColour = null;

	public double[] getBounds() {
		double[] res = { x - size / 2, y - size / 2, size, size };
		return res;
	}

	public boolean hasBothResizeableDirections() {
		return false;
	}

	public boolean isResizeable() {
		return editable;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void paint(Graphics2D g) {
		oval.setFrame((x - size / 2.0), (y - size / 2.0), size, size);
		if (getColor() != null) {
			g.setColor(getColor());
			g.draw(oval);
		}
		if (getFillColor() != null) {
			g.setColor(getFillColor());
			g.fill(oval);
		}
		if (colorized != null) {
			g.setColor(colorized);
			oval.setFrame((x - size / 4.0), (y - size / 4.0), size / 2.0,
					size / 2.0);
			g.fill(oval);
		}
	}

	public void setPosition(double x, double y) {
		if (x != this.x || y != this.y) {
			this.x = x;
			this.y = y;
			this.moved();
		}
	}

	public void setSize(double size) {
		if (size != this.size) {
			this.size = size;
			this.resized();
		}
	}

	public void setSize(double width, double height) {
		this.setSize(width);
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

	public Color getColor() {
		return lineColour;
	}

	public void setColor(Color lineColor) {
		this.lineColour = lineColor;
		this.changed();
	}

	public Color getFillColor() {
		return fillColour;
	}

	public void setFillColor(Color fillColor) {
		this.fillColour = fillColor;
		this.changed();
	}

	public boolean isMoveable() {
		return editable;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		double[] res = { x, y };
		double d = Math.sqrt(vx * vx + vy * vy);
		d = size / (2 * d);
		res[0] += vx * d;
		res[1] += vy * d;
		return res;
	}

	public double[] getPosition() {
		double[] res = { x, y };
		return res;
	}

	public boolean isLine() {
		return false;
	}

	public Poly getPoly() {
		return new PolyDefault(new Rectangle2D.Double(x - size / 2, y - size
				/ 2, size, size));
	}

	public DrawableShape copyShape() {
		Circle r = new Circle();
		r.size = size;
		r.x = x;
		r.y = y;
		r.editable = editable;
		r.selectable = selectable;
		r.fillColour = fillColour;
		r.lineColour = lineColour;
		return r;
	}

	public String toString() {
		return "Circle(" + x + ", " + y + ", " + size + ")";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		fillColour = Util.colorFromString(e.getAttribute("fill"));
		lineColour = Util.colorFromString(e.getAttribute("line"));
		size = Double.parseDouble(e.getAttribute("size"));
		x = Double.parseDouble(e.getAttribute("x"));
		y = Double.parseDouble(e.getAttribute("y"));
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		e.setAttribute("fill", Util.colorToString(fillColour));
		e.setAttribute("line", Util.colorToString(lineColour));
		e.setAttribute("size", Double.toString(size));
		e.setAttribute("x", Double.toString(x));
		e.setAttribute("y", Double.toString(y));
		return e;
	}

	@Override
	public boolean hasFillColor() {
		return true;
	}
}
