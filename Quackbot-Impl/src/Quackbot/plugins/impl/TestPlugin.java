/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.plugins.impl;

import Quackbot.Command;
import Quackbot.Controller;
import Quackbot.hook.Hook;
import java.util.List;
import org.pircbotx.DccChat;
import org.pircbotx.DccFileTransfer;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LordQuackstar
 */
public class TestPlugin extends Hook {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Override
	public void onAction(String sender, String login, String hostname, String target, String action) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onChannelInfo(String channel, int userCount, String topic) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onCommandFail(Exception e) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onCommandFinish() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onCommandGiven() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onCommandInvoke() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onCommandLoad(Command command) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onConnect() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onDisconnect() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onFileTransferFinished(DccFileTransfer transfer, Exception e) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onIncomingChatRequest(DccChat chat) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onIncomingFileTransfer(DccFileTransfer transfer) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onInit(Controller controller) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onJoin(String channel, String sender, String login, String hostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onNickChange(String oldNick, String login, String hostname, String newNick) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onPart(String channel, String sender, String login, String hostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onPluginLoadComplete() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onPluginLoadStart() throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onServerPing(String response) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onServerResponse(int code, String response) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onUnknown(String line) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onUserList(String channel, List<User> users) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
		log.trace("Running ineficcient");
	}

	@Override
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
		log.trace("Running ineficcient");
	}

}
