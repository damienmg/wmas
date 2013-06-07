package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Poly;
import wmas.geometry.PolyDefault;
import wmas.gui.shapes.AbstractDrawableShape;
import wmas.gui.shapes.DrawableShape;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Diamond extends AbstractDrawableShape {
	private boolean selectable = true;
	private boolean editable = true;
	private double size = 10;
	private double x = 0, y = 0;
	private Color lineColour = Color.BLACK;
	private Color fillColour = null;
	private static java.awt.Polygon poly = new java.awt.Polygon();

	public double[] getBounds() {
		double[] res = { x - size / 2, y - size / 2, size, size };
		return res;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		double[] r = { x, y };
		double dx = size / 2;
		double dy = size / 2;
		if (vy == 0) {
			r[0] += vx < 0 ? -dx : dx;
		} else if (vx == 0) {
			r[1] += vy < 0 ? -dy : dy;
		} else {
			// d: y = dy - x * dy/dx
			// v: y = x * vy/vx
			// d/v: dy - x * dy/dx = x * vy/vx => (vy/vx + dy/dx) x = dy
			double a = dy / dx;
			double b = vy / vx;
			double alpha = a + b;
			double beta = a - b;
			// 1+2 = |x| = |dy/alpha|, 3+4 = |x| = |dy/beta|
			if (alpha == 0) {
				double x34 = Math.abs(dy / beta);
				double y34 = x34 * vy / vx;
				r[0] += vx < 0 ? -x34 : x34;
				r[1] += vx < 0 ? -y34 : y34;
			} else if (beta == 0) {
				double x12 = Math.abs(dy / alpha);
				double y12 = x12 * vy / vx;
				r[0] += vx < 0 ? -x12 : x12;
				r[1] += vx < 0 ? -y12 : y12;
			} else {
				double x12 = Math.abs(dy / alpha);
				double x34 = Math.abs(dy / beta);
				double y34 = x34 * vy / vx;
				double y12 = x12 * vy / vx;
				if (x12 * x12 + y12 * y12 < x34 * x34 + y34 * y34) {
					r[0] += vx < 0 ? -x12 : x12;
					r[1] += vx < 0 ? -y12 : y12;
				} else {
					r[0] += vx < 0 ? -x34 : x34;
					r[1] += vx < 0 ? -y34 : y34;
				}
			}
		}
		return r;
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

	public boolean isResizeable() {
		return this.editable;
	}

	public boolean isSelectable() {
		return this.selectable;
	}

	public void paint(Graphics2D g) {
		java.awt.Polygon p = poly;
		p.reset();
		double delta = size / 2;
		p.addPoint((int) (x - delta), (int) (y));
		p.addPoint((int) (x), (int) (y - delta));
		p.addPoint((int) (x + delta), (int) (y));
		p.addPoint((int) (x), (int) (y + delta));
		if (lineColour != null) {
			g.setColor(lineColour);
			g.draw(p);
		}
		if (fillColour != null) {
			g.setColor(fillColour);
			g.fill(p);
		}
		if (colorized != null) {
			g.setColor(colorized);
			p.reset();
			delta = size / 4;
			p.addPoint((int) (x - delta), (int) (y));
			p.addPoint((int) (x), (int) (y - delta));
			p.addPoint((int) (x + delta), (int) (y));
			p.addPoint((int) (x), (int) (y + delta));
			g.fill(p);
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

	public void setColor(Color color) {
		this.lineColour = color;
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

	public Poly getPoly() {
		java.awt.Polygon p = poly;
		p.reset();
		double delta = size / 2;
		p.addPoint((int) (x - delta), (int) (y));
		p.addPoint((int) (x), (int) (y - delta));
		p.addPoint((int) (x + delta), (int) (y));
		p.addPoint((int) (x), (int) (y + delta));
		return new PolyDefault(p);
	}

	public DrawableShape copyShape() {
		Diamond r = new Diamond();
		r.selectable = selectable;
		r.editable = editable;
		r.size = size;
		r.x = x;
		r.y = y;
		r.lineColour = lineColour;
		r.fillColour = fillColour;
		return r;
	}

	public String toString() {
		return "Diamond(" + x + ", " + y + ", " + size + ")";
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
