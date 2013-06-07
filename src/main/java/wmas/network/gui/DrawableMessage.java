package wmas.network.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Map;

import wmas.geometry.Point;
import wmas.network.NetworkQueue.ScheduledMessage;
import wmas.world.EntityInterface;

public class DrawableMessage {
	private static Line2D line = new Line2D.Double();

	private EntityInterface start;
	private EntityInterface end;
	private String message;
	private double departureTime;
	private double arrivalTime;
	private double droppedTime;
	private int networkId;

	public DrawableMessage(ScheduledMessage msg) {
		start = msg.getSender();
		end = msg.getRecipient();
		message = msg.getDescription();
		droppedTime = 0;
		departureTime = msg.getDeparture();
		arrivalTime = msg.getArrival();
		networkId = msg.getNetworkId();
	}

	public void drop(double t) {
		droppedTime = t;
	}

	String drawMessage(Graphics2D g, double xover, double yover, double t,
			double xinterval, Map<EntityInterface, DrawableLifeline> lifelines,
			double yorig, double pointPerTimeUnit) {
		if (!lifelines.containsKey(start) || !lifelines.containsKey(end))
			return "";
		double xdep = lifelines.get(start).getX(xinterval);
		double xarr = lifelines.get(end).getX(xinterval);
		double ydep, yarr;
		ydep = yorig + departureTime * pointPerTimeUnit;
		yarr = yorig
				+ pointPerTimeUnit
				* (droppedTime > 0 ? droppedTime : (t < arrivalTime ? t
						: arrivalTime));
		if (droppedTime > 0) {
			xarr = xdep + (xarr - xdep) * (droppedTime - departureTime)
					/ (arrivalTime - departureTime);
		} else if (t < arrivalTime) {
			xarr = xdep + (xarr - xdep) * (t - departureTime)
					/ (arrivalTime - departureTime);
		}
		if ((int) xarr == (int) xdep)
			return "";

		Point p = new Point(xover, yover);
		double d = p.distance(new Point(xarr, yarr), new Point(xdep, ydep));
		boolean overed = (d < NetworkDisplay.OVER_DISTANCE);

		g.setColor(Color.BLACK);
		line.setLine(xdep, ydep, xarr, yarr);
		g.draw(line);
		if (overed) {
			line.setLine(xdep, ydep + 1, xarr, yarr + 1);
			g.draw(line);
		}
		if (droppedTime > 0) {
			g.setColor(Color.RED);
			line.setLine(xarr - 5, yarr - 5, xarr + 5, yarr + 5);
			g.draw(line);
			line.setLine(xarr + 5, yarr - 5, xarr - 5, yarr + 5);
			g.draw(line);
		} else {
			double dx = (xarr - xdep);
			double dy = (yarr - ydep);
			double n = Math.sqrt(dx * dx + dy * dy);
			g.setColor(Color.BLACK);
			dy /= n;
			dx /= n;
			line.setLine(xarr - 7 * dx + 3 * dy, yarr - 7 * dy - 3 * dx, xarr,
					yarr);
			g.draw(line);
			line.setLine(xarr - 7 * dx - 3 * dy, yarr - 7 * dy + 3 * dx, xarr,
					yarr);
			g.draw(line);
		}
		if (overed) {
			return networkId + "\t:\t" + start.getName() + "\t->\t"
					+ end.getName() + "\t:\t" + message + "\n";
		}
		return "";
	}
}
