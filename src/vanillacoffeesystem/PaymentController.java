package vanillacoffeesystem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Payment history for the logged-in customer.
public class PaymentController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> methodFilterCombo;
    @FXML private TableView<PaymentRecord> paymentsTable;
    @FXML private TableColumn<PaymentRecord, String> paymentIdColumn;
    @FXML private TableColumn<PaymentRecord, String> orderIdColumn;
    @FXML private TableColumn<PaymentRecord, String> branchColumn;
    @FXML private TableColumn<PaymentRecord, String> methodColumn;
    @FXML private TableColumn<PaymentRecord, String> amountColumn;
    @FXML private TableColumn<PaymentRecord, String> dateColumn;
    @FXML private TableColumn<PaymentRecord, String> statusColumn;
    @FXML private Label emptyLabel;
    @FXML private Label totalPaidLabel;

    private final ObservableList<PaymentRecord> payments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee() || SessionManager.isGuest()) {
            showAlert(Alert.AlertType.WARNING, "Payment History",
                    "Please sign in as a customer to view your payments.");
        }

        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "Paid", "Pending", "Failed"));
        statusFilterCombo.getSelectionModel().select("All");
        methodFilterCombo.setItems(FXCollections.observableArrayList("All", "Cash", "Card"));
        methodFilterCombo.getSelectionModel().select("All");

        paymentIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getPaymentId())));
        orderIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getOrderId())));
        branchColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBranchName()));
        methodColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMethod()));
        amountColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPaymentAmount())));
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentDate()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentStatus()));

        paymentsTable.setItems(payments);
        loadPayments();
        loadTotal();
    }

    public void loadPayments() {
        payments.clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee()) {
            emptyLabel.setText("Sign in as a customer to see your payment history.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        String status = statusFilterCombo.getValue();
        String method = methodFilterCombo.getValue();

        StringBuilder sql = new StringBuilder("""
                SELECT pm.payment_id, pm.order_id, b.branch_name, pm.payment_method,
                       pm.payment_amount, pm.payment_date, pm.payment_status
                FROM Payment pm
                JOIN Orders o ON pm.order_id = o.order_id
                JOIN Branches b ON o.branch_id = b.branch_id
                WHERE o.customer_id = ?
                """);

        if (status != null && !status.equals("All")) {
            sql.append(" AND pm.payment_status = ?");
        }
        if ("Cash".equals(method)) {
            sql.append(" AND pm.payment_method = 'Cash'");
        } else if ("Card".equals(method)) {
            sql.append(" AND (pm.payment_method = 'Card' OR pm.payment_method LIKE '%Card%')");
        }
        sql.append(" ORDER BY pm.payment_date DESC, pm.payment_id DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, SessionManager.getCustomerId());
            if (status != null && !status.equals("All")) {
                ps.setString(paramIndex, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(new PaymentRecord(
                            rs.getInt("payment_id"),
                            rs.getInt("order_id"),
                            rs.getString("branch_name"),
                            rs.getString("payment_method"),
                            rs.getDouble("payment_amount"),
                            rs.getString("payment_date"),
                            rs.getString("payment_status")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load payments.");
        }

        if (payments.isEmpty()) {
            emptyLabel.setText("No payments found.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }
    }

    public void loadTotal() {
        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee()) {
            totalPaidLabel.setText("Total Paid: ₪ 0.00");
            return;
        }

        String sql = """
                SELECT COALESCE(SUM(pm.payment_amount), 0) AS total_paid
                FROM Payment pm
                JOIN Orders o ON pm.order_id = o.order_id
                WHERE o.customer_id = ? AND pm.payment_status = 'Paid'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getCustomerId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalPaidLabel.setText(String.format("Total Paid: ₪ %.2f", rs.getDouble("total_paid")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            totalPaidLabel.setText("Total Paid: ₪ 0.00");
        }
    }

    @FXML
    public void filterByStatus() {
        loadPayments();
    }

    @FXML
    public void filterByMethod() {
        loadPayments();
    }

    @FXML
    public void backToHome() throws Exception {
        Stage stage = (Stage) paymentsTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
