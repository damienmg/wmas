package wmas.gui.world;

import wmas.gui.EditorInterface;
import wmas.gui.GuiModificationListener;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.xml.XMLEntity;

public interface WorldAttributeEditor extends EditorInterface {
	public void setWorld(XMLEntity w, boolean isEditable);

	public void setTreeModel(WorldObjectTreeModel treeModel);

	public void addGuiModificationListener(GuiModificationListener l);

	public void removeGuiModificationListener(GuiModificationListener l);
}
