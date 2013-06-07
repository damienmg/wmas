package wmas.geometry;

import java.awt.Color;
import java.awt.Graphics2D;

import wmas.util.Util;

public class ComplexArea {

	Color c;
	Poly areas;

	public ComplexArea(Color c) {
		areas = new PolyDefault();
		areas.clear();
		this.c = c;
	}

	public void addPoly(Poly na) {
		areas = areas.union(na);
	}

	// Add an area as a square centered at (x,y)
	public Poly addArea(double x, double y, double size) {
		double x0 = x - size / 2;
		double y0 = y - size / 2;
		double width = size;
		double height = size;
		Poly p = new PolyDefault(Util.round(x0), Util.round(y0),
				Util.round(width), Util.round(height));
		if (c != null)
			areas = areas.union(p);
		return p;
	}

	static public Poly addPath(double x0, double y0, double x1, double y1,
			double s) {
		Poly p = new PolyDefault();
		double size = s / 2;
		double szx = Math.abs(x1 - x0) + 2 * size;
		double szy = Math.abs(y1 - y0) + 2 * size;
		double x = Math.min(x0, x1);
		double y = Math.min(y0, y1);
		p = new PolyDefault(Util.round(x - size), Util.round(y - size),
				Util.round(szx), Util.round(szy));
		return p;
	}

	public Color getColor() {
		return c;
	}

	public void setColor(Color c) {
		this.c = c;
	}

	public void reset() {
		areas.clear();
	}

	public void draw(Graphics2D g) {
		if (c != null) {
			g.setColor(c);
			areas.draw(g);
		}
	}

	public void fill(Graphics2D g) {
		if (c != null) {
			g.setColor(c);
			areas.fill(g);
		}
	}

	public Poly getAreas() {
		return areas;
	}

}
