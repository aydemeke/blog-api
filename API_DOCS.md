# Blog API — Documentation

## 1. Overview

**Base URL:** `http://localhost:8080`

**Authentication Mechanism:**  
Protected endpoints require a JSON Web Token (JWT) passed as a Bearer token in the `Authorization` header:

```
Authorization: Bearer <YOUR_TOKEN>
```

Tokens are obtained from the `/api/auth/register` or `/api/auth/login` endpoints and must be included on every request that requires authentication.

**Content Type:** All request and response bodies use `application/json`.

---

## 2. Auth Endpoints

### 2.1 Register

**`POST /api/auth/register`**

Create a new user account. Returns a JWT on success.

| Property      | Value    |
|---------------|----------|
| Auth required | No       |
| Success code  | `201 Created` |

**Request Body**

| Field      | Type     | Validation                          |
|------------|----------|-------------------------------------|
| `username` | `string` | Required, must not be blank         |
| `email`    | `string` | Required, must be a valid email address |
| `password` | `string` | Required, must not be blank         |

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Response Body**

| Field   | Type     | Description              |
|---------|----------|--------------------------|
| `token` | `string` | JWT for use in subsequent authenticated requests |

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Example curl**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "secret123"
  }'
```

---

### 2.2 Login

**`POST /api/auth/login`**

Authenticate an existing user. Returns a JWT on success.

| Property      | Value    |
|---------------|----------|
| Auth required | No       |
| Success code  | `200 OK` |

**Request Body**

| Field      | Type     | Validation                  |
|------------|----------|-----------------------------|
| `username` | `string` | Required, must not be blank |
| `password` | `string` | Required, must not be blank |

```json
{
  "username": "johndoe",
  "password": "secret123"
}
```

**Response Body**

| Field   | Type     | Description              |
|---------|----------|--------------------------|
| `token` | `string` | JWT for use in subsequent authenticated requests |

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Example curl**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "secret123"
  }'
```

---

## 3. Post Endpoints

### 3.1 Create Post

**`POST /api/posts`**

Create a new blog post.

| Property      | Value         |
|---------------|---------------|
| Auth required | **Yes**       |
| Success code  | `201 Created` |

**Request Body**

| Field     | Type     | Validation                  |
|-----------|----------|-----------------------------|
| `title`   | `string` | Required, must not be blank |
| `content` | `string` | Required, must not be blank |
| `author`  | `string` | Required, must not be blank |

```json
{
  "title": "Getting Started with Spring Boot",
  "content": "Spring Boot makes it easy to create stand-alone, production-grade applications...",
  "author": "johndoe"
}
```

**Response Body**

| Field       | Type              | Description                        |
|-------------|-------------------|------------------------------------|
| `id`        | `number`          | Unique identifier of the post      |
| `title`     | `string`          | Title of the post                  |
| `content`   | `string`          | Full content of the post           |
| `author`    | `string`          | Author name                        |
| `createdAt` | `string` (ISO 8601) | Timestamp when the post was created |
| `updatedAt` | `string` (ISO 8601) | Timestamp when the post was last updated |

```json
{
  "id": 1,
  "title": "Getting Started with Spring Boot",
  "content": "Spring Boot makes it easy to create stand-alone, production-grade applications...",
  "author": "johndoe",
  "createdAt": "2026-04-13T10:00:00",
  "updatedAt": "2026-04-13T10:00:00"
}
```

**Example curl**

```bash
curl -s -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "title": "Getting Started with Spring Boot",
    "content": "Spring Boot makes it easy to create stand-alone, production-grade applications...",
    "author": "johndoe"
  }'
```

---

### 3.2 Get All Posts

**`GET /api/posts`**

Retrieve a paginated list of all blog posts.

| Property      | Value    |
|---------------|----------|
| Auth required | No       |
| Success code  | `200 OK` |

**Query Parameters**

| Parameter | Type     | Default      | Description                                      |
|-----------|----------|--------------|--------------------------------------------------|
| `page`    | `number` | `0`          | Zero-based page index                            |
| `size`    | `number` | `10`         | Number of posts per page                         |
| `sortBy`  | `string` | `createdAt`  | Field to sort by (e.g. `createdAt`, `title`)     |
| `sortDir` | `string` | `desc`       | Sort direction: `asc` or `desc`                  |

**Response Body**

| Field           | Type            | Description                                  |
|-----------------|-----------------|----------------------------------------------|
| `content`       | `array<PostDto>`| Array of post objects for the current page   |
| `page`          | `number`        | Current page index (zero-based)              |
| `size`          | `number`        | Page size requested                          |
| `totalElements` | `number`        | Total number of posts across all pages       |
| `totalPages`    | `number`        | Total number of pages                        |
| `last`          | `boolean`       | `true` if this is the last page              |

```json
{
  "content": [
    {
      "id": 2,
      "title": "Advanced Spring Security",
      "content": "In this post we dive deep into JWT authentication...",
      "author": "janedoe",
      "createdAt": "2026-04-13T11:00:00",
      "updatedAt": "2026-04-13T11:00:00"
    },
    {
      "id": 1,
      "title": "Getting Started with Spring Boot",
      "content": "Spring Boot makes it easy to create stand-alone, production-grade applications...",
      "author": "johndoe",
      "createdAt": "2026-04-13T10:00:00",
      "updatedAt": "2026-04-13T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

**Example curl**

```bash
curl -s "http://localhost:8080/api/posts?page=0&size=10&sortBy=createdAt&sortDir=desc"
```

---

### 3.3 Search Posts

**`GET /api/posts/search`**

Search posts by a keyword matched against title and/or content. Returns a paginated result.

| Property      | Value    |
|---------------|----------|
| Auth required | No       |
| Success code  | `200 OK` |

**Query Parameters**

| Parameter | Type     | Required | Default     | Description                                  |
|-----------|----------|----------|-------------|----------------------------------------------|
| `keyword` | `string` | Yes      | —           | Search term; must not be blank               |
| `page`    | `number` | No       | `0`         | Zero-based page index                        |
| `size`    | `number` | No       | `10`        | Number of posts per page                     |
| `sortBy`  | `string` | No       | `createdAt` | Field to sort by                             |
| `sortDir` | `string` | No       | `desc`      | Sort direction: `asc` or `desc`              |

**Response Body**

Same structure as [Get All Posts](#32-get-all-posts).

```json
{
  "content": [
    {
      "id": 2,
      "title": "Advanced Spring Security",
      "content": "In this post we dive deep into JWT authentication...",
      "author": "janedoe",
      "createdAt": "2026-04-13T11:00:00",
      "updatedAt": "2026-04-13T11:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

**Example curl**

```bash
curl -s "http://localhost:8080/api/posts/search?keyword=Spring&page=0&size=10"
```

---

### 3.4 Get Post by ID

**`GET /api/posts/{id}`**

Retrieve a single blog post by its ID.

| Property      | Value    |
|---------------|----------|
| Auth required | No       |
| Success code  | `200 OK` |

**Path Parameters**

| Parameter | Type     | Description          |
|-----------|----------|----------------------|
| `id`      | `number` | ID of the post       |

**Response Body**

A single `PostDto` object (see [Create Post](#31-create-post) response fields).

```json
{
  "id": 1,
  "title": "Getting Started with Spring Boot",
  "content": "Spring Boot makes it easy to create stand-alone, production-grade applications...",
  "author": "johndoe",
  "createdAt": "2026-04-13T10:00:00",
  "updatedAt": "2026-04-13T10:00:00"
}
```

**Example curl**

```bash
curl -s http://localhost:8080/api/posts/1
```

---

### 3.5 Update Post

**`PUT /api/posts/{id}`**

Replace all fields of an existing post.

| Property      | Value    |
|---------------|----------|
| Auth required | **Yes**  |
| Success code  | `200 OK` |

**Path Parameters**

| Parameter | Type     | Description          |
|-----------|----------|----------------------|
| `id`      | `number` | ID of the post to update |

**Request Body**

Same fields and validation rules as [Create Post](#31-create-post).

```json
{
  "title": "Getting Started with Spring Boot (Updated)",
  "content": "Revised content with additional examples...",
  "author": "johndoe"
}
```

**Response Body**

The updated `PostDto` object.

```json
{
  "id": 1,
  "title": "Getting Started with Spring Boot (Updated)",
  "content": "Revised content with additional examples...",
  "author": "johndoe",
  "createdAt": "2026-04-13T10:00:00",
  "updatedAt": "2026-04-13T12:30:00"
}
```

**Example curl**

```bash
curl -s -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "title": "Getting Started with Spring Boot (Updated)",
    "content": "Revised content with additional examples...",
    "author": "johndoe"
  }'
```

---

### 3.6 Delete Post

**`DELETE /api/posts/{id}`**

Delete a post by its ID.

| Property      | Value             |
|---------------|-------------------|
| Auth required | **Yes**           |
| Success code  | `204 No Content`  |

**Path Parameters**

| Parameter | Type     | Description              |
|-----------|----------|--------------------------|
| `id`      | `number` | ID of the post to delete |

**Response Body**

None — the server returns `204 No Content` with an empty body on success.

**Example curl**

```bash
curl -s -X DELETE http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## 4. Comment Endpoints

### 4.1 Add Comment

**`POST /api/posts/{postId}/comments`**

Add a comment to a specific post.

| Property      | Value         |
|---------------|---------------|
| Auth required | **Yes**       |
| Success code  | `201 Created` |

**Path Parameters**

| Parameter | Type     | Description                      |
|-----------|----------|----------------------------------|
| `postId`  | `number` | ID of the post to comment on     |

**Request Body**

| Field     | Type     | Validation                  |
|-----------|----------|-----------------------------|
| `content` | `string` | Required, must not be blank |
| `author`  | `string` | Required, must not be blank |

```json
{
  "content": "Great post! Very helpful introduction.",
  "author": "janedoe"
}
```

**Response Body**

| Field       | Type                | Description                               |
|-------------|---------------------|-------------------------------------------|
| `id`        | `number`            | Unique identifier of the comment          |
| `content`   | `string`            | Text of the comment                       |
| `author`    | `string`            | Author name                               |
| `createdAt` | `string` (ISO 8601) | Timestamp when the comment was created    |
| `postId`    | `number`            | ID of the post this comment belongs to    |

```json
{
  "id": 1,
  "content": "Great post! Very helpful introduction.",
  "author": "janedoe",
  "createdAt": "2026-04-13T13:00:00",
  "postId": 1
}
```

**Example curl**

```bash
curl -s -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "content": "Great post! Very helpful introduction.",
    "author": "janedoe"
  }'
```

---

### 4.2 Get Comments for a Post

**`GET /api/posts/{postId}/comments`**

Retrieve all comments for a specific post.

| Property      | Value    |
|---------------|----------|
| Auth required | **Yes**  |
| Success code  | `200 OK` |

**Path Parameters**

| Parameter | Type     | Description                              |
|-----------|----------|------------------------------------------|
| `postId`  | `number` | ID of the post whose comments to fetch   |

**Response Body**

An array of `CommentDto` objects.

```json
[
  {
    "id": 2,
    "content": "Thanks for sharing this!",
    "author": "bobsmith",
    "createdAt": "2026-04-13T14:00:00",
    "postId": 1
  },
  {
    "id": 1,
    "content": "Great post! Very helpful introduction.",
    "author": "janedoe",
    "createdAt": "2026-04-13T13:00:00",
    "postId": 1
  }
]
```

**Example curl**

```bash
curl -s http://localhost:8080/api/posts/1/comments \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### 4.3 Delete Comment

**`DELETE /api/posts/{postId}/comments/{commentId}`**

Delete a specific comment from a post.

| Property      | Value            |
|---------------|------------------|
| Auth required | **Yes**          |
| Success code  | `204 No Content` |

**Path Parameters**

| Parameter   | Type     | Description                       |
|-------------|----------|-----------------------------------|
| `postId`    | `number` | ID of the post                    |
| `commentId` | `number` | ID of the comment to delete       |

**Response Body**

None — the server returns `204 No Content` with an empty body on success.

**Example curl**

```bash
curl -s -X DELETE http://localhost:8080/api/posts/1/comments/2 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

## 5. Auth Rules Summary

| Method | Path                                    | Auth Required |
|--------|-----------------------------------------|---------------|
| POST   | `/api/auth/register`                    | No            |
| POST   | `/api/auth/login`                       | No            |
| GET    | `/api/posts`                            | No            |
| GET    | `/api/posts/search`                     | No            |
| GET    | `/api/posts/{id}`                       | No            |
| POST   | `/api/posts`                            | Yes           |
| PUT    | `/api/posts/{id}`                       | Yes           |
| DELETE | `/api/posts/{id}`                       | Yes           |
| POST   | `/api/posts/{postId}/comments`          | Yes           |
| GET    | `/api/posts/{postId}/comments`          | Yes           |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | Yes        |
