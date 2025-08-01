# PMES - Energy Planning System

A professional energy planning software similar to LEAP (Long-range Energy Alternatives Planning) built with Java 21 + Spring Boot, featuring Artificial Neural Network forecasting with Particle Swarm Optimization.

## 🎯 Features

- **Excel Data Import**: Import energy consumption data from Excel files using Apache POI
- **ANN Forecasting**: Artificial Neural Network forecasting for 30-year projections using Neuroph and DL4J
- **PSO Optimization**: Particle Swarm Optimization to optimize neural network architecture
- **Scenario Management**: Create and manage energy planning scenarios (renewables boost, coal phaseout, etc.)
- **Emission Calculations**: Calculate CO2, NOx, SO2 emissions from forecast results
- **REST API**: Complete REST API ready for Next.js frontend integration
- **Docker Support**: Fully containerized with Docker and Docker Compose
- **PostgreSQL Database**: Robust data storage with optimized indexes

## 🛠 Tech Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3** - Modern Spring framework
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Primary database
- **Neuroph** - Artificial Neural Networks
- **DL4J** - Deep Learning for Java
- **Apache POI** - Excel file processing
- **Apache Commons Math** - PSO algorithm implementation
- **Lombok** - Code generation
- **Docker** - Containerization
- **Liquibase** - Database migrations

## 📋 Prerequisites

- Java 21 JDK
- Docker and Docker Compose
- Maven 3.8+
- PostgreSQL 15+ (if running locally)

## 🚀 Quick Start

### Using Docker (Recommended)

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
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Manual Setup

1. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE energy_planning;
   CREATE USER energy_user WITH PASSWORD 'energy_pass';
   GRANT ALL PRIVILEGES ON DATABASE energy_planning TO energy_user;
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## 📊 API Endpoints

### Excel Import
```
POST /api/energy/import/excel
Content-Type: multipart/form-data
Parameters:
- file: Excel file (.xlsx, .xls)
- dataSource: Source identifier (optional)
```

### Forecasting
```
POST /api/energy/forecast/baseline
Content-Type: application/json
Body: {
  "sectors": ["INDUSTRIAL", "RESIDENTIAL"],
  "energySources": ["COAL", "GAS", "RENEWABLES"],
  "forecastYears": 30,
  "startYear": 2020,
  "endYear": 2050
}
```

### Scenario Management
```
GET    /api/energy/scenarios                    # List all scenarios
POST   /api/energy/scenarios                    # Create scenario
GET    /api/energy/scenarios/{id}               # Get scenario
PUT    /api/energy/scenarios/{id}               # Update scenario
DELETE /api/energy/scenarios/{id}               # Delete scenario
POST   /api/energy/scenarios/{id}/activate      # Activate scenario
```

### Scenario-based Forecasting
```
POST /api/energy/scenarios/{scenarioId}/forecast
Content-Type: application/json
Body: {
  "sectors": ["INDUSTRIAL", "RESIDENTIAL"],
  "energySources": ["COAL", "GAS", "RENEWABLES"],
  "forecastYears": 30
}
```

### Emissions Calculation
```
GET /api/energy/scenarios/{scenarioId}/emissions                    # Calculate emissions
GET /api/energy/scenarios/{scenarioId}/emissions/yearly            # Yearly emissions
GET /api/energy/scenarios/{scenarioId}/emissions/sector            # Sector emissions
GET /api/energy/scenarios/{scenarioId}/emissions/energy-source     # Energy source emissions
GET /api/energy/emissions/compare?scenario1Id=1&scenario2Id=2     # Compare scenarios
```

### Combined Forecasting with Emissions
```
POST /api/energy/scenarios/{scenarioId}/forecast-with-emissions
Content-Type: application/json
Body: {
  "sectors": ["INDUSTRIAL", "RESIDENTIAL"],
  "energySources": ["COAL", "GAS", "RENEWABLES"],
  "forecastYears": 30
}
```

### Data Management
```
GET    /api/energy/data/energy                    # List energy data
POST   /api/energy/data/energy                    # Create energy data
GET    /api/energy/data/emission-factors          # List emission factors
POST   /api/energy/data/emission-factors          # Create emission factor
```

## 📈 Excel Import Format

The system expects Excel files with the following columns:

| Column | Required | Description |
|--------|----------|-------------|
| year | Yes | Year of the data (1900-2100) |
| sector | Yes | Energy sector (INDUSTRIAL, RESIDENTIAL, etc.) |
| energy_source | Yes | Energy source (COAL, GAS, RENEWABLES, etc.) |
| consumption_twh | Yes | Energy consumption in TWh |
| gdp_billions | No | GDP in billions |
| population_millions | No | Population in millions |
| avg_temperature_celsius | No | Average temperature in Celsius |
| notes | No | Additional notes |

### Sample Excel Data
```
year,sector,energy_source,consumption_twh,gdp_billions,population_millions
2020,INDUSTRIAL,COAL,150.5,2500.0,85.0
2020,INDUSTRIAL,GAS,120.3,2500.0,85.0
2020,RESIDENTIAL,COAL,45.2,2500.0,85.0
2021,INDUSTRIAL,COAL,148.7,2550.0,85.5
```

## 🧠 Neural Network Architecture

The system uses a multi-layer perceptron with:
- **Input Layer**: 16 neurons (4 years × 4 features per year)
- **Hidden Layer 1**: 10-100 neurons (optimized by PSO)
- **Hidden Layer 2**: 5-50 neurons (optimized by PSO)
- **Output Layer**: 1 neuron (energy consumption forecast)

### PSO Optimization Parameters
- **Particle Count**: 30
- **Iterations**: 100
- **Inertia Weight**: 0.7
- **Cognitive Weight**: 1.5
- **Social Weight**: 1.5

## 🌍 Emission Factors

The system includes comprehensive emission factors for:
- **Fossil Fuels**: Coal, Gas, Oil
- **Renewables**: Solar, Wind, Hydro, Biomass, Geothermal
- **Nuclear**: Pressurized water reactors
- **Emerging Technologies**: Hydrogen, CCS, BECCS

### Sample Emission Factors (kg CO2/TWh)
- Coal (Industrial): 820
- Gas (Combined Cycle): 490
- Solar (Photovoltaic): 45
- Wind (Onshore): 11
- Nuclear: 12
- Hydro: 4

## 🏗 Project Structure

```
src/main/java/ir/aut/jalal/pmes/energy/
├── controller/
│   ├── EnergyController.java          # Main energy planning endpoints
│   └── DataController.java            # Data management endpoints
├── entity/
│   ├── EnergyData.java               # Energy consumption data
│   ├── Scenario.java                 # Energy planning scenarios
│   ├── EmissionFactor.java           # Emission factors
│   └── ForecastResult.java           # Forecast results
├── repository/
│   ├── EnergyDataRepository.java     # Energy data queries
│   ├── ScenarioRepository.java       # Scenario queries
│   ├── EmissionFactorRepository.java # Emission factor queries
│   └── ForecastResultRepository.java # Forecast result queries
└── service/
    ├── ExcelImportService.java       # Excel file processing
    ├── NeuralNetworkService.java     # ANN forecasting
    ├── ParticleSwarmOptimizationService.java # PSO optimization
    ├── ScenarioService.java          # Scenario management
    └── EmissionService.java          # Emission calculations
```

## 📊 Sample Usage

### 1. Import Energy Data
```bash
curl -X POST http://localhost:8080/api/energy/import/excel \
  -F "file=@energy_data.xlsx" \
  -F "dataSource=Sample Import"
```

### 2. Create a Scenario
```bash
curl -X POST http://localhost:8080/api/energy/scenarios \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Renewables Boost",
    "description": "Scenario with increased renewable energy deployment",
    "scenarioType": "POLICY_CHANGE",
    "startYear": 2020,
    "endYear": 2050,
    "gdpGrowthRate": 2.5,
    "renewableTarget": 40.0,
    "carbonPrice": 50.0
  }'
```

### 3. Run Baseline Forecast
```bash
curl -X POST http://localhost:8080/api/energy/forecast/baseline \
  -H "Content-Type: application/json" \
  -d '{
    "sectors": ["INDUSTRIAL", "RESIDENTIAL"],
    "energySources": ["COAL", "GAS", "RENEWABLES"],
    "forecastYears": 30,
    "startYear": 2020,
    "endYear": 2050
  }'
```

### 4. Calculate Emissions
```bash
curl -X GET http://localhost:8080/api/energy/scenarios/1/emissions
```

## 🐳 Docker Commands

### Build and Run
```bash
# Build the application
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f pmes-app

# Stop all services
docker-compose down
```

### Database Access
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U energy_user -d energy_planning

# View database logs
docker-compose logs postgres
```

## 🔍 Monitoring

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace
```

### Metrics
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### API Tests
```bash
# Test Excel import
curl -X POST http://localhost:8080/api/energy/import/excel \
  -F "file=@test_data.xlsx"

# Test forecasting
curl -X POST http://localhost:8080/api/energy/forecast/baseline \
  -H "Content-Type: application/json" \
  -d @test_forecast_request.json
```

## 📚 API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔒 Security

The system includes basic security features:
- Input validation
- SQL injection prevention
- File upload restrictions
- CORS configuration

For production deployment, consider adding:
- JWT authentication
- Role-based access control
- API rate limiting
- HTTPS enforcement

## 🚀 Production Deployment

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=production
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/energy_planning
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
```

### Docker Production
```bash
# Build production image
docker build -t energy-planning:latest .

# Run with production config
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  energy-planning:latest
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the API documentation at http://localhost:8080/swagger-ui.html
- Review the logs at `logs/energy-planning.log`

## 🔄 Version History

- **v1.0.0** - Initial release with basic energy planning features
- **v1.1.0** - Added PSO optimization and improved neural networks
- **v1.2.0** - Enhanced emission calculations and scenario management 