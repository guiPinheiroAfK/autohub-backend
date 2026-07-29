# AutoHub Backend

> Backend for [autohubbr.netlify.app](https://autohubbr.netlify.app) — automotive community platform (virtual garage, build tracking, social feed, marketplace).

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.4-DC382D?logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.7-231F20?logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Swarm-2496ED?logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-K8s-326CE5?logo=kubernetes&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-Gateway_ALB_CloudFront-FF9900?logo=amazon-aws&logoColor=white)

---

## Architecture

```
React 19 (autohubbr.netlify.app)
  |
  v HTTPS
CloudFront CDN  +  API Gateway (auth, throttle, WAF)
  |
  v
AWS ALB (SSL termination, health checks, canary routing)
  |
  +------+------+
  |             |
App Pod 1    App Pod N   (Spring Boot 3.3 / Java 21 Virtual Threads)
  |
  +-------------------+-------------------+
  |                   |                   |
PostgreSQL 16    Redis 7.4          Apache Kafka
(JPA + Flyway)  (cache/session)   (event streaming)
```

---

## Stack & Rationale

| Layer | Tech | Why |
|---|---|---|
| Language | Java 21 | Virtual Threads (Project Loom): MVC simplicity + async I/O throughput |
| Framework | Spring Boot 3.3.5 | Native K8s probes, Micrometer/Prometheus, Jakarta EE 10 |
| Database | PostgreSQL 16 | ACID + JSON columns for flexible vehicle specs |
| Cache | Redis 7.4 | Cache-aside, session store, rate limiting, sorted-set leaderboard |
| Messaging | Apache Kafka | Decouples write-heavy ops; Outbox pattern for at-least-once delivery |
| Container | Docker Swarm (staging) + Kubernetes (prod) | Swarm = fast setup; K8s = HPA, RBAC, service mesh |
| Cloud | AWS (API GW + ALB + CloudFront + RDS + ElastiCache + MSK) | Managed infra, global CDN, ~60-80% latency reduction |

### Redis Patterns
| Pattern | Use Case |
|---|---|
| Cache-aside (TTL 2 min) | Social feed |
| Centralized session store | Stateless auth |
| Sliding window counter | Rate limiting |
| SET NX EX | Idempotency keys |
| ZADD / ZRANK | AutoDash leaderboard |

### Kafka Topics
| Topic | Producer | Consumer(s) |
|---|---|---|
| `autohub.build.events` | Build Service | Feed Service, Notification Service |
| `autohub.feed.events` | Feed Service | Notification Service |
| `autohub.marketplace.events` | Marketplace Service | Payment Service, Notification Service |
| `autohub.notification.events` | All services | Push/Email workers |

### Docker Swarm vs Kubernetes

| | Docker Swarm | Kubernetes |
|---|---|---|
| Complexity | Low (built-in Docker) | High (dedicated control plane) |
| Setup time | Minutes | Hours |
| Auto-scaling | Manual | HPA (CPU/Memory/custom) |
| Service mesh | Not built-in | Istio / Linkerd |
| Our use | Staging | Production |

---

## Project Structure

```
autohub-backend/
├── src/main/java/com/autohub/
│   ├── AutohubApplication.java          # Entry point
│   ├── config/                          # Spring configs (Redis, Kafka, Security, OpenAPI)
│   ├── domain/                          # Business core — no framework dependencies
│   │   ├── model/                       # JPA entities
│   │   ├── repository/                  # Spring Data interfaces
│   │   ├── service/                     # Business logic
│   │   └── event/                       # Domain events (Kafka payloads)
│   ├── web/                             # HTTP adapters (controllers, DTOs, MapStruct mappers)
│   ├── infrastructure/                  # Outbound adapters (Redis, Kafka, JWT)
│   └── shared/exception/               # Global error handling
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                    # Flyway migrations
├── k8s/base/                            # Deployment + Service + HPA + Ingress
├── swarm/                               # Docker Swarm stack
├── infra/                               # Terraform, API Gateway, CDN config
├── docker-compose.yml                   # Local: PG + Redis + Kafka + Prometheus + Grafana
└── Dockerfile                           # Multi-stage: JDK builder -> JRE runtime (ZGC, non-root)
```

---

## Getting Started

**Prerequisites:** Java 21, Maven 3.9+, Docker + Compose

```bash
git clone https://github.com/guiPinheiroAfK/autohub-backend.git
cd autohub-backend

# Start all infrastructure
docker compose up -d

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

| Service | URL |
|---|---|
| REST API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator health | http://localhost:8080/actuator/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin / autohub) |
| Kafka UI | http://localhost:8090 |

---

## Docker Swarm Deploy (staging)

```bash
docker swarm init

# Create secrets
echo "autohub"               | docker secret create db_password -
echo "your-jwt-secret-256bit" | docker secret create jwt_secret -
echo ""                        | docker secret create redis_password -

# Deploy
docker stack deploy -c swarm/docker-compose.swarm.yml autohub

# Scale
docker service scale autohub_app=5

# Rolling update
docker service update --image guipinheiroafk/autohub-backend:v1.1 autohub_app
```

## Kubernetes Deploy (production)

```bash
kubectl apply -k k8s/base/
kubectl rollout status deployment/autohub-backend
kubectl get hpa autohub-backend-hpa
kubectl logs -l app=autohub-backend -f --tail=100
```

## AWS Infrastructure

```bash
cd infra/terraform/
terraform init
terraform plan -var-file="prod.tfvars"
terraform apply
```

---

## Key Design Decisions (ADRs)

| # | Decision | Reason |
|---|---|---|
| ADR-001 | Hexagonal Architecture | Isolate domain from frameworks; maximizes testability |
| ADR-002 | Outbox Pattern for Kafka | Guarantee at-least-once delivery without distributed transactions |
| ADR-003 | JWT + Redis session blacklist | Stateless auth + ability to revoke tokens |
| ADR-004 | Flyway migrations | Version-controlled schema; reproducible environments |
| ADR-005 | Testcontainers for integration tests | Real PG/Kafka in CI — no mocks |

---

## Observability

```
App → Micrometer → Prometheus → Grafana dashboards
App → SLF4J/Logback → (ELK / CloudWatch in prod)
App → Spring Actuator → K8s liveness/readiness probes
```

Key metrics: `autohub_http_requests_total`, `autohub_feed_cache_hit_ratio`, `autohub_kafka_consumer_lag`, JVM memory/threads.

---

## Roadmap

- [x] Project scaffolding + Docker Compose
- [x] Docker Swarm stack
- [x] Kubernetes manifests + HPA
- [ ] Flyway V1 schema (users, vehicles, builds, feed, marketplace)
- [ ] JWT auth (register / login / refresh)
- [ ] Garage domain (Vehicle + Build phases)
- [ ] Feed service with Redis cache
- [ ] Kafka producers/consumers
- [ ] Marketplace listings
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Terraform AWS infrastructure
- [ ] Rate limiting (Redis sliding window)
- [ ] WebSocket live feed

---

MIT © [Guilherme Pinheiro](https://github.com/guiPinheiroAfK)
