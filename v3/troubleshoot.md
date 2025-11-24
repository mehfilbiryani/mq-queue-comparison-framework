# Windows Troubleshooting Guide

## 🔍 Common Issues and Solutions

### Issue 1: "Java is not recognized as an internal or external command"

**Symptoms:**
```
'java' is not recognized as an internal or external command,
operable program or batch file.
```

**Solutions:**

**Option A: Add Java to PATH (Recommended)**

1. Find your Java installation directory:
   ```
   C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x
   ```

2. Add to System PATH:
   - Press `Win + X` → System
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Under "System variables", find "Path"
   - Click "Edit" → "New"
   - Add: `C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x\bin`
   - Click OK on all dialogs

3. Restart Command Prompt and verify:
   ```cmd
   java -version
   ```

**Option B: Use Full Path**

In your batch scripts, use the full path:
```batch
"C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x\bin\java.exe" -jar target\mq-comparator-1.0.0.jar
```

---

### Issue 2: "mvn is not recognized"

**Symptoms:**
```
'mvn' is not recognized as an internal or external command
```

**Solution:**

1. Download Maven from: https://maven.apache.org/download.cgi
   - Download `apache-maven-3.x.x-bin.zip`

2. Extract to: `C:\Program Files\Apache\maven`

3. Add to PATH:
   - Add: `C:\Program Files\Apache\maven\bin`

4. Set MAVEN_HOME:
   - Create new System variable:
     - Name: `MAVEN_HOME`
     - Value: `C:\Program Files\Apache\maven`

5. Verify:
   ```cmd
   mvn -version
   ```

---

### Issue 3: "Cannot connect to MQ - Connection refused"

**Symptoms:**
```
MQJE001: Completion Code '2', Reason '2538'
Unable to connect to queue manager
```

**Diagnosis:**

1. **Check if MQ is running:**
   ```cmd
   docker ps
   ```
   Should show `ibm-mq-test` container with status "Up"

2. **Check port availability:**
   ```cmd
   netstat -an | findstr 1414
   ```
   Should show: `TCP    0.0.0.0:1414    0.0.0.0:0    LISTENING`

3. **Test connectivity:**
   ```cmd
   telnet localhost 1414
   ```

**Solutions:**

**If Docker not running:**
```cmd
cd C:\MQComparator
docker-compose up -d
timeout /t 60
```

**If port 1414 already in use:**
```cmd
# Find what's using the port
netstat -ano | findstr 1414

# Kill the process (replace PID)
taskkill /PID 1234 /F
```

**If firewall blocking:**
- Open Windows Firewall
- Allow inbound connections on port 1414
- Or temporarily disable firewall to test

**If Docker issues:**
```cmd
# Restart Docker Desktop
net stop com.docker.service
net start com.docker.service

# Or restart from GUI
# Right-click Docker Desktop tray icon → Restart
```

---

### Issue 4: "Build fails - Dependencies not downloading"

**Symptoms:**
```
[ERROR] Failed to execute goal on project mq-comparator: 
Could not resolve dependencies
```

**Solutions:**

**Check internet connection:**
```cmd
ping repo.maven.apache.org
```

**Clear Maven cache:**
```cmd
cd C:\MQComparator
mvn dependency:purge-local-repository
mvn clean install -U
```

**Check Maven settings:**
Create `C:\Users\YourName\.m2\settings.xml`:
```xml
<settings>
  <mirrors>
    <mirror>
      <id>central</id>
      <mirrorOf>*</mirrorOf>
      <url>https://repo.maven.apache.org/maven2</url>
    </mirror>
  </mirrors>
</settings>
```

**Use corporate proxy (if applicable):**
```xml
<settings>
  <proxies>
    <proxy>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy.company.com</host>
      <port>8080</port>
      <username>your-username</username>
      <password>your-password</password>
    </proxy>
  </proxies>
</settings>
```

**Manual dependency download:**
```cmd
mvn dependency:resolve
mvn dependency:resolve-plugins
```

---

### Issue 5: "Out of Memory" Error

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**

**Increase Java heap size:**

Edit `scripts\start-comparator.bat`:
```batch
set JAVA_OPTS=-Xms1g -Xmx4g -XX:+UseG1GC
```

**For high-volume scenarios:**
```batch
set JAVA_OPTS=-Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**Monitor memory usage:**
```cmd
# In separate terminal
jconsole
```
Select the running Java process to monitor.

---

### Issue 6: "Permission denied" when creating directories

**Symptoms:**
```
Access denied
The system cannot find the path specified
```

**Solutions:**

**Run Command Prompt as Administrator:**
1. Right-click Command Prompt
2. Select "Run as administrator"

**Check directory permissions:**
```cmd
icacls C:\MQComparator
```

**Grant full permissions:**
```cmd
icacls C:\MQComparator /grant Users:F /T
```

**Choose different directory:**
```cmd
# Use user directory instead
cd C:\Users\%USERNAME%\MQComparator
```

---

### Issue 7: "Port 1414 already in use"

**Symptoms:**
```
Error starting container: 
Bind for 0.0.0.0:1414 failed: port is already allocated
```

**Find and kill process using port:**

```cmd
# Find process
netstat -ano | findstr :1414

# Result example:
# TCP    0.0.0.0:1414    0.0.0.0:0    LISTENING    4567

# Kill process (use PID from last column)
taskkill /PID 4567 /F
```

**Alternative: Change MQ port:**

Edit `docker-compose.yml`:
```yaml
ports:
  - "1415:1414"  # Use port 1415 instead
```

Update `config\application.properties`:
```properties
ibm.mq.conn-name=localhost(1415)
```

---

### Issue 8: "Docker Desktop is not running"

**Symptoms:**
```
error during connect: This error may indicate that the docker daemon is not running
```

**Solutions:**

1. **Start Docker Desktop:**
   - Press `Win` key
   - Type "Docker Desktop"
   - Click to launch

2. **Wait for Docker to fully start** (look for whale icon in system tray)

3. **Verify Docker is running:**
   ```cmd
   docker info
   ```

4. **If Docker won't start:**
   - Check Windows Services
   - Restart "Docker Desktop Service"
   - Or restart computer

5. **Alternative: Use native IBM MQ:**
   - Download IBM MQ for Windows
   - Install and configure manually
   - Skip Docker steps

---

### Issue 9: "Report not updating / Shows old data"

**Symptoms:**
- HTML report exists but doesn't refresh
- Statistics are stale

**Solutions:**

**Force browser refresh:**
```
Ctrl + F5 (hard refresh)
```

**Clear browser cache:**
- Chrome: `Ctrl + Shift + Delete`
- Edge: `Ctrl + Shift + Delete`

**Check if comparator is running:**
```cmd
# List Java processes
jps -l

# Should show:
# 1234 com.mq.comparator.MQMessageComparator
```

**Check file timestamp:**
```cmd
dir C:\MQComparator\reports
```

**Check write permissions:**
```cmd
icacls C:\MQComparator\reports
```

**Verify report interval setting:**

In `application.properties`:
```properties
comparator.report.interval-ms=10000
```

---

### Issue 10: "Queues not found" Error

**Symptoms:**
```
MQJE001: Completion Code '2', Reason '2085'
MQRC_UNKNOWN_OBJECT_NAME
```

**Solutions:**

**Verify queue names in config:**

Check `config\application.properties`:
```properties
mq.legacy.queue=LEGACY.OUT.QUEUE
mq.new.queue=NEW.OUT.QUEUE
```

**Create queues via Web Console:**

1. Open: https://localhost:9443/ibmmq/console
2. Login: admin / passw0rd
3. Navigate to: Manage → Local Queues
4. Click "Create"
5. Create these queues:
   - INPUT.QUEUE
   - LEGACY.OUT.QUEUE
   - NEW.OUT.QUEUE

**Create queues via command line:**

```cmd
# Connect to MQ container
docker exec -it ibm-mq-test bash

# Run mqsc
runmqsc QM1

# Create queues
DEFINE QLOCAL('INPUT.QUEUE') MAXDEPTH(5000) REPLACE
DEFINE QLOCAL('LEGACY.OUT.QUEUE') MAXDEPTH(5000) REPLACE
DEFINE QLOCAL('NEW.OUT.QUEUE') MAXDEPTH(5000) REPLACE
end
```

---

### Issue 11: "Multiple Java processes running"

**Symptoms:**
- Multiple instances consuming resources
- Unexpected behavior

**Diagnosis:**
```cmd
jps -l
```

**Solution:**

**Kill all Java processes:**
```cmd
taskkill /F /IM java.exe
```

**Kill specific process:**
```cmd
# Find PID
jps -l

# Kill by PID
taskkill /F /PID 1234
```

**Prevent multiple instances:**

Add to `start-comparator.bat`:
```batch
@echo off
taskkill /F /IM java.exe 2>nul
timeout /t 2
java %JAVA_OPTS% -jar target\mq-comparator-1.0.0.jar
```

---

### Issue 12: "Simulator not sending messages"

**Symptoms:**
- Simulator runs but no messages appear
- Comparator shows 0 messages

**Diagnosis:**

**Check MQ connection in simulator:**
- Verify same configuration as comparator
- Check queue names match

**Check queue depths:**
```cmd
# Via Web Console
https://localhost:9443/ibmmq/console
# Navigate to queue → View depth
```

**Solutions:**

**Verify queue names match:**
- Simulator uses same queue names
- Case-sensitive: `LEGACY.OUT.QUEUE` ≠ `legacy.out.queue`

**Check MQ logs:**
```cmd
docker logs ibm-mq-test
```

**Increase simulator timeout:**

Modify simulator to add delays:
```java
Thread.sleep(1000); // Add between messages
```

---

## 🛠️ Diagnostic Tools

### Quick Health Check Script

Create `scripts\health-check.bat`:

```batch
@echo off
echo ========== MQ Comparator Health Check ==========

echo.
echo [1] Checking Java...
java -version 2>&1 | findstr "version"
if %errorlevel% equ 0 (echo [OK] Java found) else (echo [ERROR] Java not found)

echo.
echo [2] Checking Maven...
mvn -version 2>&1 | findstr "Maven"
if %errorlevel% equ 0 (echo [OK] Maven found) else (echo [ERROR] Maven not found)

echo.
echo [3] Checking Docker...
docker ps 2>&1 | findstr "ibm-mq-test"
if %errorlevel% equ 0 (echo [OK] MQ container running) else (echo [WARNING] MQ container not running)

echo.
echo [4] Checking Port 1414...
netstat -an | findstr ":1414.*LISTENING"
if %errorlevel% equ 0 (echo [OK] Port 1414 listening) else (echo [ERROR] Port 1414 not available)

echo.
echo [5] Checking Project Structure...
if exist target\mq-comparator-1.0.0.jar (echo [OK] JAR exists) else (echo [ERROR] JAR not found - run mvn package)

echo.
echo [6] Checking Configuration...
if exist config\application.properties (echo [OK] Config exists) else (echo [ERROR] Config missing)

echo.
echo [7] Checking Directories...
if exist logs (echo [OK] Logs directory exists) else (echo [WARNING] Logs directory missing)
if exist reports (echo [OK] Reports directory exists) else (echo [WARNING] Reports directory missing)

echo.
echo [8] Checking Java Processes...
jps -l | findstr "MQMessageComparator"
if %errorlevel% equ 0 (echo [INFO] Comparator is running) else (echo [INFO] Comparator not running)

echo.
echo ================================================
pause
```

---

## 📊 Performance Tuning

### Low-Resource Systems (4GB RAM or less)

```batch
set JAVA_OPTS=-Xms256m -Xmx1g -XX:+UseG1GC
```

`application.properties`:
```properties
comparator.thread-pool.size=2
comparator.results.max-size=1000
comparator.report.interval-ms=30000
```

### High-Performance Systems (16GB+ RAM)

```batch
set JAVA_OPTS=-Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=100
```

`application.properties`:
```properties
comparator.thread-pool.size=10
comparator.results.max-size=50000
comparator.report.interval-ms=5000
```

---

## 🔐 Security Considerations

### Production Deployment

**Change default passwords:**

`docker-compose.yml`:
```yaml
environment:
  MQ_APP_PASSWORD: StrongPassword123!
```

`application.properties`:
```properties
ibm.mq.password=StrongPassword123!
```

**Enable SSL/TLS:**

```properties
ibm.mq.channel=SECURED.APP.SVRCONN
ibm.mq.conn-name=your-mq-server.com(1414)
ibm.mq.ssl-cipher-suite=TLS_RSA_WITH_AES_256_CBC_SHA256
```

**Restrict access:**
- Use Windows Firewall rules
- Limit network access to MQ port
- Use service accounts with minimal permissions

---

## 📝 Logging and Debugging

### Enable Debug Logging

Edit `src\main\resources\logback.xml`:

```xml
<configuration>
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/mq-comparator.log</file>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <root level="DEBUG">
    <appender-ref ref="FILE" />
  </root>
</configuration>
```

### View Live Logs (PowerShell)

```powershell
Get-Content C:\MQComparator\logs\mq-comparator.log -Wait -Tail 50
```

### Filter Logs

```cmd
# Show only errors
findstr /i "ERROR" logs\mq-comparator.log

# Show only mismatches
findstr /i "MISMATCH" logs\mq-comparator.log

# Show recent activity
powershell "Get-Content logs\mq-comparator.log -Tail 100"
```

---

## 🆘 Getting Help

### Collect Diagnostic Information

```batch
@echo off
echo Collecting diagnostic information...

echo Environment Info > diagnostics.txt
echo ================= >> diagnostics.txt
systeminfo | findstr /i "OS" >> diagnostics.txt
java -version 2>&1 >> diagnostics.txt
mvn -version 2>&1 >> diagnostics.txt

echo. >> diagnostics.txt
echo Java Processes >> diagnostics.txt
echo ============== >> diagnostics.txt
jps -lv >> diagnostics.txt

echo. >> diagnostics.txt
echo Docker Status >> diagnostics.txt
echo ============= >> diagnostics.txt
docker ps -a >> diagnostics.txt

echo. >> diagnostics.txt
echo Network Connections >> diagnostics.txt
echo ================== >> diagnostics.txt
netstat -an | findstr "1414" >> diagnostics.txt

echo. >> diagnostics.txt
echo Recent Logs >> diagnostics.txt
echo =========== >> diagnostics.txt
powershell "Get-Content logs\mq-comparator.log -Tail 50" >> diagnostics.txt

echo Diagnostics saved to diagnostics.txt
notepad diagnostics.txt
```

---

## 📚 Additional Resources

- **IBM MQ Knowledge Center**: https://www.ibm.com/docs/en/ibm-mq
- **Java Documentation**: https://docs.oracle.com/en/java/
- **Maven Guide**: https://maven.apache.org/guides/
- **Docker Documentation**: https://docs.docker.com/

---

## ✅ Quick Reference

### Common Commands

```cmd
# Build project
mvn clean package

# Start MQ
docker-compose up -d

# Start comparator
java -jar target\mq-comparator-1.0.0.jar

# View logs
type logs\mq-comparator.log

# Check processes
jps -l

# Stop all Java
taskkill /F /IM java.exe

# Stop MQ
docker-compose down
```

### Important Paths

- Project: `C:\MQComparator`
- Config: `C:\MQComparator\config\application.properties`
- JAR: `C:\MQComparator\target\mq-comparator-1.0.0.jar`
- Logs: `C:\MQComparator\logs\mq-comparator.log`
- Reports: `C:\MQComparator\reports\mq_comparison_report.html`

### Default Ports

- MQ: `1414`
- MQ Web Console: `9443`
- Report: File-based (no port)
