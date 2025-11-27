import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextFileComparator {

    private static ExtentReports extentReports;
    private static ExtentTest extentTest;

    // Main comparison result class
    public static class ComparisonResult {
        private double matchPercentage;
        private int totalLines;
        private int matchingLines;
        private int totalWords;
        private int matchingWords;
        private int totalCharacters;
        private int matchingCharacters;
        private List<LineDifference> differences;

        public ComparisonResult() {
            this.differences = new ArrayList<>();
        }

        // Getters
        public double getMatchPercentage() { return matchPercentage; }
        public int getTotalLines() { return totalLines; }
        public int getMatchingLines() { return matchingLines; }
        public int getTotalWords() { return totalWords; }
        public int getMatchingWords() { return matchingWords; }
        public int getTotalCharacters() { return totalCharacters; }
        public int getMatchingCharacters() { return matchingCharacters; }
        public List<LineDifference> getDifferences() { return differences; }

        // Setters
        public void setMatchPercentage(double matchPercentage) { this.matchPercentage = matchPercentage; }
        public void setTotalLines(int totalLines) { this.totalLines = totalLines; }
        public void setMatchingLines(int matchingLines) { this.matchingLines = matchingLines; }
        public void setTotalWords(int totalWords) { this.totalWords = totalWords; }
        public void setMatchingWords(int matchingWords) { this.matchingWords = matchingWords; }
        public void setTotalCharacters(int totalCharacters) { this.totalCharacters = totalCharacters; }
        public void setMatchingCharacters(int matchingCharacters) { this.matchingCharacters = matchingCharacters; }
        public void addDifference(LineDifference diff) { this.differences.add(diff); }

        @Override
        public String toString() {
            return String.format(
                "Match Percentage: %.2f%%\n" +
                "Lines: %d matching out of %d total (%.2f%%)\n" +
                "Words: %d matching out of %d total (%.2f%%)\n" +
                "Characters: %d matching out of %d total (%.2f%%)\n" +
                "Differences found: %d",
                matchPercentage,
                matchingLines, totalLines, totalLines > 0 ? (matchingLines * 100.0 / totalLines) : 0,
                matchingWords, totalWords, totalWords > 0 ? (matchingWords * 100.0 / totalWords) : 0,
                matchingCharacters, totalCharacters, totalCharacters > 0 ? (matchingCharacters * 100.0 / totalCharacters) : 0,
                differences.size()
            );
        }
    }

    // Line difference tracking
    public static class LineDifference {
        private int lineNumber;
        private String file1Line;
        private String file2Line;
        private DifferenceType type;

        public enum DifferenceType {
            MODIFIED, ADDED, REMOVED
        }

        public LineDifference(int lineNumber, String file1Line, String file2Line, DifferenceType type) {
            this.lineNumber = lineNumber;
            this.file1Line = file1Line;
            this.file2Line = file2Line;
            this.type = type;
        }

        public int getLineNumber() { return lineNumber; }
        public String getFile1Line() { return file1Line; }
        public String getFile2Line() { return file2Line; }
        public DifferenceType getType() { return type; }

        @Override
        public String toString() {
            return String.format("Line %d [%s]: '%s' vs '%s'", lineNumber, type, file1Line, file2Line);
        }
    }

    // Comparison options
    public static class ComparisonOptions {
        private boolean ignoreCase = false;
        private boolean ignoreWhitespace = false;
        private boolean ignoreEmptyLines = false;
        private boolean trimLines = false;
        private ComparisonLevel level = ComparisonLevel.LINE;

        public enum ComparisonLevel {
            LINE, WORD, CHARACTER
        }

        public ComparisonOptions setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public ComparisonOptions setIgnoreWhitespace(boolean ignoreWhitespace) {
            this.ignoreWhitespace = ignoreWhitespace;
            return this;
        }

        public ComparisonOptions setIgnoreEmptyLines(boolean ignoreEmptyLines) {
            this.ignoreEmptyLines = ignoreEmptyLines;
            return this;
        }

        public ComparisonOptions setTrimLines(boolean trimLines) {
            this.trimLines = trimLines;
            return this;
        }

        public ComparisonOptions setComparisonLevel(ComparisonLevel level) {
            this.level = level;
            return this;
        }

        public boolean isIgnoreCase() { return ignoreCase; }
        public boolean isIgnoreWhitespace() { return ignoreWhitespace; }
        public boolean isIgnoreEmptyLines() { return ignoreEmptyLines; }
        public boolean isTrimLines() { return trimLines; }
        public ComparisonLevel getLevel() { return level; }
    }

    /**
     * Initialize Extent Reports
     */
    public static void initializeExtentReports(String reportPath) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("File Comparison Report");
        sparkReporter.config().setReportName("Text File Comparison Analysis");
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Application", "Text File Comparator");
        extentReports.setSystemInfo("Environment", "Test");
        extentReports.setSystemInfo("User", System.getProperty("user.name"));
    }

    /**
     * Create a new test in the report
     */
    public static ExtentTest createTest(String testName, String description) {
        extentTest = extentReports.createTest(testName, description);
        return extentTest;
    }

    /**
     * Compare two text files and generate Extent Report
     */
    public static ComparisonResult compareFilesWithReport(String filePath1, String filePath2, 
                                                          ComparisonOptions options, ExtentTest test) 
            throws IOException {
        
        if (test == null) {
            test = extentTest;
        }

        test.info("Starting file comparison");
        test.info("<b>File 1:</b> " + filePath1);
        test.info("<b>File 2:</b> " + filePath2);

        // Log comparison options
        StringBuilder optionsLog = new StringBuilder("<b>Comparison Options:</b><br/>");
        optionsLog.append("Comparison Level: ").append(options.getLevel()).append("<br/>");
        optionsLog.append("Ignore Case: ").append(options.isIgnoreCase()).append("<br/>");
        optionsLog.append("Ignore Whitespace: ").append(options.isIgnoreWhitespace()).append("<br/>");
        optionsLog.append("Ignore Empty Lines: ").append(options.isIgnoreEmptyLines()).append("<br/>");
        optionsLog.append("Trim Lines: ").append(options.isTrimLines());
        test.info(optionsLog.toString());

        try {
            // Edge case: Check if files exist
            if (!Files.exists(Paths.get(filePath1))) {
                test.fail("File not found: " + filePath1);
                throw new FileNotFoundException("File not found: " + filePath1);
            }
            if (!Files.exists(Paths.get(filePath2))) {
                test.fail("File not found: " + filePath2);
                throw new FileNotFoundException("File not found: " + filePath2);
            }

            test.pass("Both files exist and are accessible");

            // Read files
            List<String> lines1 = readAndProcessLines(filePath1, options);
            List<String> lines2 = readAndProcessLines(filePath2, options);

            test.info("File 1 lines read: " + lines1.size());
            test.info("File 2 lines read: " + lines2.size());

            ComparisonResult result = new ComparisonResult();

            // Edge case: Both files are empty
            if (lines1.isEmpty() && lines2.isEmpty()) {
                result.setMatchPercentage(100.0);
                test.pass("<b>Both files are empty - 100% match</b>");
                logComparisonSummary(result, test);
                return result;
            }

            // Edge case: One file is empty
            if (lines1.isEmpty() || lines2.isEmpty()) {
                result.setMatchPercentage(0.0);
                result.setTotalLines(Math.max(lines1.size(), lines2.size()));
                test.warning("<b>One file is empty - 0% match</b>");
                logComparisonSummary(result, test);
                return result;
            }

            // Perform comparison
            switch (options.getLevel()) {
                case CHARACTER:
                    result = compareByCharacters(lines1, lines2, result);
                    break;
                case WORD:
                    result = compareByWords(lines1, lines2, result);
                    break;
                case LINE:
                default:
                    result = compareByLines(lines1, lines2, result, options);
                    break;
            }

            // Log results to Extent Report
            logComparisonSummary(result, test);
            logDetailedDifferences(result, test);

            // Set test status based on match percentage
            if (result.getMatchPercentage() == 100.0) {
                test.pass("<b>Files are identical - 100% match</b>");
            } else if (result.getMatchPercentage() >= 80.0) {
                test.warning(String.format("<b>Files are similar - %.2f%% match</b>", result.getMatchPercentage()));
            } else if (result.getMatchPercentage() >= 50.0) {
                test.warning(String.format("<b>Files are partially similar - %.2f%% match</b>", result.getMatchPercentage()));
            } else {
                test.fail(String.format("<b>Files are significantly different - %.2f%% match</b>", result.getMatchPercentage()));
            }

            return result;

        } catch (IOException e) {
            test.fail("Error during file comparison: " + e.getMessage());
            test.fail(MarkupHelper.createCodeBlock(getStackTraceAsString(e)));
            throw e;
        }
    }

    /**
     * Overloaded method with default options
     */
    public static ComparisonResult compareFilesWithReport(String filePath1, String filePath2, ExtentTest test) 
            throws IOException {
        return compareFilesWithReport(filePath1, filePath2, new ComparisonOptions(), test);
    }

    /**
     * Log comparison summary to Extent Report
     */
    private static void logComparisonSummary(ComparisonResult result, ExtentTest test) {
        StringBuilder summary = new StringBuilder();
        summary.append("<div style='background-color: #f0f0f0; padding: 15px; border-radius: 5px;'>");
        summary.append("<h3 style='color: #333;'>Comparison Summary</h3>");
        summary.append("<table style='width: 100%; border-collapse: collapse;'>");
        
        // Overall match percentage with color coding
        String matchColor = getColorForPercentage(result.getMatchPercentage());
        summary.append(String.format(
            "<tr><td style='padding: 8px; border: 1px solid #ddd;'><b>Overall Match Percentage</b></td>" +
            "<td style='padding: 8px; border: 1px solid #ddd; background-color: %s; font-size: 18px; font-weight: bold;'>%.2f%%</td></tr>",
            matchColor, result.getMatchPercentage()
        ));
        
        // Line statistics
        if (result.getTotalLines() > 0) {
            double linePercentage = (result.getMatchingLines() * 100.0 / result.getTotalLines());
            summary.append(String.format(
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><b>Line Match</b></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>%d / %d (%.2f%%)</td></tr>",
                result.getMatchingLines(), result.getTotalLines(), linePercentage
            ));
        }
        
        // Word statistics
        if (result.getTotalWords() > 0) {
            double wordPercentage = (result.getMatchingWords() * 100.0 / result.getTotalWords());
            summary.append(String.format(
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><b>Word Match</b></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>%d / %d (%.2f%%)</td></tr>",
                result.getMatchingWords(), result.getTotalWords(), wordPercentage
            ));
        }
        
        // Character statistics
        if (result.getTotalCharacters() > 0) {
            double charPercentage = (result.getMatchingCharacters() * 100.0 / result.getTotalCharacters());
            summary.append(String.format(
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><b>Character Match</b></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>%d / %d (%.2f%%)</td></tr>",
                result.getMatchingCharacters(), result.getTotalCharacters(), charPercentage
            ));
        }
        
        // Differences count
        summary.append(String.format(
            "<tr><td style='padding: 8px; border: 1px solid #ddd;'><b>Total Differences</b></td>" +
            "<td style='padding: 8px; border: 1px solid #ddd;'>%d</td></tr>",
            result.getDifferences().size()
        ));
        
        summary.append("</table></div>");
        
        test.info(summary.toString());
    }

    /**
     * Log detailed differences to Extent Report
     */
    private static void logDetailedDifferences(ComparisonResult result, ExtentTest test) {
        if (result.getDifferences().isEmpty()) {
            test.pass("No differences found - Files are identical!");
            return;
        }

        test.info("<h3>Detailed Differences</h3>");
        
        // Limit differences shown in report to avoid large reports
        int maxDifferencesToShow = Math.min(result.getDifferences().size(), 50);
        
        StringBuilder diffTable = new StringBuilder();
        diffTable.append("<table style='width: 100%; border-collapse: collapse; font-size: 12px;'>");
        diffTable.append("<thead><tr style='background-color: #4CAF50; color: white;'>");
        diffTable.append("<th style='padding: 8px; border: 1px solid #ddd;'>Line #</th>");
        diffTable.append("<th style='padding: 8px; border: 1px solid #ddd;'>Type</th>");
        diffTable.append("<th style='padding: 8px; border: 1px solid #ddd;'>File 1</th>");
        diffTable.append("<th style='padding: 8px; border: 1px solid #ddd;'>File 2</th>");
        diffTable.append("</tr></thead><tbody>");

        for (int i = 0; i < maxDifferencesToShow; i++) {
            LineDifference diff = result.getDifferences().get(i);
            String rowColor = i % 2 == 0 ? "#f9f9f9" : "#ffffff";
            String typeColor = getColorForDifferenceType(diff.getType());
            
            diffTable.append(String.format(
                "<tr style='background-color: %s;'>" +
                "<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>%d</td>" +
                "<td style='padding: 8px; border: 1px solid #ddd; background-color: %s;'><b>%s</b></td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>%s</td>" +
                "<td style='padding: 8px; border: 1px solid #ddd;'>%s</td>" +
                "</tr>",
                rowColor,
                diff.getLineNumber(),
                typeColor, diff.getType(),
                escapeHtml(diff.getFile1Line()),
                escapeHtml(diff.getFile2Line())
            ));
        }
        
        diffTable.append("</tbody></table>");
        
        if (result.getDifferences().size() > maxDifferencesToShow) {
            diffTable.append(String.format(
                "<p><i>Showing first %d of %d differences. Check detailed logs for complete list.</i></p>",
                maxDifferencesToShow, result.getDifferences().size()
            ));
        }
        
        test.info(diffTable.toString());
    }

    /**
     * Get color based on match percentage
     */
    private static String getColorForPercentage(double percentage) {
        if (percentage >= 95) return "#4CAF50"; // Green
        if (percentage >= 80) return "#8BC34A"; // Light Green
        if (percentage >= 60) return "#FFC107"; // Amber
        if (percentage >= 40) return "#FF9800"; // Orange
        return "#F44336"; // Red
    }

    /**
     * Get color based on difference type
     */
    private static String getColorForDifferenceType(LineDifference.DifferenceType type) {
        switch (type) {
            case MODIFIED: return "#FFC107"; // Amber
            case ADDED: return "#4CAF50"; // Green
            case REMOVED: return "#F44336"; // Red
            default: return "#9E9E9E"; // Grey
        }
    }

    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) return "&lt;empty&gt;";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Get stack trace as string
     */
    private static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Finalize and flush the report
     */
    public static void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    /**
     * Read and process lines based on options
     */
    private static List<String> readAndProcessLines(String filePath, ComparisonOptions options) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        return lines.stream()
                .map(line -> {
                    if (options.isTrimLines()) {
                        line = line.trim();
                    }
                    if (options.isIgnoreWhitespace()) {
                        line = line.replaceAll("\\s+", "");
                    }
                    if (options.isIgnoreCase()) {
                        line = line.toLowerCase();
                    }
                    return line;
                })
                .filter(line -> !options.isIgnoreEmptyLines() || !line.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Line by line comparison
     */
    private static ComparisonResult compareByLines(List<String> lines1, List<String> lines2, 
                                                   ComparisonResult result, ComparisonOptions options) {
        int maxLines = Math.max(lines1.size(), lines2.size());
        int matchingLines = 0;

        result.setTotalLines(maxLines);

        for (int i = 0; i < maxLines; i++) {
            String line1 = i < lines1.size() ? lines1.get(i) : null;
            String line2 = i < lines2.size() ? lines2.get(i) : null;

            if (line1 != null && line2 != null) {
                if (line1.equals(line2)) {
                    matchingLines++;
                } else {
                    result.addDifference(new LineDifference(
                        i + 1, line1, line2, LineDifference.DifferenceType.MODIFIED));
                }
            } else if (line1 == null) {
                result.addDifference(new LineDifference(
                    i + 1, "", line2, LineDifference.DifferenceType.ADDED));
            } else {
                result.addDifference(new LineDifference(
                    i + 1, line1, "", LineDifference.DifferenceType.REMOVED));
            }
        }

        result.setMatchingLines(matchingLines);
        result.setMatchPercentage(maxLines > 0 ? (matchingLines * 100.0 / maxLines) : 0);

        return result;
    }

    /**
     * Word by word comparison
     */
    private static ComparisonResult compareByWords(List<String> lines1, List<String> lines2, 
                                                   ComparisonResult result) {
        String text1 = String.join(" ", lines1);
        String text2 = String.join(" ", lines2);

        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        int maxWords = Math.max(words1.length, words2.length);

        // Use LCS algorithm
        int[][] dp = new int[words1.length + 1][words2.length + 1];

        for (int i = 1; i <= words1.length; i++) {
            for (int j = 1; j <= words2.length; j++) {
                if (words1[i - 1].equals(words2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        int matchingWords = dp[words1.length][words2.length];

        result.setTotalWords(maxWords);
        result.setMatchingWords(matchingWords);
        result.setMatchPercentage(maxWords > 0 ? (matchingWords * 100.0 / maxWords) : 0);

        return result;
    }

    /**
     * Character by character comparison
     */
    private static ComparisonResult compareByCharacters(List<String> lines1, List<String> lines2, 
                                                        ComparisonResult result) {
        String text1 = String.join("\n", lines1);
        String text2 = String.join("\n", lines2);

        int maxChars = Math.max(text1.length(), text2.length());

        // Use Levenshtein distance
        int[][] dp = new int[text1.length() + 1][text2.length() + 1];

        for (int i = 0; i <= text1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= text2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= text1.length(); i++) {
            for (int j = 1; j <= text2.length(); j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }

        int levenshteinDistance = dp[text1.length()][text2.length()];
        int matchingChars = maxChars - levenshteinDistance;

        result.setTotalCharacters(maxChars);
        result.setMatchingCharacters(matchingChars);
        result.setMatchPercentage(maxChars > 0 ? (matchingChars * 100.0 / maxChars) : 0);

        return result;
    }

    // Demo main method
    public static void main(String[] args) {
        try {
            // Initialize Extent Reports
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportPath = "FileComparisonReport_" + timestamp + ".html";
            initializeExtentReports(reportPath);

            // Create sample files
            Files.writeString(Paths.get("file1.txt"), 
                "Hello World\nThis is a test\nJava programming\nFile comparison\nExtent Reports");
            Files.writeString(Paths.get("file2.txt"), 
                "Hello World\nThis is a demo\nJava programming\nFile comparison\nExtent Reports\nExtra line");

            // Test 1: Line-by-Line Comparison
            ExtentTest test1 = createTest("Line-by-Line Comparison", "Default comparison mode");
            ComparisonResult result1 = compareFilesWithReport("file1.txt", "file2.txt", test1);

            // Test 2: Word Comparison
            ExtentTest test2 = createTest("Word-Level Comparison", "Comparing files at word level");
            ComparisonOptions wordOptions = new ComparisonOptions()
                .setComparisonLevel(ComparisonOptions.ComparisonLevel.WORD);
            ComparisonResult result2 = compareFilesWithReport("file1.txt", "file2.txt", wordOptions, test2);

            // Test 3: Character Comparison
            ExtentTest test3 = createTest("Character-Level Comparison", "Comparing files at character level");
            ComparisonOptions charOptions = new ComparisonOptions()
                .setComparisonLevel(ComparisonOptions.ComparisonLevel.CHARACTER);
            ComparisonResult result3 = compareFilesWithReport("file1.txt", "file2.txt", charOptions, test3);

            // Test 4: Case-Insensitive Comparison
            ExtentTest test4 = createTest("Case-Insensitive Comparison", "Ignoring case differences");
            ComparisonOptions caseOptions = new ComparisonOptions()
                .setIgnoreCase(true)
                .setTrimLines(true);
            ComparisonResult result4 = compareFilesWithReport("file1.txt", "file2.txt", caseOptions, test4);

            // Test 5: Identical Files
            Files.writeString(Paths.get("file3.txt"), "Same content\nLine 2\nLine 3");
            Files.writeString(Paths.get("file4.txt"), "Same content\nLine 2\nLine 3");
            ExtentTest test5 = createTest("Identical Files Comparison", "Testing with identical files");
            ComparisonResult result5 = compareFilesWithReport("file3.txt", "file4.txt", test5);

            // Flush and generate report
            flushReport();

            System.out.println("Extent Report generated successfully: " + reportPath);

            // Cleanup
            Files.deleteIfExists(Paths.get("file1.txt"));
            Files.deleteIfExists(Paths.get("file2.txt"));
            Files.deleteIfExists(Paths.get("file3.txt"));
            Files.deleteIfExists(Paths.get("file4.txt"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
