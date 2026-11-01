package kwee.gnucashcharts.library;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface TaartPuntData {

  public void putData(ArrayList<String> a_Regels);

  public Set<String> getTags();

  public Map<String, Double> getPieSlices(String a_Tag);

  public ArrayList<String> getSubjects(String a_Tag);

  public Map<String, SortedMap<String, Double>> getm_Tags();

  void putPieSlices(String a_Tag, SortedMap<String, Double> a_Slice);

}
