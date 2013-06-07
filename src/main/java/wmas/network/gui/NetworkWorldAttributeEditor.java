package wmas.network.gui;

import java.awt.Component;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import wmas.gui.GuiModificationListener;
import wmas.gui.world.WorldAttributeEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.network.Network;
import wmas.network.NetworkFactory;
import wmas.network.NetworksDescription;
import wmas.util.Doublet;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class NetworkWorldAttributeEditor implements WorldAttributeEditor,
		GuiModificationListener {
	private static final Icon networkIcon = new ImageIcon(
			NetworkWorldAttributeEditor.class.getResource("icons/network.gif"));

	private boolean edit = true;
	private Object descr = null;
	private NetworkDescriptionEditor editor = null;
	private World world = null;
	private NetworkFactory factory = null;

	public NetworkWorldAttributeEditor(NetworkFactory factory) {
		super();
		this.factory = factory;
	}

	@Override
	public void setWorld(XMLEntity w, boolean isEditable) {
		if (w instanceof World) {
			world = (World) w;
			edit = isEditable;
		} else {
			world = null;
			edit = true;
		}
	}

	@Override
	public boolean canEditObject(Object o) {
		if (o == null)
			return false;
		if (o instanceof Doublet<?, ?>) {
			if ((((Doublet<?, ?>) o).getFirst() instanceof String)
					&& (((Doublet<?, ?>) o).getSecond() instanceof XMLEntity)) {
				XMLEntity e = (XMLEntity) ((Doublet<?, ?>) o).getSecond();
				String s = (String) ((Doublet<?, ?>) o).getFirst();
				if (e instanceof World) {
					return ((World) e).getAttribute(s) instanceof NetworksDescription;
				} else if (e instanceof Model) {
					return ((Model) e).getAttribute(s) instanceof NetworksDescription;
				}
				return false;
			}
		}
		return (o instanceof NetworksDescription);
	}

	@Override
	public Component getEditor() {
		if (!edit) {
			if (world != null && world.getWorldBehaviour("network") != null) {
				return ((Network) world.getWorldBehaviour("network"))
						.getDisplay();
			}
			return null;
		}
		return editor;
	}

	@Override
	public Icon getIcon() {
		return networkIcon;
	}

	private WorldObjectTreeModel treeModel = null;

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public String getTitle() {
		if (treeModel == null)
			return "Network";
		return treeModel.getTitle(descr);
	}

	@Override
	public boolean isModified() {
		if (treeModel == null)
			return false;
		return treeModel.isModified(descr);
	}

	@Override
	public void refresh() {
		if (edit)
			editor.refresh();
		else {
			Component c = getEditor();
			if (c != null)
				c.repaint();
		}
	}

	private void edit(NetworksDescription descr) {
		if (edit) {
			if (editor == null)
				editor = new NetworkDescriptionEditor();
			editor.setParent(descr, this);
		}
	}

	@Override
	public void setObject(Object o) {
		if (o == null)
			return;
		if (o instanceof Doublet<?, ?>) {
			if ((((Doublet<?, ?>) o).getFirst() instanceof String)
					&& (((Doublet<?, ?>) o).getSecond() instanceof XMLEntity)) {
				XMLEntity e = (XMLEntity) ((Doublet<?, ?>) o).getSecond();
				String s = (String) ((Doublet<?, ?>) o).getFirst();
				if (e instanceof World) {
					if (((World) e).getAttribute(s) instanceof NetworksDescription) {
						descr = o;
						edit((NetworksDescription) ((World) e).getAttribute(s));
					}
				} else if (e instanceof Model) {
					if (((Model) e).getAttribute(s) instanceof NetworksDescription) {
						descr = o;
						edit((NetworksDescription) ((Model) e).getAttribute(s));
					}
				}
			}
		}
		if (o instanceof NetworksDescription) {
			descr = o;
			edit((NetworksDescription) o);
		}
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
		if (o == null)
			return new Object[] { descr };

		r = new Object[o.length + 1];
		int i = 0;
		r[0] = descr;
		for (i = 0; i < o.length; i++) {
			r[i + 1] = o[i];
		}
		return r;
	}

	@Override
	public void internalChanged(Object[] o) {
		if (factory != null)
			factory.refreshAttrEditor();
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.internalChanged(r);
		}
	}

	@Override
	public void representationChanged(Object[] o) {
		if (factory != null)
			factory.refreshAttrEditor();
		Object[] r = expandGuiEvent(o);
		for (GuiModificationListener l : guiListeners) {
			l.representationChanged(r);
		}
	}

}
