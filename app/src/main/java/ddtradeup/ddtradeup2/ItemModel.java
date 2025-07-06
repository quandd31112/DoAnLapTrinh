package ddtradeup.ddtradeup2;

public class ItemModel {
    private String id;
    private String title;
    private String description;
    private double price;
    private String imageUrl;
    private String userId; // Dùng để phân quyền xóa bài

    // Constructor mặc định (bắt buộc cho Firestore/Parcelable)
    public ItemModel() {}

    // Constructor đầy đủ, đúng thứ tự và tên tham số dùng trong code
    public ItemModel(String id, String title, String description, double price, String imageUrl, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
