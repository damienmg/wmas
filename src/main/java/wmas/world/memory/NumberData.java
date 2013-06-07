package wmas.world.memory;

public abstract class NumberData extends Number implements Data {
	private static final long serialVersionUID = 1L;
	public static final long NUMBER_SIZE = 8;

	protected Memory parent;
	protected Data parentData;

	@Override
	public float floatValue() {
		return (float) this.doubleValue();
	}

	public int intValue() {
		return (int) this.longValue();
	}

	public long longValue() {
		return (long) this.doubleValue();
	}

	public double doubleValue() {
		return this.longValue();
	}

	public long getSize() {
		return NUMBER_SIZE;
	}

	public Number getNumber() {
		return this;
	}

	public Data getSubData(int i) {
		return null;
	}

	public int nbSubData() {
		return -1;
	}

	public boolean setSubData(int i, Data d) {
		return false;
	}

	public void setMemory(Memory mem) {
		this.parent = mem;
	}

	public void setParent(Data parent) {
		this.parentData = parent;
	}

	public void childChanged() {
	}

	public int getNbValues() {
		return 0;
	}

	public Object getValueAtIndex(int i) {
		return null;
	}

	public boolean isLeftValue() {
		return true;
	}

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

}
