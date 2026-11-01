package kwee.gnucashcharts.gui;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.gnucashcharts.library.MessageConstants;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.gnucashcharts.library.gnuCashDb.ReadGnuCashDB;
import kwee.gnucashcharts.library.gnuCashDb.ReadGnuCashMultiDB;
import kwee.gnucashcharts.library.gnuCashDb.TaartPuntDataImpl;

import kwee.library.ApplicationMessages;
import kwee.logger.MyLogger;

public class ActionGnuCashDbPieChart {
  private static final Logger lOGGER = MyLogger.getLogger();
  private TaartPuntData pieData;
  private ApplicationMessages bundle = ApplicationMessages.getInstance();
  private ReadGnuCashMultiDB m_gnucashdbtables;

  public ActionGnuCashDbPieChart(ReadGnuCashDB a_SelectedDB) {
    lOGGER.log(Level.INFO,
        bundle.getMessage(MessageConstants.C_SelectedFile, a_SelectedDB.getFile().getAbsolutePath()));
    File[] ll_files = new File[0];
    ll_files[0] = a_SelectedDB.getFile().getAbsoluteFile();
    MainMenu.m_param.set_InputFiles(ll_files);
    MainMenu.m_param.save();

    m_gnucashdbtables.addGnuCashDB(a_SelectedDB);
  }

  String m_Filetext = "";

  public ActionGnuCashDbPieChart(ReadGnuCashMultiDB a_SelectedDBs) {
    List<File> l_files = a_SelectedDBs.getFiles();
    l_files.forEach(l_file -> {
      m_Filetext = m_Filetext + l_file.getAbsolutePath() + "; ";
    });

    lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_SelectedFile, m_Filetext));
    MainMenu.m_param.save();

    m_gnucashdbtables = a_SelectedDBs;
  }

  public TaartPuntData getData(LocalDate a_Date) {
    ArrayList<String> regels = m_gnucashdbtables.getRegels(a_Date);

    pieData = new TaartPuntDataImpl();
    pieData.putData(regels);
    return pieData;
  }
}
