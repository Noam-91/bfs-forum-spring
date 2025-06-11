package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {
	@Builder.Default
	private String id = UUID.randomUUID().toString();

	private UserInfo userInfo;

	private String comment;

	@Builder.Default
	private Boolean isActive = true;  // isActive (false -> deleted)

	private LocalDateTime createdAt;

	@Builder.Default
	private List<SubReply> subReplies = new ArrayList<>();
}
