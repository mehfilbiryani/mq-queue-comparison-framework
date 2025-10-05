package com.mq.test.config;

/**
 * Test execution configuration
 */
public class TestConfiguration {
    private String reportOutputPath;
    private long timestampToleranceMs;
    private boolean consoleLoggingEnabled;
    private boolean skipOnSetupFailure;
    
    public TestConfiguration(String reportOutputPath, long timestampToleranceMs, 
                            boolean consoleLoggingEnabled, boolean skipOnSetupFailure) {
        this.reportOutputPath = reportOutputPath;
        this.timestampToleranceMs = timestampToleranceMs;
        this.consoleLoggingEnabled = consoleLoggingEnabled;
        this.skipOnSetupFailure = skipOnSetupFailure;
    }
    
    public String getReportOutputPath() {
        return reportOutputPath;
    }
    
    public long getTimestampToleranceMs() {
        return timestampToleranceMs;
    }
    
    public boolean isConsoleLoggingEnabled() {
        return consoleLoggingEnabled;
    }
    
    public boolean isSkipOnSetupFailure() {
        return skipOnSetupFailure;
    }
}