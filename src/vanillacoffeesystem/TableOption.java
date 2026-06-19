package vanillacoffeesystem;

public class TableOption {

    private final int tableId;
    private final int capacity;

    public TableOption(int tableId, int capacity) {
        this.tableId = tableId;
        this.capacity = capacity;
    }

    public int getTableId() {
        return tableId;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Table " + tableId + " (" + capacity + " seats)";
    }
}
