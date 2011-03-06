/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.gui;

import org.quackbot.BaseCommand;
import org.quackbot.Command;
import org.quackbot.CommandManager;
import org.quackbot.hook.HookManager;
import org.quackbot.hook.Hook;
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
	protected final static DefaultTableModel pluginTableModel = new DefaultTableModel() {
		@Override
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	protected final static JTable pluginTable = new JTable(pluginTableModel) {
		@Override
		public String getToolTipText(MouseEvent e) {
			Point p = e.getPoint();
			int rowIndex = pluginTable.rowAtPoint(p);
			int colIndex = pluginTable.columnAtPoint(p);
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
			public void onPluginLoadComplete() throws Exception {
				for (BaseCommand command : CommandManager.getCommands()) {
					pluginTableModel.addRow(new Object[]{command.getName(),
								command.isEnabled(),
								command.isAdmin(),
								command.getRequiredParams(),
								command.getOptionalParams(),
								command.getHelp()});
				}
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
		pluginTableModel.addColumn("Required");
		pluginTableModel.addColumn("Optional");
		pluginTableModel.addColumn("Help");
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
				BaseCommand curPlugin = CommandManager.getCommand(plugin);
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

	public String pluginToString(BaseCommand cmd) {
		return "[enabled=" + cmd.isEnabled() + "] "
				+ " [admin=" + cmd.isAdmin() + "] ";
	}
}