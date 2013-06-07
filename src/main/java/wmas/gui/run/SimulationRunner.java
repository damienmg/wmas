package wmas.gui.run;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.gui.EditorInterface;
import wmas.gui.GuiModificationListener;
import wmas.gui.shapes.SchemeView;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.util.Triplet;
import wmas.util.Util;
import wmas.world.EntityInterface;
import wmas.world.Simulator;
import wmas.world.UpdateManager;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class SimulationRunner extends JPanel implements MouseListener,
		ActionListener, GuiModificationListener, TreeSelectionListener,
		UpdateManager {
	private static final long serialVersionUID = 1L;

	private Simulator simulator;
	private JSplitPane splitter;
	private JTree tree;
	private WorldObjectTreeModel treeModel;
	private RunnerTabbedPane tabs;
	private JLabel statusLabel;
	private JLabel timeLabel;

	private JTextField speedLabel;
	private JTextField precisionLabel;
	private JToggleButton rtButton;
	private JToggleButton stepExecButton;

	private static final Color[] colors = { Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.GRAY, Color.PINK,
			Color.LIGHT_GRAY, Color.RED, Color.YELLOW, Color.DARK_GRAY };

	private void extendColoring() {
		int i = 0;
		if (treeModel.getRoot() != null && treeModel.getRoot() instanceof World) {
			World w = (World) treeModel.getRoot();
			for (EntityInterface e : w.listActiveEntities()) {
				if (e.getBehaviours().size() > 0
						&& (e.getDrawingShape() != null || e.getBehaviours()
								.size() > 0)) {
					if (e.getColorization() == null) {
						e.colorize(colors[i]);
					}
					i++;
					if (i >= colors.length)
						i = 0;
				} else {
					if (e.getColorization() != null) {
						i--;
						e.colorize(null);
					}
				}
			}
		}

	}

	private void affectColoring() {
		int i = 0;
		if (treeModel.getRoot() != null && treeModel.getRoot() instanceof World) {
			World w = (World) treeModel.getRoot();
			for (EntityInterface e : w.listActiveEntities()) {
				if (e.getBehaviours().size() > 0
						&& (e.getDrawingShape() != null || e.getBehaviours()
								.size() > 0)) {
					e.colorize(colors[i]);
					i++;
					if (i >= colors.length)
						i = 0;
				} else
					e.colorize(null);
			}
		}
	}

	private JButton makeButton(String text, String actionCommand,
			String toolTipText) {
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		button.setText(text);
		return button;
	}

	private JButton pauseButton;
	private JButton stopButton;
	private JButton reportsButton;
	private JButton stepButton;

	private void doModifyButtons() {
		if (simulator.isStopped()) {
			stopButton.setText("▶");
			stopButton.setToolTipText("Launch simulation");
			pauseButton.setText("▮▮");
			pauseButton.setToolTipText("Pause simulation");
			pauseButton.setEnabled(false);
			reportsButton.setEnabled(true);
			stepButton.setEnabled(false);
		} else if (simulator.isPaused()) {
			stopButton.setText("◼");
			stopButton.setToolTipText("Stop simulation");
			pauseButton.setText("▶");
			pauseButton.setToolTipText("Resume simulation");
			reportsButton.setEnabled(true);
			stepButton.setEnabled(true);
		} else {
			stopButton.setText("◼");
			stopButton.setToolTipText("Stop simulation");
			pauseButton.setText("▮▮");
			pauseButton.setToolTipText("Pause simulation");
			pauseButton.setEnabled(true);
			reportsButton.setEnabled(false);
			stepButton.setEnabled(false);
		}
	}

	private void addButtons(JToolBar bar) {
		bar.add(makeButton("×", "quit", "Quit"));
		bar.add(makeButton("↶", "edit", "Go back to editing"));
		pauseButton = makeButton("▮▮", "resume", "Pause simulation");
		bar.add(pauseButton);
		stopButton = makeButton("▶", "stop", "Launch simulation");
		bar.add(stopButton);
		stepButton = makeButton("♦", "step", "Run one execution step");
		bar.add(stepButton);
		stepButton.setEnabled(false);
		pauseButton.setEnabled(false);

		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(makeButton("-", "speed_down", "Decrease speed"));
		speedLabel = new JTextField("0.00000000");
		speedLabel.setActionCommand("speed");
		speedLabel.addActionListener(this);
		speedLabel
				.setToolTipText("Execution speed. Give the ratio internal time over real time.");
		speedLabel.setColumns(10);
		Dimension dim = speedLabel.getMaximumSize();
		FontMetrics fm = speedLabel.getFontMetrics(speedLabel.getFont());
		dim.setSize(fm.stringWidth(" 0.00000000 "), dim.height);
		speedLabel.setMaximumSize(dim);
		bar.add(speedLabel);
		bar.add(makeButton("+", "speed_up", "Increase speed"));

		bar.add(new JSeparator(JSeparator.VERTICAL));
		bar.add(makeButton("-", "prec_down", "Decrease precision"));
		precisionLabel = new JTextField("0.00000000");
		precisionLabel.setActionCommand("prec");
		precisionLabel.addActionListener(this);
		precisionLabel.setColumns(10);
		precisionLabel
				.setToolTipText("Execution precision. Give the internal time step between each execution step.");
		dim = precisionLabel.getMaximumSize();
		fm = precisionLabel.getFontMetrics(precisionLabel.getFont());
		dim.setSize(fm.stringWidth(" 0.00000000 "), dim.height);
		precisionLabel.setMaximumSize(dim);
		bar.add(precisionLabel);
		bar.add(makeButton("+", "prec_up", "Increase precision"));

		bar.add(new JSeparator(JSeparator.VERTICAL));
		stepExecButton = new JToggleButton("Step exec");
		stepExecButton.setActionCommand("step_exec");
		stepExecButton.addActionListener(this);
		stepExecButton
				.setToolTipText("Does the execution stops after each behaviour step?");
		bar.add(stepExecButton);
		rtButton = new JToggleButton("Real time");
		rtButton.setActionCommand("rt");
		rtButton.addActionListener(this);
		rtButton.setToolTipText("Does the timer adapt to real time or not? If true, then time is computed relatively to the real clock.");
		bar.add(rtButton);
		reportsButton = makeButton("Reports", "reports",
				"View execution reports");
		bar.add(reportsButton);
	}

	private void transferLabels() {
		rtButton.setSelected(simulator.isRealTime());
		precisionLabel.setText(Util.getFormatted(simulator.getPrecision()));
		speedLabel.setText(Util.getFormatted(simulator.getSpeed()));
	}

	private void construct() {
		JToolBar b = new JToolBar();
		addButtons(b);
		b.setFloatable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(b, c);

		c.weighty = 1;
		treeModel = new WorldObjectTreeModel();
		treeModel.setDontShowModels(true);
		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(this);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new wmas.gui.world.tree.WorldObjectTreeCellRenderer());
		tree.setToggleClickCount(1000000); // Prevent double click from
											// collapsing/expanding the tree
		tree.setEditable(false);
		tree.addMouseListener(this);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tabs = new RunnerTabbedPane(tree);
		tabs.addGuiModificationListener(this);

		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitter.setRightComponent(tabs);
		splitter.setLeftComponent(new JScrollPane(tree));
		splitter.setDividerLocation(200);

		add(splitter, c);

		c.weighty = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.WEST;
		statusLabel = new JLabel("Stopped");
		add(statusLabel, c);
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel(), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.EAST;
		timeLabel = new JLabel("t = 0");
		add(timeLabel, c);
	}

	public SimulationRunner() {
		super(new GridBagLayout());
		construct();
		simulator = new Simulator();
		simulator.setDisplay(this);
		transferLabels();
	}

	public SimulationRunner(World w) {
		super(new GridBagLayout());
		construct();
		if (treeModel.load(w)) {
			simulator = new Simulator(w, this);
			tabs.edit(w);
		} else {
			simulator = new Simulator();
			simulator.setDisplay(this);
		}
		transferLabels();
	}

	public SimulationRunner(World w, String simu) {
		super(new GridBagLayout());
		construct();
		if (treeModel.load(w)) {
			simulator = w.getSimulator(simu);
			if (simulator == null)
				simulator = new Simulator(w, this);
			else {
				simulator.setWorld(w);
				simulator.setDisplay(this);
			}
			tabs.edit(w);
		} else {
			simulator = new Simulator();
			simulator.setDisplay(this);
		}
		transferLabels();
	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("resume")) {
			if (simulator.isPaused())
				simulator.Resume();
			else
				simulator.Pause();
			update();
		} else if (actionCommand.equals("stop")) {
			if (simulator.isStopped()) {
				affectColoring();
				simulator.Run();
			} else
				simulator.Stop();
			update();
		} else if (actionCommand.equals("speed_up")) {
			simulator.setSpeed(simulator.getSpeed() * 2);
			speedLabel.setText(Util.getFormatted(simulator.getSpeed()));
		} else if (actionCommand.equals("speed_down")) {
			simulator.setSpeed(simulator.getSpeed() / 2);
			speedLabel.setText(Util.getFormatted(simulator.getSpeed()));
		} else if (actionCommand.equals("speed")) {
			try {
				simulator.setSpeed(Double.parseDouble(speedLabel.getText()));
			} catch (Exception exn) {
			}
			speedLabel.setText(Util.getFormatted(simulator.getSpeed()));
		} else if (actionCommand.equals("prec_up")) {
			simulator.setPrecision(simulator.getPrecision() * 2);
			precisionLabel.setText(Util.getFormatted(simulator.getPrecision()));
		} else if (actionCommand.equals("prec_down")) {
			simulator.setPrecision(simulator.getPrecision() / 2);
			precisionLabel.setText(Util.getFormatted(simulator.getPrecision()));
		} else if (actionCommand.equals("prec")) {
			try {
				simulator.setPrecision(Double.parseDouble(precisionLabel
						.getText()));
			} catch (Exception exn) {
			}
			precisionLabel.setText(Util.getFormatted(simulator.getPrecision()));
		} else if (actionCommand.equals("rt")) {
			simulator.setRealTime(rtButton.isSelected());
		} else if (actionCommand.equals("step_exec")) {
			simulator.setStepExecution(stepExecButton.isSelected());
		} else if (actionCommand.equals("step")) {
			if (simulator.isPaused()) {
				simulator.actionPerformed(e);
			}
		} else if (actionCommand.equals("edit")) {
			simulator.Stop();
			Container parent = parentFrame();
			if (parent != null) {
				if (parent instanceof Frame)
					((Frame) parent).dispose();
				else if (parent instanceof Dialog)
					((Dialog) parent).dispose();
			}
			if (currentFile != null) {
				WorldEditor.run(origTitle, currentFile);
			} else {
				WorldEditor.run(origTitle, simulator.getWorld().copy());
			}
		} else if (actionCommand.equals("quit")) {
			simulator.Stop();
			Container parent = parentFrame();
			if (parent == null)
				System.exit(0);
			if (parent instanceof Frame)
				((Frame) parent).dispose();
			else if (parent instanceof Dialog)
				((Dialog) parent).dispose();
			else
				System.exit(0);
		} else if (actionCommand.equals("reports")) {
			Container parent = parentFrame();
			if (parent == null)
				new ReportDialog(origTitle, simulator.getReporter());
			else if (parent instanceof Frame)
				new ReportDialog(((Frame) parent), origTitle,
						simulator.getReporter());
			else if (parent instanceof Dialog)
				new ReportDialog(((Dialog) parent), origTitle,
						simulator.getReporter());
			else
				new ReportDialog(origTitle, simulator.getReporter());
		}
	}

	private File currentFile = null;
	private JFileChooser chooser = new JFileChooser();

	private Container parentFrame() {
		Container parent = getParent();
		while (parent != null && !(parent instanceof Frame)
				&& !(parent instanceof Dialog)) {
			parent = parent.getParent();
		}
		return parent;
	}

	private String origTitle = null;

	private void setTitle(String name) {
		Container parent = parentFrame();
		if (name == null)
			name = "";
		if (parent instanceof Frame) {
			if (origTitle == null) {
				origTitle = ((Frame) parent).getTitle();
				if (origTitle == null)
					origTitle = "";
			}
			String newTitle = origTitle;
			if (origTitle.length() > 0 && name.length() > 0)
				newTitle = origTitle + " - " + name;
			else
				newTitle = origTitle + name;
			((Frame) parent).setTitle(newTitle);
		} else if (parent instanceof Dialog) {
			if (origTitle == null) {
				origTitle = ((Dialog) parent).getTitle();
				if (origTitle == null)
					origTitle = "";
			}
			String newTitle = origTitle;
			if (origTitle.length() > 0 && name.length() > 0)
				newTitle = origTitle + " - " + name;
			else
				newTitle = origTitle + name;
			((Dialog) parent).setTitle(newTitle);
		}
	}

	private void doOnLoad(File f) {
		currentFile = f;
		if (f == null)
			setTitle(null);
		else
			setTitle(currentFile.getAbsolutePath());
		update();
		tabs.removeAll();
		tabs.edit(treeModel.getRoot());
	}

	private boolean doOpen(File f, String simu) {
		if (treeModel.load(f)) {
			if (treeModel.getRoot() instanceof World) {
				World w = (World) treeModel.getRoot();
				if (w.getSimulator(simu) != null) {
					simulator = w.getSimulator(simu);
					simulator.setDisplay(this);
				}
				simulator.setWorld(w);
				doOnLoad(f);
				return true;
			} else
				JOptionPane.showMessageDialog(this, "Loading failed!");
		} else
			JOptionPane.showMessageDialog(this, "Loading failed!");
		return false;
	}

	private boolean doOpen() {
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return doOpen(chooser.getSelectedFile(), null);
		}
		return false;
	}

	private long lastClick = 0;
	private Point lastClickPosition = null;

	private int isBehaviour(Object o) {
		if (o == null)
			return 0;
		o = treeModel.getObject(o);
		if (o == null)
			return 0;
		return (o instanceof Behaviour) ? (o instanceof BehaviourGraph) ? 2 : 1
				: 0;
	}

	private Doublet<TreePath, BehaviourGraph[]> getBehaviourPath(TreePath path) {
		return getBehaviourPath(path, true);
	}

	private Doublet<TreePath, BehaviourGraph[]> getBehaviourPath(TreePath path,
			boolean removeLast) {
		int i = 0;
		List<BehaviourGraph> l = new LinkedList<BehaviourGraph>();
		while (path != null && path.getPathCount() > 0
				&& (i = isBehaviour(path.getLastPathComponent())) > 0) {
			if (i == 2)
				l.add((BehaviourGraph) treeModel.getObject(path
						.getLastPathComponent()));
			path = path.getParentPath();
		}
		BehaviourGraph[] r = null;
		if (l.size() > 0) {
			if (path == null)
				path = new TreePath(l.get(l.size() - 1));
			else
				path = path.pathByAddingChild(l.get(l.size() - 1));
			if (removeLast)
				l.remove(l.get(l.size() - 1));
			if (l.size() > 0) {
				r = new BehaviourGraph[l.size()];
				int k = l.size() - 1;
				for (BehaviourGraph gr : l) {
					r[k] = gr;
					k--;
				}
			}
		}
		return new Doublet<TreePath, BehaviourGraph[]>(path, r);
	}

	private void edit(TreePath p) {
		Doublet<TreePath, BehaviourGraph[]> r = getBehaviourPath(p);
		Triplet<Object, Object, String> t = WorldObjectTreeModel
				.getPathComponents(r.getFirst());
		tabs.edit(t.getFirst(), t.getSecond(), r.getSecond());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (System.currentTimeMillis() - lastClick < 500
					&& lastClickPosition != null) {
				if (e.getPoint().equals(lastClickPosition)) {
					// Double click
					TreePath p = tree.getPathForLocation(e.getPoint().x,
							e.getPoint().y);
					if (p != null && p.getLastPathComponent() != null) {
						edit(p);
						lastClick = 0;
						lastClickPosition = null;
						return;
					}
				}
			}
			lastClick = System.currentTimeMillis();
			lastClickPosition = e.getPoint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void internalChanged(Object[] o) {
		treeModel.setModified(o, true);
		revalidate();
		tree.repaint();
	}

	@Override
	public void representationChanged(Object[] o) {
		treeModel.setModified(o, true);
		revalidate();
		tree.repaint();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
	}

	public void run() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				affectColoring();
				simulator.Run();
			}
		});
	}

	public JDialog createDialog(String title) {
		final JDialog d = new JDialog();
		d.setTitle(title);
		d.setContentPane(this);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		d.setLocationRelativeTo(null);
		d.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				simulator.Stop();
			}
		});
		return d;
	}

	public static void run(String title, World w) {
		SimulationRunner m = new SimulationRunner(w);
		JDialog d = m.createDialog(title);
		d.setVisible(true);
		m.run();
	}

	public static void run(String title, World w, String r) {
		SimulationRunner m = new SimulationRunner(w, r);
		JDialog d = m.createDialog(title);
		d.setVisible(true);
		m.run();
	}

	public static void run(String title) {
		SimulationRunner m = new SimulationRunner();
		JDialog d = m.createDialog(title);
		if (m.doOpen()) {
			d.setVisible(true);
			m.run();
		}
	}

	public static void run(String title, File f) {
		SimulationRunner m = new SimulationRunner();
		JDialog d = m.createDialog(title);
		if (m.doOpen(f, null)) {
			d.setVisible(true);
			m.run();
		}
	}

	public static void run(String title, File f, String simu) {
		SimulationRunner m = new SimulationRunner();
		JDialog d = m.createDialog(title);
		if (m.doOpen(f, simu)) {
			d.setVisible(true);
			m.run();
		}
	}

	@Override
	public void reset() {
		treeModel.load((XMLEntity) treeModel.getRoot());
		tree.revalidate();
		tabs.reset(treeModel);
		update();
		transferLabels();
	}

	@Override
	public void terminated() {
		update();
	}

	@Override
	public void update() {
		extendColoring();
		timeLabel.setText("t = "
				+ Util.getFormatted(simulator.getInternalTime()));
		statusLabel.setText(simulator.isPaused() ? "Paused" : simulator
				.isStopped() ? "Stopped" : "Running");
		doModifyButtons();
		tabs.refresh();
		repaint();
	}

	@Override
	public void prepareReset() {
		tabs.prepareReset(treeModel);
	}

	@Override
	public SchemeView getWorldView() {
		EditorInterface i = tabs.getEditor(treeModel.getRoot());
		if (i != null) {
			if (i instanceof WorldViewPanel) {
				return ((WorldViewPanel) i).getWorldView();
			}
		}
		return null;
	}

	@Override
	public void addWorldButton(Component comp) {
		EditorInterface i = tabs.getEditor(treeModel.getRoot());
		if (i != null) {
			if (i instanceof WorldViewPanel) {
				((WorldViewPanel) i).addWorldButton(comp);
			}
		}
	}
}
