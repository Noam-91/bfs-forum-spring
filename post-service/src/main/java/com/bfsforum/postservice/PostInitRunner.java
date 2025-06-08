package com.bfsforum.postservice;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-06-06
 */

@Component
@Profile("dev")
public class PostInitRunner implements CommandLineRunner {
	@Autowired
	private PostRepository postRepository;
	
	@Override
	public void run(String... args) throws Exception {
		// clear
		postRepository.deleteAll();
		
		// create an example
		Post post1 = new Post();
		post1.setUserId(12345L);
		post1.setTitle("Welcome to the Forum!");  // add title
		post1.setContent("Hello everyone! This is my first post on this amazing forum. Looking forward to great discussions!");
		post1.setStatus(PostStatus.PUBLISHED);
		
		Post post2 = new Post();
		post2.setUserId(12346L);
		post2.setTitle("Spring Boot Tips and Tricks");
		post2.setContent("Here are some useful Spring Boot development tips that I've learned over the years...");
		post2.setStatus(PostStatus.PUBLISHED);
		
		Post post3 = new Post();
		post3.setUserId(12345L);
		post3.setTitle("Draft Post - Work in Progress");
		post3.setContent("This is still a work in progress...");
		post3.setStatus(PostStatus.UNPUBLISHED);  // Draft status
		
		// save to db
		postRepository.save(post1);
		postRepository.save(post2);
		postRepository.save(post3);
		
		System.out.println("✅ Sample posts created successfully!");
		System.out.println("📝 Post 1 ID: " + post1.getId());
		System.out.println("📝 Post 2 ID: " + post2.getId());
		System.out.println("📝 Post 3 ID: " + post3.getId());
		
		// verify connection with db
		long count = postRepository.count();
		System.out.println("Total posts in db: " + count);
	}
}
