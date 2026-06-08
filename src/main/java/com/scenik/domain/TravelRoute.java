package com.scenik.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TravelRoute {
    private String routeId;
    private Long userId;
    private String title;
    private String summary;
    private String contentId;
    private String contentIds;
    private String contentTitle;
    private String region;
    private Integer durationDays;
    private Integer requestedDurationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dateRangeLabel;
    private String transport;
    private String companion;
    private String style;
    private Integer budgetMin;
    private Integer budgetMax;
    private String budget;
    private String trustMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getContentIds() { return contentIds; }
    public void setContentIds(String contentIds) { this.contentIds = contentIds; }
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getRequestedDurationDays() { return requestedDurationDays; }
    public void setRequestedDurationDays(Integer requestedDurationDays) { this.requestedDurationDays = requestedDurationDays; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDateRangeLabel() { return dateRangeLabel; }
    public void setDateRangeLabel(String dateRangeLabel) { this.dateRangeLabel = dateRangeLabel; }
    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public String getCompanion() { return companion; }
    public void setCompanion(String companion) { this.companion = companion; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public Integer getBudgetMin() { return budgetMin; }
    public void setBudgetMin(Integer budgetMin) { this.budgetMin = budgetMin; }
    public Integer getBudgetMax() { return budgetMax; }
    public void setBudgetMax(Integer budgetMax) { this.budgetMax = budgetMax; }
    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }
    public String getTrustMessage() { return trustMessage; }
    public void setTrustMessage(String trustMessage) { this.trustMessage = trustMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
