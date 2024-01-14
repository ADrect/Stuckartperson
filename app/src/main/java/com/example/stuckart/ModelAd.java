package com.example.stuckart;

public class ModelAd {
    String id;
    String uid;
    String Brand;
    String category;
    String condition;
    String address;
    String price;
    String title;
    String description;
    String status;
    long timestamp;
    double latitude;
    double longitude;
    boolean favourite;

    public ModelAd() {
    }


    public ModelAd(String id, String uid, String brand, String category, String condition, String address, String price, String title, String description, String status, long timestamp, double latitude, double longitude, boolean favourite) {
        this.id = id;
        this.uid = uid;
        Brand = brand;
        this.category = category;
        this.condition = condition;
        this.address = address;
        this.price = price;
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favourite = favourite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
