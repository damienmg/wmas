package wmas.gui.run;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.JTree;

import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.gui.EditorInterface;
import wmas.gui.EditorInterfaceFactory;
import wmas.gui.GuiModificationListener;
import wmas.gui.TabbedEditor;
import wmas.gui.world.WorldAttributeEditor;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class RunnerTabbedPane extends TabbedEditor implements
		GuiModificationListener, EditorInterfaceFactory {
	private static final long serialVersionUID = 1L;

	private JTree tree;

	public RunnerTabbedPane(JTree tree) {
		super(null);
		this.factory = this;
		this.tree = tree;
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	@SuppressWarnings("unchecked")
	public void edit(Object obj, Object container, BehaviourGraph[] path) {
		EditorInterface f = null;
		if (tree != null) {
			WorldObjectTreeModel m = (WorldObjectTreeModel) tree.getModel();
			obj = m.getObject(obj);
			container = m.getObject(container);
		}
		if (obj instanceof EntityInterface) {
			edit(obj);
		} else if (obj instanceof Doublet
				&& ((Doublet<?, ?>) obj).getFirst() instanceof String) {
			Doublet<String, XMLEntity> d = (Doublet<String, XMLEntity>) obj;
			if (d.getSecond() != null) {
				if (d.getSecond() instanceof World) {
					if (((World) d.getSecond()).getAttribute(d.getFirst()) == null)
						return;
				} else if (d.getSecond() instanceof Model) {
					if (((Model) d.getSecond()).getAttribute(d.getFirst()) == null)
						return;
				}
			}
			f = edit(obj);
			if (f != null && f instanceof WorldAttributeEditor
					&& container instanceof XMLEntity) {
				((WorldAttributeEditor) f).setWorld((XMLEntity) container,
						false);
			}
		} else {
			f = edit(obj);
			if (f != null && obj instanceof BehaviourGraph) {
				if (path != null)
					((BehaviourGraphFactory) f).loadSubGraphs(path);
				else
					((BehaviourGraphFactory) f).loadGraph((BehaviourGraph) obj);
			}
		}
	}

	private HashSet<GuiModificationListener> guiListeners = new HashSet<GuiModificationListener>();

	public void addGuiModificationListener(GuiModificationListener l) {
		guiListeners.add(l);
	}

	public void removeGuiModificationListener(GuiModificationListener l) {
		guiListeners.remove(l);
	}

	@Override
	public void internalChanged(Object[] o) {
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(o);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(o);
		}
		revalidate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public EditorInterface create(Object o) {
		if (o == null)
			return null;
		WorldObjectTreeModel m = null;
		if (tree != null)
			m = (WorldObjectTreeModel) tree.getModel();
		if (o instanceof World) {
			WorldViewPanel p = new WorldViewPanel((World) o);
			p.setTreeModel(m);
			return p;
		} else if (o instanceof BehaviourGraph) {
			BehaviourGraphFactory f = new BehaviourGraphFactory(false,
					(BehaviourGraph) o);
			f.setTreeModel(m);
			f.addGuiModificationListener(this);
			XMLEntity parent = m.getParent(o);
			if (parent != null) {
				if (parent instanceof Model) {
					f.setAvailableBehaviours(((Model) parent)
							.getAvailableBehaviours());
				} else if (parent instanceof World) {
					f.setAvailableBehaviours(((World) parent)
							.getAvailableBehaviours());
				} else
					f.setAvailableBehaviours(null);
			} else
				f.setAvailableBehaviours(null);
			return f;
		} else if (o instanceof BehaviourGraph[]) {
			BehaviourGraphFactory f = new BehaviourGraphFactory(false);
			f.loadSubGraphs((BehaviourGraph[]) o);
			f.getSchemeView().setEditable(false);
			f.setTreeModel(m);
			f.addGuiModificationListener(this);
			XMLEntity parent = m.getParent(o);
			if (parent != null) {
				if (parent instanceof Model) {
					f.setAvailableBehaviours(((Model) parent)
							.getAvailableBehaviours());
				} else if (parent instanceof World) {
					f.setAvailableBehaviours(((World) parent)
							.getAvailableBehaviours());
				} else
					f.setAvailableBehaviours(null);
			} else
				f.setAvailableBehaviours(null);
			return f;
		} else if (o instanceof EntityInterface) {
			EntityViewPanel p = new EntityViewPanel();
			p.setObject(o);
			return p;
		} else if (o instanceof Doublet<?, ?>) {
			Doublet<String, XMLEntity> d = (Doublet<String, XMLEntity>) o;
			if (d.getSecond() != null) {
				if (d.getSecond() instanceof World) {
					if (((World) d.getSecond()).getAttribute(d.getFirst()) == null)
						return null;
				} else if (d.getSecond() instanceof Model) {
					if (((Model) d.getSecond()).getAttribute(d.getFirst()) == null)
						return null;
				}
			}
			WorldAttributeEditor i = WorldEditor.getAttributeEditor(d
					.getFirst());
			if (i != null) {
				i.setWorld(d.getSecond(), false);
				i.addGuiModificationListener(this);
				i.setTreeModel(m);
				i.setObject(d);
			}
			return i;
		}
		return null;
	}

	private HashMap<Object, int[]> resetMap = new HashMap<Object, int[]>();

	public void prepareReset(WorldObjectTreeModel treeModel) {
		resetMap.clear();
		for (Object o : tabEditors.keySet()) {
			resetMap.put(o, treeModel.getPath(o));
		}
	}

	public void reset(WorldObjectTreeModel treeModel) {
		LinkedList<Object> l = new LinkedList<Object>();
		l.addAll(tabEditors.keySet());
		for (Object o : l) {
			if (!resetMap.containsKey(o))
				remove(o);
		}
		for (Map.Entry<Object, int[]> e : resetMap.entrySet()) {
			if (tabEditors.containsKey(e.getKey())) {
				Object n = treeModel.get(e.getValue());
				if (n == null) {
					remove(e.getKey());
				} else {
					EditorInterface i = tabEditors.get(e.getKey());
					i.setObject(n);
					tabEditors.remove(e.getKey());
					tabEditors.put(n, i);
					tabObjects.put(i.getEditor(), n);
				}
			}
		}
		resetMap.clear();
	}

	public void refresh() {
		for (EditorInterface i : tabEditors.values()) {
			i.refresh();
		}
	}
}
