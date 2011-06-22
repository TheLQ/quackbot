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
package org.quackbot.data.hibernate;

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
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.ServerStore;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(exclude={"channels", "admins"})
@Entity
@Table(name = "quackbot_server")
public class ServerStoreHb implements ServerStore {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "SERVER_ID", nullable = false)
	private Integer serverId;
	
	@Column(name = "address", length = 50)
	private String address;
	
	@Column(name = "port", length = 5)
	private Integer port;
	
	@Column(name = "password", length = 100)
	private String password;
	
	@OneToMany(mappedBy = "server")
	private Set<ChannelStoreHb> channels;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_admin_map", joinColumns = {
		@JoinColumn(name = "ADMIN_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "SERVER_ID")})
	private Set<AdminStoreHb> admins;

	public ServerStoreHb() {
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ServerStoreHb(Integer serverId) {
		this.serverId = serverId;
	}

	public void addAdmin(AdminStore admin) {
		admins.add((AdminStoreHb) admin);
	}

	public void removeAdmin(AdminStore admin) {
		admins.remove((AdminStoreHb) admin);
	}

	public void addChannel(ChannelStore channel) {
		channels.add((ChannelStoreHb) channel);
	}

	public void removeChannel(ChannelStore channel) {
		channels.remove((ChannelStoreHb) channel);
	}
}
