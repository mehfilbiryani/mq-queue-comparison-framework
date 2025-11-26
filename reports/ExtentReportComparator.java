import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.markuputils.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExtentReports integration for Report Comparison
 * Provides rich, interactive HTML reports with drill-down capabilities
 */
public class ExtentReportComparator {
    
    private ExtentReports extent;
    private ExtentSparkReporter sparkReporter;
    private String reportPath;
    
    public ExtentReportComparator(String reportPath) {
        this.reportPath = reportPath;
        initializeReport();
    }
    
    private void initializeReport() {
        sparkReporter = new ExtentSparkReporter(reportPath);
        
        // Configure report
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Legacy vs Modern Report Comparison");
        sparkReporter.config().setReportName("Parity Check Report");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        sparkReporter.config().setEncoding("utf-8");
        sparkReporter.config().setCss(".badge-default { background-color: #6c757d; }");
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        
        // System information
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Tester", "Automated QA");
        extent.setSystemInfo("Comparison Date", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * Main comparison method with ExtentReports integration
     */
    public void compareAndReport(List<ReportComparator.ReportRow> legacyRows,
                                 List<ReportComparator.ReportRow> modernRows,
                                 ReportComparator.ComparisonConfig config,
                                 String testName) {
        
        ExtentTest test = extent.createTest(testName)
            .assignCategory("Report Comparison")
            .assignAuthor("QA Automation");
        
        try {
            // Perform comparison
            ReportComparator comparator = new ReportComparator();
            ReportComparator.ComparisonResult result = 
                comparator.compare(legacyRows, modernRows, config);
            
            ReportComparator.ComparisonSummary summary = result.getSummary();
            
            // Log summary
            logSummary(test, summary, legacyRows.size(), modernRows.size());
            
            // Determine overall status
            if (summary.isPerfectMatch()) {
                test.pass("✓ Perfect Match - All rows are identical!");
                logPerfectMatchDetails(test, summary);
            } else {
                test.warning("⚠ Discrepancies Found");
                
                // Log detailed differences
                if (summary.getLegacyOnlyRows() > 0) {
                    logLegacyOnlyRows(test, result.getLegacyOnlyRows());
                }
                
                if (summary.getModernOnlyRows() > 0) {
                    logModernOnlyRows(test, result.getModernOnlyRows());
                }
                
                if (summary.getRowsWithDifferences() > 0) {
                    logFieldDifferences(test, result.getMatchedRows());
                }
                
                // Statistical analysis
                logStatisticalAnalysis(test, result);
            }
            
        } catch (Exception e) {
            test.fail("Comparison failed: " + e.getMessage());
            test.fail(e);
        }
    }
    
    /**
     * Log summary with visual markup
     */
    private void logSummary(ExtentTest test, ReportComparator.ComparisonSummary summary,
                           int legacyCount, int modernCount) {
        
        ExtentTest summaryNode = test.createNode("Summary Statistics");
        
        // Create summary table
        String[][] data = {
            {"Metric", "Legacy System", "Modern System", "Status"},
            {"Total Rows", String.valueOf(legacyCount), String.valueOf(modernCount), 
             legacyCount == modernCount ? "✓" : "✗"},
            {"Matched Rows", String.valueOf(summary.getMatchedRows()), 
             String.valueOf(summary.getMatchedRows()), "—"},
            {"Legacy-Only Rows", String.valueOf(summary.getLegacyOnlyRows()), "—",
             summary.getLegacyOnlyRows() == 0 ? "✓" : "✗"},
            {"Modern-Only Rows", "—", String.valueOf(summary.getModernOnlyRows()),
             summary.getModernOnlyRows() == 0 ? "✓" : "✗"},
            {"Matched with Differences", String.valueOf(summary.getRowsWithDifferences()),
             String.valueOf(summary.getRowsWithDifferences()),
             summary.getRowsWithDifferences() == 0 ? "✓" : "✗"}
        };
        
        Markup markup = MarkupHelper.createTable(data);
        summaryNode.info(markup);
        
        // Add badges
        summaryNode.info("<span class='badge badge-primary'>Legacy: " + 
            legacyCount + "</span> " +
            "<span class='badge badge-info'>Modern: " + modernCount + "</span> " +
            "<span class='badge badge-success'>Matched: " + summary.getMatchedRows() + "</span>");
        
        if (summary.getLegacyOnlyRows() > 0) {
            summaryNode.warning("<span class='badge badge-warning'>Legacy-Only: " + 
                summary.getLegacyOnlyRows() + "</span>");
        }
        
        if (summary.getModernOnlyRows() > 0) {
            summaryNode.warning("<span class='badge badge-warning'>Modern-Only: " + 
                summary.getModernOnlyRows() + "</span>");
        }
        
        if (summary.getRowsWithDifferences() > 0) {
            summaryNode.warning("<span class='badge badge-danger'>Differences: " + 
                summary.getRowsWithDifferences() + "</span>");
        }
    }
    
    /**
     * Log perfect match details
     */
    private void logPerfectMatchDetails(ExtentTest test, 
                                       ReportComparator.ComparisonSummary summary) {
        ExtentTest detailNode = test.createNode("Perfect Match Details");
        detailNode.pass("All " + summary.getMatchedRows() + 
            " rows matched exactly between legacy and modern systems");
        detailNode.pass("No missing rows in either system");
        detailNode.pass("No field-level differences detected");
    }
    
    /**
     * Log legacy-only rows
     */
    private void logLegacyOnlyRows(ExtentTest test, 
                                   List<ReportComparator.ReportRow> legacyOnlyRows) {
        
        ExtentTest legacyNode = test.createNode("Legacy-Only Rows (" + 
            legacyOnlyRows.size() + ")");
        legacyNode.warning("These rows exist in Legacy but are missing in Modern system");
        
        // Show first 20 rows
        int showCount = Math.min(20, legacyOnlyRows.size());
        
        for (int i = 0; i < showCount; i++) {
            ReportComparator.ReportRow row = legacyOnlyRows.get(i);
            ExtentTest rowNode = legacyNode.createNode("Legacy Line " + row.getLineNumber());
            
            // Create table for row data
            List<String[]> rowData = new ArrayList<>();
            rowData.add(new String[]{"Column", "Value"});
            
            row.getData().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> rowData.add(new String[]{
                    entry.getKey(), 
                    entry.getValue() != null ? entry.getValue() : "NULL"
                }));
            
            rowNode.warning(MarkupHelper.createTable(
                rowData.toArray(new String[0][])));
        }
        
        if (legacyOnlyRows.size() > showCount) {
            legacyNode.info("... and " + (legacyOnlyRows.size() - showCount) + 
                " more rows (showing first " + showCount + ")");
        }
    }
    
    /**
     * Log modern-only rows
     */
    private void logModernOnlyRows(ExtentTest test, 
                                   List<ReportComparator.ReportRow> modernOnlyRows) {
        
        ExtentTest modernNode = test.createNode("Modern-Only Rows (" + 
            modernOnlyRows.size() + ")");
        modernNode.warning("These rows exist in Modern but are missing in Legacy system");
        
        int showCount = Math.min(20, modernOnlyRows.size());
        
        for (int i = 0; i < showCount; i++) {
            ReportComparator.ReportRow row = modernOnlyRows.get(i);
            ExtentTest rowNode = modernNode.createNode("Modern Line " + row.getLineNumber());
            
            List<String[]> rowData = new ArrayList<>();
            rowData.add(new String[]{"Column", "Value"});
            
            row.getData().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> rowData.add(new String[]{
                    entry.getKey(), 
                    entry.getValue() != null ? entry.getValue() : "NULL"
                }));
            
            rowNode.warning(MarkupHelper.createTable(
                rowData.toArray(new String[0][])));
        }
        
        if (modernOnlyRows.size() > showCount) {
            modernNode.info("... and " + (modernOnlyRows.size() - showCount) + 
                " more rows (showing first " + showCount + ")");
        }
    }
    
    /**
     * Log field-level differences
     */
    private void logFieldDifferences(ExtentTest test, 
                                     List<ReportComparator.MatchedRow> matchedRows) {
        
        List<ReportComparator.MatchedRow> rowsWithDiffs = matchedRows.stream()
            .filter(m -> !m.hasNoDifferences())
            .toList();
        
        ExtentTest diffNode = test.createNode("Field Differences (" + 
            rowsWithDiffs.size() + ")");
        diffNode.warning("Matched rows with field-level differences");
        
        // Group differences by column
        Map<String, Integer> columnDiffCount = new HashMap<>();
        for (ReportComparator.MatchedRow match : rowsWithDiffs) {
            for (String column : match.getDifferences().keySet()) {
                columnDiffCount.merge(column, 1, Integer::sum);
            }
        }
        
        // Show column-level summary
        ExtentTest columnSummary = diffNode.createNode("Differences by Column");
        List<String[]> columnData = new ArrayList<>();
        columnData.add(new String[]{"Column", "# of Differences", "% of Matched Rows"});
        
        columnDiffCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                double percentage = (entry.getValue() * 100.0) / matchedRows.size();
                columnData.add(new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue()),
                    String.format("%.2f%%", percentage)
                });
            });
        
        columnSummary.info(MarkupHelper.createTable(
            columnData.toArray(new String[0][])));
        
        // Show detailed row differences (first 15)
        ExtentTest detailedDiffs = diffNode.createNode("Detailed Row Differences");
        int showCount = Math.min(15, rowsWithDiffs.size());
        
        for (int i = 0; i < showCount; i++) {
            ReportComparator.MatchedRow match = rowsWithDiffs.get(i);
            
            ExtentTest rowDiff = detailedDiffs.createNode(
                String.format("Legacy L%d ↔ Modern L%d (%d differences)",
                    match.getLegacyRow().getLineNumber(),
                    match.getModernRow().getLineNumber(),
                    match.getDifferences().size()));
            
            List<String[]> diffData = new ArrayList<>();
            diffData.add(new String[]{"Column", "Legacy Value", "Modern Value"});
            
            match.getDifferences().values().forEach(diff -> 
                diffData.add(new String[]{
                    diff.getColumn(),
                    diff.getLegacyValue() != null ? diff.getLegacyValue() : "NULL",
                    diff.getModernValue() != null ? diff.getModernValue() : "NULL"
                }));
            
            rowDiff.warning(MarkupHelper.createTable(
                diffData.toArray(new String[0][])));
        }
        
        if (rowsWithDiffs.size() > showCount) {
            detailedDiffs.info("... and " + (rowsWithDiffs.size() - showCount) + 
                " more rows with differences (showing first " + showCount + ")");
        }
    }
    
    /**
     * Log statistical analysis
     */
    private void logStatisticalAnalysis(ExtentTest test, 
                                       ReportComparator.ComparisonResult result) {
        
        ExtentTest statsNode = test.createNode("Statistical Analysis");
        
        ReportComparator.ComparisonSummary summary = result.getSummary();
        
        // Calculate match percentage
        int totalUnique = summary.getMatchedRows() + 
                         summary.getLegacyOnlyRows() + 
                         summary.getModernOnlyRows();
        
        double matchPercentage = totalUnique > 0 ? 
            (summary.getMatchedRows() * 100.0) / totalUnique : 0;
        
        double perfectMatchPercentage = summary.getMatchedRows() > 0 ?
            ((summary.getMatchedRows() - summary.getRowsWithDifferences()) * 100.0) / 
            summary.getMatchedRows() : 0;
        
        List<String[]> statsData = new ArrayList<>();
        statsData.add(new String[]{"Metric", "Value"});
        statsData.add(new String[]{"Row Match Rate", 
            String.format("%.2f%%", matchPercentage)});
        statsData.add(new String[]{"Perfect Match Rate", 
            String.format("%.2f%%", perfectMatchPercentage)});
        statsData.add(new String[]{"Data Completeness (Legacy)", 
            String.format("%.2f%%", 
                (summary.getMatchedRows() * 100.0) / summary.getTotalLegacyRows())});
        statsData.add(new String[]{"Data Completeness (Modern)", 
            String.format("%.2f%%", 
                (summary.getMatchedRows() * 100.0) / summary.getTotalModernRows())});
        
        statsNode.info(MarkupHelper.createTable(
            statsData.toArray(new String[0][])));
        
        // Recommendations
        ExtentTest recommendations = statsNode.createNode("Recommendations");
        
        if (summary.getLegacyOnlyRows() > 0) {
            recommendations.warning("→ Investigate data migration: " + 
                summary.getLegacyOnlyRows() + " legacy records not found in modern system");
        }
        
        if (summary.getModernOnlyRows() > 0) {
            recommendations.warning("→ Review new data: " + 
                summary.getModernOnlyRows() + " additional records in modern system");
        }
        
        if (summary.getRowsWithDifferences() > 0) {
            recommendations.warning("→ Validate transformation logic: " + 
                summary.getRowsWithDifferences() + " records have field mismatches");
        }
        
        if (matchPercentage < 95) {
            recommendations.fail("→ Critical: Row match rate below 95% threshold");
        } else if (matchPercentage < 99) {
            recommendations.warning("→ Warning: Row match rate below 99% target");
        } else if (summary.isPerfectMatch()) {
            recommendations.pass("→ Excellent: 100% parity achieved!");
        }
    }
    
    /**
     * Create a comparison test with custom configuration
     */
    public ExtentTest createComparisonTest(String testName, String description) {
        return extent.createTest(testName, description)
            .assignCategory("Report Comparison")
            .assignAuthor("QA Automation");
    }
    
    /**
     * Log configuration details
     */
    public void logConfiguration(ExtentTest test, ReportComparator.ComparisonConfig config) {
        ExtentTest configNode = test.createNode("Comparison Configuration");
        
        List<String[]> configData = new ArrayList<>();
        configData.add(new String[]{"Setting", "Value"});
        configData.add(new String[]{"Key Columns", 
            String.join(", ", config.keyColumns)});
        configData.add(new String[]{"Ignored Columns", 
            config.ignoreColumns.isEmpty() ? "None" : 
            String.join(", ", config.ignoreColumns)});
        configData.add(new String[]{"Case Sensitive", 
            String.valueOf(config.caseSensitive)});
        configData.add(new String[]{"Trim Whitespace", 
            String.valueOf(config.trimWhitespace)});
        configData.add(new String[]{"Fuzzy Match Threshold", 
            String.format("%.2f", config.fuzzyMatchThreshold)});
        
        configNode.info(MarkupHelper.createTable(
            configData.toArray(new String[0][])));
    }
    
    /**
     * Finalize and flush the report
     */
    public void flush() {
        extent.flush();
        System.out.println("ExtentReport generated at: " + reportPath);
    }
    
    /**
     * Add screenshot or attachment
     */
    public void addScreenshot(ExtentTest test, String screenshotPath, String title) {
        try {
            test.addScreenCaptureFromPath(screenshotPath, title);
        } catch (Exception e) {
            test.warning("Could not attach screenshot: " + e.getMessage());
        }
    }
}
