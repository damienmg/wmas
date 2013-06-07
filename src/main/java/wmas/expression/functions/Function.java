package wmas.expression.functions;

import java.util.List;

import wmas.world.EntityInterface;

public interface Function {
	public String getName();

	public int getMinNbArg();

	public int getMaxNbArg();

	public boolean isStatic(); // The result do not vary during the execution

	public Object getValue(EntityInterface e, List<Object> args);

	public boolean isLeft();

	public void affect(EntityInterface e, List<Object> args, Object v);
}
