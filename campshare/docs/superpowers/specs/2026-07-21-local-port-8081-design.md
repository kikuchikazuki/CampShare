# Local Port 8081 Design

## Goal

Run CampShare on port 8081 in this local workspace to avoid intermittent conflicts on port 8080.

## Approach

Add `server.port=8081` to the existing root-level `application-local.properties`. The application already imports this file through `spring.config.import=optional:file:./application-local.properties`, and `.gitignore` excludes it. This keeps the change local to the current machine and avoids changing the default port for other contributors.

## Safety and Scope

- Do not display, replace, or commit existing datasource credentials.
- Preserve every existing line in `application-local.properties`.
- Do not modify the tracked `src/main/resources/application.properties` file.
- Do not alter unrelated working-tree changes.

## Validation

Confirm the local file contains exactly one active `server.port=8081` entry. Start the application and verify the log reports Tomcat on port 8081 and `http://localhost:8081/` responds. Also confirm `application-local.properties` remains ignored by Git.
