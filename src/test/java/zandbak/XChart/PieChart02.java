package zandbak.XChart;

import java.awt.Color;
import java.util.function.Function;
import java.util.stream.Stream;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
// import org.knowm.xchart.demo.charts.ExampleChart;

/**
 * Pie Chart Custom Color Palette
 *
 * <p>
 * Demonstrates the following:
 *
 * <ul>
 * <li>Pie Chart
 * <li>PieChartBuilder
 * <li>Custom series palette
 * <li>Value Annotations
 * <li>Tooltips
 */
public class PieChart02 implements ExampleChart<PieChart> {

  public static void main(String[] args) {

    ExampleChart<PieChart> exampleChart = new PieChart02();
    PieChart chart = exampleChart.getChart();
    SwingWrapper<PieChart> wrapper = new SwingWrapper<>(chart);
    wrapper.displayChart();
    wrapper.getXChartPanel().setToolTipsEnabled(true);
  }

  @Override
  public PieChart getChart() {

    // Create Chart
    PieChart chart = new PieChartBuilder().width(800).height(600)
        .title(getClass().getSimpleName()).build();
    //@formatter:off
    int total = Stream.of(
        chart.addSeries("Beleggingen", 365026.53),
        chart.addSeries("Deposito", 25000.00),
        chart.addSeries("Goud", 238836.00),
        chart.addSeries("Lopend", 504.30),
        chart.addSeries("Obligaties", 24874.99),
        chart.addSeries("Sparen", 451867.45))
        .map(PieSeries::getValue)
        .mapToInt(Number::intValue).sum();
    // Customize Chart
    Color[] sliceColors = new Color[6];
    sliceColors[0] = Color.BLUE;
    sliceColors[1] = Color.RED;
    sliceColors[2] = Color.YELLOW;
    sliceColors[3] = Color.GRAY;
    sliceColors[4] = Color.MAGENTA;
    sliceColors[5] = Color.GREEN;
     chart.getStyler().setSeriesColors(sliceColors);
    chart.getStyler().setCustomSeriesLabelFunction(generateSeriesLabel(total));
    // chart.getStyler().setDecimalPattern("#0.000");
    // chart.getStyler().setToolTipsAlwaysVisible(true);
    //@formatter:on

    return chart;
  }

  @Override
  public void customizePanel(XChartPanel<PieChart> panel) {

    panel.setToolTipsEnabled(true);
  }

  @Override
  public String getExampleChartName() {

    return getClass().getSimpleName() + " - Pie Chart Custom Color Palette";
  }

  private Function<PieSeries, String> generateSeriesLabel(int total) {
    return pieSeries -> {
      double percent = (pieSeries.getValue().doubleValue() / total) * 100;
      return String.format("%s (%.2f%%)", pieSeries.getValue(), percent);
    };
  }
}