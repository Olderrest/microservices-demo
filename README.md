# microservices-demo

Project with focus on the development of microservices. Developed microservice architecture from scratch using the most recent software platforms, technologies, libraries and tools, following best practices, applying microservices patterns.

Key concepts that were applied during the development process:

 - Externalized configuration with Spring Cloud Config
 - CQRS with Kafka and Elastic search
 - Api versioning for versioning of Rest APIs
 - Service Registration and Discovery with Spring Cloud and Netflix Eureka
 - Api Gateway with Spring Cloud Gateway
 - Circuit breaker with Spring Cloud Gateway and Resilience4j
 - Rate limiting with Spring Cloud Gateway and Redis to use Redis as the Rate limiter
 - Distributed tracing with SLF4J MDC, Spring Cloud Sleuth and Zipkin
 - Log aggregation with ELK stack (Elasticsearch, Logstash and Kibana)
 - Client side load balancing with Spring Cloud Load Balancer
 - Database per Service
 - Messaging between microservices using Kafka

 

## Appendix

Project developed based on spring boot using Java, Spring boot, Spring cloud, Spring Security, Kafka and Elasticsearch. Also applied Event sourcing and Event-driven approches using Kafka as the event store. Also implemented Spring Security Oauth 2.0 and OpenID connect protocols for Authentication and Authorization using Keycloak and JWT. To monitoring microservices health and gather metrics was used Spring Boot Actuator along with Prometheus with Micrometer. For distributed tracing used Slf4J MDC, Spring Cloud Sleuth and Zipkin. Docker as containerization tool.



## Tech Stack

**Server:** Java 17, Spring boot and Spring cloud stack, Kafka as the Event Store, PostgreSQL, Docker, ELK, Prometheus, Grafana

**Client**: Thymeleaf, Sptring Web


