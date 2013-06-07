package wmas.gui.run.editor;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import wmas.world.Simulator;
import wmas.world.World;

public class WorldSimulatorsEditor extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private SimulatorEditor editor = new SimulatorEditor();
	private JPanel emptyPanel = new JPanel();
	private JComboBox simuList = new JComboBox();
	private JButton addButton = new JButton("+");
	private JButton delButton = new JButton("-");
	private JButton closeButton = new JButton("Close");
	private JButton executeButton = new JButton("Execute");

	private World world;
	private String execute = null;
	private Container parent = null;
	private boolean modified = false;

	private void construct() {
		JRootPane p = getRootPane();
		p.setLayout(new GridBagLayout());
		addButton.setActionCommand("+");
		delButton.setActionCommand("-");
		closeButton.setActionCommand("close");
		executeButton.setActionCommand("execute");
		addButton.addActionListener(this);
		delButton.addActionListener(this);
		closeButton.addActionListener(this);
		executeButton.addActionListener(this);
		simuList.setActionCommand("simu");
		simuList.addActionListener(this);
		editor.setVisible(false);
		delButton.setEnabled(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		p.add(new JLabel(), c);
		p.add(simuList, c);
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0;
		p.add(addButton, c);
		p.add(delButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		p.add(new JLabel(), c);

		c.weighty = 1;
		p.add(editor, c);
		p.add(emptyPanel, c);
		c.weighty = 0;
		JPanel panel = new JPanel(new GridBagLayout());
		p.add(panel, c);
		c.gridwidth = 1;
		c.weighty = 1;
		panel.add(new JLabel(), c);
		c.gridwidth = 1;
		c.weightx = 0;
		panel.add(executeButton, c);
		panel.add(closeButton, c);
		modified = false;

		transfer(null);
	}

	private void transfer(String select) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) simuList.getModel();
		model.removeAllElements();
		if (world != null) {
			for (String s : world.listSimulators()) {
				model.addElement(s);
			}
			if (!(world.listSimulators().isEmpty())) {
				if (select != null && world.listSimulators().contains(select)) {
					simuList.setSelectedItem(select);
				} else {
					simuList.setSelectedIndex(0);
				}
			}
		}
		transferSimulator();
	}

	private void transferSimulator() {
		String sel = null;
		if (world != null && simuList.getSelectedIndex() >= 0)
			sel = simuList.getSelectedItem().toString();
		if (editor.isVisible() && editor.isModified())
			modified = true;
		if (sel == null) {
			executeButton.setEnabled(false);
			delButton.setEnabled(false);
			editor.setVisible(false);
			emptyPanel.setVisible(true);
			editor.setSimu(null);
		} else {
			executeButton.setEnabled(true);
			delButton.setEnabled(true);
			editor.setVisible(true);
			emptyPanel.setVisible(false);
			editor.setSimu(world.getSimulator(sel));
		}
	}

	public WorldSimulatorsEditor(Dialog owner, String title) {
		super(owner, title, true);
		parent = owner;
		construct();
		setSize(600, 400);
	}

	public WorldSimulatorsEditor(Frame owner, String title) {
		super(owner, title, true);
		parent = owner;
		construct();
		setSize(600, 400);
	}

	public String display(World w) {
		this.world = w;
		this.execute = null;
		modified = false;
		transfer(null);
		setLocationRelativeTo(parent);
		setVisible(true);
		return execute;
	}

	public String display(World w, String select) {
		this.world = w;
		this.execute = null;
		modified = false;
		transfer(select);
		setLocationRelativeTo(parent);
		setVisible(true);
		return execute;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("close")) {
			execute = null;
			if (editor.isVisible()) {
				modified = modified || editor.isModified();
			}
			setVisible(false);
		} else if (e.getActionCommand().equals("execute")) {
			execute = simuList.getSelectedIndex() >= 0 ? simuList
					.getSelectedItem().toString() : null;
			if (editor.isVisible()) {
				modified = modified || editor.isModified();
			}
			setVisible(false);
		} else if (e.getActionCommand().equals("+")) {
			if (world != null) {
				String s = JOptionPane.showInputDialog(this,
						"Please enters name for execution:", "Execution name",
						JOptionPane.OK_CANCEL_OPTION);
				if (s != null && !s.isEmpty()
						&& !world.listSimulators().contains(s)) {
					modified = true;
					world.setSimulator(s, new Simulator());
					transfer(s);
				}
			}
		} else if (e.getActionCommand().equals("-")) {
			if (world != null && simuList.getSelectedIndex() >= 0) {
				String s = simuList.getSelectedItem().toString();
				if (s != null && !s.isEmpty()
						&& world.listSimulators().contains(s)) {
					modified = true;
					world.removeSimulator(s);
					transfer(null);
				}
			}

		} else if (e.getActionCommand().equals("simu")) {
			transferSimulator();
		}

	}

	public boolean isModified() {
		return modified;
	}
}
