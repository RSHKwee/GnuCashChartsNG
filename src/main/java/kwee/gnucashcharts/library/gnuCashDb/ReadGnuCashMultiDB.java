package kwee.gnucashcharts.library.gnuCashDb;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.logger.MyLogger;

public class ReadGnuCashMultiDB {
  private static final Logger lOGGER = MyLogger.getLogger();
  private ArrayList<ReadGnuCashDB> m_GnuCashDBs = new ArrayList<ReadGnuCashDB>();
  private DoubleConsumer m_progress = null;

  public ReadGnuCashMultiDB() {
    m_GnuCashDBs.clear();
  }

  public ReadGnuCashMultiDB(ReadGnuCashMultiDB a_DBs, DoubleConsumer progress) {
    m_GnuCashDBs.clear();
    m_progress = progress;
    m_GnuCashDBs = a_DBs.getGnuCashDBs();
  }

  public ReadGnuCashMultiDB(List<File> a_Files, DoubleConsumer progress) {
    m_GnuCashDBs.clear();
    int total = a_Files.size();
    int done = 0;
    for (File l_file : a_Files) {
      ReadGnuCashDB gnucashdb = new ReadGnuCashDB(l_file);
      m_GnuCashDBs.add(gnucashdb);
      done++;
      if (m_progress != null) {
        m_progress.accept((double) done / total); // 0..1
      }
    }
    ;
  }

  public ReadGnuCashMultiDB(File a_File) {
    m_GnuCashDBs.clear();
    ReadGnuCashDB gnucashdb = new ReadGnuCashDB(a_File);
    m_GnuCashDBs.add(gnucashdb);
  }

  public ReadGnuCashMultiDB(List<File> files) {
    this(files, null); // bestaande constructor blijft werken
  }

  public void addGnuCashDB(ReadGnuCashDB a_DB, DoubleConsumer progress) {
    m_progress = progress;
    m_GnuCashDBs.add(a_DB);
  }

  public void addGnuCashDB(File a_File, DoubleConsumer progress) {
    m_progress = progress;
    ReadGnuCashDB gnucashdb = new ReadGnuCashDB(a_File);
    m_GnuCashDBs.add(gnucashdb);
  }

  public ArrayList<ReadGnuCashDB> getGnuCashDBs() {
    ArrayList<ReadGnuCashDB> l_GnuCashDBs = new ArrayList<ReadGnuCashDB>();
    m_GnuCashDBs.forEach(l_db -> {
      l_GnuCashDBs.add(l_db);
    });
    return l_GnuCashDBs;
  }

  public List<File> getFiles() {
    List<File> l_Files = new ArrayList<File>();
    m_GnuCashDBs.forEach(db -> {
      l_Files.add(db.getFile());
    });
    return l_Files;
  }

  /**
   * Get content GnuCashFile in CSV format.
   *
   * @param a_Date Sadi on given date
   * @return Result in csv format
   */
  public ArrayList<String> getRegels(LocalDate a_Date) {
    ArrayList<String> l_Regels = new ArrayList<String>();
    m_GnuCashDBs.forEach(db -> {
      ArrayList<String> ll_Regels = new ArrayList<String>();
      ll_Regels = db.getRegels(a_Date);
      l_Regels.addAll(ll_Regels);
    });
    return l_Regels;
  }
}