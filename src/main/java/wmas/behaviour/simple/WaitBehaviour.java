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

public class WaitBehaviour extends AbstractBehaviour {
	private Expression waitingTime = new Expression(1);
	private double lastTime = -1.0;
	private boolean suspended = false;
	private double startTime = 0.0;
	private EntityInterface entity = null;
	private Variables varSet;

	public void init(EntityInterface object, Variables varSet, double t) {
		this.varSet = varSet;
		lastTime = t;
		startTime = t;
		entity = object;
		suspended = false;
	}

	public void suspend(double t) {
		suspended = true;
		lastTime = t;
	}

	public boolean terminated() {
		return lastTime < 0.0
				|| (lastTime - startTime >= waitingTime.getNumber(entity,
						varSet).doubleValue());
	}

	public void unsuspend(double t) {
		suspended = false;
		startTime += t - lastTime;
		lastTime = t;
	}

	public void update(double t) {
		if (!suspended) {
			lastTime = t;
		}
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		waitingTime = new Expression(e.getAttribute("duration"));
		sbd.getData().setData(waitingTime);
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = super.toXML(root, refs);
		e.setAttribute("duration", waitingTime.toString());
		return e;
	}

	public Expression getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(Expression waitingTime) {
		this.waitingTime = waitingTime;
		sbd.getData().setData(waitingTime);
	}

	public String toString() {
		return "wait(" + waitingTime + ")";
	}

	public Behaviour copy() {
		WaitBehaviour c = new WaitBehaviour();
		c.waitingTime = waitingTime;
		c.sbd.getData().setData(waitingTime);
		return c;
	}

	public void reset() {
	}

	@Override
	public void terminate() {
	}

	private SimpleBehaviourData<ExpressionData> sbd = new SimpleBehaviourData<ExpressionData>(
			this, new ExpressionData(waitingTime));

	public BehaviourData getRepresentation() {
		return sbd;
	}

}
