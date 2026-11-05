package zandbak;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class XChartDatumVoorbeeld {

  // Aanname: je hebt een record zoals eerder
  public record Rij(String tag, String onderwerp, LocalDate datum,
      double totaal) {
  }

  public static void main(String[] args) {
    // Voorbeelddata
    List<Rij> rijen = List.of(
        new Rij("A", "Onderwerp 1", LocalDate.of(2026, 1, 1), 10.0),
        new Rij("A", "Onderwerp 2", LocalDate.of(2026, 2, 1), 15.0),
        new Rij("A", "Onderwerp 3", LocalDate.of(2026, 3, 1), 12.0),
        new Rij("B", "Onderwerp 1", LocalDate.of(2026, 1, 1), 8.0),
        new Rij("B", "Onderwerp 2", LocalDate.of(2026, 2, 1), 20.0),
        new Rij("B", "Onderwerp 3", LocalDate.of(2026, 3, 1), 18.0));

    // 1. Groepeer per Tag en sorteer op datum
    Map<String, List<Rij>> perTag = rijen.stream()
        .collect(Collectors.groupingBy(Rij::tag, LinkedHashMap::new,
            Collectors.collectingAndThen(Collectors.toList(), list -> list
                .stream().sorted(Comparator.comparing(Rij::datum)).toList())));

    // 2. Maak een XYChart
    XYChart chart = new XYChartBuilder().width(800).height(600)
        .title("Totaal per Tag over tijd").xAxisTitle("Datum")
        .yAxisTitle("Totaal").build();

    // 3. Datumnotatie voor de X-as (bijv. "MMM yyyy")
    chart.getStyler().setDatePattern("MMM yyyy");
    // chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

    // 4. Voeg per Tag een serie toe
    for (Map.Entry<String, List<Rij>> entry : perTag.entrySet()) {
      String tag = entry.getKey();
      List<Rij> data = entry.getValue();

      List<LocalDate> datums = data.stream().map(Rij::datum).toList();
      List<Double> totalen = data.stream().map(Rij::totaal).toList();

      // LocalDate wordt direct geaccepteerd door XChart [citation:8]
      XYSeries series = chart.addSeries(tag, datums, totalen);
      series.setMarker(SeriesMarkers.CIRCLE); // optioneel: markers tonen
    }

    // 5. Toon de chart
    new SwingWrapper<>(chart).displayChart();
  }
}
