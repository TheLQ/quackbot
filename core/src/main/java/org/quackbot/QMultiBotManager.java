/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.LinkedHashMap;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon
 */
public class QMultiBotManager extends MultiBotManager<Bot> {
	protected final LinkedHashMap<ServerEntry, Bot> configurations = new LinkedHashMap<ServerEntry, Bot>();

	public void addBot(ServerEntry serverEntry) {
		configurations.put(serverEntry, null);
		super.addBot(serverEntry.getConfiguration());
	}

	@Override
	@Deprecated
	public void addBot(Configuration config) {
		throw new RuntimeException("Must add by ServerEntry");
	}

	@Override
	@Deprecated
	public void addBot(Bot bot) {
		throw new RuntimeException("Must add by ServerEntry");
	}

	public void removeBot(ServerEntry serverEntry) {
		if(configurations.get(serverEntry).isConnected())
			throw new RuntimeException("Bot is still running");
		configurations.remove(serverEntry);
		
	}

	@Override
	protected ListenableFuture<Void> startBot(final Bot bot) {
		ListenableFuture<Void> future = super.startBot(bot);
		Futures.addCallback(future, new FutureCallback<Void>() {
			public void onSuccess(Void result) {
				remove();
			}

			public void onFailure(Throwable t) {
				remove();
			}

			protected void remove() {
				configurations.put(bot.getServerEntry(), null);
			}
		});
		return future;
	}
}
