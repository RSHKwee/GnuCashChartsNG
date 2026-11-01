package kwee.gnucashcharts.library.barchart;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.SnapshotParameters;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import kwee.gnucashcharts.library.FormatAmount;
import kwee.gnucashcharts.library.MessageConstants;
import kwee.library.ApplicationMessages;
import kwee.library.FX.PatchedStackedBarChart;
import kwee.logger.MyLogger;

public class BarChartToTableScene {
  private static final Logger lOGGER = MyLogger.getLogger();
  private PatchedStackedBarChart<String, Number> m_stackedBarChart;
  private TableView<OwnTableCell[]> m_tableView = new TableView<>();
  private TableView<String[]> m_tableViewString = new TableView<>();
  private TableView<String[]> m_TransposedTableView = new TableView<>();
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  private SortedMap<String, Double> m_DateTotAmt;
  private int m_NumberColomns;
  private String m_StartPeriod = "";
  private String m_EndPeriod = "";
  private String[][] m_Table;
  private String[][] m_TableStr;
  private String[][] m_TableTranspose;
  private String[] x_Header;
  private String[] y_Header;
//  private MessageText m_Messages = new MessageText();

  public BarChartToTableScene(PatchedStackedBarChart<String, Number> a_BarChart,
      SortedMap<String, Double> a_DateTotAmt) {
    m_stackedBarChart = a_BarChart;
    m_DateTotAmt = a_DateTotAmt;
    toTable();
    toTableStr();
    transposeTable();
  }

  public VBox getVBox() {
//    return new VBox(m_tableView);
    return new VBox(m_tableViewString);
  }

  public WritableImage getTableViewImage() {
    // Convert the BarChart to a WritableImage
    SnapshotParameters params = new SnapshotParameters();
    params.setDepthBuffer(true);
    WritableImage tableViewImage = m_tableViewString.snapshot(null, null);
    return tableViewImage;
  }

  public VBox getVBoxTransposed() {
    VBox lVbox = new VBox(m_TransposedTableView);
    // m_TransposedTableView.prefHeightProperty().bind(lVbox.heightProperty());
    return lVbox;
  }

  public WritableImage getTableTransposedViewImage() {
    // Convert the BarChart to a WritableImage
    SnapshotParameters params = new SnapshotParameters();
    params.setDepthBuffer(true);
    WritableImage tableViewImage = m_TransposedTableView.snapshot(null, null);
    return tableViewImage;
  }

  public String get_StartPeriod() {
    return m_StartPeriod;
  }

  public String get_EndPeriod() {
    return m_EndPeriod;
  }

  public TableView<String[]> getTransposedTable() {
    return m_TransposedTableView;
  }

  public TableView<String[]> getTable() {
    return m_tableViewString;
  }

  // Private functions
  /**
   * Create TableView with stackedBarChart as source.
   */
  private void toTable() {
    ObservableList<Series<String, Number>> series1 = m_stackedBarChart.getData();
    ObservableList<Data<String, Number>> data1 = series1.get(0).getData();
    m_NumberColomns = data1.size() + 1;

    m_Table = new String[(series1.size() + 1)][m_NumberColomns];
    x_Header = new String[series1.size() + 1];
    y_Header = new String[m_NumberColomns];

    int x = 0;
    int y = 0;
    for (XYChart.Series<String, Number> series : m_stackedBarChart.getData()) {
      x_Header[x] = series.getName();
      y = 0;
      for (XYChart.Data<String, Number> data : series.getData()) {
        if (y == 0) {
          m_Table[x][y] = series.getName();
          y_Header[y] = "Account";
          y++;
        }
        String l_xlab = data.getXValue();
        y_Header[y] = l_xlab;
        double l_amt = (double) data.getYValue();
        m_Table[x][y] = FormatAmount.formatAmount(l_amt);
        y++;
      }
      x++;
    }
    m_StartPeriod = y_Header[1];
    m_EndPeriod = y_Header[m_NumberColomns - 1];

    // Add Totals
    for (int i = 0; i < m_NumberColomns; i++) {
      if (i == 0) {
        m_Table[x][i] = "Total:";
      } else {
        double l_amt = m_DateTotAmt.get(y_Header[i]);
        m_Table[x][i] = FormatAmount.formatAmount(l_amt);
      }
    }

    // Populate the ObservableList with your String[][] data
    ObservableList<OwnTableCell[]> data = FXCollections.observableArrayList();
    for (String[] row : m_Table) {
      OwnTableCell[] rowData = Arrays.stream(row).map(OwnTableCell::new).toArray(OwnTableCell[]::new);
      data.add(rowData);
    }

    // Inside your JavaFX application class
    for (int i = 0; i < m_Table[0].length; i++) {
      TableColumn<OwnTableCell[], String> column = new TableColumn<>(y_Header[i]);
      final int columnIndex = i;
      column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columnIndex].getValue()));
      if (i != 0) {
        column.setStyle("-fx-alignment: CENTER-RIGHT;-fx-font-size: 9px;");
      }
      m_tableView.getColumns().add(column);
    }
    m_tableView.setItems(data);
  }

  /**
   * 
   */
  private void toTableStr() {
    ObservableList<Series<String, Number>> series1 = m_stackedBarChart.getData();
    ObservableList<Data<String, Number>> data1 = series1.get(0).getData();
    m_NumberColomns = data1.size() + 1;

    m_TableStr = new String[(series1.size() + 1)][m_NumberColomns];
    x_Header = new String[series1.size() + 1];
    y_Header = new String[m_NumberColomns];

    int x = 0;
    int y = 0;
    for (XYChart.Series<String, Number> series : m_stackedBarChart.getData()) {
      x_Header[x] = series.getName();
      y = 0;
      for (XYChart.Data<String, Number> data : series.getData()) {
        if (y == 0) {
          m_TableStr[x][y] = series.getName();
          y_Header[y] = "Account";
          y++;
        }
        String l_xlab = data.getXValue();
        y_Header[y] = l_xlab;
        double l_amt = (double) data.getYValue();
        m_Table[x][y] = FormatAmount.formatAmount(l_amt);
        y++;
      }
      x++;
    }
    m_StartPeriod = y_Header[1];
    m_EndPeriod = y_Header[m_NumberColomns - 1];

    // Add Totals
    for (int i = 0; i < m_NumberColomns; i++) {
      if (i == 0) {
        m_TableStr[x][i] = "Total:";
      } else {
        double l_amt = m_DateTotAmt.get(y_Header[i]);
        m_TableStr[x][i] = FormatAmount.formatAmount(l_amt);
      }
    }

    // Populate the ObservableList with your String[][] data
    ObservableList<String[]> data = FXCollections.observableArrayList();
    for (String[] row : m_TableStr) {
      data.add(row);
    }

    // Inside your JavaFX application class
    for (int i = 0; i < m_Table[0].length; i++) {
      TableColumn<String[], String> column = new TableColumn<>(y_Header[i]);
      final int columnIndex = i;
      column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columnIndex]));
      if (i != 0) {
        column.setStyle("-fx-alignment: CENTER-RIGHT;-fx-font-size: 9px;");
      }
      m_tableViewString.getColumns().add(column);
    }
    m_tableViewString.setItems(data);

    ObservableList<String[]> dataStr = FXCollections.observableArrayList();
    for (String[] row : m_Table) {
      dataStr.add(row);
    }
    m_tableViewString.setItems(dataStr);
  }

  private void transposeTable() {
    ObservableList<Series<String, Number>> series1 = m_stackedBarChart.getData();
    m_TableTranspose = new String[m_NumberColomns][(series1.size() + 1)];

    String[] lx_Header = y_Header;
    String[] ly_HeaderNew = new String[x_Header.length + 1];

    ly_HeaderNew[0] = bundle.getMessage(MessageConstants.C_DateLabel);
    System.arraycopy(x_Header, 0, ly_HeaderNew, 1, x_Header.length);
    String[] ly_Header = ly_HeaderNew;
    ly_Header[ly_HeaderNew.length - 1] = bundle.getMessage(MessageConstants.C_Total);

    for (int x = 0; x < (series1.size() + 1); x++) {
      for (int y = 0; y < m_NumberColomns; y++) {
        m_TableTranspose[y][x] = m_Table[x][y];
      }
    }

    // Populate the ObservableList with your String[][] data
    boolean First = true;
    int ix = 0;
    ObservableList<String[]> data = FXCollections.observableArrayList();
    for (String[] row : m_TableTranspose) {
      if (!First) {
        String[] rowNew = new String[row.length + 1];
        rowNew[0] = lx_Header[ix];
        System.arraycopy(row, 0, rowNew, 1, row.length);
        data.add(rowNew);
      }
      ix++;
      First = false;
    }
    // Inside your JavaFX application class
    for (int i = 0; i < m_TableTranspose[0].length + 1; i++) {
      TableColumn<String[], String> column = new TableColumn<>(ly_Header[i]);
      final int columnIndex = i;
      column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columnIndex]));
      if (i != 0) {
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
      }
      m_TransposedTableView.getColumns().add(column);
    }

    m_TransposedTableView.setItems(data);
    lOGGER.log(Level.FINE, "Transposed");
  }
}
