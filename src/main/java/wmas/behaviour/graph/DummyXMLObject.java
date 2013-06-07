package wmas.behaviour.graph;

import java.awt.Color;
import java.awt.Graphics2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.geometry.Poly;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.ShapeListener;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

// Just a dummy object for XML parsing -- Local only
class DummyXMLObject implements DrawableShape {
	String xid = "";
	XMLCrossRef refs = null;

	public DummyXMLObject(String xid, XMLCrossRef ref) {
		super();
		this.xid = xid;
		this.refs = ref;
	}

	public DrawableShape getObject() {
		Object o = refs.getObject(this.xid);
		if (o == null)
			return this;
		if (o instanceof DrawableShape)
			return (DrawableShape) o;
		return this;
	}

	public static DrawableShape getRef(String xref, XMLCrossRef refs) {
		Object o = refs.getObject(xref);
		if (o == null)
			return new DummyXMLObject(xref, refs);
		if (o instanceof DrawableShape)
			return (DrawableShape) o;
		return new DummyXMLObject(xref, refs);
	}

	public double[] getBounds() {
		return null;
	}

	public double[] getConnectionPoint(double vx, double vy) {
		return null;
	}

	public double[] getPosition() {
		return null;
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
	}

	public void setPosition(double x, double y) {
	}

	public void setSize(double size) {
	}

	public void setSize(double width, double height) {
	}

	public Poly getPoly() {
		return null;
	}

	public Color getColor() {
		return null;
	}

	public void setColor(Color c) {
	}

	public DrawableShape copyShape() {
		return new DummyXMLObject(xid, refs);
	}

	public void addListener(ShapeListener l) {
	}

	public void removeListener(ShapeListener l) {
	}

	public void colorize(Color c) {
	}

	public XMLEntity copy() {
		return null;
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		return null;
	}

	public Color getFillColor() {
		return null;
	}

	public boolean hasFillColor() {
		return false;
	}

	public void setFillColor(Color c) {
	}

	public void changed() {
	}

	public void moved() {
	}

	public void resized() {
	}
}
