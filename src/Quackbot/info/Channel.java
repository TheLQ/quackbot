package Quackbot.info;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

/**
 *
 * @author admins
 */
@Node
public class Channel {
	@Field(path = true) private String path;
	@Field              private String name;

	/**
	 * Creates instance with specified channel
	 * @param channel
	 */
	public Channel(String channel) {
		this.name = channel;
	}

	/**
	 * Empty constructor
	 */
	public Channel() {
		super();
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
