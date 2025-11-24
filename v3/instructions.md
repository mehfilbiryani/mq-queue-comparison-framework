# IBM MQ Message Comparator - Windows Setup Guide

## 📋 Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17 or later**
   - Download: https://adoptium.net/
   - Verify installation:
     ```cmd
     java -version
     javac -version
     ```

2. **Apache Maven 3.6+**
   - Download: https://maven.apache.org/download.cgi
   - Extract to `C:\Program Files\Apache\maven`
   - Add to PATH: `C:\Program Files\Apache\maven\bin`
   - Verify:
     ```cmd
     mvn -version
     ```

3. **IBM MQ Client Libraries**
   - Option A: Docker Desktop for Windows (Recommended)
     - Download: https://www.docker.com/products/docker-desktop/
   - Option B: IBM MQ Windows Installation
     - Download: https://www.ibm.com/products/mq/advanced

4. **Text Editor / IDE**
   - IntelliJ IDEA Community Edition (Recommended)
   - VS Code with Java Extension Pack
   - Eclipse

### Optional Tools

- **Git for Windows**: https://git-scm.com/download/win
- **Windows Terminal**: https://aka.ms/terminal (Better than CMD)

---

## 🚀 Installation Steps

### Step 1: Create Project Directory

```cmd
mkdir C:\MQComparator
cd C:\MQComparator
```

### Step 2: Create Project Structure

```cmd
mkdir src\main\java\com\mq\comparator
mkdir src\main\resources
mkdir src\test\java\com\mq\comparator
mkdir config
mkdir logs
mkdir reports
mkdir scripts
mkdir target
```

### Step 3: Create Maven pom.xml

Create `C:\MQComparator\pom.xml` with the following content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mq</groupId>
    <artifactId>mq-comparator</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.ibm.mq</groupId>
            <artifactId>com.ibm.mq.allclient</artifactId>
            <version>9.3.4.1</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.14</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.mq.comparator.MQMessageComparator</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 4: Copy Java Source Files

1. Copy `MQMessageComparator.java` to:
   `C:\MQComparator\src\main\java\com\mq\comparator\`

2. Copy `MQMessageSimulator.java` to:
   `C:\MQComparator\src\main\java\com\mq\comparator\`

### Step 5: Create Configuration File

Create `C:\MQComparator\config\application.properties`:

```properties
# IBM MQ Configuration
ibm.mq.queue-manager=QM1
ibm.mq.channel=DEV.APP.SVRCONN
ibm.mq.conn-name=localhost(1414)
ibm.mq.user=app
ibm.mq.password=passw0rd

# Queue Names
mq.legacy.queue=LEGACY.OUT.QUEUE
mq.new.queue=NEW.OUT.QUEUE
mq.input.queue=INPUT.QUEUE

# Comparison Settings
comparator.timeout.pairing-ms=60000
comparator.timeout.message-wait-ms=30000
comparator.report.interval-ms=10000
comparator.report.output-path=./reports/mq_comparison_report.html
```

---

## 🐳 IBM MQ Setup (Using Docker)

### Option A: Docker Desktop (Recommended)

1. **Install Docker Desktop for Windows**
   - Download from: https://www.docker.com/products/docker-desktop/
   - Restart computer after installation

2. **Create docker-compose.yml**

Create `C:\MQComparator\docker-compose.yml`:

```yaml
version: '3.8'

services:
  ibm-mq:
    image: icr.io/ibm-messaging/mq:latest
    container_name: ibm-mq-test
    environment:
      LICENSE: accept
      MQ_QMGR_NAME: QM1
      MQ_APP_PASSWORD: passw0rd
    ports:
      - "1414:1414"
      - "9443:9443"
    volumes:
      - qm1data:/mnt/mqm

volumes:
  qm1data:
```

3. **Start IBM MQ**

```cmd
cd C:\MQComparator
docker-compose up -d
```

4. **Wait for MQ to start (30-60 seconds)**

```cmd
timeout /t 60
```

5. **Access MQ Web Console**
   - URL: https://localhost:9443/ibmmq/console
   - Username: `admin`
   - Password: `passw0rd`

6. **Create Queues via Web Console**
   - Navigate to "Manage" → "Local Queues"
   - Create three queues:
     - `INPUT.QUEUE`
     - `LEGACY.OUT.QUEUE`
     - `NEW.OUT.QUEUE`

### Option B: IBM MQ Native Windows Installation

1. Download IBM MQ from IBM website
2. Install with default options
3. Create Queue Manager named `QM1`
4. Create the required queues using MQ Explorer

---

## 🔨 Build the Project

```cmd
cd C:\MQComparator
mvn clean package
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 15.234 s
```

The JAR file will be created at: `C:\MQComparator\target\mq-comparator-1.0.0.jar`

---

## 🎯 Running the Application

### Create Batch Scripts

#### 1. Start Comparator Script

Create `C:\MQComparator\scripts\start-comparator.bat`:

```batch
@echo off
echo ========================================
echo Starting MQ Message Comparator
echo ========================================
echo.

cd C:\MQComparator

set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC

java %JAVA_OPTS% -jar target\mq-comparator-1.0.0.jar

pause
```

#### 2. Start Simulator Script

Create `C:\MQComparator\scripts\start-simulator.bat`:

```batch
@echo off
echo ========================================
echo Starting Message Simulator
echo ========================================
echo.

cd C:\MQComparator

echo Select Test Scenario:
echo 1 - Normal matching messages (50)
echo 2 - Mismatched content (20)
echo 3 - Orphaned messages (15)
echo 4 - Duplicate messages (10)
echo 5 - Mixed scenario (100)
echo 6 - Stress test (1000)
echo.

java -cp target\mq-comparator-1.0.0.jar com.mq.comparator.MQMessageSimulator

pause
```

#### 3. View Report Script

Create `C:\MQComparator\scripts\view-report.bat`:

```batch
@echo off
echo Opening comparison report...
start "" "C:\MQComparator\reports\mq_comparison_report.html"
```

#### 4. Stop Docker Script

Create `C:\MQComparator\scripts\stop-docker.bat`:

```batch
@echo off
echo Stopping IBM MQ Docker container...
cd C:\MQComparator
docker-compose down
echo IBM MQ stopped.
pause
```

#### 5. Complete Setup Script

Create `C:\MQComparator\setup-windows.bat`:

```batch
@echo off
echo ========================================
echo IBM MQ Message Comparator Setup
echo ========================================
echo.

:: Check Java
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found! Please install Java 17+
    pause
    exit /b 1
)
echo [OK] Java found

:: Check Maven
echo Checking Maven installation...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven not found! Please install Maven
    pause
    exit /b 1
)
echo [OK] Maven found

:: Check Docker
echo Checking Docker installation...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Docker not found! You'll need to install IBM MQ manually
    pause
) else (
    echo [OK] Docker found
)

:: Create directories
echo.
echo Creating project structure...
if not exist src\main\java\com\mq\comparator mkdir src\main\java\com\mq\comparator
if not exist src\main\resources mkdir src\main\resources
if not exist config mkdir config
if not exist logs mkdir logs
if not exist reports mkdir reports
if not exist scripts mkdir scripts
echo [OK] Directories created

:: Build project
echo.
echo Building project...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)
echo [OK] Build successful

:: Start Docker if available
echo.
echo Do you want to start IBM MQ Docker container? (Y/N)
set /p start_docker=
if /i "%start_docker%"=="Y" (
    echo Starting IBM MQ...
    docker-compose up -d
    echo Waiting for MQ to initialize (60 seconds)...
    timeout /t 60 /nobreak
    echo [OK] IBM MQ started
    echo Web Console: https://localhost:9443/ibmmq/console
    echo Username: admin
    echo Password: passw0rd
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Double-click: scripts\start-comparator.bat
echo 2. Double-click: scripts\start-simulator.bat
echo 3. Double-click: scripts\view-report.bat
echo.
pause
```

---

## ▶️ Running the Application

### Step 1: Run Complete Setup

```cmd
cd C:\MQComparator
setup-windows.bat
```

### Step 2: Start the Comparator

Open **Command Prompt** or **Windows Terminal**:

```cmd
cd C:\MQComparator\scripts
start-comparator.bat
```

**Or double-click**: `C:\MQComparator\scripts\start-comparator.bat`

### Step 3: Start the Simulator (New Window)

Open **another Command Prompt**:

```cmd
cd C:\MQComparator\scripts
start-simulator.bat
```

Select a test scenario (1-6) and press Enter.

### Step 4: View the Report

```cmd
cd C:\MQComparator\scripts
view-report.bat
```

**Or manually open**: `C:\MQComparator\reports\mq_comparison_report.html` in your browser

The report auto-refreshes every 10 seconds!

---

## 📊 Monitoring

### Real-time Log Viewing

```cmd
cd C:\MQComparator\logs
type mq-comparator.log
```

### Watch Logs in Real-time (PowerShell)

```powershell
Get-Content C:\MQComparator\logs\mq-comparator.log -Wait -Tail 20
```

### Check Docker Logs

```cmd
docker logs -f ibm-mq-test
```

---

## 🔧 Troubleshooting

### Issue: "Java not found"

**Solution:**
1. Install Java 17+ from https://adoptium.net/
2. Add to PATH:
   - Right-click "This PC" → Properties
   - Advanced System Settings → Environment Variables
   - Edit "Path" → Add `C:\Program Files\Eclipse Adoptium\jdk-17\bin`
3. Restart Command Prompt

### Issue: "Maven not found"

**Solution:**
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH: `C:\Program Files\Apache\maven\bin`
4. Set `MAVEN_HOME` environment variable

### Issue: "Cannot connect to MQ"

**Solution:**
1. Check Docker is running: `docker ps`
2. Check MQ container: `docker logs ibm-mq-test`
3. Verify port 1414 is available: `netstat -an | findstr 1414`
4. Check firewall settings
5. Verify queue names in MQ web console

### Issue: "Port 1414 already in use"

**Solution:**
```cmd
netstat -ano | findstr 1414
taskkill /PID [PID_NUMBER] /F
```

### Issue: "Build fails - dependencies not downloaded"

**Solution:**
```cmd
mvn dependency:purge-local-repository
mvn clean install -U
```

### Issue: "Report not updating"

**Solution:**
1. Check if comparator is running
2. Verify reports directory exists
3. Check write permissions on reports folder
4. Force browser refresh (Ctrl + F5)

---

## 📝 Configuration Tips

### Update MQ Connection Settings

Edit `C:\MQComparator\config\application.properties`:

```properties
# For remote MQ server
ibm.mq.conn-name=your-mq-server.com(1414)
ibm.mq.user=your-username
ibm.mq.password=your-password
```

### Increase Performance

Edit `start-comparator.bat`, change:
```batch
set JAVA_OPTS=-Xms1g -Xmx4g -XX:+UseG1GC
```

### Change Report Update Frequency

Edit `application.properties`:
```properties
comparator.report.interval-ms=5000
```

---

## 🎓 Common Usage Scenarios

### Scenario 1: Quick Test

```cmd
# Terminal 1
cd C:\MQComparator\scripts
start-comparator.bat

# Terminal 2 (after comparator starts)
start-simulator.bat
[Select: 1 - Normal matching messages]

# Terminal 3
view-report.bat
```

### Scenario 2: Stress Test

```cmd
# Terminal 1
start-comparator.bat

# Terminal 2
start-simulator.bat
[Select: 6 - Stress test (1000)]
```

### Scenario 3: Continuous Monitoring

```cmd
# Start comparator and let it run
start-comparator.bat

# In another terminal, generate continuous load
start-simulator.bat
[Select: 5 - Mixed scenario]
[Run multiple times]
```

---

## 🛑 Stopping the Application

### Stop Comparator
Press `Ctrl + C` in the comparator window

### Stop Docker MQ
```cmd
cd C:\MQComparator
docker-compose down
```

### Clean Up Everything
```cmd
docker-compose down -v
rmdir /s /q target
rmdir /s /q logs
rmdir /s /q reports
```

---

## 📞 Support

### Useful Commands

```cmd
# Check Java version
java -version

# Check Maven version
mvn -version

# List Docker containers
docker ps -a

# Check if MQ is accessible
telnet localhost 1414

# View all Java processes
jps -l

# Kill Java process
taskkill /F /IM java.exe
```

### Log Locations

- Application logs: `C:\MQComparator\logs\mq-comparator.log`
- Maven logs: Build output in console
- Docker logs: `docker logs ibm-mq-test`
- Reports: `C:\MQComparator\reports\mq_comparison_report.html`

---

## 🚀 Quick Start Summary

```cmd
# 1. Setup (one time)
cd C:\MQComparator
setup-windows.bat

# 2. Start comparator
scripts\start-comparator.bat

# 3. Start simulator (new window)
scripts\start-simulator.bat

# 4. View report
scripts\view-report.bat

# 5. Stop everything
[Ctrl+C in both windows]
docker-compose down
```

---

## ✅ Verification Checklist

- [ ] Java 17+ installed and in PATH
- [ ] Maven 3.6+ installed and in PATH
- [ ] Docker Desktop installed (optional)
- [ ] Project built successfully (`mvn clean package`)
- [ ] IBM MQ running (Docker or native)
- [ ] Queues created (INPUT.QUEUE, LEGACY.OUT.QUEUE, NEW.OUT.QUEUE)
- [ ] Comparator starts without errors
- [ ] Simulator can send messages
- [ ] Report generates and auto-refreshes
- [ ] Statistics update in real-time

---

## 🎯 Next Steps

1. **Customize for your environment**: Update `application.properties`
2. **Integrate with CI/CD**: Add Maven build to your pipeline
3. **Set up monitoring**: Configure alerts for high mismatch rates
4. **Archive reports**: Implement automatic backup of daily reports
5. **Scale up**: Deploy to Windows Server for production use

**Happy Testing! 🎉**
