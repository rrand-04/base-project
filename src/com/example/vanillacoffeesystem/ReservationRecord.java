package com.example.vanillacoffeesystem;

public class ReservationRecord {

    private final int reservationId;
    private final String branchName;
    private final String reservationDate;
    private final String reservationTime;
    private final int numberOfPeople;
    private final String status;

    public ReservationRecord(int reservationId, String branchName, String reservationDate,
                             String reservationTime, int numberOfPeople, String status) {
        this.reservationId = reservationId;
        this.branchName = branchName;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfPeople = numberOfPeople;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }
}
