package kwee.gnucashcharts.gui;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.gnucashcharts.library.MessageConstants;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.gnucashcharts.library.gnuCashDb.TaartPuntDataImpl;
import kwee.library.ApplicationMessages;
import kwee.gnucashcharts.library.gnuCashDb.ReadGnuCashDB;
import kwee.gnucashcharts.library.gnuCashDb.ReadGnuCashMultiDB;
import kwee.gnucashcharts.library.gnuCashDb.SamengesteldeStaafData;

import kwee.logger.MyLogger;

public class ActionGnuCshDbStackedBarChart {
  private static final Logger lOGGER = MyLogger.getLogger();
  private int m_NrBars = 6;
  private SamengesteldeStaafData barData;
  private String m_SelectedFiles;
  private ApplicationMessages bundle = ApplicationMessages.getInstance();
  private ReadGnuCashMultiDB m_gnucashdbtables;

  /**
   * 
   * @param a_SelectedFile GnuCash file
   */
  public ActionGnuCshDbStackedBarChart(ReadGnuCashDB a_SelectedDB) {
    m_SelectedFiles = a_SelectedDB.getFile().getName();
    File[] l_files = new File[0];
    l_files[0] = a_SelectedDB.getFile();
    MainMenu.m_param.set_InputFiles(l_files);
    MainMenu.m_param.save();

    m_gnucashdbtables.addGnuCashDB(a_SelectedDB);
  }

  public ActionGnuCshDbStackedBarChart(ReadGnuCashMultiDB a_SelectedDBs) {
    File[] l_files = a_SelectedDBs.getFiles().toArray(new File[0]);
    m_SelectedFiles = "";
    a_SelectedDBs.getFiles().forEach(ll_file -> {
      m_SelectedFiles = m_SelectedFiles + " " + ll_file.getName();
    });
    MainMenu.m_param.set_InputFiles(l_files);
    MainMenu.m_param.save();

    m_gnucashdbtables = new ReadGnuCashMultiDB(a_SelectedDBs);
  }

  public SamengesteldeStaafData getData(int a_nrBars, LocalDate a_Date, boolean a_delta) {
    lOGGER.log(Level.INFO,
        bundle.getMessage(MessageConstants.C_BarChartSelections, m_SelectedFiles, Integer.toString(a_nrBars)));
    try {
      m_NrBars = a_nrBars;
      MainMenu.m_param.save();
      barData = new SamengesteldeStaafData();

      addData(a_Date);
      int year = a_Date.getYear();
      int month = a_Date.getMonthValue();
      int day = 1;

      for (int i = 0; i < m_NrBars; i++) {
        LocalDate l_Date;
        int[] l_result1 = decrMonth(year, month);
        year = l_result1[0];
        month = l_result1[1];
        l_Date = LocalDate.of(year, month, day);
        addData(l_Date);
      }
    } catch (Exception e) {
      lOGGER.log(Level.INFO, e.getMessage());
    }

    if (a_delta) {
      barData.CalcDeltas();
    }
    return barData;
  }

  // Local Functions
  private void addData(LocalDate a_Date) {
    ArrayList<String> regels = m_gnucashdbtables.getRegels(a_Date);
    TaartPuntData pieData = new TaartPuntDataImpl();
    pieData.putData(regels);

    barData.AddTaartPuntData(a_Date, pieData);
  }

  private int[] decrMonth(int a_year, int a_month) {
    int l_month = a_month - 1;
    int l_year = a_year;
    if (l_month <= 0) {
      l_month = 12;
      l_year = l_year - 1;
    }
    int[] l_result = { l_year, l_month };
    return l_result;
  }
}
