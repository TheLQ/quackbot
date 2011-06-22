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

import org.quackbot.hooks.Command;
import org.quackbot.Controller;
import org.quackbot.events.HookLoadEvent;
import org.quackbot.events.HookLoadStartEvent;
import org.quackbot.hooks.HookManager;
import org.quackbot.hooks.Hook;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class InfoPlugins extends JScrollPane {
	protected DefaultTableModel pluginTableModel;
	protected JTable pluginTable;
	
	public InfoPlugins(final Controller controller) {
		super();
		setBorder(BorderFactory.createTitledBorder("Active plugins"));
		
		//Configure the table model
		pluginTableModel = new DefaultTableModel() {
			@Override
			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
		};
		pluginTableModel.addColumn("Name");
		pluginTableModel.addColumn("Type");
		pluginTableModel.addColumn("Enabled");
		pluginTableModel.addColumn("Admin Only");
		pluginTableModel.addColumn("Required");
		pluginTableModel.addColumn("Optional");
		pluginTableModel.addColumn("Help");
		pluginTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		//Configure the default column widths
		TableColumnModel columnModel = pluginTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(50);
		columnModel.getColumn(4).setPreferredWidth(40);
		columnModel.getColumn(5).setPreferredWidth(520);
		
		//Toggle command enabled status when changed on the table
		pluginTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() != TableModelEvent.UPDATE)
					return;
				TableModel model = ((TableModel) e.getSource());
				String plugin = StringUtils.trimToNull((String) ((TableModel) e.getSource()).getValueAt(e.getFirstRow(), 0));
				Command command = controller.getHookManager().getCommand(plugin);
				if(command != null) {
					command.setEnabled((Boolean) model.getValueAt(e.getFirstRow(), 1));
					log.debug((command.isEnabled() ? "Enabled" : "Disabled") + " command " + command.getName());
				}
			}
		});
		
		//Configure the table
		pluginTable = new JTable(pluginTableModel) {
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
		pluginTable.setRowSelectionAllowed(false);
		pluginTable.setColumnSelectionAllowed(false);
		pluginTable.setCellSelectionEnabled(false);
		
		//Add the table to the scroll pane
		add(pluginTable);
		
		//Get notified of hook changes
		controller.getHookManager().addHook(new Hook("QBGuiPluginPanel") {
			@Override
			public void onHookLoad(HookLoadEvent event) {
				//TODO: Inform user about exception
				if(event.getException() != null)
					return;
				Hook hook = event.getHook();
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
}
