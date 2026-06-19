package com.example.vanillacoffeesystem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DeliveryController {

    @FXML private Label pageTitleLabel;
    @FXML private Label tableTitleLabel;
    @FXML private TableView<DeliveryRecord> deliveriesTable;
    @FXML private TableColumn<DeliveryRecord, String> deliveryIdColumn;
    @FXML private TableColumn<DeliveryRecord, String> orderIdColumn;
    @FXML private TableColumn<DeliveryRecord, String> branchColumn;
    @FXML private TableColumn<DeliveryRecord, String> orderDateColumn;
    @FXML private TableColumn<DeliveryRecord, String> addressColumn;
    @FXML private TableColumn<DeliveryRecord, String> statusColumn;
    @FXML private TableColumn<DeliveryRecord, String> deliveryTimeColumn;
    @FXML private Label emptyLabel;
    @FXML private VBox customerTrackingPanel;
    @FXML private Label selectedAddressLabel;
    @FXML private Label selectedStatusLabel;
    @FXML private VBox staffUpdatePanel;
    @FXML private Label staffSelectedLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button updateStatusButton;
    @FXML private Label updateErrorLabel;

    private final ObservableList<DeliveryRecord> deliveries = FXCollections.observableArrayList();
    private boolean employeeMode;

    @FXML
    public void initialize() {
        employeeMode = SessionManager.isEmployee();

        if (!SessionManager.isLoggedIn() || SessionManager.isGuest()) {
            showAlert(Alert.AlertType.WARNING, "Delivery",
                    "Please sign in to view deliveries.");
        } else if (employeeMode) {
            pageTitleLabel.setText("Manage Deliveries");
            tableTitleLabel.setText("All Delivery Orders");
            customerTrackingPanel.setVisible(false);
            customerTrackingPanel.setManaged(false);
            staffUpdatePanel.setVisible(true);
            staffUpdatePanel.setManaged(true);
        } else {
            pageTitleLabel.setText("Delivery Tracking");
            tableTitleLabel.setText("My Deliveries");
            staffUpdatePanel.setVisible(false);
            staffUpdatePanel.setManaged(false);
        }

        statusCombo.setItems(FXCollections.observableArrayList(
                "pending", "on_the_way", "delivered", "failed"
        ));

        deliveryIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getDeliveryId())));
        orderIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getOrderId())));
        branchColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBranchName()));
        orderDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOrderDate()));
        addressColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDeliveryAddress()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatStatus(data.getValue().getDeliveryStatus())));
        deliveryTimeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatTimestamp(data.getValue().getDeliveryTime())));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DeliveryRecord record = getTableView().getItems().get(getIndex());
                setText(item);
                setStyle("-fx-text-fill: " + statusColor(record.getDeliveryStatus()) + "; -fx-font-weight: bold;");
            }
        });

        deliveriesTable.setItems(deliveries);
        deliveriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) ->
                onDeliverySelected(selected));

        loadDeliveries();
    }

    private void onDeliverySelected(DeliveryRecord selected) {
        clearUpdateError();

        if (employeeMode) {
            boolean hasSelection = selected != null;
            statusCombo.setDisable(!hasSelection);
            updateStatusButton.setDisable(!hasSelection);

            if (hasSelection) {
                staffSelectedLabel.setText("Order #" + selected.getOrderId()
                        + " — " + selected.getDeliveryAddress());
                statusCombo.getSelectionModel().select(selected.getDeliveryStatus());
            } else {
                staffSelectedLabel.setText("Select a delivery to update its status.");
                statusCombo.getSelectionModel().clearSelection();
            }
            return;
        }

        if (selected == null) {
            selectedAddressLabel.setText("Select a delivery to track its status.");
            selectedStatusLabel.setText("");
            return;
        }

        selectedAddressLabel.setText("Deliver to: " + selected.getDeliveryAddress());
        String timeText = formatTimestamp(selected.getDeliveryTime());
        selectedStatusLabel.setText("Current status: " + formatStatus(selected.getDeliveryStatus())
                + (timeText.equals("—") ? "" : "  •  Last updated: " + timeText));
        selectedStatusLabel.setStyle("-fx-text-fill: " + statusColor(selected.getDeliveryStatus())
                + "; -fx-font-weight: bold; -fx-font-size: 14px;");
    }

    public void loadDeliveries() {
        deliveries.clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        onDeliverySelected(null);

        if (!SessionManager.isLoggedIn()) {
            emptyLabel.setText("Sign in to see deliveries.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        String sql;
        if (employeeMode) {
            sql = """
                    SELECT d.delivery_id, d.order_id, b.branch_name, o.order_date,
                           d.delivery_address, d.delivery_status, d.delivery_time
                    FROM Delivery d
                    JOIN Orders o ON d.order_id = o.order_id
                    JOIN Branches b ON o.branch_id = b.branch_id
                    ORDER BY d.delivery_id DESC
                    """;
        } else {
            sql = """
                    SELECT d.delivery_id, d.order_id, b.branch_name, o.order_date,
                           d.delivery_address, d.delivery_status, d.delivery_time
                    FROM Delivery d
                    JOIN Orders o ON d.order_id = o.order_id
                    JOIN Branches b ON o.branch_id = b.branch_id
                    WHERE o.customer_id = ?
                    ORDER BY d.delivery_id DESC
                    """;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!employeeMode) {
                ps.setInt(1, SessionManager.getCustomerId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    deliveries.add(new DeliveryRecord(
                            rs.getInt("delivery_id"),
                            rs.getInt("order_id"),
                            rs.getString("branch_name"),
                            rs.getString("order_date"),
                            rs.getString("delivery_address"),
                            rs.getString("delivery_status"),
                            rs.getString("delivery_time")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load deliveries.");
        }

        if (deliveries.isEmpty()) {
            emptyLabel.setText(employeeMode
                    ? "No delivery orders to manage."
                    : "No delivery orders found.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }
    }

    @FXML
    public void updateStatus() {
        if (!employeeMode) {
            return;
        }

        clearUpdateError();

        DeliveryRecord selected = deliveriesTable.getSelectionModel().getSelectedItem();
        String newStatus = statusCombo.getValue();

        if (selected == null) {
            showUpdateError("Please select a delivery first.");
            return;
        }
        if (newStatus == null || newStatus.isBlank()) {
            showUpdateError("Please choose a status.");
            return;
        }

        String sql = "UPDATE Delivery SET delivery_status = ?, delivery_time = NOW() WHERE delivery_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, selected.getDeliveryId());
            int updated = ps.executeUpdate();

            if (updated > 0) {
                loadDeliveries();
                showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                        "Delivery #" + selected.getDeliveryId() + " is now " + formatStatus(newStatus) + ".");
            } else {
                showUpdateError("Could not update delivery status.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showUpdateError("Could not update delivery status. Please try again.");
        }
    }

    @FXML
    public void backToHome() throws Exception {
        Stage stage = (Stage) deliveriesTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status.toLowerCase()) {
            case "pending" -> "Pending";
            case "on_the_way" -> "On the way";
            case "delivered" -> "Delivered";
            case "failed" -> "Failed";
            default -> status;
        };
    }

    private String statusColor(String status) {
        if (status == null) {
            return "#5d4037";
        }
        return switch (status.toLowerCase()) {
            case "pending" -> "#ef6c00";
            case "on_the_way" -> "#1976d2";
            case "delivered" -> "#2e7d32";
            case "failed" -> "#c62828";
            default -> "#5d4037";
        };
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return "—";
        }
        return timestamp.length() >= 16 ? timestamp.substring(0, 16) : timestamp;
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
