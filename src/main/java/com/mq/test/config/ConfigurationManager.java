package com.mq.test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages configuration loading from environment-specific property files
 */
public class ConfigurationManager {
    
    private static final String DEFAULT_ENVIRONMENT = "dev";
    private static final String CONFIG_PATH = "/config/";
    private static Properties properties;
    private static String currentEnvironment;
    
    /**
     * Load configuration for specified environment
     */
    public static void loadConfiguration(String environment) throws IOException {
        currentEnvironment = environment != null ? environment.toLowerCase() : DEFAULT_ENVIRONMENT;
        String configFile = CONFIG_PATH + currentEnvironment + ".properties";
        
        properties = new Properties();
        InputStream inputStream = ConfigurationManager.class.getResourceAsStream(configFile);
        
        if (inputStream == null) {
            throw new IOException(String.format(
                "Configuration file not found: %s. Available environments: dev, qa, uat, prod", 
                configFile));
        }
        
        try {
            properties.load(inputStream);
            System.out.println(String.format("✓ Loaded configuration for environment: %s", currentEnvironment));
        } finally {
            inputStream.close();
        }
    }
    
    /**
     * Load configuration based on system property or environment variable
     */
    public static void loadConfiguration() throws IOException {
        // Check system property first, then environment variable, then default
        String environment = System.getProperty("test.environment");
        if (environment == null) {
            environment = System.getenv("TEST_ENVIRONMENT");
        }
        if (environment == null) {
            environment = DEFAULT_ENVIRONMENT;
        }
        loadConfiguration(environment);
    }
    
    /**
     * Get MQ connection configuration for the current environment
     */
    public static MQConnectionConfig getMQConfig() {
        if (properties == null) {
            throw new IllegalStateException("Configuration not loaded. Call loadConfiguration() first.");
        }
        
        return new MQConnectionConfig(
            getProperty("mq.host"),
            getPropertyAsInt("mq.port", 1414),
            getProperty("mq.queue.manager"),
            getProperty("mq.channel"),
            getProperty("mq.username"),
            getProperty("mq.password")
        );
    }
    
    /**
     * Get queue configuration
     */
    public static QueueConfiguration getQueueConfig() {
        if (properties == null) {
            throw new IllegalStateException("Configuration not loaded. Call loadConfiguration() first.");
        }
        
        return new QueueConfiguration(
            getProperty("queue1.name"),
            getProperty("queue2.name"),
            getPropertyAsInt("queue.max.messages", 1000),
            getPropertyAsBoolean("queue.browse.mode", true)
        );
    }
    
    /**
     * Get test configuration
     */
    public static TestConfiguration getTestConfig() {
        if (properties == null) {
            throw new IllegalStateException("Configuration not loaded. Call loadConfiguration() first.");
        }
        
        return new TestConfiguration(
            getProperty("report.output.path", "target/ExtentReport.html"),
            getPropertyAsLong("timestamp.tolerance.ms", 5000L),
            getPropertyAsBoolean("console.logging.enabled", true),
            getPropertyAsBoolean("skip.on.setup.failure", true)
        );
    }
    
    /**
     * Get property value
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Required property '%s' not found in %s.properties", key, currentEnvironment));
        }
        return value;
    }
    
    /**
     * Get property with default value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get property as integer
     */
    public static int getPropertyAsInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println(String.format("Invalid integer value for '%s': %s. Using default: %d", 
                key, value, defaultValue));
            return defaultValue;
        }
    }
    
    /**
     * Get property as long
     */
    public static long getPropertyAsLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            System.err.println(String.format("Invalid long value for '%s': %s. Using default: %d", 
                key, value, defaultValue));
            return defaultValue;
        }
    }
    
    /**
     * Get property as boolean
     */
    public static boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get current environment name
     */
    public static String getCurrentEnvironment() {
        return currentEnvironment != null ? currentEnvironment : DEFAULT_ENVIRONMENT;
    }
    
    /**
     * Print all configuration properties (masks sensitive data)
     */
    public static void printConfiguration() {
        if (properties == null) {
            System.out.println("No configuration loaded.");
            return;
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println(String.format("║ Configuration: %-47s ║", currentEnvironment.toUpperCase()));
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        
        properties.stringPropertyNames().stream().sorted().forEach(key -> {
            String value = properties.getProperty(key);
            // Mask sensitive properties
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret")) {
                value = "********";
            }
            System.out.println(String.format("║ %-30s = %-30s ║", key, 
                value.length() > 30 ? value.substring(0, 27) + "..." : value));
        });
        
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }
}
