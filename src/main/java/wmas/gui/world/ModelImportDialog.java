package wmas.gui.world;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import wmas.behaviour.Behaviour;
import wmas.world.EntityInterface;
import wmas.world.Model;
import wmas.world.World;

public class ModelImportDialog extends JDialog implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private JList entityList;
	private JList behaviourList;
	private JList attributeList;
	private Model model;
	private Model result = null;
	private Component parent = null;

	class AttributeListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private LinkedList<String> attr = new LinkedList<String>();

		public void create() {
			attr.clear();
			attr.addAll(model.getAttributes());
		}

		public String getSelected() {
			int ind = attributeList.getSelectedIndex();
			if (ind < 0 || ind > attr.size())
				return null;
			return attr.get(attributeList.getSelectedIndex());
		}

		public Object getElementAt(int index) {
			return attr.get(index);
		}

		public int getSize() {
			return attr.size();
		}

		void refresh() {
			create();
			fireIntervalRemoved(this, 0, 10000);
			fireIntervalAdded(this, 0, attr.size());
		}
	}

	class BehaviourListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		public Behaviour getSelected() {
			int ind = behaviourList.getSelectedIndex();
			if (ind < 0 || ind > model.getAvailableBehaviours().size())
				return null;
			return model.getAvailableBehaviours().get(ind);
		}

		public Object getElementAt(int index) {
			return model.getAvailableBehaviours().get(index);
		}

		public int getSize() {
			return model.getAvailableBehaviours().size();
		}

		void refresh() {
			fireIntervalRemoved(this, 0, 10000);
			fireIntervalAdded(this, 0, model.getAvailableBehaviours().size());
		}
	}

	class EntityListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		public EntityInterface getSelected() {
			int ind = entityList.getSelectedIndex();
			if (ind < 0 || ind > model.getEntities().size())
				return null;
			return model.getEntities().get(ind);
		}

		public Object getElementAt(int index) {
			return model.getEntities().get(index).getName();
		}

		public int getSize() {
			return model.getEntities().size();
		}

		void refresh() {
			fireIntervalRemoved(this, 0, 10000);
			fireIntervalAdded(this, 0, model.getEntities().size());
		}
	}

	private void construct() {
		entityList = new JList(new EntityListModel());
		behaviourList = new JList(new BehaviourListModel());
		attributeList = new JList(new AttributeListModel());

		entityList.addListSelectionListener(this);
		behaviourList.addListSelectionListener(this);
		attributeList.addListSelectionListener(this);

		entityList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		behaviourList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		entityList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		behaviourList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		attributeList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		setResizable(false);
		setSize(400, 300);
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;

		getContentPane().add(new JLabel("Select behaviours to import:"), c);
		c.gridwidth = 2;
		getContentPane().add(new JLabel("Select entities to import:"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(new JLabel("Select attributes to import:"), c);

		c.weighty = 1;
		c.insets = new Insets(0, 2, 2, 2);
		c.gridwidth = 1;
		getContentPane().add(behaviourList, c);
		c.gridwidth = 2;
		getContentPane().add(entityList, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(attributeList, c);

		c.weighty = 0;
		c.gridwidth = 1;
		getContentPane().add(new JLabel(), c);
		c.fill = GridBagConstraints.EAST;
		JButton b = new JButton("Cancel");
		b.setActionCommand("cancel");
		b.addActionListener(this);
		getContentPane().add(b, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.WEST;
		b = new JButton("OK");
		b.setActionCommand("ok");
		b.addActionListener(this);
		getContentPane().add(b, c);
	}

	public ModelImportDialog() {
		super();
		this.setTitle("Selection object to import");
		setVisible(false);
		construct();
	}

	public ModelImportDialog(Frame owner) {
		super(owner, "Selection object to import", true);
		setVisible(false);
		parent = owner;
		construct();
	}

	public ModelImportDialog(Dialog owner) {
		super(owner, "Selection object to import", true);
		setVisible(false);
		parent = owner;
		construct();
	}

	private void display() {
		((EntityListModel) entityList.getModel()).refresh();
		((BehaviourListModel) behaviourList.getModel()).refresh();
		((AttributeListModel) attributeList.getModel()).refresh();
		entityList.setSelectionInterval(0, model.getEntities().size());
		behaviourList.setSelectionInterval(0, model.getAvailableBehaviours()
				.size());
		attributeList.setSelectionInterval(0, model.getAttributes().size());
		result = null;
		setLocationRelativeTo(parent); // center
		setVisible(true);
	}

	public Model display(Model m) {
		model = m;
		display();
		return result;
	}

	public Model display(World m) {
		model = new Model();
		for (EntityInterface e : m.listEntities()) {
			for (Behaviour be : e.getBehaviours()) {
				if (!model.getAvailableBehaviours().contains(be))
					model.getAvailableBehaviours().add(be);
			}
			model.getEntities().add(e);
		}
		display();
		return result;
	}

	public Model display(Object o) {
		if (o instanceof World) {
			model = new Model();
			World m = (World) o;
			for (EntityInterface e : m.listEntities()) {
				for (Behaviour be : e.getBehaviours()) {
					if (!model.getAvailableBehaviours().contains(be))
						model.getAvailableBehaviours().add(be);
				}
				model.getEntities().add(e);
			}
		} else if (o instanceof Model) {
			model = (Model) o;
		}
		display();
		return result;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			result = new Model();
			int[] ind = behaviourList.getSelectedIndices();
			HashMap<Behaviour, Behaviour> bMap = new HashMap<Behaviour, Behaviour>();
			for (int i : ind) {
				if (i >= 0 && i < model.getAvailableBehaviours().size()) {
					Behaviour orig = model.getAvailableBehaviours().get(i);
					Behaviour dest = orig.copy();
					bMap.put(orig, dest);
					result.getAvailableBehaviours().add(dest);
				}
			}
			ind = entityList.getSelectedIndices();
			for (int i : ind) {
				if (i >= 0 && i < model.getEntities().size())
					result.getEntities().add(
							model.getEntities().get(i).copy(bMap));
			}
			Object[] objs = attributeList.getSelectedValues();
			for (Object o : objs) {
				result.addAttribute(o.toString(),
						model.getAttribute(o.toString()).copy());
			}
		}
		setVisible(false);
	}

	public void valueChanged(ListSelectionEvent e) {
		int[] ind = entityList.getSelectedIndices();
		int[] ind2 = behaviourList.getSelectedIndices();
		Set<Behaviour> selectedBehaviours = new HashSet<Behaviour>();
		HashMap<EntityInterface, Integer> selectedEntities = new HashMap<EntityInterface, Integer>();
		Set<Behaviour> absentBehaviours = new HashSet<Behaviour>();
		Set<Integer> entityWithAbsentBehaviours = new HashSet<Integer>();
		for (int i : ind2) {
			if (i >= 0 && i < model.getAvailableBehaviours().size()) {
				selectedBehaviours.add(model.getAvailableBehaviours().get(i));
			}
		}
		for (int i : ind) {
			if (i >= 0 && i < model.getEntities().size()) {
				EntityInterface ent = model.getEntities().get(i);
				selectedEntities.put(ent, i);
				for (Behaviour b : ent.getBehaviours()) {
					if (!selectedBehaviours.contains(b)) {
						absentBehaviours.add(b);
						entityWithAbsentBehaviours.add(i);
					}
				}
			}
		}

		if (e.getSource() == behaviourList
				&& entityWithAbsentBehaviours.size() > 0) {
			int[] res = new int[ind.length - entityWithAbsentBehaviours.size()];
			int j = 0;
			for (int i : ind2) {
				if (!entityWithAbsentBehaviours.contains(i)) {
					res[j] = i;
					j++;
				}
			}
			entityList.setSelectedIndices(res);
		} else if (e.getSource() == entityList && absentBehaviours.size() > 0) {
			int[] res = new int[ind2.length + absentBehaviours.size()];
			int j = 0;
			for (int i : ind2) {
				res[j] = i;
				j++;
			}

			for (Behaviour b : absentBehaviours) {
				res[j] = model.getAvailableBehaviours().indexOf(b);
				j++;
			}
			behaviourList.setSelectedIndices(res);
		}
	}
}
