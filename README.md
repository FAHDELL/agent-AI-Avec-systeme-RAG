# 📘 Project Documentation -- RAG AI (Spring Boot + Thymeleaf)

Ce projet est une application complète **RAG (Retrieval-Augmented
Generation)** utilisant :

-   **Back-end :** Spring Boot + Spring AI + Ollama\
-   **Front-end :** Thymeleaf + Bootsrap + Axios + Progress Bar\
-   **Base de données :** PostgreSQL + pgvector\
-   **Fonctionnalité :** Upload PDF → Split → Embedding → Stockage →
    Chat intelligent basé sur le contexte

------------------------------------------------------------------------

## 🚀 1. Architecture du Projet

   src/
 ├── main/java/net/fahd/RAG_AI
 │      ├── controllers
 │      ├── services
 │      └── RagAiApplication.java
 └── main/resources
        ├── templates/index.html
        └── application.properties

------------------------------------------------------------------------

## 🧠 2. Fonctionnalités

### ✔ Back-end (Spring Boot + Spring AI)

-   Upload de fichiers PDF (un ou plusieurs)
-   Extraction du texte
-   Découpage (chunking)
-   Embedding avec modèle LLM (Ollama ou OpenAI)
-   Stockage vecteur dans PostgreSQL (pgvector)
-   Endpoint `/rag` pour les requêtes utilisateur.
-   Intégration Spring AI (Ollama ou OpenAI).

------------------------------------------------------------------------

## 🎨 3. Front-end (Thymeleaf)

La page fournit :
- Un champ texte pour poser une question.
- Une zone affichant la réponse.
- Une zone d’upload permettant d’importer plusieurs PDF.
- Une barre de progression lors de l’indexation.

Le code inclut Bootstrap 5.3.3.

------------------------------------------------------------------------

## 🛠 4. Installation -- Back-end

### 4.1. Prérequis

-   Java 21+
-   Maven
-   PostgreSQL 18+
-   Extension pgvector :

```{=html}
<!-- -->
```
    CREATE EXTENSION IF NOT EXISTS vector;

### 4.2. Configuration du `application.properties`

        spring.application.name=RAG_AI
        spring.ai.ollama.base-url=http://localhost:11434
        spring.ai.ollama.chat.options.model=deepseek-r1
        spring.ai.vectorstore.pgvector.initialize-schema=true
        server.port=8899
        logging.level.org.springframework.ai.chat.client.advisor=DEBUG
        spring.datasource.url=jdbc:postgresql://localhost:5432/vector_store
        spring.datasource.username=postgres
        spring.datasource.password=password
        logging.level.org.springframework.ai=DEBUG

### 4.3. Lancer le backend

    cd backend
    mvn spring-boot:run

------------------------------------------------------------------------

## 🧪 Tester

1. Lancer l’application.
2. Ouvrir :  
   👉 `http://localhost:8899/rag`
3. Importer un ou plusieurs PDF.
4. Poser une question.

   <img width="1078" height="831" alt="Capture d&#39;écran 2025-12-02 150244" src="https://github.com/user-attachments/assets/d9a336a2-079f-4b4c-8ac1-e93a3f7725ac" />


---

## 📄 Licence
Libre d’utilisation pour projets éducatifs et professionnels.

---

## ✨ Auteur
Développé par **Fahd ELLAHIA**.
