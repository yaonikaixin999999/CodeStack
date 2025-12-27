package com.example.cloudstack.demo.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModerationResult {
    private String targetType;
    private Long targetId;
    private ModerationDecision decision;
    private double score;

    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    @Builder.Default
    private List<String> matchedKeywords = new ArrayList<>();

    private boolean applied;
    private Integer oldStatus;
    private Integer newStatus;
    private String provider;
}
