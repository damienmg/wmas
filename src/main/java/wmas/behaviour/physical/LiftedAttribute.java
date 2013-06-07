package wmas.behaviour.physical;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wmas.world.EntityInterface;
import wmas.xml.Copiable;

public class LiftedAttribute implements Copiable {

	private double lastTime = -1;
	private Map<EntityInterface, Set<EntityInterface>> previousCollisions = new HashMap<EntityInterface, Set<EntityInterface>>();
	private Map<EntityInterface, Set<EntityInterface>> collisions = new HashMap<EntityInterface, Set<EntityInterface>>();
	private Map<EntityInterface, EntityInterface> lifteds = new HashMap<EntityInterface, EntityInterface>();
	private Map<EntityInterface, Set<EntityInterface>> carriers = new HashMap<EntityInterface, Set<EntityInterface>>();

	public void initCollision(double t) {
		if (lastTime != t) {
			previousCollisions.clear();
			previousCollisions.putAll(collisions);
			collisions.clear();
			lastTime = t;
		}
	}

	public boolean lift(EntityInterface carrier, String liftable) {
		if (previousCollisions.containsKey(carrier)) {
			for (EntityInterface e : previousCollisions.get(carrier)) {
				if (e.getName() != null && e.getName().startsWith(liftable)
						&& !lifteds.containsKey(e)) {
					return lift(carrier, e);
				}
			}
		}
		return false;
	}

	public boolean lift(EntityInterface carrier, EntityInterface lifted) {
		if (lifteds.containsKey(lifted)) {
			return lifteds.get(lifted) == carrier;
		}
		if (!hasCollision(carrier, lifted)) {
			return false;
		}
		if (!carriers.containsKey(carrier)) {
			carriers.put(carrier, new HashSet<EntityInterface>());
		}
		carriers.get(carrier).add(lifted);
		lifteds.put(lifted, carrier);
		return true;
	}

	public boolean doCarry(EntityInterface e1) {
		return carriers.containsKey(e1);
	}

	public boolean doCarry(EntityInterface e1, EntityInterface e2) {
		return lifteds.get(e2) == (e1);
	}

	public boolean doCarry(EntityInterface e1, String namePattern) {
		if (carriers.containsKey(e1)) {
			for (EntityInterface e : carriers.get(e1)) {
				if (e.getName() != null && e.getName().startsWith(namePattern)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean drop(EntityInterface carrier, String liftable) {
		if (carriers.containsKey(carrier)) {
			for (EntityInterface e : carriers.get(carrier)) {
				if (e.getName() != null && e.getName().startsWith(liftable)) {
					return drop(carrier, e);
				}
			}
		}
		return false;
	}

	public boolean drop(EntityInterface e) {
		if (!carriers.containsKey(e))
			return false;
		for (EntityInterface e1 : carriers.get(e)) {
			lifteds.remove(e1);
		}
		carriers.remove(e);
		return true;
	}

	public boolean drop(EntityInterface e1, EntityInterface e2) {
		if (lifteds.get(e2) != e1)
			return false;
		if (!carriers.containsKey(e1))
			return false;
		if (!carriers.get(e1).contains(e2))
			return false;
		carriers.get(e1).remove(e2);
		lifteds.remove(e2);
		if (carriers.get(e1).size() == 0)
			carriers.remove(e1);
		return true;
	}

	public void addCollision(double t, EntityInterface e1, EntityInterface e2) {
		initCollision(t);
		if (lifteds.containsKey(e2))
			return; // cannot lift an already lifted object

		if (!collisions.containsKey(e1)) {
			collisions.put(e1, new HashSet<EntityInterface>());
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
			if (namePattern.isEmpty())
				return true;
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
		return new LiftedAttribute();
	}

	public void setNewPosition(EntityInterface entity, double dx, double dy) {
		if (carriers.containsKey(entity)) {
			for (EntityInterface e : carriers.get(entity)) {
				double[] pos = e.getPosition();
				if (pos != null) {
					e.setPosition(pos[0] - dx, pos[1] - dy);
				}
			}
		}
	}
}
