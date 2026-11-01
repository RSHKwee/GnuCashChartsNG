package kwee.gnucashcharts.library;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.gnucashcharts.library.gnuCashDb.TaartPuntDataImpl;
import kwee.gnucashcharts.main.UserSetting;
import kwee.library.ApplicationMessages;
import kwee.logger.MyLogger;

public class DetermineDeltas {
  private static final Logger lOGGER = MyLogger.getLogger();
  public static UserSetting m_param = new UserSetting();
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  /**
   * Data structure in princip...
   * 
   * Enddate period, TaartPuntData: Map<String, SortedMap<String, Double>> Tag, Map: (Account, Saldo)
   * 
   * Enddate, Map:(Tag, Map:(Account, Saldo))
   */
  private Map<LocalDate, TaartPuntData> m_Kolommen = new TreeMap<LocalDate, TaartPuntData>();
  private Map<LocalDate, TaartPuntData> m_KolomDelta = new TreeMap<LocalDate, TaartPuntData>();
  private TaartPuntData taartPuntPrev = null;
  private boolean b_calculated = false;

  public DetermineDeltas(Map<LocalDate, TaartPuntData> a_Kolommen) {
    bundle.changeLanguage(m_param.get_Language());
    m_Kolommen = new TreeMap<LocalDate, TaartPuntData>(a_Kolommen);
  }

  public Map<LocalDate, TaartPuntData> getDeltas() {
    if (!b_calculated) {
      lOGGER.log(Level.INFO, bundle.getMessage("app.CalcDiff"));
      calcDeltas();
    }
    return m_KolomDelta;
  }

  void calcDeltas() {
    Set<LocalDate> keys = m_Kolommen.keySet();
    keys.forEach(key -> {
      TaartPuntData taartPunt = new TaartPuntDataImpl(m_Kolommen.get(key));
      TaartPuntData taartPuntBck = new TaartPuntDataImpl(m_Kolommen.get(key));
      if (taartPuntPrev != null) {
        Set<String> keytags = taartPunt.getTags();
        keytags.forEach(keytag -> {
          SortedMap<String, Double> pieSlices = new TreeMap<String, Double>(
              (SortedMap<String, Double>) taartPunt.getPieSlices(keytag));
          Set<String> pieSlicesKeys = pieSlices.keySet();
          pieSlicesKeys.forEach(pieSlicesKey -> {
            Double l_Amt = pieSlices.get(pieSlicesKey);

            SortedMap<String, Double> piePrevSlices = new TreeMap<String, Double>(
                (SortedMap<String, Double>) taartPuntPrev.getPieSlices(keytag));
            Double l_amtPrev = piePrevSlices.get(pieSlicesKey);
            Double l_Amtdiff = l_Amt - l_amtPrev;
            pieSlices.put(pieSlicesKey, l_Amtdiff);
            lOGGER.log(Level.FINE,
                keytag + "|" + pieSlicesKey + " PrvAmt:" + l_amtPrev + " | Amt: " + l_Amt + " | AmtDiff: " + l_Amtdiff);
          });
          taartPunt.putPieSlices(keytag, pieSlices);
        });
        m_KolomDelta.put(key, taartPunt);
        taartPuntPrev = new TaartPuntDataImpl(taartPuntBck);
      } else {
        taartPuntPrev = new TaartPuntDataImpl(taartPunt);
        Set<String> keytags = taartPunt.getTags();
        keytags.forEach(keytag -> {
          SortedMap<String, Double> pieSlices = new TreeMap<String, Double>(
              (SortedMap<String, Double>) taartPunt.getPieSlices(keytag));
          Set<String> pieSlicesKeys = pieSlices.keySet();
          pieSlicesKeys.forEach(pieSlicesKey -> {
            pieSlices.put(pieSlicesKey, 0.0);
          });
          taartPunt.putPieSlices(keytag, pieSlices);
        });
        m_KolomDelta.put(key, taartPunt);
      }
    });
    b_calculated = true;
  }

}
