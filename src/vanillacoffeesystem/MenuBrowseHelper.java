package vanillacoffeesystem;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class MenuBrowseHelper {

    private static final List<String> RESTAURANT_CATEGORIES = List.of(
            "All", "BreakFast", "Starters", "Salads", "Burger", "Pasta",
            "Pizza", "Meats", "Drinks", "Desserts"
    );

    private static final List<String> DRINKS_SUB_CATEGORIES = List.of(
            "Espresso Hot", "Espresso Cold", "Refreshers"
    );

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "All", "Drinks", "Desserts"
    );

    private final HBox categoryBar;
    private final HBox drinksSubBar;
    private final FlowPane menuGrid;
    private final BiConsumer<MenuItem, Integer> onAddToCart;

    private final List<MenuItem> allItems = new ArrayList<>();
    private String activeCategory = "All";
    private String activeDrinksSubCategory = "Espresso Hot";
    private String branchName = "";

    public MenuBrowseHelper(HBox categoryBar, HBox drinksSubBar, FlowPane menuGrid,
                            BiConsumer<MenuItem, Integer> onAddToCart) {
        this.categoryBar = categoryBar;
        this.drinksSubBar = drinksSubBar;
        this.menuGrid = menuGrid;
        this.onAddToCart = onAddToCart;
    }

    public void loadForBranch(int branchId, String branchName) {
        this.branchName = branchName == null ? "" : branchName;
        allItems.clear();
        activeCategory = "All";
        activeDrinksSubCategory = "Espresso Hot";

        String sql = """
                SELECT p.product_id, p.product_name, p.product_category, p.product_description,
                       COALESCE(bp.branch_price, p.product_price) AS price,
                       bp.is_available, p.image_file
                FROM Branch_Product bp
                JOIN Product p ON bp.product_id = p.product_id
                WHERE bp.branch_id = ? AND bp.is_available = TRUE
                  AND p.product_category <> 'hookah'
                ORDER BY p.product_category, p.product_name
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    allItems.add(new MenuItem(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("product_category"),
                            rs.getDouble("price"),
                            rs.getString("product_description"),
                            rs.getBoolean("is_available"),
                            rs.getString("image_file")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadForBranchFallback(branchId);
        }

        buildCategoryBar();
        renderMenu();
    }

    private void loadForBranchFallback(int branchId) {
        allItems.clear();
        String sql = """
                SELECT p.product_id, p.product_name, p.product_category, p.product_description,
                       COALESCE(bp.branch_price, p.product_price) AS price,
                       bp.is_available
                FROM Branch_Product bp
                JOIN Product p ON bp.product_id = p.product_id
                WHERE bp.branch_id = ? AND bp.is_available = TRUE
                  AND p.product_category <> 'hookah'
                ORDER BY p.product_category, p.product_name
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    allItems.add(new MenuItem(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("product_category"),
                            rs.getDouble("price"),
                            rs.getString("product_description"),
                            rs.getBoolean("is_available"),
                            null
                    ));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clear() {
        allItems.clear();
        categoryBar.getChildren().clear();
        drinksSubBar.getChildren().clear();
        drinksSubBar.setVisible(false);
        drinksSubBar.setManaged(false);
        menuGrid.getChildren().clear();
    }

    private boolean usesRestaurantMenu() {
        String lower = branchName.toLowerCase();
        return lower.contains("tireh") || lower.contains("nablus") || lower.contains("manara");
    }

    private void buildCategoryBar() {
        categoryBar.getChildren().clear();
        categoryBar.setSpacing(0);

        List<String> preferred = usesRestaurantMenu() ? RESTAURANT_CATEGORIES : DEFAULT_CATEGORIES;
        Set<String> available = new LinkedHashSet<>();
        for (MenuItem item : allItems) {
            available.add(item.getProductCategory());
        }

        List<String> ordered = new ArrayList<>();
        for (String category : preferred) {
            if (category.equals("All")) {
                ordered.add(category);
            } else if (category.equals("Drinks") && hasDrinkItems()) {
                ordered.add(category);
            } else if (!category.equals("Drinks") && available.contains(category)) {
                ordered.add(category);
            }
        }

        for (String category : ordered) {
            Label tab = new Label(category);
            tab.setStyle(getTabStyle(category.equals(activeCategory)));
            tab.setOnMouseClicked(e -> {
                activeCategory = category;
                if (category.equals("Drinks") && !isDrinkSubCategory(activeDrinksSubCategory)) {
                    activeDrinksSubCategory = firstAvailableDrinkSubCategory();
                }
                buildCategoryBar();
                buildDrinksSubBar();
                renderMenu();
            });
            tab.setPadding(new Insets(8, 14, 8, 14));
            categoryBar.getChildren().add(tab);
        }

        buildDrinksSubBar();
    }

    private void buildDrinksSubBar() {
        drinksSubBar.getChildren().clear();
        boolean show = activeCategory.equals("Drinks") && hasDrinkItems();
        drinksSubBar.setVisible(show);
        drinksSubBar.setManaged(show);
        if (!show) {
            return;
        }

        for (String sub : DRINKS_SUB_CATEGORIES) {
            if (!hasDrinkItemsInSubCategory(sub)) {
                continue;
            }
            Label tab = new Label(sub);
            tab.setStyle(getSubTabStyle(sub.equals(activeDrinksSubCategory)));
            tab.setOnMouseClicked(e -> {
                activeDrinksSubCategory = sub;
                buildDrinksSubBar();
                renderMenu();
            });
            tab.setPadding(new Insets(6, 12, 6, 12));
            drinksSubBar.getChildren().add(tab);
        }
    }

    private void renderMenu() {
        menuGrid.getChildren().clear();

        if (allItems.isEmpty()) {
            Label empty = new Label("Select a branch to browse the menu.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #8d6e63;");
            menuGrid.getChildren().add(empty);
            return;
        }

        for (MenuItem item : allItems) {
            if (!matchesActiveCategory(item)) {
                continue;
            }
            menuGrid.getChildren().add(createMenuCard(item));
        }

        if (menuGrid.getChildren().isEmpty()) {
            Label empty = new Label("No items in this category.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #8d6e63;");
            menuGrid.getChildren().add(empty);
        }
    }

    private VBox createMenuCard(MenuItem item) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e8ddd4;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);"
        );

        StackPane imageArea = createImageArea(item);

        VBox details = new VBox(4);
        details.setPadding(new Insets(10, 12, 12, 12));

        Label category = new Label(getDisplayCategory(item));
        category.setStyle("-fx-font-size: 10px; -fx-text-fill: #a1887f; -fx-font-weight: bold;");

        Label name = new Label(item.getProductName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #3e2723;");
        name.setWrapText(true);

        Label desc = new Label(item.getProductDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #8d6e63;");

        Label price = new Label(String.format("₪ %.2f", item.getPrice()));
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6d4c41;");

        Spinner<Integer> qtySpinner = new Spinner<>();
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        qtySpinner.setPrefWidth(70);

        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(
                "-fx-background-color: #6d4c41; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;"
        );
        addBtn.setOnAction(e -> onAddToCart.accept(item, qtySpinner.getValue()));

        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actions.getChildren().addAll(qtySpinner, addBtn);
        HBox.setHgrow(addBtn, javafx.scene.layout.Priority.ALWAYS);

        details.getChildren().addAll(category, name, desc, price, actions);
        card.getChildren().addAll(imageArea, details);
        return card;
    }

    private StackPane createImageArea(MenuItem item) {
        StackPane pane = new StackPane();
        pane.setPrefHeight(110);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setStyle("-fx-background-color: #f5efe9; -fx-background-radius: 12 12 0 0;");

        URL imageUrl = findImageUrl(item);
        if (imageUrl != null) {
            ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm(), true));
            imageView.setFitWidth(220);
            imageView.setFitHeight(110);
            imageView.setPreserveRatio(true);
            pane.getChildren().add(imageView);
        } else {
            Label emoji = new Label(item.getCategoryEmoji());
            emoji.setStyle("-fx-font-size: 42px;");
            pane.getChildren().add(emoji);
        }

        return pane;
    }

    private URL findImageUrl(MenuItem item) {
        if (item.getImageFile() != null && !item.getImageFile().isBlank()) {
            URL url = getClass().getResource("images/" + item.getImageFile());
            if (url != null) {
                return url;
            }
        }

        String baseName = item.getProductName().toLowerCase()
                .replace(" & ", "_")
                .replace(" ", "_")
                .replaceAll("[^a-z0-9_]", "");
        for (String ext : new String[]{".jpg", ".jpeg", ".png"}) {
            URL url = getClass().getResource("images/" + baseName + ext);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private boolean matchesActiveCategory(MenuItem item) {
        if (activeCategory.equals("All")) {
            return true;
        }
        if (activeCategory.equals("Drinks")) {
            return item.getProductCategory().equals(activeDrinksSubCategory);
        }
        return item.getProductCategory().equals(activeCategory);
    }

    private String getDisplayCategory(MenuItem item) {
        if (isDrinkSubCategory(item.getProductCategory())) {
            return "Drinks · " + item.getProductCategory();
        }
        return item.getProductCategory();
    }

    private boolean hasDrinkItems() {
        return allItems.stream().anyMatch(item -> isDrinkSubCategory(item.getProductCategory()));
    }

    private boolean hasDrinkItemsInSubCategory(String sub) {
        return allItems.stream().anyMatch(item -> item.getProductCategory().equals(sub));
    }

    private boolean isDrinkSubCategory(String category) {
        return DRINKS_SUB_CATEGORIES.contains(category);
    }

    private String firstAvailableDrinkSubCategory() {
        for (String sub : DRINKS_SUB_CATEGORIES) {
            if (hasDrinkItemsInSubCategory(sub)) {
                return sub;
            }
        }
        return DRINKS_SUB_CATEGORIES.get(0);
    }

    private String getTabStyle(boolean active) {
        if (active) {
            return "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212121;" +
                   "-fx-border-color: transparent transparent #6d4c41 transparent;" +
                   "-fx-border-width: 0 0 3 0; -fx-cursor: hand;";
        }
        return "-fx-font-size: 13px; -fx-text-fill: #616161;" +
               "-fx-border-color: transparent; -fx-border-width: 0 0 3 0; -fx-cursor: hand;";
    }

    private String getSubTabStyle(boolean active) {
        if (active) {
            return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4e342e;" +
                   "-fx-background-color: white; -fx-background-radius: 14;" +
                   "-fx-border-color: #d7ccc8; -fx-border-radius: 14; -fx-border-width: 1; -fx-cursor: hand;";
        }
        return "-fx-font-size: 12px; -fx-text-fill: #795548;" +
               "-fx-background-color: transparent; -fx-cursor: hand;";
    }
}
