package vanillacoffeesystem;

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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

// Checkout flow: branch, cart, payment, and order creation.
public class OrderPlacementController {

    @FXML private Label userLabel;
    @FXML private ComboBox<Branch> branchCombo;
    @FXML private RadioButton dineInRadio;
    @FXML private RadioButton deliveryRadio;
    @FXML private HBox dineInBox;
    @FXML private HBox deliveryBox;
    @FXML private ComboBox<TableOption> tableCombo;
    @FXML private TextField deliveryAddressField;
    @FXML private Label stepAErrorLabel;
    @FXML private HBox menuCategoryBar;
    @FXML private HBox menuDrinksSubBar;
    @FXML private FlowPane menuGrid;
    @FXML private Label stepBErrorLabel;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartNameColumn;
    @FXML private TableColumn<CartItem, String> cartQtyColumn;
    @FXML private TableColumn<CartItem, String> cartPriceColumn;
    @FXML private TableColumn<CartItem, String> cartSubtotalColumn;
    @FXML private Label totalLabel;
    @FXML private Label stepCErrorLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private VBox cardDetailsBox;
    @FXML private TextField cardNumberField;
    @FXML private VBox walletDetailsBox;
    @FXML private TextField walletPhoneField;
    @FXML private TextField paymentAmountField;
    @FXML private Button placeOrderButton;
    @FXML private Label stepDErrorLabel;

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ObservableList<TableOption> tables = FXCollections.observableArrayList();
    private MenuBrowseHelper menuBrowseHelper;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            userLabel.setText("Ordering as " + SessionManager.getDisplayName());
        }

        menuBrowseHelper = new MenuBrowseHelper(menuCategoryBar, menuDrinksSubBar, menuGrid, this::addMenuItemToCart);

        ToggleGroup orderTypeGroup = new ToggleGroup();
        dineInRadio.setToggleGroup(orderTypeGroup);
        deliveryRadio.setToggleGroup(orderTypeGroup);
        dineInRadio.setSelected(true);

        dineInRadio.selectedProperty().addListener((obs, was, isNow) -> updateOrderTypeUi(isNow));
        deliveryRadio.selectedProperty().addListener((obs, was, isNow) -> updateOrderTypeUi(!isNow));

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

        cartNameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getProductName()));
        cartQtyColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        cartPriceColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        cartSubtotalColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getSubtotal())));

        cartTable.setItems(cartItems);
        tableCombo.setItems(tables);

        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Cash", "Credit Card", "Debit Card", "Mobile Wallet"
        ));
        updatePaymentDetailFields();

        updateOrderTypeUi(true);
        updateTotal();
        loadBranches();
        menuBrowseHelper.clear();

        if (SessionManager.getSelectedBranchId() > 0) {
            selectBranchById(SessionManager.getSelectedBranchId());
        }
    }

    private void addMenuItemToCart(MenuItem item, int qty) {
        clearStepBError();
        if (qty < 1) {
            showStepBError("Quantity must be at least 1.");
            return;
        }

        Optional<CartItem> existing = cartItems.stream()
                .filter(cartItem -> cartItem.getProductId() == item.getProductId())
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + qty);
            cartTable.refresh();
        } else {
            cartItems.add(new CartItem(
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    qty
            ));
        }

        updateTotal();
    }

    public void loadBranches() {
        branchCombo.getItems().clear();
        String sql = "SELECT branch_id, branch_name, branch_location, branch_contact FROM Branches ORDER BY branch_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                branchCombo.getItems().add(new Branch(
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("branch_location"),
                        rs.getString("branch_contact")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showStepAError("Could not load branches.");
        }
    }

    @FXML
    public void onBranchSelected() {
        clearStepAError();
        clearStepBError();
        Branch branch = branchCombo.getValue();
        if (branch == null) {
            menuBrowseHelper.clear();
            tables.clear();
            return;
        }
        menuBrowseHelper.loadForBranch(branch.getBranchId(), branch.getBranchName());
        loadTablesForBranch(branch.getBranchId());
    }

    private void loadTablesForBranch(int branchId) {
        tables.clear();
        tableCombo.getSelectionModel().clearSelection();
        String sql = "SELECT table_id, capacity FROM Tables WHERE branch_id = ? ORDER BY table_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tables.add(new TableOption(rs.getInt("table_id"), rs.getInt("capacity")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showStepAError("Could not load tables for this branch.");
        }
    }

    @FXML
    public void placeOrder() {
        clearStepAError();
        clearStepBError();
        clearStepCError();
        clearStepDError();

        if (!SessionManager.isLoggedIn() || SessionManager.isGuest() || SessionManager.isEmployee()) {
            showStepDError("Please sign in as a customer to place an order.");
            return;
        }

        Branch branch = branchCombo.getValue();
        if (branch == null) {
            showStepAError("Please select a branch.");
            return;
        }

        if (cartItems.isEmpty()) {
            showStepCError("Your cart is empty. Add items before placing an order.");
            return;
        }

        boolean isDelivery = deliveryRadio.isSelected();
        Integer tableId = null;
        String deliveryAddress = null;

        if (isDelivery) {
            deliveryAddress = deliveryAddressField.getText() == null
                    ? "" : deliveryAddressField.getText().trim();
            if (deliveryAddress.isEmpty()) {
                showStepAError("Please enter a delivery address.");
                return;
            }
        } else {
            TableOption table = tableCombo.getValue();
            if (table == null) {
                showStepAError("Please select a table for dine-in.");
                return;
            }
            tableId = table.getTableId();
        }

        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();

        String paymentError = validatePayment(total);
        if (paymentError != null) {
            showStepDError(paymentError);
            return;
        }

        String paymentMethod = paymentMethodCombo.getValue();
        PaymentValidator.PaymentResult paymentResult = PaymentValidator.processPayment(
                paymentMethod,
                cardNumberField.getText(),
                walletPhoneField.getText()
        );

        if (!paymentResult.isPaid()) {
            showStepDError(paymentResult.errorMessage() != null
                    ? paymentResult.errorMessage()
                    : "Payment could not be processed. Please try again.");
            return;
        }

        String paymentStatus = paymentResult.status();
        LocalDate paymentDate = LocalDate.now();
        double paymentAmount = total;
        String orderStatus = "Pending";

        int customerId = SessionManager.getCustomerId();
        int branchId = branch.getBranchId();

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int orderId;
            String orderSql = """
                    INSERT INTO Orders (order_date, order_status, total_price, customer_id, branch_id, table_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement ps = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, java.sql.Date.valueOf(paymentDate));
                ps.setString(2, orderStatus);
                ps.setDouble(3, total);
                ps.setInt(4, customerId);
                ps.setInt(5, branchId);
                if (tableId == null) {
                    ps.setNull(6, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(6, tableId);
                }
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new IllegalStateException("Order ID was not generated.");
                    }
                    orderId = keys.getInt(1);
                }
            }

            String itemSql = "INSERT INTO Order_Items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (CartItem item : cartItems) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            if (isDelivery) {
                String deliverySql = """
                        INSERT INTO Delivery (order_id, delivery_address, delivery_status)
                        VALUES (?, ?, 'pending')
                        """;
                try (PreparedStatement ps = con.prepareStatement(deliverySql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, deliveryAddress);
                    ps.executeUpdate();
                }
            }

            String paymentSql = """
                    INSERT INTO Payment (order_id, payment_method, payment_amount, payment_date, payment_status)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = con.prepareStatement(paymentSql)) {
                ps.setInt(1, orderId);
                ps.setString(2, paymentMethod);
                ps.setDouble(3, paymentAmount);
                ps.setDate(4, java.sql.Date.valueOf(paymentDate));
                ps.setString(5, paymentStatus);
                ps.executeUpdate();
            }

            con.commit();

            cartItems.clear();
            updateTotal();
            deliveryAddressField.clear();
            tableCombo.getSelectionModel().clearSelection();
            paymentMethodCombo.getSelectionModel().clearSelection();
            cardNumberField.clear();
            walletPhoneField.clear();
            updatePaymentDetailFields();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Placed");
            alert.setHeaderText("Thank you for your order!");
            alert.setContentText(String.format(
                    "Order #%d is pending.%nPayment of ₪ %.2f via %s was approved (%s).%n%n"
                            + "You can cancel it from My Orders while it is still pending.",
                    orderId, paymentAmount, paymentMethod, paymentStatus
            ));
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            showStepDError("Could not place order. Please try again.");
            showAlert(Alert.AlertType.ERROR, "Order Failed", "Could not place order: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void backToHome() throws Exception {
        Stage stage = (Stage) placeOrderButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.fxml("home-view.fxml")));
        Parent root = loader.load();
        stage.setScene(SceneHelper.create(root));
        stage.setTitle("Vanilla Coffee");
        stage.show();
    }

    private void selectBranchById(int branchId) {
        for (Branch branch : branchCombo.getItems()) {
            if (branch.getBranchId() == branchId) {
                branchCombo.setValue(branch);
                onBranchSelected();
                break;
            }
        }
    }

    private void updateOrderTypeUi(boolean dineIn) {
        dineInBox.setVisible(dineIn);
        dineInBox.setManaged(dineIn);
        deliveryBox.setVisible(!dineIn);
        deliveryBox.setManaged(!dineIn);
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        totalLabel.setText(String.format("Total: ₪ %.2f", total));
        paymentAmountField.setText(String.format("%.2f", total));
    }

    @FXML
    public void onPaymentMethodChanged() {
        clearStepDError();
        updatePaymentDetailFields();
    }

    private void updatePaymentDetailFields() {
        String method = paymentMethodCombo.getValue();
        boolean isCard = "Credit Card".equals(method) || "Debit Card".equals(method);
        boolean isWallet = "Mobile Wallet".equals(method);

        cardDetailsBox.setVisible(isCard);
        cardDetailsBox.setManaged(isCard);
        walletDetailsBox.setVisible(isWallet);
        walletDetailsBox.setManaged(isWallet);

        if (!isCard) {
            cardNumberField.clear();
        }
        if (!isWallet) {
            walletPhoneField.clear();
        }
    }

    private String validatePayment(double orderTotal) {
        if (paymentMethodCombo.getValue() == null || paymentMethodCombo.getValue().isBlank()) {
            return "Please select a payment method.";
        }

        String method = paymentMethodCombo.getValue();
        if ("Credit Card".equals(method) || "Debit Card".equals(method)) {
            String cardError = PaymentValidator.validateCardNumber(cardNumberField.getText());
            if (cardError != null) {
                return cardError;
            }
        } else if ("Mobile Wallet".equals(method)) {
            String phoneError = PaymentValidator.validateWalletPhone(walletPhoneField.getText());
            if (phoneError != null) {
                return phoneError;
            }
        }

        if (orderTotal <= 0) {
            return "Order total must be greater than zero.";
        }

        return null;
    }

    private void showStepAError(String message) {
        stepAErrorLabel.setText(message);
        stepAErrorLabel.setVisible(true);
        stepAErrorLabel.setManaged(true);
    }

    private void showStepBError(String message) {
        stepBErrorLabel.setText(message);
        stepBErrorLabel.setVisible(true);
        stepBErrorLabel.setManaged(true);
    }

    private void showStepCError(String message) {
        stepCErrorLabel.setText(message);
        stepCErrorLabel.setVisible(true);
        stepCErrorLabel.setManaged(true);
    }

    private void clearStepAError() {
        stepAErrorLabel.setText("");
        stepAErrorLabel.setVisible(false);
        stepAErrorLabel.setManaged(false);
    }

    private void clearStepBError() {
        stepBErrorLabel.setText("");
        stepBErrorLabel.setVisible(false);
        stepBErrorLabel.setManaged(false);
    }

    private void clearStepCError() {
        stepCErrorLabel.setText("");
        stepCErrorLabel.setVisible(false);
        stepCErrorLabel.setManaged(false);
    }

    private void showStepDError(String message) {
        stepDErrorLabel.setText(message);
        stepDErrorLabel.setVisible(true);
        stepDErrorLabel.setManaged(true);
    }

    private void clearStepDError() {
        stepDErrorLabel.setText("");
        stepDErrorLabel.setVisible(false);
        stepDErrorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
