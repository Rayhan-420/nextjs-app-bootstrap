# mediCore Hospital Management System

A comprehensive JavaFX-based Hospital Management System with role-based access control, real-time communication, and MySQL database integration.

## Features

### 🏥 Multi-Role Support
- **Admin**: Complete system control, user management, analytics
- **Doctor**: Patient management, prescriptions, appointments
- **Patient**: Appointment booking, medical history, chat with doctors
- **Nurse**: Patient vitals, medicine administration, bed management
- **Pharmacist**: Medicine inventory, prescription fulfillment
- **Security Guard**: Visitor management, digital gate passes
- **Ambulance Driver**: Emergency tracking, route management
- **Hospital Staff**: Task management, reporting systems

### 🔧 Technical Features
- **JavaFX UI**: Modern, responsive interface using FXML and CSS
- **MySQL Database**: Comprehensive data persistence
- **Real-time Communication**: Socket-based notification system
- **Multithreading**: Background processing for reports and notifications
- **Security**: Password hashing, session management, audit logging
- **File Handling**: PDF generation, document upload/download

### 🎨 UI/UX Features
- Clean, modern design with dark/light theme support
- Responsive layouts that work on different screen sizes
- Smooth animations and transitions
- Role-based dashboard customization
- Real-time notifications and alerts

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **IntelliJ IDEA** (recommended) or any Java IDE

## Database Setup

1. **Install MySQL** and create a database:
```sql
CREATE DATABASE medicore_hms;
CREATE USER 'medicore'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON medicore_hms.* TO 'medicore'@'localhost';
FLUSH PRIVILEGES;
```

2. **Update database configuration** in `src/main/resources/config/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/medicore_hms
db.username=medicore
db.password=your_password
```

## Installation & Setup

### Option 1: Using IntelliJ IDEA (Recommended)

1. **Clone or download** the project
2. **Open IntelliJ IDEA** and select "Open Project"
3. **Navigate** to the `medicore-hms` folder and open it
4. **Wait** for Maven to download dependencies
5. **Configure VM Options** for JavaFX:
   - Go to Run → Edit Configurations
   - Add VM options: `--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics`
6. **Set Main Class**: `com.medicore.MediCoreApplication`
7. **Run** the application

### Option 2: Using Command Line

1. **Navigate** to the project directory:
```bash
cd medicore-hms
```

2. **Compile** the project:
```bash
mvn clean compile
```

3. **Run** using Maven JavaFX plugin:
```bash
mvn javafx:run
```

### Option 3: Create Executable JAR

1. **Build** the JAR file:
```bash
mvn clean package
```

2. **Run** the JAR file:
```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar target/medicore-hms-1.0.0.jar
```

## Default Login Credentials

The system creates a default admin user on first run:

- **Username**: `admin`
- **Password**: `admin123`
- **Role**: Admin

**⚠️ Important**: Change the default password immediately after first login!

## Project Structure

```
medicore-hms/
├── src/main/java/com/medicore/
│   ├── MediCoreApplication.java          # Main application class
│   ├── controller/                       # FXML controllers
│   │   ├── LoginController.java
│   │   ├── AdminDashboardController.java
│   │   └── ...
│   ├── model/                           # Data models
│   │   ├── User.java
│   │   └── ...
│   ├── service/                         # Business logic services
│   │   ├── AuthenticationService.java
│   │   ├── DatabaseService.java
│   │   ├── ConfigService.java
│   │   └── ...
│   ├── util/                           # Utility classes
│   │   ├── SessionManager.java
│   │   ├── ValidationUtil.java
│   │   └── ...
│   └── network/                        # Real-time communication
│       ├── NotificationServer.java
│       └── ClientHandler.java
├── src/main/resources/
│   ├── fxml/                           # FXML UI files
│   │   ├── login.fxml
│   │   ├── admin-dashboard.fxml
│   │   └── ...
│   ├── css/                            # Stylesheets
│   │   └── main-style.css
│   ├── config/                         # Configuration files
│   │   └── application.properties
│   └── images/                         # Application images
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

## Configuration

### Application Settings
Edit `src/main/resources/config/application.properties` to customize:

- Database connection settings
- Server ports and networking
- File upload paths and limits
- Email configuration for notifications
- Security settings
- Logging configuration

### UI Themes
The application supports light and dark themes. Modify `src/main/resources/css/main-style.css` to customize the appearance.

## Development

### Adding New Features

1. **Create Model Classes** in `com.medicore.model` package
2. **Add Database Tables** in `DatabaseService.createTables()`
3. **Create FXML Files** in `src/main/resources/fxml/`
4. **Implement Controllers** in `com.medicore.controller` package
5. **Add Navigation** in existing controllers
6. **Update CSS** for styling if needed

### Database Schema

The application automatically creates the following tables:
- `users` - User accounts and profiles
- `patients` - Patient-specific information
- `doctors` - Doctor-specific information
- `appointments` - Appointment scheduling
- `prescriptions` - Medical prescriptions
- `medicines` - Medicine catalog
- `inventory` - Medicine inventory management
- `bed_allocations` - Hospital bed management
- `visitor_logs` - Visitor tracking
- `notifications` - System notifications
- `audit_logs` - System audit trail

### API Documentation

The system includes a real-time notification server that runs on port 9090 (configurable). Clients can connect using WebSocket-like protocol for:

- Real-time notifications
- Chat messaging
- Emergency alerts
- Status updates

## Troubleshooting

### Common Issues

1. **JavaFX Runtime Error**
   - Ensure JavaFX modules are properly configured
   - Check VM options include JavaFX module path

2. **Database Connection Failed**
   - Verify MySQL is running
   - Check database credentials in application.properties
   - Ensure database exists and user has proper permissions

3. **Port Already in Use**
   - Change server ports in application.properties
   - Kill existing processes using the ports

4. **Memory Issues**
   - Increase JVM heap size: `-Xmx2g`
   - Monitor memory usage in large datasets

### Logging

Application logs are written to:
- Console output (during development)
- Log files in `logs/` directory (configurable)

Log levels can be adjusted in `application.properties`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Security Considerations

- All passwords are hashed using BCrypt
- Session tokens are generated securely
- SQL injection prevention through prepared statements
- Input validation on all user inputs
- Audit logging for all critical operations

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Check the troubleshooting section above
- Review the application logs
- Create an issue in the project repository

## Version History

- **v1.0.0** - Initial release with core HMS functionality
  - Multi-role user management
  - Real-time notifications
  - Basic reporting and analytics
  - MySQL database integration

---

**mediCore HMS** - Empowering Healthcare Management with Modern Technology
