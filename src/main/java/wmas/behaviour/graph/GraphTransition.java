/**
 * 
 */
package wmas.behaviour.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.elements.Connector;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.memory.ArrayData;
import wmas.world.memory.Data;
import wmas.world.memory.ExpressionData;
import wmas.world.memory.LongData;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class GraphTransition extends Connector implements XMLEntity {
	List<List<Doublet<Expression, Boolean>>> condition // DNF (list of
														// conjunctive clauses.
														// A conjunctive clause
														// is represented as a
														// map of term and the
														// not value of the
														// litteral)
	= new LinkedList<List<Doublet<Expression, Boolean>>>();
	List<Doublet<Expression, Expression>> affectations = new LinkedList<Doublet<Expression, Expression>>(); // List
																											// of
																											// affectations
																											// to
																											// be
																											// done
																											// when
																											// taking
																											// this
																											// transition

	public Data getData() {
		ArrayData affectations = new ArrayData();
		ArrayData conditons = new ArrayData();
		int i = 0;
		for (List<Doublet<Expression, Boolean>> c : condition) {
			ArrayData conj = new ArrayData();
			int j = 0;
			for (Doublet<Expression, Boolean> d : c) {
				conj.setData(j++, new ExpressionData(d.getFirst()));
				LongData ld = new LongData();
				ld.setNumber(d.getSecond() ? 1 : 0);
				conj.setData(j++, ld);
			}
			conditons.setData(i++, conj);
		}
		i = 0;
		for (Doublet<Expression, Expression> d : this.affectations) {
			affectations.setData(i++, new ExpressionData(d.getFirst()));
			affectations.setData(i++, new ExpressionData(d.getSecond()));
		}
		ArrayData ad = new ArrayData();
		ad.setData(0, conditons);
		ad.setData(1, affectations);
		return ad;
	}

	public boolean isDefault() {
		return condition.size() == 0;
	}

	public boolean verified(EntityInterface e, Variables varSet) {
		if (condition.size() == 0)
			return true;
		for (List<Doublet<Expression, Boolean>> cClause : condition) {
			boolean r = true;
			Iterator<Doublet<Expression, Boolean>> it = cClause.iterator();
			while (r && it.hasNext()) {
				Doublet<Expression, Boolean> entry = it.next();
				if (entry.getFirst().verified(e, varSet)) {
					if (!(entry.getSecond()))
						r = false;
				} else {
					if (entry.getSecond())
						r = false;
				}
			}
			if (r)
				return true;
		}
		return false;
	}

	void doTakeTransition(EntityInterface e, Variables varSet) {
		for (Doublet<Expression, Expression> me : affectations) {
			me.getFirst()
					.affect(e, varSet, me.getSecond().getObject(e, varSet));
		}
	}

	public GraphTransition() { // For XML
		super(null, null);
	}

	public GraphTransition(DrawableShape source, DrawableShape dest) {
		super(source, dest);
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		setDestination(DummyXMLObject.getRef(e.getAttribute("xdest"), refs));
		condition.clear();
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("conjunction")) {
					List<Doublet<Expression, Boolean>> cond = new LinkedList<Doublet<Expression, Boolean>>();
					for (int j = 0; j < el.getChildNodes().getLength(); j++) {
						n = el.getChildNodes().item(j);
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							Element el2 = (Element) n;
							if (el2.getTagName().equals("expression")) {
								cond.add(new Doublet<Expression, Boolean>(
										new Expression(el2
												.getAttribute("value")),
										!el2.hasAttribute("negative")
												|| !el2.getAttribute("negative")
														.equals("1")));
							}
						}
					}
					condition.add(cond);
				} else if (el.getTagName().equals("affectation")) {
					affectations.add(new Doublet<Expression, Expression>(
							new Expression(el.getAttribute("name")),
							new Expression(el.getAttribute("value"))));
				}
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("xdest", refs.getRef(getDestination()));
		for (List<Doublet<Expression, Boolean>> cClause : condition) {
			Element conjunction = root.createElement("conjunction");
			for (Doublet<Expression, Boolean> entry : cClause) {
				Element attr = root.createElement("expression");
				attr.setAttribute("value", entry.getFirst().toString());
				attr.setAttribute("negative", entry.getSecond() ? "0" : "1");
				conjunction.appendChild(attr);
			}
			e.appendChild(conjunction);
		}
		for (Doublet<Expression, Expression> me : affectations) {
			Element affectation = root.createElement("affectation");
			affectation.setAttribute("name", me.getFirst().toString());
			affectation.setAttribute("value", me.getSecond().toString());
			e.appendChild(affectation);
		}
		return e;
	}

	@Override
	public String getText() {
		String s = "";
		for (List<Doublet<Expression, Boolean>> cClause : condition) {
			if (s.length() > 0)
				s += " ∨ ";
			s += "(";
			boolean first = true;
			for (Doublet<Expression, Boolean> entry : cClause) {
				if (first) {
					first = false;
				} else {
					s += "∧";
				}
				if (!entry.getSecond())
					s += "¬";
				s += entry.getFirst().toString();
			}
			s += ")";
		}
		for (Doublet<Expression, Expression> e : affectations) {
			s += "\n" + e.getFirst() + " ← " + e.getSecond().toString();
		}
		return s;
	}

	protected void copy(GraphTransition dest) {
		for (List<Doublet<Expression, Boolean>> m : condition) {
			List<Doublet<Expression, Boolean>> copy = new LinkedList<Doublet<Expression, Boolean>>();
			for (Doublet<Expression, Boolean> e : m) {
				copy.add(new Doublet<Expression, Boolean>(new Expression(
						(Expression) e.getFirst()), e.getSecond()));
			}
			dest.condition.add(copy);
		}
		for (Doublet<Expression, Expression> e : affectations) {
			dest.affectations
					.add(new Doublet<Expression, Expression>(new Expression(e
							.getFirst()), new Expression(e.getSecond())));
		}
	}

	public GraphTransition copy(DrawableShape orig,
			Map<GraphBehaviour, GraphBehaviour> map) {
		GraphTransition r = new GraphTransition(orig, map.get(this
				.getDestination()));
		copy(r);
		return r;
	}

	public XMLEntity copy() {
		GraphTransition r = new GraphTransition(getSource(), getDestination());
		copy(r);
		return r;
	}

}