package com.example.vanillacoffeesystem;

public class EmployeeOrderRecord {

    private final int orderId;
    private final String orderDate;
    private final String customerName;
    private final String branchName;
    private final String orderStatus;
    private final double totalPrice;
    private final String orderType;
    private final boolean isActive;
    private final String deliveryStatus;

    public EmployeeOrderRecord(int orderId, String orderDate, String customerName, String branchName,
                               String orderStatus, double totalPrice, String orderType,
                               boolean isActive, String deliveryStatus) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerName = customerName;
        this.branchName = branchName;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.orderType = orderType;
        this.isActive = isActive;
        this.deliveryStatus = deliveryStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getOrderType() {
        return orderType;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public boolean isPending() {
        return isActive && "Pending".equalsIgnoreCase(orderStatus);
    }
}
