package wmas.gui.run;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import wmas.gui.EditorInterface;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.gui.shapes.SchemeViewListener;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.tree.WorldObjectTreeModel;
import wmas.world.EntityInterface;
import wmas.world.World;
import wmas.world.events.EventComboBoxModel;
import wmas.world.events.SimuEvent;

public class WorldViewPanel extends JPanel implements EditorInterface,
		ActionListener, SchemeViewListener {
	private static final long serialVersionUID = 1L;
	private static final Icon worldIcon = new ImageIcon(
			WorldEditor.class.getResource("icons/world.gif"));

	private SchemeView worldView;
	private WorldObjectTreeModel treeModel = null;
	private JComboBox comboBox;
	private World world = null;
	private JToolBar tb;

	public WorldViewPanel(World w) {
		super(new GridBagLayout());
		this.world = w;
		construct();
		if (w != null)
			transfer();
	}

	private void transfer() {
		worldView.removeAll();
		world.transfer();
	}

	public void setWorld(World w) {
		this.world = w;
		transfer();
	}

	private void construct() {
		worldView = new SchemeView();
		worldView.setEditable(false);
		worldView.addListener(this);

		GridBagConstraints c = new GridBagConstraints();
		tb = new JToolBar(JToolBar.HORIZONTAL);
		JButton tbb = new JButton("SVG");
		tbb.addActionListener(this);
		tbb.setActionCommand("svg");
		tbb.setToolTipText("Export as SVG");
		tb.add(tbb);
		JSeparator jsep = new JSeparator() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
			}
		};
		tb.add(jsep);
		comboBox = new JComboBox(new EventComboBoxModel(true));
		comboBox.setSelectedIndex(0);
		tb.add(comboBox);
		tb.addSeparator();
		tbb = new JButton("+");
		tbb.addActionListener(this);
		tbb.setActionCommand("+");
		tbb.setToolTipText("Zoom in");
		tb.add(tbb);
		tbb = new JButton("-");
		tbb.addActionListener(this);
		tbb.setActionCommand("-");
		tbb.setToolTipText("Zoom out");
		tb.add(tbb);
		tb.setFloatable(false);

		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		add(tb, c);

		c.gridheight = 1;
		c.gridwidth = 1;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		JScrollPane sP = new JScrollPane(worldView);
		worldView.setParentPane(sP);

		add(sP, c);
	}

	public World getWorld() {
		return world;
	}

	@Override
	public boolean canEditObject(Object o) {
		if (o == null)
			return false;
		return o instanceof World;
	}

	@Override
	public Component getEditor() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return worldIcon;
	}

	@Override
	public String getTitle() {
		if (treeModel == null)
			return null;
		return treeModel.getTitle(getWorld());
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setObject(Object o) {
		if (o instanceof World)
			setWorld((World) o);
	}

	public void setTreeModel(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	private JFileChooser svgChooser = null;

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("+")) {
			worldView.setZoom(worldView.getZoom() * 2);
		} else if (e.getActionCommand().equals("-")) {
			worldView.setZoom(worldView.getZoom() / 2);
		} else if (e.getActionCommand().equals("svg")) {
			if (svgChooser == null) {
				svgChooser = new JFileChooser();
				FileFilter ff = new FileNameExtensionFilter("SVG Files", "svg");
				svgChooser.addChoosableFileFilter(ff);
				svgChooser.setFileFilter(ff);
			}
			if (svgChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					worldView.saveSVG(svgChooser.getSelectedFile());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(worldView, e1.getMessage(),
							"Error during SVG export...",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}
	}

	@Override
	public void refresh() {
		repaint();
	}

	public SchemeView getWorldView() {
		return worldView;
	}

	public void addWorldButton(Component comp) {
		tb.add(comp, 0);
	}

	@Override
	public void click(DrawableShape s) {
		EntityInterface e = world.getEntityByShape(s);
		if (e != null && comboBox.getSelectedIndex() > 0
				&& (comboBox.getSelectedItem() instanceof SimuEvent)
				&& world.getSimulator() != null) {
			world.getSimulator().Event((SimuEvent) comboBox.getSelectedItem(),
					e);
		}
	}

	public void mouseMoved(double x, double y) {
	}

	public void mouseOut() {
	}

	public boolean remove(DrawableShape s) {
		return false;
	}

	public void select(DrawableShape s) {
	}
}
