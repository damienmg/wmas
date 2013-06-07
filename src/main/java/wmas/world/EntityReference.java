package wmas.world;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.expression.Expression;
import wmas.geometry.Poly;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.ShapeListener;
import wmas.util.Util;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class EntityReference implements EntityInterface, ShapeListener {
	private EntityInterface reference = null;
	private EntityInterface copy = null;

	private Expression activeExpr = new Expression(1);
	private Expression dynWidth = null;
	private Expression dynHeight = null;
	private Expression dynX = null;
	private Expression dynY = null;

	// Specific parameters
	private String name = "";
	private XMLEntity parent = null;
	private DrawableShape myShape = null;
	private DrawableShape prevShape = null;

	private void createCopy() {
		if (copy == null && reference != null) {
			copy = reference.copy();
			if (copy != null) {
				copy.setDynamicPosition(dynX, dynY);
				copy.setDynamicSize(dynWidth, dynHeight);
				copy.setDrawingShape(myShape == null ? null : myShape
						.copyShape());
				copy.setName(name);
			}
		}
	}

	public EntityReference(EntityInterface reference) {
		this.reference = null;
		this.setReference(reference);
		if (reference != null) {
			this.name = reference.getName();
			this.myShape = prevShape != null ? prevShape.copyShape() : null;
		}
	}

	public EntityReference() {
	}

	@Override
	public void addAttribute(String name, XMLEntity attribute) {
		if (reference != null)
			reference.addAttribute(name, attribute);
	}

	@Override
	public boolean addBehaviour(Behaviour o) {
		if (reference != null)
			return reference.addBehaviour(o);
		return false;
	}

	@Override
	public void colorize(Color c) {
		if (copy != null)
			copy.colorize(c);
	}

	@Override
	public EntityInterface copy() {
		EntityReference r = new EntityReference(reference);
		r.activeExpr = new Expression(activeExpr);
		return r;
	}

	@Override
	public EntityInterface copy(Map<Behaviour, Behaviour> bMap) {
		EntityReference r = new EntityReference(reference);
		r.activeExpr = new Expression(activeExpr);
		return r;
	}

	@Override
	public XMLEntity getAttribute(String name) {
		if (copy != null)
			return copy.getAttribute(name);
		if (reference != null)
			return reference.getAttribute(name);
		return null;
	}

	@Override
	public List<Behaviour> getBehaviours() {
		if (copy != null)
			return copy.getBehaviours();
		if (reference != null)
			return reference.getBehaviours();
		return null;
	}

	@Override
	public Color getColorization() {
		if (copy != null)
			return copy.getColorization();
		return null;
	}

	@Override
	public DrawableShape getDrawingShape() {
		if (copy != null)
			return copy.getDrawingShape();
		if (reference != null && reference.getDrawingShape() != prevShape) {
			if (prevShape != null)
				prevShape.removeListener(this);
			DrawableShape s = reference.getDrawingShape();
			if (s != null) {
				double[] p = s.getPosition();
				double[] b = s.getBounds();
				if (myShape != null) {
					p = myShape.getPosition();
					b = myShape.getBounds();
				}
				myShape = s.copyShape();
				myShape.setPosition(p[0], p[1]);
				myShape.setSize(b[2], b[3]);
			} else
				myShape = null;
		}
		return myShape;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public XMLEntity getParent() {
		return parent;
	}

	@Override
	public double[] getPosition() {
		if (copy != null)
			return copy.getPosition();
		return myShape == null ? null : myShape.getPosition();
	}

	@Override
	public Poly getShape() {
		DrawableShape ds = getDrawingShape();
		if (ds != null)
			return ds.getPoly();
		return null;
	}

	@Override
	public World getWorld() {
		if (copy != null)
			return copy.getWorld();
		if (reference != null)
			return reference.getWorld();
		return null;
	}

	@Override
	public boolean hasAttribute(String name) {
		if (copy != null)
			return copy.hasAttribute(name);
		if (reference != null)
			return reference.hasAttribute(name);
		return false;
	}

	@Override
	public void init() {
		createCopy();
		if (copy != null)
			copy.init(this);
	}

	@Override
	public void initStep() {
		createCopy();
		if (copy != null)
			copy.initStep(this);
	}

	@Override
	public void init(EntityInterface metaEntity) {
		createCopy();
		if (copy != null)
			copy.init(metaEntity);
	}

	@Override
	public void initStep(EntityInterface metaEntity) {
		createCopy();
		if (copy != null)
			copy.initStep(metaEntity);
	}

	@Override
	public Collection<String> listAttributes() {
		if (copy != null)
			return copy.listAttributes();
		if (reference != null)
			return reference.listAttributes();
		return null;
	}

	@Override
	public void removeAttribute(String name) {
		if (copy != null)
			copy.removeAttribute(name);
		if (reference != null)
			reference.removeAttribute(name);
	}

	@Override
	public boolean removeBehaviour(Behaviour o) {
		if (reference != null)
			return reference.removeBehaviour(o);
		return false;
	}

	@Override
	public void reset() {
		if (copy != null) {
			copy.reset();
			copy.setDrawingShape(myShape != null ? myShape.copyShape() : null);
			copy.setName(name);
		}
	}

	@Override
	public void setDrawingShape(DrawableShape shape) {
		if (copy != null)
			copy.setDrawingShape(shape);
		else {
			myShape = shape;
		}

	}

	@Override
	public void setName(String text) {
		this.name = text;
	}

	@Override
	public void setParent(XMLEntity parent) {
		this.parent = parent;
	}

	@Override
	public void setPosition(double x, double y) {
		if (copy != null) {
			copy.setPosition(x, y);
		} else if (myShape != null) {
			myShape.setPosition(x, y);
		}
	}

	@Override
	public void setWorld(World world) {
		createCopy();
		if (copy != null)
			copy.setWorld(world);
	}

	@Override
	public void terminate() {
		if (copy != null)
			copy.terminate();
	}

	@Override
	public boolean terminated() {
		if (copy != null)
			copy.terminated();
		return true;
	}

	@Override
	public void update(double t) {
		if (copy != null)
			copy.update(t);
	}

	@Override
	public boolean updateStep(double t) {
		if (copy != null)
			return copy.updateStep(t);
		return true;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		name = e.getAttribute("name");
		if (e.hasAttribute("active"))
			activeExpr = new Expression(e.getAttribute("active"));
		myShape = null;
		String sh = e.getAttribute("shape");
		myShape = null;
		if (sh != null && sh.length() > 0) {
			myShape = (DrawableShape) Class.forName(sh).newInstance();
			myShape.setSize(Double.parseDouble(e.getAttribute("width")),
					Double.parseDouble(e.getAttribute("height")));
			myShape.setPosition(Double.parseDouble(e.getAttribute("x")),
					Double.parseDouble(e.getAttribute("y")));
			myShape.setColor(Color.decode(e.getAttribute("color")));
			if (e.hasAttribute("fill"))
				myShape.setFillColor(Color.decode(e.getAttribute("fill")));

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
				XMLEntity xe = XMLInterpretor.convert((Element) n, refs);
				if (xe instanceof EntityInterface) {
					reference = (EntityInterface) xe;
					prevShape = reference.getDrawingShape();
				}
			}
		}
		if (prevShape != null)
			prevShape.addListener(this);

	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("name", name);
		e.setAttribute("active", activeExpr.toString());
		if (myShape != null) {
			e.setAttribute("shape", myShape.getClass().getName());
			double[] pos = myShape.getBounds();
			e.setAttribute("x", Double.toString(pos[0] + pos[2] / 2));
			e.setAttribute("y", Double.toString(pos[1] + pos[3] / 2));
			e.setAttribute("width", Double.toString(pos[2]));
			e.setAttribute("height", Double.toString(pos[3]));
			e.setAttribute("color", Util.colorToString(myShape.getColor()));

			if (dynWidth != null)
				e.setAttribute("dyn_width", dynWidth.toString());
			if (dynHeight != null)
				e.setAttribute("dyn_height", dynHeight.toString());
			if (dynX != null)
				e.setAttribute("dyn_x", dynX.toString());
			if (dynY != null)
				e.setAttribute("dyn_y", dynY.toString());

			if (myShape.hasFillColor() && myShape.getFillColor() != null) {
				e.setAttribute("fill",
						Util.colorToString(myShape.getFillColor()));
			}
		}
		if (reference != null)
			e.appendChild(XMLInterpretor.makeCrossReferencedElement(root,
					reference, refs));
		return e;
	}

	public EntityInterface getReference() {
		return reference;
	}

	public void setReference(EntityInterface reference) {
		this.reference = reference;
		if (prevShape != null) {
			prevShape.removeListener(this);
		}
		prevShape = reference == null ? null : this.reference.getDrawingShape();
		if (prevShape != null) {
			prevShape.addListener(this);
		}
	}

	@Override
	public void changed(DrawableShape s) {
		if (s == prevShape && s != null) {
			double[] p = s.getPosition();
			double[] b = s.getBounds();
			if (myShape != null) {
				p = myShape.getPosition();
				b = myShape.getBounds();
			}
			myShape = s.copyShape();
			myShape.setPosition(p[0], p[1]);
			myShape.setSize(b[2], b[3]);
		}
	}

	@Override
	public void moved(DrawableShape s) {
	}

	@Override
	public void resized(DrawableShape s) {
		if (s == prevShape && s != null) {
			double[] b = s.getBounds();
			if (myShape != null) {
				myShape.setSize(b[2], b[3]);
			}
		}
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
