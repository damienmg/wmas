package wmas.behaviour.physical;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.behaviour.AbstractBehaviour;
import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.behaviour.simple.SimpleBehaviourData;
import wmas.expression.Variables;
import wmas.world.EntityInterface;
import wmas.world.memory.StringData;
import wmas.xml.XMLCrossRef;

public class LiftBehaviour extends AbstractBehaviour {

	String namePattern = "";

	public Behaviour copy() {
		LiftBehaviour r = new LiftBehaviour();
		r.namePattern = namePattern;
		r.sbd.getData().setData(namePattern);
		return r;
	}

	@Override
	public void init(EntityInterface object, Variables varSet, double t) {
		if (object != null && object.hasAttribute("carrier")
				&& object.getWorld() != null
				&& object.getWorld().hasStaticAttribute("lifting")) {
			LiftedAttribute attr = (LiftedAttribute) object.getWorld()
					.getStaticAttribute("lifting");
			if (attr.hasCollision(object, namePattern)) {
				attr.lift(object, namePattern);
			}
		}
	}

	public void suspend(double t) {
	}

	public boolean terminated() {
		return true;
	}

	public void unsuspend(double t) {
	}

	public void update(double t) {
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		namePattern = e.getAttribute("pattern");
		sbd.getData().setData(namePattern);
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("pattern", namePattern);
		return e;
	}

	public String toString() {
		return "Lift(" + namePattern + ")";
	}

	public void reset() {
	}

	public String getNamePattern() {
		return namePattern;
	}

	public void setNamePattern(String namePattern) {
		this.namePattern = namePattern;
		sbd.getData().setData(namePattern);
	}

	@Override
	public void terminate() {
	}

	private SimpleBehaviourData<StringData> sbd = new SimpleBehaviourData<StringData>(
			this, new StringData(namePattern));

	public BehaviourData getRepresentation() {
		return sbd;
	}
}
