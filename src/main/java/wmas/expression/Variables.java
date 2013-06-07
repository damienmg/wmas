package wmas.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import wmas.expression.functions.AbstractNumericFunction;
import wmas.world.memory.ArrayData;

public class Variables {
	private HashMap<String, Object> variables = new HashMap<String, Object>(); // To
																				// store
																				// variables

	public void init() {
		variables.clear();
	}

	public Object getValue(String var) {
		if (!variables.containsKey(var)) {
			return 0;
		}
		return variables.get(var);
	}

	static Object dereferences(Object parent, List<Object> indexes) {
		while (indexes != null && indexes.size() > 0) {
			int i = AbstractNumericFunction.getNumberFromArgument(
					indexes.remove(0)).intValue();
			if (parent != null) {
				if (parent instanceof DereferenceableObject) {
					DereferenceableObject deref = ((DereferenceableObject) parent);
					parent = deref.getValueAtIndex(i);
				} else
					return 0;
			} else
				return 0;
		}
		return parent == null ? 0 : parent;
	}

	static Object affectDeref(Object parent, Object value, List<Object> indexes) {
		if (indexes.size() == 0)
			return value;
		int i = AbstractNumericFunction
				.getNumberFromArgument(indexes.remove(0)).intValue();
		if (parent != null) {
			if (parent instanceof DereferenceableObject) {
				DereferenceableObject deref = ((DereferenceableObject) parent);
				Object newValue = affectDeref(deref.getValueAtIndex(i), value,
						indexes);
				deref.setValueAtIndex(i, newValue);
				return parent;
			}
		}
		ArrayData d = new ArrayData();
		d.setValueAtIndex(i, affectDeref((Object) null, value, indexes));
		return d;
	}

	public void affect(String var, Object value, List<Object> indexes) {
		if (indexes == null || indexes.size() == 0)
			variables.put(var, value);
		else {
			variables.put(var, affectDeref(variables.get(var), value, indexes));
		}
	}

	public void clear() {
		variables.clear();
	}

	public Set<String> getNames() {
		return variables.keySet();
	}
}
