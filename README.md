# GitHub Repository API

Spring Boot application that lists GitHub repositories for a given user, excluding forks.

## Tech Stack
- Java 25
- Spring Boot 4.0.1
- Virtual Threads enabled

## API Endpoint

### Get User Repositories

```
GET /api/v1/users/{username}/repositories
```

**Headers:**
```
Accept: application/json
```

**Path Parameters:**
- `username` - GitHub username

**Response 200 OK:**
```json
[
  {
    "repositoryName": "my-repo",
    "ownerLogin": "username",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123def456..."
      },
      {
        "name": "develop",
        "lastCommitSha": "xyz789uvw012..."
      }
    ]
  }
]
```

**Response 404 Not Found:**
```json
{
  "status": 404,
  "message": "User not found"
}
```

## Behavior

- Returns only repositories that are **not forks**
- For each repository, fetches all branches with their last commit SHA
- Returns empty array `[]` if user has no repositories or all are forks

## Configuration

`src/main/resources/application.properties`:
```properties
github.api.base-url=https://api.github.com
```

## Running the Application

```bash
./gradlew bootRun
```

Application starts on port 8080 by default.

## Example Usage

```bash
# Get repositories for user "octocat"
curl http://localhost:8080/api/v1/users/octocat/repositories

# Non-existent user - returns 404
curl http://localhost:8080/api/v1/users/nonexistentuser123/repositories
```

## Testing

Run integration tests with Wiremock:
```bash
./gradlew test
```
