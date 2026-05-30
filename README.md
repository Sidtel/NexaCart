# E-Commerce Microservices Platform

A production-grade, distributed e-commerce backend built with Java 17, Spring Boot, and microservices architecture. Designed to demonstrate scalable system design patterns used at companies like Amazon, Netflix, and Flipkart.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                          FRONTEND / CLIENT                           │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY (:8080)                            │
│              JWT Validation │ Rate Limiting │ Routing                 │
└──────┬──────────────┬──────────────┬──────────────┬─────────────────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│   User     │ │  Product   │ │   Order    │ │  Payment   │
│  Service   │ │  Service   │ │  Service   │ │  Service   │
│  (:8081)   │ │  (:8082)   │ │  (:8083)   │ │  (:8084)   │
└─────┬──────┘ └─────┬──────┘ └──┬───┬─────┘ └──┬───┬─────┘
      │               │           │   │          │   │
      ▼               ▼           │   │          │   │
┌──────────┐   ┌──────────┐      │   │          │   │
│ MongoDB  │   │ MongoDB  │      │   │          │   │
│ user_db  │   │product_db│      │   │          │   │
└──────────┘   └──────────┘      │   │          │   │
                                  │   │          │   │
       ┌──────────────────────────┘   │          │   │
       │  REST (Product validation)   │          │   │
       ▼                              │          │   │
┌──────────┐                          │          │   │
│ MongoDB  │                          │          │   │
│ order_db │                          │          │   │
└──────────┘                          │          │   │
                                      │          │   │
                    ┌─────────────────┘          │   │
                    │  Kafka: order-events        │   │
                    ▼                             │   │
            ┌──────────────┐                     │   │
            │    KAFKA     │◄────────────────────┘   │
            │   BROKER     │  Kafka: payment-events  │
            └──────┬───────┘                         │
                   │                                 │
                   ▼                                 │
         ┌────────────────┐                          │
         │  Notification  │◄─────────────────────────┘
         │   Service      │
         │   (:8085)      │
         └────────────────┘
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT |
| Database | MongoDB |
| Messaging | Apache Kafka |
| Containerization | Docker |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| Cloud | AWS-ready (EKS) |
| Build Tool | Maven |

## Services

| Service | Port | Responsibility |
|---------|------|---------------|
| API Gateway | 8080 | Routing, JWT validation, rate limiting |
| User Service | 8081 | Authentication, authorization, user management |
| Product Service | 8082 | Product catalog, inventory management |
| Order Service | 8083 | Order processing, product validation via REST |
| Payment Service | 8084 | Payment processing (event-driven) |
| Notification Service | 8085 | Email/SMS notifications (event-driven) |

## Event Flow

```
1. User places order via API Gateway
2. Order Service validates products (REST → Product Service)
3. Order Service reduces inventory (REST → Product Service)
4. Order Service publishes OrderPlacedEvent (Kafka)
5. Payment Service consumes event → processes payment
6. Payment Service publishes PaymentCompletedEvent (Kafka)
7. Notification Service consumes both events → sends notifications
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.9+

### Run with Docker Compose

```bash
# Clone the repository
git clone <repository-url>
cd ecommerce-platform

# Start all services
docker-compose up --build

# Stop all services
docker-compose down
```

### Run Locally (Development)

```bash
# Start infrastructure only
docker-compose up mongodb zookeeper kafka -d

# Build all services
mvn clean install -DskipTests

# Run each service (in separate terminals)
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

## API Documentation

### Authentication

```bash
# Register
POST http://localhost:8080/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "address": "123 Main St"
}

# Login
POST http://localhost:8080/auth/login
{
  "email": "john@example.com",
  "password": "securePass123"
}

# Get Profile (requires JWT)
GET http://localhost:8080/users/profile
Authorization: Bearer <token>
```

### Products

```bash
# Create Product
POST http://localhost:8080/products
{
  "name": "MacBook Pro 16",
  "description": "Apple M3 Pro chip, 18GB RAM",
  "category": "Electronics",
  "price": 2499.99,
  "stock": 50,
  "imageUrl": "https://example.com/macbook.jpg"
}

# Get All Products (with pagination & filtering)
GET http://localhost:8080/products?page=0&size=10&category=Electronics&search=MacBook

# Get Product by ID
GET http://localhost:8080/products/{id}

# Update Product
PUT http://localhost:8080/products/{id}

# Delete Product (soft delete)
DELETE http://localhost:8080/products/{id}
```

### Orders

```bash
# Place Order (requires JWT)
POST http://localhost:8080/orders
Authorization: Bearer <token>
{
  "userId": "user123",
  "items": [
    { "productId": "prod1", "quantity": 2 },
    { "productId": "prod2", "quantity": 1 }
  ],
  "shippingAddress": "456 Oak Ave, City, State 12345"
}

# Get Order by ID
GET http://localhost:8080/orders/{id}

# Get Orders by User
GET http://localhost:8080/orders/user/{userId}
```

## Kafka Topics

| Topic | Producer | Consumers |
|-------|----------|-----------|
| `order-events` | Order Service | Payment Service, Notification Service |
| `payment-events` | Payment Service | Notification Service |

## Kubernetes Deployment

```bash
# Apply all K8s manifests
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n ecommerce
kubectl get services -n ecommerce
```

## Project Structure

```
ecommerce-platform/
├── api-gateway/                 # Spring Cloud Gateway
├── user-service/                # Auth & User Management
├── product-service/             # Product Catalog
├── order-service/               # Order Processing
├── payment-service/             # Payment Processing
├── notification-service/        # Notifications
├── k8s/                         # Kubernetes manifests
├── .github/workflows/           # CI/CD pipelines
├── docker-compose.yml           # Local orchestration
└── pom.xml                      # Parent POM
```

Each service follows:
```
service-name/
├── src/main/java/com/ecommerce/{service}/
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Data access
│   ├── entity/              # Domain models
│   ├── dto/                 # Data transfer objects
│   ├── config/              # Configuration classes
│   ├── security/            # Security components
│   ├── exception/           # Exception handling
│   ├── kafka/               # Event producers/consumers
│   └── util/                # Utilities
├── src/main/resources/
│   └── application.yml      # Configuration
├── Dockerfile               # Container definition
└── pom.xml                  # Dependencies
```

## Design Decisions

1. **MongoDB over SQL**: Document-oriented storage suits e-commerce data (flexible product schemas, nested order items)
2. **Kafka over RabbitMQ**: Better suited for high-throughput event streaming with replay capability
3. **WebClient over RestTemplate**: Non-blocking HTTP client for service-to-service calls
4. **Soft deletes**: Products are deactivated rather than deleted to preserve order history integrity
5. **Idempotent consumers**: Payment service checks for existing payments before processing
6. **Gateway-level auth**: JWT validation at gateway reduces load on downstream services
7. **Per-service databases**: Each service owns its data (database-per-service pattern)

## Production Considerations

- [ ] Add Redis for distributed rate limiting and caching
- [ ] Implement Circuit Breaker (Resilience4j) for service calls
- [ ] Add distributed tracing (Zipkin/Jaeger)
- [ ] Implement Saga pattern for distributed transactions
- [ ] Add API versioning
- [ ] Implement CQRS for read-heavy services
- [ ] Add ELK stack for centralized logging
- [ ] Configure Prometheus + Grafana for monitoring

## License

MIT
