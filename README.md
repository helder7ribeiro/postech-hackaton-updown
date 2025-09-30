# UpDown - API de Processamento de Vídeos

API REST para upload e processamento de vídeos com autenticação JWT e integração AWS.

## 🚀 Tecnologias

- **Java 21** + **Spring Boot 3.4.0**
- **PostgreSQL** + **Flyway** (migrações)
- **AWS S3** (armazenamento) + **SQS** (mensageria)
- **AWS Cognito** (autenticação JWT)
- **Docker** + **Kubernetes**
- **Swagger/OpenAPI** (documentação)
- **Cucumber** (testes BDD)

## 📋 Funcionalidades

### 👤 Gestão de Usuários
- `POST /api/v1/app-users` - Criar usuário
- `GET /api/v1/app-users/{id}` - Buscar usuário
- `PUT /api/v1/app-users/{id}` - Atualizar usuário
- `DELETE /api/v1/app-users/{id}` - Remover usuário
- `GET /api/v1/app-users/exists?email={email}` - Verificar existência

### 🎬 Processamento de Jobs
- `POST /api/v1/jobs` - Upload de vídeo (multipart/form-data)
- `GET /api/v1/jobs/{id}` - Consultar status do job
- `PUT /api/v1/jobs/{id}` - Atualizar job
- `DELETE /api/v1/jobs/{id}` - Remover job
- `GET /api/v1/jobs/exists/{id}` - Verificar existência

## 🔧 Configuração

### Variáveis de Ambiente
```bash
# AWS
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_SESSION_TOKEN=your_session_token

# Cognito
COGNITO_USER_POOL_ID=your_pool_id
COGNITO_CLIENT_ID=your_client_id
COGNITO_REGION=us-east-1
COGNITO_JWKS_URI=your_jwks_uri
COGNITO_ISSUER_URI=your_issuer_uri

# Database
POSTGRES_DATASOURCE_URL=jdbc:postgresql://localhost:5432/updown
POSTGRES_USER=updown_user
POSTGRES_PASSWORD=updown_password
```

## 🏃‍♂️ Execução

### Local
```bash
# Com Maven
./mvnw spring-boot:run

# Com Docker
docker build -t updown .
docker run -p 8080:8080 updown
```

### Kubernetes
```bash
kubectl apply -f k8s/
```

## 📚 Documentação

- **Swagger UI**: http://localhost:8080/swagger
- **API Docs**: http://localhost:8080/api-docs

## 🧪 Testes

```bash
# Testes unitários
./mvnw test

# Testes BDD (Cucumber)
./mvnw test -Dtest=CucumberIntegrationTest
```

## 📁 Estrutura

```
src/
├── main/java/org/fiap/updown/
│   ├── application/     # Casos de uso e ports
│   ├── domain/          # Modelos e regras de negócio
│   └── infrastructure/  # Adaptadores (REST, DB, AWS)
└── test/               # Testes unitários e BDD
```
