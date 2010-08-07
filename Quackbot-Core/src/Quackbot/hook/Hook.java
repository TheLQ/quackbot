/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.hook;

import Quackbot.Bot;
import Quackbot.Command;
import java.util.List;
import org.pircbotx.DccChat;
import org.pircbotx.DccFileTransfer;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Hook interface is what all Hooks must implement to be added to the stack.
 *
 * This interface has one method, {@link #run(Quackbot.hook.HookList, Quackbot.Bot, Quackbot.info.BotEvent)  },
 * that is called during stack execution.
 *
 * See {@link HookManager} for an explanation on what a hook is and how hooks are
 * treated and executed.
 * @author LordQuackstar
 */
public abstract class Hook {
	private String name;
	private Logger log = LoggerFactory.getLogger(Hook.class);

	public Hook(String name) {
		this.name = name;
	}

	public Hook() {
	}

	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	public String getName() {
		return name;
	}

	public Hook setup(String name) {
		this.name = name;
		return this;
	}

	public void onAction(String sender, String login, String hostname, String target, String action) throws Exception {
	}

	public void onChannelInfo(String channel, int userCount, String topic) throws Exception {
	}

	public void onCommandFail(Exception e) throws Exception {
	}

	public void onCommandFinish() throws Exception {
	}

	public void onCommandGiven() throws Exception {
	}

	public void onCommandInvoke() throws Exception {
	}

	public void onCommandLoad(Command command) throws Exception {
	}

	public void onConnect() throws Exception {
	}

	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	public void onDisconnect() throws Exception {
	}

	public void onFileTransferFinished(DccFileTransfer transfer, Exception e) throws Exception {
	}

	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	public void onIncomingChatRequest(DccChat chat) throws Exception {
	}

	public void onIncomingFileTransfer(DccFileTransfer transfer) throws Exception {
	}

	public void onInit() throws Exception {
	}

	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) throws Exception {
	}

	public void onJoin(String channel, String sender, String login, String hostname) throws Exception {
	}

	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) throws Exception {
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message) throws Exception {
	}

	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
	}

	public void onNickChange(String oldNick, String login, String hostname, String newNick) throws Exception {
	}

	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) throws Exception {
	}

	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	public void onPart(String channel, String sender, String login, String hostname) throws Exception {
	}

	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) throws Exception {
	}

	public void onPluginLoadComplete() throws Exception {
	}

	public void onPluginLoadStart() throws Exception {
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) throws Exception {
	}

	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) throws Exception {
	}

	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
	}

	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
	}

	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onServerPing(String response) throws Exception {
	}

	public void onServerResponse(int code, String response) throws Exception {
	}

	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
	}

	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
	}

	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) throws Exception {
	}

	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) throws Exception {
	}

	public void onUnknown(String line) throws Exception {
	}

	public void onUserList(String channel, List<User> users) throws Exception {
	}

	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
	}

	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}
}
