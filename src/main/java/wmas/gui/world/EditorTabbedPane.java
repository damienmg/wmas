package wmas.gui.world;

import java.util.HashSet;

import javax.swing.JTabbedPane;
import javax.swing.JTree;

import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.gui.EditorInterface;
import wmas.gui.EditorInterfaceFactory;
import wmas.gui.GuiModificationListener;
import wmas.gui.TabbedEditor;
import wmas.gui.world.entity.EntityEditorPanel;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class EditorTabbedPane extends TabbedEditor implements
		GuiModificationListener, EditorInterfaceFactory {
	private static final long serialVersionUID = 1L;

	private JTree tree;

	public EditorTabbedPane(JTree tree) {
		super(null);
		this.factory = this;
		this.tree = tree;
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public void deleting(Object obj, BehaviourGraph[] path) {
		if (path != null) {
			EditorInterface ef = getInterface(path[0]);
			if (ef != null && ef instanceof BehaviourGraphFactory) {
				BehaviourGraphFactory f = (BehaviourGraphFactory) ef;
				if (path[path.length - 1] == obj) {
					if (path.length != 1)
						f.updateSubGraphs(path);
					else
						remove(f.getRootView());
				} else
					f.setGraph(false);
			}
		} else {
			remove(obj);
		}
		for (EditorInterface c : tabEditors.values())
			c.getEditor().repaint();
	}

	@SuppressWarnings("unchecked")
	public void edit(Object obj, Object container, BehaviourGraph[] path) {
		EditorInterface f = null;
		if (tree != null) {
			WorldObjectTreeModel m = (WorldObjectTreeModel) tree.getModel();
			obj = m.getObject(obj);
			container = m.getObject(container);
		}
		if (obj instanceof EntityInterface && container instanceof World) {
			f = edit(container);
			if (f != null) {
				f.setObject((EntityInterface) obj);
			}
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
				((WorldAttributeEditor) f)
						.setWorld((XMLEntity) container, true);
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
			WorldEditorPanel p = new WorldEditorPanel((World) o);
			p.setTreeModel(m);
			p.setWorldTree(tree);
			p.addGuiModificationListener(this);
			return p;
		} else if (o instanceof BehaviourGraph) {
			BehaviourGraphFactory f = new BehaviourGraphFactory(true,
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
			BehaviourGraphFactory f = new BehaviourGraphFactory(true);
			f.loadSubGraphs((BehaviourGraph[]) o);
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
			EntityEditorPanel p = new EntityEditorPanel();
			p.setEntity((EntityInterface) o);
			XMLEntity parent = m.getParent(o);
			if (parent != null && parent instanceof Model) {
				p.setBehaviours(((Model) parent).getAvailableBehaviours(),
						false);
			}
			p.addGuiModificationListener(this);
			p.setTreeModel(m);
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
				i.addGuiModificationListener(this);
				i.setTreeModel(m);
				i.setObject(d);
			}
			return i;
		}
		return null;
	}
}
