package vanillacoffeesystem;

public final class PaymentValidator {

    public record PaymentResult(String status, String errorMessage) {
        public boolean isPaid() {
            return "Paid".equals(status);
        }
    }

    private PaymentValidator() {
    }

    public static PaymentResult processPayment(String method, String cardNumber, String walletPhone) {
        if (method == null || method.isBlank()) {
            return new PaymentResult("Failed", "Please select a payment method.");
        }

        return switch (method) {
            case "Cash" -> new PaymentResult("Paid", null);
            case "Credit Card", "Debit Card" -> {
                String cardError = validateCardNumber(cardNumber);
                if (cardError != null) {
                    yield new PaymentResult("Failed", cardError);
                }
                yield new PaymentResult("Paid", null);
            }
            case "Mobile Wallet" -> {
                String phoneError = validateWalletPhone(walletPhone);
                if (phoneError != null) {
                    yield new PaymentResult("Failed", phoneError);
                }
                yield new PaymentResult("Paid", null);
            }
            default -> new PaymentResult("Failed", "Unsupported payment method.");
        };
    }

    public static String validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return "Please enter your card number.";
        }
        String digits = cardNumber.replaceAll("\\s+", "");
        if (!digits.matches("\\d{16}")) {
            return "Card number must be exactly 16 digits.";
        }
        if (!passesLuhnCheck(digits)) {
            return "Card number is not valid. Please check and try again.";
        }
        return null;
    }

    public static String validateWalletPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "Please enter your mobile wallet phone number.";
        }
        String normalized = phone.replaceAll("[\\s-]+", "");
        if (!normalized.matches("05\\d{8}")) {
            return "Phone must be 10 digits starting with 05 (e.g. 0591234567).";
        }
        return null;
    }

    private static boolean passesLuhnCheck(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
