package wmas.expression;

public interface DereferenceableObject {
	public int getNbValues();

	public Object getValueAtIndex(int i);

	public boolean isLeftValue();

	public Object setValueAtIndex(int i, Object nV);
}
