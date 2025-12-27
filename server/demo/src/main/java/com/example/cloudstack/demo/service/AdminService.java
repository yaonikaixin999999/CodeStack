package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.entity.Comment;
import com.example.cloudstack.demo.model.entity.Category;
import com.example.cloudstack.demo.model.entity.Announcement;
import com.example.cloudstack.demo.model.entity.Post;
import com.example.cloudstack.demo.model.entity.User;
import com.example.cloudstack.demo.repository.CategoryRepository;
import com.example.cloudstack.demo.repository.CommentRepository;
import com.example.cloudstack.demo.repository.AnnouncementRepository;
import com.example.cloudstack.demo.repository.PostRepository;
import com.example.cloudstack.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AnnouncementRepository announcementRepository;
    private final MessageService messageService;

    private final List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> emitters = Collections
            .synchronizedList(new ArrayList<>());
    private final Map<Long, List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public Map<String, Object> getDashboard(int recentLimit) {
        long totalPosts = postRepository.count();
        long totalUsers = userRepository.count();
        long totalComments = commentRepository.count();
        long pendingPosts = postRepository.findAll().stream().filter(p -> Objects.equals(p.getStatus(), 2)).count();
        long pendingComments = commentRepository.findAll().stream().filter(c -> Objects.equals(c.getStatus(), 0))
                .count();

        List<Post> recentPosts = postRepository.findAll(PageRequest.of(0, recentLimit)).getContent();
        List<Map<String, Object>> recentPostDtos = recentPosts.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("status", p.getStatus());
            m.put("statusLabel", statusLabel(p.getStatus()));
            m.put("userId", p.getUserId());
            m.put("updatedAt", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);
            m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> queue = new ArrayList<>();
        postRepository.findAll(PageRequest.of(0, recentLimit)).getContent().stream()
                .filter(p -> Objects.equals(p.getStatus(), 2))
                .forEach(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("title", p.getTitle());
                    m.put("type", "文章");
                    m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
                    queue.add(m);
                });
        commentRepository.findAll(PageRequest.of(0, recentLimit)).getContent().stream()
                .filter(c -> Objects.equals(c.getStatus(), 0))
                .forEach(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("title", c.getContent());
                    m.put("type", "评论");
                    m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
                    queue.add(m);
                });

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPosts", totalPosts);
        stats.put("totalUsers", totalUsers);
        stats.put("totalComments", totalComments);
        stats.put("pendingPosts", pendingPosts);
        stats.put("pendingComments", pendingComments);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", stats);
        data.put("recentPosts", recentPostDtos);
        data.put("moderationQueue", queue);
        data.put("metrics", buildLatestPostMetrics());
        data.put("announcements", getLatestAnnouncements());
        return data;
    }

    public Map<String, Object> getCharts(int postDays, int commentDays, int loginDays) {
        int safePostDays = Math.max(1, postDays);
        int safeCommentDays = Math.max(1, commentDays);
        int safeLoginDays = Math.max(1, loginDays);

        LocalDateTime postStart = LocalDate.now().minusDays(safePostDays - 1L).atStartOfDay();
        LocalDateTime commentStart = LocalDate.now().minusDays(safeCommentDays - 1L).atStartOfDay();
        LocalDateTime loginStart = LocalDate.now().minusDays(safeLoginDays - 1L).atStartOfDay();

        List<Post> posts = postRepository.findByStatusAndPublishedAtAfter(1, postStart);
        List<LocalDateTime> postDates = posts.stream()
                .map(p -> p.getPublishedAt() != null ? p.getPublishedAt() : p.getCreatedAt())
                .collect(Collectors.toList());

        // List<Comment> comments = commentRepository.findByStatusAndCreatedAtAfter(1,
        // commentStart);
        List<Comment> comments = commentRepository.findByStatusInAndCreatedAtAfter(Arrays.asList(0, 1), commentStart);
        List<LocalDateTime> commentDates = comments.stream()
                .map(Comment::getCreatedAt)
                .collect(Collectors.toList());

        List<User> users = userRepository.findByLastLoginAtAfter(loginStart);
        List<LocalDateTime> loginDates = users.stream()
                .map(User::getLastLoginAt)
                .collect(Collectors.toList());

        Map<Long, Long> categoryCounts = new LinkedHashMap<>();
        for (Object[] row : postRepository.countPostsByCategory()) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }
            Long categoryId = (Long) row[0];
            Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            categoryCounts.put(categoryId, count);
        }

        List<Category> categories = categoryRepository.findAllActiveCategories();
        List<Map<String, Object>> categorySlices = categories.stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("label", String.valueOf(c.getId()));
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    m.put("value", categoryCounts.getOrDefault(c.getId(), 0L));
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("postSeries", buildSeries(postDates, safePostDays));
        data.put("commentSeries", buildSeries(commentDates, safeCommentDays));
        data.put("loginSeries", buildSeries(loginDates, safeLoginDays));
        data.put("categorySlices", categorySlices);
        return data;
    }

    public List<Map<String, Object>> listPosts(Integer status, Integer limit, String q) {
        String keyword = q != null ? q.trim() : "";
        boolean hasKeyword = !keyword.isBlank();

        List<Post> posts = postRepository.findAll(PageRequest.of(0, limit != null ? limit : 50)).getContent();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Post p : posts) {
            if (status != null && !Objects.equals(p.getStatus(), status)) {
                continue;
            }

            User author = null;
            if (p.getUserId() != null) {
                author = userRepository.findById(p.getUserId()).orElse(null);
            }

            if (hasKeyword) {
                String title = p.getTitle() != null ? p.getTitle() : "";
                boolean match = title.contains(keyword);

                if (!match && author != null) {
                    String username = author.getUsername() != null ? author.getUsername() : "";
                    String nickname = author.getNickname() != null ? author.getNickname() : "";
                    String email = author.getEmail() != null ? author.getEmail() : "";
                    match = username.contains(keyword) || nickname.contains(keyword) || email.contains(keyword);
                }

                if (!match) {
                    continue;
                }
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("status", p.getStatus());
            m.put("statusLabel", statusLabel(p.getStatus()));
            m.put("userId", p.getUserId());
            m.put("username", author != null ? author.getUsername() : null);
            m.put("nickname", author != null ? author.getNickname() : null);
            m.put("email", author != null ? author.getEmail() : null);
            m.put("updatedAt", p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null);
            m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            result.add(m);
        }

        return result;
    }

    public List<Map<String, Object>> listComments(Integer status, Integer limit) {
        // List<Comment> comments = commentRepository.findAll(PageRequest.of(0, limit !=
        // null ? limit : 50)).getContent();
        int safeLimit = limit != null ? limit : 50;
        PageRequest pageRequest = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Comment> comments = status == null
                ? commentRepository.findAll(pageRequest).getContent()
                : commentRepository.findByStatus(status, pageRequest).getContent();
        return comments.stream()
                // .filter(c -> status == null || Objects.equals(c.getStatus(), status))
                .map(c -> {

                    User author = c.getUserId() != null ? userRepository.findById(c.getUserId()).orElse(null) : null;
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("username", author != null ? author.getUsername() : null);
                    m.put("id", c.getId());
                    m.put("userId", c.getUserId());
                    m.put("username", author != null ? author.getUsername() : null);
                    m.put("content", c.getContent());
                    m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional
    public Announcement createAnnouncement(Long userId, String title, String content, String type) {
        String safeTitle = title != null ? title.trim() : "";
        String safeContent = content != null ? content.trim() : "";
        String safeType = (type != null && !type.trim().isEmpty()) ? type.trim() : "系统";

        if (safeTitle.isEmpty() && safeContent.isEmpty()) {
            throw new RuntimeException("公告标题和内容不能为空");
        }

        Announcement ann = Announcement.builder()
                .title(safeTitle)
                .content(safeContent)
                .type(safeType)
                .createdBy(userId)
                .build();
        ann = announcementRepository.save(ann);

        // 仍然向所有用户发送站内信公告，保持原有行为
        try {
            messageService.broadcastAnnouncement(userId, safeTitle, safeContent, safeType);
        } catch (Exception ignored) {
            // 不因通知失败影响公告存储
        }

        broadcastRefresh();
        return ann;
    }

    public List<Map<String, Object>> getLatestAnnouncements() {
        List<Announcement> list = announcementRepository.findTop5ByOrderByCreatedAtDesc();
        List<String> palette = List.of("#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6");

        return list.stream().map((ann) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ann.getId());
            m.put("title", ann.getTitle());
            m.put("content", ann.getContent());
            m.put("time", ann.getCreatedAt() != null ? ann.getCreatedAt().toString() : "");
            m.put("tag", ann.getType() != null ? ann.getType() : "系统");

            String typeLower = (ann.getType() != null ? ann.getType() : "系统").toLowerCase(Locale.ROOT);
            String color;
            if (typeLower.contains("安全")) {
                color = "#ef4444";
            } else if (typeLower.contains("更新") || typeLower.contains("版本")) {
                color = "#3b82f6";
            } else if (typeLower.contains("系统")) {
                color = "#10b981";
            } else {
                color = palette.get((int) (ann.getId() % palette.size()));
            }
            m.put("color", color);
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void approvePost(Long id) {
        Post p = postRepository.findById(id).orElseThrow();
        p.setStatus(1);
        p.setPublishedAt(java.time.LocalDateTime.now());
        postRepository.save(p);
        broadcastRefresh();
    }

    @Transactional
    public void rejectPost(Long id) {
        Post p = postRepository.findById(id).orElseThrow();
        p.setStatus(3);
        postRepository.save(p);
        broadcastRefresh();
    }

    @Transactional
    public void deletePost(Long id) {
        Post p = postRepository.findById(id).orElseThrow();
        postRepository.delete(p);
        broadcastRefresh();
    }

    @Transactional
    public void approveComment(Long id) {
        Comment c = commentRepository.findById(id).orElseThrow();
        c.setStatus(1);
        commentRepository.save(c);
        broadcastRefresh();
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment c = commentRepository.findById(id).orElseThrow();
        commentRepository.delete(c);
        broadcastRefresh();
    }

    public List<Map<String, Object>> listUsers(Map<String, Object> params) {
        Object qObj = params != null ? params.get("q") : null;
        String keyword = qObj != null ? String.valueOf(qObj).trim() : "";
        boolean hasKeyword = !keyword.isBlank();

        List<User> users = userRepository.findAll(PageRequest.of(0, 100)).getContent();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : users) {
            if (hasKeyword) {
                String username = u.getUsername() != null ? u.getUsername() : "";
                String nickname = u.getNickname() != null ? u.getNickname() : "";
                String email = u.getEmail() != null ? u.getEmail() : "";
                if (!username.contains(keyword) && !nickname.contains(keyword) && !email.contains(keyword)) {
                    continue;
                }
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("status", u.getStatus());
            m.put("lastLoginAt", u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null);
            m.put("avatar", u.getAvatar());
            result.add(m);
        }

        return result;
    }

    @Transactional
    public void muteUser(Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setStatus(0);
        userRepository.save(u);
        // 拒绝该用户所有待审核的文章
        postRepository.rejectPendingByUserId(id);
        broadcastUserStatus(u);
    }

    @Transactional
    public void banUser(Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setStatus(-1);
        userRepository.save(u);
        broadcastUserStatus(u);
    }

    @Transactional
    public void unbanUser(Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setStatus(1);
        userRepository.save(u);
        broadcastUserStatus(u);
    }

    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String dataUrl = "data:" + (contentType != null ? contentType : "image/png") + ";base64," + base64;
        User u = userRepository.findById(userId).orElseThrow();
        u.setAvatar(dataUrl);
        userRepository.save(u);
        broadcastRefresh();
        return dataUrl;
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe() {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribeUser(Long userId) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                0L);
        userEmitters.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(emitter);

        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        return emitter;
    }

    private void removeUserEmitter(Long userId, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void broadcastRefresh() {
        synchronized (emitters) {
            Iterator<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> it = emitters.iterator();
            while (it.hasNext()) {
                org.springframework.web.servlet.mvc.method.annotation.SseEmitter e = it.next();
                try {
                    e.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("refresh")
                            .data("refresh"));
                } catch (Exception ex) {
                    it.remove();
                }
            }
        }
    }

    private void broadcastUserStatus(User user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", user.getId());
        payload.put("status", user.getStatus());
        payload.put("role", user.getRole());

        synchronized (emitters) {
            Iterator<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> it = emitters.iterator();
            while (it.hasNext()) {
                org.springframework.web.servlet.mvc.method.annotation.SseEmitter e = it.next();
                try {
                    e.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("user-status-changed")
                            .data(payload));
                } catch (Exception ex) {
                    it.remove();
                }
            }
        }

        // 兼容已有刷新逻辑
        broadcastRefresh();

        // 推送给目标用户
        List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> targets = userEmitters.get(user.getId());
        if (targets != null) {
            Iterator<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> it = targets.iterator();
            while (it.hasNext()) {
                org.springframework.web.servlet.mvc.method.annotation.SseEmitter e = it.next();
                try {
                    e.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("user-status-changed")
                            .data(payload));
                } catch (Exception ex) {
                    it.remove();
                }
            }
            if (targets.isEmpty()) {
                userEmitters.remove(user.getId());
            }
        }
    }

    private String statusLabel(Integer status) {
        if (status == null)
            return "草稿";
        if (status == 1)
            return "已发布";
        if (status == 2)
            return "待审核";
        if (status == 3)
            return "已下架";
        return "草稿";
    }

    private List<Map<String, Object>> buildSeries(List<LocalDateTime> dates, int days) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            counts.put(date, 0L);
        }
        for (LocalDateTime dt : dates) {
            if (dt == null)
                continue;
            LocalDate date = dt.toLocalDate();
            if (counts.containsKey(date)) {
                counts.put(date, counts.get(date) + 1);
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return counts.entrySet().stream().map(entry -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", entry.getKey().format(formatter));
            m.put("value", entry.getValue());
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildLatestPostMetrics() {
        List<Post> latest = postRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        return latest.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", trimTitle(p.getTitle()));
            m.put("value", statusLabel(p.getStatus()));
            m.put("progress", statusToProgress(p.getStatus()));
            m.put("trend", 0); // 暂无环比趋势，先置 0
            return m;
        }).collect(Collectors.toList());
    }

    private int statusToProgress(Integer status) {
        if (status == null) return 20;
        return switch (status) {
            case 1 -> 100; // 已发布
            case 2 -> 50; // 待审核
            case 3 -> 0; // 已下架/驳回
            default -> 20; // 草稿
        };
    }

    private String trimTitle(String title) {
        if (title == null) return "未命名文章";
        return title.length() > 18 ? title.substring(0, 18) + "…" : title;
    }
}
