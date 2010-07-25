/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import Quackbot.hook.HookManager;
import Quackbot.hook.Hook;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
		@Override
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	final static JTable pluginTable = new JTable(pluginTableModel) {
		@Override
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
		HookManager.addPluginHook(new Hook("QBGuiPluginPanel") {
			private Logger log = LoggerFactory.getLogger(getClass());
			@Override
			public void onCommandLoad(Command command) throws Exception {
				log.info("Whoo, called on "+command.getName());
				pluginTableModel.addRow(new Object[]{command.getName(),
							command.isEnabled(),
							command.isAdmin(),
							command.getHelp(),
							command.getRequiredParams(),
							command.getOptionalParams()});
			}

			@Override
			public void onPluginLoadStart() throws Exception {
				pluginTableModel.setRowCount(0);
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
		pluginTableModel.addColumn("Help");
		pluginTableModel.addColumn("Required");
		pluginTableModel.addColumn("Optional");
		pluginTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


		TableColumnModel columnModel = pluginTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(50);
		columnModel.getColumn(4).setPreferredWidth(40);
		columnModel.getColumn(5).setPreferredWidth(520);


		pluginTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() != TableModelEvent.UPDATE)
					return;
				TableModel model = ((TableModel) e.getSource());
				String plugin = StringUtils.trimToNull((String) ((TableModel) e.getSource()).getValueAt(e.getFirstRow(), 0));
				Command curPlugin = CommandManager.getCommand(plugin);
				curPlugin.setEnabled((Boolean) model.getValueAt(e.getFirstRow(), 1));
				curPlugin.setAdmin((Boolean) model.getValueAt(e.getFirstRow(), 2));
				log.debug("Set plugin " + curPlugin.getName() + " to " + pluginToString(curPlugin));
			}
		});

		//Inefficent but only way to check if value has changed
		/**new Thread(new Runnable() {
		public void run() {
		while (true) {
		for (int i = 0; i < pluginTableModel.getRowCount(); i++) {
		Boolean isIgnored = Controller.instance.findCommand(pluginTableModel.getValueAt(i, 0).toString()).isIgnore();
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

	public String pluginToString(Command cmd) {
		return "[enabled=" + cmd.isEnabled() + "] "
				+ " [admin=" + cmd.isAdmin() + "] ";
	}
}
