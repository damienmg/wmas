package wmas.gui.shapes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import wmas.geometry.Point;
import wmas.util.Util;

public class ShapeDrawer {
	private final static int ARROW_SIZEH = 3;

	private final static int ARROW_SIZEW = 7;

	private static java.awt.Polygon poly = new java.awt.Polygon();
	private static Rectangle2D rectangle = new Rectangle2D.Double();

	static public void drawArrow(Line2D line, Graphics2D g) {
		drawArrow(line.getX1(), line.getY1(), line.getX2(), line.getY2(), g);
	}

	static public void drawArrow(double x1, double y1, double x2, double y2,
			Graphics2D g) {
		// Draw an arrow corresponding to line
		double dx = x2 - x1;
		double dy = y2 - y1;
		double l = Math.sqrt(dx * dx + dy * dy);
		double x = x2;
		double y = y2;
		dx /= l;
		dy /= l;
		int[] xs = new int[3];
		int[] ys = new int[3];
		xs[0] = Util.toInteger(x);
		ys[0] = Util.toInteger(y);
		x -= (dx * ARROW_SIZEW);
		y -= (dy * ARROW_SIZEW);
		xs[1] = Util.toInteger(x - dy * ARROW_SIZEH);
		ys[1] = Util.toInteger(y + dx * ARROW_SIZEH);
		xs[2] = Util.toInteger(x + dy * ARROW_SIZEH);
		ys[2] = Util.toInteger(y - dx * ARROW_SIZEH);
		poly.npoints = 3;
		poly.xpoints = xs;
		poly.ypoints = ys;
		g.fill(poly);
	}

	// Distance to a segment
	private static double distance(double x, double y, double x1, double x2,
			double y1, double y2) {
		Point p = new Point(x, y);
		Point p1 = new Point(x1, y1);
		Point p2 = new Point(x2, y2);
		return p.distance(p1, p2);
	}

	static public boolean contains(DrawableShape s, double x, double y) {
		double[] r = s.getBounds();
		if (s.isLine()) {
			double d = distance(x, y, r[0], r[0] + r[2], r[1], r[1] + r[3]);
			return d < ARROW_SIZEW + 2;
		} else {
			double dx = x - r[0];
			double dy = y - r[1];
			return dx >= -2 && dy >= -2 && (dx <= r[2] + 2) && (dy <= r[3] + 2);
		}
	}

	static public void drawRandomRect(Graphics2D g, double x1, double x2,
			double y1, double y2) {
		rectangle.setFrame(Math.min(x1, x2), Math.min(y1, y2),
				Math.abs(x1 - x2), Math.abs(y1 - y2));
		g.draw(rectangle);
	}

	static public void drawHandle(Graphics2D g, double x, double y, double w,
			double h, boolean top, boolean left, boolean filled) {
		double xf = x;
		double yf = y;
		if (!top)
			yf += h;
		if (!left)
			xf += w;

		g.drawRect(((int) (xf - 2)), ((int) (yf - 2)), 4, 4);
		if (filled)
			g.fillRect(((int) (xf - 2)), ((int) (yf - 2)), 4, 4);
	}

	static private java.awt.Polygon getLineBorders(double[] borders) {
		double dx = -borders[3];
		double dy = borders[2];
		double d = Math.sqrt(dx * dx + dy * dy);
		dx /= d;
		dy /= d;
		dx *= ARROW_SIZEW;
		dy *= ARROW_SIZEW;
		java.awt.Polygon p = poly;
		p.reset();
		p.addPoint((int) (borders[0] - dx), (int) (borders[1] - dy));
		p.addPoint((int) (borders[0] + dx), (int) (borders[1] + dy));
		p.addPoint((int) (borders[0] + borders[2] + dx), (int) (borders[1]
				+ borders[3] + dy));
		p.addPoint((int) (borders[0] + borders[2] - dx), (int) (borders[1]
				+ borders[3] - dy));
		return p;
	}

	static private void drawLineBorder(Graphics2D g, double[] borders,
			boolean resizeable) {
		java.awt.Polygon p = getLineBorders(borders);
		g.draw(p);
		if (resizeable) {
			drawHandle(g, borders[0], borders[1], borders[2], borders[3], true,
					true, false);
			drawHandle(g, borders[0], borders[1], borders[2], borders[3],
					false, false, false);
		}
	}

	static private void drawSquareBorder(Graphics2D g, double[] borders,
			boolean diag, boolean antidiag) {
		double x1 = borders[0] - 1;
		double y1 = borders[1] - 1;
		double x2 = borders[0] + borders[2] + 1;
		double y2 = borders[1] + borders[3] + 1;
		drawRandomRect(g, x1, x2, y1, y2);
		if (diag) {
			drawHandle(g, borders[0], borders[1], borders[2], borders[3], true,
					true, false);
			drawHandle(g, borders[0], borders[1], borders[2], borders[3],
					false, false, false);
		}
		if (antidiag) {
			drawHandle(g, borders[0], borders[1], borders[2], borders[3], true,
					false, false);
			drawHandle(g, borders[0], borders[1], borders[2], borders[3],
					false, true, false);
		}
	}

	static public void drawBorder(Graphics2D g, Color c, DrawableShape s) {
		boolean diag = s.isResizeable();
		boolean antidiag = s.hasBothResizeableDirections();
		boolean isMoveable = s.isMoveable();
		if (!(s.isSelectable()) && !diag && !antidiag && !isMoveable)
			return;
		double[] borders = s.getBounds();
		g.setColor(c);
		if (s.isLine()) {
			drawLineBorder(g, borders, diag);
		} else {
			drawSquareBorder(g, borders, diag, antidiag);
		}
	}

	static private int selectType(double x, double y, DrawableShape s) {
		boolean diag = s.isResizeable();
		boolean antidiag = s.hasBothResizeableDirections() && !s.isLine();
		boolean isMoveable = s.isMoveable();
		double[] borders = s.getBounds();
		int r = isMoveable ? 0 : -1;
		double dx = borders[0] - x;
		double dy = borders[1] - y;
		double dx2 = dx + borders[2];
		double dy2 = dy + borders[3];
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		dx2 = Math.abs(dx2);
		dy2 = Math.abs(dy2);
		if (diag) {
			if (dx <= 2 && dy <= 2)
				return 1;
			if (dx2 <= 2 && dy2 <= 2)
				return 2;
		}
		if (antidiag) {
			if (dx <= 2 && dy2 <= 2)
				return 3;
			if (dx2 <= 2 && dy <= 2)
				return 4;
		}
		return r;
	}

	static boolean top, left, dHandle;

	static public double[] getUpdatedBorders(double origX, double origY,
			double newX, double newY, DrawableShape s) {
		double[] borders = s.getBounds();
		int t = selectType(origX, origY, s);
		if (t < 0)
			return null;
		top = false;
		left = false;
		dHandle = false;
		boolean antidiag = s.hasBothResizeableDirections();

		double dx = -(newX - origX);
		double dy = -(newY - origY);
		if (!antidiag && t > 0 && !s.isLine()) {
			if (Math.abs(dy) > Math.abs(dx))
				dx = dy;
			else if (Math.abs(dx) > Math.abs(dy))
				dy = dx;
		}

		switch (t) {
		case 0: // move
			borders[0] -= dx;
			borders[1] -= dy;
			break;
		case 1: // resize using the upper left corner
			if (!s.isLine()) {
				if (dx <= -borders[2])
					dx = -borders[2] + 1;
				if (dy <= -borders[3])
					dy = -borders[3] + 1;
			}
			borders[0] -= dx;
			borders[1] -= dy;
			borders[2] += dx;
			borders[3] += dy;
			dHandle = true;
			top = true;
			left = true;
			break;
		case 2: // resize using the lower right corner
			dx = -dx;
			dy = -dy;
			if (!s.isLine()) {
				if (dx <= -borders[2])
					dx = -borders[2] + 1;
				if (dy <= -borders[3])
					dy = -borders[3] + 1;
			}
			borders[2] += dx;
			borders[3] += dy;
			dHandle = true;
			top = false;
			left = false;
			break;
		case 4: // resize using the upper right corner
			dx = -dx;
			if (!s.isLine()) {
				if (dx <= -borders[2])
					dx = -borders[2] + 1;
				if (dy <= -borders[3])
					dy = -borders[3] + 1;
			}
			borders[1] -= dy;
			borders[2] += dx;
			borders[3] += dy;
			dHandle = true;
			top = true;
			left = false;
			break;
		case 3: // resize using the lower left corner
			dy = -dy;
			if (!s.isLine()) {
				if (dx <= -borders[2])
					dx = -borders[2] + 1;
				if (dy <= -borders[3])
					dy = -borders[3] + 1;
			}
			borders[0] -= dx;
			borders[2] += dx;
			borders[3] += dy;
			dHandle = true;
			top = false;
			left = true;
			break;
		}
		return borders;
	}

	static public void drawMouseDown(Graphics2D g, Color c, double origX,
			double origY, double newX, double newY, DrawableShape s) {
		double[] borders = getUpdatedBorders(origX, origY, newX, newY, s);
		if (borders == null)
			return;
		borders[0]--;
		borders[1]--;
		borders[2] += 2;
		borders[3] += 2;
		g.setColor(c);
		if (s.isLine()) {
			drawLineBorder(g, borders, false);
			left = top;
		} else {
			drawSquareBorder(g, borders, false, false);
		}
		if (dHandle) {
			drawHandle(g, borders[0], borders[1], borders[2], borders[3], top,
					left, true);
		}
	}

	static public void doMouseRelease(double origX, double origY, double newX,
			double newY, DrawableShape s) {
		double[] borders = getUpdatedBorders(origX, origY, newX, newY, s);
		if (borders == null)
			return;
		s.setSize(borders[2], borders[3]);
		s.setPosition(borders[0] + borders[2] / 2, borders[1] + borders[3] / 2);
	}

	static public int getStringWidth(Graphics2D g, String s) {
		String[] rs = s.split("\n");
		int sW = 0;
		FontMetrics fm = g.getFontMetrics();
		for (String r : rs) {
			sW = Math.max(sW, fm.stringWidth(r));
		}
		return sW;
	}

	static public int getStringHeight(Graphics2D g, String s) {
		String[] rs = s.split("\n");
		FontMetrics fm = g.getFontMetrics();
		return fm.getHeight() * rs.length;
	}

	static public void drawText(Graphics2D g, String s, int x, int y) {
		String[] rs = s.split("\n");
		FontMetrics fm = g.getFontMetrics();
		for (String r : rs) {
			g.drawString(r, x, y);
			y += fm.getHeight();
		}
	}
}
