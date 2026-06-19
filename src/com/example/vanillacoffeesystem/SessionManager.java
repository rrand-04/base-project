package com.example.vanillacoffeesystem;

public class SessionManager {

    public enum UserType {
        GUEST, CUSTOMER, EMPLOYEE
    }

    private static UserType userType = UserType.GUEST;
    private static int customerId;
    private static String customerName;
    private static int employeeId;
    private static String employeeName;
    private static String employeePosition;
    private static int selectedBranchId;
    private static String selectedBranchName;

    public static void setCustomer(int id, String name) {
        customerId = id;
        customerName = name;
        employeeId = 0;
        employeeName = null;
        employeePosition = null;
        userType = UserType.CUSTOMER;
    }

    public static void setEmployee(int id, String firstName, String lastName, String position) {
        employeeId = id;
        employeeName = firstName + " " + lastName;
        employeePosition = position;
        customerId = 0;
        customerName = null;
        userType = UserType.EMPLOYEE;
    }

    public static void setGuest() {
        clear();
        customerName = "Guest";
        userType = UserType.GUEST;
    }

    public static void setBranch(int branchId, String branchName) {
        selectedBranchId = branchId;
        selectedBranchName = branchName;
    }

    public static int getCustomerId() {
        return customerId;
    }

    public static String getCustomerName() {
        return customerName;
    }

    public static int getEmployeeId() {
        return employeeId;
    }

    public static String getEmployeeName() {
        return employeeName;
    }

    public static String getEmployeePosition() {
        return employeePosition;
    }

    public static UserType getUserType() {
        return userType;
    }

    public static int getSelectedBranchId() {
        return selectedBranchId;
    }

    public static String getSelectedBranchName() {
        return selectedBranchName;
    }

    public static boolean isGuest() {
        return userType == UserType.GUEST;
    }

    public static boolean isEmployee() {
        return userType == UserType.EMPLOYEE;
    }

    public static void clear() {
        userType = UserType.GUEST;
        customerId = 0;
        customerName = null;
        employeeId = 0;
        employeeName = null;
        employeePosition = null;
    }

    public static boolean isLoggedIn() {
        return userType == UserType.CUSTOMER || userType == UserType.EMPLOYEE;
    }

    public static String getDisplayName() {
        if (userType == UserType.EMPLOYEE) {
            return employeeName;
        }
        if (userType == UserType.CUSTOMER) {
            return customerName;
        }
        return "Guest";
    }
}
