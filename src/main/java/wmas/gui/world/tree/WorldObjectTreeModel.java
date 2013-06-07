package wmas.gui.world.tree;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.GraphBehaviour;
import wmas.expression.functions.FunctionList;
import wmas.gui.world.ModelImportDialog;
import wmas.gui.world.WorldEditor;
import wmas.util.Doublet;
import wmas.world.Entity;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class WorldObjectTreeModel extends WorldObjectTreeDataModel implements
		TreeModelListener {

	public WorldObjectTreeModel() {
		super(null);
		this.fileHandler = new WorldObjectFileHandler(this);
	}

	public boolean load(File f) {
		try {
			FunctionList.clearSession();
			root = XMLInterpretor.convert(f.getCanonicalPath(), null);
			fileHandler.reset(f);
			Object[] v = new Object[1];
			v[0] = root;
			treeStructureChanged(new TreeModelEvent(this, v));
		} catch (Exception exn) {
			exn.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean load(XMLEntity root) {
		try {
			this.root = root;
			fileHandler.reset();
			Object[] v = new Object[1];
			v[0] = root;
			treeStructureChanged(new TreeModelEvent(this, v));
		} catch (Exception exn) {
			return false;
		}
		return true;
	}

	public boolean addModel(File f) {
		try {
			if (root != null || root instanceof World) {
				XMLEntity xe = fileHandler.loadFile(f, (World) root);
				if (xe != null) {
					Object[] v = new Object[1];
					v[0] = root;
					treeStructureChanged(new TreeModelEvent(this, v));
				}
			} else
				return false;
		} catch (Exception exn) {
			return false;
		}
		return true;
	}

	public boolean save(File f) {
		File oldDir = fileHandler.rootDirectory;

		fileHandler.convertPath(f.getParentFile());
		try {
			XMLInterpretor.convert(root, null, new FileOutputStream(f));
			fileHandler.saved((String) null);
			return true;
		} catch (Exception exn) {
			fileHandler.convertPath(oldDir);
			return false;
		}
	}

	public boolean saveAll(File f) {
		for (String s : fileHandler) {
			if (s == null) {
				if (!this.save(f))
					return false;
			} else
				this.saveSelectedFile(s);
		}
		return true;
	}

	public boolean saveSelectedFile(String f) {
		if (f == null) {
			return false; // We cannot save root file, we don't have a filename
							// for it.
		}
		XMLEntity xe = fileHandler.get(f);
		if (xe == null)
			return false;
		try {
			XMLInterpretor.convert(xe, null, new FileOutputStream(f));
			fileHandler.saved(f);
			return true;
		} catch (Exception exn) {
			return false;
		}
	}

	public boolean saveSelectedFile(Object node, File rootFile) {
		String fileName = fileHandler.getFileOfObject(node);
		if (fileName == null)
			return save(rootFile);
		else if (fileName.length() == 0)
			return false;
		return saveSelectedFile(fileName);
	}

	private static String newBehaviourName(Iterable<Behaviour> behaviours) {
		int i = 1;
		HashSet<String> s = new HashSet<String>();
		for (Behaviour e : behaviours)
			s.add(e.toString());
		while (s.contains("Graph:Behaviour" + i))
			i++;
		return "Behaviour" + i;
	}

	private static String newEntityName(
			Iterable<? extends EntityInterface> entities) {
		int i = 1;
		HashSet<String> s = new HashSet<String>();
		for (EntityInterface e : entities)
			s.add(e.getName());
		while (s.contains("Entity" + i))
			i++;
		return "Entity" + i;
	}

	public void addBehaviour(TreePath treePath) {
		Doublet<TreePath, Model> m = getParentModel(treePath);
		if (m == null)
			return;
		BehaviourGraph g = new BehaviourGraph();
		g.setName(newBehaviourName(m.getSecond().getAvailableBehaviours()));
		m.getSecond().getAvailableBehaviours().add(g);
		modified(m.getFirst(), m.getSecond());
	}

	public void addAttribute(Container parent, TreePath treePath) {
		Doublet<TreePath, XMLEntity> m = getParentModelWorld(treePath);
		if (m == null) {
			m = new Doublet<TreePath, XMLEntity>(new TreePath(root), root);
		}
		HashSet<String> availableAttributes = new HashSet<String>();
		availableAttributes.addAll(WorldEditor.listAttributes());
		if (m.getSecond() instanceof Model) {
			availableAttributes.removeAll(((Model) m.getSecond())
					.getAttributes());
		} else if (m.getSecond() instanceof World) {
			availableAttributes.removeAll(((World) m.getSecond())
					.getAttributes());
		} else
			return;
		Object[] possibilities = availableAttributes.toArray();
		if (possibilities.length > 0) {
			Object s = JOptionPane.showInputDialog(parent,
					"Please select the attribute to add:",
					"Attribute selection", JOptionPane.PLAIN_MESSAGE, null,
					possibilities, possibilities[0]);
			if ((s != null)) {
				if (m.getSecond() instanceof Model) {
					((Model) m.getSecond()).addAttribute(s.toString(),
							WorldEditor.createAttribute(s.toString()));
				} else if (m.getSecond() instanceof World) {
					((World) m.getSecond()).addAttribute(s.toString(),
							WorldEditor.createAttribute(s.toString()));
				}
				modified(m.getFirst(), m.getSecond());
			}
		}
	}

	public void addEntity(TreePath treePath) {
		Doublet<TreePath, XMLEntity> m = getParentModelWorld(treePath);
		if (m == null) {
			m = new Doublet<TreePath, XMLEntity>(new TreePath(root), root);
		}
		EntityInterface e = new Entity();
		if (m.getSecond() instanceof Model) {
			e.setName(newEntityName(((Model) m.getSecond()).getEntities()));
			e.setParent(m.getSecond());
			((Model) m.getSecond()).getEntities().add(e);
		} else if (m.getSecond() instanceof World) {
			e.setName(newEntityName(((World) m.getSecond()).listEntities()));
			e.setParent(m.getSecond());
			((World) m.getSecond()).listEntities().add(e);
		}
		modified(m.getFirst(), m.getSecond());
	}

	private void modified(TreePath path, Object m) {
		fileHandler.setModified(m, true);
		treeStructureChanged(new TreeModelEvent(this, path));
	}

	@SuppressWarnings("unchecked")
	public void delete(TreePath treePath, WorldEditor editor) {
		Doublet<TreePath, XMLEntity> m = getParentObject(treePath);
		if (m == null)
			return;
		if (m.getFirst().getPathCount() >= treePath.getPathCount())
			return;
		if (m.getSecond() instanceof Model) {
			// Can be two things : entity or behaviour
			Object o = treePath.getPathComponent(m.getFirst().getPathCount());
			if (o instanceof EntityInterface) {
				((Model) m.getSecond()).getEntities().remove(o);
				objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
			} else if (o instanceof Behaviour) {
				// We delete from the model
				((Model) m.getSecond()).getAvailableBehaviours().remove(o);
				// And from the entities
				for (EntityInterface e : ((Model) m.getSecond()).getEntities()) {
					e.removeBehaviour((Behaviour) o);
				}
				objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
			} else if (o instanceof Doublet) {
				String k = ((Doublet<String, XMLEntity>) o).getFirst();
				((Model) m.getSecond()).removeAttribute(k);
				objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
			}

		} else if (m.getSecond() instanceof World) {
			// Can be two things : entity or model
			Object o = treePath.getPathComponent(m.getFirst().getPathCount());
			if (o instanceof EntityInterface) {
				((World) m.getSecond()).listEntities().remove(o);
				editor.refreshWorld((World) m.getSecond(), (EntityInterface) o);
				objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
			} else if (o instanceof Doublet
					&& ((Doublet<?, ?>) o).getFirst() instanceof World) {
				String k = ((Doublet<World, String>) o).getSecond();
				Object d = ((World) m.getSecond()).getModels().remove(k);
				if (d != null) {
					fileHandler.remove(d);
					objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
				}
			} else if (o instanceof Doublet) {
				String k = ((Doublet<String, XMLEntity>) o).getFirst();
				((World) m.getSecond()).removeAttribute(k);
				objectDeleted(null, editor, m.getFirst(), m.getSecond(), o);
			}
		} else if (m.getSecond() instanceof BehaviourGraph) {
			// Can only be a behaviour
			Object o = treePath.getPathComponent(m.getFirst().getPathCount());
			BehaviourGraph parent = (BehaviourGraph) m.getSecond();
			if (!parent.hasPrevious(o)) {
				if (o instanceof Behaviour) {
					parent.remove((Behaviour) o);
					objectDeleted(treePath, editor, m.getFirst(),
							m.getSecond(), o);
				}
			}
		} else if (m.getSecond() instanceof EntityInterface) {
			// Can only be a behaviour
			Object o = treePath.getPathComponent(m.getFirst().getPathCount());
			EntityInterface parent = (EntityInterface) m.getSecond();
			if (o instanceof Behaviour) {
				parent.removeBehaviour((Behaviour) o);
				objectDeleted(treePath, editor, m.getFirst(), m.getSecond(), o);
			}
		}
	}

	private void objectDeleted(TreePath treePath, WorldEditor editor,
			TreePath parentPath, Object parent, Object o) {
		editor.deleting(parentPath.pathByAddingChild(o));
		if (treePath != null)
			parentPath = getParentModelWorld(treePath).getFirst();
		modified(parentPath, parent);
	}

	private JFileChooser chooser = new JFileChooser();
	private ModelImportDialog importDialog = null;

	private void constructImportDialog(Container parent) {
		while (parent != null && !(parent instanceof Frame)
				&& !(parent instanceof Dialog))
			parent = parent.getParent();
		if (parent == null)
			importDialog = new ModelImportDialog();
		else if (parent instanceof Frame)
			importDialog = new ModelImportDialog((Frame) parent);
		else
			importDialog = new ModelImportDialog((Dialog) parent);
	}

	private boolean doImportModel(Container parent, Model m, Object imported) {
		if (imported == null)
			return false;
		if (imported instanceof Model || (imported instanceof World)) {
			if (importDialog == null) {
				constructImportDialog(parent);
			}
			imported = importDialog.display(imported);
		}
		if (imported == null)
			return false;
		if (imported instanceof Model) {
			Model mod = (Model) imported;
			for (Behaviour b : mod.getAvailableBehaviours()) {
				m.getAvailableBehaviours().add(b);
			}
			for (EntityInterface b : mod.getEntities()) {
				b.setParent(m);
				m.getEntities().add(b);
			}
			for (String s : mod.getAttributes()) {
				m.addAttribute(s, mod.getAttribute(s));
			}
			return true;
		}
		if (imported instanceof EntityInterface) {
			EntityInterface e = (EntityInterface) imported;
			e.setParent(m);
			for (Behaviour b : e.getBehaviours()) {
				m.getAvailableBehaviours().add(b);
			}
			m.getEntities().add(e);
			return true;
		}
		if (imported instanceof Behaviour) {
			Behaviour e = (Behaviour) imported;
			m.getAvailableBehaviours().add(e);
			return true;
		}
		return false;
	}

	private boolean doImportModelWorld(Container parent, XMLEntity m) {
		int returnVal = chooser.showOpenDialog(parent);
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				if (m instanceof Model)
					return doImportModel(parent, (Model) m,
							XMLInterpretor.convert(f.getCanonicalPath(), null));
				else
					return fileHandler.loadFile(chooser.getSelectedFile(),
							(World) m) != null;
			}
		} catch (Exception exn) {
			JOptionPane.showMessageDialog(parent, "Loading failed!");
		}
		return false;
	}

	public void doImport(Container parent, TreePath selectionPath) {
		if (selectionPath == null)
			return;
		Doublet<TreePath, XMLEntity> model = getParentModelWorld(selectionPath);
		if ((model.getSecond() instanceof Model)
				|| (model.getSecond() instanceof World)) {
			if (doImportModelWorld(parent, model.getSecond())) {
				modified(model.getFirst(), model.getSecond());
			}
		}
	}

	// Delegate methods
	public boolean isModified(Object value) {
		return fileHandler.isModified(value);
	}

	public boolean isModified(Object[] value) {
		return fileHandler.isModified(value);
	}

	public boolean isModified(TreePath value) {
		return fileHandler.isModified(value);
	}

	public Set<String> getModified() {
		return fileHandler.getModified();
	}

	public boolean hasModified() {
		return fileHandler.hasModified();
	}

	public void setModified(Object o, boolean modified) {
		fileHandler.setModified(o, modified);
	}

	public void setModified(Object[] p, boolean modified) {
		fileHandler.setModified(p, modified);
	}

	// Listeners part
	private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	public void treeNodesChanged(TreeModelEvent e) {
		for (TreeModelListener l : listeners) {
			l.treeNodesChanged(e);
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
		for (TreeModelListener l : listeners) {
			l.treeNodesInserted(e);
		}
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		for (TreeModelListener l : listeners) {
			l.treeNodesRemoved(e);
		}
	}

	public void treeStructureChanged(TreeModelEvent e) {
		for (TreeModelListener l : listeners) {
			l.treeStructureChanged(e);
		}
	}

	public void treeStructureChanged(Object o) {
		for (TreePath p : searchAll(o)) {
			treeStructureChanged(new TreeModelEvent(this, p));
		}
	}

	public Color getColorization(Behaviour o) {
		GraphBehaviour b = searchGraph(o);
		if (b != null)
			return b.getColorization();
		return null;
	}

	private GraphBehaviour searchGraph(Behaviour o) {
		if (o instanceof GraphBehaviour)
			return (GraphBehaviour) o;
		TreePath p = search(o);
		if (p == null)
			return null;
		if (p.getPathCount() < 2)
			return null;
		Object parent = p.getPathComponent(p.getPathCount() - 2);
		if (parent instanceof BehaviourGraph) {
			return ((BehaviourGraph) parent).getGraphBehaviour(o);
		}
		return null;
	}

	public int[] getPath(Object o) {
		TreePath p = search(o);
		if (p == null)
			return null;
		int[] res = new int[p.getPathCount() - 1];
		for (int i = 0; i < p.getPathCount() - 1; i++) {
			res[i] = getIndexOfChild(p.getPathComponent(i),
					p.getPathComponent(i + 1));
		}
		return res;
	}

	public Object get(int[] path) {
		if (path == null)
			return null;
		Object o = root;
		for (int i : path) {
			if (i > getChildCount(o))
				return null;
			o = getChild(o, i);
		}
		return o;
	}
}
