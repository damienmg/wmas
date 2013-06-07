package wmas.gui.run;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import wmas.gui.EditorInterface;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.entity.EntityEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.world.EntityInterface;

public class EntityViewPanel extends JTabbedPane implements EditorInterface {
	private static final long serialVersionUID = 1L;
	private static final Icon entityIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/entity.gif"));

	private Map<String, EntityAttributeView> views = new HashMap<String, EntityAttributeView>();
	private EntityInterface entity = null;
	private WorldObjectTreeModel treeModel = null;

	@Override
	public boolean canEditObject(Object o) {
		if (o instanceof EntityInterface)
			return true;
		return false;
	}

	@Override
	public Component getEditor() {
		return this;
	}

	@Override
	public Icon getIcon() {
		if (entity == null)
			return entityIcon;
		if (entity.getDrawingShape() == null)
			return entityIcon;
		Icon c = EntityEditor.getIcon(entity.getDrawingShape());
		return c != null ? c : entityIcon;
	}

	@Override
	public String getTitle() {
		if (treeModel == null) {
			if (entity == null)
				return "";
			return entity.getName();
		}
		return treeModel.getTitle(entity);
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void refresh() {
		repaint();
	}

	@Override
	public void setObject(Object o) {
		if (o instanceof EntityInterface) {
			this.entity = (EntityInterface) o;
			updateTabs();
		}
	}

	private void getView(String s) {
		if (this.entity == null)
			return;
		if (!this.entity.hasAttribute(s))
			return;
		if (views.containsKey(s)) {
			views.get(s).setEntity(this.entity);
			addTab(views.get(s).getViewName(), views.get(s).getComponent());
		} else {
			EntityAttributeView v = EntityEditor.getAttributeView(s);
			if (v != null) {
				v.setEntity(this.entity);
				addTab(v.getViewName(), v.getComponent());
				views.put(s, v);
			}
		}
	}

	private void updateTabs() {
		removeAll();
		if (this.entity != null) {
			for (String s : this.entity.listAttributes()) {
				getView(s);
			}
		}
	}

}
