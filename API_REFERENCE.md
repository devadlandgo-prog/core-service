# LandGo API Reference

This document provides `curl` command examples for the most frequently used APIs in the LandGo microservices ecosystem. All requests should be directed through the API Gateway at `https://api.landgo.ca`.

---

## 🔐 Authentication & Identity (`user-service`)

### 1. Register a New User
Registers a new account. Roles can be `buyer`, `seller`, `professional`, or `admin`.

```bash
curl -X POST https://api.landgo.ca/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "firstName": "John",
           "lastName": "Doe",
           "email": "john.doe@example.com",
           "password": "Password123!",
           "confirmPassword": "Password123!",
           "role": "buyer"
         }'
```

### 2. Login
Authenticates credentials and returns a JWT Access Token and Refresh Token.

```bash
curl -X POST https://api.landgo.ca/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "email": "john.doe@example.com",
           "password": "Password123!"
         }'
```

### 3. Get Current User Profile (Authenticated)
Requires the `accessToken` received from the login response.

```bash
curl -X GET https://api.landgo.ca/profile/me \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🏡 Real Estate Listings (`core-service`)

### 4. Search All Listings
Publicly accessible endpoint to view land listings.

```bash
curl -X GET "https://api.landgo.ca/listings?page=0&size=10"
```

### 5. Create a New Listing (Authenticated - Seller/Admin)
Requires a valid token and seller role.

```bash
curl -X POST https://api.landgo.ca/listings \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
           "title": "Beautiful 5-Acre Plot",
           "description": "High fertility soil, perfect for farming.",
           "price": 250000,
           "location": "Ontario, Canada",
           "listingType": "SALE"
         }'
```

---

## 💳 Payments & Subscriptions (`payment-service`)

### 6. Create Stripe Payment Intent
Initializes a payment session for feature upgrades or subscriptions.

```bash
curl -X POST https://api.landgo.ca/payments/intent \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
           "amount": 50.00,
           "currency": "usd",
           "description": "Premium Listing Upgrade"
         }'
```

---

## 🛠 Developer Tools & Health

### 7. Health Check (Unified Gateway Entry)
Verifies if the service and its dependencies are operational.

*   **Gateway**: `https://api.landgo.ca/actuator/health`
*   **User Service**: `https://api.landgo.ca/user-service/actuator/health`
*   **Core Service**: `https://api.landgo.ca/core-service/actuator/health`
*   **Payment Service**: `https://api.landgo.ca/payment-service/actuator/health`

### 8. Service Documentation (OpenAPI JSON)
Retrieve the raw OpenAPI specifications for integration or mapping:

*   **User OpenAPI**: `https://api.landgo.ca/user-service/v3/api-docs`
*   **Core OpenAPI**: `https://api.landgo.ca/core-service/v3/api-docs`
*   **Payment OpenAPI**: `https://api.landgo.ca/payment-service/v3/api-docs`

> [!NOTE]
> For interactive Swagger UI, it is recommended to use the individual service dashboards (e.g., `https://user.landgo.ca/swagger-ui/index.html`) until a Gateway-level UI aggregator is fully deployed.
