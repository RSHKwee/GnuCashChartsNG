package zandbak;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import zandbak.CsvNaarXYChart.Rij;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StackedBarPerTag {

  public record Rij(String tag, String onderwerp, Date datum, double totaal) {
  }

  public static void main(String[] args) {
    Path csvPad = Path.of("BarDataNG.csv");
    DateTimeFormatter datumFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd");

    // 1. CSV inlezen
    List<Rij> alleRijen = new ArrayList<Rij>();
    try {
      alleRijen = leesCsv(csvPad, datumFormatter);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // 2. Kies de Tag (bijv. via een parameter of invoer)
    String gekozenTag = "RKVermogen";

    // 3. Filter op de gekozen Tag
    List<Rij> rijen = alleRijen.stream().filter(r -> r.tag().equals(gekozenTag))
        .toList();

    // 4. Verzamel alle unieke datums (X-as categorieën), gesorteerd
    List<Date> datums = rijen.stream().map(Rij::datum).distinct().sorted()
        .toList();

    // 5. Verzamel alle unieke onderwerpen (worden de gestapelde series)
    List<String> onderwerpen = rijen.stream().map(Rij::onderwerp).distinct()
        .sorted().toList();

    // 6. Maak een map: onderwerp -> (datum -> totaal)
    Map<String, Map<Date, Double>> dataPerOnderwerp = rijen.stream()
        .collect(Collectors.groupingBy(Rij::onderwerp,
            Collectors.toMap(Rij::datum, Rij::totaal, Double::sum)));

    // 7. CategoryChart opbouwen
    CategoryChart chart = new CategoryChartBuilder().width(800).height(600)
        .title("Totaal per onderwerp per datum (Tag: " + gekozenTag + ")")
        .xAxisTitle("Datum").yAxisTitle("Totaal").build();

    // 8. Stacking aanzetten + datumnotatie
    chart.getStyler().setStacked(true);
    chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
    chart.getStyler().setPlotGridVerticalLinesVisible(false);
    chart.getStyler().setDatePattern("dd-MM-yyyy");

    // Definieer een formatter: geen wetenschappelijke notatie, duizendtallen
    // scheiden met komma's (of pas aan naar wens)
    DecimalFormat yFormatter = new DecimalFormat("#,###.##");

    chart.getStyler().setYAxisTickLabelsFormattingFunction(value -> {
      return yFormatter.format(value);
    });

    // Stel de legendapositie in op "buiten de grafiek, onderzijde"
    chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);

    // Stel de legendalay-out in op horizontaal
    chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);

    // 9. Voeg per onderwerp een serie toe
    for (String onderwerp : onderwerpen) {
      Map<Date, Double> waarden = dataPerOnderwerp.getOrDefault(onderwerp,
          Map.of());

      // Zorg dat elke serie dezelfde datums en volgorde heeft
      List<Number> yData = datums.stream()
          .map(d -> waarden.getOrDefault(d, 0.0)).collect(Collectors.toList());

      // Datums als List<Date> of List<LocalDate> meegeven
      chart.addSeries(onderwerp, datums, yData);
    }
    // Maak het paneel aan en schakel tooltips in
    XChartPanel<CategoryChart> panel = new XChartPanel<>(chart);
    panel.setToolTipsEnabled(true); // Kerninstelling: hover-tooltips
                                    // inschakelen
    chart.getStyler().setToolTipBackgroundColor(Color.LIGHT_GRAY);
    chart.getStyler().setToolTipBorderColor(Color.BLACK);
    chart.getStyler().setToolTipHighlightColor(Color.CYAN); // Highlight-kleur
                                                            // van de staaf bij
                                                            // hover
    SwingWrapper<CategoryChart> sw = new SwingWrapper<CategoryChart>(chart);
    sw.displayChart(); // eerst weergeven, paneel initialiseren
    sw.getXChartPanel().setToolTipsEnabled(true);

    // Toon het venster
    // new SwingWrapper<>(panel).displayChart();
    System.out.println();
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
//          double totaal = Double
          // .parseDouble(velden[3].trim().replace(',', '.'));
          double totaal = parseBedrag(velden[3].trim());
          Date date = Date
              .from(datum.atStartOfDay(ZoneId.systemDefault()).toInstant());

          resultaat.add(new Rij(tag, onderwerp, date, totaal));
        } catch (Exception e) {
          System.err.println(
              "Fout bij parsen regel '" + regel + "': " + e.getMessage());
        }
      }
    }
    return resultaat;
  }

  private static double parseBedrag(String tekst) {
    if (tekst == null || tekst.isBlank()) {
      throw new IllegalArgumentException("Leeg bedrag");
    }
    // Verwijder duizendtalscheiders (punten) en vervang decimaalkomma door punt
    String genormaliseerd = tekst.trim().replace(".", "") // duizendtallen weg
        .replace(',', '.'); // decimaalteken
    return Double.parseDouble(genormaliseerd);
  }
}