package com.mq.test.config;

/**
 * Configuration class for IBM MQ connection parameters
 */
public class MQConnectionConfig {
    private String host;
    private int port;
    private String queueManager;
    private String channel;
    private String username;
    private String password;
    
    public MQConnectionConfig(String host, int port, String queueManager, 
                              String channel, String username, String password) {
        this.host = host;
        this.port = port;
        this.queueManager = queueManager;
        this.channel = channel;
        this.username = username;
        this.password = password;
    }
    
    public String getHost() { 
        return host; 
    }
    
    public int getPort() { 
        return port; 
    }
    
    public String getQueueManager() { 
        return queueManager; 
    }
    
    public String getChannel() { 
        return channel; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public String getPassword() { 
        return password; 
    }
}