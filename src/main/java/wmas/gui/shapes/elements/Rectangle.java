package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;
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

public class Rectangle extends AbstractDrawableShape {
	private static Rectangle2D rectangle = new Rectangle2D.Double();

	double width = 10;
	double height = 10;
	double x = 0;
	double y = 0;

	boolean editable = true;
	boolean selectable = true;

	Color fillColour = null;
	Color colour = Color.BLACK;

	public double[] getBounds() {
		double[] res = { x, y, width, height };
		return res;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		double xmult = vx * height / 2;
		double ymult = vy * width / 2;
		double[] p = { x + width / 2, y + height / 2 };
		if (Math.abs(xmult) < Math.abs(ymult)) {
			p[0] += xmult / Math.abs(vy);
			p[1] += (vy < 0 ? -height : height) / 2;
		} else {
			p[1] += ymult / Math.abs(vx);
			p[0] += (vx < 0 ? -width : width) / 2;
		}
		return p;
	}

	public double[] getPosition() {
		double[] res = { x + width / 2, y + height / 2 };
		return res;
	}

	public boolean hasBothResizeableDirections() {
		return editable;
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
		rectangle.setFrame(x, y, width, height);
		if (colour != null || colorized != null) {
			g.setColor(colorized != null ? colorized : colour);
			g.draw(rectangle);
		}
		if (fillColour != null) {
			g.setColor(fillColour);
			g.fill(rectangle);
		}
	}

	public void setPosition(double x, double y) {
		if (this.x != x - width / 2 || this.y != y - height / 2) {
			this.x = x - width / 2;
			this.y = y - height / 2;
			this.moved();
		}
	}

	public void setSize(double size) {
		if (size != width || size != height) {
			x += (size - width) / 2;
			y += (size - height) / 2;
			width = size;
			height = size;
			this.resized();
		}
	}

	public void setSize(double width, double height) {
		if (width != this.width || height != this.height) {
			x += (-width + this.width) / 2;
			y += (-height + this.height) / 2;
			this.width = width;
			this.height = height;
			this.resized();
		}
	}

	public Color getFillColor() {
		return fillColour;
	}

	public void setFillColor(Color fillColour) {
		this.fillColour = fillColour;
		this.changed();
	}

	public Color getColor() {
		return colour;
	}

	public void setColor(Color colour) {
		this.colour = colour;
		this.changed();
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
		return new PolyDefault(new Rectangle2D.Double(x, y, width, height));
	}

	public DrawableShape copyShape() {
		Rectangle r = new Rectangle();
		r.width = width;
		r.height = height;
		r.x = x;
		r.y = y;
		r.editable = editable;
		r.selectable = selectable;
		r.fillColour = fillColour;
		r.colour = colour;
		return r;
	}

	public String toString() {
		return "Rectangle(" + x + ", " + y + ", " + width + ", " + height + ")";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		colour = Util.colorFromString(e.getAttribute("line"));
		fillColour = Util.colorFromString(e.getAttribute("fill"));
		x = Double.parseDouble(e.getAttribute("x"));
		y = Double.parseDouble(e.getAttribute("y"));
		width = Double.parseDouble(e.getAttribute("width"));
		height = Double.parseDouble(e.getAttribute("height"));
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		e.setAttribute("line", Util.colorToString(colour));
		e.setAttribute("fill", Util.colorToString(fillColour));
		e.setAttribute("x", Double.toString(x));
		e.setAttribute("y", Double.toString(y));
		e.setAttribute("width", Double.toString(width));
		e.setAttribute("height", Double.toString(height));
		return e;
	}

	@Override
	public boolean hasFillColor() {
		return true;
	}
}
