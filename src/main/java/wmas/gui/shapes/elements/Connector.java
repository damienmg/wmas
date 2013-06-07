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

public class Connector extends AbstractDrawableShape {
	private static Line2D line = new Line2D.Double();

	private DrawableShape source;
	private DrawableShape destination;
	private boolean selectable = true;
	private boolean arrow = true;
	private String text = "";
	private Color color = Color.BLACK;

	public Connector() {
		this.source = null;
		this.destination = null;
	}

	public Connector(DrawableShape source, DrawableShape dest) {
		this.source = source;
		this.destination = dest;
	}

	public boolean hasArrow() {
		return arrow;
	}

	protected double[] getPoints() {
		if (source == null || destination == null) {
			return new double[] { -10, -10, -11, -11 };
		}
		double[] res = { 0, 0, 0, 0 };
		double[] orig = source.getPosition();
		double[] dest = destination.getPosition();
		double vx = orig[0] - dest[0];
		double vy = orig[1] - dest[1];
		orig = source.getConnectionPoint(-vx, -vy);
		dest = destination.getConnectionPoint(vx, vy);
		res[0] = orig[0];
		res[1] = orig[1];
		res[2] = dest[0];
		res[3] = dest[1];
		return res;
	}

	public double[] getBounds() {
		if (source == null || destination == null) {
			return new double[] { -11, -11, 1, 1 };
		}
		if (source == destination) {
			double p1[] = source.getConnectionPoint(-1, -0.001);
			double p2[] = source.getConnectionPoint(-1, +0.001);
			double[] points = new double[4];
			points[0] = p1[0] - 10;
			points[1] = p1[1];
			points[2] = 10;
			points[3] = Math.abs(p2[1] - p1[1]);
			return points;
		} else {
			double[] points = getPoints();
			points[2] -= points[0];
			points[3] -= points[1];
			return points;
		}
	}

	public double[] getConnectionPoint(double vx, double vy) {
		return getPosition();
	}

	public double[] getPosition() {
		double[] points = getPoints();
		double[] res = { (points[0] + points[2]) / 2,
				(points[1] + points[3]) / 2 };
		return res;
	}

	public boolean hasBothResizeableDirections() {
		return false;
	}

	public boolean isMoveable() {
		return false;
	}

	public boolean isResizeable() {
		return false;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void paint(Graphics2D g) {
		if (source == null || destination == null)
			return;
		if (color != null || colorized != null) {
			if (source == destination) {
				double p1[] = source.getConnectionPoint(-1, -0.001);
				double p2[] = source.getConnectionPoint(-1, +0.001);
				g.setColor(colorized != null ? colorized : color);
				line.setLine(p1[0], p1[1], p1[0] - 10, p1[1]);
				g.draw(line);
				line.setLine(p1[0] - 10, p1[1], p2[0] - 10, p2[1]);
				g.draw(line);
				line.setLine(p2[0], p2[1], p2[0] - 10, p2[1]);
				g.draw(line);
				if (arrow) {
					ShapeDrawer.drawArrow(p2[0] - 10, p2[1], p2[0], p2[1], g);
				}
				if (getText() != "") {
					String s = getText();
					int sw = ShapeDrawer.getStringWidth(g, s);
					int hw = ShapeDrawer.getStringHeight(g, s);
					ShapeDrawer.drawText(g, s, (int) (p1[0] - 10 - sw),
							(int) (p1[1] + p2[1] - hw) / 2);
				}
			} else {
				double[] points = getPoints();
				g.setColor(colorized != null ? colorized : color);
				line.setLine(points[0], points[1], points[2], points[3]);
				g.draw(line);
				if (arrow) {
					ShapeDrawer.drawArrow(points[0], points[1], points[2],
							points[3], g);
				}
				if (getText() != "") {
					String s = getText();
					ShapeDrawer.drawText(g, s,
							(int) (points[0] + points[2] - ShapeDrawer
									.getStringWidth(g, s)) / 2,
							(int) (points[1] + points[3] - ShapeDrawer
									.getStringHeight(g, s)) / 2);
				}
			}
		}
	}

	public void setPosition(double x, double y) {
	}

	public void setSize(double size) {
	}

	public void setSize(double width, double height) {
	}

	public boolean isLine() {
		return source != destination;
	}

	public DrawableShape getSource() {
		return source;
	}

	public void setSource(DrawableShape source) {
		this.source = source;
		this.changed();
	}

	public DrawableShape getDestination() {
		return destination;
	}

	public void setDestination(DrawableShape destination) {
		this.destination = destination;
		this.changed();
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public void setArrow(boolean arrow) {
		this.arrow = arrow;
		this.changed();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		this.changed();
	}

	public Poly getPoly() {
		return null;
	}

	public DrawableShape copyShape() {
		Connector c = new Connector(source, destination);
		c.arrow = arrow;
		c.color = color;
		c.text = text;
		c.selectable = selectable;
		return c;
	}

	public String toString() {
		return "Connector(" + (source == null ? "?" : source.toString()) + ", "
				+ (destination == null ? "?" : destination.toString()) + ")";
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		color = Util.colorFromString(e.getAttribute("color"));
		text = e.getAttribute("text");
		arrow = e.hasAttribute("arrow") && e.getAttribute("arrow").equals("1");
		Object src = refs.getObject(e.getAttribute("src"));
		Object dst = refs.getObject(e.getAttribute("dest"));
		source = (src != null && src instanceof DrawableShape) ? (DrawableShape) src
				: null;
		destination = (dst != null && dst instanceof DrawableShape) ? (DrawableShape) dst
				: null;
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		e.setAttribute("color", Util.colorToString(color));
		if (arrow)
			e.setAttribute("arrow", "1");
		e.setAttribute("text", text);
		if (source != null)
			e.setAttribute("src", refs.getRef(source));
		if (destination != null)
			e.setAttribute("dst", refs.getRef(destination));
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
