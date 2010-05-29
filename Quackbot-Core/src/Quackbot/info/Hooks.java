/*
 * @(#)Hooks.java,
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.info;

import Quackbot.Controller;
import Quackbot.PluginType;
import java.util.ArrayList;
import java.util.List;

/**
 * Enum of all avalible hook types. Hook types are named from PircBot methods
 *
 * @author Lord.Quackstar
 */
public enum Hooks {
	onAction,
	onChannelInfo,
	onConnect,
	onDccChatRequest,
	onDccSendRequest,
	onDeop,
	onDeVoice,
	onDisconnect,
	onFileTransferFinished,
	onFinger,
	onIncomingChatRequest,
	onIncomingFileTransfer,
	onInvite,
	onJoin,
	onKick,
	onMessage,
	onMode,
	onNickChange,
	onNotice,
	onOp,
	onPart,
	onPing,
	onPrivateMessage,
	onQuit,
	onRemoveChannelBan,
	onRemoveChannelKey,
	onRemoveChannelLimit,
	onRemoveInviteOnly,
	onRemoveModerated,
	onRemoveNoExternalMessages,
	onRemovePrivate,
	onRemoveSecret,
	onRemoveTopicProtection,
	onServerPing,
	onServerResponse,
	onSetChannelBan,
	onSetChannelKey,
	onSetChannelLimit,
	onSetInviteOnly,
	onSetModerated,
	onSetNoExternalMessages,
	onSetPrivate,
	onSetSecret,
	onSetTopicProtection,
	onTime,
	onTopic,
	onUnknown,
	onUserList,
	onUserMode,
	onVersion,
	onVoice;

	public static boolean hookExists(Hooks hook) {
		for (PluginType curPlugin : Controller.instance.plugins)
			if (curPlugin.getHook() != null)
				return true;
		return false;
	}

	public static List<PluginType> getHooks(Hooks matchHook) {
		List<PluginType> plugins = new ArrayList<PluginType>();
		for (PluginType curPlugin : Controller.instance.plugins) {
			Hooks curHook = curPlugin.getHook();
			if (curHook != null && curHook == matchHook)
				plugins.add(curPlugin);
		}
		return plugins;
	}
}
