# 🎵 WebMusic Backend API

> A comprehensive music streaming platform backend built with Spring Boot, featuring role-based access control, premium content management, and subscription services.

## 🚀 Features

### Core Functionality
- **Music Streaming**: Browse, search, and stream music content
- **User Management**: Registration, authentication, and profile management
- **Playlist Management**: Create, edit, and share personal playlists
- **Social Features**: Like, comment, and interact with content

### Advanced Features
- **Role-Based Access Control**: PUBLIC, USER, CREATOR, ADMIN roles
- **Premium Content System**: Paid songs with individual pricing
- **Subscription Management**: Multi-tier subscriptions (Basic/Premium/VIP)
- **Song Submission Workflow**: Creator uploads → Admin approval process
- **Wallet System**: Deposits, withdrawals, and transaction tracking
- **Scheduled Tasks**: Auto-renewals and system maintenance

## 🏗️ Architecture

```
src/main/java/com/musicapp/backend/
├── entity/              # JPA Entities (User, Song, Playlist, etc.)
├── repository/          # Data Access Layer
├── service/            # Business Logic Layer
├── controller/         # REST API Controllers
├── dto/               # Data Transfer Objects
├── mapper/            # Entity-DTO Mappers
├── exception/         # Exception Handling
├── config/            # Configuration Classes
└── security/          # Security Components
```

## 🔐 User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **PUBLIC** | View songs, listen to music, view comments |
| **USER** | All public features + like, create playlists, comment |
| **CREATOR** | All user features + upload songs, manage own content |
| **ADMIN** | All features + approve songs, manage system data |

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: MySQL
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven
- **File Storage**: Local file system

## 🚀 Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 1. Database Setup
```sql
CREATE DATABASE music_app_db;

INSERT INTO roles (name) VALUES 
  ('ROLE_USER'), 
  ('ROLE_CREATOR'), 
  ('ROLE_ADMIN');
```

### 2. Configuration
Copy [.env.example](.env.example) to `.env` and configure:
```properties
DB_URL=jdbc:mysql://localhost:3306/music_app_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

### 3. Run Application
```bash
# Development
mvnw spring-boot:run

# Production
mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### 4. Access API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 📚 API Endpoints

### Authentication
```http
POST /api/v1/auth/register     # User registration
POST /api/v1/auth/authenticate # User login
```

### Songs
```http
GET    /api/v1/songs           # List all songs (paginated)
POST   /api/v1/songs           # Create song (CREATOR+)
PUT    /api/v1/songs/{id}      # Update song
DELETE /api/v1/songs/{id}      # Delete song
```

### Playlists
```http
GET    /api/v1/playlists       # User's playlists
POST   /api/v1/playlists       # Create playlist
PUT    /api/v1/playlists/{id}  # Update playlist
```

### Admin
```http
GET    /api/v1/admin/submissions     # Pending submissions
POST   /api/v1/admin/submissions/{id}/approve
POST   /api/v1/admin/submissions/{id}/reject
```

## 🧪 Testing

```bash
# Run tests
mvnw test

# Test authentication
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"123456"}'

# Test with authorization
curl -X GET http://localhost:8080/api/v1/songs \
  -H "Authorization: Bearer <your-jwt-token>"
```

## 🐳 Docker Deployment

```bash
# Build and run with Docker
docker build -t webmusic-backend .
docker run -p 8080:8080 webmusic-backend
```

## 📁 Key Components

### Entities
- [`User`](src/main/java/com/musicapp/backend/entity/User.java) - User management with roles and subscriptions
- [`Song`](src/main/java/com/musicapp/backend/entity/Song.java) - Music content with premium features
- [`SongSubmission`](src/main/java/com/musicapp/backend/entity/SongSubmission.java) - Submission workflow
- [`Transaction`](src/main/java/com/musicapp/backend/entity/Transaction.java) - Payment processing

### Services
- [`SubmissionService`](src/main/java/com/musicapp/backend/service/SubmissionService.java) - Handles song submission workflow
- `AuthenticationService` - User authentication and authorization
- `PaymentService` - Transaction and subscription management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

- **Main Developer**: Ngô Gia Khánh (zakyn)
- **Email**: 18khanh.2003@gmail.com
- **Project**: WebMusic Platform

---

> **Note**: This is a backend API service. The frontend application is available in a separate repository.