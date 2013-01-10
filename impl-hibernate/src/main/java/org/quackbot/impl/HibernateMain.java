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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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
		
		//Start prompting the user for info
		System.out.println();
		System.out.println("--------------------------");
		System.out.println("- Quackbot Initial Setup -");
		System.out.println("--------------------------");
		System.out.println();
		
		
		if(promptForInput("Do you wish to setup initial servers now? (Y/N)", false, "Y", "y", "N", "n").equalsIgnoreCase("N")) {
			System.out.println("Skipping setup. Note: You will have to setup servers manually");
			return;
		}
		
		while(true) {
			String server = promptForInput("What is the address of the server?", false);
			String portString = promptForInput("What is the server port? [Default: 6667]", true);
			String password = promptForInput("What is the server password? [Default: none]", true);
			boolean ssl = promptForInput("Does the server use SSL? [Default: no] (Y/N)", false, "Y", "y", "N", "n").equalsIgnoreCase("Y");
			
			//Store
			System.out.println("Storing server");
			int port = Integer.parseInt(StringUtils.defaultIfBlank(portString, "6667"));
			
			//TODO
		}
	}
	
	protected static String promptForInput(String prompt, boolean blankOk, String... allowedValuesArray) {
		Scanner in = new Scanner(System.in);
		List<String> allowedValues = Arrays.asList(allowedValuesArray);
		while(true) {
			System.out.print(prompt + " ");
			String inputString = in.nextLine();
			
			//If were given allowedValues make sure the input string is in there
			if(!allowedValues.isEmpty() && !allowedValues.contains(inputString)) {
				System.out.println("Unknown value " + inputString);
				continue;
			} else if(!blankOk && StringUtils.isBlank(inputString)) {
				System.out.println("Answer cannot be blank");
				continue;
			} else
				//Were good
				return inputString;
		}
	}
	
	protected static boolean convertInputToBool(String input) {
		return input.equalsIgnoreCase("Y");
	}

	public static void main(String[] args) {
		try {
			new HibernateMain().init(args);
		} catch (Exception ex) {
			throw new RuntimeException("Couldn't load bot", ex);
		}
	}
}
