package kwee.gnucashcharts.main;

import kwee.gnucashcharts.gui.MainMenu;
import kwee.library.JarInfo;

public class Main {
  static public String m_creationtime;
  static public String c_CopyrightYear;

  public static void main(String[] args) {
    m_creationtime = JarInfo.getProjectVersion(Main.class);
    c_CopyrightYear = JarInfo.getYear(Main.class);
    MainMenu.main(args);
  }
}
