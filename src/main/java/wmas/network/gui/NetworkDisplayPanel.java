package wmas.network.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import wmas.network.Network;
import wmas.world.World;

public class NetworkDisplayPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private NetworkDisplay display;

	public NetworkDisplayPanel() {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		display = new NetworkDisplay();
		JScrollPane pane = new JScrollPane(display);
		display.setScrollPane(pane);

		JToolBar tb = new JToolBar();
		JButton but = new JButton("-");
		but.setActionCommand("net_entity_down");
		but.addActionListener(this);
		tb.add(but);
		tb.add(new JLabel("Distance between lifelines"));
		but = new JButton("+");
		but.setActionCommand("net_entity_up");
		but.addActionListener(this);
		tb.add(but);
		tb.addSeparator();
		but = new JButton("-");
		but.setActionCommand("net_time_down");
		but.addActionListener(this);
		tb.add(but);
		tb.add(new JLabel("Pixel per time unit"));
		but = new JButton("+");
		but.setActionCommand("net_time_up");
		but.addActionListener(this);
		tb.add(but);
		tb.setFloatable(false);

		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		add(tb, c);
		c.weighty = 1;
		add(pane, c);
	}

	public void init() {
		display.init();
	}

	public void setWorld(World world, Network network) {
		display.setWorld(world, network);
	}

	public void update(double t) {
		display.update(t);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("net_time_up")) {
			display.setPixelPerTimeUnit(display.getPixelPerTimeUnit() * 2);
			// netTimeLabel.setText(Double.toString(networkPanel.getPixelPerTimeUnit()));
		} else if (e.getActionCommand().equals("net_time_down")) {
			display.setPixelPerTimeUnit(display.getPixelPerTimeUnit() / 2);
			// netTimeLabel.setText(Double.toString(networkPanel.getPixelPerTimeUnit()));
		} else if (e.getActionCommand().equals("net_entity_up")) {
			display.setDistanceEntity(display.getDistanceEntity() + 5);
			// netEntityLabel.setText(Double.toString(networkPanel.getDistanceEntity()));
		} else if (e.getActionCommand().equals("net_entity_down")) {
			if (display.getDistanceEntity() > 5) {
				display.setDistanceEntity(display.getDistanceEntity() - 5);
				// netEntityLabel.setText(Double.toString(networkPanel.getDistanceEntity()));
			}
		}
	}

}
