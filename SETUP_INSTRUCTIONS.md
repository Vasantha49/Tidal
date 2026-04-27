# 📋 Complete Setup Instructions

**Music Service - TIDAL API Integration**  
**Immomio Coding Challenge**

---

## ⚡ System Requirements

### **Minimum Required Software**

| Component | Minimum Version | Windows | macOS | Linux |
|-----------|-----------------|---------|-------|-------|
| **Java** | 21 or higher | ✅ | ✅ | ✅ |
| **Docker** | 20.10 or higher | ✅ | ✅ | ✅ |
| **Docker Compose** | 2.0 or higher | ✅ | ✅ | ✅ |
| **Git** | Any recent version | ✅ | ✅ | ✅ |

### **Required Accounts**
- TIDAL Developer Account (free) - https://developer.tidal.com/

---

## 📦 Step-by-Step Installation

### **STEP 1: Install Java 21**

#### **Windows**
```
1. Download OpenJDK 21 from:
   https://www.oracle.com/java/technologies/javase/jdk21-archive.html

2. Run the installer

3. Follow on-screen instructions

4. Add to PATH (if not automatic):
   - Right-click "This PC" → Properties
   - Advanced system settings → Environment Variables
   - Add JAVA_HOME pointing to JDK installation
   - Add %JAVA_HOME%\bin to PATH

5. Verify installation:
   Open Command Prompt and type:
   java -version
   
   Expected output:
   openjdk version "21.0.x"
```

#### **macOS**
```bash
# Using Homebrew (recommended)
brew install openjdk@21

# If you don't have Homebrew:
# Visit https://brew.sh/ and follow installation

# Verify installation
java -version
# Should show: openjdk version "21.0.x"
```

#### **Linux (Ubuntu/Debian)**
```bash
# Update package list
sudo apt-get update

# Install Java 21
sudo apt-get install openjdk-21-jdk

# Verify installation
java -version
# Should show: openjdk version "21.0.x"
```

#### **Linux (CentOS/Fedora)**
```bash
# Install Java 21
sudo dnf install java-21-openjdk java-21-openjdk-devel

# Verify installation
java -version
# Should show: openjdk version "21.0.x"
```

---

### **STEP 2: Install Docker**

#### **Windows**
```
1. Download Docker Desktop from:
   https://www.docker.com/products/docker-desktop

2. Run the installer (DockerDesktopInstaller.exe)

3. During installation:
   ✓ Check "Install required Windows components for WSL 2"
   ✓ Check "Add Docker to PATH"

4. Restart your computer

5. Verify installation - Open PowerShell:
   docker --version
   docker compose --version
   
   Expected output:
   Docker version 20.10.x
   Docker Compose version 2.x.x
```

#### **macOS**
```
1. Download Docker Desktop from:
   https://www.docker.com/products/docker-desktop

2. Open the .dmg file

3. Drag Docker icon to Applications folder

4. Open Applications folder and launch Docker

5. Enter your password when prompted

6. Verify installation - Open Terminal:
   docker --version
   docker compose --version
   
   Expected output:
   Docker version 20.10.x
   Docker Compose version 2.x.x
```

#### **Linux (Ubuntu/Debian)**
```bash
# Update package list
sudo apt-get update

# Install Docker
sudo apt-get install docker.io

# Install Docker Compose
sudo apt-get install docker-compose

# Enable Docker service
sudo systemctl enable docker
sudo systemctl start docker

# (Optional) Add your user to docker group to avoid using sudo
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker compose --version

# Expected output:
# Docker version 20.10.x
# Docker Compose version 2.x.x
```

#### **Linux (CentOS/Fedora)**
```bash
# Install Docker
sudo dnf install docker docker-compose

# Enable Docker service
sudo systemctl enable docker
sudo systemctl start docker

# (Optional) Add your user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker compose --version
```

---

### **STEP 3: Verify All Prerequisites**

Run these commands to verify everything is installed correctly:

```bash
# Check Java
java -version
# Expected: openjdk version "21.0.x"

# Check Docker
docker --version
# Expected: Docker version 20.10.x or higher

# Check Docker Compose
docker compose --version
# Expected: Docker Compose version 2.x.x or higher

# Check Git
git --version
# Expected: git version x.x.x or higher
```

**If any command fails or shows version less than required:**
- Go back to that step above and complete the installation
- Then run the verification again

---

## 🚀 Project Setup

### **STEP 4: Clone the Repository**

```bash
# Clone the repository
git clone https://github.com/yourusername/music-service.git
cd music

# Verify you're in the right directory
ls  # On macOS/Linux
dir # On Windows

# You should see: pom.xml, docker-compose.yml, src/, etc.
```

---

### **STEP 5: Get TIDAL API Credentials**

```
1. Visit: https://developer.tidal.com/

2. Click "Sign Up" (if you don't have an account)
   - Enter email, password, and accept terms
   - Verify your email

3. Log in to Developer Dashboard

4. Click "My Applications"

5. Click "Create a new application"
   - Name: "Music Service" (or any name)
   - Description: "Personal music curation service"
   - Accept terms

6. Copy the following:
   - Client ID (save this)
   - Client Secret (save this securely)

7. Note: You now have API credentials!
```

---

### **STEP 6: Configure Environment Variables**

#### **Option A: Create .env file (Recommended)**

```bash
# Navigate to project directory
cd music

# Copy the template
cp .env.example .env

# Edit .env with your TIDAL credentials
# On macOS/Linux, use any text editor:
nano .env
# or
vim .env

# On Windows, use Notepad:
notepad .env

# Add your credentials:
TIDAL_CLIENT_ID=your-actual-client-id
TIDAL_CLIENT_SECRET=your-actual-client-secret
TIDAL_COUNTRY_CODE=DE
```

#### **Option B: Use Environment Variables**

**macOS/Linux:**
```bash
export TIDAL_CLIENT_ID="your-client-id"
export TIDAL_CLIENT_SECRET="your-client-secret"
export TIDAL_COUNTRY_CODE="DE"
```

**Windows (PowerShell):**
```powershell
$env:TIDAL_CLIENT_ID="your-client-id"
$env:TIDAL_CLIENT_SECRET="your-client-secret"
$env:TIDAL_COUNTRY_CODE="DE"
```

**Windows (Command Prompt):**
```cmd
set TIDAL_CLIENT_ID=your-client-id
set TIDAL_CLIENT_SECRET=your-client-secret
set TIDAL_COUNTRY_CODE=DE
```

---

### **STEP 7: Start PostgreSQL Database**

```bash
# Navigate to project directory (if not already there)
cd music

# Start Docker and PostgreSQL
docker compose up -d

# Verify database is running
docker compose ps

# Expected output:
# NAME     IMAGE              STATUS
# music-db postgres:15        Up 2 minutes
# music-app ...               Exited

# Check logs to ensure no errors
docker compose logs db
```

**Troubleshooting:**
```bash
# If Docker daemon is not running, start it:
# Windows: Open Docker Desktop application
# macOS: Open Docker application
# Linux: sudo systemctl start docker

# If port is already in use:
# Check what's using port 5432:
# Windows: netstat -ano | findstr :5432
# macOS/Linux: lsof -i :5432

# See full docker logs:
docker compose logs -f
```

---

### **STEP 8: Build and Run Application**

```bash
# Navigate to project directory
cd music

# Build the project (first time only)
./mvnw clean compile

# Windows:
mvnw.cmd clean compile

# This downloads dependencies and compiles code (~30-60 seconds)

# Run the application
./mvnw spring-boot:run

# Windows:
mvnw.cmd spring-boot:run

# Expected output (at the end):
# Started MusicApplication in X seconds
# Listening on port 8080
```

**First startup may take 2-3 minutes as it:**
- Downloads dependencies
- Runs database migrations
- Starts Spring Boot application

**Keep this terminal open while developing!**

---

### **STEP 9: Verify Application is Running**

Open a new terminal/command prompt:

```bash
# Test health endpoint
curl http://localhost:8080/health/tidal

# Expected response (success):
# {"status":"UP","credentialsConfigured":true,"message":"..."}

# If using Windows and curl is not available, use:
# powershell: Invoke-WebRequest http://localhost:8080/health/tidal
# or visit: http://localhost:8080/health/tidal in your browser
```

---

### **STEP 10: Seed Initial Data**

```bash
# Create sample artists from TIDAL
curl -X POST http://localhost:8080/sync/seed

# Expected response:
# Seeding successful - 10 artists created

# Verify data was created
curl http://localhost:8080/artists

# You should see a JSON array with artist names
```

---

## ✅ Success! You're Ready

If all steps completed successfully, you should have:

✅ Java 21 installed  
✅ Docker running  
✅ PostgreSQL database started  
✅ Application running on http://localhost:8080  
✅ TIDAL API credentials configured  
✅ Sample data seeded  

---

## 🧪 Next Steps

### **Test the API**

```bash
# List all artists
curl http://localhost:8080/artists

# Search for an artist
curl "http://localhost:8080/artists/search?q=radiohead"

# Sync albums
curl -X POST http://localhost:8080/sync/albums

# Check all available endpoints
curl http://localhost:8080/swagger-ui.html
```

**See `QUICK_REFERENCE.md` for more endpoint examples**

---

## ⚠️ Troubleshooting

### **"Java not found"**
- Java is not installed or not in PATH
- Solution: Reinstall Java and ensure it's added to PATH
- Verify: `java -version` should work

### **"Docker daemon not running"**
- Docker is not started
- Solution: Open Docker Desktop (Windows/macOS) or run `sudo systemctl start docker` (Linux)

### **"Port 8080 already in use"**
- Another application is using port 8080
- Solution: Stop the other application or modify `application.yaml` to use different port

### **"Database connection failed"**
- PostgreSQL container not running
- Solution: Run `docker compose up -d` again
- Check: `docker compose ps` should show music-db as UP

### **"TIDAL credentials not working"**
- Incorrect Client ID or Client Secret
- Solution: Double-check credentials from developer.tidal.com
- Verify: Run `curl http://localhost:8080/health/tidal` to check connection

**For more troubleshooting, see `ERROR_HANDLING.md`**

---

## 📚 Documentation References

| Document | Purpose |
|----------|---------|
| [SUBMISSION_GUIDE.md](SUBMISSION_GUIDE.md) | How to submit the project |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Quick API reference |
| [API_ENDPOINTS.md](API_ENDPOINTS.md) | Complete API documentation |
| [ERROR_HANDLING.md](ERROR_HANDLING.md) | Error troubleshooting |
| [GITHUB_README.md](GITHUB_README.md) | Project overview |

---

## 🎉 Congratulations!

You now have the Music Service running locally and ready for:
- Development
- Testing
- Deployment

**Happy coding!** 🎵

