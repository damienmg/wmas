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

public class DropBehaviour extends AbstractBehaviour {

	String namePattern = "";
	boolean dropAll = true;

	public Behaviour copy() {
		DropBehaviour r = new DropBehaviour();
		r.namePattern = namePattern;
		r.dropAll = dropAll;
		r.sbd.getData().setData(dropAll ? "*" : namePattern);
		return r;
	}

	@Override
	public void init(EntityInterface object, Variables varSet, double t) {
		if (object != null && object.hasAttribute("carrier")
				&& object.getWorld() != null
				&& object.getWorld().hasStaticAttribute("lifting")) {
			LiftedAttribute attr = (LiftedAttribute) object.getWorld()
					.getStaticAttribute("lifting");
			if (dropAll) {
				attr.drop(object);
			} else if (attr.doCarry(object, namePattern)) {
				attr.drop(object, namePattern);
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
		namePattern = e.hasAttribute("pattern") ? e.getAttribute("pattern")
				: "";
		dropAll = e.hasAttribute("all") && e.getAttribute("all").equals("1");
		sbd.getData().setData(dropAll ? "*" : namePattern);

	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		if (dropAll) {
			e.setAttribute("all", "1");
		} else
			e.setAttribute("pattern", namePattern);
		return e;
	}

	public String toString() {
		if (dropAll)
			return "DropAll";
		return "Drop(" + namePattern + ")";
	}

	public void reset() {
	}

	public String getNamePattern() {
		return namePattern;
	}

	public void setNamePattern(String namePattern) {
		sbd.getData().setData(namePattern);
		this.namePattern = namePattern;
	}

	public boolean isDropAll() {
		return dropAll;
	}

	public void setDropAll(boolean dropAll) {
		sbd.getData().setData(dropAll ? "*" : namePattern);
		this.dropAll = dropAll;
	}

	@Override
	public void terminate() {
	}

	private SimpleBehaviourData<StringData> sbd = new SimpleBehaviourData<StringData>(
			this, new StringData(dropAll ? "*" : namePattern));

	public BehaviourData getRepresentation() {
		return sbd;
	}

}
