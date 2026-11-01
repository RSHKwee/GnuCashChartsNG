package kwee.gnucashcharts.library;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class LocalDateAndAmount {
  Map<LocalDate, Double> m_DateAmount = new TreeMap<LocalDate, Double>();

  public LocalDateAndAmount() {
  }

  public Map<LocalDate, Double> getDateAmount() {
    return m_DateAmount;
  }

  public double getAmount(LocalDate a_Date) {
    double l_amt = 0.0;
    if (m_DateAmount.get(a_Date) != null) {
      l_amt = m_DateAmount.get(a_Date);
    }
    return l_amt;
  }

  public void addDateAmount(LocalDate a_Date, double a_Amount) {
    m_DateAmount.put(a_Date, a_Amount);
  }

}
