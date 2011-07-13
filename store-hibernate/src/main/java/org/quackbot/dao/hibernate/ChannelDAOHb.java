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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
	@ManyToOne(cascade = CascadeType.ALL, targetEntity = ServerDAOHb.class)
	@JoinColumn(name = "SERVER_ID", nullable = false)
	private ServerDAO server;
	@ManyToMany(cascade = CascadeType.ALL, targetEntity = AdminDAOHb.class)
	@JoinTable(name = "quackbot_channel_admins", joinColumns = {
		@JoinColumn(name = "CHANNEL_ID")}, inverseJoinColumns = {
		@JoinColumn(name = "ADMIN_ID")})
	private Set<AdminDAO> admins = new HashSet();
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "CHANNEL_ID")
	private Set<UserChannelHb> userMaps = new HashSet();
	@PersistenceContext
	@Transient
	protected EntityManager em;

	public ChannelDAOHb() {
	}

	@Override
	public Set<UserDAO> getNormalUsers() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getUsers() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getOps() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getVoices() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getOwners() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getHalfOps() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	@Override
	public Set<UserDAO> getSuperOps() {
		return (Set<UserDAO>) (Object) new UserQuerySet() {
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

	protected abstract class UserQuerySet extends HashSet<UserDAOHb> {
		public UserQuerySet() {
			for (UserChannelHb userMap : userMaps)
				if (isSet(userMap))
					super.add(userMap.getUser());
		}

		/* Hook methods */
		public void onAdd(UserDAOHb entry) {
			//Update the existing object if the user already exists
			for (UserChannelHb userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, true);
					return;
				}

			//No existing map, create a new one
			UserChannelHb userMap = new UserChannelHb(ChannelDAOHb.this, entry);
			configure(userMap, true);
			userMaps.add(userMap);
		}

		public void onRemove(Object entry) {
			if (!(entry instanceof UserDAOHb))
				throw new RuntimeException("Attempting to remove unknown object " + entry);
			//Attempt to remove the existing object
			for (UserChannelHb userMap : userMaps)
				if (userMap.getUser().equals(entry)) {
					configure(userMap, true);
					return;
				}

			throw new RuntimeException("Can't remove user " + entry + " as they don't exist in this set");
		}

		public abstract boolean isSet(UserChannelHb userMap);

		public abstract void configure(UserChannelHb userMap, boolean set);

		@Override
		public boolean add(UserDAOHb e) {
			onAdd(e);
			return super.add(e);
		}

		@Override
		public boolean addAll(Collection<? extends UserDAOHb> c) {
			for (UserDAOHb curObj : c)
				onAdd(curObj);
			return super.addAll(c);
		}

		@Override
		public boolean remove(Object o) {
			onRemove(o);
			return super.remove(o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			for (Object curObj : c)
				onRemove(curObj);
			return super.removeAll(c);
		}

		@Override
		public Iterator<UserDAOHb> iterator() {
			//Wrap the iterator to capture remove operations
			final Iterator<UserDAOHb> queryItr = super.iterator();
			return new Iterator<UserDAOHb>() {
				UserDAOHb lastElement;

				public boolean hasNext() {
					return queryItr.hasNext();
				}

				public UserDAOHb next() {
					lastElement = queryItr.next();
					return lastElement;
				}

				public void remove() {
					onRemove(lastElement);
					queryItr.remove();
				}
			};
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	@Override
	public boolean delete() {
		em.remove(this);
		return true;
	}
}
