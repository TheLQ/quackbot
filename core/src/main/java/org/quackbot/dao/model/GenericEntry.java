package org.quackbot.dao.model;

import java.io.Serializable;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface GenericEntry<K extends Serializable> {
	public K getId();
}
