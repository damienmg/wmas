package wmas.behaviour.physical;

import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.behaviour.AbstractBehaviour;
import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.behaviour.simple.SimpleBehaviourData;
import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.geometry.Path;
import wmas.geometry.Point;
import wmas.geometry.Poly;
import wmas.geometry.PolyDefault;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.elements.DrawablePath;
import wmas.gui.shapes.elements.Polygon;
import wmas.util.Doublet;
import wmas.util.Util;
import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.memory.ArrayData;
import wmas.world.memory.DoubleData;
import wmas.world.memory.ExpressionData;
import wmas.world.memory.LongData;
import wmas.world.memory.StringData;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class MoveBehaviour extends AbstractBehaviour {

	public static final int MOVE_TYPE_ABSOLUTE = 0;
	public static final int MOVE_TYPE_RELATIVE = 1;
	public static final int MOVE_TYPE_PATH = 2;
	public static final int MOVE_TYPE_COVER = 3;

	// Speed
	protected Expression speed;
	// Destination
	protected Expression x;
	protected Expression y;
	protected Path path;
	protected DrawableShape toCover;
	protected int type;

	// Internal
	private double lasttime;
	private boolean hasTerminated;
	private Path realPath = null;
	private DrawablePath realPathDraw = null;
	private Color color = null;
	private EntityInterface entity;
	private Variables varSet;
	private boolean suspended = false;

	// Coloring while moving
	protected Polygon coloring;

	public MoveBehaviour() {
		x = new Expression(0);
		y = new Expression(0);
		speed = new Expression(100000); // will set the default speed to the
										// maximum speed
		lasttime = 0;
		toCover = null;
		path = null;
		hasTerminated = false;
		coloring = new Polygon(new PolyDefault());
		coloring.setColor(null);
		coloring.setFillColor(null);
	}

	public String toString() {
		switch (type) {
		case MOVE_TYPE_ABSOLUTE:
			return "Move(x = " + x + ", y = " + y + ", speed = "
					+ (speed == null ? "∞" : speed) + ")";
		case MOVE_TYPE_RELATIVE:
			return "Move(dx = " + x + ", dy = " + y + ", speed = "
					+ (speed == null ? "∞" : speed) + ")";
		case MOVE_TYPE_COVER:
			return "Move(shape = "
					+ (toCover == null ? "null" : toCover.toString())
					+ ", speed = " + (speed == null ? "∞" : speed) + ")";
		case MOVE_TYPE_PATH:
			return "Move(path = " + (path == null ? "null" : path.toString())
					+ ", speed = " + (speed == null ? "∞" : speed) + ")";
		}
		return "Move(???, " + (speed == null ? "∞" : speed) + ")";
	}

	public void setColoring(Color c) {
		coloring.setFillColor(c);
		coloring.getPoly().clear();
	}

	public Color getColoring() {
		if (coloring == null)
			return null;
		return coloring.getFillColor();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
		type = MOVE_TYPE_PATH;
	}

	public Expression getSpeed() {
		return speed;
	}

	public void setSpeed(Expression speed) {
		this.speed = speed;
	}

	public Expression[] getPosition() {
		return new Expression[] { x, y };
	}

	public void setMoveTo(Expression x, Expression y) {
		this.x = x;
		this.y = y;
		type = MOVE_TYPE_ABSOLUTE;
	}

	public int getType() {
		return type;
	}

	public void setMoveRelative(Expression dx, Expression dy) {
		this.x = dx;
		this.y = dy;
		this.type = MOVE_TYPE_RELATIVE;
	}

	public void setCoverShape(DrawableShape shape) {
		this.toCover = shape;
		this.type = MOVE_TYPE_COVER;
	}

	public DrawableShape getCoverShape() {
		return this.toCover;
	}

	public boolean terminated() {
		return hasTerminated;
	}

	private void updateDisplay() {
		if (entity != null) {
			World world = entity.getWorld();
			if (world != null) {
				if (coloring != null) {
					world.addShape(coloring, false);
				}
				if (realPathDraw != null) {
					if (hasTerminated) {
						world.removeShape(realPathDraw);
					} else {
						world.addShape(realPathDraw, true);
					}
				}
			}
		}
	}

	@Override
	public void colorize(Color c) {
		this.color = c;
		if (realPathDraw != null) {
			realPathDraw.setColor(this.color);
		}
	}

	@Override
	public Behaviour copy() {
		MoveBehaviour r = new MoveBehaviour();
		r.coloring.setFillColor(coloring.getFillColor());
		if (path != null)
			r.path = new Path(path);
		r.x = x;
		r.y = y;
		r.speed = speed;
		r.type = type;
		if (toCover != null) {
			r.toCover = toCover.copyShape();
		}
		return r;
	}

	private double determineSpeed() {
		if (speed == null)
			return -1;
		if (!entity.hasAttribute("speed"))
			return 0;
		SpeedAttribute sA = (SpeedAttribute) entity.getAttribute("speed");
		double s = speed.getNumber(entity, varSet).doubleValue();
		if (s <= 0)
			return 0;
		return Math.min(s, Math.max(sA.getSpeed(this.coloring.getColor()), 0));
	}

	private void constructPath() {
		realPath = null;
		double[] pos = entity.getPosition();
		if (pos == null)
			return;

		switch (type) {
		case MOVE_TYPE_RELATIVE:
			realPath = new Path();
			realPath.add(pos[0], pos[1]);
			realPath.add(pos[0] + x.getNumber(entity, varSet).doubleValue(),
					pos[1] + y.getNumber(entity, varSet).doubleValue());
			break;
		case MOVE_TYPE_ABSOLUTE:
			realPath = new Path();
			realPath.add(pos[0], pos[1]);
			realPath.add(x.getNumber(entity, varSet).doubleValue(), y
					.getNumber(entity, varSet).doubleValue());
			break;
		case MOVE_TYPE_COVER:
			if (toCover == null)
				realPath = new Path();
			else
				realPath = Path.cover(entity.getPosition(), toCover.getPoly(),
						entity.getDrawingShape());
			realPath.translateOriginTo(pos[0], pos[1]);
			break;
		case MOVE_TYPE_PATH:
			realPath = new Path(path);
			realPath.translateOriginTo(pos[0], pos[1]);
			break;
		}
		constructRealPathDraw();
	}

	private void constructRealPathDraw() {
		if (realPath != null) {
			if (realPathDraw != null) {
				realPathDraw.setPath(realPath);
			} else {
				realPathDraw = new DrawablePath(realPath);
			}
			if (color != null)
				realPathDraw.setColor(color);
		} else {
			if (realPathDraw != null) {
				if (entity != null && entity.getWorld() != null)
					entity.getWorld().removeShape(realPathDraw);
				realPathDraw = null;
			}
		}
	}

	@Override
	public void init(EntityInterface object, Variables varSet, double t) {
		lasttime = t;
		entity = object;
		this.varSet = varSet;
		constructPath();
		double s = determineSpeed();
		suspended = false;
		if (realPath == null || s == 0 || realPath.isEmpty()) {
			hasTerminated = true;
			updateDisplay();
		} else if (s > 0) {
			Point p = realPath.initial();
			object.setPosition(p.x, p.y);
			hasTerminated = false;
			updateDisplay();
		} else {
			Point p = realPath.initial();
			object.setPosition(p.x, p.y);
			hasTerminated = false;
			update(t);
		}
	}

	@Override
	public void suspend(double t) {
		update(t);
		suspended = true;
	}

	@Override
	public void unsuspend(double t) {
		if (suspended) {
			lasttime = t;
			suspended = false;
			update(t);
		}
	}

	private double determineSize() {
		if (entity.getDrawingShape() == null)
			return 0;
		double[] s = entity.getDrawingShape().getBounds();
		return Math.max(s[2], s[3]);
	}

	private void reportCovering(Poly p, double t) {
		if (entity != null && entity.getWorld() != null && coloring != null
				&& coloring.getFillColor() != null) {
			Color c = coloring.getFillColor();
			entity.getWorld().addReport(t, "color", Util.colorToString(c),
					p.getArea());
			entity.getWorld().addReport(t, "color", "", p.getArea());
		}
	}

	@Override
	public void update(double t) {
		double dt = t - lasttime;
		lasttime = t;
		if (entity == null)
			return;
		double s = determineSpeed();
		double size = determineSize(); // SPEED: use the radius of the shape
										// instead of the real shape
		if (!hasTerminated && (s != 0)) {
			Point orig = new Point(entity.getPosition());
			Doublet<Point, Poly> next = realPath.getNext(orig,
					s < 0 ? Double.MAX_VALUE : (s * dt), size);
			if (next.getSecond() != null) {
				if (hasCollision(t, next.getSecond())) { // Stop the movement on
															// collision
					updateLifting(t, null, 0, 0);
					hasTerminated = true;
					return;
				}
				reportCovering(next.getSecond(), t);
				coloring.setPoly(coloring.getPoly().union(next.getSecond()));
			}
			updateLifting(t, next.getSecond(), orig.x - next.getFirst().x,
					orig.y - next.getFirst().y);
			entity.setPosition(next.getFirst().x, next.getFirst().y);
			if (next.getFirst().equals(realPath.getLast()))
				hasTerminated = true;
			updateDisplay();
		}
	}

	private void updateLifting(double t, Poly p, double dx, double dy) {
		if (entity.hasAttribute("carrier") && entity.getWorld() != null) {
			LiftedAttribute pca = null;
			if (entity.getWorld().hasStaticAttribute("lifting")) {
				pca = ((LiftedAttribute) entity.getWorld().getStaticAttribute(
						"lifting"));
				pca.initCollision(t);
				if (p == null) {
					p = entity.getShape();
				} else if (entity.getDrawingShape() != null) {
					p = p.union(entity.getShape());
				}
				if (p != null) {
					for (EntityInterface e : entity.getWorld()
							.entitiesWithAttribute("liftable")) {
						if (e.getShape() != null
								&& e.getDrawingShape() != entity
										.getDrawingShape()) {
							if (!(e.getShape().intersection(p).isEmpty())) {
								if (pca != null) {
									pca.addCollision(t, entity, e);
									if (e.hasAttribute("carrier")
											&& entity.hasAttribute("liftable")) {
										pca.addCollision(t, e, entity);
									}
								}
							}
						}
					}
				}
				if (dx != 0 || dy != 0)
					pca.setNewPosition(entity, dx, dy);
			}
		}
	}

	private boolean hasCollision(double t, Poly p) {
		if (entity.hasAttribute("collide") && entity.getWorld() != null) {
			PhysicalCollisionAttribute pca = null;
			if (entity.getWorld().hasStaticAttribute("collision")) {
				pca = ((PhysicalCollisionAttribute) entity.getWorld()
						.getStaticAttribute("collision"));
				pca.initCollision(t);
			}
			boolean collision = false;
			for (EntityInterface e : entity.getWorld().entitiesWithAttribute(
					"collision")) {
				if (e.getShape() != null
						&& e.getDrawingShape() != entity.getDrawingShape()) {
					if (e.getShape().intersection(p).getArea() > 0) {
						collision = true;
						if (pca != null) {
							pca.addCollision(t, entity, e);
							if (e.hasAttribute("collide")
									&& entity.hasAttribute("collision")) {
								pca.addCollision(t, e, entity);
							}
						} else
							return true;
					}
				}
			}
			return collision;
		}
		return false;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		coloring.setFillColor(Util.colorFromString(e.getAttribute("coloring")));
		speed = e.hasAttribute("speed") ? new Expression(
				e.getAttribute("speed")) : null;
		if (e.hasAttribute("x") && e.hasAttribute("y")) {
			x = new Expression(e.getAttribute("x"));
			y = new Expression(e.getAttribute("y"));
			type = MOVE_TYPE_ABSOLUTE;
		} else if (e.hasAttribute("dx") && e.hasAttribute("dy")) {
			x = new Expression(e.getAttribute("dx"));
			y = new Expression(e.getAttribute("dy"));
			type = MOVE_TYPE_RELATIVE;
		} else {
			NodeList nl = e.getElementsByTagName("*");
			for (int i = 0; (i < nl.getLength()) && (path == null); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					XMLEntity xe = XMLInterpretor.convert((Element) n, refs);
					if (xe instanceof Path) {
						path = (Path) xe;
						type = MOVE_TYPE_PATH;
					} else if (xe instanceof DrawableShape) {
						toCover = (DrawableShape) xe;
						type = MOVE_TYPE_COVER;
					}
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("coloring", Util.colorToString(coloring.getFillColor()));
		if (speed != null)
			e.setAttribute("speed", speed.toString());
		switch (type) {
		case MOVE_TYPE_ABSOLUTE:
			e.setAttribute("x", x.toString());
			e.setAttribute("y", y.toString());
			break;
		case MOVE_TYPE_RELATIVE:
			e.setAttribute("dx", x.toString());
			e.setAttribute("dy", y.toString());
			break;
		case MOVE_TYPE_PATH:
			e.appendChild(path.toXML(root, refs));
			break;
		case MOVE_TYPE_COVER:
			e.appendChild(toCover.toXML(root, refs));
			break;
		}
		return e;
	}

	public void reset() {
		if (this.coloring != null)
			this.coloring.getPoly().clear();
	}

	public Expression getX() {
		return x;
	}

	public void setX(Expression x) {
		this.x = x;
	}

	public Expression getY() {
		return y;
	}

	public void setY(Expression y) {
		this.y = y;
	}

	public DrawableShape getToCover() {
		return toCover;
	}

	public void setToCover(DrawableShape toCover) {
		this.toCover = toCover;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setColoring(Polygon coloring) {
		this.coloring = coloring;
	}

	@Override
	public void terminate() {
		hasTerminated = true;
		updateDisplay();
		colorize(null);
	}

	private ExpressionData speedData = new ExpressionData(speed);
	private LongData typeData = new LongData();
	private ExpressionData xData = new ExpressionData(x);
	private ExpressionData yData = new ExpressionData(y);
	private ArrayData pathData = new ArrayData();
	private StringData colorData = new StringData();
	private SimpleBehaviourData<ArrayData> sbd = new SimpleBehaviourData<ArrayData>(
			this, new ArrayData());

	private void constructPathData() {
		pathData.clear();
		if (path == null) {
			return;
		}
		double[] r = path.toDoubleArray();
		int i = 0;
		for (double d : r) {
			DoubleData dd = new DoubleData();
			dd.setData(d);
			pathData.setSubData(i++, dd);
		}
	}

	private void constructCoverData() {
		pathData.clear();
		if (toCover == null || toCover.getPoly() == null) {
			return;
		}
		int i = 0;
		for (Point p : toCover.getPoly().getAllPoints()) {
			DoubleData dd = new DoubleData();
			dd.setData(p.x);
			pathData.setSubData(i++, dd);
			dd = new DoubleData();
			dd.setData(p.y);
			pathData.setSubData(i++, dd);
		}
	}

	@Override
	public BehaviourData getRepresentation() {
		sbd.getData().clear();
		speedData.setData(speed);
		typeData.setData(type);
		colorData.setData(coloring == null ? "" : Util.colorToString(coloring
				.getFillColor()));
		sbd.getData().setData(0, speedData);
		sbd.getData().setData(1, typeData);
		sbd.getData().setData(2, colorData);
		switch (type) {
		case MOVE_TYPE_ABSOLUTE:
		case MOVE_TYPE_RELATIVE:
			xData.setData(x);
			yData.setData(y);
			sbd.getData().setData(3, xData);
			sbd.getData().setData(4, yData);
			break;
		case MOVE_TYPE_PATH:
			constructPathData();
			sbd.getData().setData(3, pathData);
			break;
		case MOVE_TYPE_COVER:
			constructCoverData();
			sbd.getData().setData(3, pathData);
			break;
		}

		return sbd;
	}

}
