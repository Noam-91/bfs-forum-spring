package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.*;
import com.bfsforum.postservice.dto.UserInfoReply;
import com.bfsforum.postservice.exception.NotAuthorizedException;
import com.bfsforum.postservice.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
  @Mock
  private PostRepository postRepository;

  @Mock
  private StreamBridge streamBridge;

  @Mock
  private RequestReplyManager<UserInfoReply> requestReplyManager;

  @InjectMocks
  private PostService postService;

  private final String USER_ID = "testUser123";
  private final String ADMIN_ID = "adminUser456";
  private final String SUPER_ADMIN_ID = "superAdmin789";
  private final String ANOTHER_USER_ID = "anotherUser456";
  private final String UNVERIFIED_USER_ID = "unverifiedUser";

  private final String USER_ROLE = Role.USER.name();
  private final String ADMIN_ROLE = Role.ADMIN.name();
  private final String SUPER_ADMIN_ROLE = Role.SUPER_ADMIN.name();
  private final String UNVERIFIED_ROLE = Role.UNVERIFIED.name();

  private Post samplePublishedPost;
  private Post sampleUnpublishedPost;
  private Post sampleBannedPost;
  private Post sampleArchivedPost;
  private Post sampleDeletedPost;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    // Inject @Value fields manually for testing
    Field postViewBindingNameField = PostService.class.getDeclaredField("postViewBindingName");
    postViewBindingNameField.setAccessible(true);
    ReflectionUtils.setField(postViewBindingNameField, postService, "post-view-notification-topic");

    Field userInfoRequestBindingNameField = PostService.class.getDeclaredField("userInfoRequestBindingName");
    userInfoRequestBindingNameField.setAccessible(true);
    ReflectionUtils.setField(userInfoRequestBindingNameField, postService, "user-info-request-topic");

    samplePublishedPost = Post.builder()
        .id("post1")
        .title("Published Post")
        .content("This is a published post content.")
        .userId(USER_ID)
        .firstName("Test")
        .lastName("User")
        .status(PostStatus.PUBLISHED.name())
        .viewCount(0)
        .build();

    sampleUnpublishedPost = Post.builder()
        .id("post2")
        .title("Unpublished Post")
        .content("This is an unpublished post content.")
        .userId(USER_ID) // Owner is USER_ID
        .status(PostStatus.UNPUBLISHED.name())
        .viewCount(0)
        .build();

    sampleBannedPost = Post.builder()
        .id("post3")
        .title("Banned Post")
        .content("This content is banned.")
        .userId(ANOTHER_USER_ID)
        .status(PostStatus.BANNED.name())
        .viewCount(0)
        .build();

    sampleArchivedPost = Post.builder()
        .id("post4")
        .title("Archived Post")
        .content("This content is archived.")
        .userId(USER_ID)
        .status(PostStatus.ARCHIVED.name())
        .viewCount(0)
        .build();

    sampleDeletedPost = Post.builder()
        .id("post5")
        .title("Deleted Post")
        .content("This content is deleted.")
        .userId(USER_ID)
        .status(PostStatus.DELETED.name())
        .viewCount(0)
        .build();
  }

  // --- getPostById tests ---
  @Test
  @DisplayName("getPostById: Should return published post and increment view count for any user")
  void getPostById_publishedPost_shouldReturnAndIncrementViewCount() {
    when(postRepository.findById(samplePublishedPost.getId())).thenReturn(Optional.of(samplePublishedPost));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      // Ensure view count was incremented before saving
      assertEquals(1, savedPost.getViewCount());
      return savedPost;
    });
    // Use ArgumentCaptor to capture the message sent to streamBridge
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    when(streamBridge.send(eq("post-view-notification-topic"), messageCaptor.capture())).thenReturn(true);

    Post result = postService.getPostById(samplePublishedPost.getId(), ANOTHER_USER_ID);

    assertNotNull(result);
    assertEquals(samplePublishedPost.getId(), result.getId());
    assertEquals(1, result.getViewCount()); // Verify view count increment
    verify(postRepository, times(1)).findById(samplePublishedPost.getId());
    verify(postRepository, times(1)).save(samplePublishedPost);
    verify(streamBridge, times(1)).send(eq("post-view-notification-topic"), any(Message.class));

    // Verify content of the captured message
    Message<Post> capturedMessage = messageCaptor.getValue();
    assertEquals(samplePublishedPost.getId(), capturedMessage.getPayload().getId());
    assertNotNull(capturedMessage.getHeaders().get(KafkaHeaders.CORRELATION_ID));
  }

  @Test
  @DisplayName("getPostById: Should return unpublished post for its owner")
  void getPostById_unpublishedPostAsOwner_shouldReturn() {
    when(postRepository.findById(sampleUnpublishedPost.getId())).thenReturn(Optional.of(sampleUnpublishedPost));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved post
    when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

    Post result = postService.getPostById(sampleUnpublishedPost.getId(), USER_ID); // USER_ID is the owner

    assertNotNull(result);
    assertEquals(sampleUnpublishedPost.getId(), result.getId());
    assertEquals(1, result.getViewCount()); // View count still increments
    verify(postRepository, times(1)).findById(sampleUnpublishedPost.getId());
    verify(postRepository, times(1)).save(sampleUnpublishedPost);
    verify(streamBridge, times(1)).send(eq("post-view-notification-topic"), any(Message.class));
  }

  @Test
  @DisplayName("getPostById: Should throw NotFoundException for non-existent post")
  void getPostById_nonExistentPost_shouldThrowNotFoundException() {
    String nonExistentId = "nonExistentId";
    when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
        postService.getPostById(nonExistentId, USER_ID));

    assertEquals("Post not found", exception.getMessage());
    verify(postRepository, times(1)).findById(nonExistentId);
    verify(postRepository, never()).save(any(Post.class));
    verify(streamBridge, never()).send(anyString(), any(Message.class));
  }

  @Test
  @DisplayName("getPostById: Should throw NotAuthorizedException for unpublished post by non-owner")
  void getPostById_unpublishedPostAsNonOwner_shouldThrowNotAuthorizedException() {
    when(postRepository.findById(sampleUnpublishedPost.getId())).thenReturn(Optional.of(sampleUnpublishedPost));

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.getPostById(sampleUnpublishedPost.getId(), ANOTHER_USER_ID)); // Non-owner userId

    assertEquals("Only verified users can view unpublished posts", exception.getMessage());
    verify(postRepository, times(1)).findById(sampleUnpublishedPost.getId());
    verify(postRepository, never()).save(any(Post.class));
    verify(streamBridge, never()).send(anyString(), any(Message.class));
  }

  // --- getBatchPostsById tests ---
  @Test
  @DisplayName("getBatchPostsById: Should return a list of posts for valid IDs")
  void getBatchPostsById_validIds_shouldReturnPosts() {
    List<String> postIds = Arrays.asList(samplePublishedPost.getId(), sampleBannedPost.getId());
    List<Post> expectedPosts = Arrays.asList(samplePublishedPost, sampleBannedPost);
    when(postRepository.findAllById(postIds)).thenReturn(expectedPosts);

    List<Post> result = postService.getBatchPostsById(postIds);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.containsAll(expectedPosts));
    verify(postRepository, times(1)).findAllById(postIds);
  }

  @Test
  @DisplayName("getBatchPostsById: Should return an empty list for empty ID list")
  void getBatchPostsById_emptyIds_shouldReturnEmptyList() {
    List<String> postIds = Collections.emptyList();
    when(postRepository.findAllById(postIds)).thenReturn(Collections.emptyList());

    List<Post> result = postService.getBatchPostsById(postIds);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(postRepository, times(1)).findAllById(postIds);
  }

  @Test
  @DisplayName("getBatchPostsById: Should return only existing posts when some IDs don't exist")
  void getBatchPostsById_someNonExistentIds_shouldReturnExistingPosts() {
    List<String> postIds = Arrays.asList(samplePublishedPost.getId(), "nonExistentId", sampleBannedPost.getId());
    List<Post> expectedPosts = Arrays.asList(samplePublishedPost, sampleBannedPost);
    // Simulate repository returning only existing posts
    when(postRepository.findAllById(postIds)).thenReturn(expectedPosts);

    List<Post> result = postService.getBatchPostsById(postIds);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(samplePublishedPost));
    assertTrue(result.contains(sampleBannedPost));
    assertFalse(result.stream().anyMatch(p -> "nonExistentId".equals(p.getId())));
    verify(postRepository, times(1)).findAllById(postIds);
  }


  // --- createPost tests ---
  @Test
  @DisplayName("createPost: Should successfully create a post for a verified user")
  void createPost_verifiedUser_shouldCreatePost() {
    Post newPost = Post.builder().title("New Post").content("Content").build();
    UserInfoReply userInfo = UserInfoReply.builder()
    .userId(USER_ID).firstName("John").lastName("Doe").build();
    CompletableFuture<UserInfoReply> future = CompletableFuture.completedFuture(userInfo);

    when(requestReplyManager.createAndStoreFuture(anyString())).thenReturn(future);
    when(streamBridge.send(eq("user-info-request-topic"), any(Message.class))).thenReturn(true);
    when(requestReplyManager.awaitFuture(anyString(), eq(future))).thenReturn(userInfo);
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      // Simulate DB assigning an ID
      if (savedPost.getId() == null) savedPost.setId(UUID.randomUUID().toString());
      return savedPost;
    });

    Post createdPost = postService.createPost(newPost, USER_ID, USER_ROLE);

    assertNotNull(createdPost.getId());
    assertEquals(USER_ID, createdPost.getUserId());
    assertEquals("John", createdPost.getFirstName());
    assertEquals("Doe", createdPost.getLastName());
    assertNotNull(createdPost.getCreatedAt());
    assertNotNull(createdPost.getUpdatedAt());
    verify(requestReplyManager, times(1)).createAndStoreFuture(anyString());
    verify(streamBridge, times(1)).send(eq("user-info-request-topic"), any(Message.class));
    verify(requestReplyManager, times(1)).awaitFuture(anyString(), eq(future));
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  @DisplayName("createPost: Should throw NotAuthorizedException for unverified user")
  void createPost_unverifiedUser_shouldThrowNotAuthorizedException() {
    Post newPost = Post.builder().title("New Post").content("Content").build();

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.createPost(newPost, UNVERIFIED_USER_ID, UNVERIFIED_ROLE));

    assertEquals("Only verified users can create posts", exception.getMessage());
    verify(requestReplyManager, never()).createAndStoreFuture(anyString());
    verify(streamBridge, never()).send(anyString(), any(Message.class));
    verify(requestReplyManager, never()).awaitFuture(anyString(), any());
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("createPost: Should handle UserInfo request timeout")
  void createPost_userInfoTimeout_shouldThrowRuntimeException() {
    Post newPost = Post.builder().title("New Post").content("Content").build();
    CompletableFuture<UserInfoReply> future = new CompletableFuture<>(); // Future that will not complete

    when(requestReplyManager.createAndStoreFuture(anyString())).thenReturn(future);
    when(streamBridge.send(eq("user-info-request-topic"), any(Message.class))).thenReturn(true);
    // Simulate awaitFuture throwing a RuntimeException due to timeout
    when(requestReplyManager.awaitFuture(anyString(), eq(future)))
        .thenThrow(new RuntimeException("Future await failed/timed out: TimeoutException"));

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        postService.createPost(newPost, USER_ID, USER_ROLE));

    assertTrue(exception.getMessage().contains("Future await failed/timed out: TimeoutException"));
    verify(requestReplyManager, times(1)).createAndStoreFuture(anyString());
    verify(streamBridge, times(1)).send(eq("user-info-request-topic"), any(Message.class));
    verify(requestReplyManager, times(1)).awaitFuture(anyString(), eq(future));
    verify(postRepository, never()).save(any(Post.class));
  }

  // --- updatePost tests ---
  @Test
  @DisplayName("updatePost: Should successfully update post by owner")
  void updatePost_byOwner_shouldUpdatePost() {
    Post updatedPostDetails = Post.builder()
        .title("Updated Title")
        .content("Updated content here.")
        .build();
    Post existingPost = Post.builder()
        .id("post1")
        .title("Original Title")
        .content("Original content.")
        .userId(USER_ID)
        .status(PostStatus.PUBLISHED.name())
        .build();

    when(postRepository.findById("post1")).thenReturn(Optional.of(existingPost));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertEquals("Updated Title", savedPost.getTitle());
      assertEquals("Updated content here.", savedPost.getContent());
      assertNotNull(savedPost.getUpdatedAt()); // Verify timestamp updated
      return savedPost;
    });

    Post result = postService.updatePost("post1", updatedPostDetails, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated content here.", result.getContent());
    verify(postRepository, times(1)).findById("post1");
    verify(postRepository, times(1)).save(any(Post.class));
  }

  @Test
  @DisplayName("updatePost: Should throw NotAuthorizedException for unverified user")
  void updatePost_unverifiedUser_shouldThrowNotAuthorizedException() {
    Post updatedPostDetails = Post.builder().title("Updated Title").content("Content").build();
    Post existingPost = Post.builder().id("post1").userId(UNVERIFIED_USER_ID).status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("post1")).thenReturn(Optional.of(existingPost));

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.updatePost("post1", updatedPostDetails, UNVERIFIED_USER_ID, UNVERIFIED_ROLE));

    assertEquals("Only verified users can update posts", exception.getMessage());
    verify(postRepository, times(1)).findById("post1");
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("updatePost: Should throw NotAuthorizedException for non-owner")
  void updatePost_nonOwner_shouldThrowNotAuthorizedException() {
    Post updatedPostDetails = Post.builder().title("Updated Title").content("Content").build();
    Post existingPost = Post.builder().id("post1").userId(ANOTHER_USER_ID).status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("post1")).thenReturn(Optional.of(existingPost));

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.updatePost("post1", updatedPostDetails, USER_ID, USER_ROLE));

    assertEquals("Only post owner can update post", exception.getMessage());
    verify(postRepository, times(1)).findById("post1");
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("updatePost: Should throw NotFoundException for non-existent post")
  void updatePost_nonExistentPost_shouldThrowNotFoundException() {
    Post updatedPostDetails = Post.builder().title("Updated Title").content("Content").build();
    when(postRepository.findById("nonExistentId")).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
        postService.updatePost("nonExistentId", updatedPostDetails, USER_ID, USER_ROLE));

    assertEquals("Post not found", exception.getMessage());
    verify(postRepository, times(1)).findById("nonExistentId");
    verify(postRepository, never()).save(any(Post.class));
  }


  // --- replyPost tests ---
  @Test
  @DisplayName("replyPost: Should successfully add a top-level reply to a post")
  void replyPost_addTopLevelReply_shouldAddReply() {
    Post post = Post.builder().id("p1").title("T").content("C").userId(USER_ID).status(PostStatus.PUBLISHED.name()).build();
    String comment = "New top-level comment.";

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertEquals(1, savedPost.getReplies().size());
      assertEquals(comment, savedPost.getReplies().get(0).getComment());
      assertEquals(USER_ID, savedPost.getReplies().get(0).getUserId());
      return savedPost;
    });

    Post result = postService.replyPost("p1", null, comment, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertEquals(1, result.getReplies().size());
    assertEquals(comment, result.getReplies().get(0).getComment());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("replyPost: Should successfully add a sub-reply to an existing reply")
  void replyPost_addSubReply_shouldAddSubReply() {
    Reply existingReply = Reply.builder().id("r1").userId(ANOTHER_USER_ID).comment("Parent reply.").build();
    Post post = Post.builder().id("p1").title("T").content("C").userId(USER_ID).status(PostStatus.PUBLISHED.name()).replies(Arrays.asList(existingReply)).build();
    String comment = "New sub-reply.";

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertEquals(1, savedPost.getReplies().get(0).getSubReplies().size());
      assertEquals(comment, savedPost.getReplies().get(0).getSubReplies().get(0).getComment());
      return savedPost;
    });

    Post result = postService.replyPost("p1", "r1", comment, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertEquals(1, result.getReplies().get(0).getSubReplies().size());
    assertEquals(comment, result.getReplies().get(0).getSubReplies().get(0).getComment());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("replyPost: Should throw NotAuthorizedException for unverified user")
  void replyPost_unverifiedUser_shouldThrowNotAuthorizedException() {
    Post post = Post.builder().id("p1").title("T").content("C").userId(USER_ID).status(PostStatus.PUBLISHED.name()).build();
    String comment = "New comment.";

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.replyPost("p1", null, comment, UNVERIFIED_USER_ID, UNVERIFIED_ROLE));

    assertEquals("Only verified users can reply posts", exception.getMessage());
    verify(postRepository, never()).findById(anyString());
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("replyPost: Should throw NotFoundException for non-existent post")
  void replyPost_nonExistentPost_shouldThrowNotFoundException() {
    String nonExistentPostId = "nonExistent";
    String comment = "New comment.";
    when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
        postService.replyPost(nonExistentPostId, null, comment, USER_ID, USER_ROLE));

    assertEquals("Post not found", exception.getMessage());
    verify(postRepository, times(1)).findById(nonExistentPostId);
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("replyPost: Should throw IllegalArgumentException when replying to an archived post")
  void replyPost_archivedPost_shouldThrowIllegalArgumentException() {
    Post post = Post.builder().id("p1").title("T").content("C").userId(USER_ID).status(PostStatus.ARCHIVED.name()).build();
    String comment = "New comment.";

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        postService.replyPost("p1", null, comment, USER_ID, USER_ROLE));

    assertEquals("Cannot reply to an archived post", exception.getMessage());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, never()).save(any(Post.class));
  }

  // --- transferPostStatus tests ---
  @Test
  @DisplayName("transferPostStatus: Admin should successfully ban a published post")
  void transferPostStatus_adminBansPublishedPost_shouldSucceed() {
    Post post = Post.builder().id("p1").status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertEquals(PostStatus.BANNED.name(), savedPost.getStatus());
      return savedPost;
    });

    Post result = postService.transferPostStatus("p1", "BAN", ADMIN_ID, ADMIN_ROLE);

    assertNotNull(result);
    assertEquals(PostStatus.BANNED.name(), result.getStatus());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("transferPostStatus: Owner should successfully hide their own published post")
  void transferPostStatus_ownerHidesPublishedPost_shouldSucceed() {
    Post post = Post.builder().id("p1").status(PostStatus.PUBLISHED.name()).userId(USER_ID).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertEquals(PostStatus.HIDDEN.name(), savedPost.getStatus());
      return savedPost;
    });

    Post result = postService.transferPostStatus("p1", "HIDE", USER_ID, USER_ROLE);

    assertNotNull(result);
    assertEquals(PostStatus.HIDDEN.name(), result.getStatus());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("transferPostStatus: User should not be authorized to ban a post")
  void transferPostStatus_userBansPost_shouldThrowNotAuthorizedException() {
    Post post = Post.builder().id("p1").status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.transferPostStatus("p1", "BAN", USER_ID, USER_ROLE));

    assertEquals("Only admin or super admin can ban posts", exception.getMessage());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("transferPostStatus: Should throw NotFoundException for non-existent post")
  void transferPostStatus_nonExistentPost_shouldThrowNotFoundException() {
    String nonExistentId = "nonExistent";
    when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
        postService.transferPostStatus(nonExistentId, "BAN", ADMIN_ID, ADMIN_ROLE));

    assertEquals("Post not found", exception.getMessage());
    verify(postRepository, times(1)).findById(nonExistentId);
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("transferPostStatus: Should throw IllegalArgumentException for invalid operation string")
  void transferPostStatus_invalidOperation_shouldThrowIllegalArgumentException() {
    Post post = Post.builder().id("p1").status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
        postService.transferPostStatus("p1", "INVALID_OP", ADMIN_ID, ADMIN_ROLE));

    assertEquals("Operation not found", exception.getMessage()); // Service throws NotFoundException for IllegalArgumentException from valueOf
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, never()).save(any(Post.class));
  }


  // --- toggleReplyActive tests ---
  @Test
  @DisplayName("toggleReplyActive: Admin should successfully toggle a top-level reply's visibility")
  void toggleReplyActive_adminTogglesTopLevelReply_shouldSucceed() {
    Reply replyToToggle = Reply.builder().id("r1").userId(ANOTHER_USER_ID).comment("Original reply.").isActive(true).build();
    Post post = Post.builder().id("p1").userId(USER_ID).status(PostStatus.PUBLISHED.name()).replies(Arrays.asList(replyToToggle)).build();

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertFalse(savedPost.getReplies().get(0).getIsActive()); // Should be toggled to false
      return savedPost;
    });

    Post result = postService.toggleReplyActive("p1", "r1", null, ADMIN_ID, ADMIN_ROLE);

    assertNotNull(result);
    assertFalse(result.getReplies().get(0).getIsActive());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("toggleReplyActive: Reply owner should successfully toggle their sub-reply's visibility")
  void toggleReplyActive_replyOwnerTogglesSubReply_shouldSucceed() {
    SubReply subReplyToToggle = SubReply.builder().id("sr1").userId(USER_ID).comment("Sub-reply.").isActive(true).build();
    Reply reply = Reply.builder().id("r1").userId(ANOTHER_USER_ID).comment("Parent reply.").subReplies(Arrays.asList(subReplyToToggle)).build();
    Post post = Post.builder().id("p1").userId(ANOTHER_USER_ID).status(PostStatus.PUBLISHED.name()).replies(Arrays.asList(reply)).build();

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
      Post savedPost = invocation.getArgument(0);
      assertFalse(savedPost.getReplies().get(0).getSubReplies().get(0).getIsActive());
      return savedPost;
    });

    Post result = postService.toggleReplyActive("p1", "r1", "sr1", USER_ID, USER_ROLE); // USER_ID is sub-reply owner

    assertNotNull(result);
    assertFalse(result.getReplies().get(0).getSubReplies().get(0).getIsActive());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, times(1)).save(post);
  }

  @Test
  @DisplayName("toggleReplyActive: Non-owner/non-admin should not be authorized to toggle reply visibility")
  void toggleReplyActive_unauthorizedUser_shouldThrowNotAuthorizedException() {
    Reply replyToToggle = Reply.builder().id("r1").userId(ANOTHER_USER_ID).comment("Original reply.").isActive(true).build();
    Post post = Post.builder().id("p1").userId(ANOTHER_USER_ID).status(PostStatus.PUBLISHED.name()).replies(Arrays.asList(replyToToggle)).build();

    when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.toggleReplyActive("p1", "r1", null, UNVERIFIED_USER_ID, USER_ROLE)); // User is not owner or admin

    assertEquals("Only reply owner, post owner and admin can change reply visibility", exception.getMessage());
    verify(postRepository, times(1)).findById("p1");
    verify(postRepository, never()).save(any(Post.class));
  }

  @Test
  @DisplayName("toggleReplyActive: Should throw NotFoundException for non-existent post/reply/sub-reply")
  void toggleReplyActive_nonExistentEntities_shouldThrowNotFoundException() {
    // Test non-existent post
    when(postRepository.findById("nonExistentPost")).thenReturn(Optional.empty());
    NotFoundException e1 = assertThrows(NotFoundException.class, () ->
        postService.toggleReplyActive("nonExistentPost", "r1", null, ADMIN_ID, ADMIN_ROLE));
    assertEquals("Post not found", e1.getMessage());

    // Test non-existent reply on existing post
    Post postWithNoReplies = Post.builder().id("p1").userId(USER_ID).status(PostStatus.PUBLISHED.name()).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(postWithNoReplies));
    NotFoundException e2 = assertThrows(NotFoundException.class, () ->
        postService.toggleReplyActive("p1", "nonExistentReply", null, ADMIN_ID, ADMIN_ROLE));
    assertEquals("Reply not found", e2.getMessage());

    // Test non-existent sub-reply on existing post and reply
    Reply existingReply = Reply.builder().id("r1").userId(ANOTHER_USER_ID).comment("Parent reply.").build();
    Post postWithReply = Post.builder().id("p1").userId(USER_ID).status(PostStatus.PUBLISHED.name()).replies(Arrays.asList(existingReply)).build();
    when(postRepository.findById("p1")).thenReturn(Optional.of(postWithReply));
    NotFoundException e3 = assertThrows(NotFoundException.class, () ->
        postService.toggleReplyActive("p1", "r1", "nonExistentSubReply", ADMIN_ID, ADMIN_ROLE));
    assertEquals("SubReply not found", e3.getMessage());

    verify(postRepository, times(3)).findById(anyString()); // Each assertion calls findById
    verify(postRepository, never()).save(any(Post.class));
  }


  // --- getQueriedPosts tests ---
  @Test
  @DisplayName("getQueriedPosts: Should return all published posts with default parameters")
  void getQueriedPosts_defaultParameters_shouldReturnPublishedPosts() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> mockPage = new PageImpl<>(Arrays.asList(samplePublishedPost));

    when(postRepository.findAllPublished(pageable)).thenReturn(mockPage);

    Page<Post> result = postService.getQueriedPosts(0, 10, "createdAt", "desc",
        null, null, null, null, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    assertEquals(samplePublishedPost.getId(), result.getContent().get(0).getId());
    verify(postRepository, times(1)).findAllPublished(pageable);
  }

  @Test
  @DisplayName("getQueriedPosts: Should return posts filtered by specific status")
  void getQueriedPosts_filterByStatus_shouldReturnFilteredPosts() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> mockPage = new PageImpl<>(Arrays.asList(sampleBannedPost));

    when(postRepository.findByStatus(PostStatus.BANNED.name(), pageable)).thenReturn(mockPage);

    Page<Post> result = postService.getQueriedPosts(0, 10, "createdAt", "desc",
        PostStatus.BANNED.name(), null, null, null, ADMIN_ID, ADMIN_ROLE); // Admin can view banned posts

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    assertEquals(PostStatus.BANNED.name(), result.getContent().get(0).getStatus());
    verify(postRepository, times(1)).findByStatus(PostStatus.BANNED.name(), pageable);
  }

  @Test
  @DisplayName("getQueriedPosts: Should return posts filtered by keyword in title")
  void getQueriedPosts_searchByKeywordInTitle_shouldReturnFilteredPosts() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> mockPage = new PageImpl<>(Arrays.asList(samplePublishedPost));

    when(postRepository.findByTitleContainingAndPublished(eq("Published"), eq(pageable))).thenReturn(mockPage);

    Page<Post> result = postService.getQueriedPosts(0, 10, "createdAt", "desc",
        null, "Published", "title", null, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    assertTrue(result.getContent().get(0).getTitle().contains("Published"));
    verify(postRepository, times(1)).findByTitleContainingAndPublished(eq("Published"), eq(pageable));
  }

  @Test
  @DisplayName("getQueriedPosts: Should throw NotAuthorizedException for unverified user")
  void getQueriedPosts_unverifiedUser_shouldThrowNotAuthorizedException() {
    NotAuthorizedException exception = assertThrows(NotAuthorizedException.class, () ->
        postService.getQueriedPosts(0, 10, "createdAt", "desc",
            null, null, null, null, UNVERIFIED_USER_ID, UNVERIFIED_ROLE));

    assertEquals("Only verified users can view posts", exception.getMessage());
    verify(postRepository, never()).findAllPublished(any(Pageable.class));
  }

  @Test
  @DisplayName("getQueriedPosts: Should return posts filtered by userId")
  void getQueriedPosts_filterByUserId_shouldReturnFilteredPosts() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Post> mockPage = new PageImpl<>(Arrays.asList(samplePublishedPost));

    when(postRepository.findByUserIdAndPublished(eq(USER_ID), eq(pageable))).thenReturn(mockPage);

    Page<Post> result = postService.getQueriedPosts(0, 10, "createdAt", "desc",
        null, null, null, USER_ID, USER_ID, USER_ROLE);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    assertEquals(USER_ID, result.getContent().get(0).getUserId());
    verify(postRepository, times(1)).findByUserIdAndPublished(eq(USER_ID), eq(pageable));
  }
}