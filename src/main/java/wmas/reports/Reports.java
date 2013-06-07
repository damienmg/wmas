package wmas.reports;

import java.util.Vector;

import wmas.world.EntityInterface;
import wmas.xml.Copiable;

public class Reports implements Copiable {
	Vector<Report> reports = new Vector<Report>();
	ReportDescription descr = null;

	public Reports(ReportDescription descr) {
		super();
		this.descr = descr;
	}

	public Reports() {
		super();
		this.descr = new ReportDescription();
	}

	public ReportDescription getDescription() {
		return this.descr;
	}

	public void clear() {
		reports.clear();
	}

	public void newRun() {
		if (descr != null)
			reports.add(new Report(descr));
	}

	public void addReport(double t, String name, String param, double value) {
		if (descr != null) {
			if (reports.size() == 0) {
				reports.add(new Report(descr));
			}
			reports.get(reports.size() - 1).addReport(t, name, param, value);
		}
	}

	public void addEvent(double t, String name, String param,
			EntityInterface entity) {
		if (descr != null) {
			if (reports.size() == 0) {
				reports.add(new Report(descr));
			}
			reports.get(reports.size() - 1).addEvent(t, name, param, entity);
		}
	}

	@Override
	public Reports copy() {
		return new Reports(descr);
	}

	public int getNbReport() {
		return reports.size();
	}
}
