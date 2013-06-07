/**
 * 
 */
package wmas.behaviour.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.behaviour.simple.SimpleBehaviourData;
import wmas.expression.Variables;
import wmas.gui.shapes.ShapeDrawer;
import wmas.gui.shapes.elements.Rectangle;
import wmas.world.EntityInterface;
import wmas.world.memory.ArrayData;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class GraphBehaviour extends Rectangle implements Behaviour {
	List<GraphInterrupt> interrupts = new LinkedList<GraphInterrupt>();
	List<GraphTransition> outputs = new LinkedList<GraphTransition>();
	Behaviour behaviour = null;
	String factory = "";
	private Color colorized2 = null;

	public GraphBehaviour() {
	}

	public GraphBehaviour(Behaviour b) {
		behaviour = b;
	}

	public void updateXMLRef() {
		for (GraphInterrupt i : interrupts) {
			if (i.getDestination() != null
					&& i.getDestination() instanceof DummyXMLObject) {
				DummyXMLObject o = (DummyXMLObject) i.getDestination();
				i.setDestination(o.getObject());
				if (i.getDestination() instanceof DummyXMLObject)
					i.setDestination(null);
			}
		}
		for (GraphTransition i : outputs) {
			if (i.getDestination() != null
					&& i.getDestination() instanceof DummyXMLObject) {
				DummyXMLObject o = (DummyXMLObject) i.getDestination();
				i.setDestination(o.getObject());
				if (i.getDestination() instanceof DummyXMLObject)
					i.setDestination(null);
			}
		}
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		interrupts.clear();
		outputs.clear();
		refs.setObject(e, this);
		setPosition(Double.parseDouble(e.getAttribute("x")),
				Double.parseDouble(e.getAttribute("y")));
		this.factory = e.getAttribute("factory");
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				XMLEntity o = XMLInterpretor.convert((Element) n, refs);
				if (o != null) {
					if (o instanceof GraphInterrupt) {
						((GraphInterrupt) o).setSource(this);
						interrupts.add((GraphInterrupt) o);
					} else if (o instanceof GraphTransition) {
						((GraphTransition) o).setSource(this);
						outputs.add((GraphTransition) o);
					} else if (o instanceof Behaviour) {
						behaviour = ((Behaviour) o);
					}
				}
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		double[] pos = getPosition();
		e.setAttribute("x", Double.toString(pos[0]));
		e.setAttribute("y", Double.toString(pos[1]));
		e.setAttribute("factory", factory);
		refs.makeRef(this, e);
		Element n;
		if (behaviour != null) {
			n = XMLInterpretor
					.makeCrossReferencedElement(root, behaviour, refs);
			if (n != null)
				e.appendChild(n);
		}
		for (GraphInterrupt i : interrupts) {
			n = i.toXML(root, refs);
			if (n != null)
				e.appendChild(n);
		}
		for (GraphTransition i : outputs) {
			n = i.toXML(root, refs);
			if (n != null)
				e.appendChild(n);
		}
		return e;
	}

	public void init(EntityInterface object, Variables varSet, double t) {
		if (behaviour != null) {
			behaviour.init(object, varSet, t);
			behaviour.colorize(this.colorized2);
			super.colorize(this.colorized2);
		}
	}

	public void suspend(double t) {
		if (behaviour != null)
			behaviour.suspend(t);
	}

	private boolean _terminated() {
		if (behaviour != null)
			return behaviour.terminated();
		return true;
	}

	public boolean terminated() {
		boolean r = _terminated();
		// super.colorize(r ? null : this.colorized2);
		return r;
	}

	public void unsuspend(double t) {
		if (behaviour != null)
			behaviour.unsuspend(t);
	}

	public void update(double t) {
		if (behaviour != null)
			behaviour.update(t);
	}

	@Override
	public boolean isResizeable() {
		return false;
	}

	@Override
	public boolean hasBothResizeableDirections() {
		return false;
	}

	@Override
	public void paint(Graphics2D g) {
		String text = "";
		if (behaviour != null) {
			text = (behaviour instanceof BehaviourGraph) ? ((BehaviourGraph) behaviour)
					.getText() : behaviour.toString();
			if (text.length() > 0) {
				int h = ShapeDrawer.getStringHeight(g, text);
				int w = ShapeDrawer.getStringWidth(g, text);
				if (w % 2 == 1)
					w++;
				if (h % 2 == 1)
					h++;
				setSize(w + 12, h + 12);
			}
		}
		super.paint(g);
		if (text.length() > 0) {
			int h = ShapeDrawer.getStringHeight(g, text);
			int w = ShapeDrawer.getStringWidth(g, text);
			double[] pos = getPosition();
			pos[0] -= w / 2;
			pos[1] += g.getFontMetrics().getAscent() - h / 2;
			ShapeDrawer.drawText(g, text, (int) pos[0], (int) pos[1]);
		}
	}

	public Behaviour copy() {
		GraphBehaviour b = new GraphBehaviour(behaviour.copy());
		b.factory = factory;
		double[] pos = getPosition();
		b.setPosition(pos[0], pos[1]);
		b.interrupts.addAll(interrupts);
		b.outputs.addAll(outputs);
		return b;
	}

	public void finishCopy(Map<GraphBehaviour, GraphBehaviour> map) {
		List<GraphInterrupt> copy = new LinkedList<GraphInterrupt>();
		for (GraphInterrupt i : interrupts) {
			copy.add(i.copy(this, map));
		}
		interrupts = copy;
		List<GraphTransition> copy2 = new LinkedList<GraphTransition>();
		for (GraphTransition i : outputs) {
			copy2.add(i.copy(this, map));
		}
		outputs = copy2;
	}

	@Override
	public void colorize(Color c) {
		this.colorized2 = c;
		if (behaviour != null) {
			behaviour.colorize(c);
			// if(!behaviour.terminated())
			super.colorize(this.colorized2);
		}
	}

	public Color getColorization() {
		return this.colorized;
	}

	@Override
	public void reset() {
		behaviour.reset();
	}

	@Override
	public void terminate() {
		behaviour.terminate();
	}

	private ArrayData condData = new ArrayData();
	private ArrayData inteData = new ArrayData();
	private SimpleBehaviourData<ArrayData> sbd = new SimpleBehaviourData<ArrayData>(
			this, new ArrayData());

	@Override
	public BehaviourData getRepresentation() {
		sbd.getData().clear();
		sbd.getData().setSubData(0, behaviour.getRepresentation());
		int i = 0;
		inteData.clear();
		condData.clear();
		for (GraphInterrupt inter : interrupts) {
			inteData.setData(i++, inter.getData());
		}
		i = 0;
		for (GraphTransition cond : outputs) {
			condData.setData(i++, cond.getData());
		}
		sbd.getData().setData(0, inteData);
		sbd.getData().setData(1, condData);
		return sbd;
	}

	@Override
	public void initStep(EntityInterface object, Variables varSet, double t) {
		if (behaviour != null) {
			behaviour.initStep(object, varSet, t);
			behaviour.colorize(this.colorized2);
			super.colorize(this.colorized2);
		}
	}

	@Override
	public boolean updateStep(double t) {
		return (behaviour == null) || behaviour.updateStep(t);
	}
}