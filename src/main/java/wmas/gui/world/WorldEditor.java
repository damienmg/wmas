package wmas.gui.world;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import wmas.Main;
import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.gui.GuiModificationListener;
import wmas.gui.run.RunDialog;
import wmas.gui.run.SimulationRunner;
import wmas.gui.run.editor.WorldSimulatorsEditor;
import wmas.gui.world.tree.WorldObjectTreeCellRenderer;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.util.Triplet;
import wmas.world.Entity;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class WorldEditor extends JSplitPane implements MouseListener,
		ActionListener, GuiModificationListener, TreeSelectionListener {
	private static final long serialVersionUID = 1L;

	protected static Set<WorldAttributeFactory> attrFactories = new HashSet<WorldAttributeFactory>();
	protected static Map<String, Doublet<WorldAttributeFactory, Integer>> attrFactoriesMap = new HashMap<String, Doublet<WorldAttributeFactory, Integer>>();
	private static final Icon defaultIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/default.gif"));

	public static void registerAttributeFactory(WorldAttributeFactory f) {
		attrFactories.add(f);
		for (int i = 0; i < f.getNbWorldAttribute(); i++) {
			attrFactoriesMap.put(f.getWorldAttributeName(i),
					new Doublet<WorldAttributeFactory, Integer>(f, i));
		}
	}

	public static Set<String> listAttributes() {
		return attrFactoriesMap.keySet();
	}

	public static String getDescription(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return null;
		Doublet<WorldAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().getWorldAttributeDescription(d.getSecond());
	}

	public static WorldAttributeEditor getAttributeEditor(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return null;
		Doublet<WorldAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().getWorldAttributeView(d.getSecond());
	}

	public static XMLEntity createAttribute(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return null;
		Doublet<WorldAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().createWorldAttribute(d.getSecond());
	}

	public static Icon getAttributeIcon(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return defaultIcon;
		Doublet<WorldAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().getWorldAttributeView(d.getSecond()).getIcon();
	}

	private JTree tree;
	private WorldObjectTreeModel treeModel;
	private EditorTabbedPane tabs;
	private WorldSimulatorsEditor simuDialog = null;

	private void construct() {
		treeModel = new WorldObjectTreeModel();
		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(this);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new WorldObjectTreeCellRenderer());
		tree.setToggleClickCount(1000000); // Prevent double click from
											// collapsing/expanding the tree
		tree.setEditable(false);
		tree.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				TreePath p = tree.getSelectionPath();
				if (p != null) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_DELETE:
					case KeyEvent.VK_BACK_SPACE:
						for (TreePath tp : tree.getSelectionPaths())
							treeModel.delete(tp, WorldEditor.this);
						break;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		tree.addMouseListener(this);
		// tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tabs = new EditorTabbedPane(tree);
		tabs.addGuiModificationListener(this);

		setRightComponent(tabs);
		setLeftComponent(new JScrollPane(tree));
		setDividerLocation(200);
	}

	public WorldEditor() {
		super(HORIZONTAL_SPLIT);
		construct();
	}

	public void modified(Object f) {
		tree.revalidate();
		tree.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("model:open")) {
			if (close())
				doOpen();
		} else if (actionCommand.equals("model:save")) {
			doSave(true);
		} else if (actionCommand.equals("model:save_as")) {
			doSave(false);
		} else if (actionCommand.equals("model:save_all")) {
			doSaveAll();
		} else if (actionCommand.equals("model:save_node")) {
			doSaveNode();
		} else if (actionCommand.equals("model:new_model")) {
			if (close() && treeModel.load(new Model()))
				doOnLoad(null);
		} else if (actionCommand.equals("model:new_world")) {
			if (close() && treeModel.load(new World()))
				doOnLoad(null);
		} else if (actionCommand.equals("model:new_graph")) {
			if (close() && treeModel.load(new BehaviourGraph()))
				doOnLoad(null);
		} else if (actionCommand.equals("model:new_entity")) {
			if (close() && treeModel.load(new Entity()))
				doOnLoad(null);
		} else if (actionCommand.equals("model:add:entity")) {
			treeModel.addEntity(tree.getSelectionPath());
		} else if (actionCommand.equals("model:add:behaviour")) {
			treeModel.addBehaviour(tree.getSelectionPath());
		} else if (actionCommand.equals("model:delete")) {
			for (TreePath tp : tree.getSelectionPaths())
				treeModel.delete(tp, this);
		} else if (actionCommand.equals("model:import")) {
			treeModel.doImport(this, tree.getSelectionPath());
		} else if (actionCommand.equals("model:add:attribute")) {
			treeModel.addAttribute(this, tree.getSelectionPath());
		} else if (actionCommand.equals("model:run")) {
			doRun();
		} else if (actionCommand.equals("model:edit_run")) {
			if (treeModel.getRoot() instanceof World) {
				if (simuDialog == null) {
					Container owner = parentFrame();
					if (owner == null)
						simuDialog = new WorldSimulatorsEditor((Frame) null,
								origTitle + " - Executions");
					else if (owner instanceof Frame)
						simuDialog = new WorldSimulatorsEditor((Frame) owner,
								origTitle + " - Executions");
					else if (owner instanceof Dialog)
						simuDialog = new WorldSimulatorsEditor((Dialog) owner,
								origTitle + " - Executions");
				}
				String r = simuDialog.display((World) treeModel.getRoot());
				if (simuDialog.isModified()) {
					treeModel.setModified(treeModel.getRoot(), true);
				}
				if (r != null && !r.isEmpty()) {
					doRun(r);
				}
				updateExecutionList();
			}
		} else if (actionCommand.startsWith("model:execute_run")) {
			if (e.getSource() instanceof JMenuItem) {
				String s = ((JMenuItem) e.getSource()).getText();
				if (treeModel.getRoot() instanceof World) {
					if (((World) (treeModel.getRoot())).listSimulators()
							.contains(s)) {
						doRun(s);
					}
				}
			}
		} else if (actionCommand.startsWith("model:launch_run")) {
			if (e.getSource() instanceof JMenuItem) {
				String s = ((JMenuItem) e.getSource()).getText();
				if (treeModel.getRoot() instanceof World) {
					if (((World) (treeModel.getRoot())).listSimulators()
							.contains(s)) {
						doExecute(s);
					}
				}
			}
		}
	}

	private void doRun() {
		if (treeModel.getRoot() instanceof World) {
			if (close()) {
				Container parent = parentFrame();
				if (parent != null) {
					if (parent instanceof Frame)
						((Frame) parent).dispose();
					else if (parent instanceof Dialog)
						((Dialog) parent).dispose();
				}
				if (currentFile != null) {
					SimulationRunner.run(origTitle, currentFile);
				} else {
					SimulationRunner
							.run(origTitle, (World) treeModel.getRoot());
				}
			}
		}
	}

	private void doRun(String r) {
		if (treeModel.getRoot() instanceof World) {
			if (close()) {
				Container parent = parentFrame();
				if (parent != null) {
					if (parent instanceof Frame)
						((Frame) parent).dispose();
					else if (parent instanceof Dialog)
						((Dialog) parent).dispose();
				}
				if (currentFile != null) {
					SimulationRunner.run(origTitle, currentFile, r);
				} else {
					SimulationRunner.run(origTitle,
							(World) treeModel.getRoot(), r);
				}
			}
		}
	}

	private void doExecute(String r) {
		if (treeModel.getRoot() instanceof World) {
			if (close()) {
				Container parent = parentFrame();
				if (parent != null) {
					if (parent instanceof Frame)
						((Frame) parent).dispose();
					else if (parent instanceof Dialog)
						((Dialog) parent).dispose();
				}
				RunDialog.run(origTitle, currentFile,
						(World) treeModel.getRoot(), r);
			}
		}
	}

	private Container parentFrame() {
		Container parent = getParent();
		while (parent != null && !(parent instanceof Frame)
				&& !(parent instanceof Dialog)) {
			parent = parent.getParent();
		}
		return parent;
	}

	private File currentFile = null;
	private JFileChooser chooser = new JFileChooser();

	private void doSave(boolean b) {
		File f = currentFile;
		if (!b || f == null) {
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
		}
		if (f == null)
			return;
		try {
			if (treeModel.save(f)) {
				currentFile = f;
				setTitle(currentFile.getAbsolutePath());
				updateGrayMenu();
				tree.repaint();
			} else
				JOptionPane.showMessageDialog(this, "Save failed!");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Save failed!");
		}
	}

	private boolean doSaveAll() {
		File f = currentFile;
		if (f == null) {
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
		}
		if (f == null)
			return false;
		try {
			if (treeModel.saveAll(f)) {
				currentFile = f;
				setTitle(currentFile.getAbsolutePath());
				updateGrayMenu();
				tree.repaint();
				return true;
			} else
				JOptionPane.showMessageDialog(this, "Save failed!");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Save failed!");
		}
		return false;
	}

	private boolean doSaveNode() {
		TreePath p = tree.getSelectionPath();
		if (p == null)
			return false;
		Object node = tree.getLastSelectedPathComponent();
		File f = currentFile;
		if (node == treeModel.getRoot() && f == null) {
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
		}
		if (node == null || (node == treeModel.getRoot() && f == null))
			return false;
		try {
			if (treeModel.saveSelectedFile(node, f)) {
				currentFile = f;
				setTitle(currentFile.getAbsolutePath());
				updateGrayMenu();
				tree.repaint();
				return true;
			} else
				JOptionPane.showMessageDialog(this, "Save failed!");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Save failed!");
		}
		return false;
	}

	private String origTitle = null;

	private void setTitle(String name) {
		Container parent = parentFrame();
		if (parent == null)
			return;
		if (name == null)
			name = "";
		if (parent instanceof Frame) {
			if (origTitle == null) {
				origTitle = ((Frame) parent).getTitle();
				if (origTitle == null)
					origTitle = "";
				if (help != null)
					help.setTitle(origTitle);
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
				if (help != null)
					help.setTitle(origTitle);
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
		updateGrayMenu();
		tabs.removeAll();
		edit(new TreePath(treeModel.getRoot()));
	}

	private void doOpen(File f) {
		if (treeModel.load(f)) {
			doOnLoad(f);
		} else
			JOptionPane.showMessageDialog(this, "Loading failed!");
	}

	private void doOpen(XMLEntity root) {
		if (treeModel.load(root)) {
			doOnLoad(null);
		}
	}

	private void doOpen() {
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			doOpen(chooser.getSelectedFile());
		}
	}

	public boolean close() {
		if (treeModel.hasModified()) {
			String text = "The following files were modified: \n";
			for (String f : treeModel.getModified()) {
				if (f == null) {
					if (currentFile != null)
						f = currentFile.getAbsolutePath();
					else
						f = "Root file";
				}
				text += "\t" + f + "\n";
			}
			text += "Do you want to save them before closing?";
			int answer = JOptionPane.showConfirmDialog(this, text,
					"Closing editor", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			switch (answer) {
			case JOptionPane.CANCEL_OPTION:
				return false;
			case JOptionPane.YES_OPTION:
				return (doSaveAll());
			}

		}
		return true;
	}

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

	public void refreshWorld(World w, EntityInterface deleted) {
		if (tabs.getEditor(w) != null) {
			((WorldEditorPanel) tabs.getEditor(w)).deleted(deleted);
		}
	}

	public void deleting(TreePath p) {
		Doublet<TreePath, BehaviourGraph[]> r = getBehaviourPath(p, false);
		tabs.deleting(p.getLastPathComponent(), r.getSecond());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			// Double click
			TreePath p = tree
					.getPathForLocation(e.getPoint().x, e.getPoint().y);
			if (p != null && p.getLastPathComponent() != null) {
				edit(p);
				return;
			}
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

	private JMenu[] associatedMenu = null;
	private JMenuItem saveAsMenu = null;
	private JMenuItem saveSelectedMenu = null;
	private JMenuItem deleteMenu = null;
	private JMenuItem behaviourMenu = null;
	static private HelpDialog help = null;
	private JMenu executeMenuItem = null;
	private JMenu runMenuItem = null;

	private boolean isPathChildOfModel(TreePath p) {
		if (p == null)
			return false;
		TreePath path = treeModel.searchParent(p);
		if (path == null || path.getPathCount() < 1)
			return false;
		Object o = treeModel.getObject(path.getLastPathComponent());
		if (o == null)
			return false;
		return o instanceof Model;
	}

	private void updateGrayMenu() {
		if (associatedMenu != null) {
			TreePath p = tree.getSelectionPath();
			boolean selected = p != null;
			boolean isWorld = (treeModel.getRoot() instanceof World);
			boolean viewEdit = isWorld
					|| (treeModel.getRoot() instanceof Model);
			associatedMenu[2].setVisible(isWorld);
			associatedMenu[1].setVisible(viewEdit);
			saveAsMenu.setEnabled(currentFile != null);
			saveSelectedMenu.setEnabled(selected);
			deleteMenu.setEnabled(viewEdit && selected && p.getPathCount() > 1);
			behaviourMenu.setEnabled(selected && isPathChildOfModel(p));
			if (isWorld)
				updateExecutionList();
		}
	}

	public JMenu[] getMenus() {
		if (associatedMenu == null) {
			associatedMenu = new JMenu[3];
			associatedMenu[0] = new JMenu("File");
			associatedMenu[1] = new JMenu("Edit");

			JMenuItem mnuItem;

			associatedMenu[2] = new JMenu("Execute");
			mnuItem = new JMenuItem("Execute");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:run");
			mnuItem.addActionListener(this);
			associatedMenu[2].add(mnuItem);
			executeMenuItem = new JMenu("Execute as...");
			executeMenuItem.setEnabled(false);
			associatedMenu[2].add(executeMenuItem);
			mnuItem = new JMenuItem("Run configurations...");
			mnuItem.setActionCommand("model:edit_run");
			mnuItem.addActionListener(this);
			associatedMenu[2].add(mnuItem);
			runMenuItem = new JMenu("Run as...");
			runMenuItem.setEnabled(false);
			associatedMenu[2].add(runMenuItem);
			updateExecutionList();

			JMenu newMenu = new JMenu("New");
			mnuItem = new JMenuItem("Simulation world");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:new_world");
			newMenu.add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Simulation model");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:new_model");
			newMenu.add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Behaviour graph");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:new_graph");
			newMenu.add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Entity model");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:new_entity");
			newMenu.add(mnuItem);
			mnuItem.addActionListener(this);

			associatedMenu[0].add(newMenu);

			mnuItem = new JMenuItem("Open...");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:open");
			associatedMenu[0].add(mnuItem);
			mnuItem.addActionListener(this);

			associatedMenu[0].add(new JSeparator());
			mnuItem = new JMenuItem("Save Root");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:save");
			associatedMenu[0].add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Save Root As...");
			mnuItem.setActionCommand("model:save_as");
			associatedMenu[0].add(mnuItem);
			mnuItem.addActionListener(this);
			saveAsMenu = mnuItem;

			mnuItem = new JMenuItem("Save Selected Node");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					ActionEvent.META_MASK));
			mnuItem.setActionCommand("model:save_node");
			associatedMenu[0].add(mnuItem);
			mnuItem.addActionListener(this);
			saveSelectedMenu = mnuItem;

			mnuItem = new JMenuItem("Save All");
			mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					ActionEvent.META_MASK | ActionEvent.SHIFT_MASK));
			mnuItem.setActionCommand("model:save_all");
			associatedMenu[0].add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Add attribute");
			mnuItem.setActionCommand("model:add:attribute");
			associatedMenu[1].add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Add entity");
			mnuItem.setActionCommand("model:add:entity");
			associatedMenu[1].add(mnuItem);
			mnuItem.addActionListener(this);

			mnuItem = new JMenuItem("Add behaviour");
			mnuItem.setActionCommand("model:add:behaviour");
			associatedMenu[1].add(mnuItem);
			mnuItem.addActionListener(this);
			behaviourMenu = mnuItem;

			mnuItem = new JMenuItem("Delete");
			mnuItem.setActionCommand("model:delete");
			associatedMenu[1].add(mnuItem);
			mnuItem.addActionListener(this);
			deleteMenu = mnuItem;

			mnuItem = new JMenuItem("Import");
			mnuItem.setActionCommand("model:import");
			associatedMenu[1].add(mnuItem);
			mnuItem.addActionListener(this);

			updateGrayMenu();
		}
		return associatedMenu;
	}

	private void updateExecutionList() {
		if (executeMenuItem != null && runMenuItem != null) {
			if (treeModel.getRoot() != null
					&& treeModel.getRoot() instanceof World) {
				World w = (World) treeModel.getRoot();
				runMenuItem.removeAll();
				executeMenuItem.removeAll();
				if (w.listSimulators().size() > 0) {
					for (String s : w.listSimulators()) {
						JMenuItem it = new JMenuItem(s);
						it.addActionListener(this);
						it.setActionCommand("model:execute_run");
						executeMenuItem.add(it);
						it = new JMenuItem(s);
						it.addActionListener(this);
						it.setActionCommand("model:launch_run");
						runMenuItem.add(it);
					}
					runMenuItem.setEnabled(true);
					executeMenuItem.setEnabled(true);
				} else {
					runMenuItem.setEnabled(false);
					executeMenuItem.setEnabled(false);
				}
			} else {
				runMenuItem.setEnabled(false);
				executeMenuItem.setEnabled(false);
			}
		}
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
		updateGrayMenu();
	}

	public JDialog createDialog(final String title) {
		final JDialog d = new JDialog();
		d.setTitle(title);
		d.setContentPane(this);
		d.setJMenuBar(new JMenuBar());
		JMenu[] menus = getMenus();
		for (int i = 0; i < menus.length; i++) {
			if (i == 0) {
				menus[i].addSeparator();
				JMenuItem mnuItem = new JMenuItem("Quit");
				mnuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						ActionEvent.META_MASK));
				mnuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (close()) {
							d.setVisible(false);
							d.dispose();
						}
					}
				});
				menus[i].add(mnuItem);
			}
			d.getJMenuBar().add(menus[i]);
		}
		JMenu mnu = new JMenu("Help");
		JMenuItem it = new JMenuItem("Help...");
		it.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				ActionEvent.META_MASK));
		it.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				help = new HelpDialog(d, title);
				if (help != null) {
					help.setVisible(true);
				}

			}
		});
		mnu.add(it);
		mnu.addSeparator();
		it = new JMenuItem("About...");
		it.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showConfirmDialog(d, Main.aboutString, "About...",
						JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION);
			}
		});
		mnu.add(it);
		d.getJMenuBar().add(mnu);
		d.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (close()) {
					d.setVisible(false);
					d.dispose();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		d.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		d.setLocationRelativeTo(null);
		return d;
	}

	public static void run(String title, XMLEntity root) {
		WorldEditor m = new WorldEditor();
		JDialog d = m.createDialog(title);
		m.doOpen(root);
		d.setVisible(true);
	}

	public static void run(String title) {
		WorldEditor m = new WorldEditor();
		JDialog d = m.createDialog(title);
		m.doOpen(new World());
		d.setVisible(true);
	}

	public static void run(String title, File f) {
		WorldEditor m = new WorldEditor();
		JDialog d = m.createDialog(title);
		m.doOpen(f);
		d.setVisible(true);
	}
}
