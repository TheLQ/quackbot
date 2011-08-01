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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
@EqualsAndHashCode(of = "address")
@ToString(exclude = {"channels", "admins"})
@Entity
@Table(name = "servers")
public class ServerEntryHibernate implements ServerEntry, Serializable {
	private Long serverId;
	private String address;
	private Integer port;
	private String password;
	private Set<ChannelEntry> channels = new ListenerSet<ChannelEntry>(new HashSet()) {
		@Override
		public void onAdd(ChannelEntry entry) {
			entry.setServer(ServerEntryHibernate.this);
		}

		@Override
		public void onRemove(Object entry) {
			if (!(entry instanceof ChannelEntry))
				throw new RuntimeException("Attempting to remove unknown object from server channel list " + entry);
			//Do nothing
		}
	};
	private Set<AdminEntry> admins = new ListenerSet<AdminEntry>(new HashSet()) {
		@Override
		public void onAdd(AdminEntry entry) {
			entry.getServers().add(ServerEntryHibernate.this);
		}

		@Override
		public void onRemove(Object entry) {
			if (!(entry instanceof AdminEntry))
				throw new RuntimeException("Attempting to remove unknown object from server admin list " + entry);
			//Do nothing
		}
	};

	public ServerEntryHibernate() {
	}

	@ManyToMany(cascade = CascadeType.ALL, targetEntity = AdminEntryHibernate.class)
	@JoinTable(name = "server_admins", joinColumns = {
		@JoinColumn(name = "SERVER_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	//@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<AdminEntry> getAdmins() {
		return admins;
	}

	@OneToMany(targetEntity = ChannelEntry.class, mappedBy = "server", orphanRemoval = true)
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<ChannelEntry> getChannels() {
		return channels;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "SERVER_ID", nullable = false)
	public Long getId() {
		return serverId;
	}

	@Column(name = "address", length = 50, nullable = false)
	public String getAddress() {
		return address;
	}

	@Column(name = "port", length = 5)
	public Integer getPort() {
		return port;
	}

	@Column(name = "password", length = 100)
	public String getPassword() {
		return password;
	}
}
