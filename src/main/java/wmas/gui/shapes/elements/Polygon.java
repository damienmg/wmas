package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Area;
import wmas.geometry.Poly;
import wmas.geometry.PolyDefault;
import wmas.gui.shapes.AbstractDrawableShape;
import wmas.gui.shapes.DrawableShape;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Polygon extends AbstractDrawableShape {
	private Color colour = Color.BLACK;
	private Color fillColour = null;
	private Poly poly = null;

	public Polygon() {
	}

	public Polygon(Poly p) {
		poly = p;
	}

	public Poly getPolygon() {
		return poly;
	}

	public void setPolygon(Poly p) {
		poly = p;
		this.changed();
	};

	public double[] getBounds() {
		Area a = poly.getBounds();
		double[] res = { a.getX(), a.getY(), a.getHeight(), a.getWidth() };
		return res;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		return getPosition();
	}

	public double[] getPosition() {
		double[] r = getBounds();
		double[] res = { r[0] + r[2] / 2, r[1] + r[3] / 2 };
		return res;
	}

	public boolean hasBothResizeableDirections() {
		return false;
	}

	public boolean isLine() {
		return false;
	}

	public boolean isMoveable() {
		return false;
	}

	public boolean isResizeable() {
		return false;
	}

	public boolean isSelectable() {
		return false;
	}

	public void paint(Graphics2D g) {
		if (getColor() != null || colorized != null) {
			g.setColor(colorized != null ? colorized : getColor());
			getPolygon().draw(g);
		}
		if (getFillColor() != null) {
			g.setColor(getFillColor());
			getPolygon().fill(g);
		}
	}

	public void setPosition(double x, double y) {
	}

	public void setSize(double size) {
	}

	public void setSize(double width, double height) {
	}

	public Color getColor() {
		return colour;
	}

	public void setColor(Color colour) {
		this.colour = colour;
		this.changed();
	}

	public Color getFillColor() {
		return fillColour;
	}

	public void setFillColor(Color fillColour) {
		this.fillColour = fillColour;
		this.changed();
	}

	public Poly getPoly() {
		return poly;
	}

	public DrawableShape copyShape() {
		Polygon p = new Polygon(poly);
		p.colour = colour;
		p.fillColour = fillColour;
		return p;
	}

	public String toString() {
		return "Polygon";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);

		colour = Util.colorFromString(e.getAttribute("line"));
		fillColour = Util.colorFromString(e.getAttribute("fill"));
		PolyDefault p = new PolyDefault();
		p.fromXML(e, refs);
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);

		e.setAttribute("line", Util.colorToString(colour));
		e.setAttribute("fill", Util.colorToString(fillColour));
		if (poly != null)
			((PolyDefault) poly).toXML(e, root);
		return e;
	}

	public void setPoly(Poly poly) {
		this.poly = poly;
	}

	@Override
	public boolean hasFillColor() {
		return true;
	}
}
