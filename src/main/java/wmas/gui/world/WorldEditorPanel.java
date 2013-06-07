package wmas.gui.world;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import wmas.gui.EditorInterface;
import wmas.gui.GuiModificationListener;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.SchemeViewListener;
import wmas.gui.shapes.ShapeFactory;
import wmas.gui.shapes.ShapeListener;
import wmas.gui.shapes.elements.Connector;
import wmas.gui.world.entity.EntityEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.world.EntityInterface;
import wmas.world.EntityReference;
import wmas.world.World;

public class WorldEditorPanel extends JPanel implements SchemeViewListener,
		ShapeFactory, GuiModificationListener, ShapeListener, EditorInterface,
		ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Icon worldIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/world.gif"));

	private SchemeView worldView;
	private EntityEditor entityEditor;
	private WorldObjectTreeModel treeModel = null;
	private JTextField posField;

	private HashMap<DrawableShape, EntityInterface> shapeMap = new HashMap<DrawableShape, EntityInterface>();
	private World world = null;
	private JTree worldTree = null;
	private JFileChooser chooser = new JFileChooser();

	public WorldEditorPanel(World w) {
		super(new GridBagLayout());
		this.world = w;
		FileFilter ff = new FileNameExtensionFilter("SVG Files", "svg");
		chooser.addChoosableFileFilter(ff);
		chooser.setFileFilter(ff);
		construct();
		if (w != null)
			transfer();
	}

	private void setEntity(EntityInterface e) {
		if (e == null) {
			entityEditor.setVisible(false);
		} else {
			entityEditor.setVisible(true);
			entityEditor.setEntity(e, true);
		}
	}

	private void transfer() {
		shapeMap.clear();
		worldView.removeAll();
		for (EntityInterface e : world.listEntities()) {
			if (e.getDrawingShape() != null) {
				shapeMap.put(e.getDrawingShape(), e);
				worldView.add(e.getDrawingShape());
			}
		}
		setEntity(null);
	}

	public void setWorld(World w) {
		this.world = w;
		entityEditor.setAvailableBehaviours(world.getAvailableBehaviours(),
				true);
		transfer();
	}

	private void construct() {
		posField = new JTextField();
		posField.setOpaque(false);
		posField.setBackground(this.getBackground());
		posField.setBorder(null);
		worldView = new SchemeView(this);
		worldView.setEditable(true);
		worldView.addListener(this);

		entityEditor = new EntityEditor(world.getAvailableBehaviours(), true);
		entityEditor.setParent(worldView, null);
		entityEditor.setWorldEditor(this);
		entityEditor.setVisible(false);
		entityEditor.addGuiModificationListener(this);

		GridBagConstraints c = new GridBagConstraints();
		JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
		JButton tbb = new JButton("^");
		tbb.addActionListener(this);
		tbb.setActionCommand("up");
		tbb.setToolTipText("Move selected entity up");
		tb.add(tbb);
		tbb = new JButton("v");
		tbb.addActionListener(this);
		tbb.setActionCommand("down");
		tbb.setToolTipText("Move selected entity down");
		tb.add(tbb);

		tb.add(new JSeparator(JSeparator.VERTICAL));
		posField.setHorizontalAlignment(JLabel.CENTER);
		tb.add(posField);
		tb.add(new JSeparator(JSeparator.VERTICAL));

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
		tbb = new JButton("SVG...");
		tbb.addActionListener(this);
		tbb.setActionCommand("svg");
		tbb.setToolTipText("Export diagram in SVG format...");
		tb.add(tbb);
		tb.setFloatable(false);

		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		add(tb, c);

		c.gridheight = 1;
		c.gridwidth = 1;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		worldView.setParentPane(this);

		JScrollPane sP = new JScrollPane(worldView);
		worldView.setParentPane(sP);
		add(sP, c);

		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		add(entityEditor, c);
	}

	@Override
	public void click(DrawableShape s) {
	}

	@Override
	public boolean remove(DrawableShape s) {
		if (shapeMap.containsKey(s)) {
			remove(shapeMap.get(s));
			return true;
		}
		return false;
	}

	public void changeShape(EntityInterface e, DrawableShape oldShape) {
		if (oldShape != null)
			shapeMap.remove(oldShape);
		if (e.getDrawingShape() != null) {
			shapeMap.put(e.getDrawingShape(), e);
		}
		select(e);
	}

	public void moveUp(EntityInterface e) {
		if (e == null)
			return;
		if (e.getDrawingShape() != null) {
			worldView.moveUp(e.getDrawingShape());
		}
		int index = world.listEntities().indexOf(e);
		if (index > 0) {
			world.listEntities().remove(e);
			world.listEntities().add(index - 1, e);
		}
		internalChanged(null);
	}

	public void moveDown(EntityInterface e) {
		if (e == null)
			return;
		if (e.getDrawingShape() != null) {
			worldView.moveDown(e.getDrawingShape());
		}
		int index = world.listEntities().indexOf(e);
		if (index >= 0 && index < world.listEntities().size() - 1) {
			world.listEntities().remove(e);
			world.listEntities().add(index + 1, e);
		}
		internalChanged(null);
	}

	private void doAdd(EntityInterface e) {
		if (e.getDrawingShape() != null) {
			e.getDrawingShape().addListener(this);
			shapeMap.put(e.getDrawingShape(), e);
		}
		e.setParent(world);
		world.listEntities().add(e);
		internalChanged(null);
	}

	public void add(EntityInterface e) {
		if (e.getDrawingShape() != null) {
			worldView.add(e.getDrawingShape());
		}
		doAdd(e);
	}

	public void remove(EntityInterface e) {
		if (e.getDrawingShape() != null)
			shapeMap.remove(e.getDrawingShape());
		world.listEntities().remove(e);
		internalChanged(null);
	}

	private void doSelect(EntityInterface e) {
		if (e != null) {
			entityEditor.setEntity(e, true);
			entityEditor.setVisible(true);
		} else {
			entityEditor.setVisible(false);
		}
		entityEditor.revalidate();
		revalidate();
	}

	@Override
	public void select(DrawableShape s) {
		if (shapeMap.containsKey(s)) {
			doSelect(shapeMap.get(s));
		} else if (s == null)
			doSelect(null);
	}

	public void select(EntityInterface e) {
		if (e.getDrawingShape() != null) {
			worldView.select(e.getDrawingShape());
		} else {
			worldView.select(null);
			doSelect(e);
		}
	}

	public void setWorldTree(JTree worldTree) {
		this.worldTree = worldTree;
	}

	private void createEntities(boolean copy) {
		if (this.worldTree != null) {
			TreePath p = this.worldTree.getSelectionPath();
			if (p != null) {
				Object c = p.getLastPathComponent();
				if (c instanceof EntityInterface) {
					EntityGenerator.getInstance(this).display(
							(EntityInterface) c, lastX, lastY, copy);
				}
			}
		}
	}

	private EntityInterface createEntity(boolean copy) {
		if (this.worldTree != null) {
			TreePath p = this.worldTree.getSelectionPath();
			if (p == null)
				return null;
			Object c = p.getLastPathComponent();
			if (c instanceof EntityInterface) {
				if (copy)
					return ((EntityInterface) c).copy();
				else
					return new EntityReference((EntityInterface) c);
			}
		}
		return null;
	}

	@Override
	public DrawableShape create(int elem) {
		EntityInterface e;
		switch (elem) {
		case 0:
			e = createEntity(true);
			if (e != null) {
				doAdd(e);
				return e.getDrawingShape();
			}
			break;
		case 1:
			createEntities(true);
			break;
		case 2:
			e = createEntity(false);
			if (e != null) {
				doAdd(e);
				return e.getDrawingShape();
			}
			break;
		case 3:
			createEntities(false);
			break;
		}
		return null;
	}

	@Override
	public String[] getAddableElement() {
		return new String[] { "a copy of selected tree element",
				"several copies of selected tree element",
				"a reference of selected tree element",
				"several references of selected tree element" };
	}

	@Override
	public boolean isLine(int elem) {
		if (this.worldTree != null) {
			TreePath p = this.worldTree.getSelectionPath();
			if (p == null)
				return false;
			Object c = p.getLastPathComponent();
			if (c instanceof EntityInterface) {
				DrawableShape s = ((EntityInterface) c).getDrawingShape();
				if (s == null)
					return false;
				return s.isLine();
			}
		}
		return false;
	}

	// No connectors in the world
	@Override
	public boolean checkCanConnect(DrawableShape shape, int elem) {
		return false;
	}

	@Override
	public boolean isConnector(int elem) {
		return false;
	}

	@Override
	public Connector create(int elem, DrawableShape source, DrawableShape dest) {
		return null;
	}

	public World getWorld() {
		return world;
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
		if (o != null) {
			r = new Object[1 + o.length];
			int i = 0;
			r[0] = world;
			for (i = 0; i < o.length; i++) {
				r[i + 1] = o[i];
			}
		} else
			r = new Object[] { world };
		return r;
	}

	@Override
	public void internalChanged(Object[] o) {
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(r);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(r);
		}
	}

	@Override
	public void changed(DrawableShape s) {
		internalChanged(null);
	}

	@Override
	public void moved(DrawableShape s) {
		internalChanged(null);
	}

	@Override
	public void resized(DrawableShape s) {
		internalChanged(null);
	}

	@Override
	public boolean canEditObject(Object o) {
		if (o == null)
			return false;
		return o instanceof World;
	}

	@Override
	public Component getEditor() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return worldIcon;
	}

	@Override
	public String getTitle() {
		if (treeModel == null)
			return null;
		return treeModel.getTitle(getWorld());
	}

	@Override
	public boolean isModified() {
		if (treeModel == null)
			return false;
		return treeModel.isModified(getWorld());
	}

	@Override
	public void setObject(Object o) {
		if (o instanceof World)
			setWorld((World) o);
		else if (o instanceof EntityInterface) {
			select((EntityInterface) o);
		}
	}

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			worldView.setZoom(worldView.getZoom() * 2);
		} else if (e.getActionCommand().equals("-")) {
			worldView.setZoom(worldView.getZoom() / 2);
		} else if (e.getActionCommand().equals("up")) {
			moveUp(entityEditor.getEntity());
		} else if (e.getActionCommand().equals("down")) {
			moveDown(entityEditor.getEntity());
		} else if (e.getActionCommand().equals("svg")) {
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					worldView.saveSVG(chooser.getSelectedFile());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(),
							"Error during SVG export...",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void refresh() {
		repaint();
	}

	private double lastX = -1;
	private double lastY = -1;

	@Override
	public void mouseMoved(double x, double y) {
		lastX = x;
		lastY = y;
		posField.setText("(" + x + ", " + y + ")");

	}

	@Override
	public void mouseOut() {
		posField.setText("");
	}

	public Container parentFrame() {
		Container parent = getParent();
		while (parent != null && !(parent instanceof Frame)
				&& !(parent instanceof Dialog)) {
			parent = parent.getParent();
		}
		return parent;
	}

	public void deleted(EntityInterface deleted) {
		if (deleted.getDrawingShape() != null) {
			worldView.remove(deleted.getDrawingShape());
			shapeMap.remove(deleted.getDrawingShape());
		}
		if (entityEditor.getEntity() == deleted && entityEditor.isVisible())
			doSelect(null);
	}
}
