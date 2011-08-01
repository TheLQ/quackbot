package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.LogEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface LogDAO<T extends LogEntry, I extends Serializable> extends GenericDAO<T, I> {
}
