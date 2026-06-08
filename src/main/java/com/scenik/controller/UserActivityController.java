package com.scenik.controller;

import com.scenik.domain.User;
import com.scenik.service.AuthService;
import com.scenik.service.CommunityService;
import com.scenik.service.LikeVisitService;
import com.scenik.service.RouteService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserActivityController {
    private final AuthService authService;
    private final RouteService routeService;
    private final LikeVisitService likeVisitService;
    private final CommunityService communityService;

    public UserActivityController(
            AuthService authService,
            RouteService routeService,
            LikeVisitService likeVisitService,
            CommunityService communityService
    ) {
        this.authService = authService;
        this.routeService = routeService;
        this.likeVisitService = likeVisitService;
        this.communityService = communityService;
    }

    @GetMapping("/api/users/me/history")
    public Map<String, Object> history(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = authService.requireUser(authorization);
        return Map.of(
                "routes", routeService.listMyRoutes(user),
                "likedContentIds", likeVisitService.getLikedContentIds(user),
                "likedPlaceIds", likeVisitService.getLikedPlaceIds(user),
                "visitStatus", likeVisitService.getVisitStatuses(user),
                "posts", communityService.listMyPosts(user)
        );
    }

    @GetMapping("/api/users/me/activity-summary")
    public Map<String, Object> activitySummary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = authService.requireUser(authorization);
        var routes = routeService.listMyRoutes(user);
        var likes = likeVisitService.getLikes(user);
        var visitStatuses = likeVisitService.getVisitStatuses(user);
        int wantToGoCount = (int) visitStatuses.stream().filter(item -> "WANT_TO_GO".equals(item.status())).count();
        int visitedCount = (int) visitStatuses.stream().filter(item -> "VISITED".equals(item.status())).count();
        int postCount = communityService.listMyPosts(user).size();
        int notificationCount = communityService.countNotifications(user);
        int unreadNotificationCount = communityService.countUnreadNotifications(user);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("savedRouteCount", routes.size());
        summary.put("postCount", postCount);
        summary.put("likedContentCount", likes.contentCount());
        summary.put("likedPlaceCount", likes.placeCount());
        summary.put("wantToGoCount", wantToGoCount);
        summary.put("visitedCount", visitedCount);
        summary.put("notificationCount", notificationCount);
        summary.put("unreadNotificationCount", unreadNotificationCount);
        summary.put("hasSavedRoutes", !routes.isEmpty());
        summary.put("hasPosts", postCount > 0);
        summary.put("hasUnreadNotifications", unreadNotificationCount > 0);
        return summary;
    }
}
