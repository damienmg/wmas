package wmas.geometry;

import java.awt.geom.Point2D;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Point implements XMLEntity {
	public double x;
	public double y;

	public Point() {
		x = y = 0;
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public Point(Point2D p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	public Point(double[] position) {
		this.x = position[0];
		this.y = position[1];
	}

	public double distance(Point p) {
		double x1 = x - p.x;
		double y1 = y - p.y;
		return Math.sqrt(x1 * x1 + y1 * y1);
	}

	/**
	 * Return the scalar of (p1,p2).(p3,p4)
	 * 
	 * @param p1
	 *            First point of first vector
	 * @param p2
	 *            Second point of first vector
	 * @param p3
	 *            First point of second vector
	 * @param p4
	 *            Second point of second vector
	 * @return The scalar product
	 */
	public static double scalar(Point p1, Point p2, Point p3, Point p4) {
		double dx1 = p2.x - p1.x;
		double dx2 = p4.x - p3.x;
		double dy1 = p2.y - p1.y;
		double dy2 = p4.y - p3.y;
		return dx1 * dx2 + dy1 * dy2;
	}

	/**
	 * Return the scalar of (p1,p2).(p3,p4)
	 * 
	 * @param p1
	 *            First point of first vector
	 * @param p2
	 *            Second point of first vector
	 * @param p3
	 *            First point of second vector
	 * @param p4
	 *            Second point of second vector
	 * @return The scalar product
	 */
	public static double scalar(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
		double dx1 = p2.getX() - p1.getX();
		double dx2 = p4.getX() - p3.getX();
		double dy1 = p2.getY() - p1.getY();
		double dy2 = p4.getY() - p3.getY();
		return dx1 * dx2 + dy1 * dy2;
	}

	public boolean isBetween(Point p1, Point p2) {
		if (Double.isNaN(x) || Double.isNaN(y))
			return false;
		// BUGBUG That's not enough
		if (Double.isInfinite(p1.getX()))
			return p2.getX() == getX();
		if (Double.isInfinite(p1.getY()))
			return p2.getY() == getY();
		if (Double.isInfinite(p2.getX()))
			return p1.getX() == getX();
		if (Double.isInfinite(p1.getY()))
			return p1.getY() == getY();
		// BUGBUG
		double scalar = scalar(p1, this, this, p2);
		double v1 = distance(p1);
		double v2 = distance(p2);
		if (Util.round(scalar) < 0)
			return false;
		return Util.round(scalar - v1 * v2) == 0;
	}

	public boolean isBetween(Point p1, Point p2, double precision) {
		if (Double.isNaN(x) || Double.isNaN(y))
			return false;
		// BUGBUG That's not enough
		if (Double.isInfinite(p1.getX()))
			return p2.getX() == getX();
		if (Double.isInfinite(p1.getY()))
			return p2.getY() == getY();
		if (Double.isInfinite(p2.getX()))
			return p1.getX() == getX();
		if (Double.isInfinite(p1.getY()))
			return p1.getY() == getY();
		// BUGBUG
		double scalar = scalar(p1, this, this, p2);
		double v1 = distance(p1);
		double v2 = distance(p2);
		if (Util.round(Math.abs(scalar) - v1 * v2) == 0) {

			return (scalar > 0) || (Math.abs(Util.round(v1 - v2)) < precision);
		}
		return false;
	}

	static public boolean isBetween(Point2D p1, Point2D p2, Point2D p3) {
		if (Double.isNaN(p3.getX()) || Double.isNaN(p3.getY()))
			return false;
		// BUGBUG That's not enough
		if (Double.isInfinite(p1.getX()))
			return p2.getX() == p3.getX();
		if (Double.isInfinite(p1.getY()))
			return p2.getY() == p3.getY();
		if (Double.isInfinite(p2.getX()))
			return p1.getX() == p3.getX();
		if (Double.isInfinite(p1.getY()))
			return p1.getY() == p3.getY();
		// BUGBUG
		double scalar = scalar(p1, p3, p3, p2);
		double v1 = p3.distance(p1);
		double v2 = p3.distance(p2);
		if (Util.round(scalar) < 0)
			return false;
		return Util.round(scalar - v1 * v2) == 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point)
			return (((Point) obj).x == x) && (((Point) obj).y == y);
		return false;
	}

	public void fromXML(Element e, XMLCrossRef refs) {
		x = Double.valueOf(e.getAttribute("x"));
		y = Double.valueOf(e.getAttribute("y"));
	}

	public Element toXML(Document root, XMLCrossRef refs) {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("x", Double.toString(x));
		e.setAttribute("y", Double.toString(y));
		return e;
	}

	public Point copy() {
		return new Point(x, y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void intersect(Point p11, Point p12, Point p21, Point p22) {
		double dx1 = p12.x - p11.x;
		double dx2 = p22.x - p21.x;
		double dy1 = p12.y - p11.y;
		double dy2 = p22.y - p21.y;
		double d1 = p11.x - p21.x;
		double d2 = p11.y - p21.y;
		if (p12.isBetween(p21, p22)) {
			x = p12.x;
			y = p12.y;
			return;
		}
		if (p11.isBetween(p21, p22)) {
			x = p11.x;
			y = p11.y;
			return;
		}
		if (p22.isBetween(p11, p12)) {
			x = p22.x;
			y = p22.y;
			return;
		}
		if (p21.isBetween(p11, p12)) {
			x = p21.x;
			y = p21.y;
			return;
		}
		/*
		 * Goal: resolve equation system : L1 dx1 * t - dx2 * t' + d1 = 0 L2 dy1
		 * * t - dy2 * t' + d2 = 0 -- L3 dy1 * L1 - dx1 * L2 L4 dy2 * L1 - dx2 *
		 * L2 -- L3 (dy2 * dx1 - dx2 * dy1) * t' = dx1 * d2 - dy1 * d1 L4 (dy2 *
		 * dx1 - dy1 * dx2) * t = dx2 * d2 - dy2 * d1
		 */
		double a = (dy2 * dx1 - dx2 * dy1);
		double b = (dx1 * d2 - dy1 * d1);
		if ((a != 0) && (b != 0)) {
			x = Util.round(b * dx2 / a + p21.x);
			y = Util.round(b * dy2 / a + p21.y);
		} else {
			if (dx1 == 0)
				x = p11.x;
			else if (dx2 == 0)
				x = p21.x;
			else
				x = Double.NaN;
			if (dy1 == 0)
				y = p11.y;
			else if (dy2 == 0)
				y = p21.y;
			else
				y = Double.NaN;
		}
	}

	public void intersect(Point2D p11, Point2D p12, Point2D p21, Point2D p22) {
		double dx1 = p12.getX() - p11.getX();
		double dx2 = p22.getX() - p21.getX();
		double dy1 = p12.getY() - p11.getY();
		double dy2 = p22.getY() - p21.getY();
		double d1 = p11.getX() - p21.getX();
		double d2 = p11.getY() - p21.getY();
		if (isBetween(p21, p22, p12)) {
			x = p12.getX();
			y = p12.getY();
			return;
		}
		if (isBetween(p21, p22, p11)) {
			x = p11.getX();
			y = p11.getY();
			return;
		}
		if (isBetween(p11, p12, p22)) {
			x = p22.getX();
			y = p22.getY();
			return;
		}
		if (isBetween(p11, p12, p21)) {
			x = p21.getX();
			y = p21.getY();
			return;
		}
		/*
		 * (D1) x = dx1 * t + p11.getX() y = dy1 * t + p11.getY() (D2) x = dx2 *
		 * t' + p21.getX() y = dy2 * t' + p21.getY() (D1 n D2) => L1 dx1 * t +
		 * p11.getX() = dx2 * t' + p21.getX() L2 dy1 * t + p11.getY() = dy2 * t'
		 * + p21.getY() Goal: resolve equation system : L1 dx1 * t - dx2 * t' +
		 * d1 = 0 L2 dy1 * t - dy2 * t' + d2 = 0 -- L3 dy1 * L1 - dx1 * L2 L4
		 * dy2 * L1 - dx2 * L2 -- L3 (dy2 * dx1 - dx2 * dy1) * t' = dx1 * d2 -
		 * dy1 * d1 => a * t' = b => t' = b/a L4 (dy2 * dx1 - dy1 * dx2) * t =
		 * dx2 * d2 - dy2 * d1
		 */
		double a = Util.round(dy2 * dx1 - dx2 * dy1);
		double b = Util.round(dx1 * d2 - dy1 * d1);
		if ((a != 0) && (b != 0)) {
			x = Util.round(b * dx2 / a + p21.getX());
			y = Util.round(b * dy2 / a + p21.getY());
		} else {
			if (dx1 == 0)
				x = p11.getX();
			else if (dx2 == 0)
				x = p21.getX();
			else
				x = Double.NaN;
			if (dy1 == 0)
				y = p11.getY();
			else if (dy2 == 0)
				y = p21.getY();
			else
				y = Double.NaN;
		}
	}

	static public Point sIntersect(Point p11, Point p12, Point p21, Point p22) {
		Point r = new Point();
		r.intersect(p11, p12, p21, p22);
		return r;
	}

	static public Point sIntersect(Point2D p11, Point2D p12, Point2D p21,
			Point2D p22) {
		Point r = new Point();
		r.intersect(p11, p12, p21, p22);
		return r;
	}

	/**
	 * Return the nearest point belonging to the given line (in parametric)
	 * <p>
	 * The equation is given by :<br>
	 * <ul>
	 * <li>x = dx * t + x0</li>
	 * <li>y = dy * t + y0</li>
	 * </ul>
	 * 
	 * @param dx
	 *            delta x over delta t
	 * @param dy
	 *            delta y over delta t
	 * @param x0
	 *            initial x
	 * @param y0
	 *            initial y
	 * @param r
	 *            Point to put the result
	 * @return The nearest point belonging to the line
	 */
	public Point nearest(double dx, double dy, double x0, double y0, Point r) {
		/*
		 * resolve x = -dy * t1 + p.x = dx * t2 + x0 y = dx * t1 + p.y = dy * t2
		 * + y0
		 * 
		 * -dy * dy * t2 - dy * y0 + dy * p.y + dx * p.x = dx * dx * t2 + x0 *
		 * dx (dx^2 + dy^2) * t2 = - dy * y0 - x0 * dx + (dy * p.y + dx * p.x)
		 * 
		 * dx * dx * t1 + dx * p.y = - dy * dy * t1 + dy * p.x - dy * x0 + dx *
		 * x0 (dx^2 + dy^2) * t1 = - dy * x0 + dx * y0 - dx * p.y + dy * p.x
		 */
		double d = dx * dx + dy * dy;
		double t1 = (-dy * x0 + dx * y0 - dx * y + dy * x) / d;
		double t2 = (-dy * y0 - x0 * dx + dy * y + dx * x) / d;
		if (t2 != 0) {
			r.x = dx * t2 + x0;
			r.y = dy * t2 + y0;
		} else {
			r.x = -dy * t1 + x;
			r.y = dx * t1 + y;
		}
		return r;
	}

	/**
	 * Return the nearest point belonging to the given line (in parametric)
	 * <p>
	 * The equation is given by :<br>
	 * <ul>
	 * <li>x = dx * t + x0</li>
	 * <li>y = dy * t + y0</li>
	 * </ul>
	 * 
	 * @param dx
	 *            delta x over delta t
	 * @param dy
	 *            delta y over delta t
	 * @param x0
	 *            initial x
	 * @param y0
	 *            initial y
	 * @return The nearest point belonging to the line
	 */
	public Point nearest(double dx, double dy, double x0, double y0) {
		return nearest(dx, dy, x0, y0, new Point());
	}

	/**
	 * Return the nearest point belonging to the segment [p1,p2]
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @param r
	 *            The result point
	 * @return The nearest point belonging to the segment
	 */
	public Point nearest(Point p1, Point p2, Point r) {
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		double x0 = p1.x;
		double y0 = p1.y;
		r = nearest(dx, dy, x0, y0, r);
		if (!r.isBetween(p1, p2)) {
			if (distance(p1) < distance(p2)) {
				r.x = p1.x;
				r.y = p1.y;
			} else {
				r.x = p2.x;
				r.y = p2.y;
			}
		}
		return r;
	}

	/**
	 * Return the nearest point belonging to the segment [p1,p2]
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @return The nearest point belonging to the segment
	 */
	public Point nearest(Point p1, Point p2) {
		return nearest(p1, p2, new Point());
	}

	/**
	 * Return the distance to a segment
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @param r
	 *            A Point object to work on
	 * @return The distance between current point and [p1,p2]
	 */
	public double distance(Point p1, Point p2, Point r) {
		return distance(nearest(p1, p2, r));
	}

	/**
	 * Return the distance to a segment
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @return The distance between current point and [p1,p2]
	 */
	public double distance(Point p1, Point p2) {
		return distance(nearest(p1, p2));
	}

	/**
	 * Return the distance to a line(in parametric)
	 * <p>
	 * The equation is given by :<br>
	 * <ul>
	 * <li>x = dx * t + x0</li>
	 * <li>y = dy * t + y0</li>
	 * </ul>
	 * 
	 * @param dx
	 *            delta x over delta t
	 * @param dy
	 *            delta y over delta t
	 * @param x0
	 *            initial x
	 * @param y0
	 *            initial y
	 * @param r
	 *            A Point object to work on
	 * @return The distance between current point and the given line
	 */
	public double distance(double dx, double dy, double x0, double y0, Point r) {
		return distance(nearest(dx, dy, x0, y0, r));
	}

	/**
	 * Return the distance to a line(in parametric)
	 * <p>
	 * The equation is given by :<br>
	 * <ul>
	 * <li>x = dx * t + x0</li>
	 * <li>y = dy * t + y0</li>
	 * </ul>
	 * 
	 * @param dx
	 *            delta x over delta t
	 * @param dy
	 *            delta y over delta t
	 * @param x0
	 *            initial x
	 * @param y0
	 *            initial y
	 * @return The distance between current point and the given line
	 */
	public double distance(double dx, double dy, double x0, double y0) {
		return distance(nearest(dx, dy, x0, y0));
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public boolean approx(Point pt) {
		return (Util.round(pt.x - x) == 0) && (Util.round(pt.y - y) == 0);
	}

	public Poly lineTo(Point p, double size) {
		return new PolyDefault(p, this, size);
	}
}