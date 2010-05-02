/*
 * @(#)Hooks.java,
 *,
 * This file is part of Quackbot,
 */
package Quackbot.info;

import Quackbot.InstanceTracker;
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
		for (PluginType curPlugin : InstanceTracker.getController().plugins)
			if (curPlugin.getHook() != null)
				return true;
		return false;
	}

	public static List<PluginType> getHooks(Hooks matchHook) {
		List<PluginType> plugins = new ArrayList<PluginType>();
		for (PluginType curPlugin : InstanceTracker.getController().plugins) {
			Hooks curHook = curPlugin.getHook();
			if (curHook != null && curHook == matchHook)
				plugins.add(curPlugin);
		}
		return plugins;
	}
}
