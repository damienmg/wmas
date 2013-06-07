package wmas.world.memory;

public class DoubleData extends NumberData {
	private static final long serialVersionUID = 1L;

	private double data = 0;

	public void setNumber(Number l) {
		data = l.doubleValue();
	}

	public String toString() {
		return Double.toString(data);
	}

	public void setData(double d) {
		this.data = d;
	}

	public Data copy() {
		DoubleData d = new DoubleData();
		d.setData(data);
		return d;
	}

	public double doubleValue() {
		return data;
	}

}
