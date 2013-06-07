package wmas.world.events;

import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Element;

import wmas.world.EntityInterface;
import wmas.world.Simulator;
import wmas.xml.Copiable;

public class ScheduledEvent implements Comparable<ScheduledEvent>, Copiable {
	static HashMap<String, SimuEvent> availables = new HashMap<String, SimuEvent>();

	static public void register(SimuEvent evt) {
		availables.put(evt.getName(), evt);
	}

	static public void unregister(SimuEvent evt) {
		availables.remove(evt.getName());
	}

	private double scheduledTime = -1;
	private EntityInterface scheduledEntity = null;
	private Simulator parent = null;
	private boolean executed = false;

	SimuEvent evt = null;
	double minTime = 0;
	double maxTime = Double.POSITIVE_INFINITY;
	String namePattern = "";
	boolean reschedule = false;

	public ScheduledEvent() {
	}

	ScheduledEvent(Element el) {
		evt = el.hasAttribute("name") ? availables.get(el.getAttribute("name"))
				: null;
		minTime = Double.parseDouble(el.getAttribute("min_t"));
		maxTime = Double.parseDouble(el.getAttribute("max_t"));
		namePattern = el.getAttribute("entity");
		reschedule = el.hasAttribute("reschedule")
				&& el.getAttribute("reschedule").equals("1");
	}

	public ScheduledEvent(SimuEvent event, Simulator simu, EntityInterface e) {
		evt = event;
		parent = simu;
		scheduledTime = simu.getInternalTime();
		executed = false;
		scheduledEntity = e;
	}

	boolean reset(Simulator parent) {
		this.parent = parent;
		executed = false;
		if ((reschedule || scheduledTime < 0) && evt != null) {
			if (maxTime != Double.POSITIVE_INFINITY && maxTime > 0) {
				if (minTime >= 0)
					scheduledTime = Math.random() * (maxTime - minTime)
							+ minTime;
				else
					scheduledTime = Math.random();
				getEntity(parent);
			} else if (parent.getRunNumber() > 0) {
				if (minTime > 0)
					scheduledTime = Math.random()
							* (parent.getFirstRunDuration() - minTime)
							+ minTime;
				else
					scheduledTime = Math.random()
							* parent.getFirstRunDuration();
				getEntity(parent);
			} else {
				scheduledTime = -1;
				scheduledEntity = null;
			}
		}
		return (scheduledTime >= 0 && evt != null);
	}

	private void getEntity(Simulator parent) {
		scheduledEntity = null;
		if (parent.getWorld() != null) {
			Collection<EntityInterface> c = parent.getWorld()
					.getEntitiesByPrefix(namePattern);
			if (c != null && c.size() > 0) {
				int n = (int) (Math.random() * c.size());
				for (EntityInterface e : c) {
					if (n == 0) {
						scheduledEntity = e;
						return;
					}
					n--;
				}
			}
		}
	}

	boolean execute() {
		if (scheduledTime < 0 || executed)
			return true; // we quit the list in that case
		if (scheduledTime <= parent.getInternalTime()
				&& parent.getWorld() != null) {
			executed = true;
			if (evt.execute(parent.getWorld(), scheduledEntity)) {
				parent.getWorld().addEvent(parent.getInternalTime(), "event",
						evt.getName(), scheduledEntity);
			}
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(ScheduledEvent o) {
		if (o == null)
			return 1;
		return Double.compare(scheduledTime, o.scheduledTime);
	}

	@Override
	public ScheduledEvent copy() {
		ScheduledEvent r = new ScheduledEvent();
		r.evt = evt;
		r.minTime = minTime;
		r.maxTime = maxTime;
		r.namePattern = namePattern;
		r.reschedule = reschedule;
		return r;
	}

	void setAttributes(Element el) {
		if (evt != null)
			el.setAttribute("name", evt.getName());
		el.setAttribute("min_t", Double.toString(minTime));
		el.setAttribute("max_t", Double.toString(maxTime));
		el.setAttribute("entity", namePattern);
		el.setAttribute("reschedule", reschedule ? "1" : "0");
	}

	public double getScheduledTime() {
		return scheduledTime;
	}
}
