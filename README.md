# 🏆 HackHub2026

*HackHub2026* è una piattaforma backend RESTful per la gestione completa di hackathon, sviluppata come progetto per il corso di *Ingegneria del Software (IDS)* presso l'*Università di Camerino (UNICAM)*.

Il sistema consente a organizzatori, mentori e partecipanti di gestire l'intero ciclo di vita di un hackathon: dalla creazione dell'evento alla proclamazione del vincitore, passando per la gestione dei team, delle submission, delle valutazioni, dei report e delle richieste di supporto.

---

## 📋 Indice

- [Prerequisiti](#-prerequisiti)
- [Installazione](#-installazione)
- [Avvio dell'applicazione](#-avvio-dellapplicazione)
- [Stack Tecnologico](#-stack-tecnologico)
- [Architettura del Progetto](#-architettura-del-progetto)
- [Documentazione API (Swagger)](#-documentazione-api-swagger)

---

## ⚙️ Prerequisiti

Prima di iniziare, assicurati di avere installato:

| Requisito | Versione minima | Note |
|---|---|---|
| *Java JDK* | 21+ | Necessario per Spring Boot 4.x |
| *Git* | qualsiasi | Per clonare il repository |

> *Nota:* Non è necessario installare Gradle. Il progetto include il *Gradle Wrapper* (gradlew / gradlew.bat) che scarica automaticamente la versione corretta (Gradle 9.2.0).

---

## 📥 Installazione

### 1. Clonare il repository

bash
git clone https://github.com/SickLuca/HackHub2026.git
cd HackHub2026


### 2. Verificare la versione di Java

bash
java -version


Assicurati che l'output mostri la versione *21* o superiore.

---

## 🚀 Avvio dell'applicazione

### Su Windows

bash
.\gradlew.bat bootRun


### Su macOS / Linux

bash
./gradlew bootRun


Una volta avviata, l'applicazione sarà accessibile su:

| Servizio | URL |
|---|---|
| *Applicazione* | http://localhost:8080 |
| *Swagger UI* | http://localhost:8080/swagger-ui/index.html |

---

## 🛠️ Stack Tecnologico

### Framework & Runtime

| Tecnologia | Versione | Descrizione |
|---|---|---|
| *Spring Boot* | 4.0.1 | Framework principale per la creazione di applicazioni Java enterprise-ready. Gestisce auto-configurazione, dependency injection (IoC) e server embedded (Tomcat). |
| *Spring Web (MVC)* | — | Modulo per la costruzione di API REST tramite annotazioni (@RestController, @GetMapping, ecc.). |
| *Spring Data JPA* | — | Astrae l'accesso al database tramite interfacce Repository, eliminando la necessità di scrivere query SQL manuali per le operazioni CRUD. |
| *Spring Security* | — | Framework per l'autenticazione e l'autorizzazione. Nel progetto gestisce l'accesso tramite token JWT. |
| *Spring Validation* | — | Fornisce annotazioni di validazione (@NotBlank, @Min, @Email, ecc.) per validare automaticamente i DTO in ingresso alle API. |
| *Spring Scheduling* | — | Permette l'esecuzione di task pianificati (@Scheduled) per operazioni automatiche come la chiusura degli hackathon scaduti. |
| *Gradle* | 9.2.0 | Build tool per la compilazione, gestione delle dipendenze e packaging del progetto. |
| *Java* | 21+ | Linguaggio di programmazione e runtime. |

### Database

| Tecnologia | Descrizione |
|---|---|
| *H2 Database* | Database relazionale in-memory, ideale per sviluppo e testing. I dati vengono ricreati ad ogni avvio dell'applicazione tramite un DatabaseSeeder. |
| *Hibernate (JPA)* | ORM (Object-Relational Mapping) che mappa le classi Java annotate con @Entity nelle tabelle del database. Configurato in modalità ddl-auto=update. |

### Sicurezza

| Tecnologia | Versione | Descrizione |
|---|---|---|
| *JJWT (JSON Web Token)* | 0.11.5 | Libreria per la generazione, firma e validazione di token JWT. Utilizzata per l'autenticazione stateless delle API. Comprende i moduli jjwt-api, jjwt-impl e jjwt-jackson. |

### Librerie di Utilità

| Tecnologia | Descrizione |
|---|---|
| *Lombok* | Libreria di code-generation a compile-time. Riduce il boilerplate generando automaticamente getter, setter, costruttori, equals, hashCode e toString tramite annotazioni come @Data, @Getter, @AllArgsConstructor, @Builder, ecc. |
| *SpringDoc OpenAPI* (v2.7.0) | Genera automaticamente la documentazione OpenAPI 3.0 dalle annotazioni dei controller Spring e la espone tramite un'interfaccia web interattiva (Swagger UI). |

### Testing

| Tecnologia | Descrizione |
|---|---|
| *Spring Boot Starter Test* | Modulo che include tutte le dipendenze necessarie per il testing: *JUnit 5* (framework di test), *Mockito* (mocking), *AssertJ* (asserzioni fluide) e *Spring Test* (contesto di test per Spring). |

---

## 🏗️ Architettura del Progetto

Il progetto segue un'architettura *a strati (layered architecture)* tipica delle applicazioni Spring Boot:

```text
src/main/java/it/unicam/cs/ids/
│
├── Main.java                      # Entry point dell'applicazione
│
├── config/                        # Configurazione dell'applicazione
│   ├── DatabaseSeeder.java        # Popola il DB con dati di esempio all'avvio
│   └── OpenApiConfig.java         # Configurazione Swagger/OpenAPI con JWT
│
├── controllers/                   # Layer di presentazione (REST API)
│   ├── AuthController.java        # Registrazione e login
│   ├── HackathonController.java   # CRUD e gestione hackathon
│   ├── InvitationController.java  # Gestione inviti ai team
│   ├── ReportController.java       # Segnalazioni e report
│   ├── SubmissionController.java   # Invio e valutazione submission
│   ├── SupportRequestController.java # Richieste di supporto
│   └── TeamController.java        # Gestione team
│
├── dtos/                          # Data Transfer Objects
│   ├── requests/                  # DTO per le richieste in ingresso (17 DTO)
│   └── responses/                 # DTO per le risposte in uscita (8 DTO)
│
├── exceptions/                    # Gestione centralizzata degli errori
│   ├── GlobalExceptionHandler.java    # Handler globale (@RestControllerAdvice)
│   ├── ApiErrorResponseDTO.java       # Formato standard delle risposte di errore
│   ├── ResourceNotFoundException.java # 404 - Risorsa non trovata
│   ├── RuleViolationException.java    # 409 - Violazione regola di business
│   ├── InvalidInputException.java     # 400 - Input non valido
│   └── UnauthorizedActionException.java # 401 - Azione non autorizzata
│
├── models/                        # Entità JPA (dominio)
│   ├── abstractions/              # Classe astratta User
│   ├── utils/                     # Enum (ruoli, stati, metodi di pagamento)
│   ├── DefaultUser.java           # Utente standard (partecipante)
│   ├── StaffUser.java             # Utente staff (organizzatore/mentor)
│   ├── Hackathon.java             # Entità hackathon
│   ├── Team.java                  # Entità team
│   ├── Submission.java            # Entità submission
│   ├── Invitation.java            # Entità invito
│   ├── Report.java                # Entità report/segnalazione
│   └── SupportRequest.java        # Entità richiesta di supporto
│
├── repositories/                  # Layer di accesso ai dati (Spring Data JPA)
│   └── I*Repository.java          # 9 interfacce repository (una per entità)
│
├── security/                      # Configurazione sicurezza e JWT
│   ├── SecurityConfig.java        # Configurazione filtri e permessi HTTP
│   ├── JwtAuthenticationFilter.java # Filtro per validazione token JWT
│   ├── JwtService.java            # Generazione e parsing token JWT
│   ├── CustomUserDetails.java     # Implementazione UserDetails di Spring Security
│   ├── CustomUserDetailsService.java # Caricamento utente dal DB per autenticazione
│   └── SecurityUtils.java         # Utility per accedere all'utente autenticato
│
├── services/                      # Layer di business logic
│   ├── abstractions/              # Interfacce dei servizi (contratti)
│   ├── AuthenticationService.java # Logica di registrazione/login
│   ├── HackathonService.java      # Logica gestione hackathon
│   ├── InvitationService.java     # Logica gestione inviti
│   ├── ReportService.java         # Logica gestione report
│   ├── SubmissionService.java     # Logica gestione submission
│   ├── SupportRequestService.java # Logica richieste di supporto
│   └── TeamService.java           # Logica gestione team
│
└── utils/                         # Design pattern e utility
    ├── adapter/                   # Pattern Adapter (integrazione calendari)
    ├── builder/                   # Pattern Builder (creazione hackathon)
    ├── strategy/                  # Pattern Strategy (metodi di pagamento)
    ├── scheduler/                 # Schedulatori automatici
    └── unitOfWork/                # Pattern Unit of Work (transazioni)


---

## 📖 Documentazione API (Swagger)

Con l'applicazione in esecuzione, accedi alla documentazione interattiva delle API:


http://localhost:8080/swagger-ui/index.html


### Autenticazione su Swagger

Le API sono protette da *JWT*. Per effettuare chiamate autenticate:

1. Chiama l'endpoint di *login* (/api/auth/login) con le credenziali
2. Copia il token JWT dalla risposta
3. Clicca il pulsante *"Authorize"* 🔓 in alto a destra su Swagger UI
4. Inserisci il token nel formato: Bearer <il-tuo-token>
5. Ora tutte le chiamate includeranno automaticamente l'header di autenticazione

---
