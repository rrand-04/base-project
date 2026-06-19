package vanillacoffeesystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Home page with branch list and navigation for guests and logged-in users.
public class HomeController {

    @FXML private HBox authBox;
    @FXML private FlowPane branchPane;

    @FXML
    public void initialize() {
        buildAuthBar();
        loadBranches();
    }

    private void buildAuthBar() {
        authBox.getChildren().clear();

        if (SessionManager.isLoggedIn()) {
            Label welcome = new Label("Hi, " + SessionManager.getDisplayName());
            welcome.setStyle("-fx-font-size: 13px; -fx-text-fill: #5d4037;");
            if (SessionManager.isEmployee()) {
                welcome.setText("Hi, " + SessionManager.getDisplayName() + " (" + SessionManager.getEmployeePosition() + ")");
            }

            Button logoutBtn = new Button("Logout");
            styleOutlineButton(logoutBtn);
            logoutBtn.setOnAction(e -> handleLogout());

            if (!SessionManager.isEmployee()) {
                Button dashboardBtn = new Button("Dashboard");
                styleOutlineButton(dashboardBtn);
                dashboardBtn.setOnAction(e -> openDashboard());

                Button orderBtn = new Button("Place Order");
                stylePrimaryButton(orderBtn);
                orderBtn.setOnAction(e -> openPlaceOrder());

                Button historyBtn = new Button("My Orders");
                styleOutlineButton(historyBtn);
                historyBtn.setOnAction(e -> openOrderHistory());

                Button reservationBtn = new Button("Reservations");
                styleOutlineButton(reservationBtn);
                reservationBtn.setOnAction(e -> openReservations());

                Button deliveryBtn = new Button("Deliveries");
                styleOutlineButton(deliveryBtn);
                deliveryBtn.setOnAction(e -> openDeliveries());

                Button paymentsBtn = new Button("Payments");
                styleOutlineButton(paymentsBtn);
                paymentsBtn.setOnAction(e -> openPayments());

                Button chartsBtn = new Button("Charts");
                styleOutlineButton(chartsBtn);
                chartsBtn.setOnAction(e -> openCharts());

                authBox.getChildren().addAll(welcome, dashboardBtn, orderBtn, historyBtn, reservationBtn, deliveryBtn, paymentsBtn, chartsBtn, logoutBtn);
            } else {
                Button manageOrdersBtn = new Button("Manage Orders");
                styleOutlineButton(manageOrdersBtn);
                manageOrdersBtn.setOnAction(e -> openEmployeeOrders());

                Button manageDeliveriesBtn = new Button("Manage Deliveries");
                styleOutlineButton(manageDeliveriesBtn);
                manageDeliveriesBtn.setOnAction(e -> openDeliveries());

                Button manageReservationsBtn = new Button("Manage Reservations");
                styleOutlineButton(manageReservationsBtn);
                manageReservationsBtn.setOnAction(e -> openEmployeeReservations());

                authBox.getChildren().addAll(welcome, manageOrdersBtn, manageDeliveriesBtn, manageReservationsBtn, logoutBtn);
            }
        } else {
            Button loginBtn = new Button("Login");
            stylePrimaryButton(loginBtn);
            loginBtn.setOnAction(e -> openLogin());

            Button signUpBtn = new Button("Sign Up");
            styleOutlineButton(signUpBtn);
            signUpBtn.setOnAction(e -> openSignUp());

            authBox.getChildren().addAll(loginBtn, signUpBtn);
        }
    }

    private void loadBranches() {
        branchPane.getChildren().clear();
        String sql = "SELECT branch_id, branch_name, branch_location, branch_contact FROM Branches";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Branch branch = new Branch(
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("branch_location"),
                        rs.getString("branch_contact")
                );
                branchPane.getChildren().add(createBranchCard(branch));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createBranchCard(Branch branch) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(380);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #e0d5cc;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 2);"
        );

        Label name = new Label(branch.getBranchName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4e342e;");

        Label location = new Label("📍 " + branch.getBranchLocation());
        location.setStyle("-fx-font-size: 13px; -fx-text-fill: #8d6e63;");

        Label contact = new Label("📞 " + branch.getBranchContact());
        contact.setStyle("-fx-font-size: 13px; -fx-text-fill: #8d6e63;");

        Button viewMenu = new Button("View Menu");
        viewMenu.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(viewMenu);
        viewMenu.setOnAction(e -> openBranchMenu(branch));

        card.getChildren().addAll(name, location, contact, viewMenu);
        return card;
    }

    private void openBranchMenu(Branch branch) {
        try {
            if (!SessionManager.isLoggedIn() && !SessionManager.isGuest()) {
                SessionManager.setGuest();
            }
            SessionManager.setBranch(branch.getBranchId(), branch.getBranchName());

            Stage stage = (Stage) branchPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("menu-view.fxml")));
            Parent root = loader.load();
            stage.setScene(SceneHelper.create(root));
            stage.setTitle("Vanilla - " + branch.getBranchName());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLogin() {
        try {
            switchScene("login-view.fxml", "Vanilla Coffee - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSignUp() {
        try {
            switchScene("signup-view.fxml", "Vanilla Coffee - Sign Up");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDashboard() {
        try {
            switchScene("dashboard-view.fxml", "Vanilla Coffee - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPlaceOrder() {
        try {
            switchScene("order-placement-view.fxml", "Vanilla Coffee - Place Order");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openOrderHistory() {
        try {
            switchScene("order-history-view.fxml", "Vanilla Coffee - My Orders");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openReservations() {
        try {
            switchScene("reservation-view.fxml", "Vanilla Coffee - Reservations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDeliveries() {
        try {
            switchScene("delivery-view.fxml", "Vanilla Coffee - Delivery Tracking");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPayments() {
        try {
            switchScene("payment-view.fxml", "Vanilla Coffee - Payment History");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCharts() {
        try {
            switchScene("chart-view.fxml", "Vanilla Coffee - Charts & Reports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEmployeeOrders() {
        try {
            switchScene("employee-orders-view.fxml", "Vanilla Coffee - Manage Orders");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEmployeeReservations() {
        try {
            switchScene("reservation-view.fxml", "Vanilla Coffee - Manage Reservations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        SessionManager.clear();
        buildAuthBar();
    }

    private void switchScene(String fxml, String title) throws Exception {
        Stage stage = (Stage) authBox.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml(fxml)));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle(title);
        stage.show();
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(
                "-fx-background-color: #6d4c41;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 18;" +
                "-fx-cursor: hand;"
        );
    }

    private void styleOutlineButton(Button button) {
        button.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #6d4c41;" +
                "-fx-border-color: #d7ccc8;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 18;" +
                "-fx-cursor: hand;"
        );
    }
}
