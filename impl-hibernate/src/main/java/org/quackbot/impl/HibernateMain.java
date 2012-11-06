/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
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
package org.quackbot.impl;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public class HibernateMain {
	protected AbstractApplicationContext context;
	protected String[] configs;
	protected Properties properties;

	public void init(String[] args) throws Exception {
		//First, startup hibernate
		initHibernate();
		
		//Is the user running this for the first time?
		if (ArrayUtils.contains(args, "--firstrun"))
			firstRun();
		
		//Done!
		LoggerFactory.getLogger(getClass()).info("Done initing");
	}

	public void initHibernate() throws IOException {
		//First, make sure there's a quackbot.properties
		Resource propertyResource = new PathMatchingResourcePatternResolver().getResource("classpath:quackbot.properties");
		if (!propertyResource.exists()) {
			System.err.println("quackbot.properties not found in classpath!");
			return;
		}

		//Try to load it
		properties = new Properties();
		properties.load(propertyResource.getInputStream());

		//Use default config file or user specified ones if they exist
		String configsProperty = properties.getProperty("spring.configs");
		if (StringUtils.isNotBlank(configsProperty)) {
			configs = configsProperty.split(",");
			for (int i = 0; i < configs.length; i++)
				configs[i] = configs[i].trim();
		} else
			configs = new String[]{"classpath:spring-impl.xml"};

		//Load spring
		context = new ClassPathXmlApplicationContext(configs);
		context.registerShutdownHook();
	}

	public void firstRun() {
		LocalSessionFactoryBean session = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
		SchemaExport export = new SchemaExport(session.getConfiguration());
		export.drop(false, true);
		export.create(false, true);
	}

	public static void main(String[] args) {
		try {
			new HibernateMain().init(args);
		} catch (Exception ex) {
			throw new RuntimeException("Couldn't load bot", ex);
		}
	}
}
