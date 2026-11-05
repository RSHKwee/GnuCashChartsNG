package zandbak;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;

import zandbak.CsvNaarXYChart.Rij;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StackedBarVoorbeeld {
  public record Rij(String tag, String onderwerp, LocalDate datum,
      double totaal) {
  }

  public static void main(String[] args) {
    Path csvPad = Path.of("BarDataNG.csv");
    DateTimeFormatter datumFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd");

    // 1. CSV inlezen
    List<Rij> rijen;
    try {
      rijen = leesCsv(csvPad, datumFormatter);

      // 1. Verzamel alle unieke onderwerpen (X-as categorieën)
      List<String> onderwerpen = rijen.stream().map(Rij::onderwerp).distinct()
          .sorted().toList();

      // 2. Groepeer per Tag en map naar totalen per onderwerp
      Map<String, Map<String, Double>> totalenPerTag = rijen.stream()
          .collect(Collectors.groupingBy(Rij::tag,
              Collectors.toMap(Rij::onderwerp, Rij::totaal, Double::sum)));

      // 3. Maak de CategoryChart
      CategoryChart chart = new CategoryChartBuilder().width(800).height(600)
          .title("Totaal per Onderwerp (gestapeld per Tag)")
          .xAxisTitle("Onderwerp").yAxisTitle("Totaal").build();

      // 4. BELANGRIJK: zet stacking aan
      chart.getStyler().setStacked(true);
      chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
      chart.getStyler().setPlotGridVerticalLinesVisible(false);

      // 5. Voeg per Tag een serie toe
      for (Map.Entry<String, Map<String, Double>> entry : totalenPerTag
          .entrySet()) {
        String tag = entry.getKey();
        Map<String, Double> waarden = entry.getValue();

        // Zorg dat elke serie dezelfde volgorde van onderwerpen heeft
        List<Number> yData = onderwerpen.stream()
            .map(o -> waarden.getOrDefault(o, 0.0))
            .collect(Collectors.toList());

        chart.addSeries(tag, onderwerpen, yData);
      }

      new SwingWrapper<>(chart).displayChart();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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