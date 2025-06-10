package com.bfsforum.messageservice.controller;

import com.bfsforum.messageservice.domain.Message;
import com.bfsforum.messageservice.exception.ErrorResponse;
import com.bfsforum.messageservice.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@Slf4j
@Tag(name = "Messages", description = "Message API - Contact page in frontend")
public class MessageController {
  private final MessageService messageService;
  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  /** send message to admin, VISITOR allowed
   * @param message
   * @return void
   * */
  @PostMapping("")
  @Operation(summary = "Create Message", description = "Create a new message. VISITOR allowed.")
  @ApiResponse(responseCode = "200", description = "Create message successful",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "400", description = "Bad Request: Message creation failed",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<Map<String,String>> createMessage(@RequestBody Message message) {
    messageService.createMessage(message);
    return ResponseEntity.ok(Map.of("message", "Message has been sent to the admin team."));
  }

  @GetMapping("")
  @Operation(summary = "Get All Messages", description = "Get all messages. ADMIN / SUPER_ADMIN required.")
  @ApiResponse(responseCode = "200", description = "Get all messages successful",
      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
      array = @ArraySchema(schema = @Schema(implementation = Message.class))))
  @ApiResponse(responseCode = "400", description = "Bad Request: Get all messages failed",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<?> getAllMessages(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String userRole
  ) {
    List<Message> messages = messageService.getAllMessages(page, size, userRole);
    return ResponseEntity.ok(messages);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Solve Message", description = "Solve a message. ADMIN / SUPER_ADMIN required.")
  @ApiResponse(responseCode = "200", description = "Solve message successful",
      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
      examples = @ExampleObject(value = "{ \"message\": \"Message has been solved.\" }")))
  @ApiResponse(responseCode = "400", description = "Bad Request: Message update failed",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<Map<String,String>> solveMessage(
      @PathVariable String id,
      @RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userId,
      @RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String userRole
  ) {
    messageService.solveMessage(id, userId, userRole);
    return ResponseEntity.ok(Map.of("message", "Message has been solved."));
  }
}
