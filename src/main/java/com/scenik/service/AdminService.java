package com.scenik.service;

import com.scenik.domain.Content;
import com.scenik.domain.Place;
import com.scenik.dto.AdminDtos.ContentUpsertRequest;
import com.scenik.dto.AdminDtos.PlaceUpsertRequest;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.mapper.ContentMapper;
import com.scenik.mapper.PlaceMapper;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {
    private final AuthService authService;
    private final ContentMapper contentMapper;
    private final PlaceMapper placeMapper;

    public AdminService(AuthService authService, ContentMapper contentMapper, PlaceMapper placeMapper) {
        this.authService = authService;
        this.contentMapper = contentMapper;
        this.placeMapper = placeMapper;
    }

    @Transactional
    public ContentDto createContent(String authorization, ContentUpsertRequest request) {
        requireTemporaryAdmin(authorization);
        Content content = toContent(request, null);
        if (content.getContentId() == null || content.getContentId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentId is required.");
        }
        try {
            contentMapper.insert(content);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content already exists.");
        }
        return ContentDto.from(contentMapper.findByIdForWrite(content.getContentId()));
    }

    @Transactional
    public ContentDto updateContent(String authorization, String contentId, ContentUpsertRequest request) {
        requireTemporaryAdmin(authorization);
        Content existing = requireContent(contentId);
        Content content = toContent(request, existing);
        content.setContentId(contentId);
        contentMapper.update(content);
        return ContentDto.from(contentMapper.findByIdForWrite(contentId));
    }

    @Transactional
    public void deleteContent(String authorization, String contentId) {
        requireTemporaryAdmin(authorization);
        requireContent(contentId);
        contentMapper.softDelete(contentId);
    }

    @Transactional
    public PlaceDto createPlace(String authorization, PlaceUpsertRequest request) {
        requireTemporaryAdmin(authorization);
        Place place = toPlace(request, null);
        if (place.getPlaceId() == null || place.getPlaceId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "placeId is required.");
        }
        try {
            placeMapper.insert(place);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already exists.");
        }
        return PlaceDto.from(placeMapper.findByIdForWrite(place.getPlaceId()));
    }

    @Transactional
    public PlaceDto updatePlace(String authorization, String placeId, PlaceUpsertRequest request) {
        requireTemporaryAdmin(authorization);
        Place existing = requirePlace(placeId);
        Place place = toPlace(request, existing);
        place.setPlaceId(placeId);
        placeMapper.update(place);
        return PlaceDto.from(placeMapper.findByIdForWrite(placeId));
    }

    @Transactional
    public void deletePlace(String authorization, String placeId) {
        requireTemporaryAdmin(authorization);
        requirePlace(placeId);
        placeMapper.deleteRoutePlaceReferences(placeId);
        placeMapper.deletePlaceLikeReferences(placeId);
        placeMapper.deleteVisitStatusReferences(placeId);
        placeMapper.deleteContentPlaceReferences(placeId);
        placeMapper.deleteTagReferences(placeId);
        placeMapper.delete(placeId);
    }

    private void requireTemporaryAdmin(String authorization) {
        // TODO: Replace this temporary protection with role-based admin authorization.
        authService.requireUser(authorization);
    }

    private Content requireContent(String contentId) {
        Content content = contentMapper.findByIdForWrite(contentId);
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content was not found.");
        }
        return content;
    }

    private Place requirePlace(String placeId) {
        Place place = placeMapper.findByIdForWrite(placeId);
        if (place == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place was not found.");
        }
        return place;
    }

    private Content toContent(ContentUpsertRequest request, Content fallback) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content request is required.");
        }
        Content content = new Content();
        content.setContentId(firstText(request.contentId(), fallback == null ? null : fallback.getContentId()));
        content.setTitle(requireText(firstText(request.title(), fallback == null ? null : fallback.getTitle()), "title is required."));
        content.setCategory(requireText(firstText(request.category(), fallback == null ? null : fallback.getCategory()), "category is required."));
        content.setSubcategory(firstText(request.subcategory(), fallback == null ? null : fallback.getSubcategory()));
        content.setReleaseYear(request.releaseYear() == null && fallback != null ? fallback.getReleaseYear() : request.releaseYear());
        content.setStatus(firstText(request.status(), fallback == null ? "ACTIVE" : fallback.getStatus()));
        content.setDescription(firstText(request.description(), fallback == null ? null : fallback.getDescription()));
        content.setCastNames(firstText(request.castNames(), fallback == null ? null : fallback.getCastNames()));
        content.setThumbnailUrl(firstText(request.thumbnailUrl(), fallback == null ? null : fallback.getThumbnailUrl()));
        content.setPopular(request.isPopular() == null && fallback != null ? fallback.getPopular() : Boolean.TRUE.equals(request.isPopular()));
        content.setCountry(firstText(request.country(), fallback == null ? "Korea" : fallback.getCountry()));
        content.setOttPlatform(firstText(request.ottPlatform(), fallback == null ? null : fallback.getOttPlatform()));
        content.setTags(joinTags(request.tags(), fallback == null ? null : fallback.getTags()));
        return content;
    }

    private Place toPlace(PlaceUpsertRequest request, Place fallback) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Place request is required.");
        }
        Place place = new Place();
        place.setPlaceId(firstText(request.placeId(), fallback == null ? null : fallback.getPlaceId()));
        place.setPlaceName(requireText(firstText(request.name(), firstText(request.placeName(), fallback == null ? null : fallback.getPlaceName())), "name is required."));
        place.setAddress(firstText(request.address(), fallback == null ? null : fallback.getAddress()));
        place.setRegion(requireText(firstText(request.region(), fallback == null ? null : fallback.getRegion()), "region is required."));
        place.setDistrict(firstText(request.district(), fallback == null ? null : fallback.getDistrict()));
        place.setCategory(firstText(request.category(), fallback == null ? null : fallback.getCategory()));
        place.setDescription(firstText(request.description(), fallback == null ? null : fallback.getDescription()));
        place.setOpeningHours(firstText(request.openingHours(), fallback == null ? null : fallback.getOpeningHours()));
        place.setClosedDays(firstText(request.closedDays(), fallback == null ? null : fallback.getClosedDays()));
        place.setPhoneNumber(firstText(request.phoneNumber(), fallback == null ? null : fallback.getPhoneNumber()));
        place.setImageUrl(firstText(request.thumbnailUrl(), firstText(request.imageUrl(), fallback == null ? null : fallback.getImageUrl())));
        place.setIndoorOutdoor(firstText(request.indoorOutdoor(), fallback == null ? null : fallback.getIndoorOutdoor()));
        place.setParkingAvailable(request.parkingAvailable() == null && fallback != null ? fallback.getParkingAvailable() : Boolean.TRUE.equals(request.parkingAvailable()));
        place.setLatitude(request.latitude() == null && fallback != null ? fallback.getLatitude() : request.latitude());
        place.setLongitude(request.longitude() == null && fallback != null ? fallback.getLongitude() : request.longitude());
        return place;
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String joinTags(List<String> tags, String fallback) {
        if (tags == null) {
            return fallback;
        }
        return String.join(",", tags.stream().filter(tag -> tag != null && !tag.isBlank()).map(String::trim).toList());
    }
}
