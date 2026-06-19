package vanillacoffeesystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Saves and loads product ratings when the Review table is available.
public final class ReviewHelper {

    private static Boolean available;

    private ReviewHelper() {
    }

    public static boolean isAvailable() {
        if (available != null) {
            return available;
        }
        available = tableExists("Review");
        return available;
    }

    public static Integer getRating(int customerId, int productId) {
        if (!isAvailable()) {
            return null;
        }

        String sql = "SELECT rating FROM Review WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rating");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveReview(int customerId, int productId, int orderId, int rating, String comment) {
        if (!isAvailable() || rating < 1 || rating > 5) {
            return false;
        }

        String sql = """
                INSERT INTO Review (customer_id, product_id, order_id, rating, comment, review_date)
                VALUES (?, ?, ?, ?, ?, CURDATE())
                ON DUPLICATE KEY UPDATE
                    rating = VALUES(rating),
                    comment = VALUES(comment),
                    order_id = VALUES(order_id),
                    review_date = CURDATE()
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, orderId);
            ps.setInt(4, rating);
            if (comment == null || comment.isBlank()) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, comment.trim());
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean tableExists(String tableName) {
        String sql = """
                SELECT COUNT(*) AS cnt
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'vanilla_db' AND TABLE_NAME = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
