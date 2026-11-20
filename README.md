# GitHub Repository Searcher

## Project Overview

GitHub Repository Searcher is a Spring Boot REST API application that searches GitHub repositories using the GitHub REST API and stores repository information in a PostgreSQL database.

The application supports searching, saving, filtering and sorting GitHub repositories.

## Technologies Used

- Java 21
- Spring Boot 4.0.9
- Spring Data JPA
- PostgreSQL
- Hibernate
- Maven
- GitHub REST API
- JUnit 5
- Mockito
- Postman

## Features

- Search GitHub repositories by keyword.
- Filter search results by programming language.
- Sort GitHub search results by stars, forks and updated date.
- Save repository information in PostgreSQL.
- Update existing repositories instead of creating duplicates.
- Retrieve all saved repositories.
- Filter saved repositories by language and minimum stars.
- Sort saved repositories by stars, forks and updated date.
- Validate invalid inputs.
- Handle GitHub API errors and network failures.

## Database Configuration

Create a PostgreSQL database named:

`github_searcher`

Configure your database username and password in `src/main/resources/application.properties`.

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/github_searcher
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Replace the username and password with your own PostgreSQL credentials.

## How to Run the Project

1. Install Java 21.
2. Install PostgreSQL.
3. Create the `github_searcher` database.
4. Configure database credentials.
5. Import the project into Eclipse or another Java IDE.
6. Run `GithubSearcherApplication.java`.
7. The application starts on port 8080.

## API Endpoints

### 1. Search GitHub Repositories

**POST** `/api/github/search`

Example request:

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

Supported sort values:
- stars
- forks
- updated

### 2. Retrieve All Saved Repositories

**GET** `/api/github/saved`

Returns all repositories saved in PostgreSQL.

### 3. Retrieve Filtered Repositories

**GET** `/api/github/repositories`

Supported query parameters:

- `language` – filter by programming language.
- `minStars` – minimum number of stars.
- `sort` – sorting field: stars, forks or updated.

Example:

`/api/github/repositories?language=Java&minStars=1000&sort=stars`

## Error Handling

The application handles:
- Empty search keywords.
- Invalid sorting values.
- Negative minimum star values.
- GitHub API failures.
- GitHub API rate limits or access denial.
- Network connection failures.

## Testing

JUnit 5 and Mockito are used for unit testing.

Current test results:

- GithubSearcherApplicationTests: 1 passed.
- GitHubServiceTest: 5 passed.
- Total: 6 tests passed.
- Failures: 0.
- Errors: 0.

The REST APIs were also tested using Postman.

## Project Structure

```text
github-searcher
├── src/main/java
│   └── com.freightfox.github_searcher
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       ├── dto
│       └── exception
├── src/main/resources
│   └── application.properties
├── src/test/java
│   └── com.freightfox.github_searcher
│       └── service
└── pom.xml
```

## Author

GitHub Repository Searcher - Spring Boot Assignment
# freightfox-github-searcher
GitHub Repository Searcher using Java, Spring Boot and PostgreSQL.
