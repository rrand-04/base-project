package vanillacoffeesystem;

import java.util.regex.Pattern;

// Username and password rules used by login and sign-up.
public final class AuthValidator {

    private static final Pattern CUSTOMER_USERNAME =
            Pattern.compile("^[a-z][a-z0-9_]{2,19}$");

    private static final Pattern EMPLOYEE_USERNAME =
            Pattern.compile("^emp[0-9]{4}$");

    private static final Pattern PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=])[A-Za-z\\d@$!%*?&#^()_\\-+=]{8,}$"
    );

    private static final Pattern CONTACT = Pattern.compile("^[0-9+\\- ]{8,20}$");

    private AuthValidator() {
    }

    public static String validateCustomerUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Username is required.";
        }
        if (!CUSTOMER_USERNAME.matcher(username).matches()) {
            return "Customer username must be 3-20 characters, start with a letter, and use only lowercase letters, numbers, or _.";
        }
        return null;
    }

    public static String validateEmployeeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Employee ID is required.";
        }
        if (!EMPLOYEE_USERNAME.matcher(username).matches()) {
            return "Employee login must use your staff ID format: emp0001 (emp + 4-digit employee number).";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!PASSWORD.matcher(password).matches()) {
            return "Password must include uppercase, lowercase, a number, and a special character (@, $, !, %, etc.).";
        }
        return null;
    }

    public static String validatePasswordMatch(String password, String confirm) {
        if (confirm == null || confirm.isEmpty()) {
            return "Please confirm your password.";
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }
        return null;
    }

    public static String validateName(String name) {
        if (name == null || name.isBlank()) {
            return "Full name is required.";
        }
        if (name.length() < 2) {
            return "Name must be at least 2 characters.";
        }
        return null;
    }

    public static String validateContact(String contact) {
        if (contact == null || contact.isBlank()) {
            return "Contact number is required.";
        }
        if (!CONTACT.matcher(contact).matches()) {
            return "Enter a valid phone number (8-20 digits).";
        }
        return null;
    }

    public static boolean isEmployeeUsername(String username) {
        return username != null && EMPLOYEE_USERNAME.matcher(username).matches();
    }
}
