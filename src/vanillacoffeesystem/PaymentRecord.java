package vanillacoffeesystem;

public class PaymentRecord {

    private final int paymentId;
    private final int orderId;
    private final String branchName;
    private final String paymentMethod;
    private final double paymentAmount;
    private final String paymentDate;
    private final String paymentStatus;

    public PaymentRecord(int paymentId, int orderId, String branchName, String paymentMethod,
                         double paymentAmount, String paymentDate, String paymentStatus) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.branchName = branchName;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
