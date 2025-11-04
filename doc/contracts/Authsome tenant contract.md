# Authsome Service Interface Contracts

**Version:** 1.3  
**Last Updated:** November 2, 2025  
**Purpose:** This document defines the functional contracts for all Authsome services. Developers can implement these services in any programming language or framework.

## Table of Contents
1. [Overview](#overview)
2. [REST API Endpoints](#rest-api-endpoints)
3. [Common Data Types](#common-data-types)
4. [Tenant Service](#tenant-service)
5. [OTP Service](#otp-service)
6. [Notifier Service](#notifier-service)
7. [JWT Service](#jwt-service)
8. [Error Handling](#error-handling)
9. [Implementation Guide](#implementation-guide)

---

## Overview

### Architecture
Each service is **independent** and must implement the functions defined in this contract. Services can communicate through:
- **Direct function calls** (same application/monolith)
- **REST API calls** (microservices)
- **gRPC** (high-performance scenarios)
- **Message queues** (asynchronous operations)

### Key Principles
- **Stateless Operations:** Each function call is independent
- **Clear Responsibilities:** Each service has a single, well-defined purpose
- **JSON Serialization:** All data types must be serializable to JSON
- **Consistent Error Handling:** Use standardized error types across all services
- **No Authentication Between Services:** For initial implementation, inter-service communication does not require authentication (services are trusted within the same network)

---

## REST API Endpoints

### When to Use REST APIs

If you're implementing services as separate microservices, use the REST API contracts defined below. **Important security notes:**

- **Internal Network Only:** All inter-service communication should occur within a private/internal network
- **No Authentication Required:** For the initial implementation, services are considered trusted and do not require authentication tokens
- **Future Enhancement:** Authentication between services (mutual TLS, API keys, JWT) will be added in future versions

### Base URL Convention

Each service should be hosted at its own base URL:
- Tenant Service: `http://tenant-service:8080`
- OTP Service: `http://otp-service:8080`
- Notifier Service: `http://notifier-service:8080`
- JWT Service: `http://jwt-service:8080`

### Common HTTP Status Codes

| Status Code | Meaning | Usage |
|-------------|---------|-------|
| 200 | OK | Successful request with response body |
| 201 | Created | Resource successfully created |
| 204 | No Content | Successful request with no response body |
| 400 | Bad Request | VALIDATION_ERROR |
| 401 | Unauthorized | UNAUTHORIZED |
| 404 | Not Found | NOT_FOUND |
| 409 | Conflict | CONFLICT (duplicate resource) |
| 410 | Gone | EXPIRED |
| 500 | Internal Server Error | INTERNAL_ERROR |

### Standard Request/Response Format

All endpoints use JSON for request and response bodies.

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
```

**Error Response Format:**
```json
{
  "errorType": "VALIDATION_ERROR",
  "message": "Username cannot be empty",
  "details": {
    "field": "username"
  }
}
```

---

## Common Data Types

### Timestamps
- **Type:** Long (64-bit integer)
- **Format:** Milliseconds since Unix epoch
- **Example:** 1698710400000 (represents October 31, 2023, 00:00:00 UTC)

### Identifiers
- **Type:** String
- **Format:** UUID (36 characters with hyphens)
- **Example:** "550e8400-e29b-41d4-a716-446655440000"

### Enumerations

#### IdentityType (Tenant Service)
- `EMAIL` - Email address (e.g., user@example.com)
- `USERNAME` - Username (e.g., john_doe)
- `USER_ID` - User's unique identifier (UUID)

#### IdentityType (Notifier Service)
- `EMAIL` - Email notification channel
- Note: SMS and PUSH_NOTIFICATION may be added in future versions

#### OtpType
- `NUMERIC` - Only digits 0-9 (e.g., "1234")
- `ALPHABETIC` - Only letters a-z, A-Z (e.g., "ABCD")
- `ALPHANUMERIC` - Mix of digits and letters (e.g., "A1B2")

#### TimeUnit
- `SECONDS` - Time in seconds
- `MINUTES` - Time in minutes
- `HOURS` - Time in hours
- `DAYS` - Time in days

---

## Tenant Service

### Purpose
Manages tenant (user) accounts including creation, lookup, identity management, credential validation, and session/token management.

### REST API Endpoints

#### 1. Get Tenant by Identity

**Endpoint:** `POST /api/v1/tenant/get-by-identity`

**Request Body:**
```json
{
  "identityType": "EMAIL",
  "identity": "user@example.com"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Response (404 Not Found):** Empty body when tenant not found

---

#### 2. Get Tenant by Username

**Endpoint:** `GET /api/v1/tenant/get-by-username/{username}`

**Path Parameters:**
- `username` - Username to search for

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Response (404 Not Found):** Empty body when tenant not found

---

#### 3. Get Tenant by ID

**Endpoint:** `GET /api/v1/tenant/{tenantId}`

**Path Parameters:**
- `tenantId` - UUID of the tenant

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Response (404 Not Found):** Empty body when tenant not found

---

#### 4. Create Tenant

**Endpoint:** `POST /api/v1/tenant/create`

**Request Body:**
```json
{
  "username": "john_doe",
  "rawPassword": "SecurePass123!"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Error (409 Conflict):** Username already exists

---

#### 5. Add Identity for Tenant

**Endpoint:** `POST /api/v1/tenant/{tenantId}/identity`

**Path Parameters:**
- `tenantId` - UUID of the tenant

**Request Body:**
```json
{
  "identityType": "EMAIL",
  "identity": "user@example.com"
}
```

**Response (201 Created):**
```json
{
  "id": "identity-abc123",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "identityType": "EMAIL",
  "identity": "user@example.com"
}
```

**Error (409 Conflict):** Identity already associated with another tenant

---

#### 6. Validate Tenant Credentials

**Endpoint:** `POST /api/v1/tenant/{tenantId}/validate-credentials`

**Path Parameters:**
- `tenantId` - UUID of the tenant

**Request Body:**
```json
{
  "rawPassword": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "valid": true
}
```

---

#### 7. Create Refresh Token

**Endpoint:** `POST /api/v1/tenant/{tenantId}/refresh-token`

**Path Parameters:**
- `tenantId` - UUID of the tenant

**Request Body:**
```json
{
  "metadata": {
    "device": "mobile",
    "ip": "192.168.1.1"
  }
}
```

**Response (201 Created):**
```json
{
  "refreshToken": "refresh_token_abc123xyz789"
}
```

---

#### 8. Refresh Token

**Endpoint:** `POST /api/v1/tenant/token/refresh`

**Request Body:**
```json
{
  "refreshToken": "refresh_token_abc123xyz789"
}
```

**Response (200 OK):**
```json
{
  "tenant": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "createdAt": 1698710400000,
    "updatedAt": 1698710400000
  },
  "refreshToken": "new_refresh_token_xyz456"
}
```

**Response (404 Not Found):** Token invalid or expired

---

#### 9. Revoke Refresh Token

**Endpoint:** `DELETE /api/v1/tenant/token/revoke`

**Request Body:**
```json
{
  "refreshToken": "refresh_token_abc123xyz789"
}
```

**Response (204 No Content):** Successfully revoked

---

#### 10. Generate API Key

**Endpoint:** `POST /api/v1/tenant/{tenantId}/api-key`

**Path Parameters:**
- `tenantId` - UUID of the tenant

**Response (201 Created):**
```json
{
  "apiKey": "ask_abc123xyz789def456"
}
```

---

#### 11. Get Tenant by API Key

**Endpoint:** `POST /api/v1/tenant/get-by-api-key`

**Request Body:**
```json
{
  "apiKey": "ask_abc123xyz789def456"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Response (404 Not Found):** API key not found

---

### Function 1: getTenantByIdentity

**Description:** Find a tenant using any of their registered identities.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| identityType | IdentityType | Yes | Type of identity to search |
| identity | String | Yes | The identity value |

**Returns:** FetchedTenant or null

**FetchedTenant Structure:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "createdAt": 1698710400000,
  "updatedAt": 1698710400000
}
```

**Example Usage:**
```javascript
// Search by email
input: {
  identityType: "EMAIL",
  identity: "user@example.com"
}

output: {
  id: "550e8400-e29b-41d4-a716-446655440000",
  username: "john_doe",
  createdAt: 1698710400000,
  updatedAt: 1698710400000
}
```

**Error Cases:**
- `VALIDATION_ERROR`: Invalid identityType value
- `VALIDATION_ERROR`: Empty or null identity

---

### Function 2: getTenantByUsername

**Description:** Find a tenant by their username.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | Yes | Username to search for |

**Returns:** FetchedTenant or null

**Example Usage:**
```javascript
input: {
  username: "john_doe"
}

output: {
  id: "550e8400-e29b-41d4-a716-446655440000",
  username: "john_doe",
  createdAt: 1698710400000,
  updatedAt: 1698710400000
}
```

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null username

---

### Function 3: getTenantById

**Description:** Find a tenant by their unique identifier.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |

**Returns:** FetchedTenant or null

**Example Usage:**
```javascript
input: {
  tenantId: "550e8400-e29b-41d4-a716-446655440000"
}

output: {
  id: "550e8400-e29b-41d4-a716-446655440000",
  username: "john_doe",
  createdAt: 1698710400000,
  updatedAt: 1698710400000
}
```

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null tenantId

---

### Function 4: createTenant

**Description:** Creates a new tenant account with username and password.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| username | String | Yes | Unique username for the tenant |
| rawPassword | String | Yes | Plain text password (will be hashed) |

**Returns:** FetchedTenant

**Example Usage:**
```javascript
input: {
  username: "john_doe",
  rawPassword: "SecurePass123!"
}

output: {
  id: "550e8400-e29b-41d4-a716-446655440000",
  username: "john_doe",
  createdAt: 1698710400000,
  updatedAt: 1698710400000
}
```

**Implementation Notes:**
- Password MUST be hashed using bcrypt (cost factor 10+) or argon2id
- Username must be unique across all tenants
- Generate UUID for tenant ID
- Set createdAt and updatedAt to current timestamp

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null username or rawPassword
- `CONFLICT`: Username already exists

---

### Function 5: addIdentityForTenant

**Description:** Associates an identity (email, username) with an existing tenant.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |
| identityType | IdentityType | Yes | Type of identity to add |
| identity | String | Yes | Identity value |

**Returns:** FetchedTenantIdentity

**FetchedTenantIdentity Structure:**
```json
{
  "id": "identity-uuid-here",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "identityType": "EMAIL",
  "identity": "user@example.com"
}
```

**Example Usage:**
```javascript
input: {
  tenantId: "550e8400-e29b-41d4-a716-446655440000",
  identityType: "EMAIL",
  identity: "user@example.com"
}

output: {
  id: "identity-abc123",
  tenantId: "550e8400-e29b-41d4-a716-446655440000",
  identityType: "EMAIL",
  identity: "user@example.com"
}
```

**Implementation Notes:**
- The combination of identityType + identity must be unique
- Generate UUID for identity record ID

**Error Cases:**
- `VALIDATION_ERROR`: Invalid parameters
- `NOT_FOUND`: Tenant with given tenantId doesn't exist
- `CONFLICT`: Identity already associated with another tenant

---

### Function 6: validateTenantCredentials

**Description:** Validates a tenant's password credentials.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |
| rawPassword | String | Yes | Plain text password to validate |

**Returns:** boolean (true if valid, false if invalid)

**Example Usage:**
```javascript
input: {
  tenantId: "550e8400-e29b-41d4-a716-446655440000",
  rawPassword: "SecurePass123!"
}

output: true
```

**Implementation Notes:**
- Use secure password comparison (e.g., bcrypt.compare())
- Return false if tenant doesn't exist
- Consider implementing rate limiting to prevent brute force attacks

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null parameters

---

### Function 7: createTenantRefreshToken

**Description:** Creates and persists a new refresh token (session) for the specified tenant.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |
| metadata | Map<String, Object> | No | Additional metadata to store with the token |

**Returns:** String (refresh token identifier)

**Example Usage:**
```javascript
input: {
  tenantId: "550e8400-e29b-41d4-a716-446655440000",
  metadata: {
    "device": "mobile",
    "ip": "192.168.1.1"
  }
}

output: "refresh_token_abc123xyz789"
```

**Implementation Notes:**
- Generate cryptographically secure random token
- Store token with tenant association
- Track token expiration internally (e.g., 30 days)
- Store metadata for audit purposes
- Consider implementing token family/chain for rotation

**Error Cases:**
- `VALIDATION_ERROR`: Invalid tenantId
- `NOT_FOUND`: Tenant doesn't exist

---

### Function 8: refreshToken

**Description:** Validates and refreshes a token, optionally rotating it for security.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| refreshToken | String | Yes | Current refresh token |

**Returns:** TenantAndRefreshToken or null

**TenantAndRefreshToken Structure:**
```json
{
  "tenant": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "createdAt": 1698710400000,
    "updatedAt": 1698710400000
  },
  "refreshToken": "new_refresh_token_xyz456"
}
```

**Example Usage:**
```javascript
input: {
  refreshToken: "refresh_token_abc123xyz789"
}

output: {
  tenant: {
    id: "550e8400-e29b-41d4-a716-446655440000",
    username: "john_doe",
    createdAt: 1698710400000,
    updatedAt: 1698710400000
  },
  refreshToken: "new_refresh_token_xyz456"
}
```

**Implementation Notes:**
- Validate token exists and is not expired
- Validate token is not revoked
- Optionally rotate token (return new token, invalidate old one)
- Update last used timestamp
- Return null if token is invalid, expired, or revoked

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null refreshToken
- `EXPIRED`: Token has expired
- `UNAUTHORIZED`: Token has been revoked

---

### Function 9: revokeTenantRefreshToken

**Description:** Revokes (invalidates) a refresh token, preventing it from being used for future token refresh operations.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| refreshToken | String | Yes | Refresh token to revoke |

**Returns:** void (throws error on failure)

**Example Usage:**
```javascript
input: {
  refreshToken: "refresh_token_abc123xyz789"
}

// No return value on success
```

**Implementation Notes:**
- Mark token as revoked in storage
- Do not physically delete token (maintain audit trail)
- Idempotent operation (revoking already-revoked token should succeed)
- Consider cascade revocation for token families

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null refreshToken
- `NOT_FOUND`: Token doesn't exist

---

### Function 10: generateAPIKeyForTenant

**Description:** Generate an API key for the specified tenant that can be used for authentication.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |

**Returns:** String (API key)

**Example Usage:**
```javascript
input: {
  tenantId: "550e8400-e29b-41d4-a716-446655440000"
}

output: "ask_abc123xyz789def456"
```

**Implementation Notes:**
- Generate cryptographically secure random key
- Prefix with "ask_" for identification
- Store hashed version in database
- Key should be at least 32 characters
- Consider rate limiting API key generation

**Error Cases:**
- `VALIDATION_ERROR`: Invalid tenantId
- `NOT_FOUND`: Tenant doesn't exist

---

### Function 11: getTenantByApiKey

**Description:** Retrieve tenant information using an API key.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| apiKey | String | Yes | API key string |

**Returns:** FetchedTenant or null

**Example Usage:**
```javascript
input: {
  apiKey: "ask_abc123xyz789def456"
}

output: {
  id: "550e8400-e29b-41d4-a716-446655440000",
  username: "john_doe",
  createdAt: 1698710400000,
  updatedAt: 1698710400000
}
```

**Implementation Notes:**
- Compare hashed API key with stored values
- Consider implementing rate limiting
- Track API key usage for audit purposes

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null apiKey

---

## OTP Service

### Purpose
Generates, stores, and manages one-time passwords for authentication workflows.

### REST API Endpoints

#### 1. Generate and Save OTP

**Endpoint:** `POST /api/v1/otp/generate`

**Request Body:**
```json
{
  "otpType": "NUMERIC",
  "otpLength": 4,
  "minNumber": -1,
  "minAlphabet": -1,
  "maxNumber": -1,
  "maxAlphabet": -1,
  "expiresAfterSecond": 300,
  "context": "AUTHSOME_TENANT_SIGNUP",
  "metadata": {
    "identity": "user@example.com",
    "identityType": "EMAIL",
    "username": "john_doe",
    "password": "encrypted_password_here"
  }
}
```

**Response (201 Created):**
```json
{
  "id": "otp-550e8400-e29b-41d4-a716-446655440000",
  "code": "1234",
  "context": "AUTHSOME_TENANT_SIGNUP",
  "expiresAt": 1698710700000,
  "metadata": {
    "identity": "user@example.com",
    "identityType": "EMAIL",
    "username": "john_doe",
    "password": "encrypted_password_here"
  }
}
```

---

#### 2. Get OTP by ID

**Endpoint:** `GET /api/v1/otp/{otpId}`

**Path Parameters:**
- `otpId` - OTP identifier (UUID)

**Response (200 OK):**
```json
{
  "id": "otp-550e8400-e29b-41d4-a716-446655440000",
  "code": "1234",
  "context": "AUTHSOME_TENANT_SIGNUP",
  "expiresAt": 1698710700000,
  "metadata": {
    "identity": "user@example.com"
  }
}
```

**Response (404 Not Found):** OTP not found

---

### Function 1: generateAndSaveOtp

**Description:** Generates a random OTP and stores it with metadata for later verification.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| otpType | OtpType | Yes | Type of OTP to generate |
| otpLength | Integer | Yes | Length of OTP (typically 4-8) |
| minNumber | Integer | Yes | Min numeric chars (-1 = ignore) |
| minAlphabet | Integer | Yes | Min alphabetic chars (-1 = ignore) |
| maxNumber | Integer | Yes | Max numeric chars (-1 = ignore) |
| maxAlphabet | Integer | Yes | Max alphabetic chars (-1 = ignore) |
| expiresAfterSecond | Integer | Yes | Expiry time in seconds |
| context | String | Yes | Context identifier |
| metadata | Map<String, String> | Yes | Additional data to store |

**Returns:** FetchedOtp

**FetchedOtp Structure:**
```json
{
  "id": "otp-550e8400-e29b-41d4-a716-446655440000",
  "code": "1234",
  "context": "AUTHSOME_TENANT_SIGNUP",
  "expiresAt": 1698710700000,
  "metadata": {
    "identity": "user@example.com",
    "identityType": "EMAIL",
    "username": "john_doe",
    "password": "encryptedString"
  }
}
```

**Common Usage Example (Signup OTP):**
```javascript
input: {
  otpType: "NUMERIC",
  otpLength: 4,
  minNumber: -1,
  minAlphabet: -1,
  maxNumber: -1,
  maxAlphabet: -1,
  expiresAfterSecond: 300,
  context: "AUTHSOME_TENANT_SIGNUP",
  metadata: {
    "identity": "user@example.com",
    "identityType": "EMAIL",
    "username": "john_doe",
    "password": "encrypted_password_here"
  }
}

output: {
  id: "otp-550e8400-e29b-41d4-a716-446655440000",
  code: "1234",
  context: "AUTHSOME_TENANT_SIGNUP",
  expiresAt: 1698710700000,
  metadata: { /* same as input */ }
}
```

**Implementation Notes:**
- Use cryptographically secure random generator (e.g., SecureRandom in Java, secrets in Python)
- Calculate expiresAt = current timestamp + (expiresAfterSecond * 1000)
- For NUMERIC OTPs, ignore min/max alphabet constraints
- Store the OTP securely (consider hashing if very sensitive)
- The id + context combination should be unique

**Parameter Rules:**
- -1 means "ignore this constraint"
- 0 is invalid (throw VALIDATION_ERROR)
- Positive values apply the constraint

**Error Cases:**
- `VALIDATION_ERROR`: Invalid otpType, otpLength <= 0, expiresAfterSecond <= 0
- `VALIDATION_ERROR`: Conflicting constraints (e.g., minNumber > otpLength)
- `VALIDATION_ERROR`: Empty or null context

---

### Function 2: getOtpById

**Description:** Retrieves a stored OTP by its ID.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | String | Yes | OTP identifier (UUID) |

**Returns:** FetchedOtp or null

**Example Usage:**
```javascript
input: {
  id: "otp-550e8400-e29b-41d4-a716-446655440000"
}

output: {
  id: "otp-550e8400-e29b-41d4-a716-446655440000",
  code: "1234",
  context: "AUTHSOME_TENANT_SIGNUP",
  expiresAt: 1698710700000,
  metadata: { /* stored metadata */ }
}
```

**Implementation Notes:**
- Return null if OTP doesn't exist
- Do NOT automatically delete expired OTPs in this function
- Consider implementing automatic cleanup via background job

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null id

---

## Notifier Service

### Purpose
Sends notifications to users through various channels (currently email only).

### REST API Endpoints

#### 1. Send Notification

**Endpoint:** `POST /api/v1/notifier/send`

**Request Body:**
```json
{
  "identityType": "EMAIL",
  "identity": "user@example.com",
  "subject": "OTP to create authsome account",
  "content": "Your OTP to create your Authsome account is: 1234"
}
```

**Response (204 No Content):** Successfully sent

---

### Function 1: sendNotification

**Description:** Sends a notification via the specified channel.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| identityType | IdentityType | Yes | Notification channel (EMAIL) |
| identity | String | Yes | Recipient address |
| subject | String | Yes | Notification subject/title |
| content | String | Yes | Notification body/message |

**Returns:** void (throws error on failure)

**Example Usage:**
```javascript
input: {
  identityType: "EMAIL",
  identity: "user@example.com",
  subject: "OTP to create authsome account",
  content: "Your OTP to create your Authsome account is: 1234"
}

// No return value on success
```

**Implementation Notes:**
- For EMAIL: Use SMTP or email service provider (SendGrid, AWS SES, etc.)
- Should be asynchronous if possible (don't block caller)
- Implement retry logic for transient failures
- Log all sent notifications for audit trail
- Consider rate limiting (max 10 per identity per hour)

**Error Cases:**
- `VALIDATION_ERROR`: Invalid identityType
- `VALIDATION_ERROR`: Empty or null parameters
- `INTERNAL_ERROR`: Failed to send (network error, invalid recipient, etc.)

---

## JWT Service

### Purpose
Generates and parses JSON Web Tokens (JWT) for authentication and authorization.

### REST API Endpoints

#### 1. Generate Token

**Endpoint:** `POST /api/v1/jwt/generate`

**Request Body:**
```json
{
  "subject": "550e8400-e29b-41d4-a716-446655440000",
  "claims": {
    "role": "user",
    "scope": "read:profile"
  },
  "issuer": "AUTHSOME_TENANT",
  "expiry": 3600,
  "expiryUnit": "MINUTES"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpc3MiOiJBVVRIU09NRV9URU5BTlQiLCJleHAiOjE2OTg5Mjc2MDAsInJvbGUiOiJ1c2VyIiwic2NvcGUiOiJyZWFkOnByb2ZpbGUifQ.signature"
}
```

---

#### 2. Parse Token

**Endpoint:** `POST /api/v1/jwt/parse`

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "subject": "550e8400-e29b-41d4-a716-446655440000",
  "issuer": "AUTHSOME_TENANT",
  "issuedAt": 1698710400000,
  "expired": false,
  "claims": {
    "role": "user",
    "scope": "read:profile"
  }
}
```

**Error (400 Bad Request):** Invalid or malformed token

---

### Function 1: generateToken

**Description:** Generates a signed JWT token with specified claims and expiration.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| subject | String | Yes | Token subject (typically user/tenant ID) |
| claims | Map<String, String> | No | Additional claims to include in token |
| issuer | String | Yes | Token issuer identifier |
| expiry | Integer | Yes | Token expiration value |
| expiryUnit | TimeUnit | Yes | Unit of time for expiry (SECONDS, MINUTES, HOURS, DAYS) |

**Returns:** String (JWT token)

**Example Usage:**
```javascript
input: {
  subject: "550e8400-e29b-41d4-a716-446655440000",
  claims: {
    "role": "user",
    "scope": "read:profile"
  },
  issuer: "AUTHSOME_TENANT",
  expiry: 3600,
  expiryUnit: "MINUTES"
}

output: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpc3MiOiJBVVRIU09NRV9URU5BTlQiLCJleHAiOjE2OTg5Mjc2MDAsInJvbGUiOiJ1c2VyIiwic2NvcGUiOiJyZWFkOnByb2ZpbGUifQ.signature"
```

**Implementation Notes:**
- Use secure signing algorithm (HS256, RS256, or ES256)
- Store signing key securely (not in code)
- Calculate expiration: current time + (expiry * expiryUnit)
- Include standard JWT claims: sub (subject), iss (issuer), exp (expiration), iat (issued at)
- Add custom claims if provided
- Tokens should be stateless and verifiable

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null subject or issuer
- `VALIDATION_ERROR`: Invalid expiry value (<= 0)
- `INTERNAL_ERROR`: Token generation failure

---

### Function 2: parseToken

**Description:** Parses and validates a JWT token, returning its claims and metadata.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | String | Yes | JWT token to parse |

**Returns:** ParsedToken

**ParsedToken Structure:**
```json
{
  "subject": "550e8400-e29b-41d4-a716-446655440000",
  "issuer": "AUTHSOME_TENANT",
  "issuedAt": 1698710400000,
  "expired": false,
  "claims": {
    "role": "user",
    "scope": "read:profile"
  }
}
```

**Example Usage:**
```javascript
input: {
  token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

output: {
  subject: "550e8400-e29b-41d4-a716-446655440000",
  issuer: "AUTHSOME_TENANT",
  issuedAt: 1698710400000,
  expired: false,
  claims: {
    "role": "user",
    "scope": "read:profile"
  }
}
```

**Implementation Notes:**
- Verify token signature using the same key used for generation
- Check token expiration and set `expired` field accordingly
- Extract standard JWT claims (sub, iss, iat, exp)
- Extract custom claims into the `claims` map
- Return parsed data even if token is expired (caller decides how to handle)

**Error Cases:**
- `VALIDATION_ERROR`: Empty or null token
- `VALIDATION_ERROR`: Malformed JWT token
- `VALIDATION_ERROR`: Invalid signature
- `INTERNAL_ERROR`: Token parsing failure

---

## Error Handling

### Error Types

| Error Type | When to Use | HTTP Status |
|------------|-------------|-------------|
| VALIDATION_ERROR | Invalid input, missing fields, constraint violations | 400 |
| NOT_FOUND | Entity doesn't exist | 404 |
| CONFLICT | Duplicate resource (username/email exists) | 409 |
| EXPIRED | OTP expired, session timeout | 410 |
| UNAUTHORIZED | Invalid credentials, invalid token | 401 |
| INTERNAL_ERROR | Database errors, unexpected exceptions | 500 |

### Error Response Structure
```json
{
  "errorType": "CONFLICT",
  "message": "Username already exists",
  "details": {
    "field": "username",
    "value": "john_doe"
  }
}
```

### Example Error Scenarios

```javascript
// Username already taken
{
  errorType: "CONFLICT",
  message: "Username already exists",
  details: {
    field: "username",
    value: "john_doe"
  }
}

// Invalid OTP
{
  errorType: "VALIDATION_ERROR",
  message: "Invalid OTP code",
  details: {
    field: "otp"
  }
}

// OTP expired
{
  errorType: "EXPIRED",
  message: "OTP has expired",
  details: {
    expiresAt: 1698710700000,
    currentTime: 1698711000000
  }
}

// Invalid JWT signature
{
  errorType: "VALIDATION_ERROR",
  message: "Invalid token signature",
  details: {
    field: "token"
  }
}
```

---

## Implementation Guide

### Quick Start Checklist

#### Tenant Service
- [ ] Implement database schema for tenants and identities
- [ ] Hash passwords using bcrypt (cost 10+) or argon2id
- [ ] Ensure username uniqueness constraint
- [ ] Ensure identity + identityType uniqueness constraint
- [ ] Implement refresh token storage with expiration tracking
- [ ] Implement API key generation and storage (hashed)
- [ ] Implement all 11 functions
- [ ] If microservice: Implement all 11 REST endpoints

#### OTP Service
- [ ] Implement database schema for OTPs
- [ ] Use cryptographically secure random generator
- [ ] Implement OTP generation logic with constraints
- [ ] Set up background job for expired OTP cleanup
- [ ] Consider rate limiting (5 OTPs per identity per hour)
- [ ] If microservice: Implement all 2 REST endpoints

#### Notifier Service
- [ ] Configure email SMTP settings
- [ ] Implement email template system (optional)
- [ ] Add retry logic for failures
- [ ] Set up notification logging
- [ ] Consider rate limiting (10 notifications per identity per hour)
- [ ] If microservice: Implement all 1 REST endpoint

#### JWT Service
- [ ] Generate or obtain signing key (HS256/RS256)
- [ ] Store signing key securely (environment variable or key vault)
- [ ] Implement token generation with standard JWT claims
- [ ] Implement token parsing and validation
- [ ] Set appropriate token expiration times
- [ ] If microservice: Implement all 2 REST endpoints

### Microservices Architecture

If implementing as microservices, follow these guidelines:

#### Network Configuration
- Deploy all services in the same private network/VPC
- Use internal DNS for service discovery
- Configure firewall rules to allow inter-service communication
- Block external access to service-to-service endpoints

#### Service Communication
```
┌─────────────────┐
│   API Gateway   │  (External, authenticated)
└────────┬────────┘
         │
    ┌────┴────┬────────┬──────────┐
    │         │        │          │
┌───▼───┐ ┌──▼──┐ ┌───▼───┐ ┌────▼────┐
│Tenant │ │ OTP │ │Notify │ │   JWT   │
│Service│ │Svc  │ │ Svc   │ │ Service │
└───────┘ └─────┘ └───────┘ └─────────┘
(Internal, no auth between services)
```

#### REST Client Example (Java)
```java
// Example: Calling OTP Service from Tenant Service
RestTemplate restTemplate = new RestTemplate();
String otpServiceUrl = "http://otp-service:8080/api/v1/otp/generate";

OtpGenerateRequest request = new OtpGenerateRequest(
    OtpType.NUMERIC,
    4,
    -1, -1, -1, -1,
    300,
    "AUTHSOME_TENANT_SIGNUP",
    metadata
);

ResponseEntity<FetchedOtp> response = restTemplate.postForEntity(
    otpServiceUrl,
    request,
    FetchedOtp.class
);

FetchedOtp otp = response.getBody();
```

#### REST Client Example (Python)
```python
# Example: Calling Notifier Service from Tenant Service
import requests

notifier_url = "http://notifier-service:8080/api/v1/notifier/send"

payload = {
    "identityType": "EMAIL",
    "identity": "user@example.com",
    "subject": "OTP to create authsome account",
    "content": f"Your OTP is: {otp_code}"
}

response = requests.post(notifier_url, json=payload)
response.raise_for_status()  # Raises exception for 4xx/5xx
```

#### REST Client Example (Node.js)
```javascript
// Example: Calling JWT Service from Tenant Service
const axios = require('axios');

const jwtServiceUrl = 'http://jwt-service:8080/api/v1/jwt/generate';

const payload = {
  subject: tenantId,
  claims: { role: 'user' },
  issuer: 'AUTHSOME_TENANT',
  expiry: 3600,
  expiryUnit: 'MINUTES'
};

try {
  const response = await axios.post(jwtServiceUrl, payload);
  const token = response.data.token;
  console.log('Generated token:', token);
} catch (error) {
  console.error('Failed to generate token:', error.message);
}
```

### Security Requirements

#### Password Security
- ✓ NEVER store passwords in plain text
- ✓ Use bcrypt (cost 10+) or argon2id
- ✗ Don't use MD5, SHA1, or simple hashing

#### OTP Security
- ✓ Use SecureRandom (Java) or secrets (Python)
- ✓ Generate unpredictable codes
- ✗ Don't use Math.random() or predictable patterns

#### Token Security
- ✓ Use cryptographically secure random for refresh tokens
- ✓ Implement token rotation on refresh
- ✓ Store refresh tokens securely with expiration
- ✓ Use secure JWT signing algorithms (HS256, RS256, ES256)
- ✗ Don't use symmetric algorithms for public APIs (prefer RS256)

#### API Key Security
- ✓ Generate cryptographically secure random keys
- ✓ Store hashed version in database (like passwords)
- ✓ Prefix with "ask_" for identification
- ✓ Minimum 32 characters length
- ✗ Don't store plain text API keys

#### Data Encryption
- ✓ Use TLS/HTTPS for network communication
- ✓ Encrypt sensitive data at rest (AES-256)
- ✓ Store encryption keys securely (not in code)

#### Inter-Service Communication (Current Implementation)
- ✓ Deploy services in private network/VPC
- ✓ Use internal DNS for service discovery
- ✓ Configure firewall rules appropriately
- ✗ No authentication required between services (trusted network)
- ⚠️ **Note:** Authentication between services (mTLS, JWT) will be added in future versions

---

## Testing Guide

### Unit Tests
- Test each function with valid inputs
- Test each function with invalid inputs (null, empty, wrong type)
- Test all error cases
- Test edge cases (expired OTPs, duplicate usernames)
- Test token generation and validation
- Test password validation with correct and incorrect passwords

### Integration Tests
- Test with real database
- Test with real email provider (or mock)
- Test complete signup flow end-to-end
- Test complete sign-in and token refresh flow
- Test token revocation
- Test API key generation and authentication

### REST API Tests (if using microservices)
- Test all endpoints with valid requests
- Test all endpoints with invalid requests
- Test error responses and status codes
- Test inter-service communication
- Test network failures and timeouts
- Test retry logic

### Performance Benchmarks
- OTP generation: < 100ms
- Tenant lookup: < 50ms
- Email sending: Non-blocking (async)
- JWT generation: < 50ms
- Token validation: < 10ms
- REST API call overhead: < 20ms per hop

---

## REST API Client Libraries

### Recommended HTTP Clients by Language

| Language | Recommended Library | Notes |
|----------|-------------------|-------|
| Java | RestTemplate, WebClient | Spring framework built-in |
| Python | requests, httpx | httpx for async support |
| Node.js | axios, node-fetch | axios has better error handling |
| Go | net/http, resty | resty for higher-level API |
| C# | HttpClient | Built-in .NET client |
| Ruby | Faraday, HTTParty | Faraday more flexible |

### Common HTTP Client Configuration

```javascript
// Timeout configuration (example in Node.js)
const axiosInstance = axios.create({
  baseURL: 'http://tenant-service:8080',
  timeout: 5000, // 5 second timeout
  headers: {
    'Content-Type': 'application/json'
  }
});

// Error handling
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Server responded with error status
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      // No response received
      console.error('Network Error:', error.message);
    }
    throw error;
  }
);
```

---

## Future Enhancements

Planned features for future versions:

### Version 1.4 (Planned)
- Inter-service authentication (mutual TLS or JWT)
- API rate limiting service
- Comprehensive audit logging
- SMS notifications (`IdentityType.SMS`)
- Push notifications (`IdentityType.PUSH`)

### Version 1.5 (Planned)
- Multi-factor authentication (MFA)
- OTP resend functionality
- Password reset workflow
- Tenant profile updates
- Session management improvements

### Version 2.0 (Planned)
- OAuth 2.0 / OpenID Connect support
- SAML integration
- Role-based access control (RBAC)
- Tenant organizations/teams
- Advanced analytics and monitoring

---

## Appendix: Complete Endpoint Reference

### Tenant Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/tenant/get-by-identity | Get tenant by identity |
| GET | /api/v1/tenant/get-by-username/{username} | Get tenant by username |
| GET | /api/v1/tenant/{tenantId} | Get tenant by ID |
| POST | /api/v1/tenant/create | Create new tenant |
| POST | /api/v1/tenant/{tenantId}/identity | Add identity to tenant |
| POST | /api/v1/tenant/{tenantId}/validate-credentials | Validate credentials |
| POST | /api/v1/tenant/{tenantId}/refresh-token | Create refresh token |
| POST | /api/v1/tenant/token/refresh | Refresh token |
| DELETE | /api/v1/tenant/token/revoke | Revoke refresh token |
| POST | /api/v1/tenant/{tenantId}/api-key | Generate API key |
| POST | /api/v1/tenant/get-by-api-key | Get tenant by API key |

### OTP Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/otp/generate | Generate and save OTP |
| GET | /api/v1/otp/{otpId} | Get OTP by ID |

### Notifier Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/notifier/send | Send notification |

### JWT Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/jwt/generate | Generate JWT token |
| POST | /api/v1/jwt/parse | Parse JWT token |

---

**Document Version:** 1.3  
**Last Updated:** November 2, 2025  
**Status:** Active  
**Changes from v1.2:**
- Added REST API Endpoints section with complete specifications
- Added REST endpoint definitions for all services (11 for Tenant, 2 for OTP, 1 for Notifier, 2 for JWT)
- Added inter-service communication guidelines
- Added REST client examples in Java, Python, and Node.js
- Added HTTP status code mapping
- Added security notes for inter-service communication (no auth required in current version)
- Added REST API testing guidelines
- Added HTTP client library recommendations
- Added complete endpoint reference appendix
- Added `getTenantById` function to Tenant Service
- Added `parseToken` function to JWT Service
- Added `generateAPIKeyForTenant` function to Tenant Service
- Added `getTenantByApiKey` function to Tenant Service