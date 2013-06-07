package wmas.world.memory;

import wmas.expression.Expression;

public class ExpressionData implements Data {

	private Expression data = new Expression(0);

	public ExpressionData(Expression data) {
		super();
		this.data = data;
	}

	private Memory parent = null;
	private Data parentData = null;

	public long getSize() {
		return data != null ? data.getSize() : 0;
	}

	public Number getNumber() {
		return data != null ? data.getNumber() : 0;
	}

	public void setNumber(Number l) {
		data = new Expression(l);
	}

	public String toString() {
		return data == null ? "null" : data.toString();
	}

	public Data getSubData(int i) {
		return null;
	}

	public int nbSubData() {
		return -1;
	}

	public void setData(Expression e) {
		if (parent != null && e != null) {
			if (parent.getFreeSize() < (e.getSize() - getSize()))
				return;
		}
		this.data = e;
		if (parentData != null)
			parentData.childChanged();
	}

	public boolean setSubData(int i, Data d) {
		return false;
	}

	@Override
	public Data copy() {
		return new ExpressionData(data);
	}

	@Override
	public int getNbValues() {
		return 0;
	}

	@Override
	public Object getValueAtIndex(int i) {
		return null;
	}

	@Override
	public boolean isLeftValue() {
		return true;
	}

	public void setMemory(Memory mem) {
		this.parent = mem;
	}

	public void setParent(Data parent) {
		this.parentData = parent;
	}

	@Override
	public Object setValueAtIndex(int i, Object nV) {
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

	public void childChanged() {
	}

}
