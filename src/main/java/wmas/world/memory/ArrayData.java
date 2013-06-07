package wmas.world.memory;

import java.util.ArrayList;
import java.util.Collection;

import wmas.expression.DereferenceableObject;

public class ArrayData implements Data {

	protected ArrayList<Data> dataParts = new ArrayList<Data>();
	protected int size = 0;
	private Memory parent = null;
	private Data parentData = null;

	public ArrayData() {
	}

	public ArrayData(DereferenceableObject o) {
		for (int i = 0; i < o.getNbValues(); i++) {
			dataParts.add(Memory.createData(o.getValueAtIndex(i)));
			size += dataParts.get(i).getSize();
			dataParts.get(i).setParent(this);
			dataParts.get(i).setMemory(parent);
		}
	}

	@Override
	public Number getNumber() {
		return 0;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public Data getSubData(int i) {
		return dataParts.get(i);
	}

	@Override
	public int nbSubData() {
		return dataParts.size();
	}

	public void setData(int i, Data d) {
		long deltaSize = d.getSize() + Math.max(i - dataParts.size(), 0) * 8;
		if (dataParts.size() > i) {
			deltaSize -= dataParts.get(i).getSize();
			if (d == dataParts.get(i))
				return;
		}
		if (parent != null) {
			if (deltaSize > parent.getFreeSize())
				return;
		}
		if (dataParts.size() <= i) {
			while (dataParts.size() < i) {
				dataParts.add(new LongData());
			}
			dataParts.add(d);
		} else {
			dataParts.get(i).setParent(null);
			dataParts.set(i, d);
		}
		d.setParent(this);
		d.setMemory(parent);
		size += deltaSize;
		if (parentData != null)
			parentData.childChanged();
	}

	public void clear() {
		dataParts.clear();
		size = 0;
	}

	public boolean setSubData(int i, Data d) {
		setData(i, d);
		return true;
	}

	public void setAll(Collection<? extends Data> col) {
		dataParts.clear();
		size = 0;
		for (Data d : col) {
			dataParts.add(d);
			size += d.getSize();
		}
	}

	protected void copy(ArrayData r) {
		int i = 0;
		for (Data d : dataParts) {
			r.setData(i, d.copy());
			i++;
		}
	}

	@Override
	public Data copy() {
		ArrayData r = new ArrayData();
		copy(r);
		return r;
	}

	public String toString() {
		return dataParts.toString();
	} // XXX return "Array[n=" + dataParts.size() + ", s=" + size + "]"; }

	@Override
	public int getNbValues() {
		return nbSubData();
	}

	@Override
	public Object getValueAtIndex(int i) {
		return (i >= 0 && i < dataParts.size()) ? getSubData(i) : 0;
	}

	@Override
	public boolean isLeftValue() {
		return true;
	}

	@Override
	public void childChanged() {
		size = 0;
		for (Data d : dataParts)
			size += d.getSize();
		if (parentData != null)
			parentData.childChanged();
	}

	@Override
	public void setMemory(Memory mem) {
		for (Data d : dataParts)
			d.setMemory(mem);
		this.parent = mem;
	}

	@Override
	public void setParent(Data parent) {
		this.parentData = parent;
	}

	@Override
	public Object setValueAtIndex(int i, Object nV) {
		if (getValueAtIndex(i) != nV)
			setData(i, Memory.createData(nV));
		return this;
	}

}
