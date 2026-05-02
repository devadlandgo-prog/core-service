LandGo API Documentation Plan
=============================

Scope: Features and Their APIs
------------------------------

*   **Auth** – login, register, verify, resend verification, forgot-password, reset-password, me, logout, change password
*   **Listings** – public list/detail, feeds (hot-developer, recommendation, featured), search, my listings, favorites, favorite toggle, view tracking, enquiry, draft CRUD, submit, delete
*   **Professionals** – list, detail, dashboard, register, subscription plans, subscribe, inquiries, expertise options, update expertise
*   **Reviews** – list by professional, create
*   **Subscriptions / Payments** – plans, my subscription, payment sheet init, verify and fulfill, activate land, change plan, cancel, transactions
*   **Profile / Settings** – get/update profile, stats, notification preferences, notifications
*   **Locations / Filter** – countries, states, cities, filter options

Global section
--------------

**Base URL:** {API\_BASE\_URL} **Authentication:** Header Authorization: Bearer <accessToken> for all endpoints except where marked \[Public\]. Missing or invalid token: 401 Unauthorized. **Content-Type:** Request body: application/json unless noted. **Currency wherever price is shown:** The backend must include a currency field (string, ISO 4217, e.g. CAD, USD) in every response that contains a monetary amount, and accept currency where the client sends a price.

Common error response
---------------------

HTTP status: 400, 401, 403, 404, 422, 500. Body shape: { "error": "string", "message": "string", "details"?: { "fieldName": string\[\] } }.

1\. Auth APIs
-------------

Method

Path

Description

POST

/api/auth/register

Register a new user

POST

/api/auth/login

Login

POST

/api/auth/google

Google OAuth Login

GET

/api/auth/me

Get current user

POST

/api/auth/logout

Logout

POST

/api/auth/verify

Verify email with OTP

POST

/api/auth/resend-verification

Resend verification code

POST

/api/auth/forgot-password

Request password reset email

POST

/api/auth/reset-password

Reset password with token

POST

/api/auth/change-password

Change authenticated user password

**Request/response details:**

**POST /api/auth/register** \[Public\] \* **Request:** email (string), fullName (string), password (string), role (string), phone (string, optional), isProfessional (boolean, optional), agentAuth (boolean, optional). \* **Response:** success (boolean), user (id, name, email, phone, role, avatar, isVerified, joinedDate, currency, professionalId).

**POST /api/auth/login** \[Public\] \* **Request:** email (string, can be phone), password (string). \* **Response:** success (boolean), data object containing user (full user object) and tokens (accessToken, refreshToken, expiresIn).

**POST /api/auth/google** \[Public\] \* **Request:** idToken (string). \* **Response:** success (boolean), data object containing user and tokens.

**GET /api/auth/me** \* **Request:** No body. Requires Token. \* **Response:** data (full User object).

**POST /api/auth/verify** \[Public\] \* **Request:** email (string), code (string). \* **Response:** success (boolean), user (User object).

**POST /api/auth/forgot-password** \[Public\] \* **Request:** email (string). \* **Response:** success (boolean).

**POST /api/auth/reset-password** \[Public\] \* **Request:** token (string), password (string), confirmPassword (string). \* **Response:** success (boolean).

**POST /api/auth/change-password** \* **Request:** currentPassword (string), newPassword (string). \* **Response:** success (boolean).

2\. Listings APIs
-----------------

Method

Path

Description

GET

/api/listings

List listings (Explore feed)

GET

/api/listings/:id

Get single listing

GET

/api/listings/search

Search/Filter listings

GET

/api/listings/hot-developer

Hot Developer Deals

GET

/api/listings/recommendation

AI/Recommended properties

GET

/api/listings/featured

Featured properties feed

GET

/api/listings/my

Current user’s listings

GET

/api/listings/favorites

Current user’s favorite listings

POST

/api/listings/:id/favorite

Toggle favorite status

POST

/api/listings/:id/view

Increment view counter

POST

/api/listings/:id/enquiry

Send lead/enquiry

POST

/api/listings/drafts

Create draft

GET

/api/listings/drafts/:id

Get draft

PATCH

/api/listings/drafts/:id

Update draft step

POST

/api/listings/submit

Publish draft to pending/live

DELETE

/api/listings/:id

Delete listing

**Request/response details:**

**GET /api/listings (And all Feed GET endpoints: search, hot-developer, recommendation, featured, my, favorites)** \* **Query params:** page (number), limit (number), projectType, minPrice, maxPrice. (Search endpoint uses filters, others use feeds). \* **Response:** data (array of Listing objects), total (number). \* **Listing Object Fields:** id (string), title (string), price (number), currency (string, ISO 4217), status (string), category (string), listingType (string), area (number), areaUnit (string), address (string), lat (number), lng (number), images (array of objects: id, url, isPrimary), ownerName (string), ownerEmail (string), views (number), isFeaturedProperty (boolean), isDeveloperDeal (boolean), isRecommendationProperty (boolean), isFavorite (boolean).

**GET /api/listings/:id** \[Public\] \* **Response:** data (Single Listing Object).

**POST /api/listings/:id/favorite** \* **Request:** No body. Requires Token. \* **Response:** success (boolean), favorited (boolean).

**POST /api/listings/:id/view** \[Public\] \* **Request:** No body. \* **Response:** success (boolean), views (number).

**POST /api/listings/:id/enquiry** \* **Request:** name (string), email (string), phone (string), message (string). \* **Response:** success (boolean).

**POST /api/listings/drafts** \* **Request:** title (string), description (string). \* **Response:** success (boolean), id (string).

**PATCH /api/listings/drafts/:id** \* **Request:** Partial listing object containing step data (e.g. price, area, address, lat, lng). \* **Response:** success (boolean), id (string).

**POST /api/listings/submit** \* **Request:** draftId (string). \* **Response:** success (boolean), id (string), status (string).

3\. Professionals APIs
----------------------

Method

Path

Description

GET

/api/professionals

List professionals

GET

/api/professionals/:id

Get professional

POST

/api/professionals/register

Register as professional

GET

/api/professionals/me/dashboard

Get professional dashboard analytics

POST

/api/professionals/inquiry

Send a lead/enquiry

GET

/api/professionals/expertise-options

List available expertise tags

PATCH

/api/professionals/me/expertise

Update expertise tags

**Request/response details:**

**GET /api/professionals** \[Public\] \* **Query params:** page, limit, search, category. \* **Response:** data (array of Professional objects), total (number). \* **Professional Object Fields:** id (string), name (string), role (string), credentials (string), specialty (string), category (string), isPremium (boolean), rating (number), reviews (number), projects (number), experience (string), image (string), about (string), tags (array of strings).

**GET /api/professionals/me/dashboard** \* **Response:** id (string), status (string), stats object (inquiries, profileViews, totalProjects), recentInquiries array of objects (id, senderName, senderEmail, subject, message, date, isRead).

**POST /api/professionals/inquiry** \* **Request:** professionalId (string), senderName (string), senderEmail (string), message (string), listingId (string, optional). \* **Response:** success (boolean), inquiryId (string).

**PATCH /api/professionals/me/expertise** \* **Request:** expertise (array of strings). \* **Response:** success (boolean), expertise (array of strings).

4\. Reviews APIs
----------------

Method

Path

Description

GET

/api/professionals/:id/reviews

List reviews

POST

/api/professionals/:id/reviews

Create review

**Request/response details:**

**GET /api/professionals/:id/reviews** \[Public\] \* **Response:** data (array of Review objects: id, professionalId, authorId, authorName, rating, title, content, createdAt).

**POST /api/professionals/:id/reviews** \* **Request:** rating (number 1-5), title (string), content (string). \* **Response:** success (boolean), data (Review object).

5\. Subscriptions / Payments APIs
---------------------------------

Method

Path

Description

GET

/api/subscriptions/plans

List plans

GET

/api/subscriptions/my

Get user’s active plan

POST

/api/payment/payment-sheet

Generate Stripe PaymentIntent

POST

/api/payment/verify-and-fulfill

Confirm payment success

POST

/api/subscriptions/activate-land

Activate listing plan

POST

/api/professionals/subscribe

Activate professional plan

POST

/api/subscriptions/change

Upgrade/Downgrade plan

POST

/api/subscriptions/cancel

Cancel subscription

GET

/api/transactions

List user transactions

GET

/api/transactions/:id

Get specific receipt

**Request/response details:**

**GET /api/subscriptions/plans** \[Public\] \* **Query params:** type (market\_profession | land\_listing). \* **Response:** data array of objects (id, name, description, monthlyPrice, annualPrice, currency, features, isPopular).

**POST /api/payment/payment-sheet** \* **Request:** amount (number), currency (string, ISO 4217). \* **Response:** paymentIntent (string), ephemeralKey (string), customer (string), publishableKey (string). _(Crucial for native Stripe SDK initialization)._

**POST /api/payment/verify-and-fulfill** \* **Request:** planId (string), productType (string), paymentIntentId (string). \* **Response:** success (boolean). _(In production, this should ideally be handled via Stripe Webhooks)._

**GET /api/transactions** \* **Response:** data array of objects (id, userId, amount, currency, status, description, provider, createdAt).

6\. Profile and Settings
------------------------

Method

Path

Description

GET

/api/user/profile

Get current user profile

PATCH

/api/user/profile

Update profile

DELETE

/api/profile

Delete account

GET

/api/user/stats

Get user aggregate metrics

GET

/api/users/me/notification-settings

Get preferences

PATCH

/api/users/me/notification-settings

Update preferences

**Request/response details:**

**PATCH /api/user/profile** \* **Request:** name (string), bio (string), phone (string), location (string), currency (string, ISO 4217). \* **Response:** data (User object).

**GET /api/user/stats** \* **Response:** data object containing activeListings (number) and totalViews (number).

**GET /api/users/me/notification-settings** \* **Response:** data object containing emailAlerts (boolean) and pushAlerts (boolean).

7\. Configuration & Locations
-----------------------------

Method

Path

Description

GET

/api/locations/countries

Dropdown countries

GET

/api/locations/states

Dropdown states

GET

/api/locations/cities

Dropdown cities

GET

/api/filter-options

Dynamic search filter UI config

GET

/api/notifications

User inbox alerts

PATCH

/api/notifications/:id/read

Mark alert read

DELETE

/api/notifications/clear

Empty inbox

GET

/api/ping

Health check

**Request/response details:**

**GET /api/locations/states** \[Public\] \* **Query params:** countryCode (string). \* **Response:** data array of objects (code, name).

**GET /api/filter-options** \[Public\] \* **Response:** data object containing propertyTypes, statuses, amenities arrays.