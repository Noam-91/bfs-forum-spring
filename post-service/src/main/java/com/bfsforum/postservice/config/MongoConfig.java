package com.bfsforum.postservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Slf4j
@Configuration
@EnableMongoRepositories(basePackages = "com.bfsforum.postservice.dao")
public class MongoConfig extends AbstractMongoClientConfiguration {
	
	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;
	
	@Value("${spring.data.mongodb.database}")
	private String database;
	
	@Override
	protected String getDatabaseName() {
		return database;
	}
	
	@Override
	@Bean
	public MongoClient mongoClient() {
		log.info("Configuring MongoDB connection to: {}", mongoUri);
		
		ConnectionString connectionString = new ConnectionString(mongoUri);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();
		
		return MongoClients.create(settings);
	}
	
	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), getDatabaseName());
	}
	
	/**
	 * customized type converter
	 */
	@Override
	@Bean
	public MongoCustomConversions customConversions() {
		List<Object> converters = new ArrayList<>();
		
		// LocalDateTime to Date converter
		converters.add(new org.springframework.core.convert.converter.Converter<LocalDateTime, Date>() {
			@Override
			public Date convert(LocalDateTime source) {
				return Date.from(source.toInstant(ZoneOffset.UTC));
			}
		});
		
		// Date to LocalDateTime converter
		converters.add(new org.springframework.core.convert.converter.Converter<Date, LocalDateTime>() {
			@Override
			public LocalDateTime convert(Date source) {
				return LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
			}
		});
		
		return new MongoCustomConversions(converters);
	}
	
	/**
	 * MongoDB health check
	 */
	@Bean
	public MongoHealthIndicator mongoHealthIndicator() {
		return new MongoHealthIndicator(mongoTemplate());
	}
	
	/**
	 * customized health indicators
	 */
	public static class MongoHealthIndicator {
		private final MongoTemplate mongoTemplate;
		
		public MongoHealthIndicator(MongoTemplate mongoTemplate) {
			this.mongoTemplate = mongoTemplate;
		}
		
		public boolean isHealthy() {
			try {
				mongoTemplate.getCollection("health_check");
				return true;
			} catch (Exception e) {
				log.error("MongoDB health check failed", e);
				return false;
			}
		}
	}
}
