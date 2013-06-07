package wmas.behaviour;

import wmas.world.memory.Data;

public interface BehaviourData extends Data {
	public Behaviour createBehaviour();
}
