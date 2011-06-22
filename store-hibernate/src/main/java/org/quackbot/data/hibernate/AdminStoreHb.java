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
import javax.persistence.ManyToMany;
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
@EqualsAndHashCode(exclude={"channels", "servers"})
@Entity
@Table(name = "quackbot_admin")
public class AdminStoreHb implements AdminStore {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "ADMIN_ID", nullable = false)
	private Integer adminId;
	
	@Column(name = "name", length = 50)
	private String name;
	
	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "admins")
	private Set<ChannelStoreHb> channels;
	
	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "admins")
	private Set<ServerStoreHb> servers;

	public AdminStoreHb() {
	}

	public AdminStoreHb(Integer adminId) {
		this.adminId = adminId;
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addChannel(ChannelStore channel) {
		channels.add((ChannelStoreHb) channel);
	}

	public void removeChannel(ChannelStore channel) {
		channels.remove((ChannelStoreHb) channel);
	}

	public void addServer(ServerStore server) {
		servers.add((ServerStoreHb) server);
	}

	public void removeServer(ServerStore server) {
		servers.remove((ServerStoreHb) server);
	}
}