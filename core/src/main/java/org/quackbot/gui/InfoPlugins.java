/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.gui;

import org.quackbot.Command;
import org.quackbot.events.HookLoadEvent;
import org.quackbot.events.HookLoadStartEvent;
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
 * @author Leon Blakey <lord.quackstar at gmail.com>
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
		HookManager.addHook(new Hook("QBGuiPluginPanel") {
			private Logger log = LoggerFactory.getLogger(getClass());

			@Override
			public void onHookLoad(HookLoadEvent event) {
				Hook hook = event.getHook();
				//TODO: Handle exceptions
				if(hook instanceof Command) {
					Command command = (Command)hook;
					pluginTableModel.addRow(new Object[]{command.getName(),
									"Command",
									command.isEnabled(),
									command.isAdmin(),
									command.getRequiredParams(),
									command.getOptionalParams(),
									command.getHelp()});
				}
				else {
					pluginTableModel.addRow(new Object[]{hook.getName(),
									"Hook",
									null,
									null,
									null,
									null,
									null});
				}
			}

			@Override
			public void onHookLoadStart(HookLoadStartEvent event) {
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
		pluginTableModel.addColumn("Type");
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
				Command command = HookManager.getCommand(plugin);
				if(command != null) {
					command.setEnabled((Boolean) model.getValueAt(e.getFirstRow(), 1));
					log.debug((command.isEnabled() ? "Enabled" : "Disabled") + " command " + command.getName());
				}
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
}
