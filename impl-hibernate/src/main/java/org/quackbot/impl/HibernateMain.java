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

import java.util.Properties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class HibernateMain {
	protected AbstractApplicationContext context;
	protected String[] configs;
	protected Properties properties;
	
	public void init() {
		//First, make sure there's a quackbot.properties
		Resource propertyResource = new PathMatchingResourcePatternResolver().getResource("classpath:quackbot.properties");
		if(!propertyResource.exists()){
			System.err.println("quackbot.properties not found in classpath!");
			return;
		}
		
		//Try to load it
		try {
			properties = new Properties();
			properties.load(propertyResource.getInputStream());
		} catch (Exception e) {
			System.err.println("Error when loading properties file");
			e.printStackTrace();
			return;
		}

		//Use default config file or user specified ones if they exist
		String configsProperty = properties.getProperty("spring.configs");
		if (StringUtils.isNotBlank(configsProperty)) {
			configs = configsProperty.split(",");
			for(int i = 0; i < configs.length; i++)
				configs[i] = configs[i].trim();
		} else
			configs = new String[]{"classpath:spring-impl.xml"};
		
		//Load spring
		context = new ClassPathXmlApplicationContext(configs);
		context.registerShutdownHook();
	}
	
	public static void main(String[] args) {
		new HibernateMain().init();
	}
}
