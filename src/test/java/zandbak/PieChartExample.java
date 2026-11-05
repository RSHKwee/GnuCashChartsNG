package zandbak;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PieChartExample extends Application {
  // Record voor één rij uit de CSV
  public record Rij(String tag, String onderwerp, double totaal) {
  }

  @Override
  public void start(Stage stage) {
    Path csvPad = Path.of("PieDataNG.csv");
    DateTimeFormatter datumFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd");

    // 1. CSV inlezen
    List<Rij> rijen = new ArrayList<>();
    try {
      rijen = leesCsv(csvPad, datumFormatter);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // 1. Bereid gegevens voor: elk Data-object is een cirkelsegment
    // ObservableList<PieChart.Data> pieData =
    // FXCollections.observableArrayList(
    // new PieChart.Data("Grapefruit", 13), new PieChart.Data("Oranges", 25),
    // new PieChart.Data("Plums", 10), new PieChart.Data("Pears", 22),
    // new PieChart.Data("Apples", 30));
    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    rijen.forEach(rij -> {
      if (rij.tag.equalsIgnoreCase("RKVermogen")) {
        PieChart.Data data = new PieChart.Data(rij.onderwerp(), rij.totaal());
        pieData.add(data);
      }
    });

    // 2. Maak het cirkeldiagram
    PieChart pieChart = new PieChart(pieData);
    pieChart.setTitle("Imported Fruits");

    // Optionele instellingen
    pieChart.setLabelsVisible(true); // Toon labels
    pieChart.setLegendVisible(true); // Toon legenda
    pieChart.setClockwise(true); // Met de klok mee rangschikken

    for (PieChart.Data data : pieData) {
      // Bereken het percentage (optioneel)
      double total = pieData.stream().mapToDouble(PieChart.Data::getPieValue)
          .sum();
      double percent = (data.getPieValue() / total) * 100;

      // Maak de tooltip-tekst
      String tipText = String.format("%s: %.0f (%.1f%%)", data.getName(),
          data.getPieValue(), percent);

      // Installeer de tooltip op de Node van het segment
      Tooltip tooltip = new Tooltip(tipText);
      Tooltip.install(data.getNode(), tooltip);
    }

    // 3. Toon het
    VBox root = new VBox(pieChart);
    Scene scene = new Scene(root, 500, 500);
    stage.setScene(scene);
    stage.show();

  }

  public static void main(String[] args) {
    launch(args);
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
        if (velden.length < 2) {
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

          // Vervang eventueel komma door punt als decimaalteken
          double totaal = parseBedrag(velden[2].trim());

          resultaat.add(new Rij(tag, onderwerp, totaal));
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
