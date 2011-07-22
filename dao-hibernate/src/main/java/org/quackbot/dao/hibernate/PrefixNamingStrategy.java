package org.quackbot.dao.hibernate;

import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;

/**
 *
 * @author lordquackstar
 */
public class PrefixNamingStrategy extends ImprovedNamingStrategy {
	protected String prefix;

	public PrefixNamingStrategy(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String classToTableName(String className) {
		return StringHelper.unqualify(className);
	}

	@Override
	public String propertyToColumnName(String propertyName) {
		return propertyName;
	}

	@Override
	public String tableName(String tableName) {
		return prefix + tableName;
	}

	@Override
	public String columnName(String columnName) {
		return columnName;
	}

	public String propertyToTableName(String className, String propertyName) {
		return prefix + classToTableName(className) + '_'
				+ propertyToColumnName(propertyName);
	}
}