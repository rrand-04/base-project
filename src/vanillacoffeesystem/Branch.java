package vanillacoffeesystem;

public class Branch {

    private final int branchId;
    private final String branchName;
    private final String branchLocation;
    private final String branchContact;

    public Branch(int branchId, String branchName, String branchLocation, String branchContact) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchLocation = branchLocation;
        this.branchContact = branchContact;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getBranchLocation() {
        return branchLocation;
    }

    public String getBranchContact() {
        return branchContact;
    }
}
