package kwee.gnucashcharts.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.gnucashcharts.library.MessageConstants;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.gnucashcharts.library.html.ReadHTMLTable;
import kwee.gnucashcharts.library.html.TaartPuntDataImpl;
import kwee.library.ApplicationMessages;
import kwee.logger.MyLogger;

public class ActionHTMLPieChart {
  private static final Logger lOGGER = MyLogger.getLogger();
  private TaartPuntData pieData;
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  public ActionHTMLPieChart(File a_SelectedFile) {
    lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_SelectedFile, a_SelectedFile.getAbsolutePath()));
    File[] ll_files = new File[0];
    ll_files[0] = a_SelectedFile.getAbsoluteFile();
    MainMenu.m_param.set_InputFiles(ll_files);
    MainMenu.m_param.save();

    ReadHTMLTable htmltable = new ReadHTMLTable(a_SelectedFile.getAbsolutePath());
    ArrayList<String> regels = htmltable.parseHTMLpage();

    pieData = new TaartPuntDataImpl();
    pieData.putData(regels);
  }

  public TaartPuntData getData() {
    return pieData;
  }

}
