package org.quackbot.dao;

import org.quackbot.dao.model.LogEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface LogDAO<T extends LogEntry> extends GenericDAO<T> {
}
