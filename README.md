# bandi-monitor

Sistema di monitoraggio bandi pubblici italiani con AI locale, focalizzato su forniture di
laboratori informatici, tecnici e scientifici per istituti scolastici e altri enti.
Scarica automaticamente i dataset da dati.consip.it, indicizza i bandi con embedding vettoriali
e genera riassunti valutati tramite RAG con un modello Llama eseguito localmente via Ollama.

## Stack

- **Backend**: Spring Boot 3.4 + Spring AI 1.0
- **Frontend**: Thymeleaf (stesso JAR)
- **LLM**: Ollama con modello `llama3.2`
- **Embedding**: Ollama con modello `nomic-embed-text` (768 dimensioni)
- **Vector store**: PGVector (PostgreSQL 16)
- **DB migrations**: Flyway

## Prerequisiti

- Java 21
- Docker + Docker Compose

## Come avviare (stack completo con Docker)

```bash
# 1. Build del JAR
mvn clean package -DskipTests

# 2. Avvio dello stack
docker-compose up -d

# 3. Scarica i modelli Ollama (solo al primo avvio)
docker exec bandi-monitor-ollama-1 ollama pull nomic-embed-text
docker exec bandi-monitor-ollama-1 ollama pull llama3.2
```

L'applicazione sarà disponibile su http://localhost:8080

## Come avviare in sviluppo (solo DB + Ollama via Docker)

```bash
# Avvia solo Postgres e Ollama
docker-compose up -d db ollama

# Avvia l'applicazione con profilo dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Struttura del progetto

```
src/main/java/it/bandimonitor/
├── client/       # OpenFeign clients (es. CKAN API di dati.consip.it)
├── config/       # Spring configuration
├── domain/       # Data Transfer Objects
├── entity/       # Entità JPA
├── event/        # Domain events
├── repository/   # Spring Data JPA repositories
├── service/      # Business logic
└── web/
    ├── error/    # Error handling
    ├── filter/   # Request filters
    ├── rest/     # REST controllers
    ├── support/  # Support classes
    └── vm/       # View Model objects per Thymeleaf
```

## Variabili d'ambiente

| Variabile          | Default                                      | Descrizione                     |
|--------------------|----------------------------------------------|---------------------------------|
| `DB_URL`           | `jdbc:postgresql://localhost:5432/bandi_monitor` | URL JDBC del database       |
| `DB_USERNAME`      | `bandi_monitor`                              | Username PostgreSQL             |
| `DB_PASSWORD`      | `bandi_monitor`                              | Password PostgreSQL             |
| `OLLAMA_URL`       | `http://localhost:11434`                     | URL del server Ollama           |
| `OLLAMA_CHAT_MODEL`| `llama3.2`                                   | Modello chat da usare           |
