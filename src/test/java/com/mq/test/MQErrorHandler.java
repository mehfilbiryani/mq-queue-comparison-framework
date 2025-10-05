package com.mq.test.error;

import com.aventstack.extentreports.ExtentTest;
import com.ibm.mq.MQException;
import com.mq.test.config.MQConnectionConfig;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Centralized error handler for MQ and network-related exceptions with null safety
 */
public class MQErrorHandler {
    
    /**
     * Handle MQ-specific exceptions with detailed error information
     */
    public static void handleMQException(ExtentTest test, String queueName, MQException mqe) {
        if (test != null) {
            test.fail(String.format("MQ Error accessing queue '%s'", queueName));
            test.fail(String.format("Reason Code: %d (0x%04X)", mqe.reasonCode, mqe.reasonCode));
            test.fail(String.format("Completion Code: %d", mqe.completionCode));
            
            String userMessage = getMQErrorDescription(mqe.reasonCode);
            test.fail(String.format("<b>Issue:</b> %s", userMessage));
            test.fail(String.format("<b>Technical Details:</b> %s", mqe.getMessage()));
        } else {
            System.err.println(String.format("MQ Error accessing queue '%s'", queueName));
            System.err.println(String.format("Reason Code: %d (0x%04X)", mqe.reasonCode, mqe.reasonCode));
            System.err.println(String.format("Completion Code: %d", mqe.completionCode));
            System.err.println("Message: " + mqe.getMessage());
        }
    }
    
    /**
     * Handle MQ exception without ExtentTest (for console output)
     */
    public static String handleMQException(String queueName, MQException mqe) {
        String userMessage = getMQErrorDescription(mqe.reasonCode);
        return String.format("MQ Error accessing queue '%s': %s (Reason Code: %d, Completion Code: %d) - %s", 
            queueName, userMessage, mqe.reasonCode, mqe.completionCode, mqe.getMessage());
    }
    
    /**
     * Get user-friendly description for MQ reason codes
     */
    public static String getMQErrorDescription(int reasonCode) {
        switch (reasonCode) {
            case 2035: return "Authentication failed. Please verify username and password.";
            case 2059: return "Queue Manager is not available or not running.";
            case 2538: return "Host not available. Please verify hostname and network connectivity.";
            case 2009: return "Connection to Queue Manager was broken unexpectedly.";
            case 2085: return "Queue not found. Please verify the queue name exists.";
            case 2393: return "Channel is not available. Please verify channel name and configuration.";
            case 2397: return "Channel has been stopped. Please start the channel.";
            case 2161: return "Queue is full. Cannot put more messages.";
            case 2033: return "No messages available in the queue.";
            case 2080: return "Message was truncated and could not be read completely.";
            case 2195: return "An unexpected error occurred in the Queue Manager.";
            case 2058: return "Queue Manager name is invalid or not found.";
            case 2540: return "Not connected to Queue Manager.";
            case 2101: return "Object already exists.";
            case 2189: return "Cluster resolution error occurred.";
            case 2277: return "SSL/TLS initialization error. Check SSL configuration.";
            case 2162: return "Priority value is not valid.";
            case 2082: return "Unknown alias base queue.";
            default: return String.format("MQ Error occurred (Code: %d). Please check MQ documentation.", reasonCode);
        }
    }
    
    /**
     * Handle unknown host exceptions
     */
    public static void handleUnknownHostException(ExtentTest test, MQConnectionConfig config, UnknownHostException uhe) {
        if (test != null) {
            test.fail("DNS Resolution Failed");
            test.fail(String.format("<b>Issue:</b> Cannot resolve hostname: %s", config.getHost()));
            test.fail("<b>Possible Solutions:</b>");
            test.fail("1. Verify the hostname/IP address is correct");
            test.fail("2. Check DNS server configuration");
            test.fail("3. Try using IP address instead of hostname");
            test.fail("4. Verify network connectivity (ping the hostname)");
            test.fail("5. Check /etc/hosts or C:\\Windows\\System32\\drivers\\etc\\hosts file");
            test.fail(String.format("<b>Technical Details:</b> %s", uhe.getMessage()));
        } else {
            System.err.println("DNS Resolution Failed");
            System.err.println("Cannot resolve hostname: " + config.getHost());
            System.err.println(uhe.getMessage());
        }
    }
    
    /**
     * Handle unknown host exception without ExtentTest
     */
    public static String handleUnknownHostException(MQConnectionConfig config, UnknownHostException uhe) {
        return String.format("DNS Resolution Failed: Cannot resolve hostname '%s'. %s", 
            config.getHost(), uhe.getMessage());
    }
    
    /**
     * Handle connection refused exceptions
     */
    public static void handleConnectionException(ExtentTest test, MQConnectionConfig config, ConnectException ce) {
        if (test != null) {
            test.fail("Connection Refused");
            test.fail(String.format("<b>Issue:</b> Cannot connect to MQ server at %s:%d", 
                config.getHost(), config.getPort()));
            test.fail("<b>Possible Solutions:</b>");
            test.fail("1. Verify MQ Queue Manager is running");
            test.fail("2. Check if the port number is correct (default: 1414)");
            test.fail("3. Verify firewall rules allow connection to this port");
            test.fail("4. Ensure MQ listener is active: 'runmqlsr -t tcp -p " + config.getPort() + "'");
            test.fail("5. Check if channel is running and accepting connections");
            test.fail("6. Verify security exit programs if configured");
            test.fail("7. Check Queue Manager status: 'dspmq'");
            test.fail(String.format("<b>Technical Details:</b> %s", ce.getMessage()));
        } else {
            System.err.println("Connection Refused");
            System.err.println(String.format("Cannot connect to MQ server at %s:%d", 
                config.getHost(), config.getPort()));
            System.err.println(ce.getMessage());
        }
    }
    
    /**
     * Handle connection exception without ExtentTest
     */
    public static String handleConnectionException(MQConnectionConfig config, ConnectException ce) {
        return String.format("Connection Refused: Cannot connect to MQ server at %s:%d. %s", 
            config.getHost(), config.getPort(), ce.getMessage());
    }
    
    /**
     * Handle socket timeout exceptions
     */
    public static void handleTimeoutException(ExtentTest test, MQConnectionConfig config, SocketTimeoutException ste) {
        if (test != null) {
            test.fail("Connection Timeout");
            test.fail("<b>Issue:</b> Connection attempt timed out");
            test.fail(String.format("<b>Target:</b> %s:%d", config.getHost(), config.getPort()));
            test.fail("<b>Possible Solutions:</b>");
            test.fail("1. Check network connectivity to MQ server (ping/traceroute)");
            test.fail("2. Verify firewall is not blocking the connection");
            test.fail("3. Check if MQ server is overloaded or unresponsive");
            test.fail("4. Increase connection timeout settings if network is slow");
            test.fail("5. Verify VPN connection if accessing remote server");
            test.fail("6. Check for network congestion or packet loss");
            test.fail("7. Verify proxy settings if applicable");
            test.fail(String.format("<b>Technical Details:</b> %s", ste.getMessage()));
        } else {
            System.err.println("Connection Timeout");
            System.err.println(String.format("Target: %s:%d", config.getHost(), config.getPort()));
            System.err.println(ste.getMessage());
        }
    }
    
    /**
     * Handle timeout exception without ExtentTest
     */
    public static String handleTimeoutException(MQConnectionConfig config, SocketTimeoutException ste) {
        return String.format("Connection Timeout: Connection to %s:%d timed out. %s", 
            config.getHost(), config.getPort(), ste.getMessage());
    }
    
    /**
     * Handle I/O exceptions
     */
    public static void handleIOException(ExtentTest test, IOException ioe) {
        if (test != null) {
            test.fail("I/O Error");
            test.fail("<b>Issue:</b> Input/Output error occurred during communication");
            test.fail("<b>Possible Solutions:</b>");
            test.fail("1. Check network stability and connection quality");
            test.fail("2. Verify MQ server is responsive");
            test.fail("3. Check for network congestion or intermittent connectivity");
            test.fail("4. Verify SSL/TLS configuration if using secure connection");
            test.fail("5. Check system resources (memory, CPU, network buffers)");
            test.fail("6. Verify no antivirus/security software blocking communication");
            test.fail(String.format("<b>Technical Details:</b> %s", ioe.getMessage()));
        } else {
            System.err.println("I/O Error");
            System.err.println(ioe.getMessage());
        }
    }
    
    /**
     * Handle I/O exception without ExtentTest
     */
    public static String handleIOException(IOException ioe) {
        return String.format("I/O Error: %s", ioe.getMessage());
    }
    
    /**
     * Handle generic exceptions
     */
    public static void handleGenericException(ExtentTest test, Exception e) {
        if (test != null) {
            test.fail("Unexpected Error");
            test.fail(String.format("<b>Exception Type:</b> %s", e.getClass().getSimpleName()));
            test.fail(String.format("<b>Message:</b> %s", e.getMessage()));
            test.fail("<b>Stack Trace:</b>");
            test.fail("<pre>" + getStackTrace(e) + "</pre>");
        } else {
            System.err.println("Unexpected Error: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Stack Trace:");
            e.printStackTrace();
        }
    }
    
    /**
     * Handle generic exception without ExtentTest
     */
    public static String handleGenericException(Exception e) {
        return String.format("Unexpected Error: %s - %s", e.getClass().getSimpleName(), e.getMessage());
    }
    
    /**
     * Get stack trace as string
     */
    public static String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        if (e.getCause() != null) {
            sb.append("Caused by: ").append(e.getCause().toString()).append("\n");
            for (StackTraceElement element : e.getCause().getStackTrace()) {
                sb.append("    at ").append(element.toString()).append("\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * Format error message for console output
     */
    public static String formatConsoleError(String errorType, String details) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║ %-62s ║\n", "ERROR: " + errorType));
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        
        String[] lines = details.split("\n");
        for (String line : lines) {
            if (line.length() <= 62) {
                sb.append(String.format("║ %-62s ║\n", line));
            } else {
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if (currentLine.length() + word.length() + 1 <= 62) {
                        if (currentLine.length() > 0) currentLine.append(" ");
                        currentLine.append(word);
                    } else {
                        sb.append(String.format("║ %-62s ║\n", currentLine.toString()));
                        currentLine = new StringBuilder(word);
                    }
                }
                if (currentLine.length() > 0) {
                    sb.append(String.format("║ %-62s ║\n", currentLine.toString()));
                }
            }
        }
        
        sb.append("╚════════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}