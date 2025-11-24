# Windows Quick Start Guide - IBM MQ Message Comparator

## ⚡ 5-Minute Setup

### Prerequisites (Download & Install)
1. **Java 17+**: https://adoptium.net/ → Add to PATH
2. **Maven 3.6+**: https://maven.apache.org/download.cgi → Add to PATH
3. **Docker Desktop**: https://www.docker.com/products/docker-desktop/ → Install & Start

### Verify Installation
```cmd
java -version
mvn -version
docker --version
```

---

## 📦 Installation

### One-Time Setup

```cmd
REM 1. Create project directory
mkdir C:\MQComparator
cd C:\MQComparator

REM 2. Download all Java files and place in:
REM    src\main\java\com\mq\comparator\

REM 3. Copy pom.xml to project root

REM 4. Build project
mvn clean package

REM 5. Create docker-compose.yml and start MQ
docker-compose up -d

REM 6. Wait for MQ to initialize
timeout /t 60

REM 7. Create queues via web console
REM    https://localhost:9443/ibmmq/console (admin/passw0rd)
REM    Create: INPUT.QUEUE, LEGACY.OUT.QUEUE, NEW.OUT.QUEUE
```

---

## 🚀 Running

### Three Simple Steps

**Terminal 1 - Start Comparator:**
```cmd
cd C:\MQComparator
java -Xmx2g -jar target\mq-comparator-1.0.0.jar
```

**Terminal 2 - Start Simulator:**
```cmd
cd C:\MQComparator
java -cp target\mq-comparator-1.0.0.jar com.mq.comparator.MQMessageSimulator
```
Select scenario: `5` (Mixed scenario - 100 messages)

**Terminal 3 - View Report:**
```cmd
start C:\MQComparator\reports\mq_comparison_report.html
```

---

## 📋 Project Structure

```
C:\MQComparator\
├── src\
│   └── main\
│       └── java\
│           └── com\
│               └── mq\
│                   └── comparator\
│                       ├── MQMessageComparator.java
│                       └── MQMessageSimulator.java
├── target\
│   └── mq-comparator-1.0.0.jar
├── config\
│   └── application.properties
├── logs\
│   └── mq-comparator.log
├── reports\
│   └── mq_comparison_report.html
├── pom.xml
└── docker-compose.yml
```

---

## ⚙️ Configuration

**Edit:** `C:\MQComparator\config\application.properties`

```properties
# Connection
ibm.mq.conn-name=localhost(1414)
ibm.mq.queue-manager=QM1

# Queues
mq.legacy.queue=LEGACY.OUT.QUEUE
mq.new.queue=NEW.OUT.QUEUE

# Performance
comparator.thread-pool.size=5
comparator.timeout.pairing-ms=60000
```

---

## 🎯 Batch Scripts (Recommended)

### Create: `scripts\start-all.bat`

```batch
@echo off
echo Starting MQ Comparator System...

REM Start MQ
start "MQ Container" cmd /k "docker-compose up"
timeout /t 60

REM Start Comparator
start "Comparator" cmd /k "cd C:\MQComparator && java -Xmx2g -jar target\mq-comparator-1.0.0.jar"
timeout /t 10

REM Start Simulator
start "Simulator" cmd /k "cd C:\MQComparator && java -cp target\mq-comparator-1.0.0.jar com.mq.comparator.MQMessageSimulator"

REM Open Report
timeout /t 5
start "" "C:\MQComparator\reports\mq_comparison_report.html"

echo All components started!
pause
```

### Create: `scripts\stop-all.bat`

```batch
@echo off
echo Stopping all components...

REM Stop Java processes
taskkill /F /IM java.exe 2>nul

REM Stop Docker
docker-compose down

echo All components stopped!
pause
```

---

## 🔍 Monitoring

### Check Status
```cmd
REM List running Java processes
jps -l

REM Check MQ container
docker ps | findstr ibm-mq

REM View live logs
powershell "Get-Content logs\mq-comparator.log -Wait -Tail 20"

REM Check report timestamp
dir reports\mq_comparison_report.html
```

### View Report
- **URL**: `file:///C:/MQComparator/reports/mq_comparison_report.html`
- **Auto-refresh**: Every 10 seconds
- **Web Console**: https://localhost:9443/ibmmq/console

---

## 🐛 Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| **"java not found"** | Add Java bin to PATH: `C:\Program Files\Eclipse Adoptium\jdk-17\bin` |
| **"mvn not found"** | Add Maven bin to PATH: `C:\Program Files\Apache\maven\bin` |
| **"Cannot connect to MQ"** | Check: `docker ps` - Restart: `docker-compose restart` |
| **"Port 1414 in use"** | Find: `netstat -ano \| findstr 1414` - Kill: `taskkill /PID <PID> /F` |
| **"Queues not found"** | Create via web console: https://localhost:9443/ibmmq/console |
| **"Out of memory"** | Increase heap: `java -Xmx4g -jar ...` |
| **"Report not updating"** | Check comparator is running: `jps -l` |
| **"Build failed"** | Clear cache: `mvn clean install -U` |

---

## 📊 Test Scenarios

| Scenario | Description | Messages |
|----------|-------------|----------|
| **1** | Normal matching | 50 |
| **2** | Content mismatches | 20 |
| **3** | Orphaned messages | 15 |
| **4** | Duplicate detection | 10 |
| **5** | Mixed (realistic) | 100 |
| **6** | Stress test | 1000 |

---

## 🎓 Understanding the Report

### Dashboard Metrics

- **Total Messages**: All messages processed
- **Matched**: Legacy and New content identical ✅
- **Mismatched**: Content differs ❌
- **Orphaned (Legacy)**: Only legacy delivered ⚠️
- **Orphaned (New)**: Only new system delivered ⚠️
- **Duplicates**: Same message received twice 🔄
- **Success Rate**: (Matched / Total) × 100%

### Status Colors

- 🟢 **Green** - Match (good)
- 🔴 **Red** - Mismatch (needs review)
- 🟡 **Yellow** - Orphaned (missing pair)
- 🔵 **Blue** - Duplicate (redundant)

---

## 🔧 Performance Tuning

### For Testing (4GB RAM)
```batch
java -Xms256m -Xmx1g -jar target\mq-comparator-1.0.0.jar
```

### For Production (16GB RAM)
```batch
java -Xms2g -Xmx8g -XX:+UseG1GC -jar target\mq-comparator-1.0.0.jar
```

### High Volume
Edit `application.properties`:
```properties
comparator.thread-pool.size=10
comparator.results.max-size=50000
comparator.report.interval-ms=5000
```

---

## 📁 File Locations

| File | Path |
|------|------|
| **JAR** | `target\mq-comparator-1.0.0.jar` |
| **Config** | `config\application.properties` |
| **Logs** | `logs\mq-comparator.log` |
| **Report** | `reports\mq_comparison_report.html` |
| **Docker** | `docker-compose.yml` |

---

## 🆘 Emergency Commands

```cmd
REM Stop everything
taskkill /F /IM java.exe
docker-compose down

REM Clean and rebuild
mvn clean
mvn package
docker-compose down -v
docker-compose up -d

REM View errors only
findstr /i "ERROR" logs\mq-comparator.log

REM Check what's using port 1414
netstat -ano | findstr :1414
```

---

## 📞 Quick Support Checklist

Before asking for help, collect this info:

```cmd
REM Create diagnostics.txt
java -version > diagnostics.txt
mvn -version >> diagnostics.txt
docker ps -a >> diagnostics.txt
jps -l >> diagnostics.txt
type config\application.properties >> diagnostics.txt
powershell "Get-Content logs\mq-comparator.log -Tail 50" >> diagnostics.txt
```

---

## ✅ Success Checklist

- [ ] Java 17+ installed and in PATH
- [ ] Maven 3.6+ installed and in PATH  
- [ ] Docker Desktop running
- [ ] Project built: `mvn package` successful
- [ ] MQ container running: `docker ps` shows `ibm-mq-test`
- [ ] Queues created in MQ web console
- [ ] Comparator starts without errors
- [ ] Simulator sends messages
- [ ] Report generates and updates
- [ ] Statistics show in dashboard

---

## 🎯 Next Steps

1. **Customize**: Edit `application.properties` for your environment
2. **Integrate**: Connect to your actual MQ systems
3. **Automate**: Schedule with Windows Task Scheduler
4. **Monitor**: Set up alerts for high mismatch rates
5. **Archive**: Backup daily reports for analysis

---

## 📚 Resources

- **Java**: https://adoptium.net/
- **Maven**: https://maven.apache.org/
- **Docker**: https://docs.docker.com/
- **IBM MQ**: https://www.ibm.com/docs/en/ibm-mq
- **Full Guide**: See Windows Setup Guide document

---

**🎉 You're Ready!** Run `scripts\start-all.bat` and watch the magic happen!
