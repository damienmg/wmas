package wmas.world.events;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class EventComboBoxModel implements ComboBoxModel {
	public EventComboBoxModel() {
		super();
		nullAvailables = false;
	}

	public EventComboBoxModel(boolean nA) {
		super();
		nullAvailables = nA;
	}

	boolean nullAvailables = false;
	SimuEvent selectedItem = null;

	List<SimuEvent> availables = new LinkedList<SimuEvent>();

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		if (anItem == null || anItem.toString().isEmpty())
			selectedItem = null;
		else if (anItem instanceof SimuEvent) {
			selectedItem = (SimuEvent) anItem;
		} else
			selectedItem = ScheduledEvent.availables.get(anItem.toString());
	}

	private HashSet<ListDataListener> listeners = new HashSet<ListDataListener>();

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	private void update() {
		if (availables.size() != ScheduledEvent.availables.size()) {
			int s = Math.max(availables.size(),
					ScheduledEvent.availables.size());
			availables.clear();
			availables.addAll(ScheduledEvent.availables.values());
			for (ListDataListener l : listeners) {
				l.contentsChanged(new ListDataEvent(this,
						ListDataEvent.CONTENTS_CHANGED, 0, s));
			}
		}
	}

	@Override
	public Object getElementAt(int index) {
		update();
		return (nullAvailables && index == 0) ? "" : (this.availables.get(index
				- (nullAvailables ? 1 : 0)));
	}

	@Override
	public int getSize() {
		update();
		return this.availables.size() + (nullAvailables ? 1 : 0);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

}
