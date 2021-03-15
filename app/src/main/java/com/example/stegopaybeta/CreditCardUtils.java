package com.example.stegopaybeta;

// Credit card helper class
public class CreditCardUtils {

    // Card type return values
    private static final int NONE = 0;
    private static final int VISA = 1;
    private static final int MASTERCARD = 2;

    // Card type prefixes
    private static final String VISA_PREFIX = "4";
    private static final String MASTERCARD_PREFIX = "51,52,53,54,55,59,";

    // A method that returns a card type integer based on the prefix
    public static int getCardType (String cardNumber) {
        // Checks if the first digit of the provided card number is equal to Visa's prefix
        if (cardNumber.substring(0, 1).equals(VISA_PREFIX)) {
            return VISA;
        }

        // Checks if the first two digits of the provided card number is equal to any of Mastercard's prefixes
        else if (MASTERCARD_PREFIX.contains(cardNumber.substring(0, 2) + ",")) {
            return MASTERCARD;
        }

        return NONE;
    }

}
