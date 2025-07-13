package ddtradeup.ddtradeup2;

import java.util.List;

public class ItemModel {
    private String id;
    private String title;
    private String description;
    private List<String> tags;
    private String price;
    private String userId;
    private String imageUrl;             // nếu dùng 1 ảnh
    private List<String> imageUrls;      // nếu có danh sách ảnh
    private String status;               // Available, Sold,...
    private Double latitude;
    private Double longitude;
    private long timestamp;

    public ItemModel() {
        // Bắt buộc để Firestore deserialize
    }

    // --- GETTERS & SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    // Thêm vào cuối class trước dấu đóng }
    private Boolean negotiable;

    public Boolean isNegotiable() {
        return negotiable != null ? negotiable : false;
    }

    public void setNegotiable(Boolean negotiable) {
        this.negotiable = negotiable;
    }

}
