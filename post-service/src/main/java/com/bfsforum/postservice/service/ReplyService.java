package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dao.ReplyRepository;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.Reply;
import com.bfsforum.postservice.domain.SubReply;
import com.bfsforum.postservice.dto.SubReplyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@Service
@RequiredArgsConstructor
public class ReplyService {
	private final ReplyRepository replyRepository;
	private final PostService postService;
	
	public Reply createReply(String postId, Long userId, String comment){
		Reply reply = new Reply();
		reply.setPostId(postId);
		reply.setUserId(userId);
		reply.setComment(comment);
		reply.setCreatedAt(LocalDateTime.now());
		
		Reply savedReply = replyRepository.save(reply);
		
		updatePostReplyCount(postId);
		
		return savedReply;
	}
	
	public List<Reply> getRepliesByPostId(String postId){
		return replyRepository.findByPostIdOrderByCreatedAtAsc(postId);
	}
	
	private void updatePostReplyCount(String postId){
		long replyCount = replyRepository.countByPostId(postId);
		Post post = postService.getPostById(postId).orElse(null);
		
		if (post != null) {
			post.setReplyCount((int) replyCount);
			postService.updatePost(postId, post);
		}
	}
	
	// add subReplies
	public void addSubReply(String replyId, SubReplyDTO dto){
		Reply reply = replyRepository.findById(replyId)
				.orElseThrow(() -> new RuntimeException("Reply not found"));
		
		SubReply subReply = new SubReply();
		subReply.setUserId(dto.getUserId());
		subReply.setComment(dto.getComment());
		subReply.setIsActive(true);
		subReply.setCreatedAt(LocalDateTime.now());
		
		reply.getSubReplies().add(subReply);
		replyRepository.save(reply);
	}
}
