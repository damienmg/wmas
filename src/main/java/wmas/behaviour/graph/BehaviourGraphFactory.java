package wmas.behaviour.graph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourFactory;
import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.gui.EditorInterface;
import wmas.gui.GuiModificationListener;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.SchemeViewListener;
import wmas.gui.shapes.ShapeFactory;
import wmas.gui.shapes.ShapeListener;
import wmas.gui.shapes.elements.Circle;
import wmas.gui.shapes.elements.Connector;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class BehaviourGraphFactory implements SchemeViewListener, ShapeFactory,
		ActionListener, GuiModificationListener, ShapeListener, EditorInterface {
	final static GridBagConstraints c = new GridBagConstraints();

	static protected Set<BehaviourFactory> factories = new HashSet<BehaviourFactory>();
	static Map<String, Doublet<Integer, BehaviourFactory>> behaviourMap = new HashMap<String, Doublet<Integer, BehaviourFactory>>();
	private static final Icon behaviourIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/behaviour.gif"));

	static public void register(BehaviourFactory factory) {
		factories.add(factory);
		for (int i = 0; i < factory.getNbBehaviour(); i++) {
			behaviourMap.put(factory.getBehaviourName(i),
					new Doublet<Integer, BehaviourFactory>(i, factory));
		}
	}

	public static Set<String> listBehaviours() {
		return behaviourMap.keySet();
	}

	public static String getDescription(String name) {
		if (!behaviourMap.containsKey(name))
			return null;
		Doublet<Integer, BehaviourFactory> d = behaviourMap.get(name);
		return d.getSecond().getBehaviourDescription(d.getFirst());
	}

	private WorldObjectTreeModel treeModel = null;

	private Collection<Behaviour> behaviours = null;
	private Stack<BehaviourGraph> graphList = new Stack<BehaviourGraph>();
	private BehaviourGraph graph;
	private SchemeView schemeView;
	private Component editorPanel = null;
	private JScrollPane scroller = null;
	private JSplitPane splitter = null;
	private GraphTransitionEditor condEditor = null;
	private JTable variableView = null;
	private JPanel rootView;
	private JPanel emptyPanel;
	private boolean graphPanel = false;
	private JComboBox graphTypeBox;
	private JPanel upPanel;
	private JButton upButton;
	private JList inputList;
	private JButton addInputButton;
	private JButton delInputButton;
	private JList outputList;
	private JButton addOutputButton;
	private JButton delOutputButton;
	private JTextField nameField;

	private class InOutListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		boolean input = false;

		public InOutListModel(boolean input) {
			this.input = input;
		}

		public int getSize() {
			return input ? graph.inputs.size() : graph.outputs.size();
		}

		public Object getElementAt(int index) {
			return input ? graph.inputs.get(index) : graph.outputs.get(index);
		}

		public void add() {
			String s = JOptionPane.showInputDialog(rootView, "Enter lvalue:",
					"Add graph " + (input ? "input" : "output"),
					JOptionPane.NO_OPTION);
			if (s != null) {
				int index = getSize();
				try {
					Expression e = new Expression(s);
					if (!e.isLeftValue()) {
						JOptionPane
								.showMessageDialog(rootView, "Expression '" + s
										+ "' is not a lvalue!",
										"Invalid expression",
										JOptionPane.ERROR_MESSAGE);
					}
					if (input)
						graph.inputs.add(e);
					else
						graph.outputs.add(e);
					fireIntervalAdded(this, index, index);
					internalChanged(null);
				} catch (ExpressionParseException exn) {
					JOptionPane.showMessageDialog(rootView, exn.getMessage(),
							"Invalid expression", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		public void delete() {
			int index = input ? inputList.getSelectedIndex() : outputList
					.getSelectedIndex();
			if (index >= 0) {
				if (input)
					graph.inputs.remove(index);
				else
					graph.outputs.remove(index);
				fireIntervalRemoved(this, index, index);
			}
			internalChanged(null);
		}
	}

	public BehaviourGraphFactory(boolean edit) {
		graph = new BehaviourGraph();
		createPanel(edit);
	}

	public BehaviourGraphFactory(boolean edit, BehaviourGraph graph) {
		this.graph = graph;
		createPanel(edit);
	}

	public String[] getAddableElement() {
		String[] elem = { "Behaviour", "Transition", "Interruption" };
		return elem;
	}

	public boolean isConnector(int elem) {
		return elem != 0;
	}

	public boolean isLine(int elem) {
		return elem != 0;
	}

	public boolean checkCanConnect(DrawableShape shape, int elem) {
		if (shape instanceof Circle) {
			// Begin node
			return elem == 1;
		} else if (shape instanceof GraphBehaviour) {
			return elem != 0;
		}
		return false;
	}

	public DrawableShape create(int elem) {
		if (elem == 0) {
			if (behaviourMap.size() == 0)
				return null;
			Object[] possibilities = behaviourMap.keySet().toArray();
			String s = (String) JOptionPane.showInputDialog(rootView,
					"Please select the behaviour to add:",
					"Behaviour selection", JOptionPane.PLAIN_MESSAGE, null,
					possibilities, possibilities[0]);
			if ((s != null) && (s.length() > 0) && behaviourMap.containsKey(s)) {
				Doublet<Integer, BehaviourFactory> el = behaviourMap.get(s);
				GraphBehaviour res = new GraphBehaviour(el.getSecond()
						.getBehaviour(el.getFirst()));
				res.factory = s;
				graph.behaviours.add(res);
				internalChanged(null);
				res.addListener(this);
				return res;
			}
		}
		return null;
	}

	public Connector create(int elem, DrawableShape source, DrawableShape dest) {
		if (!(dest instanceof GraphBehaviour))
			return null;
		if (elem == 1) {
			GraphTransition res = new GraphTransition(source, dest);
			if (source instanceof GraphBehaviour) {
				((GraphBehaviour) source).outputs.add(res);
			} else {
				graph.initLink.add(res);
			}
			res.addListener(this);
			internalChanged(null);
			return res;
		} else if (elem == 2) {
			GraphInterrupt res = new GraphInterrupt(source, dest);
			if (source instanceof GraphBehaviour) {
				((GraphBehaviour) source).outputs.add(res);
			} else {
				graph.initLink.add(res);
			}
			res.addListener(this);
			internalChanged(null);
			return res;
		}
		return null;
	}

	public void click(DrawableShape s) {
		if (s instanceof GraphBehaviour) {
			if (((GraphBehaviour) s).behaviour instanceof BehaviourGraph) {
				this.loadSubGraph((BehaviourGraph) (((GraphBehaviour) s).behaviour));
			}
		} else if (s instanceof Circle && graphList.size() > 0) {
			this.loadGraph_(graphList.pop());
		}
	}

	public boolean remove(DrawableShape s) {
		if (s instanceof Circle) {
			// Prevent removal of the source
			return false;
		} else if (s instanceof GraphBehaviour) {
			// Prevent removal of linked behaviours
			if (((GraphBehaviour) s).interrupts.size() > 0)
				return false;
			if (((GraphBehaviour) s).outputs.size() > 0)
				return false;
			if (graph.hasPrevious(((GraphBehaviour) s)))
				return false;
			graph.behaviours.remove(s);
			s.removeListener(this);
			internalChanged(null);
			return true;
		} else if (s instanceof GraphInterrupt) {
			GraphBehaviour source = (GraphBehaviour) ((GraphTransition) s)
					.getSource();
			source.interrupts.remove(s);
			s.removeListener(this);
			internalChanged(null);
			return true;
		} else if (s instanceof GraphTransition) {
			if (((GraphTransition) s).getSource() instanceof Circle) {
				graph.initLink.remove(s);
			} else {
				GraphBehaviour source = (GraphBehaviour) ((GraphTransition) s)
						.getSource();
				source.outputs.remove(s);
			}
			s.removeListener(this);
			internalChanged(null);
			return true;
		} else if (s instanceof GraphInterrupt) {
			GraphBehaviour source = (GraphBehaviour) ((GraphInterrupt) s)
					.getSource();
			source.interrupts.remove(s);
			s.removeListener(this);
			internalChanged(null);
			return true;
		}
		return false;
	}

	private void createEditorPanel(boolean edit, Object constraints) {
		emptyPanel = new JPanel(new GridBagLayout());
		if (edit) {

			splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitter.setLeftComponent(scroller);
		} else {
			splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroller,
					new JScrollPane(variableView));
		}
		rootView.add(splitter, constraints);
		splitter.setResizeWeight(1);
		splitter.setDividerLocation(0.8);
		rootView.revalidate();
	}

	private Component editPanel(Component editor, boolean graph) {
		if (!graph)
			return editor;
		if (graphTypeBox == null) {
			graphTypeBox = new JComboBox();
			graphTypeBox.setActionCommand("graph:type");
		}
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		emptyPanel.removeAll();
		emptyPanel.add(graphTypeBox, c);
		graphTypeBox.removeActionListener(this);
		((DefaultComboBoxModel) graphTypeBox.getModel()).removeAllElements();
		for (String s : behaviourMap.keySet())
			((DefaultComboBoxModel) graphTypeBox.getModel()).addElement(s);
		graphTypeBox.addActionListener(this);
		if (editor != null) {
			c.weighty = 1;
			emptyPanel.add(editor, c);
		}
		return emptyPanel;
	}

	private void setEditor(Component editor, boolean graph) {
		if (editor == editorPanel && graph == this.graphPanel)
			return;
		editorPanel = editor;
		this.graphPanel = graph;
		splitter.setRightComponent(editPanel(editor, graph));
		rootView.revalidate();
	}

	private void setEditor(Component editor) {
		setEditor(editor, false);
	}

	public void select(DrawableShape s) {
		if (s == null) {
			setEditor(null);
		} else if (s instanceof GraphBehaviour) {
			GraphBehaviour b = (GraphBehaviour) s;
			if (b.factory != "") {
				if (behaviourMap.containsKey(b.factory)) {
					setEditor(behaviourMap.get(b.factory).getSecond()
							.getEditor(this, b.behaviour), true);
				} else
					setEditor(null, true);
				graphTypeBox.setSelectedItem(b.factory);
			} else
				setEditor(null, false);
		} else if (s instanceof GraphTransition) {
			if (condEditor == null) {
				condEditor = new GraphTransitionEditor();
				condEditor.addGuiModificationListener(this);
			}
			condEditor.setCondition((GraphTransition) s);
			setEditor(condEditor);
		} else if (s instanceof Circle) {
			upButton.setVisible(graphList.size() > 0);
			setEditor(upPanel);
		} else {
			setEditor(null);
		}
	}

	public BehaviourGraph getGraph() {
		return graph;
	}

	public BehaviourGraph getRootGraph() {
		if (graphList.isEmpty())
			return graph;
		return graphList.get(0);
	}

	public void setGraph(boolean resetZoom) {
		schemeView.removeAll();
		schemeView.add(graph.orig);

		for (GraphBehaviour b : graph.behaviours) {
			schemeView.add(b);
			b.addListener(this);
		}
		for (GraphTransition i : graph.initLink) {
			schemeView.add(i);
			i.addListener(this);
		}
		for (GraphBehaviour b : graph.behaviours) {
			for (GraphTransition i : b.outputs) {
				schemeView.add(i);
				i.addListener(this);
			}
			for (GraphInterrupt i : b.interrupts) {
				schemeView.add(i);
				i.addListener(this);
			}
		}
		if (nameField != null) {
			nameField.setText(graph.getName());
			delInputButton.setEnabled(false);
			delOutputButton.setEnabled(false);
			inputList.revalidate();
			outputList.revalidate();
		}
		if (rootView != null) {
			if (resetZoom)
				schemeView.setZoom(1);
			rootView.repaint();
		}
	}

	private void createView(boolean edit) {
		schemeView = new SchemeView(this);
		setGraph(true);
		schemeView.addListener(this);
		schemeView.setEditable(edit);
	}

	private void createUpPanel() {
		GridBagLayout layout = new GridBagLayout();
		upPanel = new JPanel(layout);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 0;
		upButton = new JButton("Up");
		upButton.setActionCommand("graph:up");
		upButton.addActionListener(this);
		upPanel.add(upButton, c);
		nameField = new JTextField(graph.getName(), 10);
		nameField.setActionCommand("graph:name");
		nameField.addActionListener(this);
		upPanel.add(new JLabel("Graph name:"), c);
		upPanel.add(nameField, c);

		inputList = new JList(new InOutListModel(true));
		outputList = new JList(new InOutListModel(false));
		addInputButton = new JButton("+");
		delInputButton = new JButton("-");
		addOutputButton = new JButton("+");
		delOutputButton = new JButton("-");
		addInputButton.setToolTipText("Add graph input");
		delInputButton.setToolTipText("Delete graph input");
		addOutputButton.setToolTipText("Add graph output");
		delOutputButton.setToolTipText("Delete graph output");
		addInputButton.setActionCommand("add_input");
		delInputButton.setActionCommand("del_input");
		addOutputButton.setActionCommand("add_output");
		delOutputButton.setActionCommand("del_output");
		addInputButton.addActionListener(this);
		delInputButton.addActionListener(this);
		addOutputButton.addActionListener(this);
		delOutputButton.addActionListener(this);
		delInputButton.setEnabled(false);
		delOutputButton.setEnabled(false);

		inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		inputList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				delInputButton.setEnabled(inputList.getSelectedIndex() >= 0);
			}
		});
		outputList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				delOutputButton.setEnabled(outputList.getSelectedIndex() >= 0);
			}
		});

		c.gridwidth = 1;
		upPanel.add(new JLabel("Graph inputs:"), c);
		c.weightx = 0;
		upPanel.add(addInputButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		upPanel.add(delInputButton, c);
		c.weightx = 1;
		c.weighty = 1;
		upPanel.add(inputList, c);
		c.gridwidth = 1;
		c.weighty = 0;
		upPanel.add(new JLabel("Graph outputs:"), c);
		c.weightx = 0;
		upPanel.add(addOutputButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		upPanel.add(delOutputButton, c);
		c.weightx = 1;
		c.weighty = 1;
		upPanel.add(outputList, c);
	}

	private JToolBar createToolBar() {
		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		JSeparator jsep = new JSeparator() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
			}
		};
		jsep.setBorder(null);
		JButton tbb = new JButton("SVG");
		tbb.addActionListener(this);
		tbb.setActionCommand("svg");
		tbb.setToolTipText("Save graph as SVG");
		tb.add(tbb);
		tb.add(jsep);
		tbb = new JButton("+");
		tbb.addActionListener(this);
		tbb.setActionCommand("+");
		tbb.setToolTipText("Zoom in");
		tb.add(tbb);
		tbb = new JButton("-");
		tbb.addActionListener(this);
		tbb.setActionCommand("-");
		tbb.setToolTipText("Zoom out");
		tb.add(tbb);
		tb.setFloatable(false);
		return tb;
	}

	private JFileChooser svgChooser = null;

	private void createRootView(boolean edit) {
		rootView = new JPanel();
		rootView.setLayout(new GridBagLayout());

		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		rootView.add(createToolBar(), c);

		c.weighty = 1;
		createEditorPanel(edit, c);
	}

	private void createPanel(boolean edit) {
		if (edit) {
			createUpPanel();
		} else {
			variableView = new JTable(new VariableTableModel());
		}
		createView(edit);
		scroller = new JScrollPane(schemeView);
		schemeView.setParentPane(scroller);
		createRootView(edit);
	}

	private class VariableTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return graph.varSet.getNames().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			int i = 0;
			for (String s : graph.varSet.getNames()) {
				if (i == rowIndex) {
					return columnIndex == 0 ? s : graph.varSet.getValue(s)
							.toString();
				}
				i++;
			}
			return null;
		}

		public String getColumnName(int column) {
			return column == 0 ? "Name" : "Value";
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	public SchemeView getSchemeView() {
		return schemeView;
	}

	public Component getRootView() {
		return rootView;
	}

	public void loadGraph(BehaviourGraph graph) {
		graphList.clear();
		loadGraph_(graph);
	}

	private void loadGraph_(BehaviourGraph graph) {
		if (this.graph != graph) {
			this.graph = graph;
			setGraph(true);
		}
	}

	public void loadSubGraph(BehaviourGraph graph) {
		graphList.push(this.graph);
		this.graph = graph;
		setGraph(true);
	}

	public void loadSubGraphs(BehaviourGraph[] graph) {
		if (graph == null) {
			if (graphList.size() > 0) {
				if (this.graph != graphList.firstElement()) {
					this.graph = graphList.firstElement();
					setGraph(true);
				}
				graphList.clear();
			}
			return;
		}
		if (graph.length == graphList.size()) {
			boolean cont = true;
			for (int i = 0; cont && i < graph.length; i++) {
				cont = (graph[i] == graphList.get(i));
			}
			if (cont)
				return;
		}
		BehaviourGraph oldGraph = this.graph;
		if (graphList.size() > 0)
			this.graph = graphList.firstElement();
		graphList.clear();
		graphList.add(this.graph);
		for (int i = 0; i < graph.length - 1; i++)
			graphList.add(graph[i]);

		this.graph = graph[graph.length - 1];
		setGraph(this.graph != oldGraph);
	}

	public void updateSubGraphs(BehaviourGraph[] path) {
		if (graph != null) {
			if (graphList.size() >= path.length) {
				BehaviourGraph oldGraph = this.graph;
				while (graphList.get(path.length - 1) == path[path.length - 1]) {
					this.graph = graphList.pop();
				}
				setGraph(this.graph != oldGraph);
			}
		}
	}

	private JFileChooser chooser = null;

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("graph:save")) {
			if (chooser == null)
				chooser = new JFileChooser();
			try {
				int returnVal = chooser.showSaveDialog(rootView);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saveXML(chooser.getSelectedFile().getCanonicalPath());
				}
			} catch (Exception exn) {
				JOptionPane.showMessageDialog(rootView, "Save failed!");
			}
		} else if (e.getActionCommand().equals("graph:load")) {
			if (chooser == null)
				chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(rootView);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					if (!loadXML(chooser.getSelectedFile().getCanonicalPath())) {
						JOptionPane.showMessageDialog(rootView,
								"Loading failed!");
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(rootView, "Loading failed!");
				}
			}
		} else if (e.getActionCommand().equals("graph:up")) {
			if (graphList.size() > 0) {
				loadGraph_(graphList.pop());
				representationChanged(null);
			}
		} else if (e.getActionCommand().equals("graph:name")) {
			graph.setName(nameField.getText());
			if (chooser != null) {
				try {
					String path = chooser.getCurrentDirectory()
							.getCanonicalPath()
							+ File.separator
							+ graph.getName() + ".xml";
					chooser.setSelectedFile(new File(path));
				} catch (IOException e1) {
				}
			}
			representationChanged(null);
		} else if (e.getActionCommand().equals("svg")) {
			if (svgChooser == null) {
				svgChooser = new JFileChooser();
				FileFilter ff = new FileNameExtensionFilter("SVG Files", "svg");
				svgChooser.addChoosableFileFilter(ff);
				svgChooser.setFileFilter(ff);
			}
			if (svgChooser.showSaveDialog(rootView) == JFileChooser.APPROVE_OPTION) {
				try {
					schemeView.saveSVG(svgChooser.getSelectedFile());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(rootView, e1.getMessage(),
							"Error during SVG export...",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getActionCommand().equals("+")) {
			schemeView.setZoom(schemeView.getZoom() * 2);
		} else if (e.getActionCommand().equals("-")) {
			schemeView.setZoom(schemeView.getZoom() / 2);
		} else if (e.getActionCommand().equals("del_input")) {
			((InOutListModel) inputList.getModel()).delete();
		} else if (e.getActionCommand().equals("add_input")) {
			((InOutListModel) inputList.getModel()).add();
		} else if (e.getActionCommand().equals("del_output")) {
			((InOutListModel) outputList.getModel()).delete();
		} else if (e.getActionCommand().equals("add_output")) {
			((InOutListModel) outputList.getModel()).add();
		} else if (e.getActionCommand().equals("graph:type")) {
			DrawableShape s = schemeView.getSelected();
			if (s != null && (s instanceof GraphBehaviour)
					&& graphTypeBox != null
					&& graphTypeBox.getSelectedItem() != null) {
				GraphBehaviour b = (GraphBehaviour) s;
				String name = graphTypeBox.getSelectedItem().toString();
				if (behaviourMap.containsKey(name) && !name.equals(b.factory)) {
					Doublet<Integer, BehaviourFactory> el = behaviourMap
							.get(name);
					b.behaviour = el.getSecond().getBehaviour(el.getFirst());
					b.factory = name;
					internalChanged(null);
					select(b);
				} else {
					graphTypeBox.setSelectedItem(b.factory);
				}
			}
		}
	}

	public boolean loadXML(String filename) throws Exception {
		XMLEntity xe = XMLInterpretor.convert(filename, null);
		if (xe instanceof BehaviourGraph) {
			BehaviourGraph g = (BehaviourGraph) xe;
			loadGraph(g);
			return true;
		} else
			return false;
	}

	public void saveXML(String filename) throws FileNotFoundException,
			Exception {
		String name = (new File(filename)).getName();
		name = name.split("\\.")[0];
		graph.setName(name);
		XMLInterpretor.convert(graph, null, new FileOutputStream(filename));
	}

	private HashSet<GuiModificationListener> guiListeners = new HashSet<GuiModificationListener>();

	public void addGuiModificationListener(GuiModificationListener l) {
		guiListeners.add(l);
	}

	public void removeGuiModificationListener(GuiModificationListener l) {
		guiListeners.remove(l);
	}

	private Object[] expandGuiEvent(Object[] o) {
		Object[] r;
		int s = 0;
		if (o != null)
			s = o.length;

		r = new Object[s + 1 + graphList.size()];
		int i = 0;
		for (BehaviourGraph g : graphList) {
			r[i] = g;
			i++;
		}
		r[graphList.size()] = graph;
		if (o != null) {
			for (i = 0; i < o.length; i++) {
				r[i + 1 + graphList.size()] = o[i];
			}
		}
		return r;
	}

	@Override
	public void internalChanged(Object[] o) {
		if (rootView != null)
			rootView.repaint();
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(r);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		if (rootView != null)
			rootView.repaint();
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(r);
		}
	}

	@Override
	public void changed(DrawableShape s) {
		representationChanged(new Object[] { s });

	}

	@Override
	public void moved(DrawableShape s) {
		representationChanged(new Object[] { s });
	}

	@Override
	public void resized(DrawableShape s) {
	}

	@Override
	public boolean canEditObject(Object o) {
		if (o == null)
			return false;
		return (o instanceof BehaviourGraph) || (o instanceof BehaviourGraph[]);
	}

	@Override
	public Component getEditor() {
		return getRootView();
	}

	@Override
	public Icon getIcon() {
		return behaviourIcon;
	}

	@Override
	public String getTitle() {
		if (treeModel == null)
			return null;
		return treeModel.getTitle(getRootGraph());
	}

	@Override
	public boolean isModified() {
		if (treeModel == null)
			return false;
		return treeModel.isModified(getRootGraph());
	}

	@Override
	public void setObject(Object o) {
		if (o == null)
			return;
		if (o instanceof BehaviourGraph) {
			loadGraph((BehaviourGraph) o);
			if (treeModel != null) {
				XMLEntity parent = treeModel.getParent(o);
				if (parent != null) {
					if (parent instanceof Model) {
						setAvailableBehaviours(((Model) parent)
								.getAvailableBehaviours());
					} else if (parent instanceof World) {
						setAvailableBehaviours(((World) parent)
								.getAvailableBehaviours());
					} else
						setAvailableBehaviours(null);
				} else
					setAvailableBehaviours(null);
			} else {
				setAvailableBehaviours(null);
			}
		} else if (o instanceof BehaviourGraph[]) {
			loadSubGraphs((BehaviourGraph[]) o);
			if (treeModel != null) {
				XMLEntity parent = treeModel
						.getParent(((BehaviourGraph[]) o)[0]);
				if (parent != null) {
					if (parent instanceof Model) {
						setAvailableBehaviours(((Model) parent)
								.getAvailableBehaviours());
					} else if (parent instanceof World) {
						setAvailableBehaviours(((World) parent)
								.getAvailableBehaviours());
					} else
						setAvailableBehaviours(null);
				} else
					setAvailableBehaviours(null);
			} else {
				setAvailableBehaviours(null);
			}
		}

	}

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	public void setAvailableBehaviours(Collection<Behaviour> behaviours) {
		this.behaviours = behaviours;
	}

	public Collection<Behaviour> getAvailableBehaviours() {
		return behaviours;
	}

	public void replaceBehaviour(Behaviour behaviour1, Behaviour behaviour2) {
		XMLEntity parent = treeModel.getParent(graph);
		boolean inModel = (parent != null) && (parent instanceof Model);
		for (GraphBehaviour b : graph.behaviours) {
			if (b.behaviour == behaviour1) {
				b.behaviour = inModel ? behaviour2 : behaviour2.copy();
				select(b);
				if (treeModel != null) {
					treeModel.treeStructureChanged(behaviour2);
				}
				representationChanged(new Object[] { b.behaviour });
				return;
			}
		}
	}

	public void refresh() {
		if (variableView != null) {
			((VariableTableModel) variableView.getModel())
					.fireTableDataChanged();
		}
		rootView.repaint();
	}

	@Override
	public void mouseMoved(double x, double y) {
	}

	@Override
	public void mouseOut() {
	}
}
