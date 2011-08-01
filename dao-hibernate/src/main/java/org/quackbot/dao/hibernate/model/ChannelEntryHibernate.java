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
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;
import org.quackbot.dao.model.UserEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(of = "name")
@ToString(exclude = {"admins", "userMaps"})
@Entity
@Table(name = "channels")
public class ChannelEntryHibernate implements ChannelEntry, Serializable {
	protected Long id;
	protected String name;
	protected String password;
	protected String topic;
	protected Long createTimestamp;
	protected String topicSetter;
	protected Long topicTimestamp;
	protected String mode;
	private ServerEntry server;
	private Set<AdminEntry> admins = new ListenerSet<AdminEntry>(new HashSet()) {
		@Override
		public void onAdd(AdminEntry entry) {
			entry.getChannels().add(ChannelEntryHibernate.this);
		}

		@Override
		public void onRemove(Object entry) {
			if (!(entry instanceof AdminEntryHibernate))
				throw new RuntimeException("Attempting to remove unknown object from channel admin list " + entry);
			//Do nothing
		}
	};
	private Set<UserChannelEntryHibernate> userMaps = new HashSet();

	public ChannelEntryHibernate() {
	}

	@ManyToMany(targetEntity = AdminEntryHibernate.class)
	@JoinTable(name = "channel_admins", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<AdminEntry> getAdmins() {
		return admins;
	}

	@Transient
	@Override
	public Set<UserEntry> getNormalUsers() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				//Make sure nothing is set
				return !userMap.isOp()
						&& !userMap.isVoice()
						&& !userMap.isHalfOp()
						&& !userMap.isSuperOp()
						&& !userMap.isOwner();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				//No configuration nessesary
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getUsers() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				//Get ALL users
				return true;
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				//No configuration nessesary
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getOps() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				return userMap.isOp();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				userMap.setOp(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getVoices() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				return userMap.isVoice();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				userMap.setVoice(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getOwners() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				return userMap.isOwner();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				userMap.setOwner(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getHalfOps() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				return userMap.isHalfOp();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				userMap.setHalfOp(set);
			}
		};
	}

	@Transient
	@Override
	public Set<UserEntry> getSuperOps() {
		return (Set<UserEntry>) (Object) new UserListenerSet() {
			@Override
			public boolean isSet(UserChannelEntryHibernate userMap) {
				return userMap.isSuperOp();
			}

			@Override
			public void configure(UserChannelEntryHibernate userMap, boolean set) {
				userMap.setSuperOp(set);
			}
		};
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "CHANNEL_ID", nullable = false)
	public Long getId() {
		return id;
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

	@ManyToOne(targetEntity = ServerEntryHibernate.class)
	@JoinColumn(name = "SERVER_ID", nullable = false, updatable = false)
	public ServerEntry getServer() {
		return server;
	}

	@OneToMany
	@JoinColumn(name = "CHANNEL_ID")
	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE})
	public Set<UserChannelEntryHibernate> getUserMaps() {
		return userMaps;
	}

	protected abstract class UserListenerSet extends ListenerSet<UserEntryHibernate> {
		public UserListenerSet() {
			super(new HashSet());
			for (UserChannelEntryHibernate userMap : userMaps)
				if (isSet(userMap))
					delegateSet.add(userMap.getUser());
		}

		/* Hook methods */
		@Override
		public void onAdd(UserEntryHibernate entry) {
			if (entry == null)
				throw new NullPointerException("Adding null entry");

			//Update the existing object if the user already exists
			for (UserChannelEntryHibernate userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, true);
					return;
				}

			//No existing map, create a new one
			UserChannelEntryHibernate userMap = new UserChannelEntryHibernate(ChannelEntryHibernate.this, entry);
			configure(userMap, true);
			if (server == null)
				throw new NullPointerException("Server in channel is null, can't pass to user");
			entry.setServer((ServerEntryHibernate) server);

			userMaps.add(userMap);
		}

		@Override
		public void onRemove(Object entry) {
			if (entry == null)
				throw new NullPointerException("Removing null entry");
			if (!(entry instanceof UserEntryHibernate))
				throw new RuntimeException("Attempting to remove unknown object " + entry);
			//Attempt to remove the existing object
			for (UserChannelEntryHibernate userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, false);
					userMap.getUser().setServer(null);
					return;
				}

			throw new RuntimeException("Can't remove user " + entry + " as they don't exist in this set");
		}

		public abstract boolean isSet(UserChannelEntryHibernate userMap);

		public abstract void configure(UserChannelEntryHibernate userMap, boolean set);
	}
}
