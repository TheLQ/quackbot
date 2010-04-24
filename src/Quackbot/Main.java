/**
 * @(#)Main.java
 *
 * This file is part of Quackbot
 */
package Quackbot;

import Quackbot.log.ControlAppender;
import Quackbot.log.StdRedirect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.TimeZone;

import javax.swing.BorderFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Provides a GUI for bot
 *  -Output is formated and displayed
 *  -Can initate Reload
 * 
 * There should only be <b>1</b> instance of this. It can be refrenced by {@link Quackbot.InstanceTracker#getMain() InstanceTracker.getMain}
 * @author Lord.Quackstar
 */
public class Main extends JFrame implements ActionListener {

	/**
	 * GUI log pane's
	 */
	public JTextPane BerrorLog, CerrorLog;
	/**
	 * Log4j logger
	 */
	private Logger log = Logger.getLogger(Main.class);
	/**
	 * Backup standard output stream
	 */
	public PrintStream out;
	/**
	 * Backup standard error stream
	 */
	public PrintStream err;

	/**
	 * Setup and display GUI, setup Log4j, start Controller
	 */
	public Main() {
		/***Pre init, setup error log**/
		InstanceTracker.setMain(this);
		BerrorLog = new JTextPane();
		BerrorLog.setEditable(false);
		BerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		CerrorLog = new JTextPane();
		CerrorLog.setEditable(false);
		CerrorLog.setAlignmentX(Component.CENTER_ALIGNMENT);

		//Add appenders to root logger
		out = System.out;
		err = System.err;
		System.setOut(new PrintStream(new StdRedirect(new ByteArrayOutputStream(), false)));
		System.setErr(new PrintStream(new StdRedirect(new ByteArrayOutputStream(), true)));
		Logger rootLog = Logger.getRootLogger();
		rootLog.setLevel(Level.TRACE);
		rootLog.addAppender(new ControlAppender());

		TimeZone.setDefault(TimeZone.getTimeZone("GMT-5"));

		//On close, kill all connections gracefully
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //Will exit when close button is pressed
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				Logger log = Logger.getLogger(this.getClass());
				log.info("Closing all IRC and db connections gracefully");
				Controller ctrl = InstanceTracker.getController();
				ctrl.stopAll();
				try {
					ctrl.dbm.close();
				}
				catch(Exception e) {
					e.printStackTrace(err); //send to standard output because window is closing
				}
				System.exit(0);
			}
		});
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

		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, BerrorScroll, CerrorScroll);
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
				new Controller();
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

		if (cmd.equals("Reload"))
			ThreadPoolManager.addMain(new loadCMDs());
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
