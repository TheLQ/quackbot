/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot;


import static com.google.common.base.Preconditions.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.pircbotx.Configuration;
import org.quackbot.dao.DAOFactory;
import org.quackbot.hooks.HookLoader;
import org.quackbot.hooks.loaders.JSHookLoader;

/**
 *
 * @author Leon
 */
@Data
public class QConfiguration {
	protected final Configuration templateConfiguration;
	protected final ImmutableList<String> adminLevels;
	protected final ImmutableList<String> globalPrefixes;
	protected final ImmutableBiMap<String, HookLoader> hookLoaderClasses;
	protected final DAOFactory daoFactory;

	public QConfiguration(Builder builder) {
		checkNotNull(builder, "Must specify builder");
		templateConfiguration = builder.getTemplateConfiguration();
		adminLevels = ImmutableList.copyOf(builder.getAdminLevels());
		globalPrefixes = ImmutableList.copyOf(builder.getGlobalPrefixes());
		hookLoaderClasses = ImmutableBiMap.copyOf(builder.getHookLoaderClasses());
		daoFactory = builder.getDaoFactory();
	}

	@Data
	public static class Builder {
		protected Configuration templateConfiguration = new Configuration.Builder()
				.setName("QuackbotUser")
				.setLogin("Quackbot")
				.setVersion("Quackbot Java IRC Framework 3.3 http://quackbot.googlecode.com/")
				.buildConfiguration();
		protected List<String> adminLevels = Lists.newArrayList(AdminLevels.ADMIN,
				AdminLevels.MODERATOR,
				AdminLevels.ANONYMOUS);
		protected List<String> globalPrefixes = new ArrayList();
		protected BiMap<String, HookLoader> hookLoaderClasses = HashBiMap.create();
		protected DAOFactory daoFactory;

		public Builder() {
			hookLoaderClasses.put("js", new JSHookLoader());
		}
		
		public void setAdminLevels(List<String> adminLevels) {
			checkArgument(adminLevels.contains(AdminLevels.ADMIN), "Passed admin levels must contain AdminLevels.ADMIN");
			checkArgument(adminLevels.contains(AdminLevels.ANONYMOUS), "Passed admin levels must contain AdminLevels.ANONYMOUS");
			this.adminLevels = adminLevels;
		}
	}
}
