package com.flowable.integration.platform.config;

import java.util.Properties;

public class Configuration {

	private Properties properties = new Properties();

	private Configuration() {
		try {
			properties.load(getClass().getResourceAsStream("configuration.properties"));
		} catch (Exception e) {
			throw new RuntimeException("Unable to load configuration properties file");
		}
	}

	public Configuration(Properties properties) {
		this.properties = properties;
	}
	
	private static class SingletonHelper {
		private static final Configuration INSTANCE = new Configuration();
	}

	public static Configuration getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public String getPlatformUser() {
		return properties.getProperty("PLATFORM_USER");
	}

	public String getPlatformPassword() {
		return properties.getProperty("PLATFORM_PWD");
	}

	public String getPlatformURL() {
		return properties.getProperty("PLATFORM_PROTOCOL", "http") + "://" + properties.getProperty("PLATFORM_HOST")
				+ ":" + properties.getProperty("PLATFORM_PORT");
	}
	
	public String getPlatformURL(String organization) {
		return getPlatformURL()+"/home/"+organization;
	}
	
	
	public String getAuthenticationType() {
		return properties.getProperty("AUTH_TYPE", "PLATFORM");
	}

	
	public boolean enableProxy(){
		return Boolean.valueOf(properties.getProperty("ENABLE_PROXY","false"));
	}

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		System.out.println(configuration.getPlatformURL());
		System.out.println(configuration.getPlatformUser());
		System.out.println(configuration.getPlatformPassword());
	}
}
