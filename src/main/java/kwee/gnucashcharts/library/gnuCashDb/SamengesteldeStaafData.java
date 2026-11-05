package kwee.gnucashcharts.library.gnuCashDb;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import kwee.gnucashcharts.library.DetermineDeltas;
import kwee.gnucashcharts.library.FormatAmount;
import kwee.gnucashcharts.library.LocalDateAndAmount;
import kwee.gnucashcharts.library.TaartPuntData;

public class SamengesteldeStaafData {
  /**
   * Data structure in princip...
   *
   * Enddate period, TaartPuntData: Map<String, SortedMap<String, Double>> Tag,
   * Map: (Account, Saldo)
   *
   * Enddate, Map:(Tag, Map:(Account, Saldo))
   */
  private Map<LocalDate, TaartPuntData> m_Kolommen = new TreeMap<LocalDate, TaartPuntData>();

  /**
   * Data struture in princip...
   *
   * Tag, Map:(Account, Map:(Enddate, Saldo))
   */
  private Map<String, SortedMap<String, LocalDateAndAmount>> m_Series = new TreeMap<String, SortedMap<String, LocalDateAndAmount>>();

  private boolean m_first = true;
  private boolean b_delta = false;

  public SamengesteldeStaafData() {
  }

  public void AddTaartPuntData(LocalDate a_Date,
      TaartPuntData a_TaartPuntData) {
    if (m_Kolommen.get(a_Date) == null) {
      m_Kolommen.put(a_Date, a_TaartPuntData);
    } else {
      // TODO mergen TaartPuntData....
    }
  }

  public void CalcDeltas() {
    DetermineDeltas delta = new DetermineDeltas(m_Kolommen);
    if (!b_delta) {
      m_Kolommen = new TreeMap<LocalDate, TaartPuntData>(delta.getDeltas());
      b_delta = true;
    }
  }

  /**
   *
   * @return Data structure: Tag,
   */
  public Map<String, SortedMap<String, LocalDateAndAmount>> getSeries() {
    if (m_first) {
      transposeKolommen();
      m_first = false;
    }
    return m_Series;
  }

  /**
   *
   */
  ArrayList<String> lOutStr = new ArrayList<String>();

  public ArrayList<String> print() {
    String l_delim = ";";
    lOutStr.clear();
    Set<String> tagKeys = m_Series.keySet();
    tagKeys.forEach(tagKey -> {
      SortedMap<String, LocalDateAndAmount> l_LocDatAmt = m_Series.get(tagKey);
      Set<String> l_LocDatAmtKeys = l_LocDatAmt.keySet();
      l_LocDatAmtKeys.forEach(l_LocDatAmtKey -> {
        LocalDateAndAmount l_locdata = l_LocDatAmt.get(l_LocDatAmtKey);
        Map<LocalDate, Double> l_DateAmts = l_locdata.getDateAmount();
        Set<LocalDate> l_DateAmts_Key = l_DateAmts.keySet();
        l_DateAmts_Key.forEach(l_DateKey -> {
          String lstr = "";
          lstr = tagKey + l_delim + l_LocDatAmtKey + l_delim + l_DateKey
              + l_delim + FormatAmount
                  .formatAmountNoSymbol(l_locdata.getAmount(l_DateKey));
          lOutStr.add(lstr);
        });
      });
    });
    return lOutStr;
  }

  private void transposeKolommen() {
    Set<LocalDate> keys = m_Kolommen.keySet();
    SortedSet<LocalDate> sortKeys = new TreeSet<LocalDate>(keys);
    sortKeys.forEach(l_datekey -> {
      TaartPuntData ldate = m_Kolommen.get(l_datekey);
      Set<String> tags = ldate.getTags();
      tags.forEach(tag -> {
        Map<String, Double> slices = ldate.getPieSlices(tag);
        Set<String> l_accKeys = slices.keySet();
        l_accKeys.forEach(account -> {
          double totAmt = slices.get(account);
          if (m_Series.get(tag) == null) {
            LocalDateAndAmount ldatamt = new LocalDateAndAmount();
            ldatamt.addDateAmount(l_datekey, totAmt);
            SortedMap<String, LocalDateAndAmount> lAccDateAmt = new TreeMap<String, LocalDateAndAmount>();
            lAccDateAmt.put(account, ldatamt);
            m_Series.put(tag, lAccDateAmt);
          } else {
            SortedMap<String, LocalDateAndAmount> lAccDateAmt = m_Series
                .get(tag);
            if (lAccDateAmt.get(account) == null) {
              LocalDateAndAmount ldatamt = new LocalDateAndAmount();
              ldatamt.addDateAmount(l_datekey, totAmt);
              lAccDateAmt.put(account, ldatamt);
              m_Series.put(tag, lAccDateAmt);
            } else {
              LocalDateAndAmount ldatamt = lAccDateAmt.get(account);
              ldatamt.addDateAmount(l_datekey, totAmt);
              lAccDateAmt.put(account, ldatamt);
              m_Series.put(tag, lAccDateAmt);
            }
          }
        });
      });
    });
  }

}
