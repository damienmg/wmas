package wmas.world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wmas.reports.ReportDescription;
import wmas.reports.Reports;
import wmas.util.Util;
import wmas.world.events.EventGenerator;
import wmas.world.events.SimuEvent;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class Simulator implements ActionListener, XMLEntity {

	// Configuration
	protected double precision; // in seconds
	protected double speed;
	protected boolean noTimer = false;
	protected int nbRuns = 1;
	protected EventGenerator events = new EventGenerator();

	// Run-time configuration
	protected boolean real_time = false;

	// Execution
	protected World world;
	protected UpdateManager display = null;
	protected double internaltime;
	protected long initialtime;
	protected long systemtime;
	protected boolean stopped;
	protected boolean stepExecution = false;
	protected boolean lastStepFinished = true;
	protected Timer timer;
	protected Reports reporter = new Reports();
	protected int runNumber = 0;
	protected int realRunNumber = 0;
	private double firstRunDuration = 0;

	public Simulator() {
		world = new World();
		precision = 0.05;
		systemtime = 0;
		initialtime = 0;
		internaltime = 0;
		speed = 1;
		stopped = true;
		timer = new Timer(Util.toInteger(precision * 1000), this);
		timer.stop();
		timer.setRepeats(true);
	}

	public void setRealTime(boolean rt) {
		real_time = rt;
	}

	public boolean isRealTime() {
		return real_time;
	}

	public Simulator(World world, UpdateManager display) {
		this.world = world;
		precision = 1 / 20;
		systemtime = 0;
		initialtime = 0;
		internaltime = 0;
		speed = 1;
		this.display = display;
		stopped = true;
		timer = new Timer(getTimerDelay(), this);
		timer.stop();
		timer.setRepeats(true);
	}

	private int getTimerDelay() {
		return (int) (precision * 1000 / speed);
	}

	private void setDelay() {
		timer.setDelay(getTimerDelay());
		timer.setInitialDelay(getTimerDelay());
	}

	public double getSpeed() {
		return speed;
	}

	public synchronized void setSpeed(double speed) {
		if (speed > 0 && speed != this.speed) {
			this.speed = speed;
			setDelay();
		}
	}

	public UpdateManager getDisplay() {
		return display;
	}

	public synchronized void setDisplay(UpdateManager display) {
		this.display = display;
		if (world != null)
			world.setDisplay(display);
	}

	public double getPrecision() {
		return precision;
	}

	public synchronized void setPrecision(double precision) {
		if (precision > 0 && precision != this.precision) {
			this.precision = precision;
			setDelay();
		}
	}

	public World getWorld() {
		return world;
	}

	public synchronized void setWorld(World world) {
		this.world = world;
		this.world.setDisplay(display);
	}

	public double getInternalTime() {
		return internaltime;
	}

	private void initOnce() {
		if (reporter != null) {
			if (runNumber == 0 || runNumber >= nbRuns) {
				reporter.clear();
				firstRunDuration = 0;
			}
			reporter.newRun();
		}
		realRunNumber = runNumber;
		stopped = false;
		internaltime = 0;
		initialtime = java.lang.System.nanoTime();
		if (display != null)
			display.prepareReset();
		world.reset();
		if (display != null)
			display.reset();
		systemtime = java.lang.System.nanoTime();
		lastStepFinished = true;
		world.init(this, stepExecution);
		events.init(this);
		runNumber++;
		if (display != null)
			display.update();
	}

	private void terminate() {
		timer.stop();
		stopped = true;
		world.addReport(internaltime, "duration", "", internaltime);
		systemtime = java.lang.System.nanoTime();
		world.addReport(internaltime, "time", "",
				(double) ((systemtime - initialtime) / 1000000000.0));
		if (firstRunDuration <= 0)
			firstRunDuration = internaltime;
	}

	private void runOneCycle() {
		if (!stopped) {
			double delta = precision;
			long nsystemtime = java.lang.System.nanoTime();
			if (real_time) {
				delta = ((nsystemtime - systemtime) * speed / 1000000000.0);
			}
			systemtime = nsystemtime;
			try {
				if (lastStepFinished) {
					internaltime += delta;
					events.update();
				}
				if (!stepExecution) {
					if (!lastStepFinished) {
						while (!(world.updateStep(internaltime)))
							;
						lastStepFinished = true;
					} else
						world.update(internaltime);
				} else {
					lastStepFinished = world.updateStep(internaltime);
				}
				if (lastStepFinished) {
					if (world.terminated()) {
						terminate();
					}
				}
			} catch (Exception exn) {
				world.addEvent(internaltime, "error", exn.getMessage(), null);
				exn.printStackTrace();
				terminate();
			}
			if (display != null) {
				if (stopped)
					display.terminated();
				else
					display.update();
			}
		} else
			timer.stop();
	}

	private void runOnce(UpdateManager manager) {
		while (!stopped) {
			runOneCycle();
			if (manager != null)
				manager.update();
		}
	}

	public synchronized void Run() {
		if (stopped) {
			runNumber = 0;
			if (noTimer) {
				while (runNumber < nbRuns) {
					initOnce();
					runOnce(null);
				}
			} else {
				initOnce();
				setDelay();
				timer.restart();
			}
		}
	}

	public synchronized void Run(UpdateManager manager) {
		// Run inside a swing worker
		runNumber = 0;
		while (runNumber < nbRuns) {
			initOnce();
			int r = runNumber;
			if (manager != null)
				manager.update();
			runOnce(manager);
			if (runNumber != r) {
				runNumber = r;
				return;
			}
		}
	}

	public synchronized void Pause() {
		if (!stopped && timer.isRunning()) {
			timer.stop();
			actionPerformed(null);
		}
	}

	public synchronized void Stop() {
		if (!stopped) {
			terminate();
		}
	}

	public synchronized void Resume() {
		if (!stopped && !(timer.isRunning())) {
			systemtime = java.lang.System.nanoTime();
			actionPerformed(null);
			if (!noTimer)
				timer.restart();
		}
	}

	public boolean isStopped() {
		return stopped;
	}

	public boolean isPaused() {
		return !stopped && !timer.isRunning();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		runOneCycle();
		if (stopped && runNumber < nbRuns) {
			initOnce();
			timer.restart();
		}
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		world = null;
		precision = Double.parseDouble(e.getAttribute("precision"));
		systemtime = 0;
		internaltime = 0;
		speed = Double.parseDouble(e.getAttribute("speed"));
		nbRuns = e.hasAttribute("runs") ? Integer.parseInt(e
				.getAttribute("runs")) : 1;
		stopped = true;
		setDelay();
		NodeList nl = e.getChildNodes();
		for (int i = 0; (i < nl.getLength()) && (world == null); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element n = (Element) nl.item(i);
				XMLEntity xe = XMLInterpretor.convert(n, refs);
				if (xe instanceof EventGenerator) {
					events = (EventGenerator) xe;
				} else if (xe instanceof ReportDescription) {
					reporter = new Reports((ReportDescription) xe);
				}
			}
		}
		if (world == null)
			world = new World();
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.appendChild(reporter.getDescription().toXML(root, refs));
		e.setAttribute("precision", Double.toString(precision));
		e.setAttribute("runs", Integer.toString(nbRuns));
		e.setAttribute("speed", Double.toString(speed));
		e.appendChild(events.toXML(root, refs));
		return e;
	}

	public void setNoTimer(boolean noTimer) {
		this.noTimer = noTimer;
	}

	public Simulator copy() {
		Simulator pr = new Simulator((World) world.copy(), display);
		pr.precision = precision;
		pr.systemtime = 0;
		pr.internaltime = 0;
		pr.speed = speed;
		pr.stopped = true;
		pr.nbRuns = nbRuns;
		pr.reporter = new Reports(reporter.getDescription().copy());
		pr.setDelay();
		pr.events = events.copy();
		return pr;
	}

	public Simulator copy(World parent) {
		Simulator pr = new Simulator(parent, display);
		pr.precision = precision;
		pr.systemtime = 0;
		pr.internaltime = 0;
		pr.speed = speed;
		pr.stopped = true;
		pr.nbRuns = nbRuns;
		pr.reporter = new Reports(reporter.getDescription().copy());
		pr.setDelay();
		pr.events = events.copy();
		return pr;
	}

	public Reports getReporter() {
		return reporter;
	}

	public void setReporter(ReportDescription descr) {
		reporter = new Reports(descr);
	}

	public int getNbRuns() {
		return nbRuns;
	}

	public void setNbRuns(int nbRuns) {
		this.nbRuns = nbRuns;
		if (runNumber >= nbRuns && !stopped)
			Stop();
	}

	public int getRealRunNumber() {
		return realRunNumber;
	}

	public int getRunNumber() {
		return runNumber;
	}

	public double getFirstRunDuration() {
		return firstRunDuration;
	}

	public EventGenerator getEvents() {
		return events;
	}

	public double predictDuration() {
		double d = events.predictDuration();
		if (firstRunDuration > 0)
			return Math.min(d, firstRunDuration);
		return d;
	}

	public void Kill() {
		stopped = true;
		runNumber = nbRuns + 1;
	}

	public synchronized void Event(SimuEvent event, EntityInterface e) {
		events.schedule(event, this, e);
	}

	public boolean isStepExecution() {
		return stepExecution;
	}

	public void setStepExecution(boolean stepExecution) {
		this.stepExecution = stepExecution;
	}
}
