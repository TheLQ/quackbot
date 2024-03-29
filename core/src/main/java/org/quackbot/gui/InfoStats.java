/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.gui;

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
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
class JSplitPaneDivide extends JSplitPane {
	protected boolean isPainted = false;

	JSplitPaneDivide(int split) {
		this(split, null, null);
	}

	JSplitPaneDivide(int split, Component comp1, Component comp2) {
		super(split, comp1, comp2);
		setDividerSize(6);
	}

	@Override
	public void paint(Graphics g) {
		if (!isPainted) {
			super.setDividerLocation(0.5);
			isPainted = true;
		}
		super.paint(g);
	}
}

public class InfoStats extends JSplitPaneDivide {
	protected final LayoutManager panelLayout = new GridLayout(0, 2, 5, 5);
	protected JPanel topLeft = new JPanel(panelLayout);
	protected JPanel topRight = new JPanel(panelLayout);
	protected JPanel bottom = new JPanel(panelLayout);
	protected JLabel uptime, totalServers, totalChan, totalUsers, totalMessages, totalCommands;

	public InfoStats() {
		super(VERTICAL_SPLIT);
		setLeftComponent(new JSplitPaneDivide(HORIZONTAL_SPLIT, topLeft, topRight));
		setRightComponent(bottom);

		//Info bits
		topLeft.add(new JLabel("Uptime", JLabel.RIGHT));
		topLeft.add(uptime = new JLabel("00:00:00"));
		topLeft.add(new JLabel("Connected Servers", JLabel.RIGHT));
		topLeft.add(totalServers = new JLabel("0"));
		topLeft.add(new JLabel("Connected Channels", JLabel.RIGHT));
		topLeft.add(totalChan = new JLabel("0"));
		topLeft.add(new JLabel("Total Users", JLabel.RIGHT));
		topLeft.add(totalUsers = new JLabel("0"));
		topLeft.add(new JLabel("Total Messages", JLabel.RIGHT));
		topLeft.add(totalMessages = new JLabel("0"));
		topLeft.add(new JLabel("Total Commands", JLabel.RIGHT));
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
