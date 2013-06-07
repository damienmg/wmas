package wmas.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import wmas.world.EntityInterface;

public class Report {
	class TimedReport implements Comparable<TimedReport> {
		double t;

		@Override
		public int compareTo(TimedReport o) {
			if (o == null)
				return 1;
			if (t == o.t)
				return (hashCode() - o.hashCode());
			return Double.compare(t, o.t);
		}

		@Override
		public int hashCode() {
			return (new Double(t)).hashCode();
		}
	}

	class EventReport extends TimedReport {
		String name;
		String entity;

		public int hashCode() {
			if (entity != null)
				return name.hashCode() ^ entity.hashCode() ^ super.hashCode();
			return name.hashCode() ^ super.hashCode();
		}
	}

	class DataReport extends TimedReport {
		String name;
		double value;

		public int hashCode() {
			return (new Double(value).hashCode()) ^ name.hashCode()
					^ super.hashCode();
		}
	}

	SortedSet<EventReport> events = new TreeSet<EventReport>();
	Map<String, SortedSet<DataReport>> data = new HashMap<String, SortedSet<DataReport>>();
	SortedSet<TimedReport> allReports = new TreeSet<TimedReport>();
	ReportDescription descriptor;

	public Report(ReportDescription descr) {
		this.descriptor = descr;
	}

	public void clear() {
		events.clear();
		data.clear();
		allReports.clear();
	}

	public void addReport(double t, String name, String param, double value) {
		if (descriptor != null) {
			String nName = name;
			if (param != null && !param.isEmpty())
				nName += " - " + param;
			if (descriptor.storedReport.containsKey(name)
					&& descriptor.storedReport.get(name).containsKey(param)) {
				if (!data.containsKey(nName)) {
					data.put(nName, new TreeSet<DataReport>());
				}
				if (!data.get(nName).isEmpty()
						&& ReportDescription.isCumulative(name)) {
					value += data.get(nName).last().value;
				}
				if (descriptor.isMultiple(name, param)
						|| data.get(nName).isEmpty()) {
					DataReport report = new DataReport();
					report.t = t;
					report.name = nName;
					report.value = value;
					data.get(nName).add(report);
					allReports.add(report);
				} else {
					data.get(nName).last().t = t;
					data.get(nName).last().value = value;
				}
			}
		}
	}

	public void addEvent(double t, String name, String param,
			EntityInterface entity) {
		if (descriptor != null) {
			if (descriptor.storedEvent.contains(name)) {
				String nName = name;
				if (param != null && !param.isEmpty())
					nName += " - " + param;
				EventReport report = new EventReport();
				report.t = t;
				report.name = nName;
				report.entity = entity == null ? "" : entity.getName();
				events.add(report);
				allReports.add(report);
			}
		}
	}
}