package vanillacoffeesystem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

// Customer order history with cancel and product review support.
public class OrderHistoryController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private CheckBox showCancelledCheckBox;
    @FXML private TableView<OrderHistoryRecord> ordersTable;
    @FXML private TableColumn<OrderHistoryRecord, String> orderIdColumn;
    @FXML private TableColumn<OrderHistoryRecord, String> orderDateColumn;
    @FXML private TableColumn<OrderHistoryRecord, String> branchColumn;
    @FXML private TableColumn<OrderHistoryRecord, String> statusColumn;
    @FXML private TableColumn<OrderHistoryRecord, String> totalColumn;
    @FXML private HBox cancelPanel;
    @FXML private TextField cancelReasonField;
    @FXML private Button cancelOrderButton;
    @FXML private VBox detailPanel;
    @FXML private Label detailHeaderLabel;
    @FXML private Label paymentInfoLabel;
    @FXML private ListView<OrderItemDetail> itemsListView;
    @FXML private VBox reviewPanel;
    @FXML private ComboBox<OrderItemDetail> reviewProductCombo;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private TextField reviewCommentField;
    @FXML private Button submitReviewButton;
    @FXML private Label reviewHintLabel;
    @FXML private Label emptyLabel;

    private final ObservableList<OrderHistoryRecord> orders = FXCollections.observableArrayList();
    private final ObservableList<OrderItemDetail> orderItems = FXCollections.observableArrayList();
    private OrderHistoryRecord selectedOrderForReview;

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee() || SessionManager.isGuest()) {
            showAlert(Alert.AlertType.WARNING, "Order History", "Please sign in as a customer to view your orders.");
        }

        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Completed", "Cancelled"));
        statusFilterCombo.getSelectionModel().select("All");
        showCancelledCheckBox.setSelected(false);

        orderIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getOrderId())));
        orderDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderDate()));
        branchColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBranchName()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderStatus()));
        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getTotalPrice())));

        applyCancelledCellStyle(orderIdColumn);
        applyCancelledCellStyle(orderDateColumn);
        applyCancelledCellStyle(branchColumn);
        applyCancelledCellStyle(statusColumn);
        applyCancelledCellStyle(totalColumn);

        ordersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(OrderHistoryRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (!item.isActive()) {
                    setStyle("-fx-background-color: #f5f0eb; -fx-opacity: 0.9;");
                } else {
                    setStyle("");
                }
            }
        });

        ordersTable.setItems(orders);
        itemsListView.setItems(orderItems);

        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 5));
        reviewProductCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(OrderItemDetail item) {
                return item == null ? "" : item.getDisplayName();
            }

            @Override
            public OrderItemDetail fromString(String string) {
                return null;
            }
        });
        reviewProductCombo.setOnAction(e -> onReviewProductSelected());

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            updateCancelUi(selected);
            if (selected != null) {
                onOrderSelected(selected);
            } else {
                hideDetailPanel();
            }
        });

        loadOrders();
    }

    private void applyCancelledCellStyle(TableColumn<OrderHistoryRecord, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                OrderHistoryRecord record = getTableRow() != null ? getTableRow().getItem() : null;
                if (record != null && !record.isActive()) {
                    Text text = new Text(item);
                    text.setStrikethrough(true);
                    text.setFill(Color.GRAY);
                    setGraphic(text);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(item);
                }
            }
        });
    }

    private void updateCancelUi(OrderHistoryRecord selected) {
        boolean canCancel = selected != null && selected.isPending();
        cancelOrderButton.setDisable(!canCancel);
        cancelPanel.setVisible(canCancel);
        cancelPanel.setManaged(canCancel);
        if (!canCancel) {
            cancelReasonField.clear();
        }
    }

    public void loadOrders() {
        orders.clear();
        hideDetailPanel();
        updateCancelUi(null);
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee()) {
            emptyLabel.setText("Sign in as a customer to see your order history.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        boolean showCancelled = showCancelledCheckBox.isSelected();
        String status = statusFilterCombo.getValue();

        StringBuilder sql = new StringBuilder("""
                SELECT o.order_id, o.order_date, o.order_status, o.total_price,
                       b.branch_name, o.is_active
                FROM Orders o
                JOIN Branches b ON o.branch_id = b.branch_id
                WHERE o.customer_id = ?
                  AND (o.is_active = TRUE OR ? = TRUE)
                """);

        if (status != null && !status.equals("All")) {
            sql.append(" AND o.order_status = ?");
        }
        sql.append(" ORDER BY o.order_date DESC, o.order_id DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int param = 1;
            ps.setInt(param++, SessionManager.getCustomerId());
            ps.setBoolean(param++, showCancelled);
            if (status != null && !status.equals("All")) {
                ps.setString(param, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(new OrderHistoryRecord(
                            rs.getInt("order_id"),
                            rs.getString("order_date"),
                            rs.getString("order_status"),
                            rs.getDouble("total_price"),
                            rs.getString("branch_name"),
                            rs.getBoolean("is_active")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load orders. Please try again.");
        }

        if (orders.isEmpty()) {
            emptyLabel.setText("No orders found.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }
    }

    @FXML
    public void cancelOrder() {
        OrderHistoryRecord selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isPending()) {
            return;
        }

        String reason = cancelReasonField.getText() == null ? "" : cancelReasonField.getText().trim();
        if (reason.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cancel Order", "Please enter a cancellation reason.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Cancel order #" + selected.getOrderId() + "?");
        confirm.setContentText("This will mark the order as cancelled. It will be kept for your records.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String sql = """
                UPDATE Orders
                SET is_active = FALSE, order_status = 'Cancelled', cancelled_reason = ?
                WHERE order_id = ? AND customer_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, reason);
            ps.setInt(2, selected.getOrderId());
            ps.setInt(3, SessionManager.getCustomerId());

            int updated = ps.executeUpdate();
            if (updated > 0) {
                cancelReasonField.clear();
                loadOrders();
                showAlert(Alert.AlertType.INFORMATION, "Order Cancelled",
                        "Order #" + selected.getOrderId() + " has been cancelled.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Cancel Failed", "Could not cancel this order.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Cancel Failed", "Could not cancel order: " + e.getMessage());
        }
    }

    private void onOrderSelected(OrderHistoryRecord order) {
        orderItems.clear();
        selectedOrderForReview = order;
        detailHeaderLabel.setText("Order #" + order.getOrderId() + " — Items");
        paymentInfoLabel.setText(loadPaymentInfo(order.getOrderId()));

        String sql = """
                SELECT p.product_id, p.product_name, oi.quantity, oi.price
                FROM Order_Items oi
                JOIN Product p ON oi.product_id = p.product_id
                WHERE oi.order_id = ?
                ORDER BY p.product_name
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, order.getOrderId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItemDetail item = new OrderItemDetail(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    );
                    Integer rating = ReviewHelper.getRating(SessionManager.getCustomerId(), item.getProductId());
                    item.setExistingRating(rating);
                    orderItems.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load order items.");
            return;
        }

        detailPanel.setVisible(true);
        detailPanel.setManaged(true);
        setupReviewPanel(order);
    }

    private void setupReviewPanel(OrderHistoryRecord order) {
        reviewPanel.setVisible(false);
        reviewPanel.setManaged(false);

        if (!ReviewHelper.isAvailable()) {
            return;
        }

        boolean completed = order.isActive()
                && "Completed".equalsIgnoreCase(order.getOrderStatus());

        if (!completed || orderItems.isEmpty()) {
            if (order.isActive() && "Pending".equalsIgnoreCase(order.getOrderStatus())) {
                reviewHintLabel.setText("You can rate items after the order is completed.");
                reviewPanel.setVisible(true);
                reviewPanel.setManaged(true);
                reviewProductCombo.setDisable(true);
                submitReviewButton.setDisable(true);
                ratingSpinner.setDisable(true);
                reviewCommentField.setDisable(true);
            }
            return;
        }

        reviewProductCombo.setItems(FXCollections.observableArrayList(orderItems));
        reviewProductCombo.getSelectionModel().selectFirst();
        reviewProductCombo.setDisable(false);
        ratingSpinner.setDisable(false);
        reviewCommentField.setDisable(false);
        submitReviewButton.setDisable(false);
        reviewHintLabel.setText("Rate products from this completed order (1–5 stars).");
        onReviewProductSelected();
        reviewPanel.setVisible(true);
        reviewPanel.setManaged(true);
    }

    private void onReviewProductSelected() {
        OrderItemDetail item = reviewProductCombo.getValue();
        if (item == null) {
            return;
        }
        int rating = item.getExistingRating() != null ? item.getExistingRating() : 5;
        ratingSpinner.getValueFactory().setValue(rating);
    }

    @FXML
    public void submitReview() {
        if (selectedOrderForReview == null || !ReviewHelper.isAvailable()) {
            return;
        }

        OrderItemDetail item = reviewProductCombo.getValue();
        if (item == null) {
            showAlert(Alert.AlertType.WARNING, "Review", "Please select a product to rate.");
            return;
        }

        int rating = ratingSpinner.getValue();
        if (rating < 1 || rating > 5) {
            showAlert(Alert.AlertType.WARNING, "Review", "Rating must be between 1 and 5.");
            return;
        }

        boolean saved = ReviewHelper.saveReview(
                SessionManager.getCustomerId(),
                item.getProductId(),
                selectedOrderForReview.getOrderId(),
                rating,
                reviewCommentField.getText()
        );

        if (saved) {
            item.setExistingRating(rating);
            reviewProductCombo.setItems(FXCollections.observableArrayList(orderItems));
            reviewProductCombo.getSelectionModel().select(item);
            reviewCommentField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Review Saved",
                    "Thanks! Your rating for " + item.getProductName() + " was saved.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Review", "Could not save your review. Please try again.");
        }
    }

    private String loadPaymentInfo(int orderId) {
        String sql = """
                SELECT payment_method, payment_amount, payment_date, payment_status
                FROM Payment
                WHERE order_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format(
                            "Payment: ₪ %.2f via %s — %s on %s",
                            rs.getDouble("payment_amount"),
                            rs.getString("payment_method"),
                            rs.getString("payment_status"),
                            rs.getString("payment_date")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Payment: not recorded yet.";
    }

    @FXML
    public void filterByStatus() {
        loadOrders();
    }

    @FXML
    public void toggleShowCancelled() {
        loadOrders();
    }

    @FXML
    public void backToHome() throws Exception {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    private void hideDetailPanel() {
        orderItems.clear();
        selectedOrderForReview = null;
        paymentInfoLabel.setText("");
        reviewPanel.setVisible(false);
        reviewPanel.setManaged(false);
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
