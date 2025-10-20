# Backend Integration & Deployment Guide

**Project**: Samjhadoo Backend  
**Version**: 1.0  
**Last Updated**: October 16, 2025

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Configuration](#database-configuration)
4. [External Services Integration](#external-services-integration)
5. [Build & Run](#build--run)
6. [Testing](#testing)
7. [Docker Deployment](#docker-deployment)
8. [Production Deployment](#production-deployment)
9. [Monitoring & Logging](#monitoring--logging)
10. [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 17+ | Runtime environment |
| Maven | 3.8+ | Build tool |
| PostgreSQL | 14+ | Primary database |
| Redis | 6.2+ | Caching & sessions |
| RabbitMQ | 3.9+ | Message broker for WebSocket |
| Docker | 20.10+ | Containerization |
| Docker Compose | 1.29+ | Multi-container orchestration |

### Optional Tools

- **IntelliJ IDEA** / **VS Code** - IDE
- **Postman** / **Insomnia** - API testing
- **pgAdmin** - PostgreSQL management
- **Redis Commander** - Redis management
- **RabbitMQ Management UI** - Message broker monitoring

---

## 🚀 Local Development Setup

### Step 1: Clone Repository

```bash
git clone https://github.com/yourusername/samjhadoo.git
cd samjhadoo/backend
```

### Step 2: Install Dependencies

```bash
# Using Maven
mvn clean install -DskipTests
```

### Step 3: Configure Environment Variables

Create `application-dev.properties` in `src/main/resources/`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/samjhadoo_dev
spring.datasource.username=samjhadoo_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=60000

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# OpenAI Configuration
openai.api.key=sk-your-openai-api-key
openai.api.url=https://api.openai.com/v1
openai.model.master=gpt-3.5-turbo
openai.model.agentic=gpt-4

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=./uploads

# CORS Configuration
cors.allowed-origins=http://localhost:3000,http://localhost:4200

# Logging Configuration
logging.level.root=INFO
logging.level.com.samjhadoo=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Email Configuration (Optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🗄️ Database Configuration

### PostgreSQL Setup

#### 1. Install PostgreSQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Windows:**
Download from [PostgreSQL Official Site](https://www.postgresql.org/download/windows/)

#### 2. Create Database and User

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Create database
CREATE DATABASE samjhadoo_dev;

# Create user
CREATE USER samjhadoo_user WITH PASSWORD 'your_secure_password';

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE samjhadoo_dev TO samjhadoo_user;

# Exit
\q
```

#### 3. Verify Connection

```bash
psql -h localhost -U samjhadoo_user -d samjhadoo_dev
```

#### 4. Database Schema

The application uses Hibernate with `ddl-auto=update`, so tables will be created automatically on first run.

**Manual Schema Creation (Optional):**

```sql
-- Run this if you prefer manual schema management
-- Located in: src/main/resources/schema.sql
```

---

## 🔌 External Services Integration

### 1. Redis Setup

#### Install Redis

**Ubuntu/Debian:**
```bash
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

**macOS:**
```bash
brew install redis
brew services start redis
```

**Docker:**
```bash
docker run -d --name redis -p 6379:6379 redis:6.2
```

#### Verify Redis

```bash
redis-cli ping
# Should return: PONG
```

#### Configure Redis for Sessions

```properties
# application-dev.properties
spring.session.store-type=redis
spring.session.redis.namespace=samjhadoo:session
```

---

### 2. RabbitMQ Setup

#### Install RabbitMQ

**Ubuntu/Debian:**
```bash
sudo apt install rabbitmq-server
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server

# Enable management plugin
sudo rabbitmq-plugins enable rabbitmq_management
```

**macOS:**
```bash
brew install rabbitmq
brew services start rabbitmq
```

**Docker:**
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.9-management
```

#### Access Management UI

- URL: `http://localhost:15672`
- Default credentials: `guest` / `guest`

#### Configure Queues

The application auto-creates queues, but you can pre-configure:

```bash
# Create exchange
rabbitmqadmin declare exchange name=websocket.exchange type=topic

# Create queue
rabbitmqadmin declare queue name=websocket.messages durable=true

# Bind queue to exchange
rabbitmqadmin declare binding source=websocket.exchange \
  destination=websocket.messages routing_key="websocket.messages.*"
```

---

### 3. OpenAI Integration

#### Get API Key

1. Sign up at [OpenAI Platform](https://platform.openai.com/)
2. Navigate to API Keys section
3. Create new secret key
4. Add to `application-dev.properties`

```properties
openai.api.key=sk-your-actual-api-key-here
```

#### Test OpenAI Connection

```bash
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer YOUR_API_KEY"
```

---

### 4. File Storage Configuration

#### Local Storage (Development)

```properties
file.storage.type=local
file.upload-dir=./uploads
file.max-size=10MB
```

Create upload directory:
```bash
mkdir -p uploads
chmod 755 uploads
```

#### AWS S3 (Production)

```properties
file.storage.type=s3
aws.s3.bucket-name=samjhadoo-uploads
aws.s3.region=us-east-1
aws.access-key-id=YOUR_ACCESS_KEY
aws.secret-access-key=YOUR_SECRET_KEY
```

Add AWS SDK dependency:
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.500</version>
</dependency>
```

---

## 🏗️ Build & Run

### Development Mode

#### Using Maven

```bash
# Clean and compile
mvn clean compile

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with specific port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

#### Using IDE (IntelliJ IDEA)

1. Open project in IntelliJ
2. Wait for Maven to sync dependencies
3. Right-click on `SamjhadooApplication.java`
4. Select "Run 'SamjhadooApplication'"
5. Or create Run Configuration:
   - Main class: `com.samjhadoo.SamjhadooApplication`
   - VM options: `-Dspring.profiles.active=dev`
   - Environment variables: Add any secrets

#### Using JAR

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/samjhadoo-backend-1.0.0.jar --spring.profiles.active=dev
```

### Production Mode

```bash
# Build with production profile
mvn clean package -Pprod

# Run with production profile
java -jar target/samjhadoo-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=WalletServiceMockTest
```

### Run Integration Tests

```bash
mvn verify -Pfailsafe
```

### Generate Coverage Report

```bash
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

### Test with Different Profiles

```bash
# Test with H2 in-memory database
mvn test -Dspring.profiles.active=test
```

---

## 🐳 Docker Deployment

### Docker Compose Setup

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:14
    container_name: samjhadoo-postgres
    environment:
      POSTGRES_DB: samjhadoo
      POSTGRES_USER: samjhadoo_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - samjhadoo-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U samjhadoo_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:6.2
    container_name: samjhadoo-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - samjhadoo-network
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # RabbitMQ Message Broker
  rabbitmq:
    image: rabbitmq:3.9-management
    container_name: samjhadoo-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-guest}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASS:-guest}
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - samjhadoo-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Backend Application
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: samjhadoo-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/samjhadoo
      SPRING_DATASOURCE_USERNAME: samjhadoo_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_RABBITMQ_HOST: rabbitmq
      JWT_SECRET: ${JWT_SECRET}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    networks:
      - samjhadoo-network
    volumes:
      - ./uploads:/app/uploads
    restart: unless-stopped

networks:
  samjhadoo-network:
    driver: bridge

volumes:
  postgres_data:
  redis_data:
  rabbitmq_data:
```

### Dockerfile

Create `Dockerfile` in backend directory:

```dockerfile
# Build stage
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && chmod 755 /app/uploads

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Environment Variables

Create `.env` file:

```bash
# Database
DB_PASSWORD=your_secure_db_password

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=your_rabbitmq_password

# JWT
JWT_SECRET=your-256-bit-secret-key-for-production

# OpenAI
OPENAI_API_KEY=sk-your-openai-api-key

# AWS (if using S3)
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
```

### Docker Commands

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down

# Rebuild backend
docker-compose up -d --build backend

# Scale backend instances
docker-compose up -d --scale backend=3

# Clean up volumes
docker-compose down -v
```

---

## 🚀 Production Deployment

### AWS Deployment

#### 1. EC2 Setup

```bash
# Launch EC2 instance (Ubuntu 22.04)
# Instance type: t3.medium or larger

# Connect to instance
ssh -i your-key.pem ubuntu@your-ec2-ip

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### 2. RDS PostgreSQL

```bash
# Create RDS instance
# - Engine: PostgreSQL 14
# - Instance class: db.t3.micro (or larger)
# - Storage: 20 GB (with autoscaling)
# - Enable automated backups

# Update connection string
SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-endpoint:5432/samjhadoo
```

#### 3. ElastiCache Redis

```bash
# Create ElastiCache cluster
# - Engine: Redis 6.x
# - Node type: cache.t3.micro (or larger)

# Update Redis host
SPRING_REDIS_HOST=your-elasticache-endpoint
```

#### 4. Amazon MQ (RabbitMQ)

```bash
# Create Amazon MQ broker
# - Broker engine: RabbitMQ
# - Deployment mode: Single-instance or Cluster

# Update RabbitMQ host
SPRING_RABBITMQ_HOST=your-amazonmq-endpoint
```

#### 5. Application Load Balancer

```bash
# Create ALB
# - Target group: Backend instances on port 8080
# - Health check: /actuator/health
# - SSL certificate: From ACM

# Configure security groups
# - Allow 443 (HTTPS) from 0.0.0.0/0
# - Allow 8080 from ALB security group
```

#### 6. S3 for File Storage

```bash
# Create S3 bucket
aws s3 mb s3://samjhadoo-uploads

# Configure bucket policy
# Enable versioning and lifecycle rules
```

#### 7. CloudWatch Monitoring

```bash
# Set up CloudWatch agent
sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb

# Configure metrics and logs
```

---

### Kubernetes Deployment

#### 1. Create Kubernetes Manifests

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: samjhadoo-backend
  labels:
    app: samjhadoo-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: samjhadoo-backend
  template:
    metadata:
      labels:
        app: samjhadoo-backend
    spec:
      containers:
      - name: backend
        image: your-registry/samjhadoo-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

**service.yaml:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: samjhadoo-backend-service
spec:
  selector:
    app: samjhadoo-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

#### 2. Deploy to Kubernetes

```bash
# Create secrets
kubectl create secret generic db-secret \
  --from-literal=url='jdbc:postgresql://...' \
  --from-literal=password='your-password'

# Apply manifests
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods
kubectl get services

# View logs
kubectl logs -f deployment/samjhadoo-backend
```

---

## 📊 Monitoring & Logging

### Application Monitoring

#### Spring Boot Actuator

```properties
# Enable actuator endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

**Available Endpoints:**
- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application info
- `/actuator/prometheus` - Prometheus metrics

#### Prometheus & Grafana

**prometheus.yml:**
```yaml
scrape_configs:
  - job_name: 'samjhadoo-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

**Docker Compose Addition:**
```yaml
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - samjhadoo-network

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    networks:
      - samjhadoo-network
```

### Logging Configuration

#### Logback Configuration

Create `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/samjhadoo.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/samjhadoo.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <!-- Application Logger -->
    <logger name="com.samjhadoo" level="DEBUG"/>
</configuration>
```

#### ELK Stack Integration

```yaml
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"
    networks:
      - samjhadoo-network

  logstash:
    image: docker.elastic.co/logstash/logstash:8.8.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"
    networks:
      - samjhadoo-network

  kibana:
    image: docker.elastic.co/kibana/kibana:8.8.0
    ports:
      - "5601:5601"
    networks:
      - samjhadoo-network
```

---

## 🔍 Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Error:** `Connection refused: connect`

**Solution:**
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check connection
psql -h localhost -U samjhadoo_user -d samjhadoo_dev

# Verify credentials in application.properties
```

#### 2. Redis Connection Failed

**Error:** `Unable to connect to Redis`

**Solution:**
```bash
# Check Redis is running
redis-cli ping

# Check Redis configuration
redis-cli CONFIG GET bind

# Allow remote connections (if needed)
sudo nano /etc/redis/redis.conf
# Comment out: bind 127.0.0.1
```

#### 3. RabbitMQ Connection Failed

**Error:** `Connection refused to RabbitMQ`

**Solution:**
```bash
# Check RabbitMQ status
sudo rabbitmqctl status

# Check management plugin
sudo rabbitmq-plugins list

# Enable management plugin
sudo rabbitmq-plugins enable rabbitmq_management
```

#### 4. Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

#### 5. Out of Memory Error

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution:**
```bash
# Increase heap size
java -Xms512m -Xmx2g -jar samjhadoo-backend.jar

# Or in Maven
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run
```

#### 6. WebSocket Connection Failed

**Error:** `WebSocket connection failed`

**Solution:**
```bash
# Check CORS configuration
# Verify WebSocket endpoint is accessible
curl -i -N -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Host: localhost:8080" \
  -H "Origin: http://localhost:3000" \
  http://localhost:8080/ws

# Check RabbitMQ STOMP plugin
sudo rabbitmq-plugins enable rabbitmq_stomp
```

---

## 📚 Additional Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Support
- GitHub Issues: [Project Issues](https://github.com/yourusername/samjhadoo/issues)
- Email: support@samjhadoo.com

---

## ✅ Deployment Checklist

### Pre-Deployment

- [ ] All tests passing
- [ ] Code reviewed and merged
- [ ] Database migrations prepared
- [ ] Environment variables configured
- [ ] SSL certificates obtained
- [ ] Backup strategy in place
- [ ] Monitoring configured
- [ ] Load testing completed

### Deployment

- [ ] Database backed up
- [ ] Application deployed
- [ ] Health checks passing
- [ ] Smoke tests completed
- [ ] Monitoring alerts configured
- [ ] Documentation updated

### Post-Deployment

- [ ] Monitor application logs
- [ ] Check error rates
- [ ] Verify all features working
- [ ] Performance metrics normal
- [ ] User acceptance testing
- [ ] Rollback plan ready

---

**Last Updated**: October 16, 2025  
**Maintained By**: Samjhadoo DevOps Team
