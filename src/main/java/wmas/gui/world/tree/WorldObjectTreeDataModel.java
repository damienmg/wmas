package wmas.gui.world.tree;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.util.Doublet;
import wmas.util.Triplet;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

abstract class WorldObjectTreeDataModel implements TreeModel {

	protected XMLEntity root = null;

	protected WorldObjectFileHandler fileHandler;

	protected boolean dontShowModels = false;

	public WorldObjectTreeDataModel(WorldObjectFileHandler f) {
		fileHandler = f;
	}

	protected boolean isParent(Object o) {
		if (o == null)
			return false;
		return (o instanceof Doublet)
				&& (((Doublet<?, ?>) o).getFirst() instanceof World);
	}

	public TreePath searchParent(TreePath p) {
		if (p == null)
			return null;
		while (!isParent(p.getLastPathComponent()) && p.getPathCount() > 1)
			p = p.getParentPath();
		return p;
	}

	@SuppressWarnings("unchecked")
	public Object getObject(Object o) {
		if (o == null)
			return null;
		if (o instanceof Doublet
				&& ((Doublet<?, ?>) o).getFirst() instanceof World)
			return fileHandler.getModelObject((Doublet<World, String>) o);
		return o;
	}

	@SuppressWarnings("unchecked")
	protected boolean compareObj(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		Object o12 = o1;
		Object o22 = o2;
		if (o2 instanceof Doublet) {
			if (((Doublet<?, ?>) o2).getFirst() instanceof World) {
				o22 = fileHandler.getModelObject((Doublet<World, String>) o2);
			} else {
				if (o1 instanceof Doublet) {
					return o1.equals(o2);
				}
			}
		}
		if (o1 instanceof Doublet
				&& ((Doublet<?, ?>) o1).getFirst() instanceof World) {
			o12 = fileHandler.getModelObject((Doublet<World, String>) o1);
		}
		return o1 == o22 || o2 == o12 || (o12 == o22);
	}

	protected boolean compareObj(Object o1, Object[] o2) {
		for (int i = 0; i < o2.length; i++)
			if (compareObj(o1, o2[i]))
				return true;
		return false;
	}

	protected TreePath searchOnePass(Queue<TreePath> queue, TreePath p,
			Object[] o) {
		int si = getChildCount(p.getLastPathComponent());
		for (int i = 0; i < si; i++) {
			Object obj = getChild(p.getLastPathComponent(), i);
			TreePath newPath = p.pathByAddingChild(obj);
			if (compareObj(obj, o)) {
				return newPath;
			}
			queue.offer(newPath);
		}
		return null;
	}

	protected void searchAllOnePass(Queue<TreePath> queue, TreePath p,
			Object o, Collection<TreePath> res) {
		int si = getChildCount(p.getLastPathComponent());
		for (int i = 0; i < si; i++) {
			Object obj = getChild(p.getLastPathComponent(), i);
			TreePath newPath = p.pathByAddingChild(obj);
			if (compareObj(obj, o)) {
				res.add(newPath);
			} else {
				queue.offer(newPath);
			}
		}
	}

	protected TreePath searchFirst(TreePath orig, Object[] o) {
		Queue<TreePath> q = new LinkedList<TreePath>();
		q.offer(orig);
		while (!q.isEmpty()) {
			TreePath r = searchOnePass(q, q.poll(), o);
			if (r != null)
				return r;
		}
		return null;
	}

	protected Collection<TreePath> searchAll(Object o) {
		Collection<TreePath> res = new LinkedList<TreePath>();
		Queue<TreePath> q = new LinkedList<TreePath>();
		q.offer(new TreePath(root));
		while (!q.isEmpty()) {
			searchAllOnePass(q, q.poll(), o, res);
		}
		return res;
	}

	public TreePath search(Object o) {
		TreePath r = new TreePath(root);
		if (compareObj(root, o))
			return r;
		return searchFirst(r, new Object[] { o });
	}

	public TreePath searchFirst(Object[] o) {
		TreePath r = new TreePath(root);
		if (compareObj(root, o))
			return r;
		return searchFirst(r, o);
	}

	@SuppressWarnings("unchecked")
	public Object getChild(Object parent, int index) {
		if (parent == null)
			return null;
		if (index < 0)
			return null;
		if (parent instanceof EntityInterface) {
			EntityInterface e = (EntityInterface) parent;
			if (index < e.getBehaviours().size())
				return e.getBehaviours().get(index);
		} else if (parent instanceof Model) {
			Model e = (Model) parent;
			if (index < e.getEntities().size())
				return e.getEntities().get(index);
			index -= e.getEntities().size();
			if (index < e.getAvailableBehaviours().size())
				return e.getAvailableBehaviours().get(index);
			index -= e.getAvailableBehaviours().size();
			if (index < e.getAttributes().size()) {
				int i = 0;
				for (String s : e.getAttributes()) {
					if (i == index) {
						return new Doublet<String, XMLEntity>(s,
								(XMLEntity) parent);
					} else
						i++;
				}
			}
		} else if (parent instanceof World) {
			World e = (World) parent;

			if (index < e.getModels().size() && !dontShowModels) {
				int i = 0;
				for (String s : e.getModels().keySet()) {
					if (i == index) {
						return new Doublet<World, String>(e, s);
					} else
						i++;
				}
			}
			index -= dontShowModels ? 0 : e.getModels().size();
			if (index < e.getAttributes().size()) {
				int i = 0;
				for (String s : e.getAttributes()) {
					if (i == index) {
						return new Doublet<String, XMLEntity>(s,
								(XMLEntity) parent);
					} else
						i++;
				}
			}
			index -= e.getAttributes().size();
			if (index < e.listEntities().size())
				return e.listEntities().get(index);
		} else if (parent instanceof BehaviourGraph) {
			BehaviourGraph g = (BehaviourGraph) parent;
			if (index >= 0 && index < g.size()) {
				return g.get(index);
			}
			return null;
		} else if (parent instanceof Doublet
				&& ((Doublet<?, ?>) parent).getFirst() instanceof World) {
			return dontShowModels ? null
					: getChild(
							fileHandler
									.getModelObject((Doublet<World, String>) parent),
							index);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public int getChildCount(Object node) {
		if (node == null)
			return 0;
		if (node instanceof EntityInterface) {
			return ((EntityInterface) node).getBehaviours().size();
		}
		if (node instanceof Model) {
			return ((Model) node).getEntities().size()
					+ ((Model) node).getAttributes().size()
					+ ((Model) node).getAvailableBehaviours().size();
		}
		if (node instanceof World) {
			return (dontShowModels ? 0 : ((World) node).getModels().size())
					+ ((World) node).getAttributes().size()
					+ ((World) node).listEntities().size();
		}
		if (node instanceof Doublet
				&& ((Doublet<?, ?>) node).getFirst() instanceof World) {
			return dontShowModels ? 0 : getChildCount(fileHandler
					.getModelObject((Doublet<World, String>) node));
		}
		if (node instanceof BehaviourGraph) {
			BehaviourGraph g = (BehaviourGraph) node;
			return g.size();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public int getIndexOfChild(Object parent, Object child) {
		if (child == null)
			return -1;
		if (parent == null)
			return -1;
		if (parent instanceof EntityInterface) {
			EntityInterface e = (EntityInterface) parent;
			return e.getBehaviours().indexOf(child);
		} else if (parent instanceof Model) {
			Model e = (Model) parent;
			if (child instanceof EntityInterface) {
				return e.getEntities().indexOf(child);
			}

			int index = e.getAvailableBehaviours().indexOf(child);
			if (index >= 0)
				return index + e.getEntities().size();
			int i = e.getEntities().size() + e.getAvailableBehaviours().size();
			if (child instanceof Doublet) {
				Doublet<String, XMLEntity> d = (Doublet<String, XMLEntity>) child;
				if (parent != d.getSecond())
					return -1;
				child = d.getFirst();
			}
			for (String s : e.getAttributes()) {
				if (child.equals(s)) {
					return i;
				} else
					i++;
			}
			return -1;
		} else if (parent instanceof World) {
			World e = (World) parent;
			if (child instanceof EntityInterface) {
				int ind = e.listEntities().indexOf(child);
				int mind = dontShowModels ? 0 : e.getModels().size();
				mind += e.getAttributes().size();
				if (ind >= 0)
					return mind + ind;
				return -1;
			}
			if (child instanceof Doublet
					&& ((Doublet<?, ?>) child).getFirst() instanceof World) {
				Doublet<World, String> d = (Doublet<World, String>) child;
				if (parent != d.getFirst())
					return -1;
				child = d.getSecond();
			} else if (child instanceof Doublet) {
				Doublet<String, XMLEntity> d = (Doublet<String, XMLEntity>) child;
				if (parent != d.getSecond())
					return -1;
				child = d.getFirst();
			}
			int i = 0;
			if (!dontShowModels) {
				for (Map.Entry<String, Object> s : e.getModels().entrySet()) {
					if (child == s.getValue()) {
						return i;
					} else
						i++;
				}
			}
			for (String s : e.getAttributes()) {
				if (child.equals(s)) {
					return i;
				} else
					i++;
			}
			return -1;
		} else if (parent instanceof BehaviourGraph) {
			BehaviourGraph g = (BehaviourGraph) parent;
			return g.indexOf(child);
		} else if (parent instanceof Doublet
				&& ((Doublet<?, ?>) parent).getFirst() instanceof World) {
			return dontShowModels ? -1
					: getIndexOfChild(
							fileHandler
									.getModelObject((Doublet<World, String>) parent),
							child);
		}
		return -1;
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	public static Triplet<Object, Object, String> getPathComponents(TreePath p) {
		if (p == null)
			return null;
		Object[] path = p.getPath();
		if (path.length == 0)
			return null;
		Object comp = p.getLastPathComponent();
		Object container = null;
		String fName = "";
		String bName = "";
		String eName = "";
		for (int i = path.length - 1; i >= 0 && container == null; i--) {
			Object c = path[i];
			if (c instanceof Doublet
					&& ((Doublet<?, ?>) c).getFirst() instanceof World) {
				fName = ((Doublet<?, ?>) c).getSecond().toString();
				World w = (World) (((Doublet<?, ?>) c).getFirst());
				container = w.getModels().get(fName);
				fName = "[" + fName + "] ";
			} else if (c instanceof Doublet) {
				container = ((Doublet<?, ?>) c).getSecond();
				eName = ((Doublet<?, ?>) c).getFirst().toString();
			} else if (c instanceof Model) {
				container = c;
				if (eName.equals(""))
					eName = "Model";
			} else if (c instanceof World) {
				container = c;
				if (eName.equals(""))
					eName = "World";
			} else if (c instanceof Behaviour) {
				bName = c.toString() + "." + bName;
			} else if (c instanceof EntityInterface) {
				eName = ((EntityInterface) c).getName() + "." + eName;
			}
		}
		if (container instanceof Model && comp instanceof Behaviour) {
			eName = "";
		}
		String pName = eName + bName;
		if (container != null) {
			if (container instanceof EntityInterface)
				pName = ((EntityInterface) container).getName() + "." + pName;
			else if (container instanceof Behaviour)
				pName = container.toString() + "." + pName;
		}
		String name = fName + " " + pName;
		if (name.endsWith("."))
			name = name.substring(0, name.length() - 1);
		return new Triplet<Object, Object, String>(comp, container, name);
	}

	public String getTitle(TreePath path) {
		Triplet<Object, Object, String> t = getPathComponents(path);
		return t == null ? "" : t.getThird();
	}

	public XMLEntity getParent(Object o) {
		Doublet<TreePath, XMLEntity> d = getParentModelWorld(search(o));
		if (d != null)
			return d.getSecond();
		return null;
	}

	protected Doublet<TreePath, XMLEntity> getParentModelWorld(TreePath treePath) {
		while (treePath != null && treePath.getPathCount() > 0) {
			Object o = getObject(treePath.getLastPathComponent());
			if (o instanceof Model)
				return new Doublet<TreePath, XMLEntity>(treePath, (Model) o);
			if (o instanceof World)
				return new Doublet<TreePath, XMLEntity>(treePath, (World) o);
			treePath = treePath.getParentPath();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected Doublet<TreePath, XMLEntity> getParentObject(TreePath treePath) {
		if (treePath == null)
			return null;
		if (treePath.getPathCount() == 0)
			return null;
		treePath = treePath.getParentPath();
		while (treePath != null && treePath.getPathCount() > 0) {
			Object o = getObject(treePath.getLastPathComponent());
			if (o instanceof Model)
				return new Doublet<TreePath, XMLEntity>(treePath, (Model) o);
			if (o instanceof World)
				return new Doublet<TreePath, XMLEntity>(treePath, (World) o);
			if (o instanceof BehaviourGraph)
				return new Doublet<TreePath, XMLEntity>(treePath,
						(BehaviourGraph) o);
			if (o instanceof EntityInterface)
				return new Doublet<TreePath, XMLEntity>(treePath,
						(EntityInterface) o);
			if (o instanceof Doublet)
				return new Doublet<TreePath, XMLEntity>(treePath,
						((Doublet<String, XMLEntity>) o).getSecond());
			treePath = treePath.getParentPath();
		}
		return null;
	}

	protected Doublet<TreePath, Model> getParentModel(TreePath treePath) {
		while (treePath != null && treePath.getPathCount() > 0) {
			Object o = getObject(treePath.getLastPathComponent());
			if (o instanceof Model)
				return new Doublet<TreePath, Model>(treePath, (Model) o);
			treePath = treePath.getParentPath();
		}
		return null;
	}

	public String getTitle(Object o) {
		if (o == null)
			return "";
		Triplet<Object, Object, String> t = getPathComponents(search(o));
		if (t == null) {
			if (o instanceof EntityInterface)
				return ((EntityInterface) o).getName();
			else if (o instanceof World)
				return "World";
			else if (o instanceof Model)
				return "Model";
			else if (o instanceof Doublet)
				return getTitle(((Doublet<?, ?>) o).getSecond()) + "."
						+ ((Doublet<?, ?>) o).getFirst().toString();
			else
				return o.toString();
		}
		return t.getThird();
	}

	public void setDontShowModels(boolean dontShowModels) {
		this.dontShowModels = dontShowModels;
	}
}
