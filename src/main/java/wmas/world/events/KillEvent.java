package wmas.world.events;

import wmas.world.EntityInterface;
import wmas.world.World;

public class KillEvent implements SimuEvent {

	@Override
	public boolean execute(World w, EntityInterface e) {
		if (e != null && e.getDrawingShape() != null) {
			e.setDrawingShape(null);
			if (w.getDisplay() != null) {
				w.transfer();
			}
			e.terminate();
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "kill";
	}

	public String toString() {
		return "kill";
	}

	static public void register() {
		ScheduledEvent.register(new KillEvent());
	}
}
