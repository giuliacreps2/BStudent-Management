# 🎓 BStudent

> Academy online di Latino e Greco per studenti delle superiori, con video-lezioni, test per capitolo, risorse gratuite e percorsi in abbonamento.

---

## 📋 Indice

- [Descrizione del Progetto](#descrizione-del-progetto)
- [Funzionalità Principali](#funzionalità-principali)
- [Architettura](#architettura)
- [Tecnologie](#tecnologie)
- [Struttura del Progetto](#struttura-del-progetto)
- [API Endpoints](#api-endpoints)
- [Roadmap](#roadmap)

---

## 📖 Descrizione del Progetto

**BStudent** è una piattaforma di e-learning dedicata allo studio del Latino e del Greco per studenti delle scuole superiori. Combina video-lezioni preregistrate, test di verifica per capitolo e risorse gratuite (blog, crediti) con un sistema di abbonamento mensile/annuale per accedere ai contenuti premium.

L'obiettivo è offrire un percorso di studio strutturato, progressivo e coinvolgente, con funzionalità social e gamification previste nella roadmap futura.

---

## ✨ Funzionalità Principali

### 👤 Account Gratuito
- Registrazione e accesso alla piattaforma
- Lettura delle risorse blog
- Acquisizione di crediti tramite attività gratuite
- Anteprima dei contenuti premium
- Profilo personale con storico attività

### 💳 Account Premium (Abbonamento Mensile / Annuale)
- Accesso completo al catalogo video-lezioni (hosting Vimeo)
- Test di verifica al termine di ogni capitolo
- Tracciamento dei progressi per materia e capitolo
- Accesso prioritario ai nuovi contenuti

### 🎬 Video-Lezioni
- Video preregistrati integrati tramite **Vimeo**
- Organizzati per materia (Latino / Greco), livello e capitolo
- Riproduzione sicura (embed protetto, non accessibile senza autenticazione premium)

### 📝 Test per Capitolo
- Quiz a risposta multipla al termine di ogni capitolo
- Punteggio e feedback immediato
- Storico tentativi e progressione per lo studente

### 🔐 Sicurezza & Autenticazione
- Autenticazione basata su JWT tramite **Spring Security**
- Ruoli: `ADMIN`, `PREMIUM`, `FREE`
- Accesso ai contenuti video e test filtrato per ruolo e stato abbonamento

---

## 🏗️ Architettura

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT LAYER                     │
│           React / Next.js + Tailwind/Bootstrap      │
└─────────────────────┬───────────────────────────────┘
                      │ REST API (HTTPS)
┌─────────────────────▼───────────────────────────────┐
│                   BACKEND LAYER                     │
│              Java 17+ · Spring Boot                 │
│   Spring Security · Spring Data JPA · Specification │
└──────────┬──────────────────────────┬───────────────┘
           │                          │
┌──────────▼──────────┐  ┌────────────▼───────────────┐
│   DATABASE LAYER    │  │      EXTERNAL SERVICES      │
│     PostgreSQL      │  │  Vimeo API · Payment Gateway│
└─────────────────────┘  └────────────────────────────┘
```

---

## 🛠️ Tecnologie

### Backend
| Tecnologia | Utilizzo |
|---|---|
| Java 17+ | Linguaggio principale |
| Spring Boot | Framework applicativo |
| Spring Security | Autenticazione e autorizzazione (JWT) |
| Spring Data JPA + Specification | Persistenza e query dinamiche |
| PostgreSQL | Database relazionale |
| Maven | Build e gestione dipendenze |

### Frontend
| Tecnologia | Utilizzo |
|---|---|
| React / Next.js | Framework UI |
| Tailwind CSS / Bootstrap | Styling e componenti |

### Servizi Esterni
| Servizio | Utilizzo |
|---|---|
| Vimeo | Hosting e streaming video-lezioni |
| Payment Gateway (TBD) | Gestione abbonamenti mensili/annuali |

---

## 📡 API Endpoints

### Autenticazione
| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/api/auth/register` | Registrazione nuovo utente |
| POST | `/api/auth/login` | Login e ottenimento JWT |

### Corsi & Video
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/corsi` | Lista corsi disponibili (pubblica) |
| GET | `/api/corsi/{id}` | Dettaglio corso con capitoli |
| GET | `/api/corsi/{id}/video/{videoId}` | Accesso video (PREMIUM) |
| POST | `/api/admin/corsi` | Crea corso (ADMIN) |
| POST | `/api/admin/corsi/{id}/video` | Aggiunge video a capitolo (ADMIN) |

### Test
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/test/{capitoloId}` | Recupera test del capitolo (PREMIUM) |
| POST | `/api/test/{capitoloId}/submit` | Invia risposte e ottieni punteggio (PREMIUM) |
| GET | `/api/test/storico` | Storico tentativi utente |

### Blog & Risorse
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/blog` | Lista articoli del blog (pubblica) |
| GET | `/api/blog/{id}` | Dettaglio articolo (pubblica) |
| POST | `/api/admin/blog` | Pubblica articolo (ADMIN) |

### Abbonamento
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/abbonamento/piani` | Lista piani disponibili (pubblica) |
| POST | `/api/abbonamento/sottoscrivi` | Attiva abbonamento |
| GET | `/api/abbonamento/stato` | Stato abbonamento utente |
| DELETE | `/api/abbonamento/cancella` | Cancella abbonamento |

### Crediti
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/crediti/saldo` | Saldo crediti utente (FREE + PREMIUM) |
| GET | `/api/crediti/storico` | Storico movimenti crediti |

---

## 🗺️ Roadmap

Le seguenti funzionalità sono pianificate per le versioni future della piattaforma:

### 🤖 AI Personalizzata per Studente
- Motore di raccomandazione basato sui progressi e sui risultati dei test
- Suggerimenti personalizzati su argomenti da ripassare
- Tutor virtuale per domande su grammatica e traduzione

### ⚔️ Battle Mode — Sfide tra Studenti
- **One-vs-One**: sfida diretta tra due studenti in modalità live
- **Team vs Team**: competizioni a squadre su argomenti specifici
- Classifica globale e per scuola
- Sistema di ranking e badge

### 🏅 Gamification Avanzata
- Acquisizione di skill e badge al completamento dei capitoli
- Livelli di esperienza per materia
- Streak giornaliere e ricompense

---

*BStudent — Latino e Greco, finalmente chiari. 🏛️*

---
---

# 🎓 BStudent

> Online academy for Latin and Ancient Greek for high school students, with video lessons, chapter-end tests, free resources, and premium subscription plans.

---

## 📋 Table of Contents

- [Project Description](#project-description)
- [Main Features](#main-features)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints-1)
- [Roadmap](#roadmap-1)

---

## 📖 Project Description

**BStudent** is an e-learning platform dedicated to the study of Latin and Ancient Greek for high school students. It combines pre-recorded video lessons, chapter-end quizzes and free resources (blog, credits) with a monthly/annual subscription system to access premium content.

The goal is to offer a structured, progressive and engaging study path, with social and gamification features planned in the future roadmap.

---

## ✨ Main Features

### 👤 Free Account
- Registration and platform access
- Read blog resources
- Earn credits through free activities
- Preview of premium content
- Personal profile with activity history

### 💳 Premium Account (Monthly / Annual Subscription)
- Full access to the video lesson catalog (Vimeo hosting)
- Chapter-end quizzes
- Progress tracking by subject and chapter
- Priority access to new content

### 🎬 Video Lessons
- Pre-recorded videos integrated via **Vimeo**
- Organized by subject (Latin / Greek), level and chapter
- Secure playback (protected embed, not accessible without premium authentication)

### 📝 Chapter Tests
- Multiple-choice quizzes at the end of each chapter
- Immediate score and feedback
- Attempt history and student progression tracking

### 🔐 Security & Authentication
- JWT-based authentication via **Spring Security**
- Roles: `ADMIN`, `PREMIUM`, `FREE`
- Access to video content and tests filtered by role and subscription status

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    CLIENT LAYER                     │
│           React / Next.js + Tailwind/Bootstrap      │
└─────────────────────┬───────────────────────────────┘
                      │ REST API (HTTPS)
┌─────────────────────▼───────────────────────────────┐
│                   BACKEND LAYER                     │
│              Java 17+ · Spring Boot                 │
│   Spring Security · Spring Data JPA · Specification │
└──────────┬──────────────────────────┬───────────────┘
           │                          │
┌──────────▼──────────┐  ┌────────────▼───────────────┐
│   DATABASE LAYER    │  │      EXTERNAL SERVICES      │
│     PostgreSQL      │  │  Vimeo API · Payment Gateway│
└─────────────────────┘  └────────────────────────────┘
```

---

## 🛠️ Technologies

### Backend
| Technology | Usage |
|---|---|
| Java 17+ | Main language |
| Spring Boot | Application framework |
| Spring Security | Authentication & authorization (JWT) |
| Spring Data JPA + Specification | Persistence and dynamic queries |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |

### Frontend
| Technology | Usage |
|---|---|
| React / Next.js | UI Framework |
| Tailwind CSS / Bootstrap | Styling and components |

### External Services
| Service | Usage |
|---|---|
| Vimeo | Video lesson hosting and streaming |
| Payment Gateway (TBD) | Monthly/annual subscription management |

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |

### Courses & Videos
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/corsi` | Course list (public) |
| GET | `/api/corsi/{id}` | Course detail with chapters |
| GET | `/api/corsi/{id}/video/{videoId}` | Video access (PREMIUM) |
| POST | `/api/admin/corsi` | Create course (ADMIN) |
| POST | `/api/admin/corsi/{id}/video` | Add video to chapter (ADMIN) |

### Tests
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/test/{capitoloId}` | Get chapter test (PREMIUM) |
| POST | `/api/test/{capitoloId}/submit` | Submit answers and get score (PREMIUM) |
| GET | `/api/test/storico` | User attempt history |

### Blog & Resources
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/blog` | Blog article list (public) |
| GET | `/api/blog/{id}` | Article detail (public) |
| POST | `/api/admin/blog` | Publish article (ADMIN) |

### Subscription
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/abbonamento/piani` | Available plans (public) |
| POST | `/api/abbonamento/sottoscrivi` | Activate subscription |
| GET | `/api/abbonamento/stato` | User subscription status |
| DELETE | `/api/abbonamento/cancella` | Cancel subscription |

### Credits
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/crediti/saldo` | User credits balance (FREE + PREMIUM) |
| GET | `/api/crediti/storico` | Credits movement history |

---

## 🗺️ Roadmap

The following features are planned for future versions of the platform:

### 🤖 Personalized AI per Student
- Recommendation engine based on progress and test results
- Personalized suggestions on topics to review
- Virtual tutor for grammar and translation questions

### ⚔️ Battle Mode — Student Challenges
- **One-vs-One**: direct live challenge between two students
- **Team vs Team**: team competitions on specific topics
- Global and school-based leaderboard
- Ranking system and badges

### 🏅 Advanced Gamification
- Skill and badge acquisition upon chapter completion
- Experience levels per subject
- Daily streaks and rewards

---

*BStudent — Latin and Ancient Greek, finally clear. 🏛️*
