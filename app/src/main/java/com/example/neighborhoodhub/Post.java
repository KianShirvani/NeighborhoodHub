package com.example.neighborhoodhub;

public class Post {
    private String category;
    private String description;
    private String location;
    private String mediaUrl; // Optional field, can be null or empty
    private String userId;
    private String postId;

    // Default constructor required for Firebase
    public Post() {
    }

    // Constructor with essential fields
    public Post(String category, String description, String location) {
        this.category = category;
        this.description = description;
        this.location = location;
        this.mediaUrl = ""; // Initialize as empty if unused
    }

    // Getters
    public String getPostId() {
        return postId;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getUserId() {
        return userId;
    }

    // Setters
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}