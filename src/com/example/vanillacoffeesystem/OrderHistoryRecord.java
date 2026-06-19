package com.example.vanillacoffeesystem;

public class OrderHistoryRecord {

    private final int orderId;
    private final String orderDate;
    private final String orderStatus;
    private final double totalPrice;
    private final String branchName;
    private final boolean isActive;

    public OrderHistoryRecord(int orderId, String orderDate, String orderStatus,
                              double totalPrice, String branchName, boolean isActive) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.branchName = branchName;
        this.isActive = isActive;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getBranchName() {
        return branchName;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isPending() {
        return isActive && "Pending".equalsIgnoreCase(orderStatus);
    }
}
