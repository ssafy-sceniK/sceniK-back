package com.scenik.service;

import com.scenik.domain.User;
import com.scenik.dto.LikeVisitDtos.LikesDto;
import com.scenik.dto.LikeVisitDtos.VisitStatusDto;
import com.scenik.mapper.ContentMapper;
import com.scenik.mapper.LikeVisitMapper;
import com.scenik.mapper.PlaceMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikeVisitService {
    private final LikeVisitMapper likeVisitMapper;
    private final ContentMapper contentMapper;
    private final PlaceMapper placeMapper;

    public LikeVisitService(LikeVisitMapper likeVisitMapper, ContentMapper contentMapper, PlaceMapper placeMapper) {
        this.likeVisitMapper = likeVisitMapper;
        this.contentMapper = contentMapper;
        this.placeMapper = placeMapper;
    }

    public void likeContent(User user, String contentId) {
        requireContent(contentId);
        likeVisitMapper.insertContentLike(user.getUserId(), contentId);
    }

    public void unlikeContent(User user, String contentId) {
        likeVisitMapper.deleteContentLike(user.getUserId(), contentId);
    }

    public void likePlace(User user, String placeId) {
        requirePlace(placeId);
        likeVisitMapper.insertPlaceLike(user.getUserId(), placeId);
    }

    public void unlikePlace(User user, String placeId) {
        likeVisitMapper.deletePlaceLike(user.getUserId(), placeId);
    }

    public LikesDto getLikes(User user) {
        List<String> contentIds = likeVisitMapper.findLikedContentIds(user.getUserId());
        List<String> placeIds = likeVisitMapper.findLikedPlaceIds(user.getUserId());
        return new LikesDto(contentIds, placeIds, contentIds.size(), placeIds.size());
    }

    public List<String> getLikedContentIds(User user) {
        return likeVisitMapper.findLikedContentIds(user.getUserId());
    }

    public List<String> getLikedPlaceIds(User user) {
        return likeVisitMapper.findLikedPlaceIds(user.getUserId());
    }

    public VisitStatusDto setVisitStatus(User user, String placeId, String status) {
        requirePlace(placeId);
        String normalized = normalizeStatus(status);
        likeVisitMapper.upsertVisitStatus(user.getUserId(), placeId, normalized);
        return new VisitStatusDto(placeId, normalized);
    }

    public void deleteVisitStatus(User user, String placeId) {
        requirePlace(placeId);
        likeVisitMapper.deleteVisitStatus(user.getUserId(), placeId);
    }

    public List<VisitStatusDto> getVisitStatuses(User user) {
        return likeVisitMapper.findVisitStatuses(user.getUserId()).stream()
                .map(item -> new VisitStatusDto(item.getPlaceId(), item.getStatus()))
                .toList();
    }

    private void requireContent(String contentId) {
        if (contentMapper.findByIdForWrite(contentId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content was not found.");
        }
    }

    private void requirePlace(String placeId) {
        if (placeMapper.findByIdForWrite(placeId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
    }

    private String normalizeStatus(String status) {
        if ("planned".equals(status) || "WANT_TO_GO".equals(status)) {
            return "WANT_TO_GO";
        }
        if ("completed".equals(status) || "VISITED".equals(status)) {
            return "VISITED";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit status must be WANT_TO_GO or VISITED.");
    }
}
