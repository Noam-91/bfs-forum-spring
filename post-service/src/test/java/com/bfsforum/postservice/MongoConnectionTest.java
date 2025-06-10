//package com.bfsforum.postservice;
//
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoDatabase;
//
///**
// *
// * @author luluxue
// * @date 2025-06-06
// *
// */
//
//public class MongoConnectionTest {
//	public static void main(String[] args) {
//		// load from .env
//		String uri = System.getenv("MONGODB_URI");
//
//		if (uri == null) {
//			uri = "mongodb+srv://admin:5H5TUOlCTwjbcGKt@cluster0.4cdeyph.mongodb.net/postdb?retryWrites=true&w=majority}"; // 替换成你的 URI 进行测试
//		}
//
//		try (MongoClient mongoClient = MongoClients.create(uri)) {
//			MongoDatabase database = mongoClient.getDatabase("postdb");
//			System.out.println("Connected to database: " + database.getName());
//		} catch (Exception e) {
//			System.err.println("Connection failed: " + e.getMessage());
//		}
//	}
//}