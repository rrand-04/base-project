package com.example.vanillacoffeesystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private ToggleButton customerTab;
    @FXML private ToggleButton employeeTab;
    @FXML private Label loginHintLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private HBox signUpRow;
    @FXML private Hyperlink signUpLink;

    private boolean employeeMode;

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        customerTab.setToggleGroup(group);
        employeeTab.setToggleGroup(group);
        customerTab.setSelected(true);

        customerTab.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                setEmployeeMode(false);
            }
        });
        employeeTab.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                setEmployeeMode(true);
            }
        });

        styleToggle(customerTab, true);
        styleToggle(employeeTab, false);
        customerTab.selectedProperty().addListener((obs, was, sel) -> styleToggle(customerTab, sel));
        employeeTab.selectedProperty().addListener((obs, was, sel) -> styleToggle(employeeTab, sel));

        setEmployeeMode(false);
        clearError();
    }

    private void setEmployeeMode(boolean employee) {
        employeeMode = employee;
        if (employee) {
            loginHintLabel.setText("Staff login only. Use the employee ID given by your manager (e.g. emp0002).");
            usernameField.setPromptText("e.g. emp0002");
            signUpRow.setVisible(false);
            signUpRow.setManaged(false);
        } else {
            loginHintLabel.setText("Sign in with your customer account");
            usernameField.setPromptText("e.g. lina");
            signUpRow.setVisible(true);
            signUpRow.setManaged(true);
        }
        clearError();
    }

    private void styleToggle(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #4e342e; -fx-background-radius: 8; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8d6e63; -fx-background-radius: 8; " +
                    "-fx-font-size: 13px; -fx-cursor: hand;");
        }
    }

    @FXML
    public void handleLogin() {
        clearError();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            return;
        }

        String passwordError = AuthValidator.validatePassword(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        if (employeeMode) {
            loginEmployee(username, password);
        } else {
            loginCustomer(username, password);
        }
    }

    private void loginCustomer(String username, String password) {
        String usernameError = AuthValidator.validateCustomerUsername(username);
        if (usernameError != null) {
            showError(usernameError);
            return;
        }

        String sql = "SELECT customer_id, customer_name, password FROM Customers WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showError("No customer account found with that username.");
                    return;
                }
                if (!password.equals(rs.getString("password"))) {
                    showError("Incorrect password. Please try again.");
                    return;
                }
                SessionManager.setCustomer(rs.getInt("customer_id"), rs.getString("customer_name"));
                openDashboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    private void loginEmployee(String username, String password) {
        String usernameError = AuthValidator.validateEmployeeUsername(username);
        if (usernameError != null) {
            showError(usernameError);
            return;
        }

        String sql = """
                SELECT employee_id, first_name, last_name, employee_position, password
                FROM Employee WHERE username = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showError("Employee ID not recognized. Contact your manager if you need access.");
                    return;
                }
                if (!password.equals(rs.getString("password"))) {
                    showError("Incorrect password. Please try again.");
                    return;
                }
                SessionManager.setEmployee(
                        rs.getInt("employee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("employee_position")
                );
                openDashboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleGuest() {
        try {
            SessionManager.setGuest();
            openHome();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open home page.");
        }
    }

    @FXML
    public void openSignUp() throws Exception {
        switchScene("signup-view.fxml", "Vanilla Coffee - Sign Up");
    }

    private void openDashboard() throws Exception {
        switchScene("dashboard-view.fxml", "Vanilla Coffee - Dashboard");
    }

    private void openHome() throws Exception {
        switchScene("home-view.fxml", "Vanilla Coffee");
    }

    @FXML
    public void backToHome() throws Exception {
        openHome();
    }

    private void switchScene(String fxml, String title) throws Exception {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml(fxml)));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle(title);
        stage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
