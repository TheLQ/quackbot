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
package org.quackbot.dao.hibernate.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quackbot.dao.model.UserEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(of = "nick")
@Entity
@Table(name = "users")
public class UserEntryHibernate implements Serializable, UserEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "USER_ID", nullable = false)
	protected Long id;
	@ManyToOne
	@JoinColumn(name = "SERVER_ID")
	protected ServerEntryHibernate server;
	@Column(name = "nick", length = 100, nullable = false)
	protected String nick;
	@Column(name = "login", length = 100)
	protected String login;
	@Column(name = "hostmask", length = 100)
	protected String hostmask;
	@Column(name = "realname", length = 100)
	protected String realname;
	@Column(name = "hops", length = 100)
	protected Integer hops;
	@Column(name = "connectedServer", length = 100)
	protected String connectedServer;

	public UserEntryHibernate() {
	}
}
