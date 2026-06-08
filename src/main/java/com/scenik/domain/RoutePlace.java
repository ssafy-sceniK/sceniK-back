package com.scenik.domain;

import java.time.LocalDateTime;

public class RoutePlace {
    private String routePlaceId;
    private String routeId;
    private String placeId;
    private Integer dayNumber;
    private Integer visitOrder;
    private String recommendedTime;
    private String reason;
    private String travelTip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String placeName;
    private String region;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String relatedContentId;
    private String relatedContentTitle;
    private String sceneDescription;

    public String getRoutePlaceId() { return routePlaceId; }
    public void setRoutePlaceId(String routePlaceId) { this.routePlaceId = routePlaceId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Integer getVisitOrder() { return visitOrder; }
    public void setVisitOrder(Integer visitOrder) { this.visitOrder = visitOrder; }
    public String getRecommendedTime() { return recommendedTime; }
    public void setRecommendedTime(String recommendedTime) { this.recommendedTime = recommendedTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getTravelTip() { return travelTip; }
    public void setTravelTip(String travelTip) { this.travelTip = travelTip; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getRelatedContentId() { return relatedContentId; }
    public void setRelatedContentId(String relatedContentId) { this.relatedContentId = relatedContentId; }
    public String getRelatedContentTitle() { return relatedContentTitle; }
    public void setRelatedContentTitle(String relatedContentTitle) { this.relatedContentTitle = relatedContentTitle; }
    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
}
