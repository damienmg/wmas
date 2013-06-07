/*
 * The SEI Software Open Source License, Version 1.0
 *
 * Copyright (c) 2004, Solution Engineering, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Solution Engineering, Inc. (http://www.seisw.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 3. The name "Solution Engineering" must not be used to endorse or
 *    promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    admin@seisw.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SOLUTION ENGINEERING, INC. OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package wmas.geometry;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class PolyDefault implements Poly {

	private java.awt.geom.Area internal;

	public PolyDefault() {
		internal = new java.awt.geom.Area();
	}

	public PolyDefault(PolyDefault p) {
		internal = new java.awt.geom.Area(p.internal);
	}

	public PolyDefault(double x, double y, double width, double height) {
		internal = new java.awt.geom.Area(new Rectangle2D.Double(x, y, width,
				height));
	}

	public PolyDefault(Shape s) {
		internal = new java.awt.geom.Area(s);
	}

	public PolyDefault(Point start, Point end, double size) {
		// Determine the vector
		double vx = end.x - start.x;
		double vy = end.y - start.y;
		double vn = Math.sqrt(vx * vx + vy * vy);
		double delta = size / (2 * vn);
		double dx = vx * delta;
		double dy = vy * delta;
		// The perpendicular vector
		double dpx = -dy;
		double dpy = dx;
		// Determine the real start and the real end
		double sx = Util.round(start.x - dx);
		double sy = Util.round(start.y - dy);
		double ex = Util.round(end.x + dx);
		double ey = Util.round(end.y + dy);
		// Now determine all the points
		double w = Util.round(Math.abs(ex - sx + 2 * dpx));
		double h = Util.round(Math.abs(ey - sy + 2 * dpy));
		internal = new java.awt.geom.Area(new Rectangle2D.Double(Math.min(sx,
				ex), Math.min(sy, ey), w, h));
	}

	public void clear() {
		internal = new java.awt.geom.Area();

	}

	public boolean contains(double x, double y) {
		return internal.contains(x, y);
	}

	public boolean contains(Point p) {
		return internal.contains(p.getX(), p.getY());
	}

	public boolean contains(Point2D p) {
		return internal.contains(p);
	}

	public void draw(Graphics2D g) {
		g.draw(internal);
	}

	public void fill(Graphics2D g) {
		g.fill(internal);
	}

	public int getNumPoints() {
		int i = 0;
		for (PathIterator it = internal.getPathIterator(null); !it.isDone(); it
				.next())
			i++;
		return i;
	}

	public List<Point> getAllPoints() {
		List<Point> l = new LinkedList<Point>();
		Point last = null;
		Point lastlast = null;
		double[] coords = new double[6];
		for (PathIterator it = internal.getPathIterator(null); !it.isDone(); it
				.next()) {
			int t = it.currentSegment(coords);
			switch (t) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				Point p = new Point(coords[0], coords[1]);
				if (lastlast != null) {
					if (last.isBetween(p, lastlast)) {
						l.remove(l.size() - 1);
						last = lastlast;
					}
				}
				l.add(p);
				lastlast = last;
				last = p;
				break;
			}
		}
		return l;
	}

	public double getArea() {
		double sum = 0.0;
		double r = 0;
		Point last = null;
		Point first = null;
		Point lastlast = null;
		double[] coords = new double[6];
		for (PathIterator it = internal.getPathIterator(null); !it.isDone(); it
				.next()) {
			int t = it.currentSegment(coords);
			Point p = new Point(coords[0], coords[1]);
			switch (t) {
			case PathIterator.SEG_LINETO:
				if (lastlast != null) {
					if (last.isBetween(p, lastlast)) {
						sum -= (lastlast.getX() * last.getY())
								- (lastlast.getY() * last.getX());
						last = lastlast;
					}
				}
				if (last != null)
					sum += (last.getX() * p.getY()) - (last.getY() * p.getX());
				lastlast = last;
				last = p;
				break;
			case PathIterator.SEG_MOVETO:
				if ((last != null) && (first != null) && (lastlast != null)) {
					r += Math.abs(sum + (last.getX() * first.getY())
							- (last.getY() * first.getX()));
				}
				sum = 0;
				first = p;
				lastlast = null;
				last = p;
				break;
			}
		}
		if ((last != null) && (first != null) && (lastlast != null)) {
			r += Math.abs(sum + (last.getX() * first.getY())
					- (last.getY() * first.getX()));
		}
		return Math.round(r / 2);
	}

	public Area getBounds() {
		return new Area(internal.getBounds2D());
	}

	public Point intersect(Point origin, Point dest) {
		List<Point> l = getAllPoints();
		Point last = null;
		Point r = new Point();
		for (Point p : l) {
			if (last != null) {
				r.intersect(last, p, origin, dest);
				if (r.isBetween(last, p) && r.isBetween(origin, dest))
					return r;
			}
			last = p;
		}
		if (last != null) {
			r.intersect(last, l.get(0), origin, dest);
			if (r.isBetween(last, l.get(0)) && r.isBetween(origin, dest))
				return r;
		}
		return null;
	}

	public Point intersect(Point origin, Point dest, double d) {
		List<Point> l = getAllPoints();
		Point last = null;
		Point r = new Point();
		Point f = null;
		for (Point p : l) {
			if (last != null) {
				r.intersect(last, p, origin, dest);
				if (r.isBetween(last, p, d) && r.isBetween(origin, dest)) {
					if ((f == null)
							|| (f.distance(origin) > r.distance(origin))) {
						Point nP = null;
						if (f != null)
							nP = f;
						else
							nP = new Point();
						f = r;
						r = nP;
					}
				}
			}
			last = p;
		}
		if (last != null) {
			r.intersect(last, l.get(0), origin, dest);
			if (r.isBetween(last, l.get(0), d) && r.isBetween(origin, dest))
				if ((f == null) || (f.distance(origin) > r.distance(origin)))
					return r;
		}
		return f;
	}

	public Poly intersection(Poly p) {
		PolyDefault r = new PolyDefault();
		r.internal = (java.awt.geom.Area) internal.clone();
		r.internal.intersect(((PolyDefault) p).internal);
		return r;
	}

	public boolean isEmpty() {
		return internal.isEmpty();
	}

	public Poly union(Poly p) {
		PolyDefault r = new PolyDefault();
		r.internal = (java.awt.geom.Area) internal.clone();
		r.internal.add(((PolyDefault) p).internal);
		return r;
	}

	public Poly xor(Poly p) {
		PolyDefault r = new PolyDefault();
		r.internal = (java.awt.geom.Area) internal.clone();
		r.internal.exclusiveOr(((PolyDefault) p).internal);
		return r;
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		GeneralPath p = new GeneralPath();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element n = (Element) nl.item(i);
				if (n.getTagName().equals("seg_moveto")
						|| n.getTagName().equals("seg_move"))
					p.moveTo(Float.parseFloat(n.getAttribute("x")),
							Float.parseFloat(n.getAttribute("y")));
				else if (n.getTagName().equals("seg_lineto")
						|| n.getTagName().equals("seg_line"))
					p.lineTo(Float.parseFloat(n.getAttribute("x")),
							Float.parseFloat(n.getAttribute("y")));
				else if (n.getTagName().equals("seg_close"))
					p.closePath();
				else if (n.getTagName().equals("seg_quadto")
						|| n.getTagName().equals("seg_quad"))
					p.quadTo(Float.parseFloat(n.getAttribute("x1")),
							Float.parseFloat(n.getAttribute("y1")),
							Float.parseFloat(n.getAttribute("x2")),
							Float.parseFloat(n.getAttribute("y2")));
				else if (n.getTagName().equals("seg_cubicto")
						|| n.getTagName().equals("seg_cubic"))
					p.curveTo(Float.parseFloat(n.getAttribute("x1")),
							Float.parseFloat(n.getAttribute("y1")),
							Float.parseFloat(n.getAttribute("x2")),
							Float.parseFloat(n.getAttribute("y2")),
							Float.parseFloat(n.getAttribute("x3")),
							Float.parseFloat(n.getAttribute("y3")));
			}
		}
		internal = new java.awt.geom.Area(p);
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		toXML(e, root);
		return e;
	}

	public void toXML(Element e, Document root) throws Exception {
		double[] coords = new double[6];
		for (PathIterator it = internal.getPathIterator(null); !it.isDone(); it
				.next()) {
			int t = it.currentSegment(coords);
			Element c = null;
			switch (t) {
			case PathIterator.SEG_CLOSE:
				c = root.createElement("seg_close");
				break;
			case PathIterator.SEG_CUBICTO:
				c = root.createElement("seg_cubicto");
				c.setAttribute("x1", Double.toString(coords[0]));
				c.setAttribute("y1", Double.toString(coords[1]));
				c.setAttribute("x2", Double.toString(coords[2]));
				c.setAttribute("y2", Double.toString(coords[3]));
				c.setAttribute("x3", Double.toString(coords[4]));
				c.setAttribute("y3", Double.toString(coords[5]));
				break;
			case PathIterator.SEG_QUADTO:
				c = root.createElement("seg_quadto");
				c.setAttribute("x1", Double.toString(coords[0]));
				c.setAttribute("y1", Double.toString(coords[1]));
				c.setAttribute("x2", Double.toString(coords[2]));
				c.setAttribute("y2", Double.toString(coords[3]));
				break;
			case PathIterator.SEG_MOVETO:
				c = root.createElement("seg_moveto");
				c.setAttribute("x", Double.toString(coords[0]));
				c.setAttribute("y", Double.toString(coords[1]));
				break;
			case PathIterator.SEG_LINETO:
				c = root.createElement("seg_lineto");
				c.setAttribute("x", Double.toString(coords[0]));
				c.setAttribute("y", Double.toString(coords[1]));
				break;
			}
			if (c != null)
				e.appendChild(c);
		}
	}

	public Poly subtract(Poly p) {
		PolyDefault r = new PolyDefault();
		r.internal = (java.awt.geom.Area) internal.clone();
		r.internal.subtract(((PolyDefault) p).internal);
		return r;
	}

	@Override
	public XMLEntity copy() {
		return new PolyDefault(this);
	}

}
