# Music App Backend - Complete Architecture v2.3

## 📋 Overview

Đây là base architecture hoàn chỉnh cho Music App Backend với Database Schema v2.3, được thiết kế theo chuẩn Spring Boot với đầy đủ tính năng submission workflow, premium content, subscription system, và transaction processing.

## 🚀 New Features in v2.3

### ✨ **Song Submission Workflow**
- Complete submission process từ creator đến admin approval
- Status tracking (PENDING → APPROVED/REJECTED)
- Review system với feedback và suggested changes
- Bulk operations cho admin

### 💎 **Premium Content System**
- Premium songs với individual pricing
- Access control based on purchase/subscription
- Revenue tracking và analytics
- Creator earnings management

### 🎯 **Subscription Management**
- Multi-tier subscription (Basic/Premium/VIP)
- Auto-renewal functionality
- Subscription analytics và revenue tracking
- Flexible pricing và duration

### 💳 **Transaction Processing**
- Complete wallet system
- Deposit/Withdrawal operations
- Premium song purchases
- Subscription payments
- Transaction retry mechanism

### ⚡ **Scheduled Tasks**
- Auto subscription renewals
- Expired subscription cleanup
- Failed transaction retry
- Analytics generation

## 🏗️ Architecture Components

### 1. **Enhanced Entity Layer** (`entity/`)
- ✅ `User` - Enhanced with balance và subscription relationships
- ✅ `Role` - Role-based access control (USER, CREATOR, ADMIN)  
- ✅ `Singer` - Ca sĩ management
- ✅ `Tag` - Thể loại nhạc
- ✅ `Song` - Enhanced với premium features và submission workflow
- ✅ `Playlist` - Playlist management
- ✅ `Like` - Polymorphic likes
- ✅ `Comment` - Polymorphic comments
- 🆕 `SongSubmission` - Complete submission workflow
- 🆕 `SubmissionSingers` - Many-to-many relationship
- 🆕 `SubmissionTags` - Many-to-many relationship  
- 🆕 `Transaction` - Complete transaction system
- 🆕 `UserSubscription` - Multi-tier subscription system

### 2. **Advanced Repository Layer** (`repository/`)
- ✅ Enhanced với complex queries cho premium content
- ✅ Subscription và transaction analytics
- ✅ Revenue calculations
- ✅ Advanced search và filtering
- 🆕 `SongSubmissionRepository` - Submission queries
- 🆕 `SubmissionSingersRepository` - Association queries
- 🆕 `SubmissionTagsRepository` - Association queries
- 🆕 `TransactionRepository` - Financial queries
- 🆕 `UserSubscriptionRepository` - Subscription queries

### 3. **Comprehensive Service Layer** (`service/`)
- ✅ Enhanced `SongService` với premium access control
- ✅ Existing services (SingerService, TagService, LikeService)
- 🆕 `SubmissionService` - Complete submission workflow
- 🆕 `SubscriptionService` - Subscription lifecycle management
- 🆕 `TransactionService` - Payment processing simulation
- 🆕 `SubscriptionSchedulerService` - Automated background tasks

### 4. **Complete Controller Layer** (`controller/`)
- ✅ Enhanced với role-based authorization
- ✅ Existing controllers (SingerController, TagController, SongController, LikeController)
- 🆕 `SubmissionController` - Submission management APIs
- 🆕 `SubscriptionController` - Subscription management APIs  
- 🆕 `TransactionController` - Transaction và wallet APIs

### 5. **Enhanced DTO Layer** (`dto/`)
- ✅ Enhanced `SongDto` với premium access fields
- ✅ Standardized response formats
- 🆕 Submission DTOs (SubmissionDto, CreateSubmissionRequest, ReviewSubmissionRequest)
- 🆕 Transaction DTOs (TransactionDto, CreateTransactionRequest)
- 🆕 Subscription DTOs (SubscriptionDto, CreateSubscriptionRequest)

### 6. **Enhanced Mapper Layer** (`mapper/`)
- ✅ Enhanced `SongMapper` với premium access control
- ✅ Performance optimized mappings
- 🆕 `SubmissionMapper` - Submission entity mapping
- 🆕 `TransactionMapper` - Transaction entity mapping
- 🆕 `SubscriptionMapper` - Subscription entity mapping

### 7. **Enhanced Exception Handling** (`exception/`)
- ✅ Comprehensive error handling
- 🆕 `InsufficientFundsException` - Payment errors
- 🆕 `SubmissionNotFoundException` - Submission errors
- 🆕 `SubscriptionNotFoundException` - Subscription errors
- 🆕 `TransactionNotFoundException` - Transaction errors
- ✅ Validation error handling

## 🔐 Security & Authorization

### **Role-Based Access Control:**

#### **PUBLIC (Guests):**
- ✅ View songs, singers, tags
- ✅ Listen to music
- ✅ View comments and like counts

#### **USER Role:**
- ✅ All public features
- ✅ Like/Unlike songs and playlists
- ✅ Create and manage personal playlists
- ✅ Comment on songs and playlists

#### **CREATOR Role:**
- ✅ All USER features  
- ✅ Create/upload songs (pending approval)
- ✅ Manage own songs
- ✅ View song statistics

#### **ADMIN Role:**
- ✅ All CREATOR features
- ✅ Approve/reject songs
- ✅ Manage singers and tags
- ✅ View all system data

## 📁 Project Structure

```
src/main/java/com/musicapp/backend/
├── entity/              # JPA Entities
├── repository/          # Data Access Layer  
├── service/            # Business Logic Layer
├── controller/         # REST API Controllers
├── dto/               # Data Transfer Objects
├── mapper/            # Entity-DTO Mappers
├── exception/         # Exception Handling
├── config/            # Configuration Classes
└── security/          # Security Components
```

## 🚀 Getting Started

### **1. Database Setup**
```sql
-- Tạo database
CREATE DATABASE music_app_db;

-- Thêm default roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_CREATOR'); 
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```

### **2. Environment Configuration**
File `application.properties` đã được cấu hình sẵn với:
- MySQL connection
- JWT settings
- Swagger documentation

### **3. Running the Application**
```bash
mvn spring-boot:run
```

## 📚 API Documentation & Examples

### **Base URL:** `http://localhost:8080/api/v1`

### **🔐 Authentication Examples:**

#### **1. User Registration:**
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "displayName": "Nguyễn Văn A",
  "email": "user@example.com", 
  "password": "123456",
  "confirmPassword": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

#### **2. User Login:**
```bash
POST /api/v1/auth/authenticate
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Success", 
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### **🎵 Tag Management Examples:**

#### **1. Get All Tags (Public):**
```bash
GET /api/v1/tags
# Hoặc search:
GET /api/v1/tags?search=pop
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Pop"
    },
    {
      "id": 2, 
      "name": "Rock"
    }
  ]
}
```

#### **2. Create Tag (Admin Only):**
```bash
POST /api/v1/tags?name=Ballad
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
  "success": true,
  "message": "Tag created successfully",
  "data": {
    "id": 3,
    "name": "Ballad"
  }
}
```

### **🎤 Singer Management Examples:**

#### **1. Get All Singers with Pagination:**
```bash
GET /api/v1/singers?page=0&size=10
# Hoặc search:
GET /api/v1/singers?page=0&size=10&search=sơn
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Sơn Tùng M-TP",
        "avatarPath": "/uploads/singers/sontunga.jpg",
        "songCount": 25
      }
    ],
    "pageInfo": {
      "page": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

#### **2. Create Singer (Admin Only):**
```bash
POST /api/v1/singers
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "name": "Đen Vâu",
  "avatarPath": "/uploads/singers/denvau.jpg"
}
```

### **🎶 Song Management Examples:**

#### **1. Get All Approved Songs:**
```bash
GET /api/v1/songs?page=0&size=20
# Hoặc search:
GET /api/v1/songs?page=0&size=20&search=nắng
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Nắng Ấm Xa Dần",
        "description": "Bài hát về tình yêu",
        "filePath": "/uploads/songs/nang-am-xa-dan.mp3",
        "thumbnailPath": "/uploads/thumbnails/nang-am-xa-dan.jpg",
        "listenCount": 1500,
        "status": "APPROVED",
        "createdAt": "2024-01-15T10:30:00",
        "creatorId": 2,
        "creatorName": "Producer ABC",
        "singers": [
          {
            "id": 1,
            "name": "Sơn Tùng M-TP",
            "avatarPath": "/uploads/singers/sontunga.jpg"
          }
        ],
        "tags": [
          {
            "id": 1,
            "name": "Pop"
          },
          {
            "id": 3,
            "name": "Ballad"
          }
        ],
        "likeCount": 320,
        "commentCount": 45,
        "isLikedByCurrentUser": true
      }
    ],
    "pageInfo": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

#### **2. Create Song (Creator/Admin):**
```bash
POST /api/v1/songs
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Bài Hát Mới",
  "description": "Mô tả bài hát",
  "filePath": "/uploads/songs/bai-hat-moi.mp3",
  "thumbnailPath": "/uploads/thumbnails/bai-hat-moi.jpg",
  "singerIds": [1, 2],
  "tagIds": [1, 3]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Song created successfully",
  "data": {
    "id": 2,
    "title": "Bài Hát Mới",
    "status": "PENDING",
    "createdAt": "2024-01-20T14:20:00",
    // ... other fields
  }
}
```

#### **3. Get Top Songs:**
```bash
GET /api/v1/songs/top?limit=5
```

#### **4. Approve Song (Admin Only):**
```bash
POST /api/v1/songs/2/approve
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### **❤️ Like System Examples:**

#### **1. Toggle Song Like:**
```bash
POST /api/v1/likes/songs/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response (Like):**
```json
{
  "success": true,
  "message": "Song liked successfully",
  "data": true
}
```

**Response (Unlike):**
```json
{
  "success": true,
  "message": "Song unliked successfully", 
  "data": false
}
```

#### **2. Get Like Count (Public):**
```bash
GET /api/v1/likes/songs/1/count
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": 320
}
```

#### **3. Check Like Status:**
```bash
GET /api/v1/likes/songs/1/status
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": true
}
```

## 🔧 Development Guidelines & Examples

### **🏗️ Adding New Features - Step by Step:**

#### **Example: Tạo Comment System**

#### **1. Entity First:**
```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    // Polymorphic fields
    @Column(name = "commentable_id", nullable = false)
    private Long commentableId;
    
    @Column(name = "commentable_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommentableType commentableType;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum CommentableType {
        SONG, PLAYLIST
    }
}
```

#### **2. Repository:**
```java
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments for a specific item
    Page<Comment> findByCommentableIdAndCommentableTypeOrderByCreatedAtDesc(
        Long commentableId, 
        Comment.CommentableType commentableType, 
        Pageable pageable
    );
    
    // Find user's comments
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Count comments for an item
    long countByCommentableIdAndCommentableType(
        Long commentableId, 
        Comment.CommentableType commentableType
    );
}
```

#### **3. DTOs:**
```java
// Request DTO with validation
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String content;
}

// Response DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    
    // User info
    private Long userId;
    private String userName;
    private String userAvatar;
    
    // Target info
    private Long commentableId;
    private String commentableType;
    
    // Permissions
    private Boolean canDelete; // true nếu là owner hoặc admin
}
```

#### **4. Mapper:**
```java
@Component
@RequiredArgsConstructor
public class CommentMapper {
    
    public CommentDto toDto(Comment comment, User currentUser) {
        if (comment == null) return null;
        
        boolean canDelete = currentUser != null && 
            (comment.getUser().getId().equals(currentUser.getId()) || 
             hasAdminRole(currentUser));
        
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getDisplayName())
                .userAvatar(comment.getUser().getAvatarPath())
                .commentableId(comment.getCommentableId())
                .commentableType(comment.getCommentableType().name())
                .canDelete(canDelete)
                .build();
    }
    
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}
```

#### **5. Service:**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final CommentMapper commentMapper;
    
    public Page<CommentDto> getSongComments(Long songId, Pageable pageable, User currentUser) {
        // Verify song exists and is approved
        if (!songRepository.findByIdAndStatus(songId, Song.SongStatus.APPROVED).isPresent()) {
            throw new ResourceNotFoundException("Song not found or not approved");
        }
        
        return commentRepository
                .findByCommentableIdAndCommentableTypeOrderByCreatedAtDesc(
                    songId, Comment.CommentableType.SONG, pageable)
                .map(comment -> commentMapper.toDto(comment, currentUser));
    }
    
    @Transactional
    public CommentDto createSongComment(Long songId, CreateCommentRequest request, User user) {
        // Verify song exists and is approved
        Song song = songRepository.findByIdAndStatus(songId, Song.SongStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found or not approved"));
        
        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .commentableId(songId)
                .commentableType(Comment.CommentableType.SONG)
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment, user);
    }
    
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // Check permission
        if (!comment.getUser().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
    }
    
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}
```

#### **6. Controller:**
```java
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @GetMapping("/songs/{songId}")
    public ResponseEntity<BaseResponse<PagedResponse<CommentDto>>> getSongComments(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDto> comments = commentService.getSongComments(songId, pageable, currentUser);
        PagedResponse<CommentDto> response = PagedResponse.of(comments.getContent(), comments);
        
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @PostMapping("/songs/{songId}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<CommentDto>> createSongComment(
            @PathVariable Long songId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User user) {
        
        CommentDto comment = commentService.createSongComment(songId, request, user);
        return ResponseEntity.ok(BaseResponse.success("Comment created successfully", comment));
    }
    
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {
        
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok(BaseResponse.success("Comment deleted successfully", null));
    }
}
```

#### **7. Update Security Configuration:**
```java
private static final String[] WHITE_LIST_URL = {
    // ... existing URLs
    "/api/v1/comments/songs/{id}",  // Public comment viewing
    // ... other URLs
};
```

### **🧪 Testing Examples:**

#### **1. Test APIs với Postman/cURL:**
```bash
# 1. Get song comments (public)
GET http://localhost:8080/api/v1/comments/songs/1?page=0&size=10

# 2. Create comment (authenticated)
POST http://localhost:8080/api/v1/comments/songs/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "content": "Bài hát hay quá!"
}

# 3. Delete comment (owner or admin)
DELETE http://localhost:8080/api/v1/comments/15
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### **📱 Frontend Integration Examples:**

#### **1. React/Vue.js API calls:**
```javascript
// Get song comments
const getSongComments = async (songId, page = 0) => {
  const response = await fetch(`/api/v1/comments/songs/${songId}?page=${page}&size=10`);
  const data = await response.json();
  return data.data; // PagedResponse<CommentDto>
};

// Create comment (với authentication)
const createComment = async (songId, content, token) => {
  const response = await fetch(`/api/v1/comments/songs/${songId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ content })
  });
  return await response.json();
};

// Like song
const toggleSongLike = async (songId, token) => {
  const response = await fetch(`/api/v1/likes/songs/${songId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return await response.json();
};
```

### **🔄 Database Migration Example:**
```sql
-- Insert default data
INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_CREATOR'), 
    ('ROLE_ADMIN');

INSERT INTO tags (name) VALUES
    ('Pop'),
    ('Rock'),
    ('Ballad'),
    ('R&B'),
    ('Hip Hop');

INSERT INTO singers (name, avatar_path) VALUES
    ('Sơn Tùng M-TP', '/uploads/singers/sontung.jpg'),
    ('Đen Vâu', '/uploads/singers/denvau.jpg'),
    ('Hòa Minzy', '/uploads/singers/hoaminzy.jpg');
```

### **💡 Common Patterns & Best Practices:**

#### **1. Standardized API Response:**
```java
// ✅ GOOD - Always use BaseResponse
@GetMapping("/{id}")
public ResponseEntity<BaseResponse<SongDto>> getSong(@PathVariable Long id) {
    SongDto song = songService.getSongById(id);
    return ResponseEntity.ok(BaseResponse.success(song));
}

// ✅ GOOD - With custom message
@PostMapping
public ResponseEntity<BaseResponse<SongDto>> createSong(@RequestBody CreateSongRequest request) {
    SongDto song = songService.createSong(request);
    return ResponseEntity.ok(BaseResponse.success("Song created successfully", song));
}

// ❌ BAD - Inconsistent response
@GetMapping("/{id}")
public SongDto getSong(@PathVariable Long id) {
    return songService.getSongById(id); // Không consistent!
}
```

#### **2. Pagination Pattern:**
```java
// ✅ GOOD - Standardized pagination
@GetMapping
public ResponseEntity<BaseResponse<PagedResponse<SongDto>>> getAllSongs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<SongDto> songs = songService.getAllSongs(pageable);
    PagedResponse<SongDto> response = PagedResponse.of(songs.getContent(), songs);
    
    return ResponseEntity.ok(BaseResponse.success(response));
}
```

#### **3. Search Pattern:**
```java
// ✅ GOOD - Flexible search
@GetMapping
public ResponseEntity<BaseResponse<List<TagDto>>> getAllTags(
        @RequestParam(required = false) String search) {
    
    List<TagDto> tags;
    if (search != null && !search.trim().isEmpty()) {
        tags = tagService.searchTags(search.trim());
    } else {
        tags = tagService.getAllTags();
    }
    
    return ResponseEntity.ok(BaseResponse.success(tags));
}
```

#### **4. Authorization Pattern:**
```java
// ✅ GOOD - Clear role-based access
@PostMapping
@PreAuthorize("hasRole('ADMIN')")  // Admin only
public ResponseEntity<BaseResponse<TagDto>> createTag(@RequestParam String name) {
    TagDto tag = tagService.createTag(name);
    return ResponseEntity.ok(BaseResponse.success("Tag created successfully", tag));
}

@PostMapping("/songs")
@PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")  // Creator or Admin
public ResponseEntity<BaseResponse<SongDto>> createSong(@RequestBody CreateSongRequest request) {
    // Implementation
}

@PostMapping("/likes/songs/{songId}")
@PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")  // Any authenticated user
public ResponseEntity<BaseResponse<Boolean>> toggleSongLike(@PathVariable Long songId) {
    // Implementation
}
```

#### **5. Validation Pattern:**
```java
// ✅ GOOD - Comprehensive validation
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSongRequest {
    
    @NotBlank(message = "Song title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "File path is required")
    private String filePath;
    
    @NotNull(message = "At least one singer is required")
    @NotEmpty(message = "Singer list cannot be empty")
    private List<Long> singerIds;
    
    private List<Long> tagIds; // Optional
}
```

#### **6. Exception Handling Pattern:**
```java
// ✅ GOOD - Descriptive error messages
@Service
public class SongService {
    
    public SongDto getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Song not found with id: " + id));
        return songMapper.toDto(song);
    }
    
    public void deleteSong(Long id, User user) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Song not found with id: " + id));
        
        if (!song.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException(
                "You don't have permission to delete this song");
        }
        
        songRepository.delete(song);
    }
}
```

#### **7. Mapper Optimization Pattern:**
```java
// ✅ GOOD - Performance optimized mapping
@Component
@RequiredArgsConstructor
public class SongMapper {
    
    // Full mapping với relationships (expensive)
    public SongDto toDto(Song song, User currentUser) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                // ... other basic fields
                .singers(song.getSingers() != null ? 
                    song.getSingers().stream()
                        .map(singerMapper::toDtoWithoutSongCount) // Avoid N+1
                        .collect(Collectors.toList()) : null)
                .likeCount(likeRepository.countByLikeableIdAndLikeableType(...))
                .isLikedByCurrentUser(currentUser != null ? 
                    likeRepository.existsByUserIdAndLikeableIdAndLikeableType(...) : false)
                .build();
    }
    
    // Basic mapping cho listing (fast)
    public SongDto toDtoBasic(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .thumbnailPath(song.getThumbnailPath())
                .listenCount(song.getListenCount())
                // Không load relationships
                .build();
    }
}
```

### **🚨 Common Mistakes to Avoid:**

#### **❌ 1. Exposing Entities Directly:**
```java
// BAD
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).get(); // Exposes password, etc.
}

// GOOD
@GetMapping("/{id}")
public ResponseEntity<BaseResponse<UserProfileDto>> getUser(@PathVariable Long id) {
    UserProfileDto user = userService.getUserProfile(id);
    return ResponseEntity.ok(BaseResponse.success(user));
}
```

#### **❌ 2. Missing Validation:**
```java
// BAD
@PostMapping
public ResponseEntity<Song> createSong(@RequestBody Song song) {
    return ResponseEntity.ok(songRepository.save(song)); // No validation!
}

// GOOD
@PostMapping
public ResponseEntity<BaseResponse<SongDto>> createSong(
        @Valid @RequestBody CreateSongRequest request,
        @AuthenticationPrincipal User creator) {
    SongDto song = songService.createSong(request, creator);
    return ResponseEntity.ok(BaseResponse.success("Song created successfully", song));
}
```

#### **❌ 3. Inconsistent Error Handling:**
```java
// BAD
@GetMapping("/{id}")
public SongDto getSong(@PathVariable Long id) {
    Optional<Song> song = songRepository.findById(id);
    if (!song.isPresent()) {
        throw new RuntimeException("Not found"); // Generic error!
    }
    return songMapper.toDto(song.get());
}

// GOOD
@GetMapping("/{id}")
public ResponseEntity<BaseResponse<SongDto>> getSong(@PathVariable Long id) {
    SongDto song = songService.getSongById(id); // Service handles exceptions
    return ResponseEntity.ok(BaseResponse.success(song));
}
```

### **📊 Performance Tips:**

#### **1. Use Pagination for Large Datasets:**
```java
// ✅ Always paginate large results
Page<Song> songs = songRepository.findAll(pageable);

// ❌ Don't load all data at once
List<Song> allSongs = songRepository.findAll(); // Memory issues!
```

#### **2. Optimize Database Queries:**
```java
// ✅ Use specific queries
@Query("SELECT s FROM Song s WHERE s.status = 'APPROVED' ORDER BY s.listenCount DESC")
List<Song> findTopSongs(Pageable pageable);

// ❌ Avoid N+1 queries
// Load songs and then query singers for each song separately
```

#### **3. Use DTOs for Different Use Cases:**
```java
// ✅ Light DTO for listing
public class SongListDto {
    private Long id;
    private String title;
    private String thumbnailPath;
}

// ✅ Full DTO for details
public class SongDetailDto extends SongListDto {
    private String description;
    private List<SingerDto> singers;
    private List<TagDto> tags;
    // ... more fields
}
```

## 🎯 Next Steps for Development

### **Priority 1: Playlist Management** 
```java
// TODO: Implement these classes
PlaylistService.java
PlaylistController.java
PlaylistMapper.java

// Key features to implement:
- CRUD operations for playlists
- Add/remove songs from playlist
- Public vs private playlists
- User's favorite playlists
```

**Example APIs to implement:**
```bash
GET /api/v1/playlists                    # Public playlists
GET /api/v1/playlists/my                 # User's playlists  
POST /api/v1/playlists                   # Create playlist
PUT /api/v1/playlists/{id}               # Update playlist
POST /api/v1/playlists/{id}/songs/{songId}  # Add song to playlist
DELETE /api/v1/playlists/{id}/songs/{songId} # Remove song from playlist
```

### **Priority 2: Comment System**
```java
// TODO: Complete comment implementation
CommentService.java
CommentController.java  
CommentDto.java
CreateCommentRequest.java

// Key features:
- Comment on songs and playlists
- Delete own comments
- Admin can delete any comment
- Pagination for comments
```

**Example APIs to implement:**
```bash
GET /api/v1/comments/songs/{songId}         # Get song comments
POST /api/v1/comments/songs/{songId}        # Create song comment  
GET /api/v1/comments/playlists/{playlistId} # Get playlist comments
DELETE /api/v1/comments/{commentId}         # Delete comment
```

### **Priority 3: User Profile Management**
```java
// TODO: Extend user management
UserService.java
UserController.java
UpdateProfileRequest.java
ChangePasswordRequest.java

// Key features:
- Update user profile
- Change password  
- Upload avatar
- View user statistics
```

**Example APIs to implement:**
```bash
GET /api/v1/users/profile                   # Get current user profile
PUT /api/v1/users/profile                   # Update profile
POST /api/v1/users/change-password          # Change password
POST /api/v1/users/upload-avatar            # Upload avatar
```

### **Priority 4: File Upload Service**
```java
// TODO: Implement file management
FileUploadService.java
FileController.java

// Key features:
- Upload audio files (MP3)
- Upload images (avatars, thumbnails)
- File validation and security
- Storage management
```

**Example APIs to implement:**
```bash
POST /api/v1/files/upload/audio            # Upload MP3 file
POST /api/v1/files/upload/image            # Upload image file
GET /api/v1/files/{filename}               # Serve static files
DELETE /api/v1/files/{filename}            # Delete file
```

### **Priority 5: Admin Dashboard APIs**
```java
// TODO: Implement admin features
AdminService.java
AdminController.java
UserManagementDto.java

// Key features:
- User management
- Content moderation
- System statistics
- Role management
```

**Example APIs to implement:**
```bash
GET /api/v1/admin/users                    # List all users
PUT /api/v1/admin/users/{id}/role          # Change user role
GET /api/v1/admin/stats                    # System statistics
GET /api/v1/admin/reports                  # Content reports
```

## 🛠️ Development Workflow

### **📋 Task Assignment Example:**

#### **Task 1: Playlist Management**
**Assigned to:** Developer A  
**Estimate:** 3-4 days  
**Files to create/modify:**
- `PlaylistService.java`
- `PlaylistController.java` 
- `PlaylistMapper.java`
- `CreatePlaylistRequest.java`
- `UpdatePlaylistRequest.java`

**Acceptance Criteria:**
- [ ] User can create personal playlists
- [ ] User can add/remove songs from playlists
- [ ] Admin can create public playlists
- [ ] Pagination support for playlist listing
- [ ] Like/unlike playlists functionality

#### **Task 2: Comment System**  
**Assigned to:** Developer B  
**Estimate:** 2-3 days  
**Files to create/modify:**
- `CommentService.java`
- `CommentController.java`
- `CommentDto.java`
- `CreateCommentRequest.java`

**Acceptance Criteria:**
- [ ] Users can comment on songs and playlists
- [ ] Users can delete their own comments
- [ ] Admin can delete any comment
- [ ] Comments display with pagination
- [ ] Real-time comment count updates

### **🧪 Testing Checklist:**

#### **Before Submitting Code:**
- [ ] All endpoints tested with Postman
- [ ] Validation works correctly
- [ ] Error handling returns proper responses
- [ ] Authorization rules enforced
- [ ] No SQL injection vulnerabilities
- [ ] Performance tested with large datasets

#### **Testing Examples:**
```bash
# Test authentication
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"123456"}'

# Test authorization (should fail without token)
curl -X POST http://localhost:8080/api/v1/songs \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Song"}'

# Test validation (should fail with invalid data)
curl -X POST http://localhost:8080/api/v1/tags \
  -H "Authorization: Bearer <token>" \
  -d 'name='

# Test pagination
curl "http://localhost:8080/api/v1/songs?page=0&size=5"
```

## 📞 Support & Documentation

### **🆘 When You Need Help:**
1. **Check README examples** - Most patterns are documented
2. **Look at existing code** - Follow the same patterns  
3. **Use BaseResponse** - Always wrap responses consistently
4. **Follow naming conventions** - Keep consistency across the codebase

### **📝 Code Review Checklist:**
- [ ] Follows established patterns
- [ ] Uses BaseResponse wrapper
- [ ] Has proper validation
- [ ] Includes error handling
- [ ] Has appropriate authorization
- [ ] Performance optimized
- [ ] Well documented

### **🎯 Success Metrics:**
- **Code Consistency:** 95%+ following established patterns
- **API Response Time:** < 200ms for most endpoints
- **Security:** Zero exposed sensitive data
- **Error Coverage:** All error cases handled gracefully

**Base architecture này cung cấp foundation hoàn chỉnh để team có thể phát triển nhanh chóng và maintain dễ dàng!** 🚀
