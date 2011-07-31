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
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(of = "name")
@ToString(exclude = {"channels", "servers"})
@Entity
@Table(name = "admins")
public class AdminEntryHibernate implements AdminEntry<Long>, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "ADMIN_ID", nullable = false)
	private Long id;
	@Column(name = "name", length = 50, nullable = false)
	private String name;
	@ManyToMany(targetEntity = ChannelEntryHibernate.class, mappedBy = "admins")
	private Set<ChannelEntry> channels = new HashSet();
	@ManyToMany(targetEntity = ServerEntryHibernate.class, mappedBy = "admins")
	private Set<ServerEntry> servers = new HashSet();

	public AdminEntryHibernate() {
	}
}
