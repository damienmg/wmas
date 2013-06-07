package wmas.world.events;

import wmas.world.EntityInterface;
import wmas.world.World;

public class StopEvent implements SimuEvent {

	@Override
	public boolean execute(World w, EntityInterface e) {
		if (w != null && w.getSimulator() != null
				&& !(w.getSimulator().isStopped())) {
			w.getSimulator().Stop();
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "stop";
	}

	public String toString() {
		return "stop";
	}

	static public void register() {
		ScheduledEvent.register(new StopEvent());
	}
}
