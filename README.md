# Mini12306 - Railway Ticket Booking System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.7-brightgreen.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

A simplified version of the Chinese railway ticket booking system (12306)

[Documentation](ARCHITECTURE.md) • [Setup Guide](SETUP_GUIDE.md) • [API Docs](api/README.md)

</div>

## 📋 Overview

Mini12306 is a full-stack web application that simulates the core functionalities of China's railway ticket booking system. It provides a comprehensive solution for managing users, trains, tickets, and orders with separate interfaces for end users and administrators.

### ✨ Key Features

- 🔐 **User Authentication & Authorization** - Secure JWT-based authentication with identity verification
- 🚄 **Train Management** - Browse and search trains by origin and destination
- 🎫 **Ticket Booking** - Real-time ticket booking with seat selection
- 👥 **Passenger Management** - Add and manage multiple passenger profiles
- 📦 **Order Management** - Track booking history and manage orders
- 📊 **Admin Dashboard** - Comprehensive admin panel with analytics and user management

## 🏗️ Architecture

The project follows a three-tier architecture:

```
┌─────────────┐     ┌─────────────┐
│   Frontend  │     │    Admin    │
│   (Vue.js)  │     │   (Vue.js)  │
└──────┬──────┘     └──────┬──────┘
       │                   │
       └─────────┬─────────┘
                 │ REST API
       ┌─────────▼─────────┐
       │   Backend (API)   │
       │  Spring Boot 3.x  │
       └─────────┬─────────┘
                 │
       ┌─────────▼─────────┐
       │  MySQL Database   │
       └───────────────────┘
```

For detailed architecture information, see [ARCHITECTURE.md](ARCHITECTURE.md).

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Galaxy-you/Mini-12306.git
   cd Mini-12306
   ```

2. **Setup Database**
   ```bash
   # Create database and user
   mysql -u root -p
   CREATE DATABASE Mini12306;
   CREATE USER 'Mini12306'@'localhost' IDENTIFIED BY 'C52wEARsX8TdZemE';
   GRANT ALL PRIVILEGES ON Mini12306.* TO 'Mini12306'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   
   # Import schema
   cd api
   mysql -u Mini12306 -pC52wEARsX8TdZemE Mini12306 < src/main/resources/Mini12306.session.sql
   ```

3. **Start Backend API**
   ```bash
   cd api
   mvn spring-boot:run
   # API will start on http://localhost:1145
   ```

4. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run serve
   # Frontend will start on http://localhost:8080
   ```

5. **Start Admin Dashboard**
   ```bash
   cd admin
   npm install
   npm run serve
   # Admin will start on http://localhost:8081
   ```

For detailed setup instructions, see [SETUP_GUIDE.md](SETUP_GUIDE.md).

## 📚 Project Structure

```
Mini-12306/
├── api/                        # Backend API (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/mini12306/
│   │   │   │       ├── controller/    # REST Controllers
│   │   │   │       ├── service/       # Business Logic
│   │   │   │       ├── repository/    # Data Access
│   │   │   │       ├── model/         # Domain Models
│   │   │   │       └── config/        # Configuration
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── Mini12306.session.sql
│   │   └── test/                      # Unit Tests
│   ├── pom.xml
│   └── README.md
├── frontend/                   # User Frontend (Vue.js)
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   ├── router/
│   │   └── utils/
│   ├── package.json
│   └── README.md
├── admin/                      # Admin Dashboard (Vue.js)
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── router/
│   ├── package.json
│   └── README.md
├── ARCHITECTURE.md             # Architecture Documentation
├── SETUP_GUIDE.md             # Detailed Setup Guide
└── README.md                  # This file
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/authenticate` - Submit identity verification
- `GET /api/auth/userinfo` - Get user information

### Trains & Stations
- `GET /api/train` - List all trains
- `GET /api/train/search` - Search trains
- `GET /api/station` - List all stations

### Orders & Tickets
- `POST /api/order` - Create order
- `GET /api/order` - List user orders
- `POST /api/ticket/cancel` - Cancel ticket
- `GET /api/ticket/bought` - List purchased tickets

### Passengers
- `GET /api/passenger` - List passengers
- `POST /api/passenger` - Add passenger
- `PUT /api/passenger/{id}` - Update passenger
- `DELETE /api/passenger/{id}` - Delete passenger

For complete API documentation, see [api/README.md](api/README.md).

## 🧪 Testing

### Backend Tests
```bash
cd api
mvn test
```

Current test coverage:
- ✅ 57 tests passing
- ✅ Authentication service tests
- ✅ Ticket service tests
- ✅ Unit and integration tests

### Code Quality
```bash
# Checkstyle
mvn checkstyle:check

# SpotBugs
mvn spotbugs:check
```

## 🛠️ Technology Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.1.7** - Application framework
- **Spring Data JPA** - Data persistence
- **MySQL 8.0** - Database
- **JWT** - Authentication
- **Maven** - Build tool

### Frontend & Admin
- **Vue.js 3** - Frontend framework
- **Element Plus** - UI component library
- **Axios** - HTTP client
- **Vue Router** - Routing
- **ECharts** - Data visualization (Admin)

## 📖 Documentation

- **[Architecture Guide](ARCHITECTURE.md)** - System architecture and design patterns
- **[Setup Guide](SETUP_GUIDE.md)** - Comprehensive installation and configuration
- **[API Documentation](api/README.md)** - Backend API reference
- **[Frontend Guide](frontend/README.md)** - Frontend application guide
- **[Admin Guide](admin/README.md)** - Admin dashboard guide

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Write tests for new features
- Follow existing code style and conventions
- Update documentation for significant changes
- Ensure all tests pass before submitting PR

## 🐛 Known Issues

- Some npm packages have deprecation warnings (non-critical)
- JWT tokens currently don't expire (enhancement needed)
- Real-time seat updates not implemented (future feature)

## 🔮 Future Enhancements

- [ ] WebSocket for real-time updates
- [ ] Payment gateway integration
- [ ] Mobile responsive design improvements
- [ ] Docker containerization
- [ ] Redis caching layer
- [ ] Internationalization (i18n)
- [ ] API rate limiting
- [ ] Advanced search filters
- [ ] Email notifications
- [ ] User reviews and ratings

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Galaxy-you** - Initial work

## 🙏 Acknowledgments

- Inspired by the real Chinese railway ticket booking system (12306)
- Spring Boot community for excellent documentation
- Vue.js team for the amazing framework
- All contributors who help improve this project

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Setup Guide](SETUP_GUIDE.md) for common problems
2. Review [existing issues](https://github.com/Galaxy-you/Mini-12306/issues)
3. Create a [new issue](https://github.com/Galaxy-you/Mini-12306/issues/new) with details

## ⭐ Show Your Support

Give a ⭐️ if this project helped you learn or build something cool!

---

<div align="center">
Made with ❤️ by the Mini12306 Team
</div>
