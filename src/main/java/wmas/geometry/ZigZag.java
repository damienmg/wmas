package wmas.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wmas.util.Doublet;

public class ZigZag {

	public enum Direction {
		RightBottom, LeftBottom, RightTop, LeftTop, TopRight, TopLeft, BottomRight, BottomLeft
	};

	private static HashMap<Direction, Point[]> directions = new HashMap<Direction, Point[]>();
	private static boolean initialized = false;

	private static synchronized void init() {
		if (!initialized) {
			initialized = true;
			Point x1 = new Point(1, 0);
			Point x2 = new Point(2, 0);
			Point x3 = new Point(-1, 0);
			Point x4 = new Point(-2, 0);
			Point y1 = new Point(0, 1);
			Point y2 = new Point(0, 2);
			Point y3 = new Point(0, -1);
			Point y4 = new Point(0, -2);
			// RightBottom
			Point[] v = new Point[5];
			v[0] = x2;
			v[1] = y1;
			v[2] = x4;
			v[3] = y1;
			v[4] = new Point(0, 0);
			directions.put(Direction.RightBottom, v);
			// RightTop
			v = new Point[5];
			v[0] = x2;
			v[1] = y3;
			v[2] = x4;
			v[3] = y3;
			v[4] = new Point(0, 1);
			directions.put(Direction.RightTop, v);
			// LeftBottom
			v = new Point[5];
			v[0] = x4;
			v[1] = y1;
			v[2] = x2;
			v[3] = y1;
			v[4] = new Point(1, 0);
			directions.put(Direction.LeftBottom, v);
			// LeftTop
			v = new Point[5];
			v[0] = x4;
			v[1] = y3;
			v[2] = x2;
			v[3] = y3;
			v[4] = new Point(1, 1);
			directions.put(Direction.LeftTop, v);
			// TopRight
			v = new Point[5];
			v[0] = y2;
			v[1] = x1;
			v[2] = y4;
			v[3] = x1;
			v[4] = new Point(0, 1);
			directions.put(Direction.TopRight, v);
			// TopLeft
			v = new Point[5];
			v[0] = y2;
			v[1] = x3;
			v[2] = y4;
			v[3] = x3;
			v[4] = new Point(1, 1);
			directions.put(Direction.TopLeft, v);
			// BottomRight
			v = new Point[5];
			v[0] = y4;
			v[1] = x1;
			v[2] = y2;
			v[3] = x1;
			v[4] = new Point(0, 0);
			directions.put(Direction.BottomRight, v);
			// BottomLeft
			v = new Point[5];
			v[0] = y4;
			v[1] = x3;
			v[2] = y2;
			v[3] = x3;
			v[4] = new Point(1, 0);
			directions.put(Direction.BottomLeft, v);
			//
		}
	}

	private static List<Point> getInitials(Poly p, double diameter) {
		double s = diameter / 2;
		double depsilon = diameter;
		List<Point> res = new ArrayList<Point>();
		List<Point> polyp = p.getAllPoints();
		Point p1 = new Point();
		Point p2 = new Point();
		Point p3 = new Point();
		Point p4 = new Point();
		for (Point pt : polyp) {
			p1.x = pt.getX() + depsilon;
			p1.y = pt.getY() + depsilon;
			p2.x = pt.getX() - depsilon;
			p2.y = pt.getY() + depsilon;
			p3.x = pt.getX() + depsilon;
			p3.y = pt.getY() - depsilon;
			p4.x = pt.getX() - depsilon;
			p4.y = pt.getY() - depsilon;
			if (p.contains(p1))
				res.add(new Point(pt.getX() + s, pt.getY() + s));
			if (p.contains(p3))
				res.add(new Point(pt.getX() + s, pt.getY() - s));
			if (p.contains(p4))
				res.add(new Point(pt.getX() - s, pt.getY() - s));
			if (p.contains(p2))
				res.add(new Point(pt.getX() - s, pt.getY() + s));
		}
		if (res.size() == 0 && polyp.size() > 0) {
			res.add(polyp.get(0));
		}
		return res;
	}

	private static Point[] getDirectionsAndInitial(Poly area, double diameter,
			Direction direction) {
		init();
		double r = diameter / 2;
		Point min = new Point(area.getBounds().getX() + r, area.getBounds()
				.getY() + r);
		double w = area.getBounds().getWidth() - diameter;
		double h = area.getBounds().getHeight() - diameter;
		Point[] res = new Point[5];
		Point[] v = directions.get(direction);
		res[4] = new Point(min.x + w * v[4].x, min.y + h * v[4].y);
		for (int i = 0; i < 4; i++) {
			double x = 0;
			double y = 0;
			switch ((int) Math.abs(v[i].x)) {
			case 1:
				x = Math.signum(v[i].x) * diameter;
				break;
			case 2:
				x = Math.signum(v[i].x) * (w + diameter);
				break;
			}
			switch ((int) Math.abs(v[i].y)) {
			case 1:
				y = Math.signum(v[i].y) * diameter;
				break;
			case 2:
				y = Math.signum(v[i].y) * (h + diameter);
				break;
			}
			res[i] = new Point(x, y);
		}
		return res;
	}

	public static List<Point> createZigZagPath(Area area, double diameter) {
		return createZigZagPath(area.toPoly(), diameter, Direction.RightBottom);
	}

	public static List<Point> createZigZagPath(Poly area, double diameter) {
		return createZigZagPath(area, diameter, Direction.RightBottom);
	}

	/**
	 * Determine the next point following the given direction (stop at the edge
	 * if necessary).
	 * 
	 * @param origin
	 *            Origin
	 * @param direction
	 *            Direction
	 * @param diameter
	 *            Diameter of the object
	 * @param p
	 *            Polygon to cover
	 * @return Next point
	 */
	private static Point goNext(Point origin, Point direction, double diameter,
			Poly p) {
		double dx = direction.getX();
		double dy = direction.getY();
		double ddx = Math.signum(dx) * (diameter / 2);
		double ddy = Math.signum(dy) * (diameter / 2);
		Point r = new Point(origin.getX() + dx + ddx, origin.getY() + dy + ddy);
		Point i = p.intersect(origin, r, diameter / 2);
		if (i == null) {
			r.x = r.x - ddx;
			r.y = r.y - ddy;
			if (!p.contains(r))
				return origin;
			return r;
		}
		i.x = i.x - ddx;
		i.y = i.y - ddy;
		if (!p.contains(i))
			return origin;
		return i;
	}

	private static Path createZigZag(Point origin, Poly area, double diameter,
			Direction direction) {
		List<Point> res = new ArrayList<Point>();
		Point[] p = getDirectionsAndInitial(area, diameter, direction);
		Point last = null;
		Point current = origin;
		int i = 0;
		while (!current.equals(last)) {
			res.add(current);
			last = current;
			current = goNext(last, p[i], diameter, area);
			i = (i + 1) % 4;
		}
		return new Path(res);

	}

	private static Doublet<Path, Doublet<Double, Boolean>> testNewPath(
			Point from, Point origin, Poly area, double diameter,
			Direction direction, Doublet<Path, Doublet<Double, Boolean>> old) {
		return testNewPath(from, origin, area, diameter, direction, old, false);
	}

	private static Doublet<Path, Doublet<Double, Boolean>> testNewPath(
			Point from, Point origin, Poly area, double diameter,
			Direction direction, Doublet<Path, Doublet<Double, Boolean>> old,
			boolean preferred) {
		Path nP = createZigZag(origin, area, diameter, direction);
		double nArea = nP.toArea(diameter).getArea();
		if (nArea > old.getSecond().getFirst()) {
			return new Doublet<Path, Doublet<Double, Boolean>>(nP,
					new Doublet<Double, Boolean>(nArea, preferred));
		}
		if (nArea < old.getSecond().getFirst())
			return old;
		if (preferred && !old.getSecond().getSecond())
			return new Doublet<Path, Doublet<Double, Boolean>>(nP,
					new Doublet<Double, Boolean>(nArea, preferred));
		else if (!preferred && old.getSecond().getSecond())
			return old;
		if (from.distance(old.getFirst().getFirst()) > from.distance(origin)) {
			return new Doublet<Path, Doublet<Double, Boolean>>(nP,
					new Doublet<Double, Boolean>(nArea, preferred));
		}
		return old;
	}

	private static Doublet<Path, Doublet<Double, Boolean>> testOrigin(
			Point from, Point origin, Poly area, double diameter,
			Doublet<Path, Doublet<Double, Boolean>> res,
			Direction preferredDirection) {
		res = testNewPath(from, origin, area, diameter, preferredDirection,
				res, true);
		res = testNewPath(from, origin, area, diameter, Direction.BottomLeft,
				res);
		res = testNewPath(from, origin, area, diameter, Direction.BottomRight,
				res);
		res = testNewPath(from, origin, area, diameter, Direction.LeftBottom,
				res);
		res = testNewPath(from, origin, area, diameter, Direction.LeftTop, res);
		res = testNewPath(from, origin, area, diameter, Direction.RightBottom,
				res);
		res = testNewPath(from, origin, area, diameter, Direction.RightTop, res);
		res = testNewPath(from, origin, area, diameter, Direction.TopLeft, res);
		res = testNewPath(from, origin, area, diameter, Direction.TopRight, res);
		return res;
	}

	public static Path coverZigZag(Point from, Poly area, double diameter) {
		return coverZigZag(from, area, diameter, Direction.RightBottom);
	}

	public static Path coverZigZag(Point from, Poly area, double diameter,
			Direction preferredDirection) {
		List<Point> pl = getInitials(area, diameter);
		if (pl.isEmpty())
			return null;
		Doublet<Path, Doublet<Double, Boolean>> res = new Doublet<Path, Doublet<Double, Boolean>>(
				new Path(), new Doublet<Double, Boolean>(0.0, false));
		for (Point p : pl)
			res = testOrigin(from, p, area, diameter, res, preferredDirection);
		return res.getFirst();
	}

	public static List<Point> createZigZagPath(Poly area, double diameter,
			Direction direction) {
		List<Point> res = new ArrayList<Point>();
		Point[] p = getDirectionsAndInitial(area, diameter, direction);
		Point last = null;
		Point current = p[4];
		int i = 0;
		while (!current.equals(last)) {
			res.add(current);
			last = current;
			current = goNext(last, p[i], diameter, area);
			i = (i + 1) % 4;
		}
		return res;
	}

	public static List<Point> createZigZagPath(Area area, double diameter,
			Direction direction) {
		return createZigZagPath(area.toPoly(), diameter, direction);
	}
}
