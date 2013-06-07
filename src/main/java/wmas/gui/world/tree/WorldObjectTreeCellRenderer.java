package wmas.gui.world.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.entity.EntityEditor;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.world.EntityReference;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class WorldObjectTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	private static final Icon worldIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/world.gif"));
	private static final Icon modelIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/model.gif"));
	private static final Icon entityIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/entity.gif"));
	private static final Icon behaviourIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/behaviour.gif"));
	private static final Icon defaultIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/default.gif"));
	private static final Icon refIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/shortcut.gif"));

	private boolean bold = false;

	public WorldObjectTreeCellRenderer() {
	}

	@SuppressWarnings("unchecked")
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		String v = "";
		Color col = null;
		WorldObjectTreeModel treeModel = ((WorldObjectTreeModel) (tree
				.getModel()));
		if (value == null)
			v = "null";
		else if (value instanceof EntityInterface) {
			v = ((EntityInterface) value).getName();
			col = ((EntityInterface) value).getColorization();
		} else if (value instanceof Model) {
			v = "Model";
		} else if (value instanceof World) {
			v = "World";
		} else if (value instanceof Doublet) {
			Doublet<?, ?> t = (Doublet<?, ?>) value;
			if (t.getFirst() instanceof World) {
				Doublet<World, String> me = (Doublet<World, String>) value;
				Object o = treeModel.getObject(value);
				if (o == null)
					v = "";
				else if (o instanceof Behaviour) {
					v = o.toString();
					if (o instanceof BehaviourGraph)
						col = ((BehaviourGraph) o).getColorization();
					else
						col = treeModel.getColorization((Behaviour) o);
				} else if (o instanceof EntityInterface) {
					v = ((EntityInterface) o).getName();
					col = ((EntityInterface) o).getColorization();
				} else if (o instanceof World)
					v = "World";
				else if (o instanceof Model)
					v = "Model";

				if (v.length() > 0)
					v += " ";
				v += "[" + me.getSecond() + "]";
			} else {
				v = ((Doublet<String, XMLEntity>) value).getFirst();
			}
		} else {
			if (value instanceof BehaviourGraph)
				col = ((BehaviourGraph) value).getColorization();
			else if (value instanceof Behaviour)
				col = treeModel.getColorization((Behaviour) value);
			v = value.toString();
		}
		bold = treeModel.isModified(value);
		super.getTreeCellRendererComponent(tree, v, selected, expanded, leaf,
				row, hasFocus);

		Icon c = getIconObject(value, treeModel);
		if (c != null)
			setIcon(c);
		if (col != null)
			setForeground(col);
		return this;
	}

	@SuppressWarnings("unchecked")
	private Icon getIconObject(Object o, WorldObjectTreeModel treeModel) {
		o = treeModel.getObject(o);
		if (o == null)
			return defaultIcon;
		if (o instanceof Model) {
			return modelIcon;
		}
		if (o instanceof World) {
			return worldIcon;
		}
		if (o instanceof Behaviour) {
			return behaviourIcon;
		}
		if (o instanceof Doublet
				&& ((Doublet<?, ?>) o).getFirst() instanceof String) {
			return (WorldEditor
					.getAttributeIcon(((Doublet<String, XMLEntity>) o)
							.getFirst()));
		}
		if (o instanceof EntityInterface) {
			EntityInterface e = (EntityInterface) o;
			if (e.getDrawingShape() == null)
				return entityIcon;
			Icon i = EntityEditor.getIcon(e.getDrawingShape());
			i = (i == null) ? entityIcon : i;
			if (e instanceof EntityReference) {
				i = getRefIcon(i);
			}
			return i;
		}
		return defaultIcon;
	}

	private static Map<Icon, Icon> iconMap = new HashMap<Icon, Icon>();

	private static Icon getRefIcon(Icon i) {
		if (!iconMap.containsKey(i)) {
			ImageIcon icon = new ImageIcon(new BufferedImage(i.getIconWidth(),
					i.getIconHeight(), BufferedImage.TYPE_INT_ARGB));

			Graphics2D g = (Graphics2D) icon.getImage().getGraphics();
			i.paintIcon(null, g, 0, 0);
			refIcon.paintIcon(null, g, 0, 0);
			// g.drawImage((new ImageIcon(i)).getImage(), 0, 0, null);
			iconMap.put(i, icon);
		}
		return iconMap.get(i);
	}

	@Override
	public Font getFont() {
		Font f = super.getFont();
		if (f != null && bold)
			return f.deriveFont(Font.BOLD);
		return f;
	}
}
