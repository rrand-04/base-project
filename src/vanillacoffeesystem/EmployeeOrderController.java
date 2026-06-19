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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

// Staff screen for updating pending orders.
public class EmployeeOrderController {

    @FXML private CheckBox showInactiveCheckBox;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<EmployeeOrderRecord> ordersTable;
    @FXML private TableColumn<EmployeeOrderRecord, String> orderIdColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> orderDateColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> customerColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> branchColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> typeColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> statusColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> deliveryStatusColumn;
    @FXML private TableColumn<EmployeeOrderRecord, String> totalColumn;
    @FXML private Label emptyLabel;
    @FXML private Label selectedOrderLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button updateStatusButton;
    @FXML private Label updateErrorLabel;

    private final ObservableList<EmployeeOrderRecord> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn() || !SessionManager.isEmployee()) {
            showAlert(Alert.AlertType.WARNING, "Manage Orders",
                    "Please sign in as an employee to manage orders.");
        }

        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Completed", "Cancelled"));
        statusFilterCombo.getSelectionModel().select("All");
        showInactiveCheckBox.setSelected(false);

        statusCombo.setItems(FXCollections.observableArrayList("Completed", "Cancelled"));

        orderIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getOrderId())));
        orderDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderDate()));
        customerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName()));
        branchColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBranchName()));
        typeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderType()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderStatus()));
        deliveryStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDeliveryStatus(data.getValue().getDeliveryStatus())));
        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getTotalPrice())));

        ordersTable.setItems(orders);
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) ->
                onOrderSelected(selected));

        loadOrders();
    }

    private void onOrderSelected(EmployeeOrderRecord selected) {
        clearUpdateError();

        boolean canUpdate = selected != null && selected.isPending();
        statusCombo.setDisable(!canUpdate);
        updateStatusButton.setDisable(!canUpdate);

        if (selected == null) {
            selectedOrderLabel.setText("Select a pending order to update its status.");
            statusCombo.getSelectionModel().clearSelection();
            return;
        }

        selectedOrderLabel.setText(String.format(
                "Order #%d — %s at %s (%s) — ₪ %.2f",
                selected.getOrderId(),
                selected.getCustomerName(),
                selected.getBranchName(),
                selected.getOrderType(),
                selected.getTotalPrice()
        ));

        if (canUpdate) {
            statusCombo.getSelectionModel().select("Completed");
        }
    }

    @FXML
    public void loadOrders() {
        orders.clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        onOrderSelected(null);

        if (!SessionManager.isLoggedIn() || !SessionManager.isEmployee()) {
            emptyLabel.setText("Sign in as an employee to manage orders.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        boolean showInactive = showInactiveCheckBox.isSelected();
        String statusFilter = statusFilterCombo.getValue();

        StringBuilder sql = new StringBuilder("""
                SELECT o.order_id, o.order_date, o.order_status, o.total_price, o.is_active,
                       c.customer_name, b.branch_name,
                       CASE
                           WHEN d.delivery_id IS NOT NULL THEN 'Delivery'
                           WHEN o.table_id IS NOT NULL THEN 'Dine-in'
                           ELSE 'Pickup'
                       END AS order_type,
                       d.delivery_status
                FROM Orders o
                JOIN Customers c ON o.customer_id = c.customer_id
                JOIN Branches b ON o.branch_id = b.branch_id
                LEFT JOIN Delivery d ON d.order_id = o.order_id
                WHERE (o.is_active = TRUE OR ? = TRUE)
                """);

        if (statusFilter != null && !statusFilter.equals("All")) {
            sql.append(" AND o.order_status = ?");
        }
        sql.append(" ORDER BY o.order_date DESC, o.order_id DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int param = 1;
            ps.setBoolean(param++, showInactive);
            if (statusFilter != null && !statusFilter.equals("All")) {
                ps.setString(param, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(new EmployeeOrderRecord(
                            rs.getInt("order_id"),
                            rs.getString("order_date"),
                            rs.getString("customer_name"),
                            rs.getString("branch_name"),
                            rs.getString("order_status"),
                            rs.getDouble("total_price"),
                            rs.getString("order_type"),
                            rs.getBoolean("is_active"),
                            rs.getString("delivery_status")
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
    public void updateOrderStatus() {
        clearUpdateError();

        EmployeeOrderRecord selected = ordersTable.getSelectionModel().getSelectedItem();
        String newStatus = statusCombo.getValue();

        if (selected == null || !selected.isPending()) {
            showUpdateError("Please select a pending order first.");
            return;
        }
        if (newStatus == null || newStatus.isBlank()) {
            showUpdateError("Please choose a status.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Update Order");
        confirm.setHeaderText("Mark order #" + selected.getOrderId() + " as " + newStatus + "?");
        confirm.setContentText("Customer: " + selected.getCustomerName());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if ("Completed".equalsIgnoreCase(newStatus)) {
                    completeOrder(con, selected.getOrderId());
                } else if ("Cancelled".equalsIgnoreCase(newStatus)) {
                    cancelOrder(con, selected.getOrderId());
                } else {
                    showUpdateError("Invalid status.");
                    return;
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

            loadOrders();
            showAlert(Alert.AlertType.INFORMATION, "Order Updated",
                    "Order #" + selected.getOrderId() + " is now " + newStatus + ".");

        } catch (Exception e) {
            e.printStackTrace();
            showUpdateError("Could not update order. Please try again.");
        }
    }

    private void completeOrder(Connection con, int orderId) throws Exception {
        String orderSql = """
                UPDATE Orders
                SET order_status = 'Completed'
                WHERE order_id = ? AND is_active = TRUE AND order_status = 'Pending'
                """;
        try (PreparedStatement ps = con.prepareStatement(orderSql)) {
            ps.setInt(1, orderId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Order could not be completed.");
            }
        }

        String deliverySql = """
                UPDATE Delivery
                SET delivery_status = 'delivered', delivery_time = NOW()
                WHERE order_id = ? AND delivery_status <> 'delivered'
                """;
        try (PreparedStatement ps = con.prepareStatement(deliverySql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private void cancelOrder(Connection con, int orderId) throws Exception {
        String orderSql = """
                UPDATE Orders
                SET order_status = 'Cancelled', is_active = FALSE,
                    cancelled_reason = 'Cancelled by staff'
                WHERE order_id = ? AND is_active = TRUE AND order_status = 'Pending'
                """;
        try (PreparedStatement ps = con.prepareStatement(orderSql)) {
            ps.setInt(1, orderId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException("Order could not be cancelled.");
            }
        }
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

    private String formatDeliveryStatus(String status) {
        if (status == null || status.isBlank()) {
            return "—";
        }
        return switch (status.toLowerCase()) {
            case "pending" -> "Pending";
            case "on_the_way" -> "On the way";
            case "delivered" -> "Delivered";
            case "failed" -> "Failed";
            default -> status;
        };
    }

    private void showUpdateError(String message) {
        updateErrorLabel.setText(message);
        updateErrorLabel.setVisible(true);
        updateErrorLabel.setManaged(true);
    }

    private void clearUpdateError() {
        updateErrorLabel.setText("");
        updateErrorLabel.setVisible(false);
        updateErrorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
