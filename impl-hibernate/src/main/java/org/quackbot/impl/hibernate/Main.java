package org.quackbot.impl.hibernate;

import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	public static void main(String[] args) {
		//First, make sure there's a quackbot.properties
		InputStream propertyStream = Main.class.getClassLoader().getResourceAsStream("quackbot.properties");
		if (propertyStream == null) {
			System.err.println("quackbot.properties not found in classpath!");
			return;
		}

		//Try to load it
		Properties properties;
		try {
			properties = new Properties();
			properties.load(propertyStream);
		} catch (Exception e) {
			System.err.println("Error when loading properties file");
			e.printStackTrace();
			return;
		}

		//Use default config file or user specified ones if they exist
		String[] configs = {"spring-impl.xml"};
		String configsProperty = properties.getProperty("spring.configs");
		if (StringUtils.isNotBlank(configsProperty)) {
			configs = new String[0];
			String[] rawConfigs = configsProperty.split(",");
			for (String curConfig : rawConfigs)
				ArrayUtils.add(configs, curConfig.trim());
		}
		
		//Load spring
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(configs);
		context.registerShutdownHook();
	}
}
