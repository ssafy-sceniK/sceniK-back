# LEARNED_TECH_STACK.md

## Purpose
This project must use only technologies the student has learned unless the user explicitly approves an exception.

The default MVP stack is:

```text
Vue + plain CSS
Spring Boot + Spring MVC + Spring REST API
MyBatis + MySQL-compatible relational database
MockAiRouteGenerator first, Spring AI later
```

## Allowed Technologies

### Frontend
- HTML
- CSS
- JavaScript
- JavaScript Event
- WebStorage
- Ajax
- Vue
- Vue Basic Syntax
- Vue SFC
- Vue Router
- Vue State Management
- Vue server integration

### Backend
- Java
- Servlet
- JSP
- Cookie
- Session
- JSTL
- EL
- Filter
- SQL
- DML
- DDL
- Join
- SubQuery
- JDBC
- Spring DI
- Spring AOP
- Spring MVC
- Spring Boot
- Spring Interceptor
- Spring File Upload
- MyBatis
- MyBatis dynamic query
- Spring REST API
- Spring Security
- JWT
- Spring AI

### Database
- MySQL-compatible relational database
- SQL schema and seed data
- JDBC / MyBatis based persistence

### AI
- Spring AI is allowed.
- For the first MVP, use a `MockAiRouteGenerator`.
- Real Spring AI integration can be added later.
- RAG must start from database-backed candidate places.
- Do not introduce an external Vector DB unless the user explicitly approves it later.

## Forbidden Technologies

Do not use the following unless the user explicitly approves them later:

- React
- Redux
- Zustand
- Next.js
- Nuxt
- TypeScript
- Tailwind CSS
- shadcn/ui
- Radix UI
- Material UI
- Bootstrap
- External UI component libraries
- Node backend frameworks
- Django
- FastAPI
- JPA
- Hibernate
- Prisma
- PostgreSQL
- MongoDB
- Redis
- Docker
- Kubernetes
- External Vector DB
- Any unlearned framework, library, database, infrastructure tool, or architecture pattern

## Default MVP Stack

### Frontend
- Use Vue.
- Use Vue SFC files for components.
- Use Vue Router only when multiple screens are implemented.
- Use simple Vue state management first.
- Use Ajax / `fetch` for server integration.
- Use WebStorage only for small client-side state when appropriate.
- Do not use React or TypeScript.

### Backend
- Use Spring Boot.
- Use Spring MVC and Spring REST API for backend endpoints.
- Use Spring Security and JWT when authentication is implemented.
- Use Spring Interceptor or Filter only when the project needs request-level behavior.
- Keep service classes simple and readable.
- Prefer straightforward class-level implementations that match the student's learning level.

### Persistence
- Use MyBatis, MyBatis dynamic query, SQL, and JDBC concepts.
- Do not use JPA or Hibernate.
- Store data in a MySQL-compatible relational database.
- Use SQL schema and seed data for initial MVP data.

## AI Implementation Strategy

### First MVP
- Use `MockAiRouteGenerator`.
- It must return the same response shape expected from the later AI integration.
- It must only choose places from database-backed candidate places.
- It must include valid `placeId` values.
- It must validate day number, visit order, and required fields before returning or saving a route.

### Later Integration
- Spring AI may replace the mock generator later.
- The backend must still retrieve candidate places from MySQL through MyBatis first.
- The AI prompt must receive only approved candidate places.
- Final AI output must be validated before it is returned or saved.

### RAG Constraint
- RAG starts from database-backed candidate places.
- For MVP, build context from `contents`, `places`, and `content_places` relational data.
- Do not add an external Vector DB unless the user explicitly approves it in a later task.

## Styling Strategy
- Use plain CSS, CSS variables, or Vue scoped CSS.
- Keep design tokens as CSS variables when practical.
- Do not use Tailwind CSS, Bootstrap, Material UI, shadcn/ui, Radix UI, or external UI libraries.
- Keep the selected visual direction: `Scene Trip Notebook`.
- All user-facing app text must be Korean.

## Stop-and-Report Rule
If a feature seems to require an unlearned technology, stop before implementing it and report:
- the required capability,
- why the learned stack may not be enough,
- the simplest learned-stack alternative,
- whether explicit user approval is needed.
