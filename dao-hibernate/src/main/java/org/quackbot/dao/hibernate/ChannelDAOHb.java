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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.UserDAO;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(of = "name")
@ToString(exclude = {"admins"})
@Entity
@Table(name = "channels")
public class ChannelDAOHb implements ChannelDAO, Serializable {
	protected Integer channelID;
	protected String name;
	protected String password;
	protected String topic;
	protected Long createTimestamp;
	protected String topicSetter;
	protected Long topicTimestamp;
	protected String mode;
	private ServerDAO server;
	private Set<AdminDAO> admins = new ListenerSet<AdminDAO>(new HashSet()) {
		@Override
		public void onAdd(AdminDAO entry) {
			entry.getChannels().add(ChannelDAOHb.this);
		}

		@Override
		public void onRemove(Object entry) {
			if (!(entry instanceof AdminDAOHb))
				throw new RuntimeException("Attempting to remove unknown object from channel admin list " + entry);
			//Do nothing
		}
	};
	private Set<UserChannelHb> userMaps = new HashSet();

	public ChannelDAOHb() {
	}

	@ManyToMany(targetEntity = AdminDAOHb.class)
	@JoinTable(name = "channel_admins", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<AdminDAO> getAdmins() {
		return admins;
	}

	@Transient
	@Override
	public Set<UserDAO> getNormalUsers() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				//Make sure nothing is set
				return !userMap.isOp()
						&& !userMap.isVoice()
						&& !userMap.isHalfOp()
						&& !userMap.isSuperOp()
						&& !userMap.isOwner();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				//No configuration nessesary
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getUsers() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				//Get ALL users
				return true;
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				//No configuration nessesary
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getOps() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				return userMap.isOp();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				userMap.setOp(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getVoices() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				return userMap.isVoice();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				userMap.setVoice(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getOwners() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				return userMap.isOwner();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				userMap.setOwner(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getHalfOps() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				return userMap.isHalfOp();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				userMap.setHalfOp(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserDAO> getSuperOps() {
		return (Set<UserDAO>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelHb userMap) {
				return userMap.isSuperOp();
			}

			@Override
			public void configure(UserChannelHb userMap, boolean set) {
				userMap.setSuperOp(set);
			}
		};
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "CHANNEL_ID", nullable = false)
	public Integer getChannelID() {
		return channelID;
	}

	@Column(name = "name", length = 100, nullable = false)
	public String getName() {
		return name;
	}

	@Column(name = "password", length = 100)
	public String getPassword() {
		return password;
	}

	@Column(name = "topic", length = 100)
	public String getTopic() {
		return topic;
	}

	@Column(name = "createTimestamp", length = 20)
	public Long getCreateTimestamp() {
		return createTimestamp;
	}

	@Column(name = "topicSetter", length = 50)
	public String getTopicSetter() {
		return topicSetter;
	}

	@Column(name = "topicTimestamp", length = 100)
	public Long getTopicTimestamp() {
		return topicTimestamp;
	}

	@Column(name = "mode", length = 100)
	public String getMode() {
		return mode;
	}

	@ManyToOne(targetEntity = ServerDAOHb.class)
	@JoinColumn(name = "SERVER_ID", nullable = false, updatable = false)
	public ServerDAO getServer() {
		return server;
	}

	@OneToMany
	@JoinColumn(name = "CHANNEL_ID")
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<UserChannelHb> getUserMaps() {
		return userMaps;
	}

	@Override
	public boolean delete() {
		//Remove channel from server
		server.getChannels().remove(this);

		//Remove channel from admins
		for (Iterator<AdminDAO> itr = admins.iterator(); itr.hasNext();) {
			AdminDAO curAdmin = itr.next();
			curAdmin.getChannels().remove(this);
			itr.remove();
		}

		//Delete all usermaps for this channel
		userMaps.clear();

		//Finally, delete the channel
		DAOControllerHb.getInstance().getSession().delete(this);
		return true;
	}

	protected abstract class UserListenerSet extends ListenerSet<UserDAOHb> {
		public UserListenerSet() {
			super(new HashSet());
			for (UserChannelHb userMap : userMaps)
				if (isSet(userMap))
					delegateSet.add(userMap.getUser());
		}

		/* Hook methods */
		@Override
		public void onAdd(UserDAOHb entry) {
			if (entry == null)
				throw new NullPointerException("Adding null entry");

			//Update the existing object if the user already exists
			for (UserChannelHb userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, true);
					return;
				}

			//No existing map, create a new one
			UserChannelHb userMap = new UserChannelHb(ChannelDAOHb.this, entry);
			configure(userMap, true);
			if (server == null)
				throw new NullPointerException("Server in channel is null, can't pass to user");
			entry.setServer((ServerDAOHb) server);

			userMaps.add(userMap);
		}

		@Override
		public void onRemove(Object entry) {
			if (entry == null)
				throw new NullPointerException("Removing null entry");
			if (!(entry instanceof UserDAOHb))
				throw new RuntimeException("Attempting to remove unknown object " + entry);
			//Attempt to remove the existing object
			for (UserChannelHb userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, false);
					userMap.getUser().setServer(null);
					return;
				}

			throw new RuntimeException("Can't remove user " + entry + " as they don't exist in this set");
		}

		public abstract boolean isSet(UserChannelHb userMap);

		public abstract void configure(UserChannelHb userMap, boolean set);
	}
}
