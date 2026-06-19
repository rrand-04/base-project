package vanillacoffeesystem;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// Main dashboard for customers and staff after login.
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private VBox customerSection;
    @FXML private VBox staffSection;
    @FXML private Button orderButton;
    @FXML private Button historyButton;
    @FXML private Button reservationButton;
    @FXML private Button deliveryButton;
    @FXML private Button paymentsButton;
    @FXML private Button staffDeliveryButton;
    @FXML private Button staffOrdersButton;
    @FXML private Button staffReservationsButton;

    @FXML private VBox topItemsSection;
    @FXML private ComboBox<String> periodBox;
    @FXML private HBox customRangeBar;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;
    @FXML private Button applyRangeButton;

    @FXML private ProgressIndicator storeHitProgress;
    @FXML private VBox storeHitContent;
    @FXML private Label storeHitNameLabel;
    @FXML private Label storeHitCategoryLabel;
    @FXML private Label storeHitQtyLabel;
    @FXML private Label storeHitEmptyLabel;

    @FXML private ProgressIndicator yourGoToProgress;
    @FXML private VBox yourGoToContent;
    @FXML private Label yourGoToNameLabel;
    @FXML private Label yourGoToQtyLabel;
    @FXML private Label yourGoToEmptyLabel;

    @FXML private VBox statCardsSection;
    @FXML private Label statDaysSinceValue;
    @FXML private Label statAvgOrderValue;
    @FXML private Label statUntriedItems;
    @FXML private Label statTotalSpentValue;
    @FXML private VBox couponCard;
    @FXML private Label statCouponLabel;
    @FXML private ProgressBar statCouponProgress;

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            topItemsSection.setVisible(false);
            topItemsSection.setManaged(false);
            statCardsSection.setVisible(false);
            statCardsSection.setManaged(false);
            return;
        }

        welcomeLabel.setText("Welcome, " + SessionManager.getDisplayName() + "!");

        if (SessionManager.isEmployee()) {
            roleLabel.setText("Signed in as " + SessionManager.getEmployeePosition() + " (Staff)");
            customerSection.setVisible(false);
            customerSection.setManaged(false);
            staffSection.setVisible(true);
            staffSection.setManaged(true);
            statCardsSection.setVisible(false);
            statCardsSection.setManaged(false);
        } else {
            roleLabel.setText("Signed in as Customer");
            customerSection.setVisible(true);
            customerSection.setManaged(true);
            staffSection.setVisible(false);
            staffSection.setManaged(false);
            deliveryButton.setText("Delivery Tracking");
            statCardsSection.setVisible(true);
            statCardsSection.setManaged(true);
        }

        setupTopItemsSection();
        if (!SessionManager.isEmployee()) {
            loadStatCards();
        }
    }

    private void setupTopItemsSection() {
        periodBox.setItems(FXCollections.observableArrayList(
                "Today", "Last 7 Days", "Last 30 Days", "This Month",
                "Last 3 Months", "This Year", "Custom Range"
        ));
        periodBox.getSelectionModel().select("Last 30 Days");

        setupDatePickers();

        periodBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean custom = "Custom Range".equals(newVal);
            customRangeBar.setVisible(custom);
            customRangeBar.setManaged(custom);
            if (!custom && newVal != null) {
                String[] range = getDateRange(newVal);
                loadTopItems(range[0], range[1]);
            }
        });

        String[] range = getDateRange("Last 30 Days");
        loadTopItems(range[0], range[1]);
    }

    private void setupDatePickers() {
        endPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        startPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    return;
                }
                LocalDate end = endPicker.getValue();
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
                if (end != null && date.isAfter(end)) {
                    setDisable(true);
                }
            }
        });
    }

    private String[] getDateRange(String period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "Today" -> new String[]{today.toString(), today.toString()};
            case "Last 7 Days" -> new String[]{today.minusDays(6).toString(), today.toString()};
            case "Last 30 Days" -> new String[]{today.minusDays(29).toString(), today.toString()};
            case "This Month" -> new String[]{today.withDayOfMonth(1).toString(), today.toString()};
            case "Last 3 Months" -> new String[]{today.minusDays(89).toString(), today.toString()};
            case "This Year" -> new String[]{today.withDayOfYear(1).toString(), today.toString()};
            case "Custom Range" -> {
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                yield new String[]{
                        start != null ? start.toString() : today.toString(),
                        end != null ? end.toString() : today.toString()
                };
            }
            default -> new String[]{today.minusDays(29).toString(), today.toString()};
        };
    }

    @FXML
    public void applyCustomRange() {
        LocalDate start = startPicker.getValue();
        LocalDate end = endPicker.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Custom Range",
                    "Please select both a start date and an end date.");
            return;
        }
        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Custom Range",
                    "Start date cannot be after end date.");
            return;
        }

        loadTopItems(start.toString(), end.toString());
    }

    private void loadTopItems(String startDate, String endDate) {
        showStoreHitLoading();
        showYourGoToLoading();

        Thread worker = new Thread(() -> {
            TopItemResult storeHit = queryStoreHit(startDate, endDate);
            TopItemResult yourGoTo = SessionManager.isEmployee()
                    ? null
                    : queryYourGoTo(SessionManager.getCustomerId(), startDate, endDate);

            Platform.runLater(() -> {
                displayStoreHit(storeHit);
                displayYourGoTo(yourGoTo, SessionManager.isEmployee());
            });
        });
        worker.setDaemon(true);
        worker.start();
    }

    private TopItemResult queryStoreHit(String startDate, String endDate) {
        String sql = """
                SELECT p.product_name, p.product_category,
                       SUM(oi.quantity) AS total_qty
                FROM Order_Items oi
                JOIN Orders o ON oi.order_id = o.order_id
                JOIN Product p ON oi.product_id = p.product_id
                WHERE o.order_date BETWEEN ? AND ?
                  AND o.is_active = TRUE
                GROUP BY oi.product_id, p.product_name, p.product_category
                ORDER BY total_qty DESC
                LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TopItemResult(
                            rs.getString("product_name"),
                            rs.getString("product_category"),
                            rs.getInt("total_qty")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TopItemResult queryYourGoTo(int customerId, String startDate, String endDate) {
        String sql = """
                SELECT p.product_name, p.product_category,
                       SUM(oi.quantity) AS total_qty
                FROM Order_Items oi
                JOIN Orders o ON oi.order_id = o.order_id
                JOIN Product p ON oi.product_id = p.product_id
                WHERE o.customer_id = ?
                  AND o.order_date BETWEEN ? AND ?
                  AND o.is_active = TRUE
                GROUP BY oi.product_id, p.product_name, p.product_category
                ORDER BY total_qty DESC
                LIMIT 1
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setString(2, startDate);
            ps.setString(3, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TopItemResult(
                            rs.getString("product_name"),
                            rs.getString("product_category"),
                            rs.getInt("total_qty")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showStoreHitLoading() {
        storeHitProgress.setVisible(true);
        storeHitProgress.setManaged(true);
        storeHitContent.setVisible(false);
        storeHitContent.setManaged(false);
        storeHitEmptyLabel.setVisible(false);
        storeHitEmptyLabel.setManaged(false);
    }

    private void showYourGoToLoading() {
        yourGoToProgress.setVisible(true);
        yourGoToProgress.setManaged(true);
        yourGoToContent.setVisible(false);
        yourGoToContent.setManaged(false);
        yourGoToEmptyLabel.setVisible(false);
        yourGoToEmptyLabel.setManaged(false);
    }

    private void displayStoreHit(TopItemResult result) {
        storeHitProgress.setVisible(false);
        storeHitProgress.setManaged(false);

        if (result == null) {
            storeHitContent.setVisible(false);
            storeHitContent.setManaged(false);
            storeHitEmptyLabel.setText("No orders in this period yet.");
            storeHitEmptyLabel.setVisible(true);
            storeHitEmptyLabel.setManaged(true);
            return;
        }

        storeHitNameLabel.setText(result.productName());
        storeHitCategoryLabel.setText(result.productCategory());
        storeHitQtyLabel.setText("Ordered " + result.totalQty() + " times");
        storeHitEmptyLabel.setVisible(false);
        storeHitEmptyLabel.setManaged(false);
        storeHitContent.setVisible(true);
        storeHitContent.setManaged(true);
    }

    private void displayYourGoTo(TopItemResult result, boolean employee) {
        yourGoToProgress.setVisible(false);
        yourGoToProgress.setManaged(false);

        if (employee) {
            yourGoToContent.setVisible(false);
            yourGoToContent.setManaged(false);
            yourGoToEmptyLabel.setText("Personal stats are available for customer accounts.");
            yourGoToEmptyLabel.setVisible(true);
            yourGoToEmptyLabel.setManaged(true);
            return;
        }

        if (result == null) {
            yourGoToContent.setVisible(false);
            yourGoToContent.setManaged(false);
            yourGoToEmptyLabel.setText("No orders in this period yet.");
            yourGoToEmptyLabel.setVisible(true);
            yourGoToEmptyLabel.setManaged(true);
            return;
        }

        yourGoToNameLabel.setText(result.productName());
        yourGoToQtyLabel.setText("You ordered this " + result.totalQty() + " times");
        yourGoToEmptyLabel.setVisible(false);
        yourGoToEmptyLabel.setManaged(false);
        yourGoToContent.setVisible(true);
        yourGoToContent.setManaged(true);
    }

    private void loadStatCards() {
        int customerId = SessionManager.getCustomerId();

        Thread worker = new Thread(() -> {
            StatCardResult stats = queryStatCards(customerId);
            Platform.runLater(() -> displayStatCards(stats));
        });
        worker.setDaemon(true);
        worker.start();
    }

    private StatCardResult queryStatCards(int customerId) {
        String daysSql = """
                SELECT DATEDIFF(CURDATE(), MAX(order_date)) AS days_ago
                FROM Orders WHERE customer_id = ? AND is_active = TRUE
                """;
        String avgSql = """
                SELECT ROUND(AVG(total_price), 2) AS avg_val
                FROM Orders WHERE customer_id = ? AND is_active = TRUE
                """;
        String untriedSql = """
                SELECT COUNT(*) AS untried FROM Product p
                WHERE p.product_id NOT IN (
                    SELECT DISTINCT oi.product_id
                    FROM Order_Items oi
                    JOIN Orders o ON oi.order_id = o.order_id
                    WHERE o.customer_id = ? AND o.is_active = TRUE
                )
                """;
        String lifetimeSql = """
                SELECT COALESCE(SUM(total_price), 0) AS lifetime
                FROM Orders WHERE customer_id = ? AND is_active = TRUE
                """;
        String couponSql = """
                SELECT GREATEST(0, 100 - COALESCE(SUM(total_price), 0)) AS remaining
                FROM Orders
                WHERE customer_id = ?
                  AND order_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
                  AND is_active = TRUE
                """;

        Integer daysAgo = null;
        Double avgVal = null;
        int untried = 0;
        double lifetime = 0;
        Double couponRemaining = null;
        boolean couponSupported = tableExists("Coupon");

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(daysSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getObject("days_ago") != null) {
                        daysAgo = rs.getInt("days_ago");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(avgSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getObject("avg_val") != null) {
                        avgVal = rs.getDouble("avg_val");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(untriedSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        untried = rs.getInt("untried");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(lifetimeSql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lifetime = rs.getDouble("lifetime");
                    }
                }
            }

            if (couponSupported) {
                try (PreparedStatement ps = con.prepareStatement(couponSql)) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            couponRemaining = rs.getDouble("remaining");
                        }
                    }
                } catch (SQLException e) {
                    couponSupported = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StatCardResult(daysAgo, avgVal, untried, lifetime, couponRemaining, couponSupported);
    }

    private boolean tableExists(String tableName) {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'vanilla_db' AND TABLE_NAME = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void displayStatCards(StatCardResult stats) {
        if (stats.daysAgo() == null) {
            statDaysSinceValue.setText("No orders yet");
        } else {
            statDaysSinceValue.setText(stats.daysAgo() + " days ago");
        }

        if (stats.avgVal() == null) {
            statAvgOrderValue.setText("—");
        } else {
            statAvgOrderValue.setText(String.format("%.2f ILS", stats.avgVal()));
        }

        statUntriedItems.setText(stats.untried() + " items — try something new!");
        statTotalSpentValue.setText(String.format("%.2f ILS", stats.lifetime()));

        if (!stats.couponSupported() || stats.couponRemaining() == null) {
            couponCard.setVisible(false);
            couponCard.setManaged(false);
            return;
        }

        couponCard.setVisible(true);
        couponCard.setManaged(true);

        if (stats.couponRemaining() <= 0) {
            statCouponLabel.setText("🎉 Coupon earned this month!");
            statCouponProgress.setVisible(false);
            statCouponProgress.setManaged(false);
        } else {
            double spentThisMonth = 100.0 - stats.couponRemaining();
            statCouponProgress.setProgress(Math.min(1.0, spentThisMonth / 100.0));
            statCouponProgress.setVisible(true);
            statCouponProgress.setManaged(true);
            statCouponLabel.setText(String.format(
                    "Spend %.2f more ILS to earn a coupon", stats.couponRemaining()));
        }
    }

    @FXML
    public void openOrderHistory() throws Exception {
        navigate("order-history-view.fxml", "Vanilla Coffee - My Orders");
    }

    @FXML
    public void openPlaceOrder() throws Exception {
        navigate("order-placement-view.fxml", "Vanilla Coffee - Place Order");
    }

    @FXML
    public void openReservations() throws Exception {
        navigate("reservation-view.fxml", "Vanilla Coffee - Reservations");
    }

    @FXML
    public void openPayments() throws Exception {
        navigate("payment-view.fxml", "Vanilla Coffee - Payment History");
    }

    @FXML
    public void openCharts() throws Exception {
        navigate("chart-view.fxml", "Vanilla Coffee - Charts & Reports");
    }

    @FXML
    public void openDeliveries() throws Exception {
        navigate("delivery-view.fxml", SessionManager.isEmployee()
                ? "Vanilla Coffee - Manage Deliveries"
                : "Vanilla Coffee - Delivery Tracking");
    }

    @FXML
    public void openEmployeeOrders() throws Exception {
        navigate("employee-orders-view.fxml", "Vanilla Coffee - Manage Orders");
    }

    @FXML
    public void openEmployeeReservations() throws Exception {
        navigate("reservation-view.fxml", "Vanilla Coffee - Manage Reservations");
    }

    @FXML
    public void openMainApp() throws Exception {
        navigate("home-view.fxml", "Vanilla Coffee");
    }

    @FXML
    public void handleLogout() throws Exception {
        SessionManager.clear();
        navigate("home-view.fxml", "Vanilla Coffee");
    }

    private void navigate(String fxml, String title) throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml(fxml)));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle(title);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record TopItemResult(String productName, String productCategory, int totalQty) {
    }

    private record StatCardResult(
            Integer daysAgo,
            Double avgVal,
            int untried,
            double lifetime,
            Double couponRemaining,
            boolean couponSupported
    ) {
    }
}
