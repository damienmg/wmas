package wmas.expression.functions;

import java.util.List;

import wmas.world.EntityInterface;

public abstract class AbstractFunction implements Function {

	public int getMaxNbArg() {
		return getMinNbArg();
	}

	public boolean isLeft() {
		return false;
	}

	public boolean isStatic() {
		return true;
	}

	public void affect(EntityInterface e, List<Object> args, Object v) {
	}
}
