import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * High-performance IBM MQ Message Comparator
 * Compares messages from legacy mainframe and new Java-based systems
 */
public class MQMessageComparator {
    
    // Configuration
    private static final String QUEUE_MANAGER = "QM1";
    private static final String LEGACY_OUT_QUEUE = "LEGACY.OUT.QUEUE";
    private static final String NEW_OUT_QUEUE = "NEW.OUT.QUEUE";
    private static final String INPUT_QUEUE = "INPUT.QUEUE";
    private static final String CHANNEL = "SYSTEM.DEF.SVRCONN";
    private static final String HOST = "localhost";
    private static final int PORT = 1414;
    
    // Timeout configurations
    private static final long MESSAGE_WAIT_TIMEOUT_MS = 30000; // 30 seconds
    private static final long PAIRING_TIMEOUT_MS = 60000; // 60 seconds
    private static final long REPORT_INTERVAL_MS = 10000; // 10 seconds
    
    // Concurrent data structures
    private final ConcurrentHashMap<String, MessagePair> messagePairs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> messageTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<ComparisonResult> results = new ConcurrentLinkedQueue<>();
    
    // Statistics
    private final AtomicInteger totalMessages = new AtomicInteger(0);
    private final AtomicInteger matchedMessages = new AtomicInteger(0);
    private final AtomicInteger mismatchedMessages = new AtomicInteger(0);
    private final AtomicInteger orphanedLegacy = new AtomicInteger(0);
    private final AtomicInteger orphanedNew = new AtomicInteger(0);
    private final AtomicInteger duplicatesDetected = new AtomicInteger(0);
    
    private volatile boolean running = true;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public static void main(String[] args) {
        MQMessageComparator comparator = new MQMessageComparator();
        comparator.start();
    }
    
    public void start() {
        System.out.println("Starting MQ Message Comparator...");
        
        // Start message listeners
        executor.submit(() -> listenToQueue(LEGACY_OUT_QUEUE, MessageSource.LEGACY));
        executor.submit(() -> listenToQueue(NEW_OUT_QUEUE, MessageSource.NEW));
        
        // Start timeout checker
        scheduler.scheduleAtFixedRate(this::checkTimeouts, 5, 5, TimeUnit.SECONDS);
        
        // Start HTML report generator
        scheduler.scheduleAtFixedRate(this::generateHTMLReport, 0, REPORT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            running = false;
            executor.shutdown();
            scheduler.shutdown();
            generateFinalReport();
        }));
    }
    
    private void listenToQueue(String queueName, MessageSource source) {
        MQQueueManager qMgr = null;
        MQQueue queue = null;
        
        try {
            // Connect to MQ
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(MQConstants.HOST_NAME_PROPERTY, HOST);
            props.put(MQConstants.PORT_PROPERTY, PORT);
            props.put(MQConstants.CHANNEL_PROPERTY, CHANNEL);
            
            qMgr = new MQQueueManager(QUEUE_MANAGER, props);
            
            int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_FAIL_IF_QUIESCING;
            queue = qMgr.accessQueue(queueName, openOptions);
            
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_FAIL_IF_QUIESCING;
            gmo.waitInterval = 5000; // 5 seconds
            
            System.out.println(source + " listener started on queue: " + queueName);
            
            while (running) {
                try {
                    MQMessage message = new MQMessage();
                    queue.get(message, gmo);
                    
                    byte[] msgData = new byte[message.getMessageLength()];
                    message.readFully(msgData);
                    String msgContent = new String(msgData, "UTF-8");
                    
                    // Extract correlation ID or message ID
                    String correlationId = message.correlationId != null ? 
                        bytesToHex(message.correlationId).trim() : 
                        bytesToHex(message.messageId).trim();
                    
                    // Parse global identifier from header
                    String globalId = parseGlobalIdentifier(msgContent);
                    
                    processMessage(globalId, correlationId, msgContent, source);
                    
                } catch (MQException mqe) {
                    if (mqe.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        // No message available, continue
                        Thread.sleep(100);
                    } else {
                        System.err.println(source + " MQ Error: " + mqe.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println(source + " Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (queue != null) queue.close();
                if (qMgr != null) qMgr.disconnect();
            } catch (MQException e) {
                System.err.println("Error closing MQ resources: " + e.getMessage());
            }
        }
    }
    
    private String parseGlobalIdentifier(String message) {
        // Extract global identifier from message header
        // Format: PROGRAM(7) + MMDDYYYY(8) + HHMMSS(6) + MSGNUM(3) = 24 chars
        if (message.length() >= 24) {
            return message.substring(0, 24);
        }
        return null;
    }
    
    private void processMessage(String globalId, String correlationId, String content, MessageSource source) {
        if (globalId == null) {
            System.err.println("Invalid message - no global identifier found");
            return;
        }
        
        totalMessages.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        
        // Check for duplicates
        String key = globalId + "_" + source;
        if (messageTimestamps.containsKey(key)) {
            duplicatesDetected.incrementAndGet();
            System.out.println("DUPLICATE detected: " + globalId + " from " + source);
        }
        messageTimestamps.put(key, timestamp);
        
        // Get or create message pair
        MessagePair pair = messagePairs.computeIfAbsent(globalId, k -> new MessagePair(globalId));
        
        synchronized (pair) {
            if (source == MessageSource.LEGACY) {
                if (pair.legacyMessage != null) {
                    duplicatesDetected.incrementAndGet();
                    System.out.println("DUPLICATE LEGACY: " + globalId);
                }
                pair.legacyMessage = content;
                pair.legacyTimestamp = timestamp;
            } else {
                if (pair.newMessage != null) {
                    duplicatesDetected.incrementAndGet();
                    System.out.println("DUPLICATE NEW: " + globalId);
                }
                pair.newMessage = content;
                pair.newTimestamp = timestamp;
            }
            
            // If both messages received, compare immediately
            if (pair.isComplete()) {
                compareAndRecord(pair);
                messagePairs.remove(globalId);
            }
        }
    }
    
    private void compareAndRecord(MessagePair pair) {
        ComparisonResult result = new ComparisonResult();
        result.globalId = pair.globalId;
        result.timestamp = LocalDateTime.now();
        result.legacyMessage = pair.legacyMessage;
        result.newMessage = pair.newMessage;
        result.legacyTimestamp = pair.legacyTimestamp;
        result.newTimestamp = pair.newTimestamp;
        result.latencyMs = Math.abs(pair.newTimestamp - pair.legacyTimestamp);
        
        // Parse identifier components
        result.programName = pair.globalId.substring(0, 7);
        result.dateStr = pair.globalId.substring(7, 15);
        result.timeStr = pair.globalId.substring(15, 21);
        result.messageNumber = pair.globalId.substring(21, 24);
        
        // Compare messages
        if (pair.legacyMessage.equals(pair.newMessage)) {
            result.status = ComparisonStatus.MATCH;
            matchedMessages.incrementAndGet();
        } else {
            result.status = ComparisonStatus.MISMATCH;
            result.differences = findDifferences(pair.legacyMessage, pair.newMessage);
            mismatchedMessages.incrementAndGet();
        }
        
        results.add(result);
        
        // Keep only last 10000 results to prevent memory issues
        while (results.size() > 10000) {
            results.poll();
        }
    }
    
    private void checkTimeouts() {
        long now = System.currentTimeMillis();
        List<String> timedOut = new ArrayList<>();
        
        for (Map.Entry<String, MessagePair> entry : messagePairs.entrySet()) {
            MessagePair pair = entry.getValue();
            long oldestTimestamp = Math.min(
                pair.legacyTimestamp == 0 ? Long.MAX_VALUE : pair.legacyTimestamp,
                pair.newTimestamp == 0 ? Long.MAX_VALUE : pair.newTimestamp
            );
            
            if (oldestTimestamp != Long.MAX_VALUE && (now - oldestTimestamp) > PAIRING_TIMEOUT_MS) {
                timedOut.add(entry.getKey());
                
                ComparisonResult result = new ComparisonResult();
                result.globalId = pair.globalId;
                result.timestamp = LocalDateTime.now();
                result.legacyMessage = pair.legacyMessage;
                result.newMessage = pair.newMessage;
                result.legacyTimestamp = pair.legacyTimestamp;
                result.newTimestamp = pair.newTimestamp;
                
                if (pair.legacyMessage != null && pair.newMessage == null) {
                    result.status = ComparisonStatus.ORPHANED_LEGACY;
                    orphanedLegacy.incrementAndGet();
                } else if (pair.newMessage != null && pair.legacyMessage == null) {
                    result.status = ComparisonStatus.ORPHANED_NEW;
                    orphanedNew.incrementAndGet();
                }
                
                results.add(result);
            }
        }
        
        // Remove timed out pairs
        timedOut.forEach(messagePairs::remove);
    }
    
    private String findDifferences(String legacy, String newMsg) {
        StringBuilder diff = new StringBuilder();
        
        if (legacy.length() != newMsg.length()) {
            diff.append("Length differs: Legacy=").append(legacy.length())
                .append(", New=").append(newMsg.length()).append("; ");
        }
        
        int minLen = Math.min(legacy.length(), newMsg.length());
        int diffCount = 0;
        
        for (int i = 0; i < minLen && diffCount < 10; i++) {
            if (legacy.charAt(i) != newMsg.charAt(i)) {
                diff.append("Pos ").append(i).append(": '")
                    .append(legacy.charAt(i)).append("' vs '")
                    .append(newMsg.charAt(i)).append("'; ");
                diffCount++;
            }
        }
        
        if (diffCount == 10) {
            diff.append("(more differences...)");
        }
        
        return diff.toString();
    }
    
    private void generateHTMLReport() {
        try {
            StringBuilder html = new StringBuilder();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            html.append("<!DOCTYPE html>\n<html>\n<head>\n");
            html.append("<meta charset='UTF-8'>\n");
            html.append("<meta http-equiv='refresh' content='10'>\n");
            html.append("<title>MQ Message Comparison Report</title>\n");
            html.append("<style>\n");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
            html.append("h1 { color: #333; }\n");
            html.append(".summary { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            html.append(".stat { display: inline-block; margin: 10px 20px; }\n");
            html.append(".stat-label { font-weight: bold; color: #666; }\n");
            html.append(".stat-value { font-size: 24px; font-weight: bold; }\n");
            html.append(".match { color: #28a745; }\n");
            html.append(".mismatch { color: #dc3545; }\n");
            html.append(".orphaned { color: #ffc107; }\n");
            html.append(".duplicate { color: #17a2b8; }\n");
            html.append("table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            html.append("th { background: #007bff; color: white; padding: 12px; text-align: left; }\n");
            html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }\n");
            html.append("tr:hover { background: #f8f9fa; }\n");
            html.append(".status-match { background: #d4edda; }\n");
            html.append(".status-mismatch { background: #f8d7da; }\n");
            html.append(".status-orphaned { background: #fff3cd; }\n");
            html.append(".message-preview { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: monospace; font-size: 12px; }\n");
            html.append("</style>\n</head>\n<body>\n");
            
            html.append("<h1>IBM MQ Message Comparison Report</h1>\n");
            html.append("<p>Generated: ").append(now.format(formatter)).append("</p>\n");
            
            // Summary statistics
            html.append("<div class='summary'>\n");
            html.append("<h2>Summary Statistics</h2>\n");
            html.append("<div class='stat'><div class='stat-label'>Total Messages</div><div class='stat-value'>")
                .append(totalMessages.get()).append("</div></div>\n");
            html.append("<div class='stat'><div class='stat-label'>Matched</div><div class='stat-value match'>")
                .append(matchedMessages.get()).append("</div></div>\n");
            html.append("<div class='stat'><div class='stat-label'>Mismatched</div><div class='stat-value mismatch'>")
                .append(mismatchedMessages.get()).append("</div></div>\n");
            html.append("<div class='stat'><div class='stat-label'>Orphaned Legacy</div><div class='stat-value orphaned'>")
                .append(orphanedLegacy.get()).append("</div></div>\n");
            html.append("<div class='stat'><div class='stat-label'>Orphaned New</div><div class='stat-value orphaned'>")
                .append(orphanedNew.get()).append("</div></div>\n");
            html.append("<div class='stat'><div class='stat-label'>Duplicates</div><div class='stat-value duplicate'>")
                .append(duplicatesDetected.get()).append("</div></div>\n");
            
            // Calculate success rate
            int total = matchedMessages.get() + mismatchedMessages.get() + orphanedLegacy.get() + orphanedNew.get();
            double successRate = total > 0 ? (matchedMessages.get() * 100.0 / total) : 0;
            html.append("<div class='stat'><div class='stat-label'>Success Rate</div><div class='stat-value'>")
                .append(String.format("%.2f%%", successRate)).append("</div></div>\n");
            html.append("</div>\n");
            
            // Pending pairs
            html.append("<div class='summary'>\n");
            html.append("<h2>Pending Message Pairs</h2>\n");
            html.append("<p>Waiting for pair: ").append(messagePairs.size()).append(" messages</p>\n");
            html.append("</div>\n");
            
            // Recent results table
            html.append("<h2>Recent Comparison Results (Last 100)</h2>\n");
            html.append("<table>\n<thead>\n<tr>\n");
            html.append("<th>Timestamp</th><th>Global ID</th><th>Program</th><th>Date</th><th>Time</th><th>Msg#</th>");
            html.append("<th>Status</th><th>Latency (ms)</th><th>Differences</th>");
            html.append("</tr>\n</thead>\n<tbody>\n");
            
            List<ComparisonResult> recentResults = results.stream()
                .sorted(Comparator.comparing((ComparisonResult r) -> r.timestamp).reversed())
                .limit(100)
                .collect(Collectors.toList());
            
            for (ComparisonResult result : recentResults) {
                String rowClass = "";
                if (result.status == ComparisonStatus.MATCH) rowClass = "status-match";
                else if (result.status == ComparisonStatus.MISMATCH) rowClass = "status-mismatch";
                else rowClass = "status-orphaned";
                
                html.append("<tr class='").append(rowClass).append("'>\n");
                html.append("<td>").append(result.timestamp.format(formatter)).append("</td>\n");
                html.append("<td style='font-family: monospace;'>").append(escapeHtml(result.globalId)).append("</td>\n");
                html.append("<td>").append(escapeHtml(result.programName)).append("</td>\n");
                html.append("<td>").append(escapeHtml(result.dateStr)).append("</td>\n");
                html.append("<td>").append(escapeHtml(result.timeStr)).append("</td>\n");
                html.append("<td>").append(escapeHtml(result.messageNumber)).append("</td>\n");
                html.append("<td><strong>").append(result.status).append("</strong></td>\n");
                html.append("<td>").append(result.latencyMs).append("</td>\n");
                html.append("<td>").append(escapeHtml(result.differences != null ? result.differences : "")).append("</td>\n");
                html.append("</tr>\n");
            }
            
            html.append("</tbody>\n</table>\n");
            html.append("</body>\n</html>");
            
            // Write to file
            Files.write(Paths.get("mq_comparison_report.html"), html.toString().getBytes());
            
        } catch (Exception e) {
            System.err.println("Error generating HTML report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void generateFinalReport() {
        generateHTMLReport();
        System.out.println("\nFinal Statistics:");
        System.out.println("Total Messages: " + totalMessages.get());
        System.out.println("Matched: " + matchedMessages.get());
        System.out.println("Mismatched: " + mismatchedMessages.get());
        System.out.println("Orphaned Legacy: " + orphanedLegacy.get());
        System.out.println("Orphaned New: " + orphanedNew.get());
        System.out.println("Duplicates: " + duplicatesDetected.get());
        System.out.println("\nReport saved to: mq_comparison_report.html");
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    // Inner classes
    enum MessageSource {
        LEGACY, NEW
    }
    
    enum ComparisonStatus {
        MATCH, MISMATCH, ORPHANED_LEGACY, ORPHANED_NEW
    }
    
    static class MessagePair {
        String globalId;
        String legacyMessage;
        String newMessage;
        long legacyTimestamp;
        long newTimestamp;
        
        MessagePair(String globalId) {
            this.globalId = globalId;
        }
        
        boolean isComplete() {
            return legacyMessage != null && newMessage != null;
        }
    }
    
    static class ComparisonResult {
        String globalId;
        LocalDateTime timestamp;
        String legacyMessage;
        String newMessage;
        long legacyTimestamp;
        long newTimestamp;
        long latencyMs;
        ComparisonStatus status;
        String differences;
        String programName;
        String dateStr;
        String timeStr;
        String messageNumber;
    }
}
