# ResolveX – Intelligent Enterprise Service Management Platform

ResolveX is a commercial-grade, multi-tenant enterprise service management platform engineered with **Java 21, Spring Boot 3, Spring Security, JWT, PostgreSQL, Flyway, WebSocket (STOMP), React 19, TypeScript, Vite, and Tailwind CSS**.

The platform orchestrates customer service requests, internal IT incidents, automated workload routing, service-level agreement (SLA) lifecycle monitoring, real-time push alerts, and role-specific analytics dashboards.

---

## 1. Core Platform Capabilities

- **Role-Based Access Control (RBAC):** Backend method-level security (`@EnableMethodSecurity`, `@PreAuthorize`) and frontend protected routing enforcing strict isolation across 4 roles: `ADMIN`, `MANAGER`, `EMPLOYEE`, and `CUSTOMER`.
- **Intelligent Automated Ticket Assignment:** A transparent, deterministic rule-based assignment engine that evaluates technical pod memberships, department category alignment, active availability, and selects candidates with the lowest active workload count (breaking ties deterministically by ID).
- **SLA Lifecycle & Breach Escalation Engine:** Priority-based resolution and response deadlines (CRITICAL, HIGH, MEDIUM, LOW) monitored by an idempotent scheduled backend daemon with automatic manager escalations.
- **Real-Time Push Notifications:** Full-duplex WebSocket / STOMP channels broadcasting assignment alerts, status transitions, and SLA warnings with persistent database storage and REST fallback.
- **AI-Assisted Ticket Classification:** Isolated classification service interface with Google Gemini API integration and an intelligent local heuristic keyword fallback that suggests category, priority, and links relevant self-service Knowledge Base articles with user confirmation.
- **Enterprise SaaS UX:** Modern responsive interface with 19 dedicated views, Recharts telemetry visualizations, custom Tailwind theme, modals, and toast notifications.

---

## 2. Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4.3, Spring Security 6, JWT (JJWT 0.12.6), Spring Data JPA, Hibernate, WebSocket / STOMP, Flyway 10, SpringDoc OpenAPI 2.8 |
| **Frontend** | React, TypeScript, Vite, Tailwind CSS v4, React Router v7, Axios, Recharts, Lucide React, SockJS / StompJS |
| **Database** | PostgreSQL 16 (Production / Docker), H2 In-Memory (Local / Test fallback) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **DevOps** | Docker, Multi-Stage Dockerfile, Docker Compose, Nginx |

---

## 3. Demo Login Credentials

All seeded demo accounts are initialized with the password: **`Password@123`**

| Role | Username | Email | Department / Scope |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin@resolvex.com` | Full System Administrator |
| **MANAGER** | `manager.it` | `manager.it@resolvex.com` | IT Infrastructure Manager |
| **MANAGER** | `manager.ops` | `manager.ops@resolvex.com` | Service Desk & Customer Ops Manager |
| **EMPLOYEE** | `tech.john` | `tech.john@resolvex.com` | Infrastructure & Workplace Tech Agent |
| **EMPLOYEE** | `tech.sarah` | `tech.sarah@resolvex.com` | Network & Helpdesk Specialist |
| **EMPLOYEE** | `tech.alex` | `tech.alex@resolvex.com` | Tier-2 Customer Operations Specialist |
| **EMPLOYEE** | `tech.emma` | `tech.emma@resolvex.com` | Incident Response Specialist |
| **EMPLOYEE** | `tech.michael`| `tech.michael@resolvex.com`| Cloud & Application Support Specialist |
| **CUSTOMER** | `customer.alice` | `customer.alice@acme.com` | Acme Corp Requester |
| **CUSTOMER** | `customer.bob` | `customer.bob@globex.com` | Globex Corp Requester |
| **CUSTOMER** | `customer.charlie` | `customer.charlie@initech.com`| Initech Requester |

*Note: The login page includes convenient 1-click Quick Login buttons for instant role switching.*

---

## 4. Application Architecture

```
ResolveX/
├── backend/
│   ├── src/main/java/com/resolvex/
│   │   ├── config/          # Security, OpenAPI, DataSeeder
│   │   ├── controller/      # REST Endpoints (Auth, Ticket, Dashboard, SLA, KB, etc.)
│   │   ├── dto/             # Request & Response Contracts
│   │   ├── entity/          # JPA Entities (Ticket, User, SLA, Audit, etc.)
│   │   ├── enums/           # RoleName, TicketPriority, TicketStatus
│   │   ├── exception/       # Centralized Exception Handler & API Responses
│   │   ├── repository/      # Spring Data Repositories & JPA Specifications
│   │   ├── security/        # JWT Authentication Filter, UserPrincipal, BCrypt
│   │   ├── service/         # Business Logic, AssignmentEngine, SLA Monitor
│   │   ├── specification/   # Dynamic Criteria Query Predicates
│   │   └── websocket/       # STOMP Broker Configuration
│   ├── src/main/resources/
│   │   ├── application.yml        # Base & PostgreSQL Profile
│   │   ├── application-local.yml  # H2 Standalone Local Profile
│   │   └── db/migration/          # Flyway V1 (Schema) & V2 (Seed Data)
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/      # Badges, Modals, Navbar, Sidebar, ProtectedRoute
│   │   ├── context/         # AuthContext & NotificationContext
│   │   ├── layouts/         # MainLayout (Dashboard) & AuthLayout
│   │   ├── pages/           # 19 Views (Dashboards, Tickets, Teams, Users, etc.)
│   │   ├── services/        # Axios API clients, WebSocket Client
│   │   └── types/           # Domain TypeScript Interfaces
│   ├── nginx.conf           # Reverse Proxy & SPA Routing
│   └── Dockerfile
├── docker-compose.yml       # PostgreSQL, Backend, Frontend orchestration
└── .env.example             # Documented Environment Variables
```

---

## 5. Quick Start Instructions

### Option A: Running with Docker Compose (Recommended)

1. Clone or navigate to the repository directory:
   ```bash
   cd ResolveX
   ```

2. Launch all services (PostgreSQL, Backend API, Frontend Web):
   ```bash
   docker-compose up --build
   ```

3. Access the applications:
   - **Web UI:** [http://localhost:3000](http://localhost:3000)
   - **Backend API & Swagger:** [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
   - **OpenAPI JSON Spec:** [http://localhost:8088/v3/api-docs](http://localhost:8088/v3/api-docs)

---

### Option B: Running Locally (Development Mode)

#### 1. Prerequisites
- **Java 21+** (`java -version`)
- **Maven 3.9+** (`mvn -v` or use bundled Maven)
- **Node.js 20+** & **npm** (`node -v`)

#### 2. Start the Backend API
The backend automatically runs with an embedded in-memory database and baseline seed data on port `8088`:
```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```
*The backend API will start on `http://localhost:8088` and initialize 20 realistic sample tickets, roles, and SLA policies.*

#### 3. Start the Frontend Application
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
*Open [http://localhost:5173](http://localhost:5173) in your browser.*

---

## 6. Running Automated Tests

### Backend Test Suite
Executes unit tests and integration tests covering JWT authentication, ticket status transition state machines, deterministic workload routing, and SLA due date calculations:
```bash
cd backend
mvn test "-Dspring.profiles.active=test"
```

### Frontend Build & Type Validation
Validates all TypeScript interfaces, component props, and production asset bundles:
```bash
cd frontend
npm run build
```

---

## 7. REST API Endpoints Overview

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | Public |
| `POST` | `/api/auth/register` | Register new user account | Public |
| `GET` | `/api/auth/me` | Fetch authenticated user profile | Authenticated |
| `GET` | `/api/tickets` | Search/filter tickets with pagination & sorting | Authenticated |
| `POST` | `/api/tickets` | Create ticket with auto-routing | Authenticated |
| `GET` | `/api/tickets/{id}` | Detailed ticket view (history & comments) | Authenticated |
| `PATCH`| `/api/tickets/{id}/status` | Validate & transition ticket status | Authenticated |
| `POST` | `/api/tickets/{id}/assign` | Reassign ticket (manual or auto-engine) | Manager / Admin |
| `POST` | `/api/tickets/{id}/comments` | Post public or internal staff comment | Authenticated |
| `POST` | `/api/tickets/{id}/attachments`| Secure file upload (10MB limit) | Authenticated |
| `GET` | `/api/dashboard/admin` | Executive telemetry & system KPI | Admin |
| `GET` | `/api/dashboard/manager`| Team capacity & backlog distribution | Manager / Admin |
| `GET` | `/api/dashboard/employee`| My queue & upcoming SLA deadlines | Employee / Manager |
| `GET` | `/api/dashboard/customer`| Customer active & resolved requests | Customer |
| `POST` | `/api/ai/analyze-ticket` | AI classification & KB recommendations | Authenticated |
| `GET` | `/api/sla/policies` | List SLA response/resolution policies | Authenticated |
| `PUT` | `/api/sla/policies/{id}` | Update SLA policy thresholds | Admin |
| `GET` | `/api/sla/escalations` | List active SLA breach escalations | Manager / Admin |
| `GET` | `/api/knowledge-base` | Search & browse self-service articles | Authenticated |
| `GET` | `/api/notifications` | Paginated notifications feed | Authenticated |
| `GET` | `/api/audit-logs` | Immutable system compliance audit trails | Admin |
| `GET` | `/api/health` | Service health check | Public |

---

## 8. License

ResolveX is licensed under the Apache 2.0 Enterprise License.
