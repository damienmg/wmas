package wmas.behaviour.physical;

import java.util.List;

import wmas.expression.functions.Function;
import wmas.world.EntityInterface;

public class CollisionFunction implements Function {

	private static boolean isVerified(EntityInterface e, String namePattern) {
		if (e.hasAttribute("collide") && e.getWorld() != null
				&& e.getWorld().hasStaticAttribute("collision")) {
			PhysicalCollisionAttribute pca = ((PhysicalCollisionAttribute) e
					.getWorld().getStaticAttribute("collision"));
			if (namePattern == null || namePattern.isEmpty()) {
				return pca.hasCollision(e);
			}
			return pca.hasCollision(e, namePattern);
		}
		return false;
	}

	private static boolean isVerified(EntityInterface e) {
		if (e.hasAttribute("collide") && e.getWorld() != null
				&& e.getWorld().hasStaticAttribute("collision")) {
			PhysicalCollisionAttribute pca = ((PhysicalCollisionAttribute) e
					.getWorld().getStaticAttribute("collision"));
			return pca.hasCollision(e);
		}
		return false;
	}

	public void affect(EntityInterface e, List<Object> args, Object v) {
	}

	public int getMaxNbArg() {
		return 1;
	}

	public int getMinNbArg() {
		return 0;
	}

	public String getName() {
		return "collides";
	}

	public Object getValue(EntityInterface e, List<Object> args) {
		if (e == null)
			return false;
		if (args.size() > 0) {
			return isVerified(e, args.get(0).toString());
		}
		return isVerified(e);
	}

	public boolean isLeft() {
		return false;
	}

	public boolean isStatic() {
		return false;
	}
}
