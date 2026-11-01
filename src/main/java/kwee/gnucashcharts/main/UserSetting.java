package kwee.gnucashcharts.main;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import kwee.library.FX.ShowPreferences;
import kwee.logger.MyLogger;

/**
 * User setting persistence.
 * 
 * @author rshkw
 *
 */
public class UserSetting extends ShowPreferences {
  private static final Logger LOGGER = MyLogger.getLogger();
  public static String NodePrefName = "kwee.gnucashcharts";

  private String c_Level = "Level";
  private String c_LevelValue = "INFO";

  private String c_ConfirmOnExit = "ConfirmOnExit";
  private String c_toDisk = "ToDisk";
  private String c_LogDir = "LogDir";
  private String c_LookAndFeel = "LookAndFeel";
  private String c_LookAndFeelVal = "Nimbus";
  private String c_InputFiles = "InputFiles";
  private String c_Pdf_File = "PdfFile";
  private String c_Tag = "Tag";
  private String c_NrBars = "NrBars";
  private String c_Language = "Language";
  private String c_InitialDirectory = "InitialDirectory";

  private String m_Level = c_LevelValue;
  private String m_LookAndFeel;

  private String m_OutputFolder = "";
  private String m_LogDir = "";

  private boolean m_ConfirmOnExit = false;
  private boolean m_toDisk = false;

  private String m_Tag = "";
  private int m_NrBars = 12;
  private File[] m_InputFiles = null;
  private String m_PdfFile = "";
  private String m_Language = "nl";
  private String m_InitialDirectory = "";

  private Preferences pref;
  private Preferences userPrefs = Preferences.userRoot();

  /**
   * Constructor Initialize settings
   */
  public UserSetting() {
    super(NodePrefName);
    // Navigate to the preference node that stores the user setting
    pref = userPrefs.node(NodePrefName);

    m_toDisk = pref.getBoolean(c_toDisk, false);
    m_ConfirmOnExit = pref.getBoolean(c_ConfirmOnExit, false);
    m_LookAndFeel = pref.get(c_LookAndFeel, c_LookAndFeelVal);

    String l_InputFiles = pref.get(c_InputFiles, "");
    m_InputFiles = StringToFiles(l_InputFiles);

    m_Tag = pref.get(c_Tag, "");
    m_PdfFile = pref.get(c_Pdf_File, "");
    m_NrBars = pref.getInt(c_NrBars, 12);
    m_Language = pref.get(c_Language, "nl");
    m_InitialDirectory = pref.get(c_InitialDirectory, "");

    m_Level = pref.get(c_Level, c_LevelValue);
    m_LogDir = pref.get(c_LogDir, "");
  }

  public int get_NrBars() {
    return m_NrBars;
  }

  public void set_NrBars(int m_NrBars) {
    this.m_NrBars = m_NrBars;
  }

  public String get_LogDir() {
    return this.m_LogDir;
  }

  public String get_OutputFolder() {
    return m_OutputFolder;
  }

  public String get_InitialDirectory() {
    return m_InitialDirectory;
  }

  public String get_Language() {
    return m_Language;
  }

  public Level get_Level() {
    return Level.parse(this.m_Level);
  }

  public String get_LookAndFeel() {
    return this.m_LookAndFeel;
  }

  public String get_PdfFile() {
    return this.m_PdfFile;
  }

  public String get_Tag() {
    return this.m_Tag;
  }

  public File[] get_InputFiles() {
    return this.m_InputFiles;
  }

  public boolean is_toDisk() {
    return this.m_toDisk;
  }

  public Preferences getPreferences() {
    return this.pref;
  }

  public boolean is_ConfirmOnExit() {
    return this.m_ConfirmOnExit;
  }

  public void set_Pdf_file(File a_Pdf_file) {
    pref.put(c_Pdf_File, a_Pdf_file.getAbsolutePath());
    this.m_PdfFile = a_Pdf_file.getAbsolutePath();
  }

  public void set_Tag(String a_Tag) {
    pref.put(c_Tag, a_Tag);
    this.m_Tag = a_Tag;
  }

  public void set_toDisk(boolean a_toDisk) {
    pref.putBoolean(c_toDisk, a_toDisk);
    this.m_toDisk = a_toDisk;
  }

  public void set_Language(String m_Language) {
    this.m_Language = m_Language;
  }

  public void set_LogDir(String m_LogDir) {
    this.m_LogDir = m_LogDir;
  }

  public void set_InitialDirectory(String m_InitialDirectory) {
    this.m_InitialDirectory = m_InitialDirectory;
  }

  public void set_Level(Level a_Level) {
    pref.put(c_Level, a_Level.toString());
    this.m_Level = a_Level.toString();
  }

  public void set_LookAndFeel(String a_LookAndFeel) {
    pref.put(c_LookAndFeel, a_LookAndFeel);
    this.m_LookAndFeel = a_LookAndFeel;
  }

  public void set_ConfirmOnExit(boolean a_ConfirmOnExit) {
    pref.putBoolean(c_ConfirmOnExit, a_ConfirmOnExit);
    this.m_ConfirmOnExit = a_ConfirmOnExit;
  }

  public void set_InputFiles(File[] a_InputFiles) {
    pref.put(c_InputFiles, FilesToString(a_InputFiles));
    this.m_InputFiles = a_InputFiles;
  }

  /**
   * Save all settings
   */
  public void save() {
    try {
      pref.putBoolean(c_toDisk, m_toDisk);
      pref.putBoolean(c_ConfirmOnExit, m_ConfirmOnExit);

      pref.put(c_Level, m_Level);
      pref.put(c_LogDir, m_LogDir);

      pref.put(c_LookAndFeel, m_LookAndFeel);
      pref.put(c_Pdf_File, m_PdfFile);
      pref.put(c_Tag, m_Tag);
      pref.putInt(c_NrBars, m_NrBars);
      pref.put(c_InputFiles, FilesToString(m_InputFiles));

      pref.put(c_InitialDirectory, m_InitialDirectory);

      pref.flush();
    } catch (BackingStoreException e) {
      LOGGER.log(Level.INFO, e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  public boolean validateValue(String a_Param, String a_Value) {
    boolean bstat = false;
    if ((a_Param.equals(c_toDisk) || a_Param.equals(c_ConfirmOnExit))) {
      if ((a_Value.equals("true")) || (a_Value.equals("false"))) {
        bstat = true;
      }
    } else if (a_Param.equals(c_Level)) {
      try {
        Level a_Level = Level.parse(a_Value);
        bstat = true;
      } catch (Exception e) {
        bstat = false;
      }
    } else if (a_Param.equals(c_NrBars)) {
      try {
        int bar = Integer.parseInt(a_Value);
        bstat = true;
      } catch (Exception e) {
        bstat = false;
      }
    } else {
      bstat = true;
    }
    return bstat;
  }

  public String print() {
    String l_line = "User setting \n";
    l_line = l_line + "Name: " + pref.name() + "\n";
    l_line = l_line + c_toDisk + ": " + m_toDisk + "\n";
    l_line = l_line + c_ConfirmOnExit + ": " + m_ConfirmOnExit + "\n";

    l_line = l_line + c_LookAndFeel + ": " + m_LookAndFeel + "\n";
    l_line = l_line + c_Pdf_File + ": " + m_PdfFile + "\n";
    l_line = l_line + c_InputFiles + ": " + FilesToString(m_InputFiles) + "\n";
    l_line = l_line + c_Tag + ": " + m_Tag + "\n";
    l_line = l_line + c_NrBars + ": " + m_NrBars + "\n";
    l_line = l_line + c_InitialDirectory + ": " + m_InitialDirectory + "\n";

    l_line = l_line + c_Level + ": " + m_Level + "\n";
    l_line = l_line + c_LogDir + ": " + m_LogDir + "\n";

    return l_line;
  }

  // Local functions to convert File to String and vice versa
  private String c_StringDelim = ";";

  /**
   * Convert list of Files to String for storage.
   * 
   * @param a_Files List of Files
   * @return String
   */
  private String FilesToString(File[] a_Files) {
    String l_files = "";
    if (a_Files != null) {
      for (int i = 0; i < a_Files.length; i++) {
        l_files = l_files + a_Files[i].getAbsolutePath() + c_StringDelim;
      }
    }
    return l_files;
  }

  /**
   * Convert String to Files.
   * 
   * @param a_Files String with list of Files.
   * @return List of Files
   */
  private File[] StringToFiles(String a_Files) {
    String[] ls_files = a_Files.split(c_StringDelim);
    File[] l_files = new File[ls_files.length];

    for (int i = 0; i < ls_files.length; i++) {
      File ll_file = new File(ls_files[i]);
      l_files[i] = ll_file;
    }
    return l_files;
  }
}
