package wmas.world.memory;

import wmas.expression.DereferenceableObject;
import wmas.xml.Copiable;

public interface Data extends Copiable, DereferenceableObject {
	public long getSize();

	public Number getNumber();

	public int nbSubData();

	public Data getSubData(int i);

	public boolean setSubData(int i, Data d);

	public Data copy();

	public void setMemory(Memory mem);

	public void setParent(Data parent);

	public void childChanged();
}
