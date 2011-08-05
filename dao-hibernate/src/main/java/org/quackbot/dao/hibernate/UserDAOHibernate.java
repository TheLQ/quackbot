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

import org.hibernate.criterion.Restrictions;
import org.quackbot.dao.UserDAO;
import org.quackbot.dao.hibernate.model.UserEntryHibernate;
import org.quackbot.dao.model.ServerEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Transactional(propagation = Propagation.MANDATORY)
@Repository
public class UserDAOHibernate extends GenericHbDAO<UserEntryHibernate> implements UserDAO<UserEntryHibernate> {
	public UserEntryHibernate findByNick(ServerEntry server, String userNick) {
		return (UserEntryHibernate) getSession().createCriteria(UserEntryHibernate.class).add(Restrictions.eq("server", server)).add(Restrictions.eq("nick", userNick)).uniqueResult();
	}

	public UserEntryHibernate create(String nick) {
		UserEntryHibernate user = new UserEntryHibernate();
		user.setNick(nick);
		return user;
	}
}
