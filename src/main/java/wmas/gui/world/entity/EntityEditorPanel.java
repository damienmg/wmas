package wmas.gui.world.entity;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import wmas.behaviour.Behaviour;
import wmas.gui.EditorInterface;
import wmas.gui.GuiModificationListener;
import wmas.gui.shapes.SchemeView;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class EntityEditorPanel extends JPanel implements EditorInterface {
	private static final long serialVersionUID = 1L;
	private static final Icon entityIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/entity.gif"));

	private SchemeView entityView;
	private EntityEditor entityEditor;

	private WorldObjectTreeModel treeModel = null;

	public EntityEditorPanel() {
		super(new GridBagLayout());
		construct(dummyCollection);
	}

	private void construct(Collection<Behaviour> behaviours) {
		entityView = new SchemeView();
		entityView.setEditable(true);

		entityEditor = new EntityEditor(behaviours, false);
		entityEditor.setParent(entityView, null);

		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.CENTER;
		entityView.setSize(100, 100);
		entityView.setPreferredSize(new Dimension(100, 100));
		entityView.setMinimumSize(new Dimension(100, 100));
		entityView.setParentPane(this);

		add(entityView, c);

		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		add(entityEditor, c);
	}

	public void setBehaviours(Collection<Behaviour> behaviours, boolean copy) {
		entityEditor.setAvailableBehaviours(behaviours, copy);
	}

	public void setEntity(EntityInterface e) {
		entityEditor.setEntity(e, false);
	}

	public EntityInterface getEntity() {
		return entityEditor.getEntity();
	}

	public void addGuiModificationListener(GuiModificationListener l) {
		entityEditor.addGuiModificationListener(l);
	}

	public void removeGuiModificationListener(GuiModificationListener l) {
		entityEditor.removeGuiModificationListener(l);
	}

	@Override
	public boolean canEditObject(Object o) {
		return o instanceof EntityInterface;
	}

	@Override
	public Component getEditor() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return entityIcon;
	}

	@Override
	public String getTitle() {
		return treeModel.getTitle(getEntity());
	}

	@Override
	public boolean isModified() {
		if (treeModel == null)
			return false;
		return treeModel.isModified(getEntity());
	}

	private final static Collection<Behaviour> dummyCollection = new LinkedList<Behaviour>();

	@Override
	public void setObject(Object o) {
		if (o instanceof EntityInterface) {
			entityEditor.setEntity((EntityInterface) o, false);
			if (treeModel != null) {
				XMLEntity parent = treeModel.getParent(o);
				if (parent == null) {
					setBehaviours(dummyCollection, false);
				}
				if (parent instanceof Model) {
					setBehaviours(((Model) parent).getAvailableBehaviours(),
							false);
				} else if (parent instanceof World) {
					setBehaviours(((World) parent).getAvailableBehaviours(),
							true);
				} else {
					setBehaviours(dummyCollection, false);
				}
			} else
				setBehaviours(dummyCollection, false);
		}
	}

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	@Override
	public void refresh() {
		repaint();
	}
}
