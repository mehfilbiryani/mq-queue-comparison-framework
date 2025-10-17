Goal:
Implement a Java Spring Boot service that continuously monitors a high-volume DynamoDB table of MQ messages, compares them, generates detailed reports, and sends notifications. The system must be robust with error handling for all possible failure scenarios.

Requirements
1️⃣ DynamoDB Integration

Read from table MQMessages with fields: id, programname, correlationid, message1, message2.

Messages may be JSON or XML.

Ignore configurable fields (e.g., timestamp, messageId, requestId) during comparison.

High-volume support:

Scan in batches and handle DynamoDB pagination.

Avoid re-comparing messages already processed using a separate ProcessedTracker table keyed by correlationid.

Error Handling:

Table not found → log and skip the run.

DynamoDB permission/network errors → log and retry next scheduled run.

Malformed or missing item fields → skip item and log in report.

2️⃣ Message Comparison

Compare messages intelligently:

JSON → recursively compare ignoring configured fields.

XML → use XMLUnit or equivalent to compare nodes, ignoring whitespace, comments, and configured nodes.

Capture detailed differences (fields, node values) for reporting.

Error Handling:

Malformed JSON/XML → include parsing error in Extent report.

Unexpected exceptions → log and continue with remaining messages.

3️⃣ Extent Report

Generate HTML report for every run.

For each record:

✅ Messages match → mark as passed.

❌ Messages differ → include human-readable differences.

⚠️ Parsing or comparison errors → include details in report.

Save report locally with timestamped filename.

4️⃣ S3 Integration

Optionally upload HTML report to S3.

Optionally generate public link or pre-signed URL for private buckets.

Configurable enable/disable via properties.

Error Handling:

Network errors, permission errors, bucket not found → log and continue with local report.

5️⃣ Email Notifications

Optionally send email with HTML report attached.

Configurable enable/disable and recipients.

Error Handling:

SMTP/authentication failures → log and continue without blocking other tasks.

6️⃣ Microsoft Teams Notifications

Optionally send Teams notification for mismatches.

Include clickable S3 report link.

Configurable enable/disable and webhook URL.

Error Handling:

Invalid webhook URL or network errors → log and continue.

7️⃣ Continuous Operation

Run continuously using @Scheduled (e.g., every 10 minutes).

Only process new/unprocessed messages using the tracker table.

Gracefully handle large volumes of incoming messages.

Error Handling:

Wrap entire scheduled method in try/catch to prevent job crash.

Log any uncaught exceptions with timestamp.

8️⃣ Configuration

Use application.properties for all settings:

DynamoDB tables and AWS region.

S3 bucket/folder and public URL flag.

Email enable/recipients.

Teams enable/webhook URL.

Fields to ignore during comparison.

Scheduler interval.

9️⃣ Logging & Reporting

Log all critical events, errors, and warnings to console/file.

Include all parsing, comparison, S3, email, and Teams errors in Extent report.

Distinguish between matched, mismatched, and error messages in report.

Deliverables

Complete Spring Boot application.

Fully implemented service class for scanning, comparing, reporting, and notifications.

Robust error handling for all layers (DynamoDB, JSON/XML parsing, S3, email, Teams, scheduler).

Sample application.properties with all configurable flags.

Maven dependencies for DynamoDB, S3, Jackson, XMLUnit, Extent Reports, Spring Mail, and JUnit 5.

Extent reports with detailed differences and error messages.

Optional: JUnit 5 test class to mock DynamoDB/S3 for unit testing.
