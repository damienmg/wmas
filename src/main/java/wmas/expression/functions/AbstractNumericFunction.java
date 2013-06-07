package wmas.expression.functions;

import java.util.ArrayList;
import java.util.List;

import wmas.world.EntityInterface;
import wmas.world.memory.Data;

public abstract class AbstractNumericFunction extends AbstractFunction {

	public static Number getNumberFromArgument(Object o) {
		if (o instanceof Number)
			return (Number) o;
		if (o instanceof Boolean)
			return ((Boolean) o) ? 1 : 0;
		if (o instanceof Data)
			return ((Data) o).getNumber();
		return 0;
	}

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

	public abstract Number getNumberValue(EntityInterface e, List<Number> args);

	@Override
	public Object getValue(EntityInterface e, List<Object> args) {
		ArrayList<Number> argNumber = new ArrayList<Number>();
		for (Object o : args) {
			argNumber.add(getNumberFromArgument(o));
		}
		return getNumberValue(e, argNumber);
	}

}
