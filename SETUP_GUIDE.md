# Mini12306 Complete Setup Guide

This guide will walk you through setting up the Mini12306 project from scratch, including all dependencies and configurations.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Setup](#database-setup)
4. [Backend Setup](#backend-setup)
5. [Frontend Setup](#frontend-setup)
6. [Admin Dashboard Setup](#admin-dashboard-setup)
7. [Running the Application](#running-the-application)
8. [Testing](#testing)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software
- **Java Development Kit (JDK)**: Version 21 or higher
- **Apache Maven**: Version 3.6 or higher
- **MySQL**: Version 8.0 or higher
- **Node.js**: Version 16 or higher
- **npm**: Version 8 or higher
- **Git**: For version control

### Verify Installations

```bash
# Check Java version
java -version
# Expected output: openjdk version "21.x.x" or higher

# Check Maven version
mvn -version
# Expected output: Apache Maven 3.6.x or higher

# Check MySQL version
mysql --version
# Expected output: mysql Ver 8.0.x or higher

# Check Node.js version
node --version
# Expected output: v16.x.x or higher

# Check npm version
npm --version
# Expected output: 8.x.x or higher
```

## Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Galaxy-you/Mini-12306.git
cd Mini-12306
```

### 2. Project Structure

After cloning, you should see:
```
Mini-12306/
├── api/              # Backend Spring Boot application
├── admin/            # Admin dashboard (Vue.js)
├── frontend/         # User-facing frontend (Vue.js)
├── ARCHITECTURE.md   # Architecture documentation
├── SETUP_GUIDE.md    # This file
└── README.md         # Project overview
```

## Database Setup

### 1. Start MySQL Service

```bash
# On Linux/Ubuntu
sudo systemctl start mysql
sudo systemctl enable mysql  # Enable auto-start on boot

# On macOS (with Homebrew)
brew services start mysql

# On Windows
# Start MySQL from Services or use MySQL Workbench
```

### 2. Create Database and User

Log into MySQL as root:
```bash
mysql -u root -p
```

Execute the following SQL commands:
```sql
-- Create database
CREATE DATABASE IF NOT EXISTS Mini12306;

-- Create user with password
CREATE USER IF NOT EXISTS 'Mini12306'@'localhost' IDENTIFIED BY 'C52wEARsX8TdZemE';

-- Grant all privileges
GRANT ALL PRIVILEGES ON Mini12306.* TO 'Mini12306'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

### 3. Import Database Schema and Sample Data

```bash
cd api
mysql -u Mini12306 -pC52wEARsX8TdZemE Mini12306 < src/main/resources/Mini12306.session.sql
```

### 4. Verify Database Setup

```bash
mysql -u Mini12306 -pC52wEARsX8TdZemE Mini12306 -e "SHOW TABLES;"
```

Expected output should include tables like:
- admin_user
- user_account
- station
- train
- orders
- ticket
- passenger

## Backend Setup

### 1. Navigate to API Directory

```bash
cd api
```

### 2. Configure Application Properties

The default configuration in `src/main/resources/application.properties` should work with the database setup above. If you need to change database credentials, edit this file:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/Mini12306?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=Mini12306
spring.datasource.password=C52wEARsX8TdZemE

# Server Configuration
server.port=1145
```

### 3. Build the Backend

```bash
# Clean and compile
mvn clean compile

# Or build the complete package
mvn clean package -DskipTests
```

Expected output: `BUILD SUCCESS`

### 4. Run Tests

```bash
mvn test
```

You should see:
```
Tests run: 57, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 5. Run the Backend Server

Option 1 - Using Maven:
```bash
mvn spring-boot:run
```

Option 2 - Using the provided script:
```bash
./run.sh
```

Option 3 - Running the JAR directly:
```bash
java -jar target/mini12306-api-0.0.1-SNAPSHOT.jar
```

The server will start on port 1145. You should see:
```
Started Mini12306ApiApplication in X.XXX seconds
```

### 6. Verify Backend is Running

Open a new terminal and test the API:
```bash
# Test station endpoint
curl http://localhost:1145/api/station

# Expected: JSON response with station list
```

## Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd ../frontend  # From api directory
# Or from project root:
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

This will install all required packages. You may see some deprecation warnings, which are normal.

### 3. Configure API Endpoint

Check if the frontend is configured to connect to the correct API endpoint. Look for configuration in:
- `src/config.js` or
- `src/utils/request.js` or similar

The default should be configured to connect to `http://localhost:1145`.

### 4. Run Development Server

```bash
npm run serve
```

The frontend will start on `http://localhost:8080` (or another port if 8080 is busy).

Expected output:
```
  App running at:
  - Local:   http://localhost:8080/
```

### 5. Build for Production (Optional)

```bash
npm run build
```

This creates optimized production files in the `dist/` directory.

## Admin Dashboard Setup

### 1. Navigate to Admin Directory

```bash
cd ../admin  # From frontend directory
# Or from project root:
cd admin
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Configure API Endpoint

Similar to the frontend, verify the API endpoint configuration.

### 4. Run Development Server

```bash
npm run serve
```

The admin dashboard will start on `http://localhost:8081` (or another available port).

### 5. Build for Production (Optional)

```bash
npm run build
```

## Running the Application

### Complete Startup Sequence

1. **Start MySQL** (if not already running)
   ```bash
   sudo systemctl start mysql
   ```

2. **Start Backend API** (Terminal 1)
   ```bash
   cd api
   mvn spring-boot:run
   ```
   Wait for: "Started Mini12306ApiApplication"

3. **Start Frontend** (Terminal 2)
   ```bash
   cd frontend
   npm run serve
   ```
   Wait for: "App running at: http://localhost:8080"

4. **Start Admin Dashboard** (Terminal 3)
   ```bash
   cd admin
   npm run serve
   ```
   Wait for: "App running at: http://localhost:8081"

### Accessing the Application

- **User Frontend**: http://localhost:8080
- **Admin Dashboard**: http://localhost:8081
- **API Documentation**: http://localhost:1145/api/

### Test the Application

1. Register a new user through the frontend
2. Login with the new account
3. Search for trains
4. Add passengers
5. Book tickets
6. View orders

## Testing

### Backend Tests

```bash
cd api

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceImplTest

# Run with coverage report
mvn clean test jacoco:report

# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check
```

### Frontend Tests

```bash
cd frontend

# Run unit tests (if configured)
npm run test:unit

# Run e2e tests (if configured)
npm run test:e2e

# Lint code
npm run lint
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Port Already in Use

**Problem**: `Address already in use: bind`

**Solution**:
```bash
# Find process using port 1145
lsof -i :1145
# Or on Windows
netstat -ano | findstr :1145

# Kill the process
kill -9 <PID>
# Or on Windows
taskkill /PID <PID> /F

# Or change the port in application.properties
server.port=8888
```

#### 2. MySQL Connection Failed

**Problem**: `Access denied for user 'Mini12306'@'localhost'`

**Solution**:
- Verify MySQL is running: `sudo systemctl status mysql`
- Check credentials in `application.properties`
- Reset user password:
  ```sql
  ALTER USER 'Mini12306'@'localhost' IDENTIFIED BY 'C52wEARsX8TdZemE';
  FLUSH PRIVILEGES;
  ```

#### 3. Java Version Mismatch

**Problem**: `Unsupported class file major version`

**Solution**:
```bash
# Set JAVA_HOME to JDK 21
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version
```

#### 4. Maven Build Failures

**Problem**: Dependencies cannot be downloaded

**Solution**:
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

#### 5. npm Install Errors

**Problem**: `ERESOLVE unable to resolve dependency tree`

**Solution**:
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall with legacy peer deps
npm install --legacy-peer-deps
```

#### 6. CORS Errors in Browser

**Problem**: Cross-Origin Request Blocked

**Solution**:
- Verify CORS configuration in `Mini12306ApiApplication.java`
- Ensure frontend is using correct API URL
- Check browser console for specific error details

#### 7. Database Tables Not Created

**Problem**: Tables are missing in the database

**Solution**:
```bash
# Re-import the SQL file
mysql -u Mini12306 -pC52wEARsX8TdZemE Mini12306 < api/src/main/resources/Mini12306.session.sql

# Or enable auto DDL in application.properties (not recommended for production)
spring.jpa.hibernate.ddl-auto=update
```

## Development Tips

### Hot Reload

- **Backend**: Use Spring Boot DevTools for automatic restart
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-devtools</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

- **Frontend**: Vue CLI has hot reload enabled by default

### Debugging

#### Backend Debugging
```bash
# Run with debug enabled
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Attach debugger to port 5005
```

#### Frontend Debugging
- Use Vue.js DevTools browser extension
- Enable source maps in vue.config.js
- Use browser developer tools

### Code Quality

```bash
# Backend
cd api
mvn checkstyle:check    # Check code style
mvn spotbugs:check      # Find bugs
mvn test                # Run tests

# Frontend
cd frontend
npm run lint            # Check and fix linting issues
npm run lint -- --fix   # Auto-fix linting issues
```

## Next Steps

After successful setup:

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the system design
2. Explore the API endpoints using tools like Postman or curl
3. Review the code structure in each module
4. Make your first code contribution
5. Write tests for new features

## Getting Help

If you encounter issues not covered in this guide:
1. Check existing GitHub issues
2. Review the [ARCHITECTURE.md](ARCHITECTURE.md) documentation
3. Check application logs for error details
4. Create a new GitHub issue with:
   - Your environment details (OS, Java version, etc.)
   - Steps to reproduce the problem
   - Error messages and logs

## Summary of Ports

| Service         | Port | URL                        |
|----------------|------|----------------------------|
| Backend API    | 1145 | http://localhost:1145      |
| User Frontend  | 8080 | http://localhost:8080      |
| Admin Dashboard| 8081 | http://localhost:8081      |
| MySQL Database | 3306 | localhost:3306             |

## Quick Start Commands

```bash
# Terminal 1 - Backend
cd api && mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend && npm run serve

# Terminal 3 - Admin
cd admin && npm run serve
```

Happy coding! 🚀
