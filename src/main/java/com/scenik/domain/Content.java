package com.scenik.domain;

import java.time.LocalDateTime;

public class Content {
    private String contentId;
    private String title;
    private String category;
    private String subcategory;
    private Integer releaseYear;
    private String status;
    private String description;
    private String castNames;
    private String thumbnailUrl;
    private Boolean popular;
    private String country;
    private String ottPlatform;
    private String tags;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer viewCount;
    private Integer relatedPlaceCount;
    private Boolean liked;

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCastNames() { return castNames; }
    public void setCastNames(String castNames) { this.castNames = castNames; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public Boolean getPopular() { return popular; }
    public void setPopular(Boolean popular) { this.popular = popular; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getOttPlatform() { return ottPlatform; }
    public void setOttPlatform(String ottPlatform) { this.ottPlatform = ottPlatform; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getRelatedPlaceCount() { return relatedPlaceCount; }
    public void setRelatedPlaceCount(Integer relatedPlaceCount) { this.relatedPlaceCount = relatedPlaceCount; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}
