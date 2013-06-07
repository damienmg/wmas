package wmas.network;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import wmas.expression.functions.Function;
import wmas.world.EntityInterface;
import wmas.world.memory.ArrayData;
import wmas.world.memory.Data;
import wmas.world.memory.Memory;

public class NetworkMessage extends ArrayData implements Function {
	private static Stack<NetworkMessage> pool = new Stack<NetworkMessage>();

	private static NetworkMessage getInstance(String r, List<Data> args) {
		if (pool.isEmpty())
			return new NetworkMessage(r, args);
		else {
			NetworkMessage nm = pool.pop();
			if (args != null)
				nm.setAll(args);
			nm.type = r;
			return nm;
		}
	}

	public static NetworkMessage getInstance(String r) {
		return getInstance(r, null);
	}

	private String type = "";

	public long getSize() {
		return getTypeSize() + super.getSize();
	}

	private static long getTypeSize() {
		return 2; // change it depending on the number of message type?
	}

	protected NetworkMessage(String t, List<Data> args) {
		type = t;
		if (args != null)
			setAll(args);
	}

	public String getType() {
		return type;
	}

	public void free() {
		clear();
		pool.push(this);
	}

	@Override
	public String toString() {
		String r = type + "(";
		boolean first = true;
		for (Data d : dataParts) {
			if (!first)
				r += ", ";
			first = false;
			r += d.toString();
		}
		return r + ")";
	}

	public void affect(EntityInterface e, List<Object> args, Object v) {
	}

	public int getMaxNbArg() {
		return Integer.MAX_VALUE;
	}

	public int getMinNbArg() {
		return 0;
	}

	public String getName() {
		return type;
	}

	public boolean isLeft() {
		return false;
	}

	public boolean isStatic() {
		return true;
	}

	public Object getValue(EntityInterface e, List<Object> args) {
		List<Data> r = new LinkedList<Data>();
		for (Object o : args) {
			r.add(Memory.createData(o));
		}
		return getInstance(this.type, r);
	}
}
