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

		//Get any non-default configuration files
		String[] locations = {"spring-dao-hibernate.xml", "spring-impl.xml"};
		locations = getLocations(locations, properties.getProperty("spring.configs"));
		locations = getLocations(locations, properties.getProperty("spring.otherconfigs"));
		
		//Load spring
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(locations);
		context.registerShutdownHook();
	}
	
	protected static String[] getLocations(String[] locations, String property) {
		if (StringUtils.isNotBlank(property)) {
			String[] configs = property.split(",");
			for (String curConfig : configs)
				ArrayUtils.add(locations, curConfig.trim());
		}
		return locations;
	}
}
