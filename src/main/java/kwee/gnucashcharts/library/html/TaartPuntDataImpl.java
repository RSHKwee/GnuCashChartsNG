package kwee.gnucashcharts.library.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import kwee.gnucashcharts.library.TaartPuntData;

public class TaartPuntDataImpl implements TaartPuntData {
  // m_Tags : tag, SliceName, SliceAmount
  private Map<String, SortedMap<String, Double>> m_Tags = new TreeMap<String, SortedMap<String, Double>>();

  public TaartPuntDataImpl() {
  }

  public void putData(ArrayList<String> a_regels) {
    ArrayList<DataRij> tablerows = new ArrayList<DataRij>();
    a_regels.forEach(regel -> {
      DataRij data = new DataRij(regel);
      tablerows.add(data);
    });

    tablerows.forEach(row -> {
      if (!row.getRemark().isBlank()) {
        String[] elems = row.getRemark().split(",");
        for (int i = 0; i < elems.length; i++) {
          String[] tags = elems[i].split(":");
          String tag = tags[0].strip();
          if (tags.length >= 2) {
            String slice = tags[1].strip();
            SortedMap<String, Double> slices = m_Tags.get(tag);
            if (slices == null) {
              slices = new TreeMap<String, Double>();
              slices.put(slice, row.getAmount());
              m_Tags.put(tag, slices);
            } else {
              double amt = 0.0;
              try {
                amt = slices.get(slice);
              } catch (Exception e) {
                // Do nothing...
              }
              amt = amt + row.getAmount();
              slices.put(slice, amt);
              m_Tags.put(tag, slices);
            }
          }
        }
      }
    });
  }

  public Set<String> getTags() {
    return m_Tags.keySet();
  }

  public Map<String, Double> getPieSlices(String a_Tag) {
    Map<String, Double> l_Slices = m_Tags.get(a_Tag);
    if (l_Slices == null) {
      l_Slices = new HashMap<String, Double>();
    }
    return l_Slices;
  }

  @Override
  public ArrayList<String> getSubjects(String a_Tag) {
    Map<String, Double> l_Slices = m_Tags.get(a_Tag);
    Set<String> l_subjects = l_Slices.keySet();
    ArrayList<String> la_subjects = new ArrayList<String>(l_subjects);
    return la_subjects;
  }

  @Override
  public Map<String, SortedMap<String, Double>> getm_Tags() {
    return m_Tags;
  }

  @Override
  public void putPieSlices(String a_Tag, SortedMap<String, Double> a_Slice) {
    m_Tags.put(a_Tag, a_Slice);
  }

}
