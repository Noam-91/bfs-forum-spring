package com.bfsforum.postservice;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;

/**
 * @author luluxue
 * @date 2025-06-06
 */

/**
 * simplified test
 */
public class LombokTestMain {
	
	public static void main(String[] args) {
		System.out.println("🔍 Testing Lombok functionality...");
		
		try {
			// 创建Post对象
			Post post = new Post();
			System.out.println("✅ Post object created successfully");
			
			// 测试setter方法
			post.setTitle("Main Test Title");
			post.setContent("Main Test Content");
			post.setUserId(789L);
			post.setStatus(PostStatus.PUBLISHED);
			System.out.println("✅ Setter methods executed successfully");
			
			// 测试getter方法
			String title = post.getTitle();
			String content = post.getContent();
			Long userId = post.getUserId();
			PostStatus status = post.getStatus();
			System.out.println("✅ Getter methods executed successfully");
			
			// 打印结果
			System.out.println("\n📋 Test Results:");
			System.out.println("   Title: " + title);
			System.out.println("   Content: " + content);
			System.out.println("   User ID: " + userId);
			System.out.println("   Status: " + status);
			System.out.println("   Is Archived: " + post.getIsArchived());
			System.out.println("   View Count: " + post.getViewCount());
			System.out.println("   Reply Count: " + post.getReplyCount());
			
			System.out.println("\n🎉 Lombok is working perfectly!");
			
		} catch (Exception e) {
			System.err.println("❌ Lombok test failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
}