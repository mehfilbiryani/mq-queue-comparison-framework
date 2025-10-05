package com.mq.test.util;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.mq.test.model.ComparisonResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging utility for test execution
 */
public class TestLogger {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final boolean CONSOLE_LOGGING_ENABLED = true;
    
    /**
     * Log info message
     */
    public static void logInfo(ExtentTest test, String message) {
        if (test != null) {
            test.log(Status.INFO, message);
        }
        if (CONSOLE_LOGGING_ENABLED) {
            System.out.println("[INFO] " + getTimestamp() + " - " + message);
        }
    }
    
    /**
     * Log success message
     */
    public static void logPass(ExtentTest test, String message) {
        if (test != null) {
            test.pass(message);
        }
        if (CONSOLE_LOGGING_ENABLED) {
            System.out.println("[PASS] " + getTimestamp() + " - " + message);
        }
    }
    
    /**
     * Log failure message
     */
    public static void logFail(ExtentTest test, String message) {
        if (test != null) {
            test.fail(message);
        }
        if (CONSOLE_LOGGING_ENABLED) {
            System.err.println("[FAIL] " + getTimestamp() + " - " + message);
        }
    }
    
    /**
     * Log warning message
     */
    public static void logWarning(ExtentTest test, String message) {
        if (test != null) {
            test.warning(message);
        }
        if (CONSOLE_LOGGING_ENABLED) {
            System.out.println("[WARN] " + getTimestamp() + " - " + message);
        }
    }
    
    /**
     * Log skip message
     */
    public static void logSkip(ExtentTest test, String message) {
        if (test != null) {
            test.skip(message);
        }
        if (CONSOLE_LOGGING_ENABLED) {
            System.out.println("[SKIP] " + getTimestamp() + " - " + message);
        }
    }
    
    /**
     * Log comparison result
     */
    public static void logComparisonResult(ExtentTest test, ComparisonResult result) {
        logInfo(test, result.getMessage());
        
        if (result.isPassed()) {
            logPass(test, "Comparison passed successfully");
        } else {
            logFail(test, "Comparison failed");
            for (String diff : result.getDifferences()) {
                logFail(test, diff);
            }
        }
    }
    
    /**
     * Log comparison result with warning level
     */
    public static void logComparisonResultAsWarning(ExtentTest test, ComparisonResult result) {
        logInfo(test, result.getMessage());
        
        if (result.isPassed()) {
            logPass(test, "Comparison passed successfully");
        } else {
            logWarning(test, "Comparison has differences (non-critical)");
            for (String diff : result.getDifferences()) {
                logWarning(test, diff);
            }
        }
    }
    
    /**
     * Log test start
     */
    public static void logTestStart(ExtentTest test, String testName) {
        String message = String.format("=== Starting Test: %s ===", testName);
        logInfo(test, message);
    }
    
    /**
     * Log test end
     */
    public static void logTestEnd(ExtentTest test, String testName, boolean passed) {
        String status = passed ? "PASSED" : "FAILED";
        String message = String.format("=== Test %s: %s ===", status, testName);
        if (passed) {
            logPass(test, message);
        } else {
            logFail(test, message);
        }
    }
    
    /**
     * Log connection attempt
     */
    public static void logConnectionAttempt(ExtentTest test, String host, int port, String queueManager) {
        logInfo(test, String.format("Attempting to connect to MQ: %s:%d (QM: %s)", host, port, queueManager));
    }
    
    /**
     * Log connection success
     */
    public static void logConnectionSuccess(ExtentTest test, String queueName, int messageCount) {
        logPass(test, String.format("Successfully connected and read %d messages from queue '%s'", 
            messageCount, queueName));
    }
    
    /**
     * Log statistics
     */
    public static void logStatistics(ExtentTest test, String queueName, int messageCount, 
                                     long totalSize, double avgSize, long minSize, long maxSize) {
        logInfo(test, String.format("<b>%s Statistics:</b>", queueName));
        logInfo(test, String.format("├─ Total Messages: %d", messageCount));
        logInfo(test, String.format("├─ Total Size: %,d bytes (%.2f KB)", totalSize, totalSize / 1024.0));
        logInfo(test, String.format("├─ Average Size: %.2f bytes", avgSize));
        logInfo(test, String.format("├─ Min Size: %,d bytes", minSize));
        logInfo(test, String.format("└─ Max Size: %,d bytes", maxSize));
    }
    
    /**
     * Log separator
     */
    public static void logSeparator(ExtentTest test) {
        logInfo(test, "─────────────────────────────────────────────────────────");
    }
    
    /**
     * Print console banner
     */
    public static void printConsoleBanner(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println(String.format("║ %-64s ║", centerText(title, 64)));
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Print console section
     */
    public static void printConsoleSection(String section) {
        System.out.println("\n┌──────────────────────────────────────────────────────────────────┐");
        System.out.println(String.format("│ %-64s │", section));
        System.out.println("└──────────────────────────────────────────────────────────────────┘");
    }
    
    /**
     * Print console summary
     */
    public static void printConsoleSummary(boolean setupSuccessful, String errorMessage, 
                                          int queue1Count, int queue2Count, String reportPath) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         TEST SUMMARY                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ Setup Status    : %-46s ║", 
            setupSuccessful ? "✓ SUCCESS" : "✗ FAILED"));
        
        if (!setupSuccessful && errorMessage != null) {
            String[] lines = wrapText(errorMessage, 44);
            System.out.println(String.format("║ Error Message   : %-46s ║", lines[0]));
            for (int i = 1; i < lines.length; i++) {
                System.out.println(String.format("║                   %-46s ║", lines[i]));
            }
        }
        
        System.out.println(String.format("║ Queue1 Messages : %-46d ║", queue1Count));
        System.out.println(String.format("║ Queue2 Messages : %-46d ║", queue2Count));
        System.out.println(String.format("║ Report Location : %-46s ║", reportPath));
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
    }
    
    /**
     * Get current timestamp
     */
    private static String getTimestamp() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
    
    /**
     * Center text within given width
     */
    private static String centerText(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    /**
     * Wrap text to fit within specified width
     */
    private static String[] wrapText(String text, int width) {
        if (text.length() <= width) return new String[]{text};
        
        String[] words = text.split(" ");
        StringBuilder[] lines = new StringBuilder[10];
        int lineIndex = 0;
        lines[lineIndex] = new StringBuilder();
        
        for (String word : words) {
            if (lines[lineIndex].length() + word.length() + 1 <= width) {
                if (lines[lineIndex].length() > 0) lines[lineIndex].append(" ");
                lines[lineIndex].append(word);
            } else {
                lineIndex++;
                if (lineIndex >= lines.length) break;
                lines[lineIndex] = new StringBuilder(word);
            }
        }
        
        String[] result = new String[lineIndex + 1];
        for (int i = 0; i <= lineIndex; i++) {
            result[i] = lines[i].toString();
        }
        return result;
    }
}

