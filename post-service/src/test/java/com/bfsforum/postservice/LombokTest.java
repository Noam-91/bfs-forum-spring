package com.bfsforum.postservice;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author luluxue
 * @date 2025-06-06
 */
@SpringBootTest
public class LombokTest {
	
	@Test
	public void testLomokGettersSetters() {
		// 测试Post类的getter/setter
		Post post = new Post();
		
		// 这些方法应该存在 - 如果Lombok工作正常
		post.setTitle("Test Title");
		post.setContent("Test content");
		post.setUserId(123L);
		post.setStatus(PostStatus.PUBLISHED);
		
		// 验证getter方法
		assert post.getTitle().equals("Test Title");
		assert post.getContent().equals("Test content");
		assert post.getUserId().equals(123L);
		assert post.getStatus() == PostStatus.PUBLISHED;
		
		System.out.println("✅ Lombok getters/setters are working correctly!");
		System.out.println("Title: " + post.getTitle());
		System.out.println("Content: " + post.getContent());
		System.out.println("User ID: " + post.getUserId());
		System.out.println("Status: " + post.getStatus());
	}
	
	@Test
	public void testLombokConstructors() {
		// 测试无参构造函数
		Post post1 = new Post();
		assert post1 != null;
		
		// 测试便利构造函数（如果需要的话）
		System.out.println("✅ Lombok constructors are working correctly!");
	}
}
