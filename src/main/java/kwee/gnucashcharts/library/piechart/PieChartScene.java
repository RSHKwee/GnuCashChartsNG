package kwee.gnucashcharts.library.piechart;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import kwee.gnucashcharts.library.SubjectsColors;
import kwee.gnucashcharts.library.FormatAmount;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.logger.MyLogger;

public class PieChartScene {
  private static final Logger lOGGER = MyLogger.getLogger();
  // private static final long serialVersionUID = -709619748064818482L;
  private int m_Teller = 0;
  private double m_total_amount = 0.0;
  private PieChart m_PieChart;
  private GridPane m_legendGrid;
  private PieChart.Data[] m_pieChartData;
  private SubjectsColors m_AccColors;

  public PieChartScene(TaartPuntData pieData, String tag, SubjectsColors a_AccColors) {
    // Create a pie chart & a GridPane to hold the legend and tooltips
    m_AccColors = a_AccColors;
    createPieChart(pieData.getPieSlices(tag));
    createLegenAndTooltipsdGrid();
  }

  public PieChart getPieChart() {
    return m_PieChart;
  }

  public GridPane getLegend() {
    return m_legendGrid;
  }

  public Scene getScene(VBox SaveDialog) {
    SaveDialog.setAlignment(Pos.CENTER_RIGHT); // Set alignment to right

    // Create a VBox to hold the pie chart and the legend
    VBox vbox = new VBox(m_PieChart, m_legendGrid, SaveDialog);

    // Set up the scene and add the VBox to it
    Scene scene = new Scene(vbox, 800, 600);

    adjustColors();
    m_PieChart.setLegendVisible(false);

    return scene;
  }

  public Scene getScene(SubjectsColors a_AccColors) {
    // Create a VBox to hold the pie chart and the legend
    VBox vbox = new VBox(m_PieChart, m_legendGrid);

    // Set up the scene and add the VBox to it
    Scene scene = new Scene(vbox, 800, 600);

    adjustColors();
    m_PieChart.setLegendVisible(false);

    return scene;
  }

  public double getTotalAmount() {
    double number = Math.round(m_total_amount * 100);
    number = number / 100;
    return number;
  }

  public WritableImage getPieChartImage() {
    // Convert the PieChart to a WritableImage
    WritableImage pieChartImage = m_PieChart.snapshot(null, null);
    return pieChartImage;
  }

  public WritableImage getLegendImage() {
    // Convert the PieChart to a WritableImage
    WritableImage legendImage = m_legendGrid.snapshot(null, null);
    return legendImage;
  }

  // private functions
  private void adjustColors() {
    ObservableList<Data> observableList = m_PieChart.getData();
    m_Teller = 0;
    observableList.forEach(item -> {
      Color AccKleur = m_AccColors.getColor(item.getName());
      // Color Kleur = getDistinctColor(m_Teller, observableList.size());
      item.getNode().setStyle("-fx-pie-color: " + m_AccColors.toHex(AccKleur) + ";");
      m_Teller++;
    });
  }

  private void createPieChart(Map<String, Double> taartpuntdata) {
    lOGGER.log(Level.FINE, "#Taartpunt data: " + taartpuntdata.size());
    PieChart.Data[] l_pieChartData = new PieChart.Data[taartpuntdata.size()];
    m_total_amount = 0.0;
    m_Teller = 0;

    Set<String> keys = taartpuntdata.keySet();
    keys.forEach(key -> {
      double ll_amt = taartpuntdata.get(key);
      if (ll_amt > 0) {
        l_pieChartData[m_Teller] = new PieChart.Data(key, taartpuntdata.get(key));
        double l_amt = l_pieChartData[m_Teller].getPieValue();
        m_total_amount = m_total_amount + l_amt;

        lOGGER.log(Level.FINE, "i:" + m_Teller + " Key:" + key + " value:" + l_pieChartData[m_Teller].getPieValue());
        m_Teller++;
      } else {
        // lOGGER.log(Level.FINE, "i:" + m_Teller + " Key:" + key + " value:" +
        // l_pieChartData[m_Teller].getPieValue());
      }
    });

    m_pieChartData = new PieChart.Data[m_Teller];
    for (int i = 0; i < m_Teller; i++) {
      m_pieChartData[i] = l_pieChartData[i];
    }

    // Create the pie chart and add the data slices to it
    m_PieChart = new PieChart();
    m_PieChart.getData().addAll(m_pieChartData);
  }

  private void createLegenAndTooltipsdGrid() {
    m_legendGrid = new GridPane();
    m_legendGrid.setCenterShape(true);
    m_legendGrid.setHgap(20);
    m_legendGrid.setVgap(5);
    m_legendGrid.setAlignment(Pos.CENTER);

    int row = 0;
    int col = 0;
    m_Teller = 0;
    for (PieChart.Data data : m_pieChartData) {
      Color AccKleur = m_AccColors.getColor(data.getName());
      // Color Kleur = getDistinctColor(m_Teller, observableList.size());
      Label colorBox = new Label();
      colorBox.setPrefSize(15, 15);
      colorBox.setStyle("-fx-background-color: " + m_AccColors.toHex(AccKleur) + ";");

      double amt = data.getPieValue();
      int perc = (int) ((amt / m_total_amount) * 10000.0);
      Label label = new Label(data.getName() + " - " + perc / 100.0 + "%");
      label.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

      Tooltip tooltip = new Tooltip(data.getName() + " " + FormatAmount.formatAmount(amt));
      Tooltip.install(m_pieChartData[m_Teller].getNode(), tooltip);

      m_legendGrid.add(colorBox, col * 2, row);
      m_legendGrid.add(label, col * 2 + 1, row);

      col++;
      if (col == 3) {
        col = 0;
        row++;
      }
      m_Teller++;
    }
  }
}
