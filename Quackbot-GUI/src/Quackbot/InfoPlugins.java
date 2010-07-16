/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.hook.Event;
import Quackbot.hook.HookList;
import Quackbot.hook.HookManager;
import Quackbot.hook.PluginHook;
import Quackbot.info.BotEvent;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
public class InfoPlugins extends JScrollPane {
	final static DefaultTableModel pluginTableModel = new DefaultTableModel() {
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	final static JTable pluginTable = new JTable(pluginTableModel) {
		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();
			int rowIndex = pluginTable.rowAtPoint(p);
			int colIndex = pluginTable.columnAtPoint(p);
			//System.out.println("Current column: " + colIndex);
			if ((colIndex == 0 || colIndex == 5 || colIndex == 6) && getValueAt(rowIndex, colIndex) != null)
				return getValueAt(rowIndex, colIndex).toString();
			return "";

		}
	};
	private Logger log = LoggerFactory.getLogger(InfoPlugins.class);

	static {
		HookManager.addHook(Event.onPluginLoadStart, "QBGuiPluginClear", new PluginHook() {
			public void run(HookList hookStack, Bot bot, BotEvent msgInfo) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						pluginTableModel.setRowCount(0);
					}
				});
			}
		});

		HookManager.addHook(Event.onPluginLoad, "QBGuiPluginAdd", new PluginHook<PluginType, Void>() {
			public void run(HookList hookStack, Bot bot, final BotEvent<PluginType, Void> msgInfo) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PluginType plugin = msgInfo.getExtra();
						pluginTableModel.addRow(new Object[]{plugin.getName(),
									plugin.isEnabled(),
									plugin.isAdmin(),
									plugin.isService(),
									plugin.isUtil(),
									plugin.getHelp(),
									plugin.getParamConfig()});
					}
				});
			}
		});
	}

	public InfoPlugins() {
		super(pluginTable);
		setBorder(BorderFactory.createTitledBorder("Active plugins"));
		pluginTable.setRowSelectionAllowed(false);
		pluginTable.setColumnSelectionAllowed(false);
		pluginTable.setCellSelectionEnabled(false);

		pluginTableModel.addColumn("Name");
		pluginTableModel.addColumn("Enabled");
		pluginTableModel.addColumn("Admin Only");
		pluginTableModel.addColumn("Service");
		pluginTableModel.addColumn("Util");
		pluginTableModel.addColumn("Help");
		pluginTableModel.addColumn("ParamConfig");
		pluginTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		pluginTable.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
			}
		});

		TableColumnModel columnModel = pluginTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(50);
		columnModel.getColumn(4).setPreferredWidth(40);
		columnModel.getColumn(5).setPreferredWidth(520);
		columnModel.getColumn(6).setPreferredWidth(620);


		pluginTableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (e.getType() != TableModelEvent.UPDATE)
					return;
				TableModel model = ((TableModel) e.getSource());
				String plugin = StringUtils.trimToNull((String) ((TableModel) e.getSource()).getValueAt(e.getFirstRow(), 0));
				PluginType curPlugin = Controller.instance.findPlugin(plugin);
				curPlugin.setEnabled((Boolean) model.getValueAt(e.getFirstRow(), 1));
				curPlugin.setAdmin((Boolean) model.getValueAt(e.getFirstRow(), 2));
				curPlugin.setService((Boolean) model.getValueAt(e.getFirstRow(), 3));
				curPlugin.setUtil((Boolean) model.getValueAt(e.getFirstRow(), 4));
				log.debug("Set plugin " + curPlugin.getName() + " to " + pluginToString(curPlugin));
			}
		});

		//Inefficent but only way to check if value has changed
		/**new Thread(new Runnable() {
		public void run() {
		while (true) {
		for (int i = 0; i < pluginTableModel.getRowCount(); i++) {
		Boolean isIgnored = Controller.instance.findPlugin(pluginTableModel.getValueAt(i, 0).toString()).isIgnore();
		if (isIgnored == ((Boolean) pluginTableModel.getValueAt(i, 1)))
		pluginTableModel.setValueAt(isIgnored, i, 1);
		}
		try {
		Thread.sleep(1000);
		} catch (InterruptedException e) {
		log.warn("Wait to update plugin list interupted");
		return;
		}
		}
		}
		}).start();**/
	}

	public String pluginToString(PluginType plug) {
		return "[enabled=" + plug.isEnabled() + "] "
				+ " [admin=" + plug.isAdmin() + "] "
				+ " [service=" + plug.isService() + "] "
				+ " [util=" + plug.isUtil() + "] ";
	}
}
