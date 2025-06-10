package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import com.bfsforum.postservice.domain.Reply;
import com.bfsforum.postservice.domain.SubReply;
import com.bfsforum.postservice.exception.NotAuthorizedException;
import com.bfsforum.postservice.exception.NotFoundException;
import com.bfsforum.postservice.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = PostController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class // EXCLUDE SPRING SECURITY
)
class PostControllerTest {
  @MockitoBean
  private PostService postService;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  private final String USER_ID = "testUser123";
  private final String ADMIN_ID = "adminUser456";
  private final String USER_ROLE = "USER";
  private final String ADMIN_ROLE = "ADMIN";

  private Post samplePost;
  private Post bannedPost;
  private Post postWithReplies;

  @BeforeEach
  void setUp() {
    // Initialize sample data before each test
    samplePost = Post.builder()
        .id("samplePostId")
        .title("Test Post Title")
        .content("This is some content.")
        .userId(USER_ID)
        .status(PostStatus.PUBLISHED.name())
        .build();
    bannedPost = Post.builder()
        .id("bannedPostId")
        .title("Banned Post Title")
        .content("This content is banned.")
        .userId("anotherUser")
        .status(PostStatus.BANNED.name())
        .build();

    Reply reply1 = Reply.builder()
        .id("reply1")
        .userId("replyUser1")
        .comment("First reply")
        .isActive(true)
        .build();
    SubReply subReply1_1 = SubReply.builder()
        .id("subReply1_1")
        .userId("subReplyUser1")
        .comment("Sub-reply to first reply")
        .isActive(true)
        .build();
    reply1.getSubReplies().add(subReply1_1);

    Reply reply2 = Reply.builder()
        .id("reply2")
        .userId("replyUser2")
        .comment("Second reply")
        .isActive(false) // Inactive reply
        .build();

    postWithReplies = Post.builder()
        .id("postWithRepliesId")
        .title("Post with Replies")
        .content("Content with comments.")
        .userId("postUser")
        .status(PostStatus.PUBLISHED.name())
        .build();
    postWithReplies.getReplies().add(reply1);
    postWithReplies.getReplies().add(reply2);
  }

  @Test
  @DisplayName("Get all posts with default pagination")
  void getQueriedPosts_defaultParameters_shouldReturnOk() throws Exception {
    Page<Post> mockPage = new PageImpl<>(Collections.singletonList(samplePost));
    when(postService.getQueriedPosts(
        anyInt(), anyInt(), anyString(), anyString(), any(), any(), any(), any(), anyString(), anyString()))
        .thenReturn(mockPage);

    mockMvc.perform(get("/posts")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].id").value(samplePost.getId()));
  }

  @Test
  @DisplayName("Get posts with custom pagination and sorting")
  void getQueriedPosts_customParameters_shouldReturnOk() throws Exception {
    Page<Post> mockPage = new PageImpl<>(Arrays.asList(samplePost)); // Simulate a page with 1 post
    when(postService.getQueriedPosts(
        eq(1), eq(5), eq("title"), eq("asc"), any(), any(), any(), any(), anyString(), anyString()))
        .thenReturn(mockPage);

    mockMvc.perform(get("/posts")
            .param("page", "1")
            .param("size", "5")
            .param("sortBy", "title")
            .param("sortDir", "asc")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content[0].title").value(samplePost.getTitle()));
  }

  @Test
  @DisplayName("Get posts filtered by status (e.g., PUBLISHED)")
  void getQueriedPosts_filterByStatus_shouldReturnOk() throws Exception {
    Page<Post> mockPage = new PageImpl<>(Collections.singletonList(samplePost));
    when(postService.getQueriedPosts(
        anyInt(), anyInt(), anyString(), anyString(), eq(PostStatus.PUBLISHED.name()), any(), any(), any(), anyString(), anyString()))
        .thenReturn(mockPage);

    mockMvc.perform(get("/posts")
            .param("status", "PUBLISHED")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));
  }

  @Test
  @DisplayName("Admin retrieves all posts including hidden/banned")
  void getQueriedPosts_adminAccess_shouldReturnOk() throws Exception {
    Page<Post> mockPage = new PageImpl<>(Collections.singletonList(bannedPost));
    when(postService.getQueriedPosts(
        anyInt(), anyInt(), anyString(), anyString(), eq("BANNED"), any(), any(), any(), eq(ADMIN_ID), eq(ADMIN_ROLE)))
        .thenReturn(mockPage);

    mockMvc.perform(get("/posts")
            .param("status", "BANNED")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(bannedPost.getId()))
        .andExpect(jsonPath("$.content[0].status").value("BANNED"));
  }

  @Test
  @DisplayName("User attempts to retrieve banned posts they don't own - Forbidden")
  void getQueriedPosts_userForbiddenStatus_shouldReturnForbidden() throws Exception {
    doThrow(new NotAuthorizedException("Access to banned posts is forbidden for this user."))
        .when(postService).getQueriedPosts(
        anyInt(), anyInt(), anyString(), anyString(), eq("BANNED"),
            any(), any(), any(), eq(USER_ID), eq(USER_ROLE));

    mockMvc.perform(get("/posts")
            .param("status", "BANNED")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("Access to banned posts is forbidden for this user."));
  }

  @Test
  @DisplayName("Invalid sortDir parameter - Bad Request")
  void getQueriedPosts_invalidSortDir_shouldReturnBadRequest() throws Exception {
    when(postService.getQueriedPosts(
        anyInt(), anyInt(), anyString(), eq("invalid"), any(), any(),
        any(), any(), anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("Invalid sort direction: invalid. Must be 'asc' or 'desc'."));

    mockMvc.perform(get("/posts")
            .param("sortDir", "invalid")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error")
            .value("Invalid sort direction: invalid. Must be 'asc' or 'desc'."));
  }

  // --- POST /posts tests ---
  @Test
  @DisplayName("Successfully create a new post")
  void createPost_validPost_shouldReturnCreated() throws Exception {
    Post newPost = Post.builder()
        .title("New Post")
        .content("This is a new post.")
        .build();
    Post createdPost = Post.builder()
        .id("newPostId")
        .title(newPost.getTitle())
        .content(newPost.getContent())
        .userId(USER_ID)
        .status(PostStatus.PUBLISHED.name())
        .build();

    when(postService.createPost(any(Post.class), eq(USER_ID), eq(USER_ROLE)))
        .thenReturn(createdPost);

    mockMvc.perform(post("/posts")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newPost)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(createdPost.getId()))
        .andExpect(jsonPath("$.title").value(newPost.getTitle()));
  }

  @Test
  @DisplayName("Create post with missing required fields - Bad Request")
  void createPost_missingTitle_shouldReturnBadRequest() throws Exception {
    // Simulating validation error for missing title
    String invalidPostJson = "{\"content\": \"This post has no title.\"}";

    mockMvc.perform(post("/posts")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPostJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Create post with empty title/content - Bad Request")
  void createPost_emptyFields_shouldReturnBadRequest() throws Exception {
    // Simulating validation error for empty title/content (if @NotBlank or @NotEmpty is used)
    String invalidPostJson = "{\"title\": \"\", \"content\": \"\"}";

    mockMvc.perform(post("/posts")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPostJson))
        .andExpect(status().isBadRequest());
  }


  // --- GET /posts/{postId} tests ---
  @Test
  @DisplayName("Get an existing active post by ID")
  void getPostById_existingPost_shouldReturnOk() throws Exception {
    when(postService.getPostById(eq(samplePost.getId()), eq(USER_ID)))
        .thenReturn(samplePost);

    mockMvc.perform(get("/posts/{postId}", samplePost.getId())
            .header("X-User-Id", USER_ID)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(samplePost.getId()));
  }

  @Test
  @DisplayName("Get a non-existent post by ID - Not Found")
  void getPostById_nonExistentPost_shouldReturnNotFound() throws Exception {
    String nonExistentId = "nonExistentPostId";
    when(postService.getPostById(eq(nonExistentId), anyString()))
        .thenThrow(new NotFoundException("Post not found with ID: " + nonExistentId));

    mockMvc.perform(get("/posts/{postId}", nonExistentId)
            .header("X-User-Id", USER_ID)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Post not found with ID: " + nonExistentId));
  }

  @Test
  @DisplayName("Non-owner user attempts to view a BANNED post - Forbidden")
  void getPostById_userForbiddenBannedPost_shouldReturnForbidden() throws Exception {
    String nonOwnerId = "nonOwnerUser";
    when(postService.getPostById(eq(bannedPost.getId()), eq(nonOwnerId)))
        .thenThrow(new NotAuthorizedException("Access to this post is forbidden."));

    mockMvc.perform(get("/posts/{postId}", bannedPost.getId())
            .header("X-User-Id", nonOwnerId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("Access to this post is forbidden."));
  }

  @Test
  @DisplayName("Admin user views a BANNED post")
  void getPostById_adminViewsBannedPost_shouldReturnOk() throws Exception {
    when(postService.getPostById(eq(bannedPost.getId()), eq(ADMIN_ID)))
        .thenReturn(bannedPost);

    mockMvc.perform(get("/posts/{postId}", bannedPost.getId())
            .header("X-User-Id", ADMIN_ID)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bannedPost.getId()))
        .andExpect(jsonPath("$.status").value("BANNED"));
  }


  // --- PUT /posts/{postId} tests ---
  @Test
  @DisplayName("Successfully update an existing post (owner)")
  void updatePost_ownerUpdatesPost_shouldReturnOk() throws Exception {
    Post updatedPost = Post.builder()
        .id(samplePost.getId())
        .title("Updated Title")
        .content("New content")
        .userId(USER_ID)
        .status(PostStatus.PUBLISHED.name())
        .build();
    when(postService.updatePost(eq(samplePost.getId()), any(Post.class), eq(USER_ID), eq(USER_ROLE)))
        .thenReturn(updatedPost);

    mockMvc.perform(put("/posts/{postId}", samplePost.getId())
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedPost)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"))
        .andExpect(jsonPath("$.content").value("New content"));
  }

  @Test
  @DisplayName("Admin successfully updates any post")
  void updatePost_adminUpdatesAnyPost_shouldReturnOk() throws Exception {
    Post updatedBannedPost = Post.builder()
        .id(bannedPost.getId())
        .title("Admin Updated Title")
        .content("Admin new content")
        .userId(bannedPost.getUserId())
        .status(PostStatus.BANNED.name())
        .build();
    when(postService.updatePost(eq(bannedPost.getId()), any(Post.class), eq(ADMIN_ID), eq(ADMIN_ROLE)))
        .thenReturn(updatedBannedPost);

    mockMvc.perform(put("/posts/{postId}", bannedPost.getId())
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedBannedPost)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Admin Updated Title"));
  }

  @Test
  @DisplayName("Non-owner user attempts to update a post - Forbidden")
  void updatePost_nonOwnerForbidden_shouldReturnForbidden() throws Exception {
    String nonOwnerId = "anotherUser";
    Post updatedPost = Post.builder()
        .id(samplePost.getId())
        .title("Attempted Update")
        .content("Forbidden content")
        .userId(nonOwnerId)
        .status(PostStatus.PUBLISHED.name())
        .build();

    when(postService.updatePost(eq(samplePost.getId()), any(Post.class), eq(nonOwnerId), eq(USER_ROLE)))
        .thenThrow(new NotAuthorizedException("You are not authorized to update this post."));

    mockMvc.perform(put("/posts/{postId}", samplePost.getId())
            .header("X-User-Id", nonOwnerId)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedPost)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("You are not authorized to update this post."));
  }

  @Test
  @DisplayName("Update non-existent post - Not Found")
  void updatePost_nonExistentPost_shouldReturnNotFound() throws Exception {
    String nonExistentId = "nonExistentPostId";
    Post updatedPost = Post.builder()
        .id(nonExistentId)
        .title("Updated Title")
        .content("Updated Content")
        .userId(USER_ID)
        .status(PostStatus.PUBLISHED.name())
        .build();

    when(postService.updatePost(eq(nonExistentId), any(Post.class), anyString(), anyString()))
        .thenThrow(new NotFoundException("Post not found with ID: " + nonExistentId));

    mockMvc.perform(put("/posts/{postId}", nonExistentId)
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedPost)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Post not found with ID: " + nonExistentId));
  }

  @Test
  @DisplayName("Update post with invalid request body (validation error) - Bad Request")
  void updatePost_invalidBody_shouldReturnBadRequest() throws Exception {
    String invalidPostJson = "{\"id\": \"" + samplePost.getId() + "\", \"title\": \"\", \"content\": \"Valid Content\"}"; // Empty title

    mockMvc.perform(put("/posts/{postId}", samplePost.getId())
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPostJson))
        .andExpect(status().isBadRequest());
  }


  // --- POST /posts/{postId} (replyPost) tests ---
  @Test
  @DisplayName("Successfully add a top-level reply to a post")
  void replyPost_addTopLevelReply_shouldReturnOk() throws Exception {
    String comment = "This is a new comment on post3.";
    Post updatedPostWithNewReply = postWithReplies; // For simplicity, assume service modifies and returns same object

    when(postService.replyPost(eq(postWithReplies.getId()), isNull(), eq(comment), eq(USER_ID), eq(USER_ROLE)))
        .thenReturn(updatedPostWithNewReply);

    mockMvc.perform(post("/posts/{postId}", postWithReplies.getId())
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.TEXT_PLAIN) // comment is @RequestBody String
            .content(comment))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postWithReplies.getId()));
  }

  @Test
  @DisplayName("Successfully add a sub-reply to an existing reply")
  void replyPost_addSubReply_shouldReturnOk() throws Exception {
    String comment = "This is a sub-comment to replyABC.";
    String replyId = "reply1"; // Existing reply ID
    Post updatedPostWithSubReply = postWithReplies;

    when(postService.replyPost(eq(postWithReplies.getId()), eq(replyId), eq(comment), eq("replyUser1"), eq(USER_ROLE)))
        .thenReturn(updatedPostWithSubReply);

    mockMvc.perform(post("/posts/{postId}", postWithReplies.getId())
            .param("replyId", replyId)
            .header("X-User-Id", "replyUser1") // Assuming this user can reply
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.TEXT_PLAIN)
            .content(comment))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postWithReplies.getId()));
  }

  @Test
  @DisplayName("Reply to a non-existent post - Not Found")
  void replyPost_nonExistentPost_shouldReturnNotFound() throws Exception {
    String nonExistentPostId = "nonExistentPostId";
    String comment = "Some comment.";

    when(postService.replyPost(eq(nonExistentPostId), any(), eq(comment), anyString(), anyString()))
        .thenThrow(new NotFoundException("Post not found with ID: " + nonExistentPostId));

    mockMvc.perform(post("/posts/{postId}", nonExistentPostId)
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.TEXT_PLAIN)
            .content(comment))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Post not found with ID: " + nonExistentPostId));
  }

  @Test
  @DisplayName("Reply to a post with an invalid replyId (for sub-reply) - Not Found")
  void replyPost_invalidReplyId_shouldReturnNotFound() throws Exception {
    String invalidReplyId = "invalidReplyId";
    String comment = "Some comment.";

    when(postService.replyPost(eq(postWithReplies.getId()), eq(invalidReplyId), eq(comment), anyString(), anyString()))
        .thenThrow(new NotFoundException("Reply not found with ID: " + invalidReplyId));

    mockMvc.perform(post("/posts/{postId}", postWithReplies.getId())
            .param("replyId", invalidReplyId)
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.TEXT_PLAIN)
            .content(comment))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Reply not found with ID: " + invalidReplyId));
  }

  @Test
  @DisplayName("Reply with empty comment - Bad Request")
  void replyPost_emptyComment_shouldReturnBadRequest() throws Exception {
    String emptyComment = ""; // Or just " " for blank string

    when(postService.replyPost(eq(postWithReplies.getId()), any(), eq(emptyComment), anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("Comment cannot be empty."));

    mockMvc.perform(post("/posts/{postId}", postWithReplies.getId())
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE)
            .contentType(MediaType.TEXT_PLAIN)
            .content(emptyComment))
        .andExpect(status().isBadRequest());
  }

  // --- PATCH /posts/{postId} (transferPostStatus) tests ---
  @Test
  @DisplayName("Admin successfully bans a post")
  void transferPostStatus_adminBansPost_shouldReturnOk() throws Exception {
    Post bannedByAdmin = Post.builder()
        .id(samplePost.getId())
        .title(samplePost.getTitle())
        .content(samplePost.getContent())
        .userId(samplePost.getUserId())
        .status(PostStatus.BANNED.name())
        .build();
    when(postService.transferPostStatus(eq(samplePost.getId()), eq("BAN"), eq(ADMIN_ID), eq(ADMIN_ROLE)))
        .thenReturn(bannedByAdmin);

    mockMvc.perform(patch("/posts/{postId}", samplePost.getId())
            .param("operation", "BAN")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("BANNED"));
  }

  @Test
  @DisplayName("Owner successfully hides their own post")
  void transferPostStatus_ownerHidesPost_shouldReturnOk() throws Exception {
    Post hiddenByOwner = Post.builder()
        .id(samplePost.getId())
        .title(samplePost.getTitle())
        .content(samplePost.getContent())
        .userId(samplePost.getUserId())
        .status(PostStatus.HIDDEN.name())
        .build();
    when(postService.transferPostStatus(eq(samplePost.getId()), eq("HIDE"), eq(USER_ID), eq(USER_ROLE)))
        .thenReturn(hiddenByOwner);

    mockMvc.perform(patch("/posts/{postId}", samplePost.getId())
            .param("operation", "HIDE")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("HIDDEN"));
  }

  @Test
  @DisplayName("Non-owner user attempts to BAN a post - Forbidden")
  void transferPostStatus_nonOwnerForbidden_shouldReturnForbidden() throws Exception {
    String nonOwnerId = "anotherUser";
    when(postService.transferPostStatus(eq(samplePost.getId()), eq("BAN"), eq(nonOwnerId), eq(USER_ROLE)))
        .thenThrow(new NotAuthorizedException("You are not authorized to perform this operation."));

    mockMvc.perform(patch("/posts/{postId}", samplePost.getId())
            .param("operation", "BAN")
            .header("X-User-Id", nonOwnerId)
            .header("X-User-Role", USER_ROLE))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("You are not authorized to perform this operation."));
  }

  @Test
  @DisplayName("Transfer status with invalid operation - Bad Request")
  void transferPostStatus_invalidOperation_shouldReturnBadRequest() throws Exception {
    when(postService.transferPostStatus(eq(samplePost.getId()), eq("INVALID_OPERATION"), anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("Invalid operation: INVALID_OPERATION"));

    mockMvc.perform(patch("/posts/{postId}", samplePost.getId())
            .param("operation", "INVALID_OPERATION")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid operation: INVALID_OPERATION"));
  }

  @Test
  @DisplayName("Transfer status of a non-existent post - Not Found")
  void transferPostStatus_nonExistentPost_shouldReturnNotFound() throws Exception {
    String nonExistentId = "nonExistentPostId";
    when(postService.transferPostStatus(eq(nonExistentId), eq("DELETE"), anyString(), anyString()))
        .thenThrow(new NotFoundException("Post not found with ID: " + nonExistentId));

    mockMvc.perform(patch("/posts/{postId}", nonExistentId)
            .param("operation", "DELETE")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Post not found with ID: " + nonExistentId));
  }


  // --- PATCH /posts/{postId}/reply/{replyId} (toggleReplyActive) tests ---
  @Test
  @DisplayName("Admin toggles a top-level reply's visibility")
  void toggleReplyActive_adminTogglesTopLevelReply_shouldReturnOk() throws Exception {
    Post updatedPost = postWithReplies; // Simulating service returning updated post
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq("reply1"),
        isNull(), eq(ADMIN_ID), eq(ADMIN_ROLE)))
        .thenReturn(updatedPost);

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), "reply1")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postWithReplies.getId()));
    // Further assertions could check the reply's active status in the returned JSON
  }

  @Test
  @DisplayName("Post owner toggles a sub-reply's visibility")
  void toggleReplyActive_moderatorTogglesSubReply_shouldReturnOk() throws Exception {
    Post updatedPost = postWithReplies;
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq("reply1"),
        eq("subReply1_1"), eq("postUser"), eq(USER_ROLE)))
        .thenReturn(updatedPost);

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), "reply1")
            .param("subReplyId", "subReply1_1")
            .header("X-User-Id", "postUser")
            .header("X-User-Role", USER_ROLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postWithReplies.getId()));
  }

  @Test
  @DisplayName("Owner of the post toggles a reply's visibility")
  void toggleReplyActive_postOwnerTogglesReply_shouldReturnOk() throws Exception {
    Post updatedPost = postWithReplies;
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq("reply1"), isNull(), eq(USER_ID), eq(USER_ROLE)))
        .thenReturn(updatedPost);

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), "reply1")
            .header("X-User-Id", USER_ID)
            .header("X-User-Role", USER_ROLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(postWithReplies.getId()));
  }

  @Test
  @DisplayName("Non-owner, non-admin/moderator user attempts to toggle reply visibility - Forbidden")
  void toggleReplyActive_forbidden_shouldReturnForbidden() throws Exception {
    String unauthorizedUser = "unauthorizedUser";
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq("reply1"), isNull(), eq(unauthorizedUser), eq(USER_ROLE)))
        .thenThrow(new NotAuthorizedException("You are not authorized to toggle this reply's visibility."));

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), "reply1")
            .header("X-User-Id", unauthorizedUser)
            .header("X-User-Role", USER_ROLE))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("You are not authorized to toggle this reply's visibility."));
  }

  @Test
  @DisplayName("Toggle reply visibility on a non-existent post - Not Found")
  void toggleReplyActive_nonExistentPost_shouldReturnNotFound() throws Exception {
    String nonExistentPostId = "nonExistentPostId";
    when(postService.toggleReplyActive(eq(nonExistentPostId), eq("reply1"), isNull(), anyString(), anyString()))
        .thenThrow(new NotFoundException("Post not found with ID: " + nonExistentPostId));

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", nonExistentPostId, "reply1")
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Post not found with ID: " + nonExistentPostId));
  }

  @Test
  @DisplayName("Toggle non-existent top-level reply - Not Found")
  void toggleReplyActive_nonExistentTopLevelReply_shouldReturnNotFound() throws Exception {
    String nonExistentReplyId = "nonExistentReply";
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq(nonExistentReplyId), isNull(), anyString(), anyString()))
        .thenThrow(new NotFoundException("Reply not found with ID: " + nonExistentReplyId));

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), nonExistentReplyId)
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Reply not found with ID: " + nonExistentReplyId));
  }

  @Test
  @DisplayName("Toggle non-existent sub-reply - Not Found")
  void toggleReplyActive_nonExistentSubReply_shouldReturnNotFound() throws Exception {
    String nonExistentSubReplyId = "nonExistentSubReply";
    when(postService.toggleReplyActive(eq(postWithReplies.getId()), eq("reply1"), eq(nonExistentSubReplyId), anyString(), anyString()))
        .thenThrow(new NotFoundException("Sub-reply not found with ID: " + nonExistentSubReplyId));

    mockMvc.perform(patch("/posts/{postId}/reply/{replyId}", postWithReplies.getId(), "reply1")
            .param("subReplyId", nonExistentSubReplyId)
            .header("X-User-Id", ADMIN_ID)
            .header("X-User-Role", ADMIN_ROLE))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Sub-reply not found with ID: " + nonExistentSubReplyId));
  }
}