package kwee.gnucashcharts.library.gnuCashDb;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.logger.MyLogger;

public class ReadGnuCashMultiDB {
  private static final Logger lOGGER = MyLogger.getLogger();
  private ArrayList<ReadGnuCashDB> m_GnuCashDBs = new ArrayList<ReadGnuCashDB>();

  public ReadGnuCashMultiDB() {
    m_GnuCashDBs.clear();
  }

  public ReadGnuCashMultiDB(ReadGnuCashMultiDB a_DBs) {
    m_GnuCashDBs.clear();
    m_GnuCashDBs = a_DBs.getGnuCashDBs();
  }

  public ReadGnuCashMultiDB(List<File> a_Files) {
    m_GnuCashDBs.clear();
    a_Files.forEach(l_file -> {
      ReadGnuCashDB gnucashdb = new ReadGnuCashDB(l_file);
      m_GnuCashDBs.add(gnucashdb);
    });
  }

  public ReadGnuCashMultiDB(File a_File) {
    m_GnuCashDBs.clear();
    ReadGnuCashDB gnucashdb = new ReadGnuCashDB(a_File);
    m_GnuCashDBs.add(gnucashdb);
  }

  public void addGnuCashDB(ReadGnuCashDB a_DB) {
    m_GnuCashDBs.add(a_DB);
  }

  public void addGnuCashDB(File a_File) {
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