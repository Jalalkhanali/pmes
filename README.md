# PMES - Energy Planning System

A professional energy planning software similar to LEAP (Long-range Energy Alternatives Planning) with Artificial Neural Network (ANN) forecasting and Particle Swarm Optimization (PSO) capabilities.

## 🚀 Features

### Core Functionality
- **Energy Data Import**: Excel file import with validation and error handling
- **Neural Network Forecasting**: ANN-based energy demand forecasting with PSO optimization
- **Scenario Management**: Create, manage, and compare different energy planning scenarios
- **Emissions Calculation**: CO2 emissions calculation using emission factors
- **RESTful API**: Comprehensive REST API with Swagger documentation

### Advanced Capabilities
- **Particle Swarm Optimization**: Optimizes neural network weights for better forecasting accuracy
- **Multi-sector Analysis**: Support for Industrial, Residential, Commercial, Transportation, and Agriculture sectors
- **Energy Source Modeling**: Electricity, Natural Gas, Oil, Coal, and Renewables
- **Scenario Comparison**: Compare emissions and energy consumption between scenarios
- **Excel Integration**: Import historical energy data from Excel files

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2.2 with Java 21
- **Database**: PostgreSQL 15
- **AI/ML**: Apache Commons Math for PSO and mathematical operations
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker with Docker Compose
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 15 (if running locally)

## 🚀 Quick Start

### Option 1: Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pmes
   ```

2. **Start the application with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Option 2: Local Development

1. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE energy_planning;
   CREATE USER energy_user WITH PASSWORD 'energy_pass';
   GRANT ALL PRIVILEGES ON DATABASE energy_planning TO energy_user;
   ```

2. **Run the application**
   ```bash
   ./start.sh
   ```

## 📊 API Endpoints

### Energy Data Import
- `POST /api/energy/import/excel` - Import energy data from Excel file

### Forecasting
- `POST /api/energy/forecast/baseline` - Baseline energy demand forecasting
- `POST /api/energy/scenarios/{scenarioId}/forecast` - Scenario-based forecasting

### Scenario Management
- `GET /api/energy/scenarios` - Get all scenarios
- `POST /api/energy/scenarios` - Create new scenario
- `GET /api/energy/scenarios/{id}` - Get scenario by ID
- `PUT /api/energy/scenarios/{id}` - Update scenario
- `DELETE /api/energy/scenarios/{id}` - Delete scenario
- `POST /api/energy/scenarios/{id}/activate` - Activate scenario

### Emissions Calculation
- `GET /api/energy/scenarios/{scenarioId}/emissions` - Calculate emissions for scenario
- `GET /api/energy/scenarios/{scenarioId}/emissions/range` - Calculate emissions for year range
- `GET /api/energy/scenarios/{scenarioId}/emissions/yearly` - Get yearly emissions
- `GET /api/energy/scenarios/{scenarioId}/emissions/sector` - Get emissions by sector
- `GET /api/energy/scenarios/{scenarioId}/emissions/energy-source` - Get emissions by energy source
- `GET /api/energy/emissions/compare` - Compare emissions between scenarios

### Combined Operations
- `POST /api/energy/scenarios/{scenarioId}/forecast-with-emissions` - Combined forecast and emissions

## 📁 Project Structure

```
pmes/
├── src/main/java/ir/aut/jalal/pmes/energy/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── entity/          # JPA entities
│   ├── repository/      # Data repositories
│   └── service/         # Business logic services
├── src/main/resources/
│   ├── application.yml  # Application configuration
│   └── db/changelog/    # Database migrations
├── sample-data/         # Sample energy data
├── compose.yaml         # Docker Compose configuration
├── Dockerfile          # Docker image definition
├── start.sh           # Startup script
└── README.md          # This file
```

## 🔧 Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
energy:
  neural-network:
    pso:
      particle-count: 30
      iterations: 100
      inertia-weight: 0.7
      cognitive-weight: 1.5
      social-weight: 1.5
    training:
      epochs: 500
      learning-rate: 0.01
      batch-size: 32
    forecasting:
      confidence-level: 0.95
      max-forecast-years: 30
```

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/energy_planning
    username: energy_user
    password: energy_pass
```

## 📈 Usage Examples

### 1. Import Energy Data
```bash
curl -X POST "http://localhost:8080/api/energy/import/excel" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@sample-data/energy_data_sample.csv" \
  -F "dataSource=Sample Data"
```

### 2. Create a Scenario
```bash
curl -X POST "http://localhost:8080/api/energy/scenarios" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "High Renewables Scenario",
    "description": "Scenario with increased renewable energy deployment",
    "startYear": 2023,
    "endYear": 2050,
    "sectorGrowthRates": {
      "Industrial": 0.02,
      "Residential": 0.015
    },
    "energySourceAdjustments": {
      "Renewables": 0.1,
      "Coal": -0.05
    }
  }'
```

### 3. Run Forecasting
```bash
curl -X POST "http://localhost:8080/api/energy/forecast/baseline" \
  -H "Content-Type: application/json" \
  -d '{
    "sectors": ["Industrial", "Residential"],
    "energySources": ["Electricity", "Natural Gas"],
    "forecastYears": 10,
    "startYear": 2023,
    "endYear": 2033
  }'
```

### 4. Calculate Emissions
```bash
curl -X GET "http://localhost:8080/api/energy/scenarios/1/emissions"
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

## 📊 Monitoring

### Health Checks
- Application health: `GET /actuator/health`
- Database health: `GET /actuator/health/db`
- Custom metrics: `GET /actuator/metrics`

### Logs
Application logs are written to `logs/energy-planning.log`

## 🔒 Security

The application includes:
- Spring Security configuration
- Input validation and sanitization
- SQL injection prevention through JPA
- File upload security measures

## 🚀 Deployment

### Production Deployment
1. Set environment variables for production database
2. Configure logging levels
3. Set up monitoring and alerting
4. Use production Docker Compose configuration

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/energy_planning
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=prod_pass
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the Swagger documentation at `/swagger-ui.html`
- Review the application logs

## 🔄 Version History

- **v1.0.0**: Initial release with core energy planning functionality
- Neural Network forecasting with PSO optimization
- Scenario management and comparison
- Emissions calculation
- Excel data import
- RESTful API with Swagger documentation 