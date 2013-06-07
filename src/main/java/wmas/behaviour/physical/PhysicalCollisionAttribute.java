package wmas.behaviour.physical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wmas.world.EntityInterface;
import wmas.xml.Copiable;

public class PhysicalCollisionAttribute implements Copiable {

	private double lastTime = -1;
	private Map<EntityInterface, Set<EntityInterface>> collisions = new HashMap<EntityInterface, Set<EntityInterface>>();
	private Map<EntityInterface, Set<EntityInterface>> previousCollisions = new HashMap<EntityInterface, Set<EntityInterface>>();

	public void initCollision(double t) {
		if (lastTime != t) {
			previousCollisions.clear();
			previousCollisions.putAll(collisions);
			collisions.clear();
			lastTime = t;
		}
	}

	public void addCollision(double t, EntityInterface e1, EntityInterface e2) {
		initCollision(t);
		if (!collisions.containsKey(e1)) {
			collisions.put(e1, new HashSet<EntityInterface>());
		}
		if (e1.getWorld() != null)
			if (!collisions.get(e1).contains(e2)) {
				e1.getWorld().addEvent(t, "collision", e2.getName(), e1);
			}
		collisions.get(e1).add(e2);
	}

	public boolean hasCollision(EntityInterface e1) {
		return previousCollisions.containsKey(e1);
	}

	public boolean hasCollision(EntityInterface e1, EntityInterface e2) {
		if (previousCollisions.containsKey(e1)) {
			return previousCollisions.get(e1).contains(e2);
		}
		return false;
	}

	public boolean hasCollision(EntityInterface e1, String namePattern) {
		if (previousCollisions.containsKey(e1)) {
			for (EntityInterface e : previousCollisions.get(e1)) {
				if (e.getName() != null && e.getName().startsWith(namePattern)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Copiable copy() {
		return new PhysicalCollisionAttribute();
	}
}
