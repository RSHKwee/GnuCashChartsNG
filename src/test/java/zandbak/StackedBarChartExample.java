package zandbak;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StackedBarChartExample extends Application {
  public record Rij(String tag, String onderwerp, Date datum, double totaal) {
  }

  @Override
  public void start(Stage stage) {
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

    StackedBarChart<String, Number> stackedChart = maakStackedChart(gekozenTag,
        alleRijen, "Jaar", "Bedrag");

    // 5. Toon het
    VBox root = new VBox(stackedChart);
    Scene scene = new Scene(root, 600, 500);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  public StackedBarChart<String, Number> maakStackedChart(String tag,
      List<Rij> rijen, String xAxisLabel, String yAxisLabel) {

    List<Rij> gefilterd = rijen.stream().filter(r -> tag.equals(r.tag()))
        .toList();

    Map<String, List<Rij>> perOnderwerp = gefilterd.stream().collect(Collectors
        .groupingBy(Rij::onderwerp, LinkedHashMap::new, Collectors.toList()));

    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

    for (Map.Entry<String, List<Rij>> entry : perOnderwerp.entrySet()) {
      String onderwerp = entry.getKey();
      XYChart.Series<String, Number> series = new XYChart.Series<>();
      series.setName(onderwerp);

      List<Rij> gesorteerd = entry.getValue().stream()
          .sorted(Comparator.comparing(Rij::datum)).toList();

      for (Rij r : gesorteerd) {
        String datumLabel = fmt.format(r.datum());

        XYChart.Data<String, Number> data = new XYChart.Data<>(datumLabel,
            r.totaal());

        // Tooltip-tekst vooraf opbouwen (veiliger dan pas bij hover)
        String tooltipText = String.format("%s%n%s%n€%.2f", datumLabel,
            onderwerp, r.totaal());

        // BELANGRIJK: node bestaat pas later -> luisteren naar nodeProperty
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
          if (newNode != null) {
            Tooltip tip = new Tooltip(tooltipText);
            tip.setShowDelay(Duration.millis(150));
            tip.setHideDelay(Duration.millis(50));
            Tooltip.install(newNode, tip);

            // Optioneel: highlight bij hover
            newNode
                .setOnMouseEntered(e -> newNode.setStyle("-fx-opacity: 0.8;"));
            newNode.setOnMouseExited(e -> newNode.setStyle(""));
          }
        });

        series.getData().add(data);
      }
      seriesList.add(series);
    }

    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel(xAxisLabel);
    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(yAxisLabel);

    StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
    chart.getData().addAll(seriesList);
    chart.setTitle("Vermogen");
    return chart;
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
          if (regel.startsWith("#")) {
            // Comments
            continue;
          } else {
            System.err.println("Ongeldige regel overgeslagen: " + regel);
            continue;
          }
        }

        try {
          String tag = velden[0].trim();
          String onderwerp = velden[1].trim();
          LocalDate datum = LocalDate.parse(velden[2].trim(), datumFormatter);

          // Vervang eventueel komma door punt als decimaalteken
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