package com.example.vanillacoffeesystem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class ReservationController {

    @FXML private Label pageTitleLabel;
    @FXML private Label listTitleLabel;
    @FXML private VBox bookFormPanel;
    @FXML private ComboBox<Branch> branchCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private Spinner<Integer> peopleSpinner;
    @FXML private Button bookButton;
    @FXML private Label formErrorLabel;
    @FXML private TableView<ReservationRecord> reservationsTable;
    @FXML private TableColumn<ReservationRecord, String> idColumn;
    @FXML private TableColumn<ReservationRecord, String> customerColumn;
    @FXML private TableColumn<ReservationRecord, String> branchColumn;
    @FXML private TableColumn<ReservationRecord, String> dateColumn;
    @FXML private TableColumn<ReservationRecord, String> timeColumn;
    @FXML private TableColumn<ReservationRecord, String> peopleColumn;
    @FXML private TableColumn<ReservationRecord, String> statusColumn;
    @FXML private Button cancelButton;
    @FXML private Label emptyLabel;
    @FXML private HBox customerCancelPanel;
    @FXML private VBox staffUpdatePanel;
    @FXML private Label staffSelectedLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button updateStatusButton;
    @FXML private Label updateErrorLabel;

    private final ObservableList<ReservationRecord> reservations = FXCollections.observableArrayList();
    private final ObservableList<Branch> branches = FXCollections.observableArrayList();
    private boolean employeeMode;

    @FXML
    public void initialize() {
        employeeMode = SessionManager.isEmployee();

        if (!SessionManager.isLoggedIn() || SessionManager.isGuest()) {
            showAlert(Alert.AlertType.WARNING, "Reservations",
                    "Please sign in to view reservations.");
            bookButton.setDisable(true);
        } else if (employeeMode) {
            pageTitleLabel.setText("Manage Reservations");
            listTitleLabel.setText("All Reservations");
            bookFormPanel.setVisible(false);
            bookFormPanel.setManaged(false);
            customerCancelPanel.setVisible(false);
            customerCancelPanel.setManaged(false);
            staffUpdatePanel.setVisible(true);
            staffUpdatePanel.setManaged(true);
            customerColumn.setVisible(true);
        } else {
            pageTitleLabel.setText("Table Reservations");
            staffUpdatePanel.setVisible(false);
            staffUpdatePanel.setManaged(false);
            customerColumn.setVisible(false);
        }

        branchCombo.setItems(branches);
        branchCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Branch branch) {
                return branch == null ? "" : branch.getBranchName();
            }

            @Override
            public Branch fromString(String string) {
                return null;
            }
        });

        peopleSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        datePicker.setValue(LocalDate.now().plusDays(1));
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    return;
                }
                setDisable(item.isBefore(LocalDate.now()));
            }
        });

        statusCombo.setItems(FXCollections.observableArrayList(
                "pending", "confirmed", "completed", "cancelled"));

        idColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getReservationId())));
        customerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCustomerName()));
        branchColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBranchName()));
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReservationDate()));
        timeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatTime(data.getValue().getReservationTime())));
        peopleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getNumberOfPeople())));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(capitalizeStatus(data.getValue().getStatus())));

        reservationsTable.setItems(reservations);
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) ->
                onReservationSelected(selected));

        if (!employeeMode) {
            loadBranches();
        }
        loadReservations();
    }

    private void onReservationSelected(ReservationRecord selected) {
        clearUpdateError();

        if (employeeMode) {
            boolean canUpdate = selected != null && selected.isCancellable();
            statusCombo.setDisable(!canUpdate);
            updateStatusButton.setDisable(!canUpdate);

            if (selected == null) {
                staffSelectedLabel.setText("Select a reservation to update its status.");
                statusCombo.getSelectionModel().clearSelection();
            } else {
                staffSelectedLabel.setText("Reservation #" + selected.getReservationId()
                        + " — " + selected.getCustomerName()
                        + " at " + selected.getBranchName()
                        + " on " + selected.getReservationDate()
                        + " " + formatTime(selected.getReservationTime()));
                statusCombo.getSelectionModel().select(selected.getStatus().toLowerCase());
            }
            return;
        }

        cancelButton.setDisable(selected == null || !selected.isCancellable());
    }

    private void loadBranches() {
        branches.clear();
        String sql = "SELECT branch_id, branch_name, branch_location, branch_contact FROM Branches ORDER BY branch_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                branches.add(new Branch(
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("branch_location"),
                        rs.getString("branch_contact")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load branches.");
        }
    }

    public void loadReservations() {
        reservations.clear();
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        onReservationSelected(null);

        if (!SessionManager.isLoggedIn()) {
            emptyLabel.setText("Sign in to see reservations.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        String sql;
        if (employeeMode) {
            sql = """
                    SELECT r.reservation_id, b.branch_name, r.reservation_date, r.reservation_time,
                           r.number_of_people, r.status, c.customer_name
                    FROM Reservation r
                    JOIN Branches b ON r.branch_id = b.branch_id
                    JOIN Customers c ON r.customer_id = c.customer_id
                    ORDER BY r.reservation_date DESC, r.reservation_time DESC
                    """;
        } else {
            sql = """
                    SELECT r.reservation_id, b.branch_name, r.reservation_date, r.reservation_time,
                           r.number_of_people, r.status
                    FROM Reservation r
                    JOIN Branches b ON r.branch_id = b.branch_id
                    WHERE r.customer_id = ?
                    ORDER BY r.reservation_date DESC, r.reservation_time DESC
                    """;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!employeeMode) {
                ps.setInt(1, SessionManager.getCustomerId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (employeeMode) {
                        reservations.add(new ReservationRecord(
                                rs.getInt("reservation_id"),
                                rs.getString("branch_name"),
                                rs.getString("reservation_date"),
                                rs.getString("reservation_time"),
                                rs.getInt("number_of_people"),
                                rs.getString("status"),
                                rs.getString("customer_name")
                        ));
                    } else {
                        reservations.add(new ReservationRecord(
                                rs.getInt("reservation_id"),
                                rs.getString("branch_name"),
                                rs.getString("reservation_date"),
                                rs.getString("reservation_time"),
                                rs.getInt("number_of_people"),
                                rs.getString("status")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load reservations.");
        }

        if (reservations.isEmpty()) {
            emptyLabel.setText(employeeMode ? "No reservations found." : "No reservations yet.");
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        }
    }

    @FXML
    public void bookReservation() {
        clearFormError();

        if (!SessionManager.isLoggedIn() || SessionManager.isGuest() || SessionManager.isEmployee()) {
            showFormError("Please sign in as a customer to book a reservation.");
            return;
        }

        Branch branch = branchCombo.getValue();
        if (branch == null) {
            showFormError("Please select a branch.");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            showFormError("Please select a reservation date.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showFormError("Reservation date cannot be in the past.");
            return;
        }

        String timeError = validateTime(timeField.getText());
        if (timeError != null) {
            showFormError(timeError);
            return;
        }
        LocalTime time = LocalTime.parse(timeField.getText().trim(),
                DateTimeFormatter.ofPattern("H:mm"));

        int people = peopleSpinner.getValue();
        if (people < 1 || people > 20) {
            showFormError("Number of people must be between 1 and 20.");
            return;
        }

        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            showFormError("Reservation time cannot be in the past.");
            return;
        }

        String sql = """
                INSERT INTO Reservation (customer_id, branch_id, reservation_date, reservation_time,
                                         number_of_people, status)
                VALUES (?, ?, ?, ?, ?, 'pending')
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getCustomerId());
            ps.setInt(2, branch.getBranchId());
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            ps.setInt(5, people);
            ps.executeUpdate();

            timeField.clear();
            peopleSpinner.getValueFactory().setValue(2);
            loadReservations();

            showAlert(Alert.AlertType.INFORMATION, "Reservation Booked",
                    "Your table reservation at " + branch.getBranchName()
                            + " on " + date + " at " + time + " is pending confirmation.");

        } catch (Exception e) {
            e.printStackTrace();
            showFormError("Could not book reservation. Please try again.");
        }
    }

    @FXML
    public void cancelReservation() {
        ReservationRecord selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.isCancellable()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Reservation");
        confirm.setHeaderText("Cancel reservation #" + selected.getReservationId() + "?");
        confirm.setContentText(selected.getBranchName() + " on " + selected.getReservationDate()
                + " at " + formatTime(selected.getReservationTime()));
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String sql = """
                UPDATE Reservation SET status = 'cancelled'
                WHERE reservation_id = ? AND customer_id = ?
                  AND status IN ('pending', 'confirmed')
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selected.getReservationId());
            ps.setInt(2, SessionManager.getCustomerId());
            int updated = ps.executeUpdate();

            if (updated > 0) {
                loadReservations();
                showAlert(Alert.AlertType.INFORMATION, "Cancelled",
                        "Reservation #" + selected.getReservationId() + " has been cancelled.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Could not cancel this reservation. It may already be completed or cancelled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not cancel reservation.");
        }
    }

    @FXML
    public void updateReservationStatus() {
        if (!employeeMode) {
            return;
        }

        clearUpdateError();

        ReservationRecord selected = reservationsTable.getSelectionModel().getSelectedItem();
        String newStatus = statusCombo.getValue();

        if (selected == null) {
            showUpdateError("Please select a reservation first.");
            return;
        }
        if (newStatus == null || newStatus.isBlank()) {
            showUpdateError("Please choose a status.");
            return;
        }
        if (newStatus.equalsIgnoreCase(selected.getStatus())) {
            showUpdateError("Reservation already has this status.");
            return;
        }

        String sql = "UPDATE Reservation SET status = ? WHERE reservation_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, selected.getReservationId());
            int updated = ps.executeUpdate();

            if (updated > 0) {
                loadReservations();
                showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                        "Reservation #" + selected.getReservationId()
                                + " is now " + capitalizeStatus(newStatus) + ".");
            } else {
                showUpdateError("Could not update reservation status.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showUpdateError("Could not update reservation status.");
        }
    }

    @FXML
    public void backToHome() throws Exception {
        Stage stage = (Stage) reservationsTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    private String validateTime(String text) {
        if (text == null || text.isBlank()) {
            return "Please enter a time (HH:MM).";
        }
        try {
            LocalTime.parse(text.trim(), DateTimeFormatter.ofPattern("H:mm"));
            return null;
        } catch (DateTimeParseException ex) {
            return "Invalid time. Use 24-hour format (HH:MM), e.g. 18:30.";
        }
    }

    private String formatTime(String dbTime) {
        if (dbTime == null || dbTime.isBlank()) {
            return "";
        }
        try {
            return LocalTime.parse(dbTime.trim().substring(0, Math.min(8, dbTime.trim().length())))
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ex) {
            return dbTime.length() >= 5 ? dbTime.substring(0, 5) : dbTime;
        }
    }

    private String capitalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    private void showFormError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }

    private void clearFormError() {
        formErrorLabel.setText("");
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);
    }

    private void clearUpdateError() {
        updateErrorLabel.setText("");
        updateErrorLabel.setVisible(false);
        updateErrorLabel.setManaged(false);
    }

    private void showUpdateError(String message) {
        updateErrorLabel.setText(message);
        updateErrorLabel.setVisible(true);
        updateErrorLabel.setManaged(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
