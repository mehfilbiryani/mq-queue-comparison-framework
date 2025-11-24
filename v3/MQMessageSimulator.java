import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Message Simulator for Testing the MQ Comparator
 * Generates test messages with various scenarios
 */
public class MQMessageSimulator {
    
    private static final String QUEUE_MANAGER = "QM1";
    private static final String LEGACY_OUT_QUEUE = "LEGACY.OUT.QUEUE";
    private static final String NEW_OUT_QUEUE = "NEW.OUT.QUEUE";
    private static final String CHANNEL = "DEV.APP.SVRCONN";
    private static final String HOST = "localhost";
    private static final int PORT = 1414;
    
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public static void main(String[] args) {
        MQMessageSimulator simulator = new MQMessageSimulator();
        
        System.out.println("Starting Message Simulator...");
        System.out.println("1 - Normal matching messages");
        System.out.println("2 - Mismatched content");
        System.out.println("3 - Orphaned messages");
        System.out.println("4 - Duplicate messages");
        System.out.println("5 - Mixed scenario (realistic load test)");
        System.out.println("6 - Stress test (1000 messages)");
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select scenario: ");
        int choice = scanner.nextInt();
        
        simulator.runScenario(choice);
        scanner.close();
    }
    
    private void runScenario(int scenario) {
        switch (scenario) {
            case 1:
                normalMessages(50);
                break;
            case 2:
                mismatchedMessages(20);
                break;
            case 3:
                orphanedMessages(15);
                break;
            case 4:
                duplicateMessages(10);
                break;
            case 5:
                mixedScenario(100);
                break;
            case 6:
                stressTest(1000);
                break;
            default:
                System.out.println("Invalid scenario");
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Simulation complete!");
    }
    
    private void normalMessages(int count) {
        System.out.println("Generating " + count + " normal matching messages...");
        for (int i = 0; i < count; i++) {
            String globalId = generateGlobalId("TESTPGM", i);
            String message = generateMessage(globalId, "Normal test message " + i);
            
            final int msgNum = i;
            executor.submit(() -> {
                sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                // Add slight delay to simulate processing time
                sleep(random.nextInt(100));
                sendMessage(NEW_OUT_QUEUE, globalId, message);
                if (msgNum % 10 == 0) {
                    System.out.println("Sent message pair: " + msgNum);
                }
            });
            
            sleep(50); // Throttle message generation
        }
    }
    
    private void mismatchedMessages(int count) {
        System.out.println("Generating " + count + " mismatched messages...");
        for (int i = 0; i < count; i++) {
            String globalId = generateGlobalId("MISMTCH", i);
            String legacyMsg = generateMessage(globalId, "Legacy message content " + i);
            String newMsg = generateMessage(globalId, "New message content DIFFERENT " + i);
            
            final int msgNum = i;
            executor.submit(() -> {
                sendMessage(LEGACY_OUT_QUEUE, globalId, legacyMsg);
                sleep(random.nextInt(100));
                sendMessage(NEW_OUT_QUEUE, globalId, newMsg);
                System.out.println("Sent mismatched pair: " + msgNum);
            });
            
            sleep(50);
        }
    }
    
    private void orphanedMessages(int count) {
        System.out.println("Generating " + count + " orphaned messages...");
        for (int i = 0; i < count; i++) {
            String globalId = generateGlobalId("ORPHANS", i);
            String message = generateMessage(globalId, "Orphaned message " + i);
            
            final int msgNum = i;
            final boolean sendToLegacy = random.nextBoolean();
            
            executor.submit(() -> {
                if (sendToLegacy) {
                    sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                    System.out.println("Sent legacy-only message: " + msgNum);
                } else {
                    sendMessage(NEW_OUT_QUEUE, globalId, message);
                    System.out.println("Sent new-only message: " + msgNum);
                }
            });
            
            sleep(50);
        }
    }
    
    private void duplicateMessages(int count) {
        System.out.println("Generating " + count + " messages with duplicates...");
        for (int i = 0; i < count; i++) {
            String globalId = generateGlobalId("DUPLICT", i);
            String message = generateMessage(globalId, "Duplicate test message " + i);
            
            final int msgNum = i;
            executor.submit(() -> {
                // Send normal pair
                sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                sendMessage(NEW_OUT_QUEUE, globalId, message);
                
                // Send duplicates
                sleep(random.nextInt(200));
                sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                sleep(random.nextInt(100));
                sendMessage(NEW_OUT_QUEUE, globalId, message);
                
                System.out.println("Sent message with duplicates: " + msgNum);
            });
            
            sleep(100);
        }
    }
    
    private void mixedScenario(int totalMessages) {
        System.out.println("Generating " + totalMessages + " messages (mixed scenario)...");
        
        int matches = (int)(totalMessages * 0.7); // 70% matches
        int mismatches = (int)(totalMessages * 0.15); // 15% mismatches
        int orphans = (int)(totalMessages * 0.1); // 10% orphans
        int duplicates = totalMessages - matches - mismatches - orphans; // 5% duplicates
        
        List<Runnable> tasks = new ArrayList<>();
        
        // Normal matches
        for (int i = 0; i < matches; i++) {
            final int msgNum = i;
            tasks.add(() -> {
                String globalId = generateGlobalId("MIXNORM", msgNum);
                String message = generateMessage(globalId, "Mixed normal message " + msgNum);
                sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                sleep(random.nextInt(50));
                sendMessage(NEW_OUT_QUEUE, globalId, message);
            });
        }
        
        // Mismatches
        for (int i = 0; i < mismatches; i++) {
            final int msgNum = i;
            tasks.add(() -> {
                String globalId = generateGlobalId("MIXMISM", msgNum);
                String legacyMsg = generateMessage(globalId, "Legacy " + msgNum);
                String newMsg = generateMessage(globalId, "New " + msgNum + " DIFF");
                sendMessage(LEGACY_OUT_QUEUE, globalId, legacyMsg);
                sleep(random.nextInt(50));
                sendMessage(NEW_OUT_QUEUE, globalId, newMsg);
            });
        }
        
        // Orphans
        for (int i = 0; i < orphans; i++) {
            final int msgNum = i;
            tasks.add(() -> {
                String globalId = generateGlobalId("MIXORPH", msgNum);
                String message = generateMessage(globalId, "Orphan " + msgNum);
                if (random.nextBoolean()) {
                    sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                } else {
                    sendMessage(NEW_OUT_QUEUE, globalId, message);
                }
            });
        }
        
        // Duplicates
        for (int i = 0; i < duplicates; i++) {
            final int msgNum = i;
            tasks.add(() -> {
                String globalId = generateGlobalId("MIXDUPL", msgNum);
                String message = generateMessage(globalId, "Duplicate " + msgNum);
                sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                sendMessage(NEW_OUT_QUEUE, globalId, message);
                sleep(random.nextInt(100));
                sendMessage(LEGACY_OUT_QUEUE, globalId, message); // Duplicate
            });
        }
        
        // Shuffle and execute
        Collections.shuffle(tasks);
        tasks.forEach(task -> {
            executor.submit(task);
            sleep(20); // Throttle
        });
    }
    
    private void stressTest(int count) {
        System.out.println("Running stress test with " + count + " message pairs...");
        long startTime = System.currentTimeMillis();
        
        CountDownLatch latch = new CountDownLatch(count);
        
        for (int i = 0; i < count; i++) {
            final int msgNum = i;
            executor.submit(() -> {
                try {
                    String globalId = generateGlobalId("STRESS!", msgNum);
                    String message = generateMessage(globalId, "Stress test message " + msgNum);
                    
                    sendMessage(LEGACY_OUT_QUEUE, globalId, message);
                    sendMessage(NEW_OUT_QUEUE, globalId, message);
                    
                    if (msgNum % 100 == 0) {
                        System.out.println("Progress: " + msgNum + "/" + count);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            long duration = System.currentTimeMillis() - startTime;
            double throughput = (count * 2 * 1000.0) / duration; // Messages per second
            System.out.println("Stress test completed in " + duration + "ms");
            System.out.println("Throughput: " + String.format("%.2f", throughput) + " messages/sec");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private String generateGlobalId(String programName, int messageNumber) {
        // Ensure program name is exactly 7 characters
        String program = String.format("%-7s", programName).substring(0, 7);
        
        // Generate date and time
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("MMddyyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        
        // Message number (000-999)
        String msgNum = String.format("%03d", messageNumber % 1000);
        
        return program + date + time + msgNum;
    }
    
    private String generateMessage(String globalId, String content) {
        StringBuilder message = new StringBuilder();
        message.append(globalId); // Header with global ID
        message.append("|");
        message.append(content);
        message.append("|");
        message.append("TIMESTAMP=").append(System.currentTimeMillis());
        message.append("|");
        message.append("RANDOM=").append(UUID.randomUUID());
        return message.toString();
    }
    
    private void sendMessage(String queueName, String globalId, String message) {
        MQQueueManager qMgr = null;
        MQQueue queue = null;
        
        try {
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(MQConstants.HOST_NAME_PROPERTY, HOST);
            props.put(MQConstants.PORT_PROPERTY, PORT);
            props.put(MQConstants.CHANNEL_PROPERTY, CHANNEL);
            
            qMgr = new MQQueueManager(QUEUE_MANAGER, props);
            
            int openOptions = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_FAIL_IF_QUIESCING;
            queue = qMgr.accessQueue(queueName, openOptions);
            
            MQMessage mqMessage = new MQMessage();
            mqMessage.format = MQConstants.MQFMT_STRING;
            mqMessage.writeString(message);
            
            // Set correlation ID based on global ID
            byte[] correlationId = new byte[24];
            System.arraycopy(globalId.getBytes(), 0, correlationId, 0, 
                Math.min(globalId.length(), 24));
            mqMessage.correlationId = correlationId;
            
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            queue.put(mqMessage, pmo);
            
        } catch (Exception e) {
            System.err.println("Error sending message to " + queueName + ": " + e.getMessage());
        } finally {
            try {
                if (queue != null) queue.close();
                if (qMgr != null) qMgr.disconnect();
            } catch (MQException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Configuration Manager for Dynamic Settings
 */
class ConfigurationManager {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigurationManager.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not load configuration: " + e.getMessage());
        }
    }
    
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, System.getenv(key) != null ? System.getenv(key) : defaultValue);
    }
    
    public static int getInt(String key, int defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static long getLong(String key, long defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

/**
 * Performance Monitor for tracking comparator metrics
 */
class PerformanceMonitor {
    private final ConcurrentHashMap<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();
    
    public void recordLatency(String operation, long latencyMs) {
        PerformanceMetric metric = metrics.computeIfAbsent(operation, k -> new PerformanceMetric());
        metric.addSample(latencyMs);
    }
    
    public void printStatistics() {
        System.out.println("\n=== Performance Statistics ===");
        metrics.forEach((operation, metric) -> {
            System.out.printf("%s: avg=%.2fms, min=%dms, max=%dms, count=%d%n",
                operation, metric.getAverage(), metric.getMin(), metric.getMax(), metric.getCount());
        });
    }
    
    static class PerformanceMetric {
        private long totalLatency = 0;
        private long minLatency = Long.MAX_VALUE;
        private long maxLatency = 0;
        private int count = 0;
        
        public synchronized void addSample(long latency) {
            totalLatency += latency;
            minLatency = Math.min(minLatency, latency);
            maxLatency = Math.max(maxLatency, latency);
            count++;
        }
        
        public double getAverage() {
            return count > 0 ? (double) totalLatency / count : 0;
        }
        
        public long getMin() {
            return minLatency == Long.MAX_VALUE ? 0 : minLatency;
        }
        
        public long getMax() {
            return maxLatency;
        }
        
        public int getCount() {
            return count;
        }
    }
}

/**
 * Alert Manager for critical issues
 */
class AlertManager {
    private static final int MISMATCH_THRESHOLD = 10;
    private static final int ORPHAN_THRESHOLD = 5;
    
    private int consecutiveMismatches = 0;
    private int consecutiveOrphans = 0;
    
    public void checkMatch() {
        consecutiveMismatches = 0;
    }
    
    public void checkMismatch() {
        consecutiveMismatches++;
        if (consecutiveMismatches >= MISMATCH_THRESHOLD) {
            sendAlert("HIGH MISMATCH RATE: " + consecutiveMismatches + " consecutive mismatches detected!");
        }
    }
    
    public void checkOrphan() {
        consecutiveOrphans++;
        if (consecutiveOrphans >= ORPHAN_THRESHOLD) {
            sendAlert("DELIVERY ISSUE: " + consecutiveOrphans + " consecutive orphaned messages detected!");
        }
    }
    
    private void sendAlert(String message) {
        System.err.println("\n!!! ALERT !!! " + message);
        // Add email/webhook notification here
    }
}
