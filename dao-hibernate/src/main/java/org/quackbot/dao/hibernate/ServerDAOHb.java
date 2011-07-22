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
import java.util.HashSet;
import java.util.Iterator;
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
import org.hibernate.Session;
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
@Table(name = "servers")
public class ServerDAOHb implements ServerDAO, Serializable {
	private Integer serverId;
	private String address;
	private Integer port;
	private String password;
	private Set<ChannelDAO> channels = new ListenerSet<ChannelDAO>(new HashSet()) {
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
	private Set<AdminDAO> admins = new ListenerSet<AdminDAO>(new HashSet()) {
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

	public ServerDAOHb() {
	}

	@ManyToMany(cascade= CascadeType.ALL, targetEntity = AdminDAOHb.class)
	@JoinTable(name = "server_admins", joinColumns = {
		@JoinColumn(name = "SERVER_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	//@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<AdminDAO> getAdmins() {
		return admins;
	}

	@OneToMany(targetEntity = ChannelDAOHb.class, mappedBy = "server", orphanRemoval = true)
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<ChannelDAO> getChannels() {
		return channels;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "SERVER_ID", nullable = false)
	public Integer getServerId() {
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

	@Override
	public boolean delete() {
		Session session = DAOControllerHb.getInstance().getSession();
		for(ChannelDAO channel : channels)
			channel.setServer(this);
		for(AdminDAO admin : admins) {
			admin.getServers().remove(this);
		}
		session.delete(this);

		if (true)
			return true;

		//Delete all channels manually (clone the set so a ConcurrentModificationException isn't raised as channel deletes itself)
		//for(ChannelDAO channel : new HashSet<ChannelDAO>(channels))
		//	channel.delete();
		System.out.println("--- BEGIN DELETING USER MAPS ---");
		session.createQuery("delete from UserChannelHb where USER_ID in (from UserDAOHb where SERVER_ID = :server_id)").setInteger("server_id", serverId).executeUpdate();
		System.out.println("--- BEGIN DELETING USERS ---");
		session.createQuery("delete from UserDAOHb where SERVER_ID = :server_id").setInteger("server_id", serverId).executeUpdate();
		System.out.println("--- BEGIN DELETING CHANNELS ---");
		channels.clear();
		System.out.println("--- BEGIN DELETING ADMINS ---");


		//session.createQuery("delete from UserChannelHb where USER_ID in (from UserDAOHb where SERVER_ID = :server_id)").setInteger("server_id", serverId).executeUpdate();
		//session.refresh(this);
		//channels.clear();

		//Remove all admins
		//for(Iterator<AdminDAO> itr = admins.iterator(); itr.hasNext();) {
		//	AdminDAO curAdmin = itr.next();
		//	curAdmin.getServers().remove(this);
		//	itr.remove();
		//}
		admins.clear();

		/*
		for (Iterator<AdminDAO> itr = admins.iterator(); itr.hasNext();) {
		AdminDAO curAdmin = itr.next();
		curAdmin.getServers().remove(this);
		itr.remove();
		
		if(curAdmin.getName().equals("globalAdmin")) {
		
		}
		ClassMetadata metadata = session.getSessionFactory().getClassMetadata(AdminDAOHb.class);
		System.out.println("Identifyier for admin " + curAdmin.getName() + ": " + metadata.getIdentifier(curAdmin, (SessionImplementor) session));
		}*/
		System.out.println("--- BEGIN DELETING SERVER ---");
		DAOControllerHb.getInstance().getSession().delete(this);
		System.out.println("--- DELETE FINISHED ---");
		return true;
	}
}
