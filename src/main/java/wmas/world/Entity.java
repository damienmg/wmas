package wmas.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.geometry.Poly;
import wmas.gui.shapes.DrawableShape;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class Entity implements XMLEntity, EntityInterface {
	private static Variables varSet = new Variables();

	private World world = null;
	private Map<String, XMLEntity> attributes = new HashMap<String, XMLEntity>();
	private String name = "";
	private DrawableShape shape = null;
	private Color colorized = null;
	private List<Behaviour> behaviours = new ArrayList<Behaviour>();
	private Collection<Behaviour> runningBehaviours = new LinkedList<Behaviour>();
	private Collection<Behaviour> terminatedBehaviours = new LinkedList<Behaviour>();
	private XMLEntity parent = null;

	private Expression activeExpr = new Expression(1);
	private Expression dynWidth = null;
	private Expression dynHeight = null;
	private Expression dynX = null;
	private Expression dynY = null;

	private Entity aCopy = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#addAttribute(java.lang.String,
	 * laas.botsimu.xml.XMLEntity)
	 */
	public void addAttribute(String name, XMLEntity attribute) {
		this.attributes.put(name, attribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String name) {
		return this.attributes.containsKey(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getAttribute(java.lang.String)
	 */
	public XMLEntity getAttribute(String name) {
		return this.attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getShape()
	 */
	public Poly getShape() {
		return shape == null ? null : shape.getPoly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getDrawingShape()
	 */
	public DrawableShape getDrawingShape() {
		return shape;
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		name = e.getAttribute("name");
		if (e.hasAttribute("active")) {
			activeExpr = new Expression(e.getAttribute("active"));
		}
		String sh = e.getAttribute("shape");
		shape = null;
		if (sh != null && sh.length() > 0) {
			shape = (DrawableShape) Class.forName(sh).newInstance();
			shape.setSize(Double.parseDouble(e.getAttribute("width")),
					Double.parseDouble(e.getAttribute("height")));
			shape.setPosition(Double.parseDouble(e.getAttribute("x")),
					Double.parseDouble(e.getAttribute("y")));
			shape.setColor(Color.decode(e.getAttribute("color")));
			if (e.hasAttribute("fill"))
				shape.setFillColor(Color.decode(e.getAttribute("fill")));

			dynX = (e.hasAttribute("dyn_x") ? new Expression(
					e.getAttribute("dyn_x")) : null);
			dynY = (e.hasAttribute("dyn_y") ? new Expression(
					e.getAttribute("dyn_y")) : null);
			dynWidth = (e.hasAttribute("dyn_width") ? new Expression(
					e.getAttribute("dyn_width")) : null);
			dynHeight = (e.hasAttribute("dyn_height") ? new Expression(
					e.getAttribute("dyn_height")) : null);
		}
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("attribute")) {
					String attrName = el.getAttribute("name");
					XMLEntity c = null;
					for (int j = 0; j < el.getChildNodes().getLength()
							&& c == null; j++) {
						Node n1 = el.getChildNodes().item(j);
						if (n1.getNodeType() == Node.ELEMENT_NODE) {
							c = XMLInterpretor.convert((Element) n1, refs);
						}
					}
					attributes.put(attrName, c);
				} else if (el.getTagName().equals("behaviour")) {
					Object o = refs.getObject(el.getAttribute("xref"));
					if (o instanceof Behaviour)
						behaviours.add((Behaviour) o);
				} else {
					XMLEntity o = XMLInterpretor.convert(el, refs);
					if (o != null) {
						if (o instanceof Behaviour) {
							behaviours.add((Behaviour) o);
						}
					}
				}
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("active", activeExpr.toString());
		e.setAttribute("name", name);
		if (shape != null) {
			e.setAttribute("shape", shape.getClass().getName());
			double[] pos = shape.getBounds();
			e.setAttribute("x", Double.toString(pos[0] + pos[2] / 2));
			e.setAttribute("y", Double.toString(pos[1] + pos[3] / 2));
			e.setAttribute("width", Double.toString(pos[2]));
			e.setAttribute("height", Double.toString(pos[3]));
			e.setAttribute("color", Util.colorToString(shape.getColor()));

			if (dynWidth != null)
				e.setAttribute("dyn_width", dynWidth.toString());
			if (dynHeight != null)
				e.setAttribute("dyn_height", dynHeight.toString());
			if (dynX != null)
				e.setAttribute("dyn_x", dynX.toString());
			if (dynY != null)
				e.setAttribute("dyn_y", dynY.toString());

			if (shape.hasFillColor() && shape.getFillColor() != null) {
				e.setAttribute("fill", Util.colorToString(shape.getFillColor()));
			}
		}
		for (Behaviour b : behaviours) {
			e.appendChild(XMLInterpretor.makeCrossReferencedElement(root, b,
					refs));
		}
		for (Map.Entry<String, XMLEntity> me : attributes.entrySet()) {
			Element attr = root.createElement("attribute");
			attr.setAttribute("name", me.getKey());
			if (me.getValue() != null)
				attr.appendChild(me.getValue().toXML(root, refs));
			e.appendChild(attr);
		}
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#reset()
	 */
	public void reset() {
		if (aCopy != null) {
			attributes.clear();
			behaviours.clear();
			for (Map.Entry<String, XMLEntity> attr : aCopy.attributes
					.entrySet())
				attributes.put(attr.getKey(), attr.getValue() == null ? null
						: (XMLEntity) attr.getValue().copy());
			name = aCopy.name;
			shape = aCopy.shape != null ? aCopy.shape.copyShape() : null;
			for (Behaviour b : aCopy.behaviours)
				behaviours.add(b.copy());
		}
		for (Behaviour b : terminatedBehaviours) {
			b.reset();
		}
		for (Behaviour b : runningBehaviours) {
			b.reset();
		}
		terminatedBehaviours.clear();
		runningBehaviours.clear();
	}

	public Entity copy() {
		Entity e = new Entity();
		for (Map.Entry<String, XMLEntity> attr : attributes.entrySet())
			e.attributes.put(attr.getKey(), attr.getValue() == null ? null
					: (XMLEntity) attr.getValue().copy());
		e.name = name;
		e.shape = shape == null ? null : shape.copyShape();
		e.activeExpr = new Expression(activeExpr);
		for (Behaviour b : behaviours)
			e.behaviours.add(b.copy());
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#copy(java.util.Map)
	 */
	public Entity copy(Map<Behaviour, Behaviour> bMap) {
		Entity e = new Entity();
		for (Map.Entry<String, XMLEntity> attr : attributes.entrySet())
			e.attributes.put(attr.getKey(), attr.getValue() == null ? null
					: (XMLEntity) attr.getValue().copy());
		e.name = name;
		e.shape = shape == null ? null : shape.copyShape();
		e.activeExpr = new Expression(activeExpr);
		for (Behaviour b : behaviours)
			e.behaviours.add(bMap.get(b));
		return e;
	}

	// Execution part
	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#init()
	 */
	public void init(EntityInterface metaEntity) {
		if (aCopy == null)
			aCopy = (Entity) copy();
		if (this.shape != null) {
			varSet.clear();
			if (dynWidth != null && dynHeight != null) {
				shape.setSize(dynWidth.getNumber(this, varSet).doubleValue(),
						dynHeight.getNumber(this, varSet).doubleValue());
			}
			if (dynX != null && dynY != null) {
				shape.setPosition(dynX.getNumber(this, varSet).doubleValue(),
						dynY.getNumber(this, varSet).doubleValue());
			}
			this.shape.colorize(this.colorized);
		}
		runningBehaviours.clear();
		terminatedBehaviours.clear();
		for (Behaviour b : behaviours) {
			b.init(metaEntity, new Variables(), 0);
			runningBehaviours.add(b);
			b.colorize(this.colorized);
		}
	}

	public void init() {
		init(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#update(double)
	 */
	public void update(double t) {
		runningBehaviours.removeAll(terminatedBehaviours);
		for (Behaviour b : runningBehaviours) {
			b.update(t);
		}
	}

	private double lastTime;
	private Set<Behaviour> stepFinished = new HashSet<Behaviour>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#initStep()
	 */
	public void initStep() {
		initStep(this);
	}

	public void initStep(EntityInterface metaEntity) {
		lastTime = 0;
		stepFinished.clear();
		if (aCopy == null)
			aCopy = (Entity) copy();
		if (this.shape != null) {
			this.shape.colorize(this.colorized);
		}
		runningBehaviours.clear();
		terminatedBehaviours.clear();
		for (Behaviour b : behaviours) {
			b.initStep(this, new Variables(), 0);
			runningBehaviours.add(b);
			b.colorize(this.colorized);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#updateStep(double)
	 */
	public boolean updateStep(double t) {
		if (t != lastTime) {
			runningBehaviours.removeAll(terminatedBehaviours);
			lastTime = t;
			stepFinished.clear();
		}
		for (Behaviour b : runningBehaviours) {
			if (!stepFinished.contains(b)) {
				if (b.updateStep(t)) {
					stepFinished.add(b);
				}
			}
		}
		return stepFinished.size() == runningBehaviours.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#terminate()
	 */
	public void terminate() {
		for (Behaviour b : runningBehaviours) {
			b.terminate();
		}
		runningBehaviours.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#terminated()
	 */
	public boolean terminated() {
		for (Behaviour b : runningBehaviours) {
			if (!b.terminated()) {
				return false;
			} else {
				terminatedBehaviours.add(b);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laas.botsimu.world.EntityInterface#setDrawingShape(laas.botsimu.gui.shapes
	 * .DrawableShape)
	 */
	public void setDrawingShape(DrawableShape shape) {
		this.shape = shape;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#setName(java.lang.String)
	 */
	public void setName(String text) {
		this.name = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#listAttributes()
	 */
	public Collection<String> listAttributes() {
		return attributes.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getBehaviours()
	 */
	public List<Behaviour> getBehaviours() {
		return behaviours;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laas.botsimu.world.EntityInterface#addBehaviour(laas.botsimu.behaviour
	 * .Behaviour)
	 */
	public boolean addBehaviour(Behaviour o) {
		return behaviours.add(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laas.botsimu.world.EntityInterface#removeBehaviour(laas.botsimu.behaviour
	 * .Behaviour)
	 */
	public boolean removeBehaviour(Behaviour o) {
		return behaviours.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#colorize(java.awt.Color)
	 */
	public void colorize(Color c) {
		this.colorized = c;
		for (Behaviour b : runningBehaviours) {
			b.colorize(c);
		}
		if (this.shape != null)
			this.shape.colorize(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getColorization()
	 */
	public Color getColorization() {
		return this.colorized;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getWorld()
	 */
	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#setPosition(double, double)
	 */
	public void setPosition(double x, double y) {
		if (shape != null)
			shape.setPosition(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getPosition()
	 */
	public double[] getPosition() {
		if (shape != null)
			return shape.getPosition();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see laas.botsimu.world.EntityInterface#getParent()
	 */
	public XMLEntity getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * laas.botsimu.world.EntityInterface#setParent(laas.botsimu.xml.XMLEntity)
	 */
	public void setParent(XMLEntity parent) {
		this.parent = parent;
	}

	@Override
	public Expression getEnabledExpression() {
		return activeExpr;
	}

	@Override
	public void setEnabledExpression(Expression expr) {
		this.activeExpr = expr;
	}

	@Override
	public Expression getDynamicHeight() {
		return dynHeight;
	}

	@Override
	public Expression getDynamicWidth() {
		return dynWidth;
	}

	@Override
	public Expression getDynamicX() {
		return dynX;
	}

	@Override
	public Expression getDynamicY() {
		return dynY;
	}

	@Override
	public void setDynamicPosition(Expression x, Expression y) {
		this.dynX = x;
		this.dynY = y;
	}

	@Override
	public void setDynamicPosition() {
		this.dynX = null;
		this.dynY = null;

	}

	@Override
	public void setDynamicSize(Expression width, Expression height) {
		this.dynWidth = width;
		this.dynHeight = height;
	}

	@Override
	public void setDynamicSize(Expression size) {
		this.dynWidth = this.dynHeight = size;

	}

	@Override
	public void setDynamicSize() {
		this.dynWidth = this.dynHeight = null;
	}

	@Override
	public void setDynamicHeight(Expression height) {
		this.dynHeight = height;
	}

	@Override
	public void setDynamicWidth(Expression width) {
		this.dynWidth = width;
	}

	@Override
	public void setDynamicX(Expression x) {
		this.dynX = x;
	}

	@Override
	public void setDynamicY(Expression y) {
		this.dynY = y;
	}
}
