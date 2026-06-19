package vanillacoffeesystem;

public class OrderItemDetail {

    private final int productId;
    private final String productName;
    private final int quantity;
    private final double price;
    private Integer existingRating;

    public OrderItemDetail(int productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public Integer getExistingRating() {
        return existingRating;
    }

    public void setExistingRating(Integer existingRating) {
        this.existingRating = existingRating;
    }

    public String getDisplayName() {
        if (existingRating != null) {
            return productName + " (rated " + existingRating + "/5)";
        }
        return productName;
    }

    @Override
    public String toString() {
        return String.format("%s  ×  %d   @  ₪ %.2f   =   ₪ %.2f",
                productName, quantity, price, price * quantity);
    }
}
