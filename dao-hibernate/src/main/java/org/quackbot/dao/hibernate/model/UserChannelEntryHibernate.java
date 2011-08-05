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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quackbot.dao.model.GenericEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(of = {"user", "channel"})
@Entity
@Table(name = "usermap")
public class UserChannelEntryHibernate implements Serializable, GenericEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "ID", nullable = false)
	private Long id;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "USER_ID")
	protected UserEntryHibernate user;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "CHANNEL_ID")
	protected ChannelEntryHibernate channel;
	@Column(name = "op")
	protected boolean op;
	@Column(name = "voice")
	protected boolean voice;
	@Column(name = "halfOp")
	protected boolean halfOp;
	@Column(name = "superOp")
	protected boolean superOp;
	@Column(name = "owner")
	protected boolean owner;

	public UserChannelEntryHibernate() {
		//Blank constructor for Hibernate
	}

	public UserChannelEntryHibernate(ChannelEntryHibernate channel, UserEntryHibernate user) {
		this.channel = channel;
		this.user = user;
	}
}
