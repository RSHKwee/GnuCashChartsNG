package kwee.gnucashcharts.library;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.library.ApplicationMessages;
import kwee.logger.MyLogger;

public class FormatAmount {
  private static final Logger lOGGER = MyLogger.getLogger();

  public static String formatAmount(double amt) {
    Locale locale = ApplicationMessages.getInstance().getLocale();
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
    String formattedNumber = currencyFormat.format(amt);
    return formattedNumber;
  }

  public static double convertToDouble(String input) {
    lOGGER.log(Level.FINE, "input: " + input);
    input = input.trim();
    // Remove the Euro currency symbol and the thousand separator
    String cleanedInput = input.replace("€", "").replace(".", "").replace(",", ".");
    lOGGER.log(Level.FINE, "cleanedInput: " + cleanedInput);

    try {
      // Parse the cleaned string as a double
      double result = Double.parseDouble(cleanedInput);
      return result;
    } catch (Exception e) {
      if (!cleanedInput.isBlank()) {
        lOGGER.log(Level.FINE, "Error converting to double: " + e.getMessage() + " " + cleanedInput);
      }
      return 0.0; // Return a default value or handle the error accordingly
    }
  }
}
