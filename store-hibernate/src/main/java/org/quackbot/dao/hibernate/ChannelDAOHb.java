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

import java.util.Collections;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(exclude={"admins"})
@Entity
@Table(name = "quackbot_channel")
public class ChannelDAOHb implements ChannelDAO {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "CHANNEL_ID", nullable = false)
	private Integer channelID;
	
	@Column(name = "name", length = 100)
	private String name;
	
	@Column(name = "password", length = 100)
	private String password;
	
	@ManyToOne
	@JoinColumn (name="SERVER_ID")
	private ServerDAOHb server;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch= FetchType.EAGER)
	@JoinTable(name = "quackbot_admin_map", joinColumns = {
		@JoinColumn(name = "ADMIN_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "CHANNEL_ID")})
	private Set<AdminDAOHb> admins;

	public ChannelDAOHb() {
	}

	public ChannelDAOHb(Integer channelID) {
		this.channelID = channelID;
	}
	
	public Set<AdminDAO> getAdmins() {
		return (Set<AdminDAO>)(Object)Collections.checkedSet(admins, AdminDAOHb.class);
	}
	
	public void setServer(ServerDAO server) {
		this.server = (ServerDAOHb)server;
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
