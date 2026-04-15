# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Blog API — Claude Code Guide

## Project Stack

- **Java 21** with **Spring Boot 3.3**
- **Maven** for build and dependency management
- **H2** in-memory database (not PostgreSQL)
- **Lombok** for boilerplate reduction
- **Bean Validation** (`spring-boot-starter-validation`) for request DTO constraints

---

## Common Commands

```bash
# Run the application
mvn spring-boot:run

# Run all tests
mvn test

# Build a deployable JAR
mvn clean package
```

---

## H2 Console

The H2 console is enabled for development at:

```
http://localhost:8080/h2-console
```

JDBC URL: `jdbc:h2:mem:testdb`  
Username: `sa` / Password: *(empty)*

Do not disable the H2 console in `application.properties` during development.

---

## Intentionally Bad Code — Do Not Fix

`GET /api/posts/stats` in `PostController` is **deliberately broken** for review/training purposes.
It violates Rules 1, 2, 3, and 4 on purpose. **Do not refactor, fix, or improve this endpoint.**

---

## Architecture Rules

### 1. Strict Layering — Controller → Service → Repository

Never skip layers. Controllers must not call Repositories directly, and Repositories must not contain business logic.

```
Request → @RestController → @Service → @Repository → H2
```

### 2. DTOs in Controllers — Never Expose Entities

Controllers always receive and return DTOs, never JPA `@Entity` objects. Mapping between DTOs and entities is the responsibility of the Service layer.

```java
// Correct
@PostMapping
public ResponseEntity<PostResponse> create(@RequestBody PostRequest dto) { ... }

// Wrong — never do this
@PostMapping
public ResponseEntity<Post> create(@RequestBody Post entity) { ... }
```

### 3. `@Transactional` on All Write Operations

Every Service method that performs a write (create, update, delete) must be annotated with `@Transactional`.

```java
@Transactional
public PostResponse createPost(PostRequest request) { ... }

@Transactional
public void deletePost(Long id) { ... }
```

Read-only methods should use `@Transactional(readOnly = true)`.

### 4. Never Use `Optional.get()` Naked — Always `orElseThrow()`

Using `Optional.get()` without a prior `isPresent()` check is forbidden. Always use `orElseThrow()` with a meaningful exception.

```java
// Correct
Post post = postRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

// Wrong
Post post = postRepository.findById(id).get();
```

### 5. Lombok Usage

Always prefer Lombok annotations to reduce boilerplate:

| Use case | Annotation |
|---|---|
| Data classes / DTOs | `@Data` |
| Builder pattern | `@Builder` |
| Constructor injection | `@RequiredArgsConstructor` |

Do not write manual getters, setters, or constructors where Lombok covers it.

```java
@Data
@Builder
public class PostRequest {
    private String title;
    private String content;
}

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
}
```

### 6. JavaDoc on All Public Service Methods

Every `public` method in a `@Service` class must have a JavaDoc comment describing what it does, its parameters, and its return value.

```java
/**
 * Retrieves a published blog post by its ID.
 *
 * @param id the ID of the post to retrieve
 * @return the post data as a {@link PostResponse}
 * @throws ResourceNotFoundException if no post exists with the given ID
 */
@Transactional(readOnly = true)
public PostResponse getPostById(Long id) { ... }
```

### 7. Centralized Exception Handling via `@ControllerAdvice`

Do not handle exceptions inside individual controllers. All exception-to-HTTP-status mapping lives in a single `@ControllerAdvice` class (e.g., `GlobalExceptionHandler`).

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

## Package Structure Convention

```
com.example.blogapi
├── controller      # @RestController classes — DTOs in/out only
├── service         # @Service classes — business logic, @Transactional
├── repository      # @Repository interfaces — Spring Data JPA
├── entity          # @Entity classes — never leave the service layer
├── dto
│   ├── request     # Inbound DTOs (e.g., PostRequest)
│   └── response    # Outbound DTOs (e.g., PostResponse)
└── exception       # Custom exceptions + GlobalExceptionHandler
```
