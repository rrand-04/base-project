package com.example.vanillacoffeesystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    @FXML
    public void initialize() {
        clearError();
    }

    @FXML
    public void handleRegister() {
        clearError();

        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        String error = firstError(
                AuthValidator.validateName(name),
                AuthValidator.validateContact(contact),
                AuthValidator.validateCustomerUsername(username),
                AuthValidator.validatePassword(password),
                AuthValidator.validatePasswordMatch(password, confirm)
        );

        if (error != null) {
            showError(error);
            return;
        }

        if (AuthValidator.isEmployeeUsername(username)) {
            showError("This username is reserved for staff. Please choose a different customer username.");
            return;
        }

        String checkSql = "SELECT customer_id FROM Customers WHERE username = ?";
        String insertSql = "INSERT INTO Customers (customer_name, customer_contact, username, password) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setString(1, username);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        showError("Username already taken. Please choose another.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, contact);
                ps.setString(3, username);
                ps.setString(4, password);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        SessionManager.setCustomer(keys.getInt(1), name);
                        openHome();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Registration failed. Please try again.");
        }
    }

    private String firstError(String... errors) {
        for (String error : errors) {
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    @FXML
    public void openLogin() throws Exception {
        switchScene("login-view.fxml", "Vanilla Coffee - Login");
    }

    @FXML
    public void openHome() throws Exception {
        switchScene("home-view.fxml", "Vanilla Coffee");
    }

    private void switchScene(String fxml, String title) throws Exception {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml(fxml)));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 1100, 720));
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
