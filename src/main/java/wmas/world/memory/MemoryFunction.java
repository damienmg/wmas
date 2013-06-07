package wmas.world.memory;

import java.util.List;

import wmas.expression.DereferenceableObject;
import wmas.expression.functions.AbstractNumericFunction;
import wmas.expression.functions.Function;
import wmas.world.EntityInterface;

public class MemoryFunction implements Function {

	@Override
	public int getMaxNbArg() {
		return 1;
	}

	@Override
	public int getMinNbArg() {
		return 1;
	}

	@Override
	public String getName() {
		return "mem";
	}

	@Override
	public Object getValue(EntityInterface e, List<Object> args) {
		int index = AbstractNumericFunction.getNumberFromArgument(args.get(0))
				.intValue();
		if (e == null)
			return 0;
		if (e.hasAttribute("memory")) {
			Memory mem = (Memory) e.getAttribute("memory");
			args.remove(0);
			return mem.getData(index);
		}
		return 0;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	private static Data getData(Object v) {
		if (v instanceof Data) {
			return ((Data) v);
		} else if (v instanceof Number) {
			return Memory.createNumberData((Number) v);
		} else if (v instanceof DereferenceableObject) {
			return new ArrayData((DereferenceableObject) v);
		} else {
			StringData d = new StringData();
			d.setData(v.toString());
			return d;
		}
	}

	@Override
	public void affect(EntityInterface e, List<Object> args, Object v) {
		if (e == null)
			return;
		if (e.hasAttribute("memory")) {
			Memory mem = (Memory) e.getAttribute("memory");
			int index = AbstractNumericFunction.getNumberFromArgument(
					args.get(0)).intValue();
			if (index < 0)
				index = 0;
			if (v == null
					|| (v instanceof Number && ((Number) v).doubleValue() == 0)) {
				mem.delete(index);
			} else
				mem.add(index, getData(v));
		}
	}

	@Override
	public boolean isLeft() {
		return true;
	}

}
