package wmas.world.events;

import javax.swing.table.AbstractTableModel;

public class EventTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	EventGenerator eventGenerator = null;
	boolean modified = false;

	public void setGenerator(EventGenerator gen) {
		eventGenerator = gen;
		modified = false;
		fireTableDataChanged();
	}

	public void add() {
		eventGenerator.events.add(new ScheduledEvent());
		modified = true;
		fireTableRowsInserted(eventGenerator.events.size(),
				eventGenerator.events.size());
	}

	public void del(int index) {
		if (index >= 0 && index < eventGenerator.events.size()) {
			eventGenerator.events.remove(index);
			modified = true;
			fireTableRowsDeleted(index, index);
		}
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return eventGenerator == null ? 0 : eventGenerator.events.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (eventGenerator == null)
			return null;
		if (rowIndex < 0 || rowIndex >= eventGenerator.events.size())
			return null;
		ScheduledEvent ev = eventGenerator.events.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return ev.evt;
		case 1:
			return (ev.minTime <= 0 ? 0 : ev.minTime);
		case 2:
			return (ev.maxTime <= 0 ? "0"
					: ((ev.maxTime == Double.POSITIVE_INFINITY) ? "∞"
							: Double.toString(ev.maxTime)));
		case 3:
			return ev.namePattern;
		case 4:
			return ev.reschedule;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (eventGenerator == null)
			return;
		if (rowIndex < 0 || rowIndex >= eventGenerator.events.size())
			return;
		ScheduledEvent ev = eventGenerator.events.get(rowIndex);
		switch (columnIndex) {
		case 0:
			ev.evt = (aValue instanceof SimuEvent) ? ((SimuEvent) aValue)
					: (ScheduledEvent.availables.get(aValue.toString()));
			break;
		case 1:
			try {
				ev.minTime = Double.parseDouble(aValue.toString());
				if (ev.minTime < 0)
					ev.minTime = 0;
			} catch (NumberFormatException ex) {
				return;
			}
			break;
		case 2:
			String s = aValue.toString().trim();
			if (s.isEmpty())
				return;
			if (s.charAt(0) == '+')
				s = s.substring(1).trim();
			if (s.equals("∞")) {
				ev.maxTime = Double.POSITIVE_INFINITY;
			} else {
				try {
					ev.maxTime = Double.parseDouble(aValue.toString());
					if (ev.maxTime < 0)
						ev.maxTime = Double.POSITIVE_INFINITY;
				} catch (NumberFormatException ex) {
					return;
				}
			}
			break;
		case 3:
			ev.namePattern = aValue.toString().trim();
			break;
		case 4:
			if (!(aValue instanceof Boolean))
				return;
			ev.reschedule = (Boolean) aValue;
			break;
		default:
			return;
		}
		modified = true;
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Event";
		case 1:
			return "Minimum time";
		case 2:
			return "Maximum time";
		case 3:
			return "Entity name prefix";
		case 4:
			return "Reschedule?";
		}
		return super.getColumnName(column);
	}

	public boolean isModified() {
		return modified;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return SimuEvent.class;
		case 1:
			return Double.class;
		case 2:
			return String.class;
		case 3:
			return String.class;
		case 4:
			return Boolean.class;
		}
		return super.getColumnClass(column);
	}

}
