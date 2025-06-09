package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.domain.Reply;
import com.bfsforum.postservice.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@RestController
@RequestMapping("/posts/{postId}/replies")
@RequiredArgsConstructor
public class ReplyController {
	
	private final ReplyService replyService;
	
	@PostMapping
	public ResponseEntity<Reply> createReply(
			@PathVariable String postId,
			@RequestBody String comment,
			@RequestHeader("User-Id") Long userId){
		
		Reply reply = replyService.createReply(postId, userId, comment);
		return ResponseEntity.status(HttpStatus.CREATED).body(reply);
		
	}
	
	@GetMapping
	public ResponseEntity<List<Reply>> getReplies(@PathVariable String postId){
		List<Reply> replies = replyService.getRepliesByPostId(postId);
		return ResponseEntity.ok(replies);
	}
}
