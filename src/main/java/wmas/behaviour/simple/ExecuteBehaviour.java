package wmas.behaviour.simple;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.behaviour.AbstractBehaviour;
import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.expression.Expression;
import wmas.expression.Variables;
import wmas.world.EntityInterface;
import wmas.world.memory.ExpressionData;
import wmas.xml.XMLCrossRef;

public class ExecuteBehaviour extends AbstractBehaviour {
	private Expression behaviourExpression = new Expression(0);
	private Behaviour internal = null;

	private void doInit(EntityInterface object, Variables varSet) {
		Object o = behaviourExpression.getObject(object, varSet);
		internal = null;
		if (o instanceof Behaviour) {
			internal = ((Behaviour) o);
		} else if (o instanceof BehaviourData) {
			internal = ((BehaviourData) o).createBehaviour();
		} else {
			return;
		}
	}

	public void init(EntityInterface object, Variables varSet, double t) {
		doInit(object, varSet);
		internal.init(object, varSet, t);
	}

	public void suspend(double t) {
		if (internal != null)
			internal.suspend(t);
	}

	public boolean terminated() {
		return internal == null || internal.terminated();
	}

	public void unsuspend(double t) {
		if (internal != null)
			internal.unsuspend(t);
	}

	public void update(double t) {
		if (internal != null)
			internal.update(t);
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		behaviourExpression = new Expression(e.getAttribute("behaviour"));
		sbd.getData().setData(behaviourExpression);
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = super.toXML(root, refs);
		e.setAttribute("behaviour", behaviourExpression.toString());
		return e;
	}

	public String toString() {
		return "execute(" + behaviourExpression + ")";
	}

	public Behaviour copy() {
		ExecuteBehaviour c = new ExecuteBehaviour();
		c.behaviourExpression = behaviourExpression;
		c.sbd.getData().setData(behaviourExpression);
		return c;
	}

	public void reset() {
		if (internal != null)
			internal.reset();
	}

	public void terminate() {
		if (internal != null)
			internal.terminate();
	}

	private SimpleBehaviourData<ExpressionData> sbd = new SimpleBehaviourData<ExpressionData>(
			this, new ExpressionData(behaviourExpression));

	public BehaviourData getRepresentation() {
		return sbd;
	}

	public Expression getBehaviourExpression() {
		return behaviourExpression;
	}

	public void setBehaviourExpression(Expression behaviourExpression) {
		this.behaviourExpression = behaviourExpression;
	}

	@Override
	public void initStep(EntityInterface object, Variables varSet, double t) {
		doInit(object, varSet);
		internal.initStep(object, varSet, t);
	}

	@Override
	public boolean updateStep(double t) {
		if (internal != null) {
			return internal.updateStep(t);
		}
		return true;
	}

}
