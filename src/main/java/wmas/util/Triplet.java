package wmas.util;

public class Triplet<E1, E2, E3> {
	protected E1 first;
	protected E2 second;
	protected E3 third;

	public Triplet() {
		super();
		this.first = null;
		this.second = null;
		this.third = null;
	}

	public Triplet(E1 first, E2 second, E3 third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public E1 getFirst() {
		return first;
	}

	public void setFirst(E1 first) {
		this.first = first;
	}

	public E2 getSecond() {
		return second;
	}

	public void setSecond(E2 second) {
		this.second = second;
	}

	public E3 getThird() {
		return third;
	}

	public void setThird(E3 third) {
		this.third = third;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(this.getClass())) {
			Triplet<E1, E2, E3> d = (Triplet<E1, E2, E3>) obj;
			if (d.first == null) {
				if (first != null)
					return false;
			} else if (!d.first.equals(first))
				return false;
			if (d.second == null) {
				if (second != null)
					return false;
			} else if (!d.second.equals(second))
				return false;
			if (d.third == null)
				return third == null;
			return (d.third.equals(third));
		} else
			return false;
	}

	public String toString() {
		return "<" + first.toString() + "," + second.toString() + ","
				+ third.toString() + ">";
	}
}
