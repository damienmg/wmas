package wmas.network.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.elements.Circle;
import wmas.gui.shapes.elements.Connector;
import wmas.network.InternalNetworkListener;
import wmas.network.Network;
import wmas.network.NetworkQueue.ScheduledMessage;
import wmas.network.NetworksAttribute;
import wmas.util.Util;
import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.WorldBehaviour;

public class NetworkDisplay extends Component implements
		InternalNetworkListener, MouseMotionListener, MouseListener,
		ActionListener {

	private static final long serialVersionUID = 5822817690552627009L;

	public static final int OVER_DISTANCE = 10;

	protected Map<EntityInterface, List<DrawableMessage>> messages = new HashMap<EntityInterface, List<DrawableMessage>>();
	protected Map<EntityInterface, DrawableLifeline> lifelines = new HashMap<EntityInterface, DrawableLifeline>();
	protected Map<ScheduledMessage, DrawableMessage> msgMap = new HashMap<ScheduledMessage, DrawableMessage>();

	protected double pixelPerTimeUnit;

	protected double marginHigh;

	private double xover;
	private double yover;

	protected JScrollPane parent;

	protected World world;
	protected Network network;
	protected SchemeView worldView;

	protected Stack<Connector> freeConnectors = new Stack<Connector>();
	protected Map<EntityInterface, List<Circle>> ranges = new HashMap<EntityInterface, List<Circle>>();
	protected Map<EntityInterface, Map<EntityInterface, Connector>> connections = new HashMap<EntityInterface, Map<EntityInterface, Connector>>();
	protected JToggleButton rangeButton = null;
	protected JToggleButton conButton = null;

	private void createRanges() {
		ranges.clear();
		connections.clear();
		if (world != null) {
			for (EntityInterface e : world.entitiesWithAttribute("networked")) {
				ranges.put(e, new LinkedList<Circle>());
				connections.put(e, new HashMap<EntityInterface, Connector>());
				NetworksAttribute attr = ((NetworksAttribute) e
						.getAttribute("networked"));
				for (int i : attr.getSupportedNetworks()) {
					NetworksAttribute.NetworkAttribute na = attr
							.getSupportedNetwork(i);
					if (na.range > 0) {
						Circle c = new Circle();
						c.setSize(na.range * 2);
						c.setColor(Color.LIGHT_GRAY);
						ranges.get(e).add(c);
						if (rangeButton.isSelected()) {
							world.addShape(c, false);
						}
					}
				}
			}
		}
	}

	private void updateRanges() {
		for (EntityInterface e : ranges.keySet()) {
			for (Circle c : ranges.get(e)) {
				double[] pos = e.getPosition();
				if (pos != null) {
					c.setPosition(pos[0], pos[1]);
				} else
					c.setPosition(-100, -100); // Trick move outside the display
												// :)
			}
		}
		if (worldView != null)
			worldView.repaint();
	}

	private void createButtons() {
		if (rangeButton == null) {
			rangeButton = new JToggleButton("Draw ranges");
			rangeButton.setToolTipText("Display ranges of the networks");
			rangeButton.setActionCommand("ranges");
			rangeButton.addActionListener(this);
			rangeButton.setSelected(false);
		}
		if (conButton == null) {
			conButton = new JToggleButton("Draw links");
			conButton.setToolTipText("Display network links");
			conButton.setActionCommand("links");
			conButton.addActionListener(this);
			conButton.setSelected(false);
		}
	}

	private void addButtons() {
		if (world != null) {
			if (world.getDisplay().getWorldView() != null
					&& world.getDisplay().getWorldView() != worldView) {
				worldView = world.getDisplay().getWorldView();
				createButtons();
				world.getDisplay().addWorldButton(rangeButton);
				world.getDisplay().addWorldButton(conButton);
			}
		}

	}

	public NetworkDisplay() {
		super();
		pixelPerTimeUnit = 100;
		distanceEntity = 100;
		marginHigh = 50;
		xover = yover = -OVER_DISTANCE - 1;
		this.parent = null;
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public void setWorld(World world, Network network) {
		this.world = world;
		if (this.network != null)
			this.network.removeListener(this);
		this.network = network;
		if (this.network != null) {
			this.network.addListener(this);
		}
		addButtons();
	}

	public double getPixelPerTimeUnit() {
		return pixelPerTimeUnit;
	}

	public void setPixelPerTimeUnit(double pixelPerTimeUnit) {
		this.pixelPerTimeUnit = pixelPerTimeUnit;
		repaint();
	}

	protected int distanceEntity;

	public int getDistanceEntity() {
		return distanceEntity;
	}

	public void setDistanceEntity(int distanceEntity) {
		this.distanceEntity = distanceEntity;
		repaint();
	}

	public void update(double t) {
		addButtons();
		updateRanges();
		for (DrawableLifeline df : lifelines.values()) {
			df.update(t);
		}
		repaint();
	}

	public void init() {
		addButtons();
		createRanges();
		lifelines.clear();
		msgMap.clear();
		messages.clear();
		int position = 0;
		if (world != null) {
			for (EntityInterface e : world.entitiesWithAttribute("networked")) {
				lifelines.put(e, new DrawableLifeline(e, position));
				position++;
				messages.put(e, new LinkedList<DrawableMessage>());
			}
		}
	}

	public synchronized JScrollPane getScrollPane() {
		return parent;
	}

	@Override
	public Dimension getPreferredSize() {
		double t = 0;
		if (world != null)
			t = world.getTime();
		double height = 4 * marginHigh + pixelPerTimeUnit * t;
		;
		double width = distanceEntity * (lifelines.size());
		return new Dimension(Util.toInteger(width), Util.toInteger(height));
	}

	@Override
	public void paint(Graphics g1) {
		super.paint(g1);
		Graphics2D g = (Graphics2D) g1;
		Color c = g.getColor();
		g.setColor(Color.WHITE);
		Rectangle b = g.getClipBounds();
		if (b != null) {
			g.fillRect(Util.toInteger(b.getX()), Util.toInteger(b.getY()),
					Util.toInteger(b.getWidth()), Util.toInteger(b.getHeight()));
		} else {
			g.setBackground(Color.WHITE);
		}
		g.setColor(c);
		String s = drawAllEntities(g) + drawAllMessages(g);
		paintOver(s, g);
	}

	private String drawAllMessages(Graphics2D g) {
		String s = "";
		if (world != null) {
			double t = world.getTime();
			for (EntityInterface e : messages.keySet()) {
				for (DrawableMessage m : messages.get(e)) {
					s += m.drawMessage(g, xover, yover, t, distanceEntity,
							lifelines, marginHigh, pixelPerTimeUnit);
				}
			}
		}
		return s;
	}

	private String drawAllEntities(Graphics2D g) {
		String s = "";
		for (EntityInterface e : lifelines.keySet()) {
			s += lifelines.get(e).draw(g, distanceEntity, marginHigh,
					distanceEntity / 3, pixelPerTimeUnit, xover, yover);
		}
		return s;
	}

	@Override
	public void repaint() {
		super.repaint();
		if (parent != null)
			parent.revalidate();
	}

	public synchronized void setScrollPane(JScrollPane parent) {
		this.parent = parent;
	}

	static private List<String[]> tableDecomposition(String s) {
		LinkedList<String[]> ll = new LinkedList<String[]>();
		String[] r = s.split("\n");
		for (int i = 0; i < r.length; i++)
			ll.add(r[i].split("\t"));
		return ll;
	}

	static private double[] tableWidth(List<String[]> r, FontMetrics fm) {
		double wspace = fm.stringWidth(" ");
		int nbResults = 1;
		double[] res;
		for (String[] e : r) {
			nbResults = Math.max(nbResults, e.length);
		}
		res = new double[nbResults + 1];
		for (String[] e : r) {
			double cSpace = wspace * (e.length - 1);
			for (int i = 0; i < e.length; i++) {
				double lw = fm.stringWidth(e[i]);
				res[i + 1] = Math.max(res[i + 1], lw);
				cSpace += lw;
			}
			res[0] = Math.max(res[0], cSpace);
		}
		double w = 0;
		for (int i = 1; i < res.length; i++) {
			w += res[i];
		}
		w += (res.length - 2) * wspace;
		res[0] = Math.max(res[0], w);
		return res;
	}

	private void paintOver(String s, Graphics2D g) {
		if (s.length() == 0)
			return;
		double y = yover + 20;
		double x = xover;
		List<String[]> r = tableDecomposition(s);
		double wspace = g.getFontMetrics().stringWidth(" ");
		double hspace = g.getFontMetrics().getHeight();
		double[] ld = tableWidth(r, g.getFontMetrics());
		double w = ld[0];
		double h = (r.size() + 0.5) * hspace;
		if (x < w / 2 + wspace)
			x = w / 2 + wspace;
		g.setColor(Color.DARK_GRAY);
		g.drawRect((int) (x - w / 2 - wspace), (int) y, (int) (w + 2 * wspace),
				(int) h);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect((int) (x - w / 2 - wspace), (int) y, (int) (w + 2 * wspace),
				(int) h);
		g.setColor(Color.BLACK);
		for (String[] t : r) {
			y += hspace;
			if (t.length > 1) {
				double xinit = x - w / 2;
				for (int i = 0; i < t.length; i++) {
					g.drawString(t[i], (float) xinit, (float) y);
					if (i < t.length - 1) {
						xinit += ld[i + 1] + wspace;
					}
				}
			} else {
				g.drawString(t[0],
						(float) (x - g.getFontMetrics().stringWidth(t[0]) / 2),
						(float) y);
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		xover = e.getX();
		yover = e.getY();
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		xover = -OVER_DISTANCE - 1;
		yover = -OVER_DISTANCE - 1;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void arrived(ScheduledMessage msg) {
		msgMap.remove(msg);
	}

	public void dropped(double t, ScheduledMessage msg) {
		if (msgMap.containsKey(msg))
			msgMap.remove(msg).drop(t);
	}

	public void scheduled(ScheduledMessage msg) {
		if (messages.containsKey(msg.getRecipient())) {
			msgMap.put(msg, new DrawableMessage(msg));
			messages.get(msg.getRecipient()).add(msgMap.get(msg));
		}
	}

	public void inComRange(double t, int network, EntityInterface e1,
			EntityInterface e2) {
		if (!connections.get(e1).containsKey(e2)
				&& !connections.get(e2).containsKey(e1)) {
			connections.get(e1).put(e2, createConnector(e1, e2));
			if (conButton.isSelected()) {
				world.addShape(connections.get(e1).get(e2), false);
			}
		}
	}

	private Connector createConnector(EntityInterface e1, EntityInterface e2) {
		Connector c = null;
		if (freeConnectors.isEmpty()) {
			c = new Connector(e1.getDrawingShape(), e2.getDrawingShape());
			c.setColor(Color.BLACK);
			c.setArrow(false);
		} else {
			c = freeConnectors.pop();
			c.setSource(e1.getDrawingShape());
			c.setDestination(e2.getDrawingShape());
		}
		return c;
	}

	public void noMoreInComRange(double t, int network, EntityInterface e1,
			EntityInterface e2) {
		if (!this.network.isInComRange(e1, e2)) {
			if (connections.get(e1).containsKey(e2)) {
				Connector c = connections.get(e1).get(e2);
				if (conButton.isSelected())
					world.removeShape(c);
				connections.get(e1).remove(e2);
				if (!freeConnectors.contains(c))
					freeConnectors.push(c);
			}
			if (connections.get(e2).containsKey(e1)) {
				Connector c = connections.get(e2).get(e1);
				if (conButton.isSelected())
					world.removeShape(c);
				connections.get(e2).remove(e1);
				if (!freeConnectors.contains(c))
					freeConnectors.push(c);
			}
		}
	}

	public WorldBehaviour copy() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ranges")) {
			boolean add = rangeButton.isSelected();
			for (EntityInterface ent : ranges.keySet()) {
				for (Circle c : ranges.get(ent)) {
					if (add)
						world.addShape(c, false);
					else
						world.removeShape(c);
				}
			}
		} else if (e.getActionCommand().equals("links")) {
			boolean add = conButton.isSelected();
			for (EntityInterface ent : connections.keySet()) {
				for (Connector c : connections.get(ent).values()) {
					if (add)
						world.addShape(c, false);
					else
						world.removeShape(c);
				}
			}

		}

	}

}
