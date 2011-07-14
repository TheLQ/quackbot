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

import org.hibernate.Criteria;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author lordquackstar
 */
public class DeleteTest extends GenericHbTest {
	@Test
	public void deleteAdminGlobaleTest() {
		setupEnviornment();
		
		session.beginTransaction();
		//Grab the global admin and delete it
		Criteria query = session.createCriteria(AdminDAOHb.class);
		query.add(Restrictions.eq("name", "globalAdmin"));
		
		AdminDAOHb globalAdmin = (AdminDAOHb) query.uniqueResult();
		globalAdmin.delete();
		session.getTransaction().commit();
		
		session.beginTransaction();
		//Make sure its gone from server1
		ServerDAOHb server1 = (ServerDAOHb) session.createQuery("from ServerDAOHb WHERE SERVER_ID = 1").uniqueResult();
		assertEquals(server1.getAdmins().size(), 1, "Too many server1 admins: " + server1.getAdmins());
		assertEquals(server1.getAdmins().iterator().next().getName(), "serverAdmin1", "Remaining server1 admin name is wrong");
		
		//Make sure its gone from server2
		ServerDAOHb server2 = (ServerDAOHb) session.createQuery("from ServerDAOHb WHERE SERVER_ID = 2").uniqueResult();
		assertEquals(server2.getAdmins().size(), 1, "Too many server2 admins: " + server2.getAdmins());
		assertEquals(server2.getAdmins().iterator().next().getName(), "serverAdmin2", "Remaining server2 admin name is wrong");
	}
	
	protected void setupEnviornment() {
		session.beginTransaction();
		AdminDAOHb globalAdmin = generateAdmin("globalAdmin");
		session.save(generateEnviornment(1, globalAdmin));
		session.save(generateEnviornment(2, globalAdmin));
		session.getTransaction().commit();
	}
}
