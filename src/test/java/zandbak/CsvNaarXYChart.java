package zandbak;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CsvNaarXYChart {

  // Record voor één rij uit de CSV
  public record Rij(String tag, String onderwerp, LocalDate datum,
      double totaal) {
  }

  public static void main(String[] args) throws IOException {
    Path csvPad = Path.of("BarDataNG.csv");
    DateTimeFormatter datumFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd");

    // 1. CSV inlezen
    List<Rij> rijen = leesCsv(csvPad, datumFormatter);

    // 2. Groepeer per Tag en sorteer op datum
    Map<String, List<Rij>> perTag = rijen.stream()
        .collect(Collectors.groupingBy(Rij::tag, LinkedHashMap::new,
            Collectors.collectingAndThen(Collectors.toList(), list -> list
                .stream().sorted(Comparator.comparing(Rij::datum)).toList())));

    // 3. XYChart opbouwen
    XYChart chart = new XYChartBuilder().width(800).height(600)
        .title("Totaal per Tag over tijd").xAxisTitle("Datum")
        .yAxisTitle("Totaal").build();

    chart.getStyler().setDatePattern("MMM yyyy");
    chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);

    // 4. Per Tag een serie toevoegen
    for (Map.Entry<String, List<Rij>> entry : perTag.entrySet()) {
      List<LocalDate> datums = entry.getValue().stream().map(Rij::datum)
          .toList();
      List<Double> totalen = entry.getValue().stream().map(Rij::totaal)
          .toList();

      XYSeries series = chart.addSeries(entry.getKey(), datums, totalen);
      series.setMarker(SeriesMarkers.CIRCLE);
    }

    new SwingWrapper<>(chart).displayChart();
  }

  /**
   * Leest een puntkomma-gescheiden CSV-bestand met header:
   * Tag;Onderwerp;Datum;Totaal
   */
  private static List<Rij> leesCsv(Path pad, DateTimeFormatter datumFormatter)
      throws IOException {

    List<Rij> resultaat = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(pad,
        StandardCharsets.UTF_8)) {
      String regel;
      boolean eersteRegel = true;

      while ((regel = reader.readLine()) != null) {
        // Header overslaan
        if (eersteRegel) {
          eersteRegel = false;
          // Optioneel: controleer of header klopt
          continue;
        }

        // Lege regels overslaan
        if (regel.isBlank()) {
          continue;
        }

        // Splits op puntkomma; -1 zodat lege laatste velden behouden blijven
        String[] velden = regel.split(";", -1);
        if (velden.length < 4) {
          System.err.println("Ongeldige regel overgeslagen: " + regel);
          continue;
        }

        try {
          String tag = velden[0].trim();
          String onderwerp = velden[1].trim();
          LocalDate datum = LocalDate.parse(velden[2].trim(), datumFormatter);
          // Vervang eventueel komma door punt als decimaalteken
          double totaal = Double
              .parseDouble(velden[3].trim().replace(',', '.'));

          resultaat.add(new Rij(tag, onderwerp, datum, totaal));

        } catch (Exception e) {
          System.err.println(
              "Fout bij parsen regel '" + regel + "': " + e.getMessage());
        }
      }
    }

    return resultaat;
  }
}
