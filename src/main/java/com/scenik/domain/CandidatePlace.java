package com.scenik.domain;

public class CandidatePlace {
    private String placeId;
    private String name;
    private String region;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String relatedContentId;
    private String relatedContentTitle;
    private String sceneDescription;
    private Boolean verified;
    private String imageUrl;

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
    public String getRelatedContentId() { return relatedContentId; }
    public void setRelatedContentId(String relatedContentId) { this.relatedContentId = relatedContentId; }
    public String getRelatedContentTitle() { return relatedContentTitle; }
    public void setRelatedContentTitle(String relatedContentTitle) { this.relatedContentTitle = relatedContentTitle; }
    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
