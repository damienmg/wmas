package wmas.behaviour.simple;

import wmas.behaviour.AbstractBehaviour;
import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.expression.Variables;
import wmas.world.EntityInterface;
import wmas.world.memory.VoidData;

// This behaviour do really nothing
public class NopBehaviour extends AbstractBehaviour {
	public void init(EntityInterface object, Variables varSet, double t) {
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

	public String toString() {
		return "nop";
	}

	public Behaviour copy() {
		return new NopBehaviour();
	}

	public void reset() {
	}

	public void terminate() {
	}

	private SimpleBehaviourData<VoidData> sbd = new SimpleBehaviourData<VoidData>(
			this, VoidData.instance);

	public BehaviourData getRepresentation() {
		return sbd;
	}
}
