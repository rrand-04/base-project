package com.example.vanillacoffeesystem;

public class MenuItem {

    private final int productId;
    private final String productName;
    private final String productCategory;
    private final double price;
    private final String productDescription;
    private final boolean available;
    private final String imageFile;

    public MenuItem(int productId, String productName, String productCategory,
                    double price, String productDescription, boolean available, String imageFile) {
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.price = price;
        this.productDescription = productDescription;
        this.available = available;
        this.imageFile = imageFile;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public double getPrice() {
        return price;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getImageFile() {
        return imageFile;
    }

    public String getCategoryEmoji() {
        return switch (productCategory) {
            case "BreakFast" -> "🍳";
            case "Starters" -> "🥙";
            case "Salads" -> "🥗";
            case "Burger" -> "🍔";
            case "Pasta" -> "🍝";
            case "Pizza" -> "🍕";
            case "International" -> "🌍";
            case "Meats" -> "🥩";
            case "Om Ali" -> "🍮";
            case "hookah" -> "💨";
            case "Food" -> "🥐";
            case "Drinks" -> "☕";
            case "Espresso Hot" -> "☕";
            case "Espresso Cold" -> "🧊";
            case "Refreshers" -> "🍹";
            case "Desserts" -> "🍰";
            default -> "🍽";
        };
    }
}
