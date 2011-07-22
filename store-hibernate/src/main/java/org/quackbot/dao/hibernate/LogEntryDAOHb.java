package org.quackbot.dao.hibernate;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import org.quackbot.dao.LogEntryDAO;

/**
 *
 * @author lordquackstar
 */
@Data
@Entity(name = "quackbot_log")
public class LogEntryDAOHb implements LogEntryDAO, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "LOG_ID", nullable = false)
	protected Integer id;
	
	@Column(name = "timestamp", nullable = false)
	protected Integer timestamp;
	
	@Column(name = "server", nullable = false)
	protected String server;
	
	protected String channel;
	
	protected String type;
	
	protected String user;
	
	protected String message;
	
	protected String rawLine;
}
