import com.aventstack.extentreports.*;
import java.util.*;

/**
 * Complete usage examples with ExtentReports integration
 */
public class ExtentReportComparisonExamples {
    
    public static void main(String[] args) {
        // Example 1: Basic comparison with ExtentReports
        example1_BasicComparison();
        
        // Example 2: Multiple report comparisons in one test suite
        example2_MultipleComparisons();
        
        // Example 3: Advanced with custom nodes and drill-down
        example3_AdvancedDrillDown();
        
        // Example 4: Integration with TestNG
        example4_TestNGIntegration();
    }
    
    /**
     * Example 1: Basic CSV comparison with ExtentReports
     */
    public static void example1_BasicComparison() {
        System.out.println("=== Example 1: Basic Comparison with ExtentReports ===\n");
        
        // Initialize ExtentReports
        ExtentReportComparator reportComparator = 
            new ExtentReportComparator("reports/comparison_report.html");
        
        try {
            // Load sample data
            List<ReportComparator.ReportRow> legacyRows = createSampleLegacyData();
            List<ReportComparator.ReportRow> modernRows = createSampleModernData();
            
            // Configure comparison
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ORDER_ID"))
                    .ignoreColumns("TIMESTAMP", "PROCESS_ID")
                    .setCaseSensitive(false);
            
            // Perform comparison and generate report
            reportComparator.compareAndReport(
                legacyRows, 
                modernRows, 
                config,
                "Order Report Comparison - Legacy vs Modern"
            );
            
        } finally {
            reportComparator.flush();
        }
    }
    
    /**
     * Example 2: Multiple comparisons in one test suite
     */
    public static void example2_MultipleComparisons() {
        System.out.println("\n=== Example 2: Multiple Comparisons ===\n");
        
        ExtentReportComparator reportComparator = 
            new ExtentReportComparator("reports/multi_comparison_report.html");
        
        try {
            // Test 1: Order Report
            ExtentTest orderTest = reportComparator.createComparisonTest(
                "Order Report Comparison",
                "Compare order data between legacy and modern systems"
            );
            
            compareOrderReports(reportComparator, orderTest);
            
            // Test 2: Customer Report
            ExtentTest customerTest = reportComparator.createComparisonTest(
                "Customer Report Comparison",
                "Compare customer data between legacy and modern systems"
            );
            
            compareCustomerReports(reportComparator, customerTest);
            
            // Test 3: Transaction Report
            ExtentTest transactionTest = reportComparator.createComparisonTest(
                "Transaction Report Comparison",
                "Compare transaction data between legacy and modern systems"
            );
            
            compareTransactionReports(reportComparator, transactionTest);
            
        } finally {
            reportComparator.flush();
        }
    }
    
    private static void compareOrderReports(ExtentReportComparator reportComparator, 
                                           ExtentTest test) {
        try {
            List<ReportComparator.ReportRow> legacyRows = createSampleLegacyData();
            List<ReportComparator.ReportRow> modernRows = createSampleModernData();
            
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ORDER_ID"));
            
            reportComparator.logConfiguration(test, config);
            
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            logComparisonResults(test, result, "Order");
            
            if (result.getSummary().isPerfectMatch()) {
                test.pass("Order reports match perfectly");
            } else {
                test.warning("Order reports have discrepancies");
            }
            
        } catch (Exception e) {
            test.fail("Order comparison failed: " + e.getMessage());
        }
    }
    
    private static void compareCustomerReports(ExtentReportComparator reportComparator, 
                                              ExtentTest test) {
        // Similar implementation for customer reports
        test.info("Customer report comparison logic here");
        test.pass("Customer reports matched");
    }
    
    private static void compareTransactionReports(ExtentReportComparator reportComparator, 
                                                 ExtentTest test) {
        // Similar implementation for transaction reports
        test.info("Transaction report comparison logic here");
        test.warning("Found 5 discrepancies in transaction reports");
    }
    
    /**
     * Example 3: Advanced comparison with custom drill-down
     */
    public static void example3_AdvancedDrillDown() {
        System.out.println("\n=== Example 3: Advanced Drill-Down ===\n");
        
        ExtentReportComparator reportComparator = 
            new ExtentReportComparator("reports/advanced_comparison_report.html");
        
        try {
            ExtentTest mainTest = reportComparator.createComparisonTest(
                "Comprehensive Report Analysis",
                "Detailed comparison with statistical analysis and recommendations"
            );
            
            // Load data
            List<ReportComparator.ReportRow> legacyRows = createLargeDataset(1000);
            List<ReportComparator.ReportRow> modernRows = createLargeDataset(1020);
            
            // Configure
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(
                    Arrays.asList("CUSTOMER_ID", "PRODUCT_ID"))
                    .ignoreColumns("LAST_UPDATED", "VERSION");
            
            reportComparator.logConfiguration(mainTest, config);
            
            // Compare
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            // Advanced drill-down
            createAdvancedAnalysis(mainTest, result);
            
        } finally {
            reportComparator.flush();
        }
    }
    
    private static void createAdvancedAnalysis(ExtentTest test, 
                                              ReportComparator.ComparisonResult result) {
        
        // Node 1: Executive Summary
        ExtentTest execSummary = test.createNode("Executive Summary");
        ReportComparator.ComparisonSummary summary = result.getSummary();
        
        if (summary.isPerfectMatch()) {
            execSummary.pass("<b>✓ PARITY ACHIEVED</b>");
        } else {
            execSummary.warning("<b>⚠ PARITY CHECK FAILED</b>");
            execSummary.warning("Requires immediate attention from development team");
        }
        
        // Node 2: Data Quality Metrics
        ExtentTest qualityNode = test.createNode("Data Quality Metrics");
        
        int totalRows = summary.getTotalLegacyRows() + summary.getTotalModernRows();
        double qualityScore = 100.0 - 
            ((summary.getLegacyOnlyRows() + summary.getModernOnlyRows() + 
              summary.getRowsWithDifferences()) * 100.0 / totalRows);
        
        qualityNode.info(String.format("Overall Data Quality Score: <b>%.2f%%</b>", 
            qualityScore));
        
        if (qualityScore >= 99) {
            qualityNode.pass("Excellent data quality");
        } else if (qualityScore >= 95) {
            qualityNode.warning("Acceptable data quality with minor issues");
        } else {
            qualityNode.fail("Poor data quality - requires investigation");
        }
        
        // Node 3: Root Cause Analysis
        ExtentTest rootCauseNode = test.createNode("Root Cause Analysis");
        
        if (summary.getLegacyOnlyRows() > 0) {
            ExtentTest legacyCause = rootCauseNode.createNode("Missing Modern Records");
            legacyCause.warning("Possible causes:");
            legacyCause.warning("• Data migration incomplete");
            legacyCause.warning("• Filtering logic differences");
            legacyCause.warning("• Timing issues in data extraction");
        }
        
        if (summary.getModernOnlyRows() > 0) {
            ExtentTest modernCause = rootCauseNode.createNode("Extra Modern Records");
            modernCause.warning("Possible causes:");
            modernCause.warning("• New data created in modern system");
            modernCause.warning("• Different reporting period");
            modernCause.warning("• Legacy system data archival");
        }
        
        // Node 4: Impact Assessment
        ExtentTest impactNode = test.createNode("Business Impact Assessment");
        
        if (summary.isPerfectMatch()) {
            impactNode.pass("✓ No business impact - systems are in sync");
        } else {
            impactNode.warning("⚠ Business Impact: MEDIUM");
            impactNode.warning("Recommendation: Review discrepancies before go-live");
            
            if (summary.getLegacyOnlyRows() > 100) {
                impactNode.fail("⚠ HIGH IMPACT: Significant data loss detected");
            }
        }
        
        // Node 5: Action Items
        ExtentTest actionNode = test.createNode("Recommended Action Items");
        actionNode.info("1. Review and validate missing records");
        actionNode.info("2. Update data transformation logic");
        actionNode.info("3. Rerun comparison after fixes");
        actionNode.info("4. Document any acceptable differences");
    }
    
    /**
     * Example 4: Integration with TestNG
     */
    public static void example4_TestNGIntegration() {
        System.out.println("\n=== Example 4: TestNG Integration Pattern ===\n");
        System.out.println("See TestNGReportComparisonTest class for full implementation");
    }
    
    // Helper methods
    private static void logComparisonResults(ExtentTest test, 
                                            ReportComparator.ComparisonResult result,
                                            String reportType) {
        
        ReportComparator.ComparisonSummary summary = result.getSummary();
        
        ExtentTest resultsNode = test.createNode(reportType + " Comparison Results");
        
        resultsNode.info("Legacy Rows: " + summary.getTotalLegacyRows());
        resultsNode.info("Modern Rows: " + summary.getTotalModernRows());
        resultsNode.info("Matched Rows: " + summary.getMatchedRows());
        
        if (summary.getLegacyOnlyRows() > 0) {
            resultsNode.warning("Legacy-Only: " + summary.getLegacyOnlyRows());
        }
        
        if (summary.getModernOnlyRows() > 0) {
            resultsNode.warning("Modern-Only: " + summary.getModernOnlyRows());
        }
        
        if (summary.getRowsWithDifferences() > 0) {
            resultsNode.warning("With Differences: " + summary.getRowsWithDifferences());
        }
    }
    
    private static List<ReportComparator.ReportRow> createSampleLegacyData() {
        return Arrays.asList(
            createRow(1, "ORDER_ID", "ORD001", "CUSTOMER", "John Doe", 
                     "AMOUNT", "1000.00", "STATUS", "COMPLETED"),
            createRow(2, "ORDER_ID", "ORD002", "CUSTOMER", "Jane Smith", 
                     "AMOUNT", "2500.50", "STATUS", "PENDING"),
            createRow(3, "ORDER_ID", "ORD003", "CUSTOMER", "Bob Johnson", 
                     "AMOUNT", "750.25", "STATUS", "COMPLETED")
        );
    }
    
    private static List<ReportComparator.ReportRow> createSampleModernData() {
        return Arrays.asList(
            createRow(1, "ORDER_ID", "ORD001", "CUSTOMER", "John Doe", 
                     "AMOUNT", "1000.00", "STATUS", "COMPLETED"),
            createRow(2, "ORDER_ID", "ORD002", "CUSTOMER", "Jane Smith", 
                     "AMOUNT", "2500.75", "STATUS", "PENDING"),  // Different amount
            createRow(4, "ORDER_ID", "ORD004", "CUSTOMER", "Alice Brown", 
                     "AMOUNT", "3200.00", "STATUS", "SHIPPED")   // New order
        );
    }
    
    private static List<ReportComparator.ReportRow> createLargeDataset(int size) {
        List<ReportComparator.ReportRow> rows = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= size; i++) {
            rows.add(createRow(i, 
                "CUSTOMER_ID", "C" + String.format("%05d", i),
                "PRODUCT_ID", "P" + random.nextInt(100),
                "QUANTITY", String.valueOf(random.nextInt(10) + 1),
                "PRICE", String.format("%.2f", random.nextDouble() * 1000)));
        }
        
        return rows;
    }
    
    private static ReportComparator.ReportRow createRow(int lineNum, String... keyValues) {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            data.put(keyValues[i], keyValues[i + 1]);
        }
        return new ReportComparator.ReportRow(lineNum, data);
    }
}

/**
 * TestNG Integration Example
 */
class TestNGReportComparisonTest {
    
    private ExtentReportComparator reportComparator;
    private ExtentTest currentTest;
    
    // @BeforeClass
    public void setupExtentReport() {
        reportComparator = new ExtentReportComparator(
            "reports/testng_comparison_" + System.currentTimeMillis() + ".html");
    }
    
    // @AfterClass
    public void tearDownExtentReport() {
        if (reportComparator != null) {
            reportComparator.flush();
        }
    }
    
    // @BeforeMethod
    public void setupTest(Object[] testMethod) {
        // Get test method name from TestNG
        String testName = "Sample Test";  // testMethod.getMethodName()
        currentTest = reportComparator.createComparisonTest(testName, 
            "Automated comparison test");
    }
    
    // @Test(priority = 1)
    public void testOrderReportParity() {
        try {
            currentTest.info("Starting order report comparison");
            
            // Load reports
            List<ReportComparator.ReportRow> legacyRows = loadFromCSV("legacy_orders.csv");
            List<ReportComparator.ReportRow> modernRows = loadFromCSV("modern_orders.csv");
            
            // Configure
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ORDER_ID"));
            
            reportComparator.logConfiguration(currentTest, config);
            
            // Compare
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            // Log results
            logDetailedResults(result);
            
            // Assert
            ReportComparator.ComparisonSummary summary = result.getSummary();
            if (summary.isPerfectMatch()) {
                currentTest.pass("✓ Order reports match perfectly");
            } else {
                currentTest.fail("✗ Order reports have " + 
                    (summary.getLegacyOnlyRows() + summary.getModernOnlyRows() + 
                     summary.getRowsWithDifferences()) + " discrepancies");
                
                // Uncomment for TestNG assertions
                // Assert.fail("Parity check failed");
            }
            
        } catch (Exception e) {
            currentTest.fail(e);
            throw new RuntimeException(e);
        }
    }
    
    // @Test(priority = 2)
    public void testCustomerReportParity() {
        currentTest.info("Customer report comparison");
        // Similar implementation
    }
    
    // @Test(priority = 3, dependsOnMethods = {"testOrderReportParity", "testCustomerReportParity"})
    public void generateExecutiveSummary() {
        ExtentTest summaryTest = reportComparator.createComparisonTest(
            "Executive Summary",
            "Overall parity check results across all reports"
        );
        
        summaryTest.info("All comparison tests completed");
        summaryTest.pass("✓ Test suite execution successful");
    }
    
    private void logDetailedResults(ReportComparator.ComparisonResult result) {
        ReportComparator.ComparisonSummary summary = result.getSummary();
        
        ExtentTest detailNode = currentTest.createNode("Detailed Results");
        detailNode.info("Total Legacy Rows: " + summary.getTotalLegacyRows());
        detailNode.info("Total Modern Rows: " + summary.getTotalModernRows());
        detailNode.info("Matched Rows: " + summary.getMatchedRows());
        
        if (summary.getLegacyOnlyRows() > 0) {
            detailNode.warning("Legacy-Only Rows: " + summary.getLegacyOnlyRows());
        }
        
        if (summary.getModernOnlyRows() > 0) {
            detailNode.warning("Modern-Only Rows: " + summary.getModernOnlyRows());
        }
        
        if (summary.getRowsWithDifferences() > 0) {
            detailNode.warning("Rows with Differences: " + summary.getRowsWithDifferences());
        }
    }
    
    private List<ReportComparator.ReportRow> loadFromCSV(String filename) {
        // CSV loading logic
        return new ArrayList<>();
    }
}
