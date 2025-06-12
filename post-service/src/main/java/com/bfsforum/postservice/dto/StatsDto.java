package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class StatsDto {
    public Integer total;
    public Integer UnpublishedCount;
    public Integer PublishedCount;
    public Integer HiddenCount;
    public Integer BannedCount;
    public Integer ArchivedCount;
    public Integer DeletedCount;
}
