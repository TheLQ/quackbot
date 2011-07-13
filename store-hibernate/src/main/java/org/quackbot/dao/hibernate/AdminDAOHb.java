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
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(exclude = {"channels", "servers"})
@ToString(exclude = {"channels", "servers"})
@Entity
@Table(name = "quackbot_admins")
public class AdminDAOHb implements AdminDAO, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "ADMIN_ID", nullable = false)
	private Integer adminId;
	@Column(name = "name", length = 50, nullable = false)
	private String name;
	@ManyToMany(targetEntity = ChannelDAOHb.class, mappedBy = "admins")
	private Set<ChannelDAO> channels;
	@ManyToMany(targetEntity = ServerDAOHb.class, mappedBy = "admins")
	private Set<ServerDAO> servers;

	public AdminDAOHb() {
	}

	public boolean delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
