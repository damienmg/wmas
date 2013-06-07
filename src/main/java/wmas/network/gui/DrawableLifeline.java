package wmas.network.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.LinkedList;

import wmas.gui.shapes.DrawableShape;
import wmas.world.EntityInterface;

public class DrawableLifeline {
	private static Line2D line = new Line2D.Double();

	private EntityInterface entity;
	private DrawableShape ds;
	private DrawableShape oDs;
	private boolean hasNetworkAtStart;
	private boolean hasNetwork;
	private double time;
	private LinkedList<Double> timeNetworkUpDown = new LinkedList<Double>();

	private int position = 0;

	public DrawableLifeline(EntityInterface e, int position) {
		init(e, position);
	}

	public void init(EntityInterface e, int position) {
		entity = e;
		if (e.getDrawingShape() != null) {
			oDs = e.getDrawingShape();
			ds = oDs.copyShape();
		} else {
			oDs = null;
			ds = null;
		}
		hasNetworkAtStart = entity.getPosition() != null;
		hasNetwork = hasNetworkAtStart;
		timeNetworkUpDown.clear();
		time = 0;
		this.position = position;
	}

	public void update(double t) {
		if (entity.getPosition() == null) {
			if (hasNetwork) {
				timeNetworkUpDown.add(t);
				hasNetwork = false;
			}
		} else if (!hasNetwork) {
			hasNetwork = true;
			timeNetworkUpDown.add(t);
		}
		if (entity.getDrawingShape() != null && oDs != entity.getDrawingShape()) {
			oDs = entity.getDrawingShape();
			ds = oDs.copyShape();
		}
		if (ds != null)
			ds.colorize(entity.getColorization());
		time = t;
	}

	public double getX(double xinterval) {
		return xinterval * position + xinterval / 2;
	}

	public String draw(Graphics2D g, double xinterval, double y, double s,
			double pointsPerTimeUnit, double xover, double yover) {
		boolean over = true;
		double x = xinterval * position + xinterval / 2;
		if (Math.abs(xover - x) > NetworkDisplay.OVER_DISTANCE)
			over = false;
		if (yover < y)
			over = false;
		if (ds == null)
			return "";
		ds.setPosition(x, y);
		ds.setSize(s);
		ds.paint(g);
		double[] sPoint = ds.getConnectionPoint(0, 1);
		boolean draw = hasNetworkAtStart;
		for (double t : timeNetworkUpDown) {
			if (draw) {
				g.setColor(Color.BLACK);
				line.setLine(sPoint[0], sPoint[1], x, y + t * pointsPerTimeUnit);
				g.draw(line);
				if (over) {
					line.setLine(sPoint[0] + 1, sPoint[1], x + 1, y + t
							* pointsPerTimeUnit);
					g.draw(line);
				}
				g.setColor(Color.RED);
				line.setLine(x - s / 3, y + t * pointsPerTimeUnit - s / 3, x
						+ s / 3, y + t * pointsPerTimeUnit + s / 3);
				g.draw(line);
				line.setLine(x - s / 3, y + t * pointsPerTimeUnit + s / 3, x
						- s / 3, y + t * pointsPerTimeUnit + s / 3);
				g.draw(line);
				draw = false;
			} else {
				draw = true;
				sPoint[1] = y + t * pointsPerTimeUnit;
				g.setColor(Color.BLACK);
				line.setLine(sPoint[0] - s / 3, sPoint[1], sPoint[0] + s / 3,
						sPoint[1]);
				g.draw(line);
			}
		}
		if (draw) {
			g.setColor(Color.BLACK);
			line.setLine(sPoint[0], sPoint[1], x, y + time * pointsPerTimeUnit);
			g.draw(line);
			if (over) {
				line.setLine(sPoint[0] + 1, sPoint[1], x + 1, y + time
						* pointsPerTimeUnit);
				g.draw(line);
			}
			line.setLine(x, y + time * pointsPerTimeUnit, x - 3, y + time
					* pointsPerTimeUnit - 7);
			g.draw(line);
			line.setLine(x, y + time * pointsPerTimeUnit, x + 3, y + time
					* pointsPerTimeUnit - 7);
			g.draw(line);
		}
		return over ? (entity.getName() + "\n") : "";
	}
}
