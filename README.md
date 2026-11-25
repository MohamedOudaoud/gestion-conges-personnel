# Gestion des congés du personnel

Application JEE pour gérer les employés, départements, types de congé, soldes,
demandes, validations et historiques du personnel.

## Technologies

- Java 17
- Jakarta Servlet/JSP
- Hibernate/JPA
- MySQL
- Apache Tomcat 10
- Maven
- JUnit 5

## Modules

- M1: authentification et autorisation.
- M2: gestion des employés.
- M3: gestion des départements.
- M4: gestion des types de congé.
- M5: soldes annuels et ajustements RH.
- M6: demandes personnelles de congé.
- M7: validation par responsables et administrateurs.
- M8: historique filtré et tableaux de bord par rôle.

## Configuration locale

1. Installer Java 17, Tomcat 10.1, Maven et MySQL.
2. Créer la base `gestconges`.
3. Configurer `src/main/resources/hibernate.cfg.xml` localement avec les
   paramètres MySQL.
4. Démarrer le projet comme application WAR Maven/JEE avec Tomcat.

The real `hibernate.cfg.xml` is Git-ignored because it contains local database
credentials. Do not commit passwords or tokens.
