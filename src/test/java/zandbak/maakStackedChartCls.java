package zandbak;

import javafx.application.Application;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.text.SimpleDateFormat;

import java.util.*;
import java.util.stream.Collectors;

public class maakStackedChartCls {
  public record Rij(String tag, String onderwerp, Date datum, double totaal) {
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
        String tooltipText = String.format(
            "%s%nOnderwerp: %s%nDatum: %s%nBedrag: %.2f", tag, onderwerp,
            datumLabel, r.totaal());

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
    return chart;
  }
}