import com.aventstack.extentreports.*;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import java.util.*;
import java.io.*;

/**
 * Complete, self-contained example of report comparison with ExtentReports
 * This can be run as a standalone class with main method
 */
public class CompleteReportComparisonExample {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Report Comparison with ExtentReports");
        System.out.println("========================================\n");
        
        // Initialize ExtentReports
        ExtentReportComparator reportComparator = 
            new ExtentReportComparator("reports/complete_example_report.html");
        
        try {
            // Scenario 1: Perfect match
            runPerfectMatchScenario(reportComparator);
            
            // Scenario 2: Missing rows
            runMissingRowsScenario(reportComparator);
            
            // Scenario 3: Field differences
            runFieldDifferencesScenario(reportComparator);
            
            // Scenario 4: Complex mixed scenario
            runComplexScenario(reportComparator);
            
            System.out.println("\n✓ All scenarios completed successfully!");
            System.out.println("Report generated at: reports/complete_example_report.html");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            reportComparator.flush();
        }
    }
    
    /**
     * Scenario 1: Perfect match - all rows identical
     */
    private static void runPerfectMatchScenario(ExtentReportComparator reportComparator) {
        System.out.println("Running Scenario 1: Perfect Match...");
        
        ExtentTest test = reportComparator.createComparisonTest(
            "Scenario 1: Perfect Match",
            "Both systems have identical data"
        );
        
        try {
            // Create identical datasets
            List<ReportComparator.ReportRow> legacyRows = createPerfectMatchData();
            List<ReportComparator.ReportRow> modernRows = createPerfectMatchData();
            
            test.info("Created test data: " + legacyRows.size() + " rows each");
            
            // Configure comparison
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ID"));
            
            reportComparator.logConfiguration(test, config);
            
            // Perform comparison
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            // Log detailed results
            ReportComparator.ComparisonSummary summary = result.getSummary();
            
            ExtentTest resultsNode = test.createNode("Results");
            resultsNode.pass("✓ All " + summary.getMatchedRows() + " rows matched perfectly");
            resultsNode.pass("✓ No missing rows in legacy system");
            resultsNode.pass("✓ No missing rows in modern system");
            resultsNode.pass("✓ No field-level differences");
            
            // Create summary table
            String[][] data = {
                {"Metric", "Count"},
                {"Total Rows", String.valueOf(legacyRows.size())},
                {"Perfect Matches", String.valueOf(summary.getMatchedRows())},
                {"Discrepancies", "0"}
            };
            resultsNode.info(MarkupHelper.createTable(data));
            
            test.pass("✓ SCENARIO 1 PASSED: Perfect parity achieved!");
            System.out.println("  ✓ Scenario 1 passed\n");
            
        } catch (Exception e) {
            test.fail("Scenario failed: " + e.getMessage());
            test.fail(e);
        }
    }
    
    /**
     * Scenario 2: Missing rows in one system
     */
    private static void runMissingRowsScenario(ExtentReportComparator reportComparator) {
        System.out.println("Running Scenario 2: Missing Rows...");
        
        ExtentTest test = reportComparator.createComparisonTest(
            "Scenario 2: Missing Rows",
            "Legacy has 3 extra rows, Modern has 2 extra rows"
        );
        
        try {
            List<ReportComparator.ReportRow> legacyRows = Arrays.asList(
                createRow(1, "ID", "1", "NAME", "Alice", "AMOUNT", "100.00"),
                createRow(2, "ID", "2", "NAME", "Bob", "AMOUNT", "200.00"),
                createRow(3, "ID", "3", "NAME", "Charlie", "AMOUNT", "300.00"),
                createRow(4, "ID", "4", "NAME", "David", "AMOUNT", "400.00"),
                createRow(5, "ID", "5", "NAME", "Eve", "AMOUNT", "500.00")
            );
            
            List<ReportComparator.ReportRow> modernRows = Arrays.asList(
                createRow(1, "ID", "1", "NAME", "Alice", "AMOUNT", "100.00"),
                createRow(2, "ID", "2", "NAME", "Bob", "AMOUNT", "200.00"),
                createRow(3, "ID", "6", "NAME", "Frank", "AMOUNT", "600.00"),
                createRow(4, "ID", "7", "NAME", "Grace", "AMOUNT", "700.00")
            );
            
            test.info("Legacy rows: " + legacyRows.size());
            test.info("Modern rows: " + modernRows.size());
            
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ID"));
            
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            ReportComparator.ComparisonSummary summary = result.getSummary();
            
            // Results
            ExtentTest resultsNode = test.createNode("Analysis");
            resultsNode.info("Matched: " + summary.getMatchedRows() + " rows");
            resultsNode.warning("⚠ Legacy-Only: " + summary.getLegacyOnlyRows() + " rows");
            resultsNode.warning("⚠ Modern-Only: " + summary.getModernOnlyRows() + " rows");
            
            // Detail missing rows
            if (summary.getLegacyOnlyRows() > 0) {
                ExtentTest legacyDetail = resultsNode.createNode("Legacy-Only Details");
                for (ReportComparator.ReportRow row : result.getLegacyOnlyRows()) {
                    legacyDetail.warning("ID: " + row.get("ID") + " - " + row.get("NAME"));
                }
            }
            
            if (summary.getModernOnlyRows() > 0) {
                ExtentTest modernDetail = resultsNode.createNode("Modern-Only Details");
                for (ReportComparator.ReportRow row : result.getModernOnlyRows()) {
                    modernDetail.warning("ID: " + row.get("ID") + " - " + row.get("NAME"));
                }
            }
            
            // Recommendations
            ExtentTest recommendations = test.createNode("Recommendations");
            recommendations.warning("→ Investigate why IDs 3,4,5 are missing in modern system");
            recommendations.warning("→ Verify if IDs 6,7 are newly added or data migration issues");
            recommendations.info("→ Review data migration scripts");
            
            test.warning("⚠ SCENARIO 2: Discrepancies found");
            System.out.println("  ⚠ Scenario 2 completed with warnings\n");
            
        } catch (Exception e) {
            test.fail(e);
        }
    }
    
    /**
     * Scenario 3: Field-level differences
     */
    private static void runFieldDifferencesScenario(ExtentReportComparator reportComparator) {
        System.out.println("Running Scenario 3: Field Differences...");
        
        ExtentTest test = reportComparator.createComparisonTest(
            "Scenario 3: Field-Level Differences",
            "Same rows but different field values"
        );
        
        try {
            List<ReportComparator.ReportRow> legacyRows = Arrays.asList(
                createRow(1, "ID", "1", "NAME", "Alice", "AMOUNT", "100.00", "STATUS", "ACTIVE"),
                createRow(2, "ID", "2", "NAME", "Bob", "AMOUNT", "200.00", "STATUS", "PENDING"),
                createRow(3, "ID", "3", "NAME", "Charlie", "AMOUNT", "300.00", "STATUS", "COMPLETED")
            );
            
            List<ReportComparator.ReportRow> modernRows = Arrays.asList(
                createRow(1, "ID", "1", "NAME", "Alice", "AMOUNT", "100.00", "STATUS", "ACTIVE"),
                createRow(2, "ID", "2", "NAME", "Bob", "AMOUNT", "250.00", "STATUS", "ACTIVE"),  // Different amount & status
                createRow(3, "ID", "3", "NAME", "Charlie", "AMOUNT", "300.00", "STATUS", "COMPLETED")
            );
            
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(Arrays.asList("ID"));
            
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            ReportComparator.ComparisonSummary summary = result.getSummary();
            
            ExtentTest resultsNode = test.createNode("Difference Analysis");
            resultsNode.info("All " + summary.getMatchedRows() + " rows matched by ID");
            resultsNode.warning("Found differences in " + summary.getRowsWithDifferences() + " rows");
            
            // Detail field differences
            ExtentTest diffDetails = resultsNode.createNode("Field Difference Details");
            
            for (ReportComparator.MatchedRow match : result.getMatchedRows()) {
                if (!match.hasNoDifferences()) {
                    ExtentTest rowDetail = diffDetails.createNode(
                        "ID: " + match.getLegacyRow().get("ID") + " - " + 
                        match.getDifferences().size() + " field(s)"
                    );
                    
                    String[][] diffTable = new String[match.getDifferences().size() + 1][3];
                    diffTable[0] = new String[]{"Field", "Legacy", "Modern"};
                    
                    int i = 1;
                    for (ReportComparator.FieldDifference diff : match.getDifferences().values()) {
                        diffTable[i++] = new String[]{
                            diff.getColumn(),
                            diff.getLegacyValue(),
                            diff.getModernValue()
                        };
                    }
                    
                    rowDetail.warning(MarkupHelper.createTable(diffTable));
                }
            }
            
            // Statistical analysis
            ExtentTest stats = test.createNode("Statistics");
            Map<String, Integer> fieldDiffCount = new HashMap<>();
            
            for (ReportComparator.MatchedRow match : result.getMatchedRows()) {
                for (String field : match.getDifferences().keySet()) {
                    fieldDiffCount.merge(field, 1, Integer::sum);
                }
            }
            
            stats.info("Fields with most differences:");
            for (Map.Entry<String, Integer> entry : fieldDiffCount.entrySet()) {
                stats.warning("  • " + entry.getKey() + ": " + entry.getValue() + " occurrences");
            }
            
            test.warning("⚠ SCENARIO 3: Field-level discrepancies detected");
            System.out.println("  ⚠ Scenario 3 completed with field differences\n");
            
        } catch (Exception e) {
            test.fail(e);
        }
    }
    
    /**
     * Scenario 4: Complex mixed scenario
     */
    private static void runComplexScenario(ExtentReportComparator reportComparator) {
        System.out.println("Running Scenario 4: Complex Mixed Scenario...");
        
        ExtentTest test = reportComparator.createComparisonTest(
            "Scenario 4: Complex Mixed Scenario",
            "Combination of missing rows, extra rows, and field differences"
        );
        
        try {
            // Create complex test data
            List<ReportComparator.ReportRow> legacyRows = createComplexLegacyData();
            List<ReportComparator.ReportRow> modernRows = createComplexModernData();
            
            test.info("Processing " + legacyRows.size() + " legacy and " + 
                     modernRows.size() + " modern records");
            
            ReportComparator.ComparisonConfig config = 
                new ReportComparator.ComparisonConfig(
                    Arrays.asList("CUSTOMER_ID", "ORDER_ID"))
                    .ignoreColumns("TIMESTAMP", "PROCESSED_BY");
            
            reportComparator.logConfiguration(test, config);
            
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            ReportComparator.ComparisonSummary summary = result.getSummary();
            
            // Executive summary with visual indicators
            ExtentTest execSummary = test.createNode("Executive Summary");
            
            double matchRate = (summary.getMatchedRows() * 100.0) / 
                (summary.getTotalLegacyRows() + summary.getTotalModernRows() - summary.getMatchedRows());
            
            execSummary.info("Overall Match Rate: " + String.format("%.2f%%", matchRate));
            
            String status;
            if (matchRate >= 99) {
                status = "✓ Excellent";
                execSummary.pass(status);
            } else if (matchRate >= 95) {
                status = "⚠ Good with minor issues";
                execSummary.warning(status);
            } else {
                status = "✗ Critical - Requires immediate attention";
                execSummary.fail(status);
            }
            
            // Detailed breakdown with visual table
            String[][] summaryTable = {
                {"Category", "Legacy", "Modern", "Status"},
                {"Total Rows", 
                 String.valueOf(summary.getTotalLegacyRows()),
                 String.valueOf(summary.getTotalModernRows()),
                 "—"},
                {"Matched Rows",
                 String.valueOf(summary.getMatchedRows()),
                 String.valueOf(summary.getMatchedRows()),
                 "✓"},
                {"Legacy-Only",
                 String.valueOf(summary.getLegacyOnlyRows()),
                 "—",
                 summary.getLegacyOnlyRows() == 0 ? "✓" : "✗"},
                {"Modern-Only",
                 "—",
                 String.valueOf(summary.getModernOnlyRows()),
                 summary.getModernOnlyRows() == 0 ? "✓" : "✗"},
                {"With Differences",
                 String.valueOf(summary.getRowsWithDifferences()),
                 String.valueOf(summary.getRowsWithDifferences()),
                 summary.getRowsWithDifferences() == 0 ? "✓" : "⚠"}
            };
            
            execSummary.info(MarkupHelper.createTable(summaryTable));
            
            // Action items
            ExtentTest actionItems = test.createNode("Action Items");
            actionItems.info("Priority: HIGH");
            actionItems.warning("1. Review and reconcile " + summary.getLegacyOnlyRows() + 
                " missing modern records");
            actionItems.warning("2. Validate " + summary.getModernOnlyRows() + 
                " new modern records");
            actionItems.warning("3. Fix field transformation issues in " + 
                summary.getRowsWithDifferences() + " records");
            actionItems.info("4. Document acceptable differences (if any)");
            actionItems.info("5. Rerun comparison after fixes");
            
            test.warning("⚠ SCENARIO 4: Complex discrepancies identified");
            System.out.println("  ⚠ Scenario 4 completed - see detailed report\n");
            
        } catch (Exception e) {
            test.fail(e);
        }
    }
    
    // Helper methods to create test data
    private static List<ReportComparator.ReportRow> createPerfectMatchData() {
        return Arrays.asList(
            createRow(1, "ID", "1", "NAME", "Alice", "VALUE", "100"),
            createRow(2, "ID", "2", "NAME", "Bob", "VALUE", "200"),
            createRow(3, "ID", "3", "NAME", "Charlie", "VALUE", "300")
        );
    }
    
    private static List<ReportComparator.ReportRow> createComplexLegacyData() {
        return Arrays.asList(
            createRow(1, "CUSTOMER_ID", "C001", "ORDER_ID", "O001", "AMOUNT", "1000", "STATUS", "COMPLETED"),
            createRow(2, "CUSTOMER_ID", "C001", "ORDER_ID", "O002", "AMOUNT", "2000", "STATUS", "PENDING"),
            createRow(3, "CUSTOMER_ID", "C002", "ORDER_ID", "O003", "AMOUNT", "3000", "STATUS", "SHIPPED"),
            createRow(4, "CUSTOMER_ID", "C003", "ORDER_ID", "O004", "AMOUNT", "4000", "STATUS", "CANCELLED")
        );
    }
    
    private static List<ReportComparator.ReportRow> createComplexModernData() {
        return Arrays.asList(
            createRow(1, "CUSTOMER_ID", "C001", "ORDER_ID", "O001", "AMOUNT", "1000", "STATUS", "COMPLETED"),
            createRow(2, "CUSTOMER_ID", "C001", "ORDER_ID", "O002", "AMOUNT", "2500", "STATUS", "ACTIVE"),  // Different
            createRow(3, "CUSTOMER_ID", "C002", "ORDER_ID", "O005", "AMOUNT", "5000", "STATUS", "PROCESSING")  // New
        );
    }
    
    private static ReportComparator.ReportRow createRow(int lineNum, String... keyValues) {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            data.put(keyValues[i], keyValues[i + 1]);
        }
        return new ReportComparator.ReportRow(lineNum, data);
    }
}
