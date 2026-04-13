# SpotFlow High-Concurrency Backend

A backend-focused Spring Boot project demo that combines **JWT authentication**, **Redis caching**, **RabbitMQ-based asynchronous processing**, and **distributed locking** to support high-concurrency voucher ordering and social interaction workflows.

## Overview

SpotFlow is a Java backend project built to practice production-oriented backend patterns beyond basic CRUD.  
The project includes user authentication, shop and voucher REST APIs, blog interactions, Redis-based performance optimization, and a flash-sale ordering pipeline designed for high-concurrency scenarios.

This project focuses on:
- high-concurrency request handling
- cache design with Redis
- asynchronous order processing with RabbitMQ
- distributed locking with Redisson
- JWT-based authentication
- containerized deployment with Docker

## Tech Stack

- **Java 17**
- **Spring Boot 3.5**
- **Spring Data JPA**
- **MySQL**
- **Redis**
- **RabbitMQ**
- **Redisson**
- **JWT (jjwt)**
- **Swagger / OpenAPI**
- **Docker / Docker Compose**
- **Lombok**

## Features

- JWT-based authentication
- Redis-backed token blacklist logout
- Rest APIs
- High-concurrency voucher ordering
- Blog publishing and like/unlike support
- Follow/unfollow support
- Redis bitmap-based daily sign-in tracking
- Operation logging with AOP
- Swagger API documentation

## High-Concurrency Design

The flash-sale workflow is the main technical highlight of the project.

### Ordering pipeline
1. A request first goes through a **Redis Lua script**
2. The script atomically checks:
   - whether stock is available
   - whether the user has already placed an order
3. If validation passes, an order message is sent to **RabbitMQ** queue.
4. A consumer processes the message asynchronously
5. **Redisson distributed locking** is used to enforce one-user-one-order for double check
6. The final order is created transactionally in MySQL using jpa

This design reduces database pressure during peak traffic and helps prevent overselling and duplicate orders.

## Redis Usage

Redis is used in several ways across the project:

- **Cache penetration protection** by caching empty values
- **Logical expiration** for hotspot cache rebuild
- **Bitmap sign-in tracking** for efficient attendance/check-in features
- **Token blacklist** for logout handling
- **Lua scripting** for atomic seckill validation
