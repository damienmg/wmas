package wmas.behaviour;

import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.expression.Variables;
import wmas.world.EntityInterface;
import wmas.xml.XMLCrossRef;

public abstract class AbstractBehaviour implements Behaviour {

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		return root.createElement(this.getClass().getName());
	}

	public boolean updateStep(double t) {
		update(t);
		return true;
	}

	public void initStep(EntityInterface object, Variables varSet, double t) {
		init(object, varSet, t);
	}

	public void colorize(Color c) {
	}
}
