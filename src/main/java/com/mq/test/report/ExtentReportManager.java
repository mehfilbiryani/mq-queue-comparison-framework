// ============= ExtentReportManager.java =============
// Location: src/main/java/com/mq/test/report/ExtentReportManager.java

package com.mq.test.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Manager class for Extent Reports
 */
public class ExtentReportManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    
    public static void initReport(String reportPath) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("MQ Queue Comparison Report");
        sparkReporter.config().setReportName("Queue Message Comparison Test");
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", "Test");
        extent.setSystemInfo("Framework", "JUnit 5");
    }
    
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = extent.createTest(testName, description);
        test.set(extentTest);
        return extentTest;
    }
    
    public static ExtentTest getTest() {
        return test.get();
    }
    
    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}