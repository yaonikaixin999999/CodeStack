package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.dto.moderation.AiModerationResult;
import com.example.cloudstack.demo.dto.moderation.ModerationDecision;
import com.example.cloudstack.demo.model.entity.Comment;
import com.example.cloudstack.demo.model.entity.Post;
import com.example.cloudstack.demo.repository.CommentRepository;
import com.example.cloudstack.demo.repository.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiModerationService {
    private static final int POST_STATUS_PUBLISHED = 1;
    private static final int POST_STATUS_PENDING = 2;
    private static final int POST_STATUS_REJECTED = 3;

    private static final int COMMENT_STATUS_PENDING = 0;
    private static final int COMMENT_STATUS_PUBLISHED = 1;
    private static final int COMMENT_STATUS_DELETED = 2;

    private static final List<String> DEFAULT_BLOCKLIST = List.of(
            "spam",
            "scam",
            "fraud",
            "porn",
            "gambling",
            "violence",
            "terror"
    );

    private static final List<String> DEFAULT_REVIEWLIST = List.of(
            "ad",
            "promo",
            "contact",
            "wechat",
            "qq",
            "telegram",
            "link"
    );

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AdminService adminService;
    private final List<String> blocklist;
    private final List<String> reviewlist;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper;

    @Value("${ai.moderation.model:qwen-plus}")
    private String model;

    @Value("${ai.moderation.api-base:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String apiBase;

    @Value("${ai.moderation.api-key:}")
    private String apiKey;

    public AiModerationService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            AdminService adminService,
            @Value("${ai.moderation.blocklist:}") String blocklistConfig,
            @Value("${ai.moderation.reviewlist:}") String reviewlistConfig,
            ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.adminService = adminService;
        this.blocklist = parseKeywords(blocklistConfig, DEFAULT_BLOCKLIST);
        this.reviewlist = parseKeywords(reviewlistConfig, DEFAULT_REVIEWLIST);
        this.objectMapper = objectMapper;
    }

    public AiModerationResult reviewPost(Long postId, boolean apply) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        AiModerationResult result = evaluate("post", post.getId(), post.getTitle(), post.getContent());
        result.setOldStatus(post.getStatus());

        if (apply) {
            applyPostDecision(post, result);
        } else {
            result.setApplied(false);
            result.setNewStatus(post.getStatus());
        }

        return result;
    }

    public AiModerationResult reviewComment(Long commentId, boolean apply) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        AiModerationResult result = evaluate("comment", comment.getId(), null, comment.getContent());
        result.setOldStatus(comment.getStatus());

        if (apply) {
            applyCommentDecision(comment, result);
        } else {
            result.setApplied(false);
            result.setNewStatus(comment.getStatus());
        }

        return result;
    }

    public AiModerationResult reviewText(String targetType, String title, String content) {
        String safeType = targetType == null || targetType.isBlank() ? "text" : targetType.trim();
        return evaluate(safeType, null, title, content);
    }

    private AiModerationResult evaluate(String targetType, Long targetId, String title, String content) {
        String text = buildText(title, content);

        // 优先尝试模型审核，失败则回退规则
        AiModerationResult llmResult = callModel(text, targetType, targetId);
        if (llmResult != null) {
            return llmResult;
        }

        List<String> blockHits = matchKeywords(text, blocklist);
        List<String> reviewHits = matchKeywords(text, reviewlist);

        ModerationDecision decision;
        List<String> reasons = new ArrayList<>();

        if (!blockHits.isEmpty()) {
            decision = ModerationDecision.REJECT;
            reasons.add("blocklist_hit");
        } else if (!reviewHits.isEmpty()) {
            decision = ModerationDecision.REVIEW;
            reasons.add("reviewlist_hit");
        } else {
            decision = ModerationDecision.APPROVE;
            reasons.add("clean");
        }

        double score = Math.min(1.0, blockHits.size() * 0.6 + reviewHits.size() * 0.3);
        List<String> matched = new ArrayList<>();
        matched.addAll(blockHits);
        matched.addAll(reviewHits);

        return AiModerationResult.builder()
                .targetType(targetType)
                .targetId(targetId)
                .decision(decision)
                .score(score)
                .reasons(reasons)
                .matchedKeywords(matched)
                .applied(false)
                .provider("rule")
                .build();
    }

    private AiModerationResult callModel(String text, String targetType, Long targetId) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            String prompt = buildPrompt(text);
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a strict content safety classifier. Return ONLY a JSON object with fields: decision (APPROVE|REVIEW|REJECT), reasons (array), matchedKeywords (array)."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String url = apiBase.endsWith("/") ? apiBase + "chat/completions" : apiBase + "/chat/completions";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                return null;
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isEmpty()) {
                return null;
            }
            String content = contentNode.asText();
            JsonNode parsed = objectMapper.readTree(content);

            String decisionStr = parsed.path("decision").asText("");
            ModerationDecision decision = parseDecision(decisionStr);
            if (decision == null) {
                return null;
            }

            List<String> reasons = new ArrayList<>();
            if (parsed.has("reasons") && parsed.get("reasons").isArray()) {
                parsed.get("reasons").forEach(r -> reasons.add(r.asText()));
            }
            List<String> keywords = new ArrayList<>();
            if (parsed.has("matchedKeywords") && parsed.get("matchedKeywords").isArray()) {
                parsed.get("matchedKeywords").forEach(k -> keywords.add(k.asText()));
            }

            return AiModerationResult.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .decision(decision)
                    .score(0.8) // 模型未返回分数，此处给出固定参考值
                    .reasons(reasons.isEmpty() ? List.of("llm_decision") : reasons)
                    .matchedKeywords(keywords)
                    .applied(false)
                    .provider("dashscope-llm")
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private ModerationDecision parseDecision(String decision) {
        if (decision == null) return null;
        String d = decision.trim().toUpperCase(Locale.ROOT);
        return switch (d) {
            case "APPROVE", "PASS", "ALLOW" -> ModerationDecision.APPROVE;
            case "REVIEW", "MANUAL", "PENDING" -> ModerationDecision.REVIEW;
            case "REJECT", "BLOCK", "DENY" -> ModerationDecision.REJECT;
            default -> null;
        };
    }

    private String buildPrompt(String text) {
        return """
                对以下内容进行安全审核，返回 JSON：
                { "decision": "APPROVE|REVIEW|REJECT", "reasons": ["..."], "matchedKeywords": ["..."] }
                审核内容：
                """ + text;
    }

    private void applyPostDecision(Post post, AiModerationResult result) {
        if (result.getDecision() == ModerationDecision.REVIEW) {
            result.getReasons().add("manual_review_required");
            result.setApplied(false);
            result.setNewStatus(post.getStatus());
            return;
        }

        if (!Objects.equals(post.getStatus(), POST_STATUS_PENDING)) {
            result.getReasons().add("status_not_pending");
            result.setApplied(false);
            result.setNewStatus(post.getStatus());
            return;
        }

        if (result.getDecision() == ModerationDecision.APPROVE) {
            adminService.approvePost(post.getId());
            result.setApplied(true);
            result.setNewStatus(POST_STATUS_PUBLISHED);
        } else if (result.getDecision() == ModerationDecision.REJECT) {
            adminService.rejectPost(post.getId());
            result.setApplied(true);
            result.setNewStatus(POST_STATUS_REJECTED);
        }
    }

    private void applyCommentDecision(Comment comment, AiModerationResult result) {
        if (result.getDecision() == ModerationDecision.REVIEW) {
            result.getReasons().add("manual_review_required");
            result.setApplied(false);
            result.setNewStatus(comment.getStatus());
            return;
        }

        if (!Objects.equals(comment.getStatus(), COMMENT_STATUS_PENDING)) {
            result.getReasons().add("status_not_pending");
            result.setApplied(false);
            result.setNewStatus(comment.getStatus());
            return;
        }

        if (result.getDecision() == ModerationDecision.APPROVE) {
            adminService.approveComment(comment.getId());
            result.setApplied(true);
            result.setNewStatus(COMMENT_STATUS_PUBLISHED);
        } else if (result.getDecision() == ModerationDecision.REJECT) {
            adminService.deleteComment(comment.getId());
            result.setApplied(true);
            result.setNewStatus(COMMENT_STATUS_DELETED);
        }
    }

    private List<String> parseKeywords(String raw, List<String> defaults) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>(defaults);
        }

        Set<String> unique = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ArrayList<>(unique);
    }

    private List<String> matchKeywords(String text, List<String> keywords) {
        if (text.isEmpty() || keywords.isEmpty()) {
            return List.of();
        }

        List<String> hits = new ArrayList<>();
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                hits.add(keyword);
            }
        }
        return hits;
    }

    private String buildText(String title, String content) {
        String safeTitle = title == null ? "" : title;
        String safeContent = content == null ? "" : content;
        return (safeTitle + " " + safeContent).toLowerCase(Locale.ROOT);
    }
}
