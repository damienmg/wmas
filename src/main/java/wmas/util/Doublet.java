package wmas.util;

public class Doublet<E1, E2> {
	protected E1 first;
	protected E2 second;

	public Doublet() {
		super();
		this.first = null;
		this.second = null;
	}

	public Doublet(Doublet<E1, E2> t) {
		super();
		this.first = t.first;
		this.second = t.second;
	}

	public Doublet(E1 first, E2 second) {
		super();
		this.first = first;
		this.second = second;
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(this.getClass())) {
			Doublet<E1, E2> d = (Doublet<E1, E2>) obj;
			if (d.first == null) {
				if (first != null)
					return false;
			} else if (!d.first.equals(first))
				return false;
			if (d.second == null)
				return second == null;
			return (d.second.equals(second));
		} else
			return false;
	}

	public String toString() {
		return "<" + first.toString() + "," + second.toString() + ">";
	}

}
