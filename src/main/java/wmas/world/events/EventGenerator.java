package wmas.world.events;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.world.EntityInterface;
import wmas.world.Simulator;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class EventGenerator implements XMLEntity {

	List<ScheduledEvent> events = new LinkedList<ScheduledEvent>();
	private PriorityQueue<ScheduledEvent> eventQueue = new PriorityQueue<ScheduledEvent>();

	public double predictDuration() {
		double r = Double.POSITIVE_INFINITY;
		for (ScheduledEvent ev : eventQueue) {
			if (ev.evt instanceof StopEvent)
				r = Math.min(r, ev.getScheduledTime());
		}
		return r;
	}

	public synchronized void schedule(SimuEvent event, Simulator simu,
			EntityInterface e) {
		ScheduledEvent se = new ScheduledEvent(event, simu, e);
		eventQueue.offer(se);
	}

	public synchronized void init(Simulator simu) {
		eventQueue.clear();
		if (simu != null) {
			for (ScheduledEvent e : events) {
				if (e.reset(simu)) {
					eventQueue.offer(e);
				}
			}
		}
	}

	public synchronized void update() {
		while (!eventQueue.isEmpty() && eventQueue.peek().execute()) {
			eventQueue.poll();
		}
	}

	@Override
	public EventGenerator copy() {
		EventGenerator r = new EventGenerator();
		for (ScheduledEvent e : events) {
			r.events.add(e.copy());
		}
		return r;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		events.clear();
		eventQueue.clear();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) (nl.item(i));
				if (el.getTagName().equals("event")) {
					events.add(new ScheduledEvent(el));
				}
			}
		}
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		for (ScheduledEvent ev : events) {
			Element el = root.createElement("event");
			ev.setAttributes(el);
			e.appendChild(el);
		}
		return e;
	}
}