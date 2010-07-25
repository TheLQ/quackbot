/**
 * @(#)GUI.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Quackbot.log.ControlAppender;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

/**
 * Provides a GUI for bot
 *  -Output is formated and displayed
 *  -Can initate Reload
 *
 * There should only be <b>1</b> instance of this. It can be refrenced by {@link #instance}
 * @author Lord.Quackstar
 */
public class GUI extends JFrame implements ActionListener {
	public static GUI instance;
	/**
	 * GUI log pane's
	 */
	public JTextPane BerrorLog, CerrorLog;
	/**
	 * Log4j logger
	 */
	private Logger log = LoggerFactory.getLogger(GUI.class);
	

	/**
	 * Recall's this in AWT event queue
	 */
	public GUI() {
		/***Pre init, setup error log**/
		BerrorLog = new JTextPaneNW();
		BerrorLog.setEditable(false);
		BerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		CerrorLog = new JTextPaneNW();
		CerrorLog.setEditable(false);
		CerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);

		//Add appenders to root logger
		org.apache.log4j.Logger rootLog = org.apache.log4j.Logger.getRootLogger();
		rootLog.setLevel(Level.ALL);
		rootLog.addAppender(new ControlAppender());

		TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Quackbot GUI Control Panel");
		setMinimumSize(new Dimension(1000, 700));

		//Configuration of body
		JPanel msgPanel = new JPanel(new BorderLayout());
		JScrollPane BerrorScroll = new JScrollPane(BerrorLog);
		BerrorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		BerrorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		BerrorScroll.setBorder(BorderFactory.createTitledBorder("Bots"));
		JScrollPane CerrorScroll = new JScrollPane(CerrorLog);
		CerrorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		CerrorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		CerrorScroll.setBorder(BorderFactory.createTitledBorder("Controller"));

		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, BerrorScroll, CerrorScroll);
		msgPanel.add(mainSplit, BorderLayout.CENTER);

		//Configure bottom controls
		JPanel bottom = new JPanel();
		JButton cancel = new JButton("Stop");
		cancel.addActionListener(this);
		bottom.add(cancel);
		JButton start = new JButton("Clear");
		start.addActionListener(this);
		bottom.add(start);
		JButton reload = new JButton("Reload");
		reload.addActionListener(this);
		bottom.add(reload);
		JComboBox logCombo = new JComboBox(new Level[]{Level.ALL, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.OFF});
		logCombo.addActionListener(this);
		bottom.add(logCombo);
		msgPanel.add(bottom, BorderLayout.SOUTH);

		//Configure Plugin Sidepanel
		JTabbedPane contentPane = new JTabbedPane();
		contentPane.addTab("Console",msgPanel);
		contentPane.addTab("Plugin Info",new InfoPlugins());
		contentPane.addTab("Stats",new InfoStats());

		add(contentPane); //add to JFrame
		setVisible(true); //make JFrame visible

		mainSplit.setDividerLocation(0.50);

		instance = this;
	}




	/**
	 * Button action listener, controls for Controller
	 * @param e  Event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			Level level = (Level) ((JComboBox) e.getSource()).getSelectedItem();
			log.info("Setting log level to " + level);
			org.apache.log4j.Logger.getRootLogger().setLevel(level);
			return;
		}

		String cmd = e.getActionCommand();

		if (cmd.equals("Reload"))
			Controller.instance.reloadPlugins();
		if (cmd.equals("Clear")) {
			CerrorLog.setText("");
			BerrorLog.setText("");
		}
	}

	public class JTextPaneNW extends JTextPane {
		public void setSize(Dimension d) {
			if (d.width < getParent().getSize().width)
				d.width = getParent().getSize().width;
			super.setSize(d);
		}

		public boolean getScrollableTracksViewportWidth() {
			return false;
		}
	}
}
