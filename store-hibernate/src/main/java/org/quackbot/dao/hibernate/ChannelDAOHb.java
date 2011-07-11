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
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
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
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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

	public ChannelDAOHb() {
	}

	public void setServer(ServerDAO server) {
		this.server = (ServerDAOHb) server;
	}

	@Override
	public Set<AdminDAO> getAdmins() {
		return (Set<AdminDAO>) (Object) Collections.checkedSet(admins, AdminDAOHb.class);
	}

	@Override
	public Set<UserDAO> getNormalUsers() {
		final Session session = DAOControllerHb.getInstance().getSession();
		Criteria criteria = session.createCriteria(UserChannelHb.class);
		criteria.add(Restrictions.eq("channel", this));
		criteria.add(Restrictions.eq("op", false));
		criteria.add(Restrictions.eq("voice", false));
		criteria.add(Restrictions.eq("halfOp", false));
		criteria.add(Restrictions.eq("superOp", false));
		criteria.add(Restrictions.eq("owner", false));
		List list = criteria.list();
		return (Set<UserDAO>) (Object) new QuerySet<UserDAOHb>(list) {
			@Override
			public void onAdd(UserDAOHb entry) {
				//Just save the object with default state
				session.save(new UserChannelHb(ChannelDAOHb.this, entry));
			}

			@Override
			public void onRemove(Object entry) {
				if (entry instanceof UserDAOHb)
					throw new RuntimeException("Attempting to remove object that isn't a UserDAOHb from normal user status list: " + entry);
				//Normal user means no special status, so just delete the whole row
				Criteria criteria = session.createCriteria(UserChannelHb.class);
				criteria.add(Restrictions.eq("channel", this));
				criteria.add(Restrictions.eq("op", false));
				criteria.add(Restrictions.eq("voice", false));
				criteria.add(Restrictions.eq("halfOp", false));
				criteria.add(Restrictions.eq("superOp", false));
				criteria.add(Restrictions.eq("owner", false));
				criteria.add(Restrictions.eq("user", entry));
				Object result = criteria.uniqueResult();
				if (result == null)
					throw new RuntimeException("Can't find UserChannelHb mapping for user " + entry + " in channel " + ChannelDAOHb.this);
				session.delete(criteria.uniqueResult());
			}
		};
	}

	@Override
	public Set<UserDAO> getUsers() {
		return getByStatus(null);
	}

	@Override
	public Set<UserDAO> getOps() {
		return getByStatus("op");
	}

	@Override
	public Set<UserDAO> getVoices() {
		return getByStatus("voice");
	}

	@Override
	public Set<UserDAO> getOwners() {
		return getByStatus("owner");
	}

	@Override
	public Set<UserDAO> getHalfOps() {
		return getByStatus("halfOp");
	}

	@Override
	public Set<UserDAO> getSuperOps() {
		return getByStatus("superOp");
	}

	protected Set<UserDAO> getByStatus(final String status) {
		final Session session = DAOControllerHb.getInstance().getSession();
		Criteria criteria = session.createCriteria(UserChannelHb.class);
		criteria.add(Restrictions.eq("channel", this));
		if (status != null)
			criteria.add(Restrictions.eq(status, true));
		List list = criteria.list();
		return (Set<UserDAO>) (Object) new QuerySet<UserDAOHb>(list) {
			@Override
			public void onAdd(UserDAOHb entry) {
				UserChannelHb userChannelMap = new UserChannelHb(ChannelDAOHb.this, entry);
				try {
					//Since this is a generic class, hack through with reflection
					Field field = userChannelMap.getClass().getField(status);
					field.setAccessible(true);
					field.set(userChannelMap, true);
					session.save(userChannelMap);
				} catch (Exception e) {
					//Rethrow with RuntimeException causing something to fail
					throw new RuntimeException("Can't set " + status + " field in object " + userChannelMap, e);
				}
			}

			@Override
			public void onRemove(Object entry) {
				if (entry instanceof UserDAOHb)
					throw new RuntimeException("Attempting to remove object that isn't a UserDAOHb from " + status + " status list: " + entry);
				//Just delete the mapping
				Criteria criteria = session.createCriteria(UserChannelHb.class);
				criteria.add(Restrictions.eq("channel", this));
				if (status != null)
					criteria.add(Restrictions.eq(status, true));
				criteria.add(Restrictions.eq("user", entry));
				Object result = criteria.uniqueResult();
				if (result == null)
					throw new RuntimeException("Can't find UserChannelHb mapping for user " + entry + " in channel " + ChannelDAOHb.this);
				session.delete(criteria.uniqueResult());
			}
		};
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
