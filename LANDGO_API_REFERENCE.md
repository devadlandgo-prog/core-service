# LandGo Unified API Reference (Full JSON Documentation)

All requests must be routed through the **API Gateway** (Port 8080).
**Base URL:** `http://{GATEWAY_HOST}:8080`

---

## 1. User & Authentication Service

### POST `/api/auth/register` [Public]
**Request Body:**
```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "password": "strongPassword123",
  "role": "BUYER",
  "phone": "+1234567890",
  "isProfessional": false,
  "agentAuth": false
}
```
**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid",
    "fullName": "John Doe",
    "email": "user@example.com",
    "phone": "+1234567890",
    "role": "BUYER",
    "isVerified": false,
    "joinedDate": "2024-05-01T12:00:00",
    "currency": "CAD"
  }
}
```

### POST `/api/auth/login` [Public]
**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "strongPassword123"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": "uuid",
      "fullName": "John Doe",
      "email": "user@example.com",
      "role": "BUYER"
    },
    "tokens": {
      "accessToken": "eyJ...",
      "refreshToken": "eyJ...",
      "expiresIn": 3600
    }
  }
}
```

### POST `/api/auth/google` [Public]
**Request Body:**
```json
{
  "idToken": "google-id-token-string"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Google login successful",
  "data": {
    "user": { "id": "uuid", "fullName": "John Doe", "email": "user@example.com" },
    "tokens": { "accessToken": "...", "refreshToken": "...", "expiresIn": 3600 }
  }
}
```

### POST `/api/auth/mfa/verify` [Public]
**Request Body:**
```json
{
  "mfaSession": "short-lived-session-token",
  "code": "123456"
}
```
**Response:** (Returns final JWT tokens)
```json
{
  "success": true,
  "message": "MFA verified successfully",
  "data": {
    "user": { ... },
    "tokens": { ... }
  }
}
```

### POST `/api/auth/mfa/resend` [Public]
**Request Body:**
```json
{
  "mfaSession": "short-lived-session-token"
}
```
**Response:**
```json
{
  "success": true,
  "message": "MFA code resent successfully",
  "data": null
}
```

### PATCH `/api/users/me/mfa` [Token Required]
**Request Body:**
```json
{
  "phone": "+1234567890",
  "enabled": true
}
```
**Response:**
```json
{
  "success": true,
  "message": "MFA status updated",
  "data": { ... }
}
```

### POST `/api/auth/verify` [Public]
**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Email verified",
  "data": {
    "id": "uuid",
    "fullName": "John Doe",
    "email": "user@example.com",
    "isVerified": true
  }
}
```

### GET `/api/auth/me` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "User retrieved",
  "data": {
    "id": "uuid",
    "fullName": "John Doe",
    "email": "user@example.com",
    "role": "BUYER",
    "isVerified": true
  }
}
```

### PATCH `/api/user/profile` [Token Required]
**Request Body:**
```json
{
  "fullName": "John Updated Doe",
  "phone": "+1234567890",
  "location": "Toronto",
  "bio": "Searching for land",
  "currency": "CAD"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Profile updated",
  "data": {
    "id": "uuid",
    "fullName": "John Updated Doe",
    "email": "user@example.com",
    "phone": "+1234567890",
    "location": "Toronto",
    "isVerified": true
  }
}
```

### DELETE `/api/profile` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "Account deleted successfully",
  "data": null
}
```

### GET `/api/user/stats` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "Stats retrieved",
  "data": {
    "activeListings": 5,
    "totalViews": 1250
  }
}
```

---

## 2. Listings & Properties

### GET `/api/listings` [Public]
**Response:**
```json
{
  "success": true,
  "message": "Listings retrieved",
  "data": {
    "content": [
      {
        "id": "uuid",
        "title": "Industrial Plot",
        "price": 1200000,
        "currency": "CAD",
        "city": "Brampton",
        "isFeatured": false
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### GET `/api/listings/recommendation` [Public]
**Response:** (Sorted by Price Desc)
```json
{
  "success": true,
  "message": "Recommendations retrieved",
  "data": {
    "content": [
      {
        "id": "uuid",
        "title": "Luxury Mansion Site",
        "price": 9500000,
        "currency": "CAD",
        "isFeatured": true
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### POST `/api/listings/submit` [Token Required]
**Request Body:**
```json
{
  "draftId": "uuid"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Listing submitted",
  "data": {
    "id": "listing-uuid",
    "status": "PENDING_APPROVAL"
  }
}
```

### POST `/api/listings/:id/favorite` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "Favorite toggled",
  "data": {
    "success": true,
    "favorited": true
  }
}
```

### POST `/api/listings/:id/enquiry` [Public/Token]
**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "message": "I want to visit this plot."
}
```
**Response:**
```json
{
  "success": true,
  "message": "Enquiry sent successfully",
  "data": null
}
```

---

## 3. Professionals

### POST `/api/professionals/inquiry` [Token Required]
**Request Body:**
```json
{
  "professionalId": "uuid",
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "message": "Quote for fencing please",
  "listingId": "optional-uuid"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Inquiry sent",
  "data": {
    "inquiryId": "uuid"
  }
}
```

### PATCH `/api/professionals/me/expertise` [Token Required]
**Request Body:**
```json
{
  "expertise": ["Architecture", "Land Surveying"]
}
```
**Response:**
```json
{
  "success": true,
  "message": "Expertise updated",
  "data": {
    "expertise": ["Architecture", "Land Surveying"]
  }
}
```

---

## 4. Payments & Subscriptions

### POST `/api/payment/payment-sheet` [Token Required]
**Request Body:**
```json
{
  "amount": 49.99,
  "currency": "CAD"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Payment sheet ready",
  "data": {
    "paymentIntent": "pi_123...",
    "ephemeralKey": "ek_123...",
    "customer": "cus_123...",
    "publishableKey": "pk_test_..."
  }
}
```

### GET `/api/transactions` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "Transactions retrieved",
  "data": {
    "content": [
      {
        "id": "uuid",
        "amount": 49.99,
        "currency": "CAD",
        "status": "succeeded",
        "createdAt": "2024-05-02T14:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1
  }
}
```

---

## 5. System & Notifications

### GET `/api/notifications` [Token Required]
**Response:**
```json
{
  "success": true,
  "message": "Notifications retrieved",
  "data": [
    {
      "id": "uuid",
      "message": "Your profile was verified!",
      "isRead": false,
      "createdAt": "2024-05-02T10:00:00"
    }
  ]
}
```

### GET `/api/ping` [Public]
**Response:**
```json
{
  "success": true,
  "message": "System healthy",
  "data": {
    "status": "UP"
  }
}
```
