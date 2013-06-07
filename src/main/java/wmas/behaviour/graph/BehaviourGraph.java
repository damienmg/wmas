package wmas.behaviour.graph;

import java.awt.Color;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.behaviour.simple.SimpleBehaviourData;
import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.gui.shapes.elements.Circle;
import wmas.world.EntityInterface;
import wmas.world.memory.ArrayData;
import wmas.world.memory.ExpressionData;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class BehaviourGraph extends AbstractCollection<Behaviour> implements
		Behaviour, List<Behaviour> {
	List<GraphTransition> initLink = new LinkedList<GraphTransition>();
	List<GraphBehaviour> behaviours = new LinkedList<GraphBehaviour>();
	List<Expression> inputs = new LinkedList<Expression>(); // list of inputs
	List<Expression> outputs = new LinkedList<Expression>(); // list of outputs

	Circle orig = new Circle();

	EntityInterface entity = null;

	GraphBehaviour current = null;
	GraphBehaviour interrupted = null;
	String name = "";
	Color colour = null;

	Variables varSet = new Variables(); // list of variables in this context
	Variables parentVarSet = new Variables(); // list of variables of parent
												// context

	public BehaviourGraph() {
		orig.setSelectable(true);
		orig.setEditable(false);
		orig.setPosition(0, 0);
		orig.setSize(30);
		orig.setFillColor(Color.LIGHT_GRAY);
		orig.setColor(null);
	}

	public void copy(BehaviourGraph g) {
		orig = g.orig;
		initLink = g.initLink;
		current = null;
		behaviours = g.behaviours;
		entity = null;
		interrupted = null;
		name = g.name;
	}

	private boolean checkInterrupts() {
		if (current != null) {
			for (GraphInterrupt interruption : current.interrupts) {
				if (interruption.getDestination() instanceof GraphBehaviour) {
					if (interruption.verified(entity, varSet)) {
						interrupted = current;
						current = (GraphBehaviour) interruption
								.getDestination();
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean getNext(double t, boolean first, boolean step) {
		GraphBehaviour def = null;
		GraphTransition defcond = null;
		if (current != null || first) {
			if (current != null) {
				current.colorize(null);
			}
			List<GraphTransition> conds = first ? initLink : current.outputs;
			for (GraphTransition cond : conds) {
				if (cond.getDestination() instanceof GraphBehaviour) {
					if (cond.verified(entity, varSet)) {
						if (cond.isDefault()) {
							def = (GraphBehaviour) cond.getDestination();
							defcond = cond;
						} else {
							cond.doTakeTransition(entity, varSet);
							current = (GraphBehaviour) cond.getDestination();
							if (interrupted != null)
								interrupted.terminate();
							interrupted = null;
							if (step)
								current.initStep(entity, varSet, t);
							else
								current.init(entity, varSet, t);
							current.colorize(colour);
							return true;
						}
					}
				}
			}
		}
		if (def != null) {
			if (interrupted != null)
				interrupted.terminate();
			current = def;
			interrupted = null;
			defcond.doTakeTransition(entity, varSet);
			if (step)
				def.initStep(entity, varSet, t);
			else
				def.init(entity, varSet, t);
			def.colorize(this.colour);
			return true;
		}
		if (interrupted != null && !first) {
			current = interrupted;
			interrupted = null;
			current.unsuspend(t);
			current.colorize(this.colour);
			return true;
		}
		return false;
	}

	private double lastT;
	private boolean stepFinished = false;

	@Override
	public void initStep(EntityInterface object, Variables varSet, double t) {
		lastT = t;
		stepFinished = false;
		this.entity = object;
		current = null;
		this.parentVarSet = varSet;
		this.varSet.clear();
		if (this.parentVarSet != null) {
			for (Expression i : inputs) {
				i.affect(object, this.varSet, i.getObject(object, varSet));
			}
		}
		if (!getNext(t, true, true)) {
			current = null;
		} else {
			stepFinished = current != null && current.terminated();
		}
	}

	@Override
	public boolean updateStep(double t) {
		if (current != null) {
			if (lastT != t || !stepFinished) {
				stepFinished = false;
				lastT = t;
				if (current.updateStep(t)) {
					if (checkInterrupts()) {
						interrupted.suspend(t);
						current.initStep(entity, varSet, t);
						current.colorize(this.colour);
					}
				} else
					return false;
			}
			stepFinished = false;
			if (current != null && current.terminated()) {
				if (!getNext(t, false, true)) {
					current = null;
				} else {
					stepFinished = current != null && current.terminated();
					return false;
				}
			}
			return (current == null) || !(current.terminated());
		}
		return true;
	}

	public void init(EntityInterface object, Variables varSet, double t) {
		this.entity = object;
		current = null;
		this.parentVarSet = varSet;
		this.varSet.clear();
		if (this.parentVarSet != null) {
			for (Expression i : inputs) {
				i.affect(object, this.varSet, i.getObject(object, varSet));
			}
		}
		if (!getNext(t, true, false))
			current = null;
		while (current != null && current.terminated()) { // Some behaviour does
															// not last
			if (!getNext(t, false, false))
				current = null;
		}
	}

	public void suspend(double t) {
		if (current != null) {
			current.suspend(t);
		}
	}

	public boolean terminated() {
		if (current == null || (current.terminated() && !stepFinished)) {
			if (current != null) {
				current.colorize(null);
			}
			if (this.parentVarSet != null) {
				for (Expression i : outputs) {
					i.affect(entity, this.parentVarSet,
							i.getObject(entity, varSet));
				}
				this.parentVarSet = null;
			}
			return true;
		}
		return false;
	}

	public void unsuspend(double t) {
		if (current != null) {
			current.unsuspend(t);
		}
	}

	public void update(double t) {
		stepFinished = false;
		if (current != null) {
			current.update(t);
			if (checkInterrupts()) {
				interrupted.suspend(t);
				current.init(entity, varSet, t);
				current.colorize(colour);
			}

			while (current != null && current.terminated()) { // Some behaviour
																// does not last
				if (!getNext(t, false, false))
					current = null;
			}
		}
	}

	private void updateXMLRef() {
		for (GraphTransition i : initLink) {
			if (i.getDestination() != null
					&& i.getDestination() instanceof DummyXMLObject) {
				DummyXMLObject o = (DummyXMLObject) i.getDestination();
				i.setDestination(o.getObject());
				if (i.getDestination() instanceof DummyXMLObject)
					i.setDestination(null);
			}
		}
		for (GraphBehaviour i : behaviours) {
			i.updateXMLRef();
		}
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		if (refs == null)
			refs = new XMLCrossRef();
		refs.setObject(e, this);
		inputs.clear();
		outputs.clear();
		name = e.getAttribute("name");
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("input")) {
					inputs.add(new Expression(el.getAttribute("name")));
				} else if (el.getTagName().equals("output")) {
					outputs.add(new Expression(el.getAttribute("name")));
				} else {
					XMLEntity o = XMLInterpretor.convert(el, refs);
					if (o != null) {
						if (o instanceof GraphTransition) {
							((GraphTransition) o).setSource(this.orig);
							initLink.add((GraphTransition) o);
						} else if (o instanceof GraphBehaviour) {
							behaviours.add((GraphBehaviour) o);
						}
					}
				}
			}
		}
		updateXMLRef();
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		if (refs == null)
			refs = new XMLCrossRef();
		Element e = root.createElement(this.getClass().getName());
		if (name != null)
			e.setAttribute("name", name);

		refs.makeRef(this, e);
		for (GraphTransition i : initLink) {
			e.appendChild(i.toXML(root, refs));
		}
		for (GraphBehaviour i : behaviours) {
			e.appendChild(i.toXML(root, refs));
		}
		for (Expression s : inputs) {
			Element el = root.createElement("input");
			el.setAttribute("name", s.toString());
			e.appendChild(el);
		}
		for (Expression s : outputs) {
			Element el = root.createElement("output");
			el.setAttribute("name", s.toString());
			e.appendChild(el);
		}
		return e;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return ((name == null) || name.length() == 0) ? "Graph"
				: ("Graph:" + name);
	}

	public String getText() {
		String res = toString();
		if (inputs.size() > 0 || outputs.size() > 0) {
			res += "\n<";
			boolean first = true;
			for (Expression i : inputs) {
				if (first)
					first = false;
				else
					res += ", ";
				res += i;
			}
			res += "> ↦ <";
			first = true;
			for (Expression i : outputs) {
				if (first)
					first = false;
				else
					res += ", ";
				res += i;
			}
			res += ">";
		}
		return res;
	}

	public Behaviour copy() {
		BehaviourGraph g = new BehaviourGraph();
		g.name = name;
		HashMap<GraphBehaviour, GraphBehaviour> behaviourMap = new HashMap<GraphBehaviour, GraphBehaviour>();
		for (GraphBehaviour b : behaviours) {
			GraphBehaviour c = (GraphBehaviour) b.copy();
			behaviourMap.put(b, c);
			g.behaviours.add(c);
		}
		for (GraphTransition c : initLink) {
			g.initLink.add(c.copy(g.orig, behaviourMap));
		}
		for (GraphBehaviour b : g.behaviours) {
			b.finishCopy(behaviourMap);
		}
		g.inputs.addAll(inputs);
		g.outputs.addAll(outputs);
		return g;
	}

	private class BehaviourIterator implements ListIterator<Behaviour> {
		private ListIterator<GraphBehaviour> it;

		BehaviourIterator(ListIterator<GraphBehaviour> it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Behaviour next() {
			return it.next().behaviour;
		}

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public void add(Behaviour o) {
			it.add(new GraphBehaviour(o));
		}

		@Override
		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		@Override
		public int nextIndex() {
			return it.nextIndex();
		}

		@Override
		public Behaviour previous() {
			return it.previous();
		}

		@Override
		public int previousIndex() {
			return it.previousIndex();
		}

		@Override
		public void set(Behaviour o) {
			it.set(new GraphBehaviour(o));
		}
	}

	@Override
	public Iterator<Behaviour> iterator() {
		return new BehaviourIterator(behaviours.listIterator());
	}

	@Override
	public int size() {
		return behaviours.size();
	}

	@Override
	public void add(int index, Behaviour element) {
		behaviours.add(new GraphBehaviour(element));
	}

	@Override
	public boolean addAll(int index, Collection<? extends Behaviour> c) {
		for (Behaviour b : c) {
			behaviours.add(new GraphBehaviour(b));
		}
		return true;
	}

	@Override
	public Behaviour get(int index) {
		return behaviours.get(index).behaviour;
	}

	@Override
	public int indexOf(Object o) {
		for (int i = 0; i < behaviours.size(); i++) {
			if (behaviours.get(i).behaviour == o)
				return i;
			if (o != null && o.equals(behaviours.get(i)))
				return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = behaviours.size() - 1; i >= 0; i--) {
			if (behaviours.get(i).behaviour == o)
				return i;
			if (o != null && o.equals(behaviours.get(i)))
				return i;
		}
		return -1;
	}

	@Override
	public ListIterator<Behaviour> listIterator() {
		return new BehaviourIterator(behaviours.listIterator());
	}

	@Override
	public ListIterator<Behaviour> listIterator(int index) {
		return new BehaviourIterator(behaviours.listIterator(index));
	}

	@Override
	public Behaviour remove(int index) {
		GraphBehaviour r = behaviours.remove(index);
		if (r == null)
			return null;
		return r.behaviour;
	}

	public boolean remove(Behaviour b) {
		for (GraphBehaviour r : behaviours) {
			if (r.behaviour == b) {
				behaviours.remove(r);
				return true;
			}
		}
		return false;
	}

	@Override
	public Behaviour set(int index, Behaviour element) {
		GraphBehaviour r = behaviours.get(index);
		if (r != null) {
			Behaviour res = r.behaviour;
			r.behaviour = element;
			return res;
		}
		return null;
	}

	@Override
	public List<Behaviour> subList(int fromIndex, int toIndex) {
		List<Behaviour> subList = new LinkedList<Behaviour>();
		List<GraphBehaviour> subListOrig = behaviours.subList(fromIndex,
				toIndex);
		if (subListOrig == null)
			return null;
		for (GraphBehaviour b : subListOrig) {
			subList.add(b.behaviour);
		}
		return subList;
	}

	public boolean hasPrevious(GraphBehaviour b) {
		if (b == null)
			return false;
		for (GraphTransition i : initLink) {
			if (i.getDestination() == b)
				return true;
		}
		for (GraphBehaviour j : behaviours) {
			for (GraphTransition i : j.outputs) {
				if (i.getDestination() == b)
					return true;
			}
			for (GraphInterrupt i : j.interrupts) {
				if (i.getDestination() == b)
					return true;
			}
		}
		return false;
	}

	public boolean hasPrevious(Object o) {
		if (!(o instanceof Behaviour))
			return false;
		int index = indexOf(o);
		if (index < 0)
			return false;
		GraphBehaviour b = behaviours.get(index);
		return hasPrevious(b);
	}

	public void colorize(Color c) {
		this.colour = c;
	}

	public GraphBehaviour getGraphBehaviour(Behaviour o) {
		for (GraphBehaviour j : behaviours) {
			if (j.behaviour == o)
				return j;
		}
		return null;
	}

	public Color getColorization() {
		return terminated() ? null : this.colour;
	}

	@Override
	public void reset() {
		for (Behaviour b : behaviours) {
			b.reset();
		}
	}

	@Override
	public void terminate() {
		if (current != null) {
			current.terminate();
			current = null;
		}
	}

	private ArrayData behavioursData = new ArrayData();
	private ArrayData initLinkData = new ArrayData();
	private ArrayData inData = new ArrayData();
	private ArrayData outData = new ArrayData();
	private SimpleBehaviourData<ArrayData> sbd = new SimpleBehaviourData<ArrayData>(
			this, new ArrayData());

	@Override
	public BehaviourData getRepresentation() {
		behavioursData.clear();
		initLinkData.clear();
		inData.clear();
		outData.clear();
		int i = 0;
		for (Behaviour b : behaviours) {
			behavioursData.setData(i++, b.getRepresentation());
		}
		i = 0;
		for (GraphTransition t : initLink) {
			behavioursData.setData(i++, t.getData());
		}
		i = 0;
		for (Expression t : inputs) {
			inData.setData(i++, new ExpressionData(t));
		}
		i = 0;
		for (Expression t : outputs) {
			outData.setData(i++, new ExpressionData(t));
		}
		sbd.getData().setData(0, behavioursData);
		sbd.getData().setData(1, initLinkData);
		sbd.getData().setData(2, inData);
		sbd.getData().setData(3, outData);
		return sbd;
	}
}
