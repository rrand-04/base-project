package com.example.vanillacoffeesystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button orderButton;
    @FXML private Button historyButton;
    @FXML private Button reservationButton;
    @FXML private Button deliveryButton;
    @FXML private Button paymentsButton;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + SessionManager.getDisplayName() + "!");
            if (SessionManager.isEmployee()) {
                roleLabel.setText("Signed in as " + SessionManager.getEmployeePosition() + " (Staff)");
                orderButton.setVisible(false);
                orderButton.setManaged(false);
                historyButton.setVisible(false);
                historyButton.setManaged(false);
                reservationButton.setVisible(false);
                reservationButton.setManaged(false);
                deliveryButton.setText("Manage Deliveries");
                paymentsButton.setVisible(false);
                paymentsButton.setManaged(false);
            } else {
                roleLabel.setText("Signed in as Customer");
                deliveryButton.setText("Deliveries");
            }
        }
    }

    @FXML
    public void openOrderHistory() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("order-history-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee - My Orders");
        stage.show();
    }

    @FXML
    public void openPlaceOrder() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("order-placement-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee - Place Order");
        stage.show();
    }

    @FXML
    public void openReservations() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("reservation-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee - Reservations");
        stage.show();
    }

    @FXML
    public void openPayments() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("payment-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee - Payment History");
        stage.show();
    }

    @FXML
    public void openDeliveries() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("delivery-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle(SessionManager.isEmployee()
                ? "Vanilla Coffee - Manage Deliveries"
                : "Vanilla Coffee - Delivery Tracking");
        stage.show();
    }

    @FXML
    public void openMainApp() throws Exception {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    @FXML
    public void handleLogout() throws Exception {
        SessionManager.clear();
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }
}
