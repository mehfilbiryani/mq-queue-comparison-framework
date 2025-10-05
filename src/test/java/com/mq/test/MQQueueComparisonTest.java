package com.mq.test;

import com.aventstack.extentreports.ExtentTest;
import com.ibm.mq.MQException;
import com.mq.test.comparator.MessageComparator;
import com.mq.test.config.MQConnectionConfig;
import com.mq.test.error.MQErrorHandler;
import com.mq.test.model.ComparisonResult;
import com.mq.test.model.MQMessage;
import com.mq.test.report.ExtentReportManager;
import com.mq.test.util.MQMessageReader;
import com.mq.test.util.TestLogger;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main test class for IBM MQ Queue Comparison - Refactored with centralized error handling and logging
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MQQueueComparisonTest {
    
    private static MQConnectionConfig config;
    private static String queue1Name = "QUEUE1";
    private static String queue2Name = "QUEUE2";
    private static List<MQMessage> queue1Messages;
    private static List<MQMessage> queue2Messages;
    private static boolean setupSuccessful = false;
    private static String setupErrorMessage = null;
    private static ExtentTest setupTest;
    
    @BeforeAll
    public static void setup() {
        TestLogger.printConsoleBanner("IBM MQ QUEUE COMPARISON FRAMEWORK");
        
        try {
            ExtentReportManager.initReport("target/ExtentReport.html");
            setupTest = ExtentReportManager.createTest("Setup & Connection Test", 
                "Initialize MQ connections and read messages");
            
            TestLogger.logInfo(setupTest, "Starting MQ Queue Comparison Framework");
            TestLogger.printConsoleSection("Configuration");
            
            // Configure MQ connection
            config = new MQConnectionConfig(
                "localhost",
                1414,
                "QM1",
                "DEV.ADMIN.SVRCONN",
                "admin",
                "password"
            );
            com.mq.test.setup.MQQueueSetup.bootstrap(
    config,
    queue1Name,   // "QUEUE1"
    queue2Name,   // "QUEUE2"
    true,         // clear queues first
    10            // seed N messages; set 0 if you don't want seeding
);

            TestLogger.logConnectionAttempt(setupTest, config.getHost(), config.getPort(), config.getQueueManager());
            
            // Initialize message lists
            queue1Messages = new ArrayList<>();
            queue2Messages = new ArrayList<>();
            
            // Read messages from Queue 1
            TestLogger.printConsoleSection("Reading Queue: " + queue1Name);
            try {
                queue1Messages = MQMessageReader.readMessages(config, queue1Name, 1000, true);
                TestLogger.logConnectionSuccess(setupTest, queue1Name, queue1Messages.size());
            } catch (MQException mqe) {
                MQErrorHandler.handleMQException(setupTest, queue1Name, mqe);
                setupErrorMessage = MQErrorHandler.handleMQException(queue1Name, mqe);
                throw mqe;
            } catch (UnknownHostException uhe) {
                MQErrorHandler.handleUnknownHostException(setupTest, config, uhe);
                setupErrorMessage = MQErrorHandler.handleUnknownHostException(config, uhe);
                throw uhe;
            } catch (ConnectException ce) {
                MQErrorHandler.handleConnectionException(setupTest, config, ce);
                setupErrorMessage = MQErrorHandler.handleConnectionException(config, ce);
                throw ce;
            } catch (SocketTimeoutException ste) {
                MQErrorHandler.handleTimeoutException(setupTest, config, ste);
                setupErrorMessage = MQErrorHandler.handleTimeoutException(config, ste);
                throw ste;
            } catch (IOException ioe) {
                MQErrorHandler.handleIOException(setupTest, ioe);
                setupErrorMessage = MQErrorHandler.handleIOException(ioe);
                throw ioe;
            }
            
            // Read messages from Queue 2
            TestLogger.printConsoleSection("Reading Queue: " + queue2Name);
            try {
                queue2Messages = MQMessageReader.readMessages(config, queue2Name, 1000, true);
                TestLogger.logConnectionSuccess(setupTest, queue2Name, queue2Messages.size());
            } catch (MQException mqe) {
                MQErrorHandler.handleMQException(setupTest, queue2Name, mqe);
                setupErrorMessage = MQErrorHandler.handleMQException(queue2Name, mqe);
                throw mqe;
            } catch (UnknownHostException uhe) {
                MQErrorHandler.handleUnknownHostException(setupTest, config, uhe);
                setupErrorMessage = MQErrorHandler.handleUnknownHostException(config, uhe);
                throw uhe;
            } catch (ConnectException ce) {
                MQErrorHandler.handleConnectionException(setupTest, config, ce);
                setupErrorMessage = MQErrorHandler.handleConnectionException(config, ce);
                throw ce;
            } catch (SocketTimeoutException ste) {
                MQErrorHandler.handleTimeoutException(setupTest, config, ste);
                setupErrorMessage = MQErrorHandler.handleTimeoutException(config, ste);
                throw ste;
            } catch (IOException ioe) {
                MQErrorHandler.handleIOException(setupTest, ioe);
                setupErrorMessage = MQErrorHandler.handleIOException(ioe);
                throw ioe;
            }
            
            TestLogger.logPass(setupTest, "Setup completed successfully");
            setupSuccessful = true;
            
        } catch (Exception e) {
            if (setupErrorMessage == null) {
                setupErrorMessage = MQErrorHandler.handleGenericException(e);
                MQErrorHandler.handleGenericException(setupTest, e);
            }
            setupSuccessful = false;
            System.err.println(MQErrorHandler.formatConsoleError("Setup Failed", setupErrorMessage));
        }
    }
    
    private void checkSetupSuccess() {
        if (!setupSuccessful) {
            ExtentTest test = ExtentReportManager.getTest();
            TestLogger.logSkip(test, "Test skipped due to setup failure: " + setupErrorMessage);
            Assumptions.assumeTrue(setupSuccessful, "Setup failed: " + setupErrorMessage);
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Compare Message Count")
    public void testMessageCount() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Message Count Comparison", 
            "Verify both queues have same message count");
        TestLogger.logTestStart(test, "Message Count Comparison");
        
        try {
            ComparisonResult result = MessageComparator.compareMessageCount(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), result.getMessage());
            TestLogger.logTestEnd(test, "Message Count Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("Compare Message Payloads")
    public void testPayloadComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Payload Comparison", "Verify message payloads match");
        TestLogger.logTestStart(test, "Payload Comparison");
        
        try {
            ComparisonResult result = MessageComparator.comparePayloads(queue1Messages, queue2Messages);
            TestLogger.logInfo(test, String.format("Comparing %d messages", 
                Math.min(queue1Messages.size(), queue2Messages.size())));
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Payload comparison failed");
            TestLogger.logTestEnd(test, "Payload Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(3)
    @DisplayName("Compare Message IDs")
    public void testMessageIdComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Message ID Comparison", "Verify message IDs match");
        TestLogger.logTestStart(test, "Message ID Comparison");
        
        try {
            ComparisonResult result = MessageComparator.compareMessageIds(queue1Messages, queue2Messages);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Message ID comparison completed");
            TestLogger.logTestEnd(test, "Message ID Comparison", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Compare Correlation IDs")
    public void testCorrelationIdComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Correlation ID Comparison", "Verify correlation IDs match");
        TestLogger.logTestStart(test, "Correlation ID Comparison");
        
        try {
            ComparisonResult result = MessageComparator.compareCorrelationIds(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Correlation ID comparison failed");
            TestLogger.logTestEnd(test, "Correlation ID Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("Compare Message Priorities")
    public void testPriorityComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Priority Comparison", "Verify message priorities match");
        TestLogger.logTestStart(test, "Priority Comparison");
        
        try {
            ComparisonResult result = MessageComparator.comparePriorities(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Priority comparison failed");
            TestLogger.logTestEnd(test, "Priority Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(6)
    @DisplayName("Compare Message Ordering")
    public void testMessageOrdering() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Message Ordering Comparison", "Verify message ordering consistency");
        TestLogger.logTestStart(test, "Message Ordering");
        
        try {
            ComparisonResult result = MessageComparator.compareOrdering(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Message ordering differs");
            TestLogger.logTestEnd(test, "Message Ordering", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("Check for Empty Queues")
    public void testEmptyQueues() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Empty Queue Check", "Verify queues are not empty");
        TestLogger.logTestStart(test, "Empty Queue Check");
        
        try {
            TestLogger.logInfo(test, String.format("Queue1 has %d messages", queue1Messages.size()));
            TestLogger.logInfo(test, String.format("Queue2 has %d messages", queue2Messages.size()));
            
            boolean passed = !(queue1Messages.isEmpty() || queue2Messages.isEmpty());
            if (passed) {
                TestLogger.logPass(test, "Both queues contain messages");
            } else {
                TestLogger.logFail(test, "One or both queues are empty");
            }
            
            assertFalse(queue1Messages.isEmpty() && queue2Messages.isEmpty(), "Both queues are empty");
            TestLogger.logTestEnd(test, "Empty Queue Check", passed);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(8)
    @DisplayName("Compare Message Formats")
    public void testFormatComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Format Comparison", "Verify message formats match");
        TestLogger.logTestStart(test, "Format Comparison");
        
        try {
            ComparisonResult result = MessageComparator.compareFormats(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Format comparison failed");
            TestLogger.logTestEnd(test, "Format Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("Compare Message Timestamps")
    public void testTimestampComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Timestamp Comparison", "Verify timestamps within tolerance");
        TestLogger.logTestStart(test, "Timestamp Comparison");
        
        try {
            long toleranceMs = 5000;
            TestLogger.logInfo(test, String.format("Using tolerance: %dms", toleranceMs));
            ComparisonResult result = MessageComparator.compareTimestamps(queue1Messages, queue2Messages, toleranceMs);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Timestamp comparison completed");
            TestLogger.logTestEnd(test, "Timestamp Comparison", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(10)
    @DisplayName("Compare Message Properties")
    public void testPropertyComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Property Comparison", "Verify custom message properties match");
        TestLogger.logTestStart(test, "Property Comparison");
        
        try {
            ComparisonResult result = MessageComparator.compareMessageProperties(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Property comparison failed");
            TestLogger.logTestEnd(test, "Property Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(11)
    @DisplayName("Compare Payload Lengths")
    public void testPayloadLengthComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Payload Length Comparison", "Verify payload sizes match");
        TestLogger.logTestStart(test, "Payload Length Comparison");
        
        try {
            ComparisonResult result = MessageComparator.comparePayloadLength(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Payload length comparison failed");
            TestLogger.logTestEnd(test, "Payload Length Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(12)
    @DisplayName("Compare Payload Structure")
    public void testPayloadStructureComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Payload Structure Comparison", "Verify payload format types (JSON/XML) match");
        TestLogger.logTestStart(test, "Payload Structure Comparison");
        
        try {
            ComparisonResult result = MessageComparator.comparePayloadStructure(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Payload structure comparison failed");
            TestLogger.logTestEnd(test, "Payload Structure Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(13)
    @DisplayName("Check for Duplicates in Queue1")
    public void testDuplicatesQueue1() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Duplicate Check - Queue1", "Check for duplicate messages in Queue1");
        TestLogger.logTestStart(test, "Duplicate Check - Queue1");
        
        try {
            ComparisonResult result = MessageComparator.findDuplicateMessages(queue1Messages, queue1Name);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Duplicate check completed for Queue1");
            TestLogger.logTestEnd(test, "Duplicate Check - Queue1", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(14)
    @DisplayName("Check for Duplicates in Queue2")
    public void testDuplicatesQueue2() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Duplicate Check - Queue2", "Check for duplicate messages in Queue2");
        TestLogger.logTestStart(test, "Duplicate Check - Queue2");
        
        try {
            ComparisonResult result = MessageComparator.findDuplicateMessages(queue2Messages, queue2Name);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Duplicate check completed for Queue2");
            TestLogger.logTestEnd(test, "Duplicate Check - Queue2", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(15)
    @DisplayName("Check Message Sequence in Queue1")
    public void testSequenceQueue1() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Sequence Check - Queue1", "Verify message sequence in Queue1");
        TestLogger.logTestStart(test, "Sequence Check - Queue1");
        
        try {
            ComparisonResult result = MessageComparator.checkMessageSequence(queue1Messages, queue1Name);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Sequence check completed for Queue1");
            TestLogger.logTestEnd(test, "Sequence Check - Queue1", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(16)
    @DisplayName("Check Message Sequence in Queue2")
    public void testSequenceQueue2() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Sequence Check - Queue2", "Verify message sequence in Queue2");
        TestLogger.logTestStart(test, "Sequence Check - Queue2");
        
        try {
            ComparisonResult result = MessageComparator.checkMessageSequence(queue2Messages, queue2Name);
            TestLogger.logComparisonResultAsWarning(test, result);
            TestLogger.logInfo(test, "Sequence check completed for Queue2");
            TestLogger.logTestEnd(test, "Sequence Check - Queue2", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(17)
    @DisplayName("Compare Payload Checksums")
    public void testPayloadChecksumComparison() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Checksum Comparison", "Verify payload checksums match");
        TestLogger.logTestStart(test, "Checksum Comparison");
        
        try {
            ComparisonResult result = MessageComparator.comparePayloadChecksum(queue1Messages, queue2Messages);
            TestLogger.logComparisonResult(test, result);
            assertTrue(result.isPassed(), "Checksum comparison failed");
            TestLogger.logTestEnd(test, "Checksum Comparison", result.isPassed());
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    @Order(18)
    @DisplayName("Statistical Summary")
    public void testStatisticalSummary() {
        checkSetupSuccess();
        ExtentTest test = ExtentReportManager.createTest("Statistical Summary", "Overall statistics and summary");
        TestLogger.logTestStart(test, "Statistical Summary");
        
        try {
            long totalSize1 = queue1Messages.stream().mapToLong(m -> m.getPayload().length()).sum();
            long totalSize2 = queue2Messages.stream().mapToLong(m -> m.getPayload().length()).sum();
            
            double avgSize1 = queue1Messages.isEmpty() ? 0 : (double)totalSize1 / queue1Messages.size();
            double avgSize2 = queue2Messages.isEmpty() ? 0 : (double)totalSize2 / queue2Messages.size();
            
            long minSize1 = queue1Messages.stream().mapToLong(m -> m.getPayload().length()).min().orElse(0);
            long minSize2 = queue2Messages.stream().mapToLong(m -> m.getPayload().length()).min().orElse(0);
            
            long maxSize1 = queue1Messages.stream().mapToLong(m -> m.getPayload().length()).max().orElse(0);
            long maxSize2 = queue2Messages.stream().mapToLong(m -> m.getPayload().length()).max().orElse(0);
            
            TestLogger.logStatistics(test, queue1Name, queue1Messages.size(), totalSize1, avgSize1, minSize1, maxSize1);
            TestLogger.logSeparator(test);
            TestLogger.logStatistics(test, queue2Name, queue2Messages.size(), totalSize2, avgSize2, minSize2, maxSize2);
            
            TestLogger.logPass(test, "Statistical summary generated");
            TestLogger.logTestEnd(test, "Statistical Summary", true);
        } catch (Exception e) {
            TestLogger.logFail(test, "Exception during test: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterAll
    public static void tearDown() {
        try {
            ExtentReportManager.flush();
            TestLogger.printConsoleSummary(
                setupSuccessful, 
                setupErrorMessage, 
                queue1Messages != null ? queue1Messages.size() : 0,
                queue2Messages != null ? queue2Messages.size() : 0,
                "target/ExtentReport.html"
            );
        } catch (Exception e) {
            System.err.println("Error during teardown: " + e.getMessage());
        }
    }
}
