# Mini12306 Architecture Overview

## Project Introduction

Mini12306 is a simplified version of the Chinese railway ticket booking system (12306). It provides core functionalities including user management, train searching, ticket booking, and order management.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
├──────────────────────┬──────────────────────────────────────┤
│  User Frontend (Vue) │  Admin Dashboard (Vue)               │
└──────────┬───────────┴──────────────┬───────────────────────┘
           │                          │
           └──────────────┬───────────┘
                          │ HTTP/REST API
                          ↓
           ┌──────────────────────────┐
           │   API Server (Spring)    │
           │  - Authentication        │
           │  - Authorization         │
           │  - Business Logic        │
           └──────────┬───────────────┘
                      │
                      ↓
           ┌──────────────────────────┐
           │   MySQL Database         │
           │  - User Data             │
           │  - Train Data            │
           │  - Order Data            │
           └──────────────────────────┘
```

## Technology Stack

### Backend (API Server)
- **Language**: Java 21
- **Framework**: Spring Boot 3.1.7
- **ORM**: Spring Data JPA with Hibernate
- **Database**: MySQL 8.0
- **Security**: JWT-based authentication with Spring Security
- **Build Tool**: Maven 3.6+

### Frontend (User Application)
- **Framework**: Vue.js 3
- **UI Library**: Element Plus
- **HTTP Client**: Axios
- **Build Tool**: Vue CLI / Vite

### Admin Dashboard
- **Framework**: Vue.js 3
- **UI Library**: Element Plus, ECharts (for statistics)
- **HTTP Client**: Axios
- **Build Tool**: Vue CLI

## Core Features

### 1. User Management
- User registration with identity verification
- User login with JWT token generation
- User profile management
- Authentication status tracking

### 2. Passenger Management
- Add/edit/delete passenger information
- Query passenger list
- Associate passengers with user accounts

### 3. Train Management
- Query available trains
- Search trains by origin and destination
- View train details and schedules
- Real-time seat availability

### 4. Order Management
- Create booking orders
- View order history
- Cancel orders
- Order status tracking

### 5. Ticket Management
- Purchase tickets for passengers
- View purchased tickets
- Cancel tickets
- Ticket status management

### 6. Admin Functions
- User management
- Train data management
- System statistics and analytics
- Order monitoring

## Database Schema

### Core Tables

#### user_account
Stores user authentication and profile information.
- id (PK)
- username (unique)
- password (encrypted)
- real_name
- card_id (ID card number)
- phone
- auth_status (0: unverified, 1: verified)

#### passenger
Stores passenger information for ticket booking.
- id (PK)
- user_id (FK to user_account)
- name
- id_type
- id_number
- phone

#### train
Stores train information and schedules.
- id (PK)
- code (train number)
- type (G/D/C/K)
- start_station_id (FK to station)
- end_station_id (FK to station)
- start_time
- end_time
- seat_count
- price

#### station
Stores railway station information.
- id (PK)
- name
- code
- city
- province
- type

#### orders
Stores ticket booking orders.
- id (PK)
- order_no (unique)
- user_id (FK to user_account)
- train_id (FK to train)
- status
- total_price
- create_time

#### ticket
Stores individual ticket information.
- id (PK)
- ticket_no (unique)
- order_id (FK to orders)
- passenger_id (FK to passenger)
- train_id (FK to train)
- seat_number
- status

## API Endpoints

### Authentication (/api/auth)
- POST `/register` - User registration
- POST `/login` - User login
- POST `/authenticate` - Submit identity verification
- GET `/userinfo` - Get current user info

### Passenger (/api/passenger)
- GET `/` - List passengers
- POST `/` - Add passenger
- PUT `/{id}` - Update passenger
- DELETE `/{id}` - Delete passenger
- GET `/{id}` - Get passenger details

### Train (/api/train)
- GET `/` - List all trains
- GET `/search` - Search trains by stations
- GET `/{id}` - Get train details

### Order (/api/order)
- POST `/` - Create order
- GET `/` - List user orders
- POST `/cancel` - Cancel order
- GET `/{orderNo}` - Get order details
- GET `/{orderNo}/tickets` - Get order tickets

### Ticket (/api/ticket)
- GET `/bought` - List purchased tickets
- GET `/passenger/{passengerId}` - List tickets by passenger
- POST `/cancel` - Cancel ticket
- GET `/{ticketNo}` - Get ticket details

### Station (/api/station)
- GET `/` - List all stations
- GET `/search` - Search stations
- GET `/{id}` - Get station details

### Admin (/api/admin)
- GET `/users` - List all users
- GET `/stats` - Get system statistics
- PUT `/user/{id}` - Update user info
- DELETE `/user/{id}` - Delete user

## Security Model

### Authentication Flow
1. User submits login credentials (username/password)
2. Server validates credentials
3. Server generates JWT token with user ID
4. Client stores token (localStorage)
5. Client includes token in Authorization header for subsequent requests
6. Server validates token and extracts user ID from token

### Authorization
- Token-based authentication using JWT
- Password encryption using BCrypt
- CORS configuration for cross-origin requests
- Request interceptors to inject authentication tokens

## Development Setup

### Prerequisites
- JDK 21 or higher
- Maven 3.6 or higher
- Node.js 16+ and npm 8+
- MySQL 8.0+

### Backend Setup
```bash
# Navigate to API directory
cd api

# Install dependencies and compile
mvn clean install

# Run the application
mvn spring-boot:run
# OR use the script
./run.sh
```

The API server will start on port 1145 by default.

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Run development server
npm run serve

# Build for production
npm run build
```

### Admin Setup
```bash
# Navigate to admin directory
cd admin

# Install dependencies
npm install

# Run development server
npm run serve

# Build for production
npm run build
```

## Database Configuration

1. Create MySQL database:
```sql
CREATE DATABASE Mini12306;
CREATE USER 'Mini12306'@'localhost' IDENTIFIED BY 'C52wEARsX8TdZemE';
GRANT ALL PRIVILEGES ON Mini12306.* TO 'Mini12306'@'localhost';
FLUSH PRIVILEGES;
```

2. Import schema and sample data:
```bash
mysql -u Mini12306 -p Mini12306 < api/src/main/resources/Mini12306.session.sql
```

## Configuration

### Backend Configuration
Location: `api/src/main/resources/application.properties`

Key configurations:
- Server port: `server.port=1145`
- Database URL: `spring.datasource.url`
- JPA settings: `spring.jpa.*`
- CORS settings: `spring.mvc.cors.*`

### Frontend Configuration
Location: `frontend/src/config.js` or similar

Key configurations:
- API base URL
- Request timeout
- Authentication settings

## Testing

### Backend Tests
```bash
cd api
mvn test
```

Current test coverage:
- AuthServiceImplTest: 23 tests covering user authentication
- TicketServiceImplTest: 34 tests covering ticket operations
- Total: 57 tests, all passing

### Running Specific Tests
```bash
# Run specific test class
mvn test -Dtest=AuthServiceImplTest

# Run with code coverage
mvn test jacoco:report
```

## Code Quality

### Checkstyle
```bash
cd api
mvn checkstyle:check
```

Configuration: `api/checkstyle.xml`

### SpotBugs
```bash
cd api
mvn spotbugs:check
```

Configuration:
- Include filters: `api/spotbugs-include.xml`
- Exclude filters: `api/spotbugs-exclude.xml`

## Deployment Considerations

### Production Deployment
1. Configure production database
2. Update CORS settings for production domains
3. Enable HTTPS/SSL
4. Set appropriate JWT secret and expiration
5. Configure proper logging levels
6. Set up database connection pooling
7. Configure reverse proxy (Nginx/Apache)

### Docker Deployment (Future Enhancement)
Consider containerizing the application with Docker:
- Separate containers for API, Frontend, Admin, and Database
- Docker Compose for local development
- Kubernetes manifests for production

## Monitoring and Logging

### Application Logs
- Location: Console output (can be configured to file)
- Levels: DEBUG, INFO, WARN, ERROR
- SQL query logging enabled in development

### Metrics (Future Enhancement)
- Spring Boot Actuator endpoints
- Prometheus metrics
- Grafana dashboards

## Future Enhancements

1. **Search Optimization**
   - Implement full-text search for train queries
   - Add caching layer (Redis) for frequently accessed data

2. **Payment Integration**
   - Integrate payment gateways
   - Implement refund processing

3. **Real-time Features**
   - WebSocket for real-time seat availability
   - Notification system for order updates

4. **Mobile Support**
   - Responsive design improvements
   - Native mobile apps

5. **Analytics**
   - Enhanced admin dashboard with detailed analytics
   - User behavior tracking
   - Revenue reporting

6. **Internationalization**
   - Multi-language support
   - Currency conversion

7. **Performance**
   - Database indexing optimization
   - API response caching
   - CDN integration for static assets

## License

MIT License

## Contributing

Contributions are welcome! Please follow these guidelines:
1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions:
- GitHub Issues: [Project Repository]
- Documentation: This file and README files in each module
