package zandbak;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StackedBarChartExample extends Application {
    @Override
    public void start(Stage stage) {
        // 1. Definieer de categorieën van de x-as (bijv. jaren)
        CategoryAxis xAxis = new CategoryAxis(
            FXCollections.observableArrayList("2007", "2008", "2009")
        );
        xAxis.setLabel("Year");

        // 2. Definieer de numerieke as van de y-as
        NumberAxis yAxis = new NumberAxis("Units Sold", 0, 10000, 1000);

        // 3. Maak gegevensreeksen (elke reeks is een "laag" in de stapel)
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Regio 1");
        series1.getData().add(new XYChart.Data<>("2007", 567));
        series1.getData().add(new XYChart.Data<>("2008", 1292));
        series1.getData().add(new XYChart.Data<>("2009", 2180));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Regio 2");
        series2.getData().add(new XYChart.Data<>("2007", 956));
        series2.getData().add(new XYChart.Data<>("2008", 1665));
        series2.getData().add(new XYChart.Data<>("2009", 2450));

        XYChart.Series<String, Number> series3 = new XYChart.Series<>();
        series3.setName("Regio 3");
        series3.getData().add(new XYChart.Data<>("2007", 800));
        series3.getData().add(new XYChart.Data<>("2008", 1000));
        series3.getData().add(new XYChart.Data<>("2009", 2800));

        // 4. Maak het gestapelde staafdiagram en voeg de gegevens toe
        ObservableList<XYChart.Series<String, Number>> barData =
            FXCollections.observableArrayList(series1, series2, series3);

        StackedBarChart<String, Number> stackedChart =
            new StackedBarChart<>(xAxis, yAxis, barData);
        stackedChart.setTitle("Units Sold per Year");

        // 5. Toon het
        VBox root = new VBox(stackedChart);
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}