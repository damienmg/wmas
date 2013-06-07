package wmas;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.behaviour.physical.PhysicalFactory;
import wmas.behaviour.simple.SimpleFunctions;
import wmas.expression.functions.ArithmeticOperators;
import wmas.expression.functions.FunctionList;
import wmas.expression.functions.LogicalOperators;
import wmas.gui.behaviour.simple.SimpleBehaviourFactory;
import wmas.gui.shapes.SchemeView;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.entity.EntityEditor;
import wmas.gui.world.simple.SimpleEntityShapeFactory;
import wmas.network.NetworkFactory;
import wmas.reports.ReportDescription;
import wmas.reports.ReportsTableModel;
import wmas.world.Simulator;
import wmas.world.UpdateManager;
import wmas.world.World;
import wmas.world.events.KillEvent;
import wmas.world.events.StopEvent;
import wmas.world.functions.WorldFunctionAttributeFactory;
import wmas.world.memory.MemoryFactory;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class Main {
	private static final String programName = "WMAS";
	private static final String longProgramName = "Wireless Mobile Agent Simulator";
	public static final String aboutString = programName
			+ " - "
			+ longProgramName
			+ "\n"
			+ "Copyright (c) 2010-2013 CNRS, INRIA - Damien Martin-Guillerez\n"
			+ "This program is licenced under the CeCILL-B licence v1\n"
			+ "Please go to http://cecill.info/licences/Licence_CeCILL-B_V1-en.html for more informations.";

	private static void registerAll() {
		StopEvent.register();
		KillEvent.register();
		ReportDescription.registerDataReport("duration");
		ReportDescription.registerDataReport("time");
		ReportDescription.registerEventReport("event");
		BehaviourGraphFactory.register(new SimpleBehaviourFactory());
		EntityEditor.registerShapeFactory(new SimpleEntityShapeFactory());
		ArithmeticOperators.registerAll();
		SimpleFunctions.registerAll();
		LogicalOperators.registerAll();
		PhysicalFactory.registerAll();
		MemoryFactory.registerAll();
		NetworkFactory.registerAll();
		WorldFunctionAttributeFactory.registerAll();
	}

	private static boolean silent = false;
	private static File file = null;
	private static File report = null;
	private static boolean excel = true;
	private static String exec = "";

	public static void parse(String[] args) {
		boolean parseArgs = true;
		for (int i = 0; i < args.length; i++) {
			if (parseArgs && args[i].charAt(0) == '-' && args[i].length() > 2) {
				if (args[i].equals("--")) {
					parseArgs = false;
				} else if (args[i].equals("--silent") || args[i].equals("-s")) {
					silent = true;
				} else if (args[i].charAt(1) == '-') {
					if (args[i].equals("--exec")) {
						i++;
						if (i >= args.length)
							usage();
						exec = args[i];
					} else if (args[i].equals("--out")) {
						i++;
						if (i >= args.length)
							usage();
						report = new File(args[i]);
						excel = (args[i].toLowerCase().endsWith(".xls"));
					}

				} else if (args[i].startsWith("-e")) {
					exec = args[i].substring(2);
				} else if (args[i].startsWith("-o")) {
					report = new File(args[i].substring(2));
					excel = (args[i].toLowerCase().endsWith(".xls"));
				} else
					usage();
			} else {
				file = new File(args[i]);
			}
		}
	}

	private static void usage() {
		System.err
				.println("Syntax: progname [options] [filename]\n"
						+ "Options are:\n"
						+ "\t--silent|-s        Run with no output\n"
						+ "\t--out file|-ofile  Do a report at the end into specified file (.xls for\n"
						+ "\t                   MS Excel format, .csv for CSV format).\n"
						+ "\t--exec simu|-esimu Execute given simulation. If not specified, the graphical\n"
						+ "\t                   interface will run and other option except file to load\n"
						+ "\t                   will be ignored.\n");
		System.exit(-1);
	}

	public static void main(String[] args) {
		registerAll();
		parse(args);
		if (exec != null && !exec.isEmpty() && file != null && file.exists()) {
			run();
		} else if (file != null && file.exists())
			WorldEditor.run(programName, file);
		else
			WorldEditor.run(programName);
	}

	private static void run() {
		World world = null;
		try {
			FunctionList.clearSession();
			XMLEntity root = XMLInterpretor.convert(file.getCanonicalPath(),
					null);
			if (!(root instanceof World)) {
				System.err.println("Specified file is not a world file!\n");
				System.exit(-1);
			}
			world = (World) root;
			if (!(world.listSimulators().contains(exec))) {
				System.err.println("Execution script '" + exec
						+ "' does not exist int the specified world file!\n");
				System.exit(-1);
			}
		} catch (Exception exn) {
			System.err
					.println("Error while loading world file (probably a wrong file format)!\n");
			exn.printStackTrace();
			System.exit(-1);
		}
		try {
			final Simulator simu = world.getSimulator(exec);
			simu.setWorld(world);
			simu.Run(silent ? null : new UpdateManager() {
				long lastTime;

				public void update() {
					long nTime = System.currentTimeMillis();
					if (nTime - lastTime > 1000) {
						lastTime = nTime;
						System.out.println("Run " + simu.getRunNumber() + " / "
								+ simu.getNbRuns() + ", t = "
								+ simu.getInternalTime());
						System.out.flush();
					}
				}

				public void reset() {
					System.out.println("Resetting, run " + simu.getRunNumber()
							+ " / " + simu.getNbRuns());
					System.out.flush();
				}

				public void terminated() {
				}

				public void prepareReset() {
				}

				public SchemeView getWorldView() {
					return null;
				}

				public void addWorldButton(Component comp) {
				}
			});
			if (!silent) {
				System.out.println("finished!\n");
				System.out.flush();
			}
			if (report != null) {
				ReportsTableModel tModel = new ReportsTableModel(
						simu.getReporter(), -1);
				try {
					if (excel)
						tModel.exportExcel(report);
					else
						tModel.exportCSV(report);
				} catch (IOException ex) {
					System.err.println("Error while saving reports\n");
					ex.printStackTrace();
					System.exit(-1);
				}
			}
		} catch (Exception exn) {
			exn.printStackTrace();
			System.exit(-1);
		}

	}
}
