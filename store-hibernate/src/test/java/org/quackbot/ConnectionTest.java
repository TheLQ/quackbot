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

package org.quackbot;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.quackbot.data.hibernate.AdminStoreHb;
import org.quackbot.data.hibernate.ChannelStoreHb;
import org.quackbot.data.hibernate.ServerStoreHb;

/**
 *
 * @author lordquackstar
 */
public class ConnectionTest {
	public static void main(String[] args) {
		Session session = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .buildSessionFactory().openSession();
		
		System.out.println("TRANSACTION BEFORE:" + session.getTransaction().isActive());
		
		session.beginTransaction();
		System.out.println("TRANSACTION AFTER" + session.getTransaction().isActive());
		
		List result = session.createQuery( "from ServerStoreHb" ).list();
		
		for(ServerStoreHb server : (List<ServerStoreHb>)result) {
			System.out.println("Server: " + server.getAddress());
			for(AdminStoreHb admin : server.getAdmins())
				System.out.println("-Admin: " + admin.getName());
			for(ChannelStoreHb channel : server.getChannels()) {
				System.out.println("-Channel: " + channel.getName());
				for(AdminStoreHb admin : channel.getAdmins())
					System.out.println("--Admin: " + admin.getName());
			}
		}
		
		session.getTransaction().commit();
		session.close();
	}
}
