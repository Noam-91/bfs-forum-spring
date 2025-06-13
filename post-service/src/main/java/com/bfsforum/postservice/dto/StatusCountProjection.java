package com.bfsforum.postservice.dto;
import com.bfsforum.postservice.domain.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@AllArgsConstructor // Lombok: Generates a constructor with all fields
@NoArgsConstructor // Lombok: Generates a no-argument constructor
public class StatusCountProjection {
    // This field name '_id' maps directly to the $group _id output in MongoDB
    // Spring Data MongoDB will try to convert the String _id to PostStatus enum
    private PostStatus _id;
    private Long count;
}
