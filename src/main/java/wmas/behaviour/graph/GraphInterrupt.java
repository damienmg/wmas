/**
 * 
 */
package wmas.behaviour.graph;

import java.awt.Graphics2D;
import java.util.Map;

import wmas.gui.shapes.DrawableShape;
import wmas.xml.XMLEntity;

public class GraphInterrupt extends GraphTransition {
	private static java.awt.Polygon poly = new java.awt.Polygon();
	private final int DELTA_ORIGIN = 3;

	public GraphInterrupt() { // FOR XML
		super(null, null);
	}

	public GraphInterrupt(DrawableShape source, DrawableShape dest) {
		super(source, dest);
	}

	@Override
	public void paint(Graphics2D g) {
		super.paint(g);
		if (getColor() != null) {
			double[] points = getPoints();
			g.setColor(getColor());
			double x = points[0];
			double y = points[1];
			double dx = x - points[2];
			double dy = y - points[3];
			double d = Math.sqrt(dx * dx + dy * dy);
			dx = DELTA_ORIGIN * dx / d;
			dy = DELTA_ORIGIN * dy / d;
			x += dx;
			y += dy;
			poly.reset();
			poly.addPoint((int) (x - dx), (int) (y - dy));
			poly.addPoint((int) (x + dy), (int) (y - dx));
			poly.addPoint((int) (x + dx), (int) (y + dy));
			poly.addPoint((int) (x - dy), (int) (y + dx));
			g.draw(poly);
		}
	}

	public GraphInterrupt copy(DrawableShape orig,
			Map<GraphBehaviour, GraphBehaviour> map) {
		GraphInterrupt r = new GraphInterrupt(orig, map.get(this
				.getDestination()));
		copy(r);
		return r;
	}

	public XMLEntity copy() {
		GraphInterrupt r = new GraphInterrupt(getSource(), getDestination());
		copy(r);
		return r;
	}

}