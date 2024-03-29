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

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class PrefixNamingStrategy extends ImprovedNamingStrategy {
	protected String prefix;

	public PrefixNamingStrategy(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String tableName(String tableName) {
		return prefix + super.tableName(tableName);
	}
}