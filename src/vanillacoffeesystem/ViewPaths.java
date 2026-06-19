package vanillacoffeesystem;

// Helper for locating FXML files under the views package.
public final class ViewPaths {

    private static final String BASE = "/vanillacoffeesystem/views/";

    private ViewPaths() {
    }

    public static String fxml(String fileName) {
        return BASE + fileName;
    }
}
