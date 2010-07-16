/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author LordQuackstar
 */
class JSplitPaneDivid extends JSplitPane {
	boolean isPainted = false;

	JSplitPaneDivid(int split, Component comp1, Component comp2) {
		super(split, comp1, comp2);
		setDividerSize(6);
	}

	public void paint(Graphics g) {
		if (!isPainted) {
			super.setDividerLocation(0.5);
			isPainted = true;
		}
		super.paint(g);
	}
}

public class InfoStats extends JSplitPaneDivid {
	static LayoutManager panelLayout = new GridLayout(0, 2, 5, 5);
	static JPanel topLeft = new JPanel(panelLayout);
	static JPanel topRight = new JPanel(panelLayout);
	static JPanel bottom = new JPanel(panelLayout);
	JLabel uptime, totalServers, totalChan, totalUsers, totalMessages, totalCommands;

	public InfoStats() {
		super(VERTICAL_SPLIT, new JSplitPaneDivid(HORIZONTAL_SPLIT, topLeft, topRight), bottom);

		//Info bits
		topLeft.add(new JLabel("Uptime",JLabel.RIGHT));
		topLeft.add(uptime = new JLabel("00:00:00"));
		topLeft.add(new JLabel("Connected Servers",JLabel.RIGHT));
		topLeft.add(totalServers = new JLabel("0"));
		topLeft.add(new JLabel("Connected Channels",JLabel.RIGHT));
		topLeft.add(totalChan =  new JLabel("0"));
		topLeft.add(new JLabel("Total Users",JLabel.RIGHT));
		topLeft.add(totalUsers  = new JLabel("0"));
		topLeft.add(new JLabel("Total Messages",JLabel.RIGHT));
		topLeft.add(totalMessages = new JLabel("0"));
		topLeft.add(new JLabel("Total Commands",JLabel.RIGHT));
		topLeft.add(totalCommands = new JLabel("0"));

		topRight.add(new JButton("Top Right"));
		bottom.add(new JButton("Bottom"));
	}

	public JPanel generateInfo(String text, JComponent comp) {
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel(text));
		panel.add(comp);
		return panel;
	}
}
