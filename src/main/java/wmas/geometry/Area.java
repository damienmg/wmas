package wmas.geometry;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Area implements XMLEntity {
	protected double x, y;
	protected double width, height;
	protected Color color;
	protected String name;
	protected double savedX, savedY;

	public Area(double x, double y, double width, double height) {
		super();
		name = "Area";
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		color = Color.BLACK;
	}

	public Area(Rectangle2D a) {
		super();
		name = "Area";
		this.x = a.getX();
		this.y = a.getY();
		this.width = a.getWidth();
		this.height = a.getHeight();
		color = Color.BLACK;
	}

	public Area(Area a) {
		super();
		name = a.name;
		this.x = a.x;
		this.y = a.y;
		this.width = a.width;
		this.height = a.height;
		color = a.color;
	}

	public Area() {
		super();
		x = y = width = height = 0;
		color = Color.BLACK;
	}

	public void setDimensions(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setDimensions(Area area) {
		this.x = area.x;
		this.y = area.y;
		this.width = area.width;
		this.height = area.height;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean contain(double x, double y) {
		return ((this.x <= x) && (this.y <= y) && (this.width >= x - this.x) && (this.height >= y
				- this.y));
	}

	public boolean contain(Point p) {
		return contain(p.x, p.y);
	}

	public void setPosition(Point p) {
		setX(p.x);
		setY(p.y);
	}

	public Point getPosition() {
		return new Point(x, y);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void extend(double x, double y) {
		if (this.x > x)
			this.x = x;
		if (this.y > y)
			this.y = y;
		if (this.x + width < x)
			width = x - this.x;
		if (this.y + height < y)
			height = y - this.y;
	}

	public void extend(Point p) {
		extend(p.x, p.y);
	}

	public void extend(double x, double y, double width, double height) {
		extend(x, y);
		extend(x + width, y + height);
	}

	public void extend(Area a) {
		if (a != null) {
			extend(a.x, a.y);
			extend(a.x + a.width, a.y + a.height);
		}
	}

	public void extend(double s) {
		if (s > 0) {
			extend(x - s, y - s);
			extend(x + width + s, y + height + s);
		}
	}

	public void save() {
		savedX = x;
		savedY = y;
	}

	public void reset() {
		x = savedX;
		y = savedY;
	}

	public Poly toPoly() {
		return new PolyDefault(x, y, width, height);
	}

	public Rectangle2D toRectangle() {
		return new Rectangle2D.Double(x, y, width, height);
	}

	@Override
	public Area copy() {
		return new Area(this);
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		name = e.getAttribute("name");
		width = Double.valueOf(e.getAttribute("width"));
		height = Double.valueOf(e.getAttribute("height"));
		color = Color.decode(e.getAttribute("color"));
		x = Double.valueOf(e.getAttribute("x"));
		y = Double.valueOf(e.getAttribute("y"));
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("name", name);
		e.setAttribute("width", Double.toString(width));
		e.setAttribute("height", Double.toString(height));
		e.setAttribute("color", Util.colorToString(color));
		e.setAttribute("x", Double.toString(x));
		e.setAttribute("y", Double.toString(y));
		return e;
	}
}
