/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.info;

/**
 *
 * @author Owner
 */
public class BotMessage {
    public String message;
    public String channel;
    public String user;
    public String rawMsg = null;

    public BotMessage(UserMessage usrMsg, String message) {
	this.message = message;
	this.channel = usrMsg.channel;
	this.user = usrMsg.sender;
    }

    public BotMessage(UserMessage usrMsg, Throwable t) {
	this.message = t.getMessage();
	this.channel = usrMsg.channel;
	this.user = usrMsg.sender;
    }

    public BotMessage(String channel, String user, String message) {
	this.message = message;
	this.channel = channel;
	this.user = user;
    }

    public BotMessage(String channel, String message) {
	this.message = message;
	this.channel = channel;
    }

    public BotMessage(String msg) {
	this.rawMsg = msg;
    }

    public String toString() {
	if(rawMsg != null)
	    return rawMsg;
	//Use StringBuilder just in case one of the values is null
	return new StringBuilder().append(user).append(": ").append(message).toString();
    }
}
