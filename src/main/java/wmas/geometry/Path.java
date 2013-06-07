package wmas.geometry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.geometry.ZigZag.Direction;
import wmas.gui.shapes.DrawableShape;
import wmas.util.Doublet;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class Path implements XMLEntity, Iterable<Point> {

	LinkedList<Point> path;
	ListIterator<Point> pathIterator;
	private Point lastNext = null;

	public double[] toDoubleArray() {
		double[] r = new double[path.size() * 2];
		int i = 0;
		for (Point p : path) {
			r[i++] = p.x;
			r[i++] = p.y;
		}
		return r;
	}

	public Path(double[] p) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		for (int i = 1; i < p.length; i += 2) {
			path.add(new Point(p[i - 1], p[i]));
		}
	}

	public Path() {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
	}

	public Path(List<Point> p) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		path.addAll(p);
		pathIterator = null;
	}

	public Path(Path p) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		add(p);
	}

	public Path(Path p1, Path p2) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		add(p1);
		add(p2);
	}

	public Path(Area area, double diameter, Direction direction) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		path.addAll(ZigZag.createZigZagPath(area, diameter, direction));
	}

	public Path(Area area, double diameter) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		path.addAll(ZigZag.createZigZagPath(area, diameter));
	}

	public Path(Point from, Poly area, double diameter) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		path.addAll(cover(from, area, diameter).path);
	}

	public Path(Point p1, Point p2) {
		super();
		path = new LinkedList<Point>();
		path.clear();
		pathIterator = null;
		path.add(p1);
		path.add(p2);
	}

	public void add(double x, double y) {
		path.addLast(new Point(x, y));
	}

	public void add(Point p) {
		path.addLast(new Point(p));
	}

	public void addFirst(Point o) {
		path.addFirst(new Point(o));
	}

	public void remove() {
		if (!path.isEmpty())
			path.removeLast();
	}

	public void remove(int index) {
		path.remove(index);
	}

	public void clear() {
		path.clear();
	}

	public void add(Path p) {
		if (path.isEmpty())
			path.addAll(p.path);
		else {
			for (Point pt : p.path) {
				if (!path.getLast().approx(pt))
					path.addLast(new Point(pt));
			}
		}
	}

	public void invert() {
		LinkedList<Point> l = path;
		path = new LinkedList<Point>();
		path.clear();
		Iterator<Point> it = l.iterator();
		while (it.hasNext())
			path.addFirst(it.next());
	}

	public void resetPath() {
		pathIterator = null;
		lastNext = null;
	}

	public Point initial() {
		pathIterator = path.listIterator();
		lastNext = path.getFirst();
		return path.getFirst();
	}

	public Point getLast() {
		return path.getLast();
	}

	public Point getFirst() {
		return path.getFirst();
	}

	public boolean isEmpty() {
		return path.isEmpty();
	}

	private boolean positionPathIterator(Point orig) {
		if (pathIterator == null)
			pathIterator = path.listIterator();
		if (!pathIterator.hasNext())
			return false;
		Point n1 = pathIterator.next();
		while (pathIterator.hasNext()) {
			Point n2 = pathIterator.next();
			if (orig.isBetween(n1, n2)) {
				pathIterator.previous();
				return true;
			}
			n1 = n2;
		}
		return false;
	}

	private Doublet<Point, Double> getNextPoint(double d, Point r) {
		Point next = pathIterator.next();
		double d2 = r.distance(next);
		if (d2 > d) {
			pathIterator.previous();
			pathIterator.previous();
			double d1 = d / d2;
			double x = (1 - d1) * r.x + d1 * next.x;
			double y = (1 - d1) * r.y + d1 * next.y;
			Point p = new Point(x, y);
			lastNext = p;
			return new Doublet<Point, Double>(p, d);
		}
		lastNext = next;
		if (d2 == d)
			pathIterator.previous();
		return new Doublet<Point, Double>(next, d2);
	}

	public Doublet<Point, Poly> getNext(Point orig, double distance, double s) {
		if (distance == 0) {
			lastNext = orig;
			return new Doublet<Point, Poly>(orig, null);
		}
		if (!positionPathIterator(orig))
			if (!positionPathIterator(orig)) {
				lastNext = getLast();
				return new Doublet<Point, Poly>(orig, null);
			}
		Poly poly = null;
		Point r = orig;
		double d = distance;
		while (pathIterator.hasNext()) {
			Doublet<Point, Double> next = getNextPoint(d, r);
			d -= next.getSecond();
			Poly poly2 = ComplexArea.addPath(r.x, r.y, next.getFirst().x,
					next.getFirst().y, s);
			if (poly != null)
				poly = poly2.union(poly);
			else
				poly = poly2;
			if (d <= 0)
				return new Doublet<Point, Poly>(next.getFirst(), poly);
			r = next.getFirst();
		}
		return new Doublet<Point, Poly>(r, poly);
	}

	public Area getBounds() {
		Area a = null;
		for (Point p : path) {
			if (a == null)
				a = new Area(p.x, p.y, 0, 0);
			else
				a.extend(p);
		}
		return a;
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		path.clear();
		pathIterator = null;
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				XMLEntity xe = XMLInterpretor.convert((Element) n, refs);
				if (xe instanceof Point)
					path.add((Point) xe);
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) {
		Element e = root.createElement(this.getClass().getName());
		toXML(e, root, refs);
		return e;
	}

	public void toXML(Element e, Document root, XMLCrossRef refs) {
		for (Point p : path)
			e.appendChild(p.toXML(root, refs));
	}

	public Iterator<Point> iterator() {
		return path.iterator();
	}

	public String toString() {
		String s = "";
		for (Point p : path) {
			s += "(" + p.x + "," + p.y + "), ";
		}
		if (s != "")
			s = s.substring(0, s.length() - 2);
		return "[" + s + "]";
	}

	public double length() {
		double r = 0;
		Point last = null;
		for (Point p : path) {
			if (last != null)
				r += last.distance(p);
			last = p;
		}
		return r;
	}

	public int size() {
		return path.size();
	}

	public double remaininglength() {
		if (lastNext == null)
			return length();
		if (!pathIterator.hasNext())
			return 0;
		Iterator<Point> pi = path.listIterator(pathIterator.nextIndex());
		pi.next();
		Point r = lastNext;
		double d = 0;
		while (pi.hasNext()) {
			Point next = pi.next();
			d += r.distance(next);
			r = next;
		}
		return d;
	}

	public double remaininglength(Point until, double tolerance) {
		Point p = new Point();
		if (lastNext == null)
			return length();
		if (!pathIterator.hasNext())
			return 0;
		Iterator<Point> pi = path.listIterator(pathIterator.nextIndex());
		pi.next();
		Point r = lastNext;
		double d = 0;
		while (pi.hasNext()) {
			Point next = pi.next();
			p = until.nearest(r, next, p);
			double d1 = p.distance(until);
			if (d1 <= tolerance) {
				double rp = r.distance(p);
				if (p.equals(r))
					return d;
				if (d1 == tolerance)
					return d + rp;
				double gamma = d1 * d1 - tolerance * tolerance;
				double beta = 2 * Point.scalar(r, p, p, until) / rp;
				/* resolve d2^2 + beta * d2 + gamma = 0 */
				double delta = beta * beta - 4 * gamma;
				double d2 = 0;
				if (delta > 0) {
					double del = Math.sqrt(delta);
					if (beta > -del)
						d2 = (-beta + del) / 2;
					else
						d2 = (-beta - del) / 2;
				} else if (delta == 0) {
					d2 = -beta / 2;
				}
				if (d2 > 0)
					return Math.max(d - d2 + rp, 0); // Max for when the last
														// point was already in
														// the asked disc
				return d + rp;
			} else
				d += r.distance(next);
		}
		return -1;
	}

	public double getMinSegmentSize() {
		double r = Double.POSITIVE_INFINITY;
		if (path == null)
			return 0;
		if (path.isEmpty())
			return 0;
		Point p = null;
		for (Point p2 : path) {
			if (p != null)
				r = Math.min(r, p.distance(p2));
			p = p2;
		}
		if (r == Double.POSITIVE_INFINITY)
			return 0;
		return r;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			Path p2 = (Path) obj;
			Iterator<Point> it1 = path.iterator();
			Iterator<Point> it2 = p2.path.iterator();
			while (it1.hasNext() && it2.hasNext()
					&& (it1.next().equals(it2.next())))
				;
			return !it1.hasNext() && !it2.hasNext();
		}
		return false;
	}

	public Path getSubPath(Point p) {
		Point last = null;
		Path r = new Path();
		for (Point pt : path) {
			if (last != null) {
				r.add(last);
				if (p.isBetween(last, pt)) {
					r.add(p);
					return r;
				}
			}
			last = pt;
		}
		r.clear();
		return r;
	}

	public Path endSubPath(double d) {
		Path r = new Path();
		Point last = null;
		double l = length();
		for (Point p : path) {
			if (l <= d)
				r.add(p);
			else if (last != null) {
				double d1 = last.distance(p);
				if (d1 > l - d) {
					double d2 = (l - d) / d1;
					double x = (1 - d2) * last.x + d2 * p.x;
					double y = (1 - d2) * last.y + d2 * p.y;
					Point next = new Point(x, y);
					r.add(next);
					r.add(p);
				} else if (d1 == d - l) {
					r.add(p);
				}
				l -= d1;
			}
			last = p;
		}
		return r;
	}

	public Path beginSubPath(double d) {
		Path r = new Path();
		Point last = null;
		for (Point p : path) {
			if (last != null) {
				double d1 = last.distance(p);
				if (Util.round(d1 - d) == 0) {
					r.add(p);
					return r;
				}
				if (d1 > d) {
					double d2 = d / d1;
					double x = (1 - d2) * last.x + d2 * p.x;
					double y = (1 - d2) * last.y + d2 * p.y;
					Point next = new Point(x, y);
					r.add(next);
					return r;
				}
			}
			r.add(p);
			last = p;
		}
		return r;
	}

	public Path minus(Path coveredPath) {
		Path r = new Path();
		if (coveredPath.size() > 1) {
			r.addFirst(coveredPath.getLast());
			Point p = path.get(coveredPath.size() - 1);
			if (!coveredPath.getLast().equals(p))
				r.add(p);
			for (Iterator<Point> it = path.listIterator(coveredPath.size()); it
					.hasNext(); r.add(it.next()))
				;
		} else
			r.add(this);
		return r;
	}

	public Poly toArea(double size) {
		Poly r = new PolyDefault();
		Point last = null;
		for (Point p : path) {
			if (last != null)
				r = r.union(last.lineTo(p, size));
			else {
				Poly rp = new PolyDefault(Util.round(p.x) - size / 2,
						Util.round(p.y) - size / 2, size, size);
				r = r.union(rp);
			}
			last = p;
		}
		return r;
	}

	public static Path cover(Point from, Poly p, double sz) {
		return ZigZag.coverZigZag(from, p, sz);
	}

	public static Path cover(double[] from, Poly p, DrawableShape sz) {
		if (sz == null)
			return new Path();
		double[] s = sz.getBounds();
		if (s == null)
			return new Path();
		return ZigZag.coverZigZag(new Point(from), p, Math.min(s[2], s[3]));
	}

	public void translateOriginTo(double x, double y) {
		// TODO check
		if (path.size() == 0)
			return;
		double dx = x - path.get(0).x;
		double dy = y - path.get(0).y;
		for (Point p : path) {
			p.x += dx;
			p.y += dy;
		}
	}

	@Override
	public wmas.xml.XMLEntity copy() {
		return new Path(this);
	}

	public Point get(int index) {
		return path.get(index);
	}
}
