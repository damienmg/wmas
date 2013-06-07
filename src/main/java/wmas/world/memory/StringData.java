package wmas.world.memory;

public class StringData implements Data {

	private String data = "";

	static final int charsize = 4;
	private Memory parent = null;
	private Data parentData = null;

	public StringData() {
	}

	public StringData(String string) {
		this.data = string;
	}

	public long getSize() {
		return charsize * data.length();
	}

	public Number getNumber() {
		try {
			return Long.parseLong(data);
		} catch (NumberFormatException ex) {
			try {
				return Double.parseDouble(data);
			} catch (NumberFormatException ex2) {
				return 0;
			}
		}
	}

	public void setNumber(Number l) {
		String str;
		if (l.doubleValue() == l.longValue())
			str = Long.toString(l.longValue());
		else
			str = Double.toString(l.doubleValue());
		setData(str);
	}

	public String toString() {
		return data;
	}

	public Data getSubData(int i) {
		return null;
	}

	public int nbSubData() {
		return -1;
	}

	public void setData(String d) {
		if (parent != null) {
			if (parent.getFreeSize() < (d.length() - data.length()) * charsize)
				return;
		}
		this.data = d;
		if (parentData != null)
			parentData.childChanged();
	}

	public boolean setSubData(int i, Data d) {
		return false;
	}

	@Override
	public Data copy() {
		StringData d = new StringData();
		d.setData(data);
		return d;
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

	public static long stringSize(String str) {
		return charsize * str.length();
	}

}
