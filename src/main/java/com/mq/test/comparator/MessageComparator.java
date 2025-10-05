package com.mq.test.comparator;

import com.mq.test.model.ComparisonResult;
import com.mq.test.model.MQMessage;
import java.util.List;
import java.util.Map;

/**
 * Comparator class containing all message comparison methods
 */
public class MessageComparator {
    
    public static ComparisonResult compareMessageCount(List<MQMessage> queue1, List<MQMessage> queue2) {
        boolean passed = queue1.size() == queue2.size();
        String msg = String.format("Queue1: %d messages, Queue2: %d messages", queue1.size(), queue2.size());
        ComparisonResult result = new ComparisonResult(passed, msg);
        if (!passed) {
            result.addDifference("Message count mismatch");
        }
        return result;
    }
    
    public static ComparisonResult comparePayloads(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Payload comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            String payload1 = queue1.get(i).getPayload();
            String payload2 = queue2.get(i).getPayload();
            if (!payload1.equals(payload2)) {
                result = new ComparisonResult(false, "Payload mismatch found");
                result.addDifference(String.format("Message %d - Queue1: %s, Queue2: %s", i, truncate(payload1), truncate(payload2)));
            }
        }
        return result;
    }
    
    public static ComparisonResult compareMessageIds(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Message ID comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            String id1 = queue1.get(i).getMessageId();
            String id2 = queue2.get(i).getMessageId();
            if (!id1.equals(id2)) {
                result = new ComparisonResult(false, "Message ID mismatch found");
                result.addDifference(String.format("Message %d - Queue1 ID: %s, Queue2 ID: %s", i, id1, id2));
            }
        }
        return result;
    }
    
    public static ComparisonResult compareCorrelationIds(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Correlation ID comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            String corr1 = queue1.get(i).getCorrelationId();
            String corr2 = queue2.get(i).getCorrelationId();
            if (!corr1.equals(corr2)) {
                result = new ComparisonResult(false, "Correlation ID mismatch found");
                result.addDifference(String.format("Message %d - Queue1: %s, Queue2: %s", i, corr1, corr2));
            }
        }
        return result;
    }
    
    public static ComparisonResult comparePriorities(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Priority comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            int priority1 = queue1.get(i).getPriority();
            int priority2 = queue2.get(i).getPriority();
            if (priority1 != priority2) {
                result = new ComparisonResult(false, "Priority mismatch found");
                result.addDifference(String.format("Message %d - Queue1: %d, Queue2: %d", i, priority1, priority2));
            }
        }
        return result;
    }
    
    public static ComparisonResult compareOrdering(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Message ordering comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            long ts1 = queue1.get(i).getTimestamp();
            long ts2 = queue2.get(i).getTimestamp();
            if (i > 0) {
                long prevTs1 = queue1.get(i-1).getTimestamp();
                long prevTs2 = queue2.get(i-1).getTimestamp();
                boolean order1 = ts1 >= prevTs1;
                boolean order2 = ts2 >= prevTs2;
                if (order1 != order2) {
                    result = new ComparisonResult(false, "Message ordering differs");
                    result.addDifference(String.format("Ordering differs at position %d", i));
                }
            }
        }
        return result;
    }
    
    public static ComparisonResult compareFormats(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Message format comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            String format1 = queue1.get(i).getFormat();
            String format2 = queue2.get(i).getFormat();
            if (!format1.equals(format2)) {
                result = new ComparisonResult(false, "Format mismatch found");
                result.addDifference(String.format("Message %d - Queue1: %s, Queue2: %s", i, format1, format2));
            }
        }
        return result;
    }
    
    public static ComparisonResult compareTimestamps(List<MQMessage> queue1, List<MQMessage> queue2, long toleranceMs) {
        ComparisonResult result = new ComparisonResult(true, String.format("Timestamp comparison (tolerance: %dms)", toleranceMs));
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            long ts1 = queue1.get(i).getTimestamp();
            long ts2 = queue2.get(i).getTimestamp();
            long diff = Math.abs(ts1 - ts2);
            if (diff > toleranceMs) {
                result = new ComparisonResult(false, "Timestamp difference exceeds tolerance");
                result.addDifference(String.format("Message %d - Difference: %dms", i, diff));
            }
        }
        return result;
    }
    
    public static ComparisonResult compareMessageProperties(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Message properties comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            Map<String, Object> props1 = queue1.get(i).getProperties();
            Map<String, Object> props2 = queue2.get(i).getProperties();
            if (props1.size() != props2.size()) {
                result = new ComparisonResult(false, "Property count mismatch");
                result.addDifference(String.format("Message %d - Queue1: %d properties, Queue2: %d properties", i, props1.size(), props2.size()));
                continue;
            }
            for (String key : props1.keySet()) {
                if (!props2.containsKey(key)) {
                    result = new ComparisonResult(false, "Property key mismatch");
                    result.addDifference(String.format("Message %d - Property '%s' missing in Queue2", i, key));
                } else if (!props1.get(key).equals(props2.get(key))) {
                    result = new ComparisonResult(false, "Property value mismatch");
                    result.addDifference(String.format("Message %d - Property '%s': Queue1=%s, Queue2=%s", i, key, props1.get(key), props2.get(key)));
                }
            }
        }
        return result;
    }
    
    public static ComparisonResult comparePayloadLength(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Payload length comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            int len1 = queue1.get(i).getPayload().length();
            int len2 = queue2.get(i).getPayload().length();
            if (len1 != len2) {
                result = new ComparisonResult(false, "Payload length mismatch");
                result.addDifference(String.format("Message %d - Queue1: %d bytes, Queue2: %d bytes", i, len1, len2));
            }
        }
        return result;
    }
    
    public static ComparisonResult comparePayloadStructure(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Payload structure comparison (JSON/XML)");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            String payload1 = queue1.get(i).getPayload().trim();
            String payload2 = queue2.get(i).getPayload().trim();
            boolean isJson1 = payload1.startsWith("{") || payload1.startsWith("[");
            boolean isJson2 = payload2.startsWith("{") || payload2.startsWith("[");
            boolean isXml1 = payload1.startsWith("<");
            boolean isXml2 = payload2.startsWith("<");
            if (isJson1 != isJson2 || isXml1 != isXml2) {
                result = new ComparisonResult(false, "Payload structure type mismatch");
                result.addDifference(String.format("Message %d - Different payload formats detected", i));
            }
        }
        return result;
    }
    
    public static ComparisonResult findDuplicateMessages(List<MQMessage> messages, String queueName) {
        ComparisonResult result = new ComparisonResult(true, String.format("Duplicate message check for %s", queueName));
        for (int i = 0; i < messages.size(); i++) {
            for (int j = i + 1; j < messages.size(); j++) {
                if (messages.get(i).getPayload().equals(messages.get(j).getPayload())) {
                    result = new ComparisonResult(false, "Duplicate messages found");
                    result.addDifference(String.format("Messages at positions %d and %d are duplicates", i, j));
                }
            }
        }
        return result;
    }
    
    public static ComparisonResult checkMessageSequence(List<MQMessage> messages, String queueName) {
        ComparisonResult result = new ComparisonResult(true, String.format("Message sequence check for %s", queueName));
        for (int i = 1; i < messages.size(); i++) {
            long prevTs = messages.get(i-1).getTimestamp();
            long currTs = messages.get(i).getTimestamp();
            if (currTs < prevTs) {
                result = new ComparisonResult(false, "Message sequence violation");
                result.addDifference(String.format("Message at position %d is out of sequence", i));
            }
        }
        return result;
    }
    
    public static ComparisonResult comparePayloadChecksum(List<MQMessage> queue1, List<MQMessage> queue2) {
        ComparisonResult result = new ComparisonResult(true, "Payload checksum comparison");
        int minSize = Math.min(queue1.size(), queue2.size());
        for (int i = 0; i < minSize; i++) {
            int checksum1 = queue1.get(i).getPayload().hashCode();
            int checksum2 = queue2.get(i).getPayload().hashCode();
            if (checksum1 != checksum2) {
                result = new ComparisonResult(false, "Checksum mismatch");
                result.addDifference(String.format("Message %d - Checksums differ", i));
            }
        }
        return result;
    }
    
    private static String truncate(String str) {
        return str.length() > 50 ? str.substring(0, 50) + "..." : str;
    }
}