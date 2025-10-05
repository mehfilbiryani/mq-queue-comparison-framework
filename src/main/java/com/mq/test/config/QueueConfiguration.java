package com.mq.test.config;

/**
 * Queue-specific configuration
 */
public class QueueConfiguration {
    private String queue1Name;
    private String queue2Name;
    private int maxMessages;
    private boolean browseMode;
    
    public QueueConfiguration(String queue1Name, String queue2Name, int maxMessages, boolean browseMode) {
        this.queue1Name = queue1Name;
        this.queue2Name = queue2Name;
        this.maxMessages = maxMessages;
        this.browseMode = browseMode;
    }
    
    public String getQueue1Name() {
        return queue1Name;
    }
    
    public String getQueue2Name() {
        return queue2Name;
    }
    
    public int getMaxMessages() {
        return maxMessages;
    }
    
    public boolean isBrowseMode() {
        return browseMode;
    }
}