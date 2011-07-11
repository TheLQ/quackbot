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
import java.util.Collections;
import java.util.Set;
import javax.persistence.Basic;
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
@EqualsAndHashCode(exclude = {"channels", "admins"})
@ToString(exclude = {"channels", "admins"})
@Entity
@Table(name = "quackbot_servers")
public class ServerDAOHb implements ServerDAO, Serializable {
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
	@OneToMany
	@JoinColumn(name="SERVER_ID")
	private Set<ChannelDAOHb> channels;
	@ManyToMany
	@JoinTable(name = "quackbot_adminmap", joinColumns = {
		@JoinColumn(name = "SERVER_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	private Set<AdminDAOHb> admins;

	public ServerDAOHb() {
	}

	public Set<ChannelDAO> getChannels() {
		return (Set<ChannelDAO>) (Object) Collections.checkedSet(channels, ChannelDAOHb.class);
	}

	public Set<AdminDAO> getAdmins() {
		return (Set<AdminDAO>) (Object) Collections.checkedSet(admins, AdminDAOHb.class);
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
