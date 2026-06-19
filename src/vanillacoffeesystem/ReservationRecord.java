package vanillacoffeesystem;

public class ReservationRecord {

    private final int reservationId;
    private final String branchName;
    private final String reservationDate;
    private final String reservationTime;
    private final int numberOfPeople;
    private final String status;
    private final String customerName;

    public ReservationRecord(int reservationId, String branchName, String reservationDate,
                             String reservationTime, int numberOfPeople, String status) {
        this(reservationId, branchName, reservationDate, reservationTime, numberOfPeople, status, "");
    }

    public ReservationRecord(int reservationId, String branchName, String reservationDate,
                             String reservationTime, int numberOfPeople, String status,
                             String customerName) {
        this.reservationId = reservationId;
        this.branchName = branchName;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfPeople = numberOfPeople;
        this.status = status;
        this.customerName = customerName == null ? "" : customerName;
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

    public String getCustomerName() {
        return customerName;
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isCancellable() {
        return ("pending".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status));
    }
}
