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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author lordquackstar
 */
@Data
@EqualsAndHashCode(exclude = {"user", "channel"})
@Table(name = "quackbot_usermap")
@Entity
public class UserChannelHb implements Serializable {
	@Id
	@GenericGenerator(name = "test", strategy = "seqhilo")
	@GeneratedValue(generator = "test")
	protected Long id;
	@ManyToOne(cascade = CascadeType.ALL)
	protected UserDAOHb user;
	@ManyToOne(cascade = CascadeType.ALL)
	protected ChannelDAOHb channel;
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

	public UserChannelHb() {
		//Blank constructor for Hibernate
	}

	public UserChannelHb(ChannelDAOHb channel, UserDAOHb user) {
		this.channel = channel;
		this.user = user;
	}
}
