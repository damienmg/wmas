package wmas.world.events;

import wmas.world.EntityInterface;
import wmas.world.World;

public interface SimuEvent {
	public boolean execute(World w, EntityInterface e);

	public String getName();
}
