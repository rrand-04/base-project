package com.example.vanillacoffeesystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChartController {

    @FXML private Label subtitleLabel;
    @FXML private BarChart<String, Number> monthlyOrdersChart;
    @FXML private PieChart spendingByCategoryChart;
    @FXML private PieChart orderStatusChart;

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn() || SessionManager.isEmployee() || SessionManager.isGuest()) {
            subtitleLabel.setText("Sign in as a customer to view your order charts.");
            showAlert(Alert.AlertType.WARNING, "Charts & Reports",
                    "Please sign in as a customer to view your order charts.");
            return;
        }

        subtitleLabel.setText("Reports for " + SessionManager.getDisplayName());
        loadMonthlyOrdersChart();
        loadSpendingByCategoryChart();
        loadOrderStatusChart();
    }

    private void loadMonthlyOrdersChart() {
        monthlyOrdersChart.getData().clear();
        monthlyOrdersChart.setTitle("Orders per Month");
        monthlyOrdersChart.setLegendVisible(false);

        String sql = """
                SELECT DATE_FORMAT(order_date, '%Y-%m') AS month, COUNT(*) AS total
                FROM Orders
                WHERE customer_id = ?
                GROUP BY month
                ORDER BY month
                """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getCustomerId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(
                            rs.getString("month"),
                            rs.getInt("total")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Charts", "Could not load monthly orders chart.");
        }

        monthlyOrdersChart.getData().add(series);
    }

    private void loadSpendingByCategoryChart() {
        spendingByCategoryChart.getData().clear();
        spendingByCategoryChart.setTitle("Spending by Product Category");
        spendingByCategoryChart.setLegendVisible(true);

        String sql = """
                SELECT p.product_category, SUM(oi.price * oi.quantity) AS total_spent
                FROM Order_Items oi
                JOIN Orders o ON oi.order_id = o.order_id
                JOIN Product p ON oi.product_id = p.product_id
                WHERE o.customer_id = ?
                GROUP BY p.product_category
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getCustomerId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("product_category");
                    double spent = rs.getDouble("total_spent");
                    spendingByCategoryChart.getData().add(new PieChart.Data(
                            category + String.format(" (₪%.2f)", spent),
                            spent
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Charts", "Could not load spending by category chart.");
        }
    }

    private void loadOrderStatusChart() {
        orderStatusChart.getData().clear();
        orderStatusChart.setTitle("Orders by Status");
        orderStatusChart.setLegendVisible(true);

        String sql = """
                SELECT order_status, COUNT(*) AS cnt
                FROM Orders
                WHERE customer_id = ?
                GROUP BY order_status
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getCustomerId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("order_status");
                    int count = rs.getInt("cnt");
                    orderStatusChart.getData().add(new PieChart.Data(
                            status + " (" + count + ")",
                            count
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Charts", "Could not load order status chart.");
        }
    }

    @FXML
    public void backToHome() throws Exception {
        navigate("home-view.fxml", "Vanilla Coffee");
    }

    @FXML
    public void backToDashboard() throws Exception {
        navigate("dashboard-view.fxml", "Vanilla Coffee - Dashboard");
    }

    private void navigate(String fxml, String title) throws Exception {
        Stage stage = (Stage) subtitleLabel.getScene().getWindow();
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
}
