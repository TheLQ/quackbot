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
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.User;
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
public abstract class Hook implements BaseHook {
	private String name;
	private Logger log = LoggerFactory.getLogger(Hook.class);

	public Hook(String name) {
		this.name = name;
	}

	public Hook() {
	}

	@Override
	public Bot getBot() {
		return Bot.getPoolLocal();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Hook setup(String name) {
		this.name = name;
		return this;
	}

	@Override
	public void onAction(String sender, String login, String hostname, String target, String action) throws Exception {
	}

	@Override
	public void onChannelInfo(String channel, int userCount, String topic) throws Exception {
	}

	@Override
	public void onCommandFail(Exception e) throws Exception {
	}

	@Override
	public void onCommandFinish() throws Exception {
	}

	@Override
	public void onCommandGiven() throws Exception {
	}

	@Override
	public void onCommandInvoke() throws Exception {
	}

	@Override
	public void onCommandLoad(Command command) throws Exception {
	}

	@Override
	public void onConnect() throws Exception {
	}

	@Override
	public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	@Override
	public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	@Override
	public void onDisconnect() throws Exception {
	}

	@Override
	public void onFileTransferFinished(DccFileTransfer transfer, Exception e) throws Exception {
	}

	@Override
	public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	@Override
	public void onIncomingChatRequest(DccChat chat) throws Exception {
	}

	@Override
	public void onIncomingFileTransfer(DccFileTransfer transfer) throws Exception {
	}

	@Override
	public void onInit() throws Exception {
	}

	@Override
	public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) throws Exception {
	}

	@Override
	public void onJoin(String channel, String sender, String login, String hostname) throws Exception {
	}

	@Override
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) throws Exception {
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) throws Exception {
	}

	@Override
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
	}

	@Override
	public void onNickChange(String oldNick, String login, String hostname, String newNick) throws Exception {
	}

	@Override
	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) throws Exception {
	}

	@Override
	public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}

	@Override
	public void onPart(String channel, String sender, String login, String hostname) throws Exception {
	}

	@Override
	public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) throws Exception {
	}

	@Override
	public void onPluginLoadComplete() throws Exception {
	}

	@Override
	public void onPluginLoadStart() throws Exception {
	}

	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) throws Exception {
	}

	@Override
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) throws Exception {
	}

	@Override
	public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
	}

	@Override
	public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
	}

	@Override
	public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onServerPing(String response) throws Exception {
	}

	@Override
	public void onServerResponse(int code, String response) throws Exception {
	}

	@Override
	public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) throws Exception {
	}

	@Override
	public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) throws Exception {
	}

	@Override
	public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) throws Exception {
	}

	@Override
	public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) throws Exception {
	}

	@Override
	public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	@Override
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed) throws Exception {
	}

	@Override
	public void onUnknown(String line) throws Exception {
	}

	@Override
	public void onUserList(String channel, User[] users) throws Exception {
	}

	@Override
	public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) throws Exception {
	}

	@Override
	public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) throws Exception {
	}

	@Override
	public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) throws Exception {
	}
}
