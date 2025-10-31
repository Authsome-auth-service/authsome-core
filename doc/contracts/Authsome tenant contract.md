# Authsome Service Interface Contracts

**Version:** 1.2  
**Last Updated:** October 31, 2025  
**Purpose:** This document defines the functional contracts for all Authsome services. Developers can implement these services in any programming language or framework.

## Table of Contents
1. [Overview](#overview)
2. [Common Data Types](#common-data-types)
3. [Tenant Service](#tenant-service)
4. [OTP Service](#otp-service)
5. [Notifier Service](#notifier-service)
6. [JWT Service](#jwt-service)
7. [Error Handling](#error-handling)
8. [Implementation Guide](#implementation-guide)

---

## Overview

### Architecture
Each service is **independent** and must implement the functions defined in this contract. Services communicate through well-defined interfaces - the exact communication mechanism (REST, gRPC, direct calls) is implementation-specific.

### Key Principles
- **Stateless Operations:** Each function call is independent
- **Clear Responsibilities:** Each service has a single, well-defined purpose
- **JSON Serialization:** All data types must be serializable to JSON
- **Consistent Error Handling:** Use standardized error types across all services

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

---

## Tenant Service

### Purpose
Manages tenant (user) accounts including creation, lookup, identity management, credential validation, and session/token management.

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

### Function 3: createTenant

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

### Function 4: addIdentityForTenant

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

### Function 5: validateTenantCredentials

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

### Function 6: createTenantRefreshToken

**Description:** Creates and persists a new refresh token (session) for the specified tenant.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| tenantId | String | Yes | UUID of the tenant |
| metadata | Map<String, String> | No | Additional metadata to store with the token |

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

### Function 7: refreshToken

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

### Function 8: revokeTenantRefreshToken

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

## OTP Service

### Purpose
Generates, stores, and manages one-time passwords for authentication workflows.

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
Generates JSON Web Tokens (JWT) for authentication and authorization.

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
- [ ] Implement all 8 functions

#### OTP Service
- [ ] Implement database schema for OTPs
- [ ] Use cryptographically secure random generator
- [ ] Implement OTP generation logic with constraints
- [ ] Set up background job for expired OTP cleanup
- [ ] Consider rate limiting (5 OTPs per identity per hour)

#### Notifier Service
- [ ] Configure email SMTP settings
- [ ] Implement email template system (optional)
- [ ] Add retry logic for failures
- [ ] Set up notification logging
- [ ] Consider rate limiting (10 notifications per identity per hour)

#### JWT Service
- [ ] Generate or obtain signing key (HS256/RS256)
- [ ] Store signing key securely (environment variable or key vault)
- [ ] Implement token generation with standard JWT claims
- [ ] Implement token validation/verification (for future use)
- [ ] Set appropriate token expiration times

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

#### Data Encryption
- ✓ Use TLS/HTTPS for network communication
- ✓ Encrypt sensitive data at rest (AES-256)
- ✓ Store encryption keys securely (not in code)

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

### Performance Benchmarks
- OTP generation: < 100ms
- Tenant lookup: < 50ms
- Email sending: Non-blocking (async)
- JWT generation: < 50ms
- Token validation: < 10ms

---

## Future Enhancements

Planned features for future versions:
- SMS notifications (`IdentityType.SMS`)
- Push notifications (`IdentityType.PUSH`)
- Multi-factor authentication (MFA)
- OTP resend functionality
- Password reset workflow
- Tenant profile updates
- Comprehensive audit logging
- Dedicated rate limiting service
- Token blacklisting for immediate revocation
- JWT token validation function

---

**Document Version:** 1.2  
**Last Updated:** October 31, 2025  
**Status:** Active  
**Changes from v1.1:**
- Added `validateTenantCredentials` function to Tenant Service
- Added `createTenantRefreshToken` function to Tenant Service
- Added `refreshToken` function to Tenant Service
- Added `revokeTenantRefreshToken` function to Tenant Service
- Added JWT Service section with `generateToken` function
- Updated implementation checklist and security requirements