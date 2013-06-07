package wmas.gui.shapes.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Area;
import wmas.geometry.Path;
import wmas.geometry.Point;
import wmas.geometry.Poly;
import wmas.gui.shapes.AbstractDrawableShape;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.ShapeDrawer;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class DrawablePath extends AbstractDrawableShape {
	private static Line2D line = new Line2D.Double();
	private Path path = null;
	private boolean arrow = true;
	private Color colour = Color.BLACK;

	public DrawablePath() {
		path = null;
	}

	public DrawablePath(Path p) {
		path = p;
	}

	public boolean hasArrow() {
		return arrow;
	}

	public Path getPath() {
		return path;
	}

	public double[] getBounds() {
		Area a = path.getBounds();
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
		return false; // TODO
	}

	public boolean isResizeable() {
		return false;
	}

	public boolean isSelectable() {
		return false; // TODO
	}

	public void paint(Graphics2D g) {
		if (colour != null || colorized != null) {
			g.setColor(colorized != null ? colorized : colour);
			Point last = null;
			boolean two = false;
			for (Point pt : path) {
				if (last == null)
					last = pt;
				else {
					two = true;
					Point next = pt;
					line.setLine(last.getX(), last.getY(), next.getX(),
							next.getY());
					g.draw(line);
					last = next;
				}
			}
			if (two && arrow) {
				ShapeDrawer.drawArrow(line, g);
			}
		}
	}

	public void setPosition(double x, double y) {
		// TODO
	}

	public void setSize(double size) {
	}

	public void setSize(double width, double height) {
	}

	public void setPath(Path path) {
		this.path = path;
		this.changed();
	}

	public void setArrow(boolean arrow) {
		this.arrow = arrow;
		this.changed();
	}

	public Poly getPoly() {
		return null;
	}

	public Color getColor() {
		return colour;
	}

	public void setColor(Color c) {
		this.colour = c;
		this.changed();
	}

	public DrawableShape copyShape() {
		DrawablePath r = new DrawablePath();
		r.path = new Path(path);
		r.colour = colour;
		r.arrow = arrow;
		return r;
	}

	public String toString() {
		return path == null ? "Path[]" : "Path" + path.toString();
	}

	public XMLEntity copy() {
		return copyShape();
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		refs.setObject(e, this);
		arrow = e.hasAttribute("arrow") && e.getAttribute("arrow").equals("1");

		colour = Util.colorFromString(e.getAttribute("color"));
		path = new Path();
		path.fromXML(e, refs);
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		refs.makeRef(this, e);
		if (arrow)
			e.setAttribute("arrow", "1");

		e.setAttribute("color", Util.colorToString(colour));
		if (path != null)
			path.toXML(e, root, refs);
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