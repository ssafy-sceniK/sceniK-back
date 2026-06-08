package com.scenik.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ListQuerySupport {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private ListQuerySupport() {
    }

    public static String normalizeSort(String sort) {
        return sort == null ? "" : sort.trim().toLowerCase(Locale.ROOT);
    }

    public static void validateSort(String sort, Set<String> allowed) {
        String normalized = normalizeSort(sort);
        if (!normalized.isEmpty() && !allowed.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort parameter.");
        }
    }

    public static <T> List<T> paginate(List<T> items, Integer page, Integer size) {
        if (page == null && size == null) {
            return items;
        }
        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? DEFAULT_PAGE_SIZE : size;
        if (safePage < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page parameter.");
        }
        if (safeSize <= 0 || safeSize > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid size parameter.");
        }
        int fromIndex = safePage * safeSize;
        if (fromIndex >= items.size()) {
            return List.of();
        }
        return items.subList(fromIndex, Math.min(items.size(), fromIndex + safeSize));
    }

    public static boolean containsText(String source, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (source == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }
}
