package wmas.gui.world.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.behaviour.Behaviour;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.expression.Expression;
import wmas.expression.ExpressionParseException;
import wmas.gui.GuiModificationListener;
import wmas.gui.run.EntityAttributeView;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.ShapeListener;
import wmas.gui.world.WorldEditorPanel;
import wmas.util.Doublet;
import wmas.world.EntityInterface;
import wmas.xml.XMLEntity;

public class EntityEditor extends JSplitPane implements ShapeListener,
		ActionListener, ListSelectionListener, GuiModificationListener {
	private static final long serialVersionUID = 1L;

	protected JComboBox shapeChooser;
	protected EntityInterface entity;

	protected JTextField activeField;
	protected JLabel activeLabel;

	protected JTextField nameField;
	protected JLabel nameLabelLabel;
	protected JTextField xField;
	protected JLabel xFieldLabel;
	protected JTextField yField;
	protected JLabel yFieldLabel;
	protected JTextField wField;
	protected JLabel wFieldLabel;
	protected JTextField hField;
	protected JLabel hFieldLabel;

	protected JCheckBox dynPositionCheckbox;
	protected JTextField dynXField;
	protected JLabel dynXFieldLabel;
	protected JTextField dynYField;
	protected JLabel dynYFieldLabel;
	protected JCheckBox dynSizeCheckbox;
	protected JTextField dynWField;
	protected JLabel dynWFieldLabel;
	protected JTextField dynHField;
	protected JLabel dynHFieldLabel;

	protected JList attributeList;
	protected JLabel attributeLabel;
	protected JButton addAttrButton;
	protected JButton delAttrButton;
	protected JList behaviourList;
	protected JLabel behaviourLabel;
	protected JButton addBehaviourButton;
	protected JButton delBehaviourButton;

	protected JButton colorLabel;
	protected ImageIcon colorIcon;
	protected JLabel colorLabelLabel;
	protected JButton fillColorLabel;
	protected ImageIcon fillColorIcon;
	protected JLabel fillColorLabelLabel;

	protected boolean moveable = false;

	protected SchemeView parent = null;
	private WorldEditorPanel worldEditor = null;
	protected BehaviourGraphFactory graphFactory = null;

	protected Collection<Behaviour> availableBehaviours = null;
	protected boolean copyBehaviours = false;

	protected static Set<EntityShapeFactory> shapeFactories = new HashSet<EntityShapeFactory>();
	protected static Set<EntityAttributeFactory> attrFactories = new HashSet<EntityAttributeFactory>();
	protected static Map<String, Doublet<EntityAttributeFactory, Integer>> attrFactoriesMap = new HashMap<String, Doublet<EntityAttributeFactory, Integer>>();

	public static void registerShapeFactory(EntityShapeFactory f) {
		shapeFactories.add(f);
	}

	public static void registerAttributeFactory(EntityAttributeFactory f) {
		attrFactories.add(f);
		for (int i = 0; i < f.getNbAttribute(); i++) {
			attrFactoriesMap.put(f.getAttributeName(i),
					new Doublet<EntityAttributeFactory, Integer>(f, i));
		}
	}

	public static Set<String> listAttributes() {
		return attrFactoriesMap.keySet();
	}

	public static String getDescription(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return null;
		Doublet<EntityAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().getAttributeDescription(d.getSecond());
	}

	public static EntityAttributeView getAttributeView(String name) {
		if (!attrFactoriesMap.containsKey(name))
			return null;
		Doublet<EntityAttributeFactory, Integer> d = attrFactoriesMap.get(name);
		return d.getFirst().getAttributeViewer(d.getSecond());
	}

	public static Icon getIcon(DrawableShape s) {
		for (EntityShapeFactory f : shapeFactories) {
			int i = f.getShapeIndex(s);
			if (i >= 0)
				return f.getShapeIcon(i);
		}
		return null;
	}

	class AttributeFactoryListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		private List<String> data = new ArrayList<String>();
		private Set<String> didMap = new HashSet<String>();
		private Set<String> map = new HashSet<String>();

		AttributeFactoryListModel() {
			transfer();
		}

		void transfer() {
			int s = data.size();
			data.clear();
			if (s > 0)
				fireIntervalRemoved(this, 0, s);
			if (entity != null) {
				data.addAll(entity.listAttributes());
				if (data.size() > 0)
					fireIntervalAdded(this, 0, data.size());
			}
		}

		public Object getElementAt(int index) {
			return data.get(index);
		}

		public Component getComponent() {
			getMap();
			int i = attributeList.getSelectedIndex();
			if (data.size() <= i || i < 0)
				return null;
			XMLEntity o = entity.getAttribute(data.get(i));
			if (!didMap.contains(data.get(i)))
				return null;
			Doublet<EntityAttributeFactory, Integer> e = attrFactoriesMap
					.get(data.get(i));
			if (o == null || e == null)
				return null;
			return e.getFirst().getAttributeEditor(entity.getParent(),
					e.getSecond(), o, EntityEditor.this);
		}

		public int getSize() {
			return data.size();
		}

		private void getMap() {
			map.clear();
			didMap.clear();
			if (entity != null) {
				for (EntityAttributeFactory f : attrFactories) {
					for (int i = 0; i < f.getNbAttribute(); i++) {
						if (!entity.hasAttribute(f.getAttributeName(i)))
							map.add(f.getAttributeName(i));
						else
							didMap.add(f.getAttributeName(i));
					}
				}
			}
		}

		boolean canAdd() {
			getMap();
			return map.size() > 0;
		}

		void add() {
			if (!canAdd())
				return;
			Object[] possibilities = map.toArray();
			String s = (String) JOptionPane.showInputDialog(EntityEditor.this,
					"Please select the attribute to add:",
					"Attribute selection", JOptionPane.PLAIN_MESSAGE, null,
					possibilities, possibilities[0]);
			if ((s != null) && (s.length() > 0) && map.contains(s)) {
				Doublet<EntityAttributeFactory, Integer> el = attrFactoriesMap
						.get(s);
				XMLEntity e = el.getFirst().createAttribute(el.getSecond());
				String name = el.getFirst().getAttributeName(el.getSecond());
				entity.addAttribute(name, e);
				data.add(name);
				internalChanged(null);
				fireIntervalAdded(this, data.size(), data.size() + 1);
			}
		}

		void delete() {
			int i = attributeList.getSelectedIndex();
			if (data.size() <= i || i < 0)
				return;
			entity.removeAttribute(data.get(i));
			data.remove(i);
			internalChanged(null);
			fireIntervalRemoved(this, i, i + 1);
		}
	}

	class BehaviourListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		private List<Behaviour> data = new ArrayList<Behaviour>();

		BehaviourListModel() {
			transfer();
		}

		void transfer() {
			int s = data.size();
			data.clear();
			if (s > 0)
				fireIntervalRemoved(this, 0, s);
			if (entity != null) {
				data.addAll(entity.getBehaviours());
				if (data.size() > 0)
					fireIntervalAdded(this, 0, data.size());
			}
		}

		public Object getElementAt(int index) {
			return data.get(index).toString();
		}

		public int getSize() {
			return data.size();
		}

		boolean canAdd() {
			return availableBehaviours.size() > 0;
		}

		void add() {
			if (!canAdd())
				return;
			Object[] possibilities = availableBehaviours.toArray();
			Object s = JOptionPane.showInputDialog(EntityEditor.this,
					"Please select the behaviour to add:",
					"Behaviour selection", JOptionPane.PLAIN_MESSAGE, null,
					possibilities, possibilities[0]);
			if ((s != null) && (s instanceof Behaviour)) {
				Behaviour b = (Behaviour) s;
				if (copyBehaviours)
					b = b.copy();
				entity.addBehaviour(b);
				data.add(b);
				internalChanged(null);
				fireIntervalAdded(this, data.size(), data.size() + 1);
			}
		}

		void delete() {
			int i = behaviourList.getSelectedIndex();
			if (data.size() <= i || i < 0)
				return;
			entity.removeBehaviour(data.get(i));
			data.remove(i);
			internalChanged(null);
			fireIntervalRemoved(this, i, i + 1);
		}

	}

	class ShapeFactoryComboBoxModel implements ComboBoxModel, ListCellRenderer {

		Object selected = "None";
		ArrayList<Object> list = new ArrayList<Object>();
		Map<Object, Doublet<Integer, EntityShapeFactory>> map = new HashMap<Object, Doublet<Integer, EntityShapeFactory>>();
		JLabel lab;

		ShapeFactoryComboBoxModel() {
			lab = new JLabel();
			lab.setOpaque(true);
			lab.setVerticalAlignment(JLabel.CENTER);
			map.put("None", null);
			list.add("None");
			if (shapeFactories != null) {
				for (EntityShapeFactory f : shapeFactories) {
					for (int i = 0; i < f.getNbShape(); i++) {
						Object o = new Doublet<Icon, String>(f.getShapeIcon(i),
								f.getShapeName(i));
						list.add(o);
						map.put(o, new Doublet<Integer, EntityShapeFactory>(i,
								f));
					}
				}
			}
		}

		public Object getSelectedItem() {
			return selected;
		}

		public void setShape() {
			Doublet<Integer, EntityShapeFactory> s = map.get(selected);
			if (s != null) {
				DrawableShape ns = s.getSecond().getShape(s.getFirst());
				if (entity.getDrawingShape() != null) {
					if (ns.getClass().equals(
							entity.getDrawingShape().getClass()))
						return;
					if (moveable) {
						double[] pos = entity.getDrawingShape().getPosition();
						double[] bounds = entity.getDrawingShape().getBounds();
						ns.setSize(bounds[2], bounds[3]);
						ns.setPosition(pos[0], pos[1]);
					}
					ns.setColor(entity.getDrawingShape().getColor());
				}
				entity.setDrawingShape(ns);
				transferShape();
				representationChanged(null);
			} else {
				if (entity.getDrawingShape() != null) {
					entity.setDrawingShape(null);
					transferShape();
					representationChanged(null);
				}
			}
		}

		public void setSelectedItem(Object anItem) {
			if (map.containsKey(anItem))
				selected = anItem;
			else
				selected = "None";
		}

		public void setSelectedItem() {
			if (curShape == null) {
				selected = "None";
			} else {
				DrawableShape s = curShape;
				for (Map.Entry<Object, Doublet<Integer, EntityShapeFactory>> se : map
						.entrySet()) {
					if (se.getValue() != null) {
						if (se.getValue().getSecond().getShapeIndex(s) == se
								.getValue().getFirst()) {
							selected = se.getKey();
							return;
						}
					}
				}
				selected = "None";
			}
			shapeChooser.setSelectedItem(selected);
		}

		public void addListDataListener(ListDataListener l) {
		}

		public void removeListDataListener(ListDataListener l) {
		}

		public Object getElementAt(int index) {
			return list.get(index);
		}

		public int getSize() {
			return list.size();
		}

		@SuppressWarnings("unchecked")
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				lab.setBackground(list.getSelectionBackground());
				lab.setForeground(list.getSelectionForeground());
			} else {
				lab.setBackground(list.getBackground());
				lab.setForeground(list.getForeground());
			}

			// Set the icon and text. If icon was null, say so.
			Icon icon = null;
			String pet = "";
			if (value instanceof Doublet) {
				icon = ((Doublet<Icon, String>) value).getFirst();
				pet = ((Doublet<Icon, String>) value).getSecond();
			} else
				pet = value.toString();
			lab.setIcon(icon);
			lab.setText(pet);
			setFont(list.getFont());

			return lab;
		}
	}

	public EntityEditor(Collection<Behaviour> aBehaviours) {
		super(VERTICAL_SPLIT);
		this.copyBehaviours = false;
		this.availableBehaviours = aBehaviours;
		construct();
	}

	public EntityEditor(Collection<Behaviour> aBehaviours,
			boolean copyBehaviours) {
		super(VERTICAL_SPLIT);
		this.copyBehaviours = copyBehaviours;
		this.availableBehaviours = aBehaviours;
		construct();
	}

	private void construct() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		activeField = new JTextField("");
		activeField.setActionCommand("active");
		activeField.addActionListener(this);
		activeLabel = new JLabel("Active?");

		nameField = new JTextField("");
		nameField.setActionCommand("name");
		nameField.addActionListener(this);
		nameLabelLabel = new JLabel("Entity:");

		Image i = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		colorIcon = new ImageIcon(i);
		colorLabel = new JButton(colorIcon);
		colorLabel.setActionCommand("color");
		colorLabel.addActionListener(this);
		colorLabelLabel = new JLabel("Color:");

		Image i2 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		fillColorIcon = new ImageIcon(i2);
		fillColorLabel = new JButton(fillColorIcon);
		fillColorLabel.setActionCommand("fill_color");
		fillColorLabel.addActionListener(this);
		fillColorLabelLabel = new JLabel("Filled with:");

		xField = new JTextField("", 10);
		xField.setActionCommand("x");
		xField.addActionListener(this);
		xFieldLabel = new JLabel("X position:");

		yField = new JTextField("", 10);
		yField.setActionCommand("y");
		yField.addActionListener(this);
		yFieldLabel = new JLabel("Y position:");

		wField = new JTextField("", 10);
		wField.setActionCommand("w");
		wField.addActionListener(this);
		wFieldLabel = new JLabel("Width:");

		hField = new JTextField("", 10);
		hField.setActionCommand("h");
		hField.addActionListener(this);
		hFieldLabel = new JLabel("Height:");

		dynPositionCheckbox = new JCheckBox("Dynamic position?");
		dynPositionCheckbox.addActionListener(this);
		dynPositionCheckbox.setActionCommand("dyn_pos");

		dynSizeCheckbox = new JCheckBox("Dynamic size?");
		dynSizeCheckbox.addActionListener(this);
		dynSizeCheckbox.setActionCommand("dyn_size");

		dynXField = new JTextField("", 10);
		dynXField.setActionCommand("dyn_x");
		dynXField.addActionListener(this);
		dynXFieldLabel = new JLabel("X position:");

		dynYField = new JTextField("", 10);
		dynYField.setActionCommand("dyn_y");
		dynYField.addActionListener(this);
		dynYFieldLabel = new JLabel("Y position:");

		dynWField = new JTextField("", 10);
		dynWField.setActionCommand("dyn_w");
		dynWField.addActionListener(this);
		dynWFieldLabel = new JLabel("Width:");

		dynHField = new JTextField("", 10);
		dynHField.setActionCommand("dyn_h");
		dynHField.addActionListener(this);
		dynHFieldLabel = new JLabel("Height:");

		ShapeFactoryComboBoxModel s = new ShapeFactoryComboBoxModel();
		shapeChooser = new JComboBox(s);
		shapeChooser.setActionCommand("shape");
		shapeChooser.addActionListener(this);
		shapeChooser.setRenderer(s);

		attributeLabel = new JLabel("Attributes:");
		attributeList = new JList(new AttributeFactoryListModel());
		attributeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		attributeList.addListSelectionListener(this);
		attributeList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		addAttrButton = new JButton("+");
		addAttrButton.setToolTipText("Add attribute");
		addAttrButton.setActionCommand("add_attr");
		addAttrButton.addActionListener(this);
		delAttrButton = new JButton("-");
		delAttrButton.setToolTipText("Remove attribute");
		delAttrButton.setActionCommand("del_attr");
		delAttrButton.addActionListener(this);

		behaviourLabel = new JLabel("Behaviours:");
		behaviourList = new JList(new BehaviourListModel());
		behaviourList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		behaviourList.addListSelectionListener(this);
		behaviourList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		addBehaviourButton = new JButton("+");
		addBehaviourButton.setToolTipText("Add behaviour");
		addBehaviourButton.setActionCommand("add_behaviour");
		addBehaviourButton.addActionListener(this);
		delBehaviourButton = new JButton("-");
		delBehaviourButton.setToolTipText("Remove behaviour");
		delBehaviourButton.setActionCommand("del_behaviour");
		delBehaviourButton.addActionListener(this);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0;

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(activeLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(activeField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(nameLabelLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(nameField, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(shapeChooser, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(colorLabelLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(colorLabel, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(fillColorLabelLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(fillColorLabel, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(xFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(xField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(yFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(yField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(wFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(wField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(hFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(hField, c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynPositionCheckbox, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(dynXFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynXField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(dynYFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynYField, c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynSizeCheckbox, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(dynWFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynWField, c);

		c.weightx = 0.0;
		c.gridwidth = 1;
		p.add(dynHFieldLabel, c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(dynHField, c);

		c.weightx = 0;
		c.gridwidth = 1;
		p.add(attributeLabel, c);
		c.weightx = 1;
		p.add(new JLabel(), c);
		c.weightx = 0;
		p.add(addAttrButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(delAttrButton, c);
		c.weightx = 1;
		c.weighty = 1;
		p.add(attributeList, c);

		c.weighty = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		p.add(behaviourLabel, c);
		c.weightx = 1;
		p.add(new JLabel(), c);
		c.weightx = 0;
		p.add(addBehaviourButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		p.add(delBehaviourButton, c);
		c.weightx = 1;
		c.weighty = 1;
		p.add(behaviourList, c);

		this.xField.setVisible(false);
		this.yField.setVisible(false);
		this.wField.setVisible(false);
		this.hField.setVisible(false);
		this.xFieldLabel.setVisible(false);
		this.yFieldLabel.setVisible(false);
		this.wFieldLabel.setVisible(false);
		this.hFieldLabel.setVisible(false);

		this.dynPositionCheckbox.setVisible(false);
		this.dynXField.setVisible(false);
		this.dynYField.setVisible(false);
		this.dynXFieldLabel.setVisible(false);
		this.dynYFieldLabel.setVisible(false);

		this.dynSizeCheckbox.setVisible(false);
		this.dynWField.setVisible(false);
		this.dynHField.setVisible(false);
		this.dynWFieldLabel.setVisible(false);
		this.dynHFieldLabel.setVisible(false);

		this.colorLabel.setVisible(false);
		this.colorLabelLabel.setVisible(false);
		this.fillColorLabel.setVisible(false);
		this.fillColorLabelLabel.setVisible(false);

		setLeftComponent(p);
	}

	private void setColorButton(Color c) {
		Image i = colorIcon.getImage();
		Graphics g = i.getGraphics();
		if (c == null) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(
					AlphaComposite.CLEAR, 0.0f));
		} else
			g.setColor(c);
		g.fillRect(0, 0, 20, 20);
		g.dispose();
		i.flush();
		repaint();
	}

	private void setFillColorButton(Color c) {
		Image i = fillColorIcon.getImage();
		Graphics g = i.getGraphics();
		if (c == null) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(
					AlphaComposite.CLEAR, 0.0f));
		} else
			g.setColor(c);
		g.fillRect(0, 0, 20, 20);
		g.dispose();
		i.flush();
		repaint();
	}

	private DrawableShape curShape = null;

	private void transferShape() {
		if (this.entity != null && this.entity.getDrawingShape() != null) {
			if (curShape == this.entity.getDrawingShape())
				return;
			if (curShape != null) {
				curShape.removeListener(this);
				if (parent != null)
					parent.replace(curShape, this.entity.getDrawingShape());
			} else {
				if (!parent.contains(this.entity.getDrawingShape()))
					parent.add(this.entity.getDrawingShape());
			}
			if (worldEditor != null)
				worldEditor.changeShape(this.entity, curShape);
			curShape = this.entity.getDrawingShape();
		} else if (curShape != null) {
			curShape.removeListener(this);
			if (parent != null)
				parent.remove(curShape);
			if (worldEditor != null)
				worldEditor.changeShape(this.entity, curShape);
			curShape = null;
		} else
			return;
		if (curShape != null) {
			curShape.addListener(this);
			this.xField.setVisible(moveable);
			this.yField.setVisible(moveable);
			this.wField.setVisible(true);
			this.hField.setVisible(true);
			this.xFieldLabel.setVisible(moveable);
			this.yFieldLabel.setVisible(moveable);
			this.wFieldLabel.setVisible(true);
			this.hFieldLabel.setVisible(true);
			this.colorLabel.setVisible(true);
			this.colorLabelLabel.setVisible(true);
			this.fillColorLabel.setVisible(curShape.hasFillColor());
			this.fillColorLabelLabel.setVisible(curShape.hasFillColor());

			this.dynPositionCheckbox.setVisible(true);
			Expression expr1 = this.entity == null ? null : this.entity
					.getDynamicX();
			Expression expr2 = this.entity == null ? null : this.entity
					.getDynamicY();
			boolean visible = expr1 != null && expr2 != null;
			this.dynPositionCheckbox.setSelected(visible);
			this.dynXField.setVisible(visible);
			this.dynYField.setVisible(visible);
			this.dynXFieldLabel.setVisible(visible);
			this.dynYFieldLabel.setVisible(visible);
			if (visible) {
				this.dynXField.setText(expr1.toString());
				this.dynYField.setText(expr2.toString());
			}

			this.dynSizeCheckbox.setVisible(true);
			expr1 = this.entity == null ? null : this.entity.getDynamicWidth();
			expr2 = this.entity == null ? null : this.entity.getDynamicHeight();
			visible = expr1 != null && expr2 != null;
			this.dynSizeCheckbox.setSelected(visible);
			this.dynWField.setVisible(visible);
			this.dynHField.setVisible(visible);
			this.dynWFieldLabel.setVisible(visible);
			this.dynHFieldLabel.setVisible(visible);
			if (visible) {
				this.dynWField.setText(expr1.toString());
				this.dynHField.setText(expr2.toString());
			}

			double[] pos = curShape.getPosition();
			if (!moveable) {
				double[] b = curShape.getBounds();
				pos[0] = b[2] / 2 + SchemeView.MARGIN;
				pos[1] = b[3] / 2 + SchemeView.MARGIN;
				curShape.setPosition(pos[0], pos[1]);
			}
			this.xField.setText(Double.toString(pos[0]));
			this.yField.setText(Double.toString(pos[1]));
			pos = curShape.getBounds();
			this.wField.setText(Double.toString(pos[2]));
			this.hField.setText(Double.toString(pos[3]));
			setColorButton(curShape.getColor());
			setFillColorButton(curShape.getFillColor());
			if (parent != null)
				parent.repaint();
		} else {
			this.xField.setVisible(false);
			this.yField.setVisible(false);
			this.wField.setVisible(false);
			this.hField.setVisible(false);
			this.xFieldLabel.setVisible(false);
			this.yFieldLabel.setVisible(false);
			this.wFieldLabel.setVisible(false);
			this.hFieldLabel.setVisible(false);
			this.colorLabel.setVisible(false);
			this.colorLabelLabel.setVisible(false);
			this.fillColorLabel.setVisible(false);
			this.fillColorLabelLabel.setVisible(false);

			this.dynPositionCheckbox.setVisible(false);
			this.dynXField.setVisible(false);
			this.dynYField.setVisible(false);
			this.dynXFieldLabel.setVisible(false);
			this.dynYFieldLabel.setVisible(false);

			this.dynSizeCheckbox.setVisible(false);
			this.dynWField.setVisible(false);
			this.dynHField.setVisible(false);
			this.dynWFieldLabel.setVisible(false);
			this.dynHFieldLabel.setVisible(false);

			setColorButton(null);
			setFillColorButton(null);
		}
		if (parent != null)
			parent.invalidate();

	}

	public void setEntity(EntityInterface e, boolean moveable) {
		this.entity = e;
		this.moveable = moveable;
		if (!moveable && parent != null)
			parent.removeAll();
		if (curShape != null) {
			curShape.removeListener(this);
			curShape = null;
		}
		nameField.setText(e.getName());
		activeField.setText(e.getEnabledExpression().toString());
		transferShape();
		((ShapeFactoryComboBoxModel) shapeChooser.getModel()).setSelectedItem();
		((AttributeFactoryListModel) attributeList.getModel()).transfer();
		((BehaviourListModel) behaviourList.getModel()).transfer();
		updateGrayButton();
	}

	public void setParent(SchemeView parent, BehaviourGraphFactory factory) {
		this.parent = parent;
		this.graphFactory = factory;
	}

	public void setWorldEditor(WorldEditorPanel worldEditor) {
		this.worldEditor = worldEditor;
	}

	public void changed(DrawableShape s) {
		representationChanged(null);
	}

	public void moved(DrawableShape s) {
		if (curShape != null) {
			if (!moveable) {
				double[] size = curShape.getBounds();
				curShape.setPosition(size[2] / 2 + SchemeView.MARGIN, size[3]
						/ 2 + SchemeView.MARGIN);
				if (parent != null)
					parent.repaint();
			} else {
				double[] pos = curShape.getPosition();
				xField.setText(Double.toString(pos[0]));
				yField.setText(Double.toString(pos[1]));
				representationChanged(null);
			}
		}
	}

	public void resized(DrawableShape s) {
		if (curShape != null) {
			double[] size = curShape.getBounds();
			if (!moveable) {
				curShape.setPosition(size[2] / 2 + SchemeView.MARGIN, size[3]
						/ 2 + SchemeView.MARGIN);
				if (parent != null)
					parent.repaint();
			}
			wField.setText(Double.toString(size[2]));
			hField.setText(Double.toString(size[3]));
			representationChanged(null);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("shape")) {
			((ShapeFactoryComboBoxModel) shapeChooser.getModel()).setShape();
			transferShape();
		} else if (action.equals("name")) {
			entity.setName(nameField.getText());
			representationChanged(null);
		} else if (action.equals("active")) {
			try {
				entity.setEnabledExpression(new Expression(activeField
						.getText()));
			} catch (ExpressionParseException e1) {
			}
			activeField.setText(entity.getEnabledExpression().toString());
			internalChanged(null);
		} else if (action.equals("dyn_pos")) {
			if (this.dynPositionCheckbox.isSelected()) {
				this.dynXField.setVisible(true);
				this.dynYField.setVisible(true);
				this.dynXFieldLabel.setVisible(true);
				this.dynYFieldLabel.setVisible(true);
				if (this.entity != null && curShape != null) {
					double[] pos = curShape.getPosition();
					this.entity.setDynamicPosition(new Expression(pos[0]),
							new Expression(pos[1]));
					this.dynXField
							.setText(this.entity.getDynamicX().toString());
					this.dynYField
							.setText(this.entity.getDynamicY().toString());
				}
			} else {
				this.dynXField.setVisible(false);
				this.dynYField.setVisible(false);
				this.dynXFieldLabel.setVisible(false);
				this.dynYFieldLabel.setVisible(false);
				if (this.entity != null) {
					this.entity.setDynamicPosition();
				}
			}
		} else if (action.equals("dyn_size")) {
			if (this.dynSizeCheckbox.isSelected()) {
				this.dynWField.setVisible(true);
				this.dynHField.setVisible(true);
				this.dynWFieldLabel.setVisible(true);
				this.dynHFieldLabel.setVisible(true);
				if (this.entity != null && curShape != null) {
					double[] pos = curShape.getBounds();
					this.entity.setDynamicSize(new Expression(pos[2]),
							new Expression(pos[3]));
					this.dynWField.setText(this.entity.getDynamicWidth()
							.toString());
					this.dynHField.setText(this.entity.getDynamicHeight()
							.toString());
				}
			} else {
				this.dynWField.setVisible(false);
				this.dynHField.setVisible(false);
				this.dynWFieldLabel.setVisible(false);
				this.dynHFieldLabel.setVisible(false);
				if (this.entity != null) {
					this.entity.setDynamicSize();
				}
			}
		} else if (action.equals("dyn_x")) {
			if (dynXField.isVisible() && this.entity != null) {
				try {
					this.entity
							.setDynamicX(new Expression(dynXField.getText()));
					dynXField.setText(this.entity.getDynamicX().toString());
					internalChanged(null);
				} catch (Exception exn) {
					JOptionPane.showMessageDialog(this, "Parse error",
							exn.getMessage(), JOptionPane.ERROR_MESSAGE);
					dynXField.setText(this.entity.getDynamicX().toString());
				}
			}
		} else if (action.equals("dyn_y")) {
			if (dynYField.isVisible() && this.entity != null) {
				try {
					this.entity
							.setDynamicY(new Expression(dynYField.getText()));
					dynYField.setText(this.entity.getDynamicY().toString());
					internalChanged(null);
				} catch (Exception exn) {
					JOptionPane.showMessageDialog(this, "Parse error",
							exn.getMessage(), JOptionPane.ERROR_MESSAGE);
					dynYField.setText(this.entity.getDynamicY().toString());
				}
			}
		} else if (action.equals("dyn_w")) {
			if (dynWField.isVisible() && this.entity != null) {
				try {
					this.entity.setDynamicWidth(new Expression(dynWField
							.getText()));
					dynWField.setText(this.entity.getDynamicWidth().toString());
					internalChanged(null);
				} catch (Exception exn) {
					JOptionPane.showMessageDialog(this, "Parse error",
							exn.getMessage(), JOptionPane.ERROR_MESSAGE);
					dynWField.setText(this.entity.getDynamicWidth().toString());
				}
			}
		} else if (action.equals("dyn_h")) {
			if (dynHField.isVisible() && this.entity != null) {
				try {
					this.entity.setDynamicHeight(new Expression(dynHField
							.getText()));
					dynHField
							.setText(this.entity.getDynamicHeight().toString());
					internalChanged(null);
				} catch (Exception exn) {
					JOptionPane.showMessageDialog(this, "Parse error",
							exn.getMessage(), JOptionPane.ERROR_MESSAGE);
					dynHField
							.setText(this.entity.getDynamicHeight().toString());
				}
			}
		} else if (action.equals("x")) {
			if (moveable && curShape != null) {
				double[] pos = curShape.getPosition();
				try {
					curShape.setPosition(Double.parseDouble(xField.getText()),
							pos[1]);
					representationChanged(null);
				} catch (NumberFormatException exn) {
					xField.setText(Double.toString(pos[0]));
				}
			}
		} else if (action.equals("y")) {
			if (moveable && curShape != null) {
				double[] pos = curShape.getPosition();
				try {
					curShape.setPosition(pos[0],
							Double.parseDouble(yField.getText()));
					representationChanged(null);
				} catch (NumberFormatException exn) {
					yField.setText(Double.toString(pos[1]));
				}
			}
		} else if (action.equals("w")) {
			if (curShape != null) {
				double[] pos = curShape.getBounds();
				try {
					curShape.setSize(Double.parseDouble(wField.getText()),
							pos[3]);
					representationChanged(null);
				} catch (NumberFormatException exn) {
					wField.setText(Double.toString(pos[2]));
				}
			}
		} else if (action.equals("h")) {
			if (curShape != null) {
				double[] pos = curShape.getBounds();
				try {
					curShape.setSize(pos[2],
							Double.parseDouble(hField.getText()));
					representationChanged(null);
				} catch (NumberFormatException exn) {
					hField.setText(Double.toString(pos[3]));
				}
			}
		} else if (action.equals("add_attr")) {
			((AttributeFactoryListModel) attributeList.getModel()).add();
			updateGrayButton();
			updateAttributePanel();
		} else if (action.equals("del_attr")) {
			((AttributeFactoryListModel) attributeList.getModel()).delete();
			updateGrayButton();
			updateAttributePanel();
		} else if (action.equals("color")) {
			if (entity == null)
				return;
			if (curShape == null)
				return;
			Color c = curShape.getColor();
			Color newColor = JColorChooser.showDialog(this,
					"Choose color of entity", c);
			if (newColor != null) {
				if (newColor.equals(Color.WHITE))
					newColor = null;
				setColorButton(newColor);
				curShape.setColor(newColor);
				representationChanged(null);
				repaint();
			}
		} else if (action.equals("fill_color")) {
			if (entity == null)
				return;
			if (curShape == null)
				return;
			if (!curShape.hasFillColor())
				return;
			Color c = curShape.getFillColor();
			Color newColor = JColorChooser.showDialog(this,
					"Choose filling color of entity", c);
			if (newColor != null) {
				if (newColor.equals(Color.WHITE))
					newColor = null;
				setFillColorButton(newColor);
				curShape.setFillColor(newColor);
				representationChanged(null);
				repaint();
			}
		} else if (action.equals("add_behaviour")) {
			((BehaviourListModel) behaviourList.getModel()).add();
			updateGrayButton();
			updateAttributePanel();
		} else if (action.equals("del_behaviour")) {
			((BehaviourListModel) behaviourList.getModel()).delete();
			updateGrayButton();
			updateAttributePanel();
		}
	}

	private void updateAttributePanel() {
		Component o = ((AttributeFactoryListModel) attributeList.getModel())
				.getComponent();
		if (o == null) {
			Component c = this.getBottomComponent();
			if (c != null)
				c.setVisible(false);
		} else {
			o.setVisible(true);
			this.setBottomComponent(o);
		}
	}

	public void updateBehaviour() {
		updateGrayButton();
	}

	private void updateGrayButton() {
		delAttrButton.setEnabled(attributeList.getSelectedIndex() >= 0);
		addAttrButton.setEnabled(((AttributeFactoryListModel) attributeList
				.getModel()).canAdd());
		delBehaviourButton.setEnabled(behaviourList.getSelectedIndex() >= 0);
		addBehaviourButton.setEnabled(((BehaviourListModel) behaviourList
				.getModel()).canAdd());
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == attributeList) {
			updateAttributePanel();
		} else if (e.getSource() == behaviourList) {
			if (graphFactory != null) {
				Object o = (behaviourList.getSelectedValue());
				if (o == null || !(o instanceof BehaviourGraph)) {
					graphFactory.getRootView().setVisible(false);
				} else {
					graphFactory.getRootView().setVisible(true);
					graphFactory.loadGraph((BehaviourGraph) o);
				}
			}
		}
		updateGrayButton();
	}

	public void setAvailableBehaviours(
			Collection<Behaviour> availableBehaviours, boolean copy) {
		this.copyBehaviours = copy;
		this.availableBehaviours = availableBehaviours;
		((BehaviourListModel) behaviourList.getModel()).transfer();
	}

	public EntityInterface getEntity() {
		return entity;
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
			r[0] = entity;
			for (i = 0; i < o.length; i++) {
				r[i + 1] = o[i];
			}
		} else
			r = new Object[] { entity };
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
}
