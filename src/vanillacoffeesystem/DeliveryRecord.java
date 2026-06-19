package vanillacoffeesystem;

public class DeliveryRecord {

    private final int deliveryId;
    private final int orderId;
    private final String branchName;
    private final String orderDate;
    private final String deliveryAddress;
    private final String deliveryStatus;
    private final String deliveryTime;

    public DeliveryRecord(int deliveryId, int orderId, String branchName, String orderDate,
                          String deliveryAddress, String deliveryStatus, String deliveryTime) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.branchName = branchName;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
        this.deliveryStatus = deliveryStatus;
        this.deliveryTime = deliveryTime;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }
}
