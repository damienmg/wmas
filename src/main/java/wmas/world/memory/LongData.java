package wmas.world.memory;

public class LongData extends NumberData {
	private static final long serialVersionUID = 1L;

	private long data = 0;

	public void setNumber(Number l) {
		data = l.longValue();
	}

	public String toString() {
		return Double.toString(data);
	}

	public void setData(long d) {
		this.data = d;
	}

	public Data copy() {
		LongData d = new LongData();
		d.setData(data);
		return d;
	}

	public long longValue() {
		return data;
	}
}
