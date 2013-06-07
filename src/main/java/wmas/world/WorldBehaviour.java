package wmas.world;

import wmas.xml.Copiable;

public interface WorldBehaviour extends Copiable {
	public void init();

	public void update(double t);

	public WorldBehaviour copy();

	public void setWorld(World world);
}
