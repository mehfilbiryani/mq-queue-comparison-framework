package com.mq.test.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing an IBM MQ message
 */
public class MQMessage {
    private String messageId;
    private String correlationId;
    private String payload;
    private Map<String, Object> properties;
    private long timestamp;
    private int priority;
    private String format;
    
    public MQMessage() {
        this.properties = new HashMap<>();
    }
    
    public String getMessageId() { 
        return messageId; 
    }
    
    public void setMessageId(String messageId) { 
        this.messageId = messageId; 
    }
    
    public String getCorrelationId() { 
        return correlationId; 
    }
    
    public void setCorrelationId(String correlationId) { 
        this.correlationId = correlationId; 
    }
    
    public String getPayload() { 
        return payload; 
    }
    
    public void setPayload(String payload) { 
        this.payload = payload; 
    }
    
    public Map<String, Object> getProperties() { 
        return properties; 
    }
    
    public void setProperties(Map<String, Object> properties) { 
        this.properties = properties; 
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
    
    public int getPriority() { 
        return priority; 
    }
    
    public void setPriority(int priority) { 
        this.priority = priority; 
    }
    
    public String getFormat() { 
        return format; 
    }
    
    public void setFormat(String format) { 
        this.format = format; 
    }
}