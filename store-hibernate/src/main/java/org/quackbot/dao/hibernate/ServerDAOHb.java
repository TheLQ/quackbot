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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.dao.hibernate;

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
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(of = "address")
@ToString(exclude = {"channels", "admins"})
@Entity
@Table(name = "quackbot_servers")
public class ServerDAOHb implements ServerDAO, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "SERVER_ID", nullable = false)
	private Integer serverId;
	@Column(name = "address", length = 50, nullable = false)
	private String address;
	@Column(name = "port", length = 5)
	private Integer port;
	@Column(name = "password", length = 100)
	private String password;
	@OneToMany(targetEntity = ChannelDAOHb.class, mappedBy = "server", orphanRemoval = true)
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	private Set<ChannelDAO> channels = new HashSet();
	@ManyToMany(cascade = CascadeType.ALL, targetEntity = AdminDAOHb.class)
	@JoinTable(name = "quackbot_server_admins", joinColumns = {
		@JoinColumn(name = "SERVER_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	private Set<AdminDAO> admins = new HashSet();

	public ServerDAOHb() {
	}

	public Set<AdminDAO> getAdmins() {
		return new ListenerSet<AdminDAO>(admins) {
			@Override
			public void onAdd(AdminDAO entry) {
				entry.getServers().add(ServerDAOHb.this);
			}

			@Override
			public void onRemove(Object entry) {
				if (!(entry instanceof AdminDAOHb))
					throw new RuntimeException("Attempting to remove unknown object from server admin list " + entry);
				//Do nothing
			}
		};
	}

	public Set<ChannelDAO> getChannels() {
		return new ListenerSet<ChannelDAO>(channels) {
			@Override
			public void onAdd(ChannelDAO entry) {
				entry.setServer(ServerDAOHb.this);
			}

			@Override
			public void onRemove(Object entry) {
				if (!(entry instanceof ChannelDAO))
					throw new RuntimeException("Attempting to remove unknown object from server channel list " + entry);
				//Do nothing
			}
		};
	}

	@Override
	public boolean delete() {
		DAOControllerHb.getInstance().getSession().delete(this);
		return true;
	}
}
