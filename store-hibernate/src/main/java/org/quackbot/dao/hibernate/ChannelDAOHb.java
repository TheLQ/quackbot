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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.annotations.WhereJoinTable;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.UserDAO;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(exclude = {"admins"})
@ToString(exclude = {"admins"})
@Entity
@Table(name = "quackbot_channels")
public class ChannelDAOHb implements ChannelDAO, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "CHANNEL_ID", nullable = false)
	protected Integer channelID;
	@Column(name = "name", length = 100, nullable = false)
	protected String name;
	@Column(name = "password", length = 100)
	protected String password;
	@Column(name = "topic", length = 100)
	protected String topic;
	@Column(name = "createTimestamp", length = 20)
	protected Long createTimestamp;
	@Column(name = "topicSetter", length = 50)
	protected String topicSetter;
	@Column(name = "topicTimestamp", length = 100)
	protected Long topicTimestamp;
	@Column(name = "mode", length = 100)
	protected String mode;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "SERVER_ID", insertable = false)
	private ServerDAOHb server;
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_adminmap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	private Set<AdminDAOHb> admins;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	protected Set<UserDAOHb> users = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	@FilterJoinTable(name="ops_true", condition=":ops = 1")
	protected Set<UserDAOHb> ops = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	@FilterJoinTable(name="voices_true", condition=":voices = 1")
	protected Set<UserDAOHb> voices = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	@FilterJoinTable(name="halfOps_true", condition=":halfOps = 1")
	protected Set<UserDAOHb> halfOps = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	@FilterJoinTable(name="superOps_true", condition=":superOps = 1")
	protected Set<UserDAOHb> superOps = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "quackbot_usermap", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID")})
	 @FilterJoinTable(name="owner_true", condition=":owner = 1")
	protected Set<UserDAOHb> owners = new HashSet();

	public ChannelDAOHb() {
	}

	public void setServer(ServerDAO server) {
		this.server = (ServerDAOHb) server;
	}

	@Override
	public Set<AdminDAO> getAdmins() {
		return (Set<AdminDAO>) (Object) Collections.checkedSet(admins, AdminDAOHb.class);
	}

	public Set<UserDAO> getNormalUsers() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public Set<UserDAO> getUsers() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(users, UserDAOHb.class);
	}

	@Override
	public Set<UserDAO> getOps() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(ops, UserDAOHb.class);
	}

	@Override
	public Set<UserDAO> getVoices() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(voices, UserDAOHb.class);
	}

	@Override
	public Set<UserDAO> getOwners() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(owners, UserDAOHb.class);
	}

	@Override
	public Set<UserDAO> getHalfOps() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(halfOps, UserDAOHb.class);
	}

	@Override
	public Set<UserDAO> getSuperOps() {
		return (Set<UserDAO>) (Object) Collections.checkedSet(superOps, UserDAOHb.class);
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
