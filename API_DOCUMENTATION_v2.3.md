# Music App Backend API Documentation v2.3

## Overview
This documentation covers the new APIs introduced in Database Schema v2.3, including song submission workflow, premium content management, subscription system, and transaction processing.

## Base URL
```
http://localhost:8080/api
```

## Authentication
All authenticated endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Submission APIs

### Create Song Submission
**POST** `/submissions`
- **Role Required**: CREATOR, ADMIN
- **Description**: Submit a new song for review

**Request Body**:
```json
{
  "title": "My New Song",
  "description": "A beautiful melody",
  "filePath": "/audio/my-song.mp3",
  "thumbnailPath": "/images/my-song-thumb.jpg",
  "singerIds": [1, 2],
  "tagIds": [1, 3, 5],
  "isPremium": true,
  "premiumPrice": 2.99
}
```

**Response**:
```json
{
  "success": true,
  "message": "Submission created successfully",
  "data": {
    "id": 1,
    "title": "My New Song",
    "description": "A beautiful melody",
    "status": "PENDING",
    "isPremium": true,
    "premiumPrice": 2.99,
    "submittedAt": "2025-08-06T10:30:00",
    "creatorId": 123,
    "creatorName": "John Creator",
    "singers": [...],
    "tags": [...]
  }
}
```

### Get My Submissions
**GET** `/submissions/my?page=0&size=10&status=PENDING`
- **Role Required**: CREATOR, ADMIN
- **Description**: Get current user's submissions with optional status filter

### Review Submission (Admin)
**POST** `/submissions/{id}/review`
- **Role Required**: ADMIN
- **Description**: Review a submission with feedback

**Request Body**:
```json
{
  "status": "APPROVED",
  "reviewNotes": "Great song, approved for publication",
  "suggestedChanges": []
}
```

### Get Pending Submissions (Admin)
**GET** `/submissions/pending?page=0&size=10`
- **Role Required**: ADMIN
- **Description**: Get all pending submissions for review

---

## Subscription APIs

### Create Subscription
**POST** `/subscriptions`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Subscribe to a premium plan

**Request Body**:
```json
{
  "subscriptionType": "PREMIUM",
  "durationMonths": 1,
  "autoRenewal": true
}
```

**Response**:
```json
{
  "success": true,
  "message": "Subscription created successfully",
  "data": {
    "id": 1,
    "subscriptionType": "PREMIUM",
    "startDate": "2025-08-06T10:30:00",
    "endDate": "2025-09-06T10:30:00",
    "price": 9.99,
    "status": "ACTIVE",
    "autoRenewal": true
  }
}
```

### Get My Subscription
**GET** `/subscriptions/my`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Get current user's active subscription

### Cancel Subscription
**POST** `/subscriptions/cancel`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Cancel current subscription

### Check Premium Access
**GET** `/subscriptions/check-access?requiredTier=PREMIUM`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Check if user has access to premium content

### Get Subscription Tiers
**GET** `/subscriptions/tiers`
- **Description**: Get available subscription tiers (public endpoint)

**Response**:
```json
{
  "success": true,
  "data": {
    "BASIC": {
      "name": "Basic",
      "price": 0.00,
      "features": ["Free songs", "Basic audio quality"]
    },
    "PREMIUM": {
      "name": "Premium",
      "price": 9.99,
      "features": ["Premium songs", "High audio quality", "No ads"]
    },
    "VIP": {
      "name": "VIP",
      "price": 19.99,
      "features": ["All premium features", "Exclusive content", "Early access"]
    }
  }
}
```

---

## Transaction APIs

### Deposit Money
**POST** `/transactions/deposit?amount=50.00&paymentMethod=credit_card`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Add money to user's wallet

### Withdraw Money
**POST** `/transactions/withdraw?amount=25.00&withdrawalMethod=bank_transfer`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Withdraw money from user's wallet

### Purchase Premium Song
**POST** `/transactions/purchase-song/{songId}`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Purchase access to a premium song

**Response**:
```json
{
  "success": true,
  "message": "Song purchased successfully",
  "data": {
    "id": 1,
    "transactionCode": "TXN_ABC123DEF456",
    "amount": 2.99,
    "type": "PREMIUM_SONG_PURCHASE",
    "status": "COMPLETED",
    "description": "Purchase premium song: My New Song",
    "createdAt": "2025-08-06T10:30:00",
    "completedAt": "2025-08-06T10:30:01"
  }
}
```

### Purchase Subscription
**POST** `/transactions/purchase-subscription`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Purchase a subscription plan

**Request Body**:
```json
{
  "subscriptionId": 1,
  "amount": 9.99,
  "description": "Premium subscription payment"
}
```

### Get My Transactions
**GET** `/transactions/my?page=0&size=10&type=PREMIUM_SONG_PURCHASE`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Get current user's transaction history

### Get Balance
**GET** `/transactions/balance`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Get current user's wallet balance

**Response**:
```json
{
  "success": true,
  "message": "Balance retrieved successfully",
  "data": 123.45
}
```

### Get Spending Summary
**GET** `/transactions/spending-summary?startDate=2025-07-01&endDate=2025-08-01`
- **Role Required**: USER, CREATOR, ADMIN
- **Description**: Get spending summary for a date range

---

## Enhanced Song APIs

### Get Songs with Access Control
**GET** `/songs?page=0&size=10`
- **Description**: Get all songs with premium access information (public endpoint)

**Response**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Premium Song",
      "isPremium": true,
      "premiumPrice": 2.99,
      "canAccess": false,
      "isPurchased": false,
      "singers": [...],
      "tags": [...]
    }
  ],
  "pageable": {...},
  "totalElements": 100
}
```

### Get Premium Songs Only
**GET** `/songs/premium?page=0&size=10`
- **Description**: Get only premium songs

### Get Free Songs Only
**GET** `/songs/free?page=0&size=10`
- **Description**: Get only free songs

### Get Song with Access Check
**GET** `/songs/{id}`
- **Description**: Get song details with access control information

---

## Admin APIs

### Submission Management
- **GET** `/submissions/admin/all` - Get all submissions with search and filtering
- **POST** `/submissions/{id}/approve` - Approve a submission
- **POST** `/submissions/{id}/reject?reason=Quality issues` - Reject a submission
- **GET** `/submissions/stats/admin` - Get submission statistics

### Subscription Management
- **GET** `/subscriptions/admin/all` - Get all subscriptions
- **GET** `/subscriptions/admin/user/{userId}` - Get user's subscription history
- **GET** `/subscriptions/admin/stats` - Get subscription statistics
- **GET** `/subscriptions/admin/revenue` - Get subscription revenue report
- **POST** `/subscriptions/admin/manual-renewal` - Trigger manual renewal process

### Transaction Management
- **GET** `/transactions/admin/all` - Get all transactions
- **GET** `/transactions/admin/user/{userId}` - Get user's transaction history
- **GET** `/transactions/admin/stats` - Get transaction statistics
- **GET** `/transactions/admin/revenue` - Get revenue reports
- **POST** `/transactions/admin/refund/{transactionId}?reason=Duplicate charge` - Refund a transaction
- **GET** `/transactions/admin/pending-withdrawals` - Get pending withdrawals
- **POST** `/transactions/admin/approve-withdrawal/{transactionId}` - Approve a withdrawal

---

## Error Responses

### Common Error Codes
- **400**: Bad Request - Invalid input or business logic error
- **401**: Unauthorized - Missing or invalid authentication
- **403**: Forbidden - Insufficient permissions
- **404**: Not Found - Resource doesn't exist
- **409**: Conflict - Resource already exists
- **500**: Internal Server Error

### Error Response Format
```json
{
  "success": false,
  "message": "Insufficient funds for withdrawal",
  "timestamp": "2025-08-06T10:30:00"
}
```

### Business Logic Errors
- **InsufficientFundsException**: User doesn't have enough balance
- **SubmissionNotFoundException**: Submission not found
- **SubscriptionNotFoundException**: Subscription not found
- **TransactionNotFoundException**: Transaction not found

---

## Scheduled Tasks

The system includes automated background tasks:

### Daily Tasks (2:00 AM)
- **Subscription Renewals**: Process auto-renewal for expiring subscriptions
- **Subscription Expiry Warnings**: Send notifications 3 days before expiry

### Hourly Tasks
- **Expired Subscription Updates**: Mark expired subscriptions as inactive

### Every 6 Hours
- **Failed Transaction Retry**: Retry failed transactions from last 24 hours

### Weekly Tasks (Sunday 3:00 AM)
- **Transaction Cleanup**: Archive old failed transactions (12+ months old)

---

## Premium Content Access Rules

### Guest Users
- Can access only free songs
- Cannot purchase premium content
- No subscription access

### Authenticated Users
- Can access free songs
- Can purchase individual premium songs
- Can subscribe to premium plans
- Premium access via subscription or individual purchase

### Subscription Tiers
1. **Basic** ($0/month): Free songs only
2. **Premium** ($9.99/month): Access to premium songs, high quality audio
3. **VIP** ($19.99/month): All premium features + exclusive content

### Access Priority
1. Individual song purchase (permanent access)
2. Active subscription (duration-based access)
3. Free song access (always available)

---

## Rate Limiting

### Public Endpoints
- 100 requests per minute per IP

### Authenticated Endpoints
- 1000 requests per minute per user

### Admin Endpoints
- 2000 requests per minute per admin user

---

## Pagination

All list endpoints support pagination with these parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 10, max: 100)

Response includes pagination metadata:
```json
{
  "content": [...],
  "pageable": {
    "page": 0,
    "size": 10
  },
  "totalElements": 150,
  "totalPages": 15,
  "first": true,
  "last": false
}
```

---

## WebSocket Events (Future Enhancement)

Planned real-time notifications:
- Submission status updates
- Subscription expiry warnings
- Transaction completions
- New premium song releases
