/**
 * @(#)Main.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.log.ControlAppender; 
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.TimeZone;
import javax.swing.BorderFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import org.apache.log4j.ConsoleAppender;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 * Provides a GUI for bot
 *  -Output is formated and displayed
 *  -Can initate stop, start, and reload from here
 * @author Lord.Quackstar
 */
public class Main extends JFrame implements ActionListener {

	public JTextPane BerrorLog,CerrorLog;
	public Controller ctrl = null;
	Logger log = Logger.getLogger(Main.class);

	/**
	 * Setup and display GUI, redirect output streams, start Controller
	 */
	public Main() {
		/***Pre init, setup error log**/
		BerrorLog = new JTextPane();
		BerrorLog.setEditable(false);
		BerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		CerrorLog = new JTextPane();
		CerrorLog.setEditable(false);
		CerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);

		//Add appenders to root logger
		Logger rootLog = Logger.getRootLogger();
		rootLog.setLevel(Level.TRACE);
		rootLog.addAppender(new ControlAppender(this));
		//rootLog.addAppender(new ConsoleAppender(new PatternLayout()));
		
		TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Will exit when close button is pressed
		setTitle("Quackbot GUI Control Panel");
		setMinimumSize(new Dimension(1000, 700));

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		//Configuration of body
		JScrollPane BerrorScroll = new JScrollPane(BerrorLog);
		BerrorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		BerrorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		BerrorScroll.setBorder(BorderFactory.createTitledBorder("Bot talk"));
		JScrollPane CerrorScroll = new JScrollPane(CerrorLog);
		CerrorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		CerrorScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
		CerrorScroll.setBorder(BorderFactory.createTitledBorder("Controller talk"));

		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,BerrorScroll,CerrorScroll);
		contentPane.add(mainSplit, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		JButton cancel = new JButton("Stop");
		cancel.addActionListener(this);
		bottom.add(cancel);
		JButton start = new JButton("Start");
		start.addActionListener(this);
		bottom.add(start);
		JButton reload = new JButton("Reload");
		reload.addActionListener(this);
		bottom.add(reload);

		contentPane.add(bottom, BorderLayout.SOUTH);

		add(contentPane); //add to JFrame
		setVisible(true); //make JFrame visible

		//Initialize controller in new thread to prevent GUI lockups
		new Thread(new Runnable() {
			public void run() {
				log.info("Initialiing controller");
				Main.this.ctrl = new Controller(Main.this);
			}
		}).start();

		mainSplit.setDividerLocation(0.50);
	}

	/**
	 * Button action listener, controls for Controller
	 * @param e  Event
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("Stop")) {
			ctrl.stopAll();
			ctrl = null;
		} else if (cmd.equals("Start")) {
			if (ctrl != null) {
				int num = JOptionPane.showConfirmDialog(this, "Controller is not null! /n Are you sure you want to create another instance?", "Warning!", JOptionPane.YES_NO_OPTION);
				if (num == JOptionPane.NO_OPTION) {
					return;
				}
			}
			ctrl = new Controller(this);
		} else if (cmd.equals("Reload")) {
			ctrl.threadPool.execute(new loadCMDs(ctrl));
		}
	}

	/**
	 * Main method, starts Main
	 * @param args  Passed parameters. This is ignored
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new Main();
			}
		});
	}
}
