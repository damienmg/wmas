package wmas.behaviour.simple;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourData;
import wmas.world.memory.ArrayData;
import wmas.world.memory.Data;
import wmas.world.memory.Memory;

public class SimpleBehaviourData<E extends Data> implements BehaviourData {
	private Behaviour behaviour;
	private E data;

	public SimpleBehaviourData(Behaviour behaviour, E data) {
		super();
		this.behaviour = behaviour;
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public Data copy() {
		return new SimpleBehaviourData<E>(behaviour, (E) data.copy());
	}

	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	public Behaviour createBehaviour() {
		return behaviour.copy();
	}

	public long getSize() {
		return data.getSize() + 1;
	}

	public String toString() {
		return "Data[s=" + getSize() + ", behaviour=" + behaviour + "]";
	}

	public void childChanged() {
		data.childChanged();
	}

	public Number getNumber() {
		return data.getNumber();
	}

	public Data getSubData(int i) {
		return data.getSubData(i);
	}

	public int nbSubData() {
		return data.nbSubData();
	}

	public boolean setSubData(int i, Data d) {
		return data.setSubData(i, d);
	}

	public void setMemory(Memory mem) {
		data.setMemory(mem);
		parent = mem;
	}

	public void setParent(Data parent) {
		data.setParent(parent);
		parentData = parent;
	}

	public int getNbValues() {
		return data.getNbValues();
	}

	public Object getValueAtIndex(int i) {
		return data.getValueAtIndex(i);
	}

	public boolean isLeftValue() {
		return true;
	}

	private Memory parent;
	private Data parentData;

	public Object setValueAtIndex(int i, Object nV) {
		if (data.isLeftValue()) {
			Object o = data.setValueAtIndex(i, nV);
			if (o != null && o != data)
				return o;
			if (o != null)
				return this;
		}
		ArrayData d = new ArrayData();
		d.setValueAtIndex(i, nV);
		if (this.parent != null) {
			if (d.getSize() - getSize() > this.parent.getFreeSize())
				return this;
		}
		d.setParent(parentData);
		d.setMemory(parent);
		return d;
	}

	public E getData() {
		return data;
	}

	public void setData(E data) {
		this.data = data;
	}
}
