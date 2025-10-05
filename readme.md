# IBM MQ Queue Comparison Test Framework

A comprehensive, production-ready JUnit 5 test framework for comparing messages between two IBM MQ queues with detailed reporting, error handling, and environment-based configuration.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Test Scenarios](#test-scenarios)
- [Error Handling](#error-handling)
- [Logging](#logging)
- [Reports](#reports)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## Features

### Core Capabilities
- **18 Comprehensive Test Scenarios** - Message count, payload, metadata, ordering, duplicates, and more
- **Non-Destructive Testing** - Browse mode reads messages without removing them from queues
- **Environment-Based Configuration** - Separate configs for DEV, QA, UAT, and PROD
- **Advanced Error Handling** - Detailed error messages with troubleshooting guidance for 20+ MQ error codes
- **Extent Reports Integration** - Beautiful HTML reports with pass/fail/warning status
- **Console Logging** - Formatted console output with timestamps and visual separators
- **Configuration Management** - Type-safe configuration with validation and password masking
- **Optional Queue Bootstrap** - Automatically seed test queues with sample data

# IBM MQ Queue Comparison Test Framework

A comprehensive, production-ready JUnit 5 test framework for comparing messages between two IBM MQ queues with detailed reporting, error handling, and environment-based configuration.

[![Java](https://img.shields.io/badge/Java-11%2B-orange)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![JUnit](https://img.shields.io/badge/JUnit-5.10.0-green)](https://junit.org/junit5/)
[![IBM MQ](https://img.shields.io/badge/IBM%20MQ-9.3%2B-red)](https://www.ibm.com/products/mq)

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Test Scenarios](#test-scenarios)
- [Error Handling](#error-handling)
- [Logging & Reports](#logging--reports)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)

---

## Features

### Core Capabilities
- **18 Comprehensive Test Scenarios** - Message count, payload, metadata, ordering, duplicates, and more
- **Non-Destructive Testing** - Browse mode reads messages without removing them from queues
- **Environment-Based Configuration** - Separate configs for DEV, QA, UAT, and PROD
- **Advanced Error Handling** - Detailed error messages with troubleshooting guidance for 20+ MQ error codes
- **Extent Reports Integration** - Beautiful HTML reports with pass/fail/warning status
- **Console Logging** - Formatted console output with timestamps and visual separators
- **Configuration Management** - Type-safe configuration with validation and password masking
- **Optional Queue Bootstrap** - Automatically seed test queues with sample data

### Test Coverage
1. ✅ Message Count Comparison
2. ✅ Payload Content Validation
3. ✅ Message ID Analysis
4. ✅ Correlation ID Matching
5. ✅ Priority Verification
6. ✅ Message Ordering Consistency
7. ✅ Empty Queue Detection
8. ✅ Message Format Validation
9. ✅ Timestamp Comparison (with tolerance)
10. ✅ Custom Properties Comparison
11. ✅ Payload Length Validation
12. ✅ Payload Structure Detection (JSON/XML)
13. ✅ Duplicate Message Detection (Queue 1)
14. ✅ Duplicate Message Detection (Queue 2)
15. ✅ Sequence Validation (Queue 1)
16. ✅ Sequence Validation (Queue 2)
17. ✅ Payload Checksum Comparison
18. ✅ Statistical Summary & Analytics

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java JDK** | 11 or higher | Runtime environment |
| **Apache Maven** | 3.6 or higher | Build and dependency management |
| **IBM MQ Server** | 9.x or higher | Message queue server |
| **IBM MQ Client** | 9.3.3.0 | MQ client libraries (included via Maven) |

### Verify Prerequisites

```bash
# Check Java version
java -version
# Expected: java version "11.0.x" or higher

# Check Maven version
mvn -version
# Expected: Apache Maven 3.6.x or higher

# Check IBM MQ installation (if local)
dspmqver
# Or check if MQ server is accessible
telnet your-mq-host 1414
```

---

## Installation

### Step 1: Clone or Download the Project

**Option A: Clone from Git**
```bash
git clone https://github.com/your-repo/mq-queue-comparison-framework.git
cd mq-queue-comparison-framework
```

**Option B: Create from Scratch**
```bash
# Create project directory
mkdir mq-queue-comparison-framework
cd mq-queue-comparison-framework

# Create directory structure
mkdir -p src/main/java/com/mq/test/{config,model,util,comparator,report,error}
mkdir -p src/test/java/com/mq/test
mkdir -p src/test/resources/config
```

### Step 2: Create pom.xml

Create `pom.xml` in the project root directory:

```bash
# Windows
notepad pom.xml

# Linux/Mac
nano pom.xml
```

Copy and paste this content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mq.test</groupId>
    <artifactId>mq-queue-comparison-framework</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>IBM MQ Queue Comparison Framework</name>
    <description>Comprehensive test framework for comparing messages between IBM MQ queues</description>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
        <ibm.mq.version>9.3.3.0</ibm.mq.version>
        <extentreports.version>5.1.1</extentreports.version>
    </properties>

    <dependencies>
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- IBM MQ -->
        <dependency>
            <groupId>com.ibm.mq</groupId>
            <artifactId>com.ibm.mq.allclient</artifactId>
            <version>${ibm.mq.version}</version>
        </dependency>

        <!-- Extent Reports -->
        <dependency>
            <groupId>com.aventstack</groupId>
            <artifactId>extentreports</artifactId>
            <version>${extentreports.version}</version>
        </dependency>

        <!-- SLF4J for logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 3: Download Dependencies

```bash
mvn clean install
```

This will download all required dependencies (~150MB). First run takes 5-10 minutes.

### Step 4: Copy Java Source Files

Copy all Java files from the artifacts into their respective directories:

**Main Source Files** (`src/main/java/com/mq/test/`):
- `config/MQConnectionConfig.java`
- `config/ConfigurationManager.java`
- `config/QueueConfiguration.java`
- `config/TestConfiguration.java`
- `model/MQMessage.java`
- `model/ComparisonResult.java`
- `util/MQMessageReader.java`
- `util/TestLogger.java`
- `comparator/MessageComparator.java`
- `report/ExtentReportManager.java`
- `error/MQErrorHandler.java`

**Test Files** (`src/test/java/com/mq/test/`):
- `MQQueueComparisonTest.java`

### Step 5: Verify Installation

```bash
# Compile the project
mvn clean compile

# Expected output:
# [INFO] BUILD SUCCESS
```

If you see compilation errors, verify all Java files are in the correct directories.

---

## Configuration

### Step 1: Create Configuration Files

Create environment-specific property files in `src/test/resources/config/`:

```bash
# Windows
mkdir src\test\resources\config
notepad src\test\resources\config\dev.properties

# Linux/Mac
mkdir -p src/test/resources/config
nano src/test/resources/config/dev.properties
```

### Step 2: Configure Development Environment

**File: `src/test/resources/config/dev.properties`**

```properties
# ============================================
# Development Environment Configuration
# ============================================

# MQ Connection Settings
mq.host=localhost
mq.port=1414
mq.queue.manager=QM1
mq.channel=DEV.ADMIN.SVRCONN
mq.username=admin
mq.password=password

# Queue Names
queue1.name=DEV.QUEUE1
queue2.name=DEV.QUEUE2

# Queue Reading Settings
queue.max.messages=1000
queue.browse.mode=true

# Test Configuration
report.output.path=target/reports/dev/ExtentReport.html
timestamp.tolerance.ms=5000
console.logging.enabled=true
skip.on.setup.failure=true

# Environment Metadata
environment.name=Development
environment.description=Local development environment
```

### Step 3: Update Configuration for Your Environment

Edit `dev.properties` and update these values:

1. **mq.host** - Your MQ server hostname or IP
2. **mq.port** - MQ listener port (default: 1414)
3. **mq.queue.manager** - Your Queue Manager name
4. **mq.channel** - Channel name (e.g., SYSTEM.DEF.SVRCONN)
5. **mq.username** - MQ username
6. **mq.password** - MQ password
7. **queue1.name** - First queue to compare
8. **queue2.name** - Second queue to compare

### Step 4: Create Additional Environment Files

Repeat for other environments:

```bash
# QA Environment
notepad src\test\resources\config\qa.properties

# UAT Environment
notepad src\test\resources\config\uat.properties

# Production Environment
notepad src\test\resources\config\prod.properties
```

Use the same property structure but with environment-specific values.

### Step 5: Verify Configuration

```bash
# Test configuration loading
mvn test -Dtest.environment=dev -Dtest=MQQueueComparisonTest#testMessageCount

# Check console output for:
# ✓ Loaded configuration for environment: dev
```

---

## Usage

### Basic Usage

#### Run All Tests (Default: DEV environment)

```bash
mvn clean test
```

#### Run Tests for Specific Environment

```bash
# QA Environment
mvn clean test -Dtest.environment=qa

# UAT Environment
mvn clean test -Dtest.environment=uat

# Production Environment
mvn clean test -Dtest.environment=prod
```

#### Run Specific Test

```bash
# Run only message count comparison
mvn test -Dtest=MQQueueComparisonTest#testMessageCount

# Run only payload comparison
mvn test -Dtest=MQQueueComparisonTest#testPayloadComparison
```

#### Run Multiple Specific Tests

```bash
mvn test -Dtest=MQQueueComparisonTest#testMessageCount,testPayloadComparison
```

### Advanced Usage

#### Set Environment via Environment Variable

**Windows:**
```cmd
set TEST_ENVIRONMENT=qa
mvn test
```

**Linux/Mac:**
```bash
export TEST_ENVIRONMENT=qa
mvn test
```

#### Skip Tests on Setup Failure

By default, tests are skipped if setup fails. To run tests anyway:

Edit your `.properties` file:
```properties
skip.on.setup.failure=false
```

#### Custom Report Location

```bash
mvn test -Dreport.output.path=custom/path/report.html
```

#### Increase Timestamp Tolerance

For slow networks or high-latency environments:

Edit your `.properties` file:
```properties
timestamp.tolerance.ms=30000
```

### Output Locations

After running tests:

```
target/
├── reports/
│   ├── dev/
│   │   └── ExtentReport.html          ← DEV environment report
│   ├── qa/
│   │   └── ExtentReport.html          ← QA environment report
│   └── prod/
│       └── ExtentReport.html          ← PROD environment report
├── surefire-reports/                  ← JUnit XML reports
└── test-classes/                      ← Compiled test classes
```

### View Reports

**Option 1: Open in Browser**
```bash
# Windows
start target/reports/dev/ExtentReport.html

# Linux
xdg-open target/reports/dev/ExtentReport.html

# Mac
open target/reports/dev/ExtentReport.html
```

**Option 2: Using HTTP Server**
```bash
# Using Python
cd target/reports/dev
python -m http.server 8080

# Then open: http://localhost:8080/ExtentReport.html
```

---

## Test Scenarios

### Complete Test Suite

| Order | Test Name | Description | Assertion Type |
|-------|-----------|-------------|----------------|
| 1 | Message Count | Verifies both queues have same number of messages | Hard Assert |
| 2 | Payload Content | Compares message payloads byte-by-byte | Hard Assert |
| 3 | Message IDs | Checks if message IDs match | Warning Only |
| 4 | Correlation IDs | Validates correlation ID consistency | Hard Assert |
| 5 | Priorities | Ensures message priorities are identical | Hard Assert |
| 6 | Ordering | Verifies message ordering consistency | Hard Assert |
| 7 | Empty Queues | Detects empty queues | Hard Assert |
| 8 | Formats | Compares message format types | Hard Assert |
| 9 | Timestamps | Checks timestamp differences (with tolerance) | Warning Only |
| 10 | Properties | Validates custom message properties | Hard Assert |
| 11 | Payload Length | Compares payload sizes | Hard Assert |
| 12 | Structure | Detects payload format (JSON/XML) | Hard Assert |
| 13 | Duplicates Q1 | Finds duplicate messages in Queue 1 | Warning Only |
| 14 | Duplicates Q2 | Finds duplicate messages in Queue 2 | Warning Only |
| 15 | Sequence Q1 | Validates message sequence in Queue 1 | Warning Only |
| 16 | Sequence Q2 | Validates message sequence in Queue 2 | Warning Only |
| 17 | Checksums | Compares payload checksums | Hard Assert |
| 18 | Statistics | Generates summary statistics | Info Only |

### Test Execution Flow

```
┌─────────────────────────────────────┐
│  1. Load Environment Configuration  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  2. Initialize MQ Connection        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  3. Read Messages from Queue 1      │
│     (Browse Mode - Non-Destructive) │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  4. Read Messages from Queue 2      │
│     (Browse Mode - Non-Destructive) │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  5. Execute 18 Comparison Tests     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  6. Generate Extent Report          │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  7. Display Console Summary         │
└─────────────────────────────────────┘
```

---

## Error Handling

### Supported Error Types

The framework handles and provides guidance for:

#### MQ-Specific Errors (20+ codes)

| Error Code | Description | Auto-Guidance |
|------------|-------------|---------------|
| 2035 | Authentication Failed | Check username/password |
| 2059 | Queue Manager Unavailable | Start Queue Manager |
| 2538 | Host Not Available | Check network/hostname |
| 2009 | Connection Broken | Check network stability |
| 2085 | Queue Not Found | Verify queue name exists |
| 2393 | Channel Not Available | Check channel configuration |
| 2397 | Channel Stopped | Start the channel |
| 2161 | Queue Full | Clear space or increase limit |
| 2033 | No Messages Available | Queue is empty |
| ... | | And 10+ more |

#### Network Errors

| Error Type | Description | Auto-Guidance |
|------------|-------------|---------------|
| UnknownHostException | DNS resolution failed | Check hostname, DNS, hosts file |
| ConnectException | Connection refused | Check firewall, MQ listener running |
| SocketTimeoutException | Connection timeout | Check network, VPN, latency |
| IOException | I/O error | Check network stability, SSL config |

### Error Output Example

```
╔════════════════════════════════════════════════════════════════╗
║ ERROR: Connection Refused                                      ║
╠════════════════════════════════════════════════════════════════╣
║ Cannot connect to MQ server at localhost:1414                 ║
║                                                                ║
║ Possible Solutions:                                            ║
║ 1. Verify MQ Queue Manager is running                         ║
║ 2. Check if the port number is correct (default: 1414)        ║
║ 3. Verify firewall rules allow connection to this port        ║
║ 4. Ensure MQ listener is active                               ║
╚════════════════════════════════════════════════════════════════╝
```

---

## Logging & Reports

### Console Output

Beautiful formatted console output with:
- Timestamps on every log entry
- Visual separators and banners
- Color-coded status (INFO, PASS, FAIL, WARN)
- Configuration summary
- Test execution progress
- Final summary with statistics

### Extent Reports

Rich HTML reports featuring:
- **Dashboard** - Overall test statistics
- **Test Details** - Pass/Fail status for each test
- **Comparison Results** - Detailed differences found
- **Timestamps** - Execution timeline
- **System Info** - Environment details
- **Screenshots** - Error details
- **Trends** - Historical comparison (if enabled)

### Report Features

- Interactive navigation
- Filtering by status (Pass/Fail/Skip)
- Search functionality
- Export capabilities
- Mobile-responsive design
- Dark/Light theme

---

## CI/CD Integration

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'qa', 'uat'], description: 'Target environment')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/your-repo/mq-queue-comparison-framework.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh "mvn test -Dtest.environment=${params.ENVIRONMENT}"
            }
        }
        
        stage('Publish Reports') {
            steps {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: "target/reports/${params.ENVIRONMENT}",
                    reportFiles: 'ExtentReport.html',
                    reportName: 'MQ Comparison Report'
                ])
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

### GitLab CI

```yaml
stages:
  - test
  - report

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

test:dev:
  stage: test
  script:
    - mvn clean test -Dtest.environment=dev
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/reports/dev/
    expire_in: 30 days

test:qa:
  stage: test
  script:
    - mvn clean test -Dtest.environment=qa
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/reports/qa/
    expire_in: 30 days
  only:
    - develop

pages:
  stage: report
  script:
    - mkdir -p public
    - cp -r target/reports/* public/
  artifacts:
    paths:
      - public
  only:
    - main
```

### GitHub Actions

```yaml
name: MQ Queue Comparison Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'dev'
        type: choice
        options:
          - dev
          - qa
          - uat

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: maven
    
    - name: Run Tests
      run: mvn clean test -Dtest.environment=${{ github.event.inputs.environment || 'dev' }}
    
    - name: Publish Test Report
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-report
        path: target/reports/
    
    - name: Publish Test Results
      uses: EnricoMi/publish-unit-test-result-action@v2
      if: always()
      with:
        files: target/surefire-reports/*.xml
```

---

## Troubleshooting

### Common Issues

#### 1. "No sources to compile"

**Problem**: Maven can't find Java files

**Solution**:
```bash
# Verify directory structure
dir /s *.java  # Windows
find . -name "*.java"  # Linux/Mac

# Ensure files are in correct location:
# src/main/java/com/mq/test/**/*.java
# src/test/java/com/mq/test/*.java
```

#### 2. "Configuration file not found"

**Problem**: Property files missing

**Solution**:
```bash
# Create resources directory
mkdir src\test\resources\config

# Create dev.properties
notepad src\test\resources\config\dev.properties

# Verify file exists
dir src\test\resources\config\dev.properties
```

#### 3. "MQRC_NOT_AUTHORIZED (2035)"

**Problem**: Authentication failed

**Solution**:
- Verify username/password in properties file
- Check MQ user permissions
- Ensure channel security allows connection

```bash
# Check MQ channel status
runmqsc QM1
DISPLAY CHANNEL(YOUR.CHANNEL)
```

#### 4. "Connection refused"

**Problem**: Can't connect to MQ server

**Solution**:
```bash
# 1. Check if Queue Manager is running
dspmq

# 2. Check listener status
runmqsc QM1
DISPLAY LISTENER(*)

# 3. Test connectivity
telnet your-mq-host 1414

# 4. Check firewall
# Windows: Check Windows Firewall
# Linux: sudo iptables -L
```

#### 5. "Tests skipped"

**Problem**: All tests show as skipped

**Solution**:
- Setup failed - check console output for errors
- Fix MQ connection issues
- Verify queues exist and are accessible

#### 6. "ClassNotFoundException: MQQueueSetup"

**Problem**: Bootstrap class not found (not an error)

**Solution**: This is expected if you don't have the optional MQQueueSetup class. The framework continues normally without bootstrapping.

### Debug Mode

Enable detailed logging:

```bash
mvn test -X -Dtest.environment=dev
```

### Get Help

1. Check console output for detailed error messages
2. Review Extent Report for test failure details
3. Enable Maven debug mode with `-X` flag
4. Check MQ error logs: `/var/mqm/qmgrs/QM1/errors/AMQERR01.LOG`

---

## Project Structure

```
mq-queue-comparison-framework/
│
├── pom.xml                                      # Maven configuration
├── README.md                                    # This file
├── .gitignore                                   # Git ignore rules
│
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── mq/
    │               └── test/
    │                   ├── config/              # Configuration classes
    │                   │   ├── MQConnectionConfig.java
    │                   │   ├── ConfigurationManager.java
    │                   │   ├── QueueConfiguration.java
    │                   │   └── TestConfiguration.java
    │                   │
    │                   ├── model/               # Data models
    │                   │   ├── MQMessage.java
    │                   │   └── ComparisonResult.java
    │                   │
    │                   ├── util/                # Utility classes
    │                   │   ├── MQMessageReader.java
    │                   │   └── TestLogger.java
    │                   │
    │                   ├── comparator/          # Comparison logic
    │                   │   └── MessageComparator.java
    │                   │
    │                   ├── report/              # Reporting utilities
    │                   │   └── ExtentReportManager.java
    │                   │
    │                   └── error/               # Error handling
    │                       └── MQErrorHandler.java
    │
    └── test/
        ├── java/
        │   └── com/
        │       └── mq/
        │           └── test/
        │               └── MQQueueComparisonTest.java    # Main test class
        │
        └── resources/
            └── config/                          # Environment configs
                ├── dev.properties               # Development
                ├── qa.properties                # QA
                ├── uat.properties               # UAT
                └── prod.properties              # Production
```

---

## Best Practices

### Security
1. **Never commit passwords** - Use environment variables or vault
2. **Encrypt property files** - Use Maven encryption for production
3. **Limit permissions** - Use read-only MQ users for testing
4. **Audit access** - Log all MQ connections

### Performance
1. **Use browse mode** - Prevents message removal
2. **Limit message count** - Set reasonable `queue.max.messages`
3. **Run off-peak** - Schedule tests during low traffic
4. **Monitor resources** - Watch memory and network usage

### Maintenance
1. **Regular updates** - Keep dependencies current
2. **Version control** - Track configuration changes
3. **Document changes** - Update README for modifications
4. **Review reports** - Archive historical test reports

---

## License

This project is licensed under the MIT License.

---

## Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Contact: your-email@company.com
- Documentation: https://your-docs-site.com

---

## Changelog

### Version 1.0.0 (2025-01-04)
- Initial release
- 18 comprehensive test scenarios
- Environment-based configuration
- Advanced error handling
- Extent Reports integration
- Console logging with formatting

---

**Built with ❤️ for Quality Assurance Teams**
