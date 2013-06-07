package wmas.world.memory;

public class VoidData implements Data {

	private VoidData() {
	}

	final public static VoidData instance = new VoidData();

	public long getSize() {
		return 0;
	}

	public Number getNumber() {
		return 0;
	}

	public void setNumber(Number l) {
	}

	public String toString() {
		return "void";
	}

	public Data getSubData(int i) {
		return null;
	}

	public int nbSubData() {
		return -1;
	}

	public void setData(String d) {
	}

	public boolean setSubData(int i, Data d) {
		return false;
	}

	public Data copy() {
		return this;
	}

	public int getNbValues() {
		return 0;
	}

	public Object getValueAtIndex(int i) {
		return null;
	}

	public boolean isLeftValue() {
		return false;
	}

	public void setMemory(Memory mem) {
	}

	public void setParent(Data parent) {
	}

	public Object setValueAtIndex(int i, Object nV) {
		return null;
	}

	public void childChanged() {
	}

}
