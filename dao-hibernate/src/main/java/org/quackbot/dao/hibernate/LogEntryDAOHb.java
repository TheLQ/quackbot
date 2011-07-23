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
package org.quackbot.dao.hibernate;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import org.quackbot.dao.LogEntryDAO;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@Entity(name = "log")
public class LogEntryDAOHb implements LogEntryDAO, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "LOG_ID", nullable = false)
	protected Integer id;
	
	@Column(name = "timestamp", nullable = false)
	protected Integer timestamp;
	
	@Column(name = "server", nullable = false)
	protected String server;
	
	protected String channel;
	
	protected String type;
	
	protected String user;
	
	protected String message;
	
	protected String rawLine;
}
