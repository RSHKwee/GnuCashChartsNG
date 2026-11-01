package kwee.gnucashcharts.gui;

/**
 * 
 */
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import kwee.logger.LoggerPrintErrStream;
import kwee.logger.LoggerPrintOutStream;
import kwee.logger.MyLogger;

import kwee.gnucashcharts.main.Main;
import kwee.gnucashcharts.main.UserSetting;
import kwee.library.ApplicationMessages;
import kwee.library.ChmOpener;
import kwee.library.JarInfo;
import kwee.library.FX.AboutWindow;
import kwee.library.FX.JavaFXLogHandler;
import kwee.gnucashcharts.library.MessageConstants;
import kwee.gnucashcharts.library.SetLibMessagesBundle;
import kwee.gnucashcharts.library.SubjectsColors;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.gnucashcharts.library.gnuCashDb.ReadGnuCashMultiDB;

public class MainMenu extends Application {
  private static final Logger lOGGER = MyLogger.getLogger();
  static String m_creationtime = Main.m_creationtime;
  static final String c_CopyrightYear = Main.c_CopyrightYear;
  private static String c_reponame = "GNUCashCharts";

  public static UserSetting m_param = new UserSetting();
  private final String[] c_levels = { "OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL" };

  // Replace "path/to/help/file" with the actual path to your help file
  static final String m_HelpFile = "GNUCharts.chm";

  private String m_Language = "en";
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  private int c_NrBars = 24; // Months
  private int nrBars = c_NrBars;

  private Level m_Level = Level.INFO;
//  private Level m_Level = Level.FINE;
  private String m_Logdir = "c:\\";
  private boolean m_toDisk = false;

  private String m_tag = "";
  private TaartPuntData m_pieData;
  private SubjectsColors m_SubjColors;
  private LocalDate m_Date = LocalDate.now();
  private boolean m_Diff = false;

  private List<File> m_SelectedFiles;
  private String m_FileTagTxt = "";

  private ActionGnuCashDbPieChart m_pieSelect;
  private BarChartWithLegend m_barwindow = null;
  private ReadGnuCashMultiDB m_gnucashdbtables;
  private SetLibMessagesBundle m_SetLibMessagesBundle = new SetLibMessagesBundle();

  @Override
  public void start(Stage primaryStage) {
    // Redirect Out- and Error stream to Logger
    try {
      LoggerPrintOutStream.redirectOutStreamToLogger(lOGGER, Level.WARNING);
      LoggerPrintErrStream.redirectErrorStreamToLogger(lOGGER, Level.WARNING);
    } catch (Exception e) {

    }
    // Defaults
    nrBars = m_param.get_NrBars();

    m_Language = m_param.get_Language();
    lOGGER.log(Level.FINE, "m_param.get_Language: " + m_Language);
    bundle = ApplicationMessages.getInstance();
    bundle.changeLanguage(m_Language);
    if ((m_Level == Level.FINE) || (m_Level == Level.FINER) || (m_Level == Level.FINEST)) {
      bundle.dumpBundle();
    }
    m_SetLibMessagesBundle.changeLanguage(m_Language);

    m_Logdir = m_param.get_LogDir();
    m_toDisk = m_param.is_toDisk();

    // Logger setup
    TextArea logTextArea = new TextArea();
    logTextArea.setFont(new Font("Arial", 12));
    try {
      MyLogger.setup(m_Level, m_Logdir, m_toDisk);

      JavaFXLogHandler fxLogHandler = new JavaFXLogHandler(logTextArea);
      lOGGER.addHandler(fxLogHandler);
    } catch (IOException e1) {
      lOGGER.log(Level.INFO, e1.getMessage());
    }

    // Menubar
    MenuBar menubar = doTheMenuBar(primaryStage);

    // Main Window
    FileChooser inpFileChooser = new FileChooser();
    ComboBox<String> comboTagBox = new ComboBox<>(
        FXCollections.observableArrayList("Option 1", "Option 2", "Option 3"));
    Button buttonPiechart = new Button(bundle.getMessage(MessageConstants.C_OpenPieChart));
    TextField integerField = new TextField(Integer.toString(nrBars));
    DatePicker datePicker = new DatePicker();
    Button buttonBarchart = new Button(bundle.getMessage(MessageConstants.C_OpenBarChart));
    CheckBox checkDiff = new CheckBox(bundle.getMessage(MessageConstants.C_Difference));

    comboTagBox.setDisable(true);
    buttonPiechart.setDisable(true);
    integerField.setDisable(true);
    datePicker.setDisable(true);
    buttonBarchart.setDisable(true);

    Label l_file = new Label(bundle.getMessage(MessageConstants.C_ChooseGnuCashFile));
    Label l_tag = new Label(bundle.getMessage(MessageConstants.C_SelectSubject));

    Button openFileButton = new Button(bundle.getMessage(MessageConstants.C_OpenFile));
    openFileButton.setOnAction(_ -> {
      File[] l_files = m_param.get_InputFiles();
      if (l_files != null) {
        if (m_param.get_InputFiles().length == 0) {
          File[] intFiles = m_param.get_InputFiles();
          String ldir = intFiles[0].getParent();
          inpFileChooser.setInitialDirectory(new File(ldir));
        }
      }
      // File selectedFile = inpFileChooser.showOpenDialog(primaryStage);
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Multiple Files");
      // Set initial directory from preferences
      String lastDirectory = m_param.get_InitialDirectory();
      if (lastDirectory != null) {
        File dir = new File(lastDirectory);
        if (dir.exists() && dir.isDirectory()) {
          fileChooser.setInitialDirectory(dir);
        }
      }
      List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
      if (selectedFiles != null && !selectedFiles.isEmpty()) {
        // Save the directory of the first selected file
        File firstFile = selectedFiles.get(0);
        File directory = firstFile.getParentFile();
        if (directory != null && directory.exists()) {
          m_param.set_InitialDirectory(directory.getAbsolutePath());
        }
        m_SelectedFiles = selectedFiles;

        l_file.setText("Processing.....");

        m_gnucashdbtables = new ReadGnuCashMultiDB(m_SelectedFiles);
        m_pieSelect = new ActionGnuCashDbPieChart(m_gnucashdbtables);
        m_barwindow = new BarChartWithLegend(m_gnucashdbtables);
        m_pieData = m_pieSelect.getData(m_Date);

        m_FileTagTxt = "";
        selectedFiles.forEach(ll_file -> {
          m_FileTagTxt = m_FileTagTxt + " " + ll_file.getName();
        });
        l_file.setText(m_FileTagTxt);

        Set<String> tags = m_pieData.getTags();
        ObservableList<String> observableList;
        observableList = FXCollections.observableArrayList(tags);
        comboTagBox.setItems(observableList);

        comboTagBox.setDisable(false);
        datePicker.setDisable(false);
        l_tag.setText(bundle.getMessage(MessageConstants.C_SelectedSubject, ""));

        File[] l_fileArr = m_SelectedFiles.toArray(new File[0]);
        m_param.set_InputFiles(l_fileArr);
        m_param.save();
      }
    });

    comboTagBox.setOnAction(_ -> {
      String selectedOption = comboTagBox.getValue();
      if (selectedOption != null) {
        lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_SelectedSubject, selectedOption));
        m_tag = selectedOption;
        l_tag.setText(m_tag);
        m_param.set_Tag(m_tag);
        m_param.save();

        buttonPiechart.setDisable(false);
        integerField.setDisable(false);
        buttonBarchart.setDisable(false);

        ArrayList<String> l_Subjects = m_pieData.getSubjects(m_tag);
        m_SubjColors = new SubjectsColors(l_Subjects);
      }
    });

    PieChartWithLegend piwindow = new PieChartWithLegend();
    buttonPiechart.setOnAction(_ -> {
      m_pieData = m_pieSelect.getData(m_Date);
      piwindow.openPieChartWindow(m_pieData, m_tag, m_SubjColors, m_Date);
    });

    Label nrBarsLabel = new Label(bundle.getMessage(MessageConstants.C_MonthLab));
    integerField.focusedProperty().addListener((_, _, isNowFocused) -> {
      if (!isNowFocused) { // Wanneer de focus verloren gaat
        try {
          int value = Integer.parseInt(integerField.getText());
          nrBars = value;
          m_param.set_NrBars(nrBars);
          m_param.save();
          lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_NrOfMonth, nrBars));
        } catch (NumberFormatException e) {
          lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_InvInpInt));
        }
      }
    });

    Label endDateLabel = new Label(bundle.getMessage(MessageConstants.C_DateLabel));
    datePicker.setOnAction(_ -> {
      LocalDate selectedDate = datePicker.getValue();
      m_Date = selectedDate;
      lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_SelectedDate, selectedDate));
    });

    checkDiff.setOnAction(_ -> {
      if (checkDiff.isSelected()) {
        m_Diff = true;
        checkDiff.setText(bundle.getMessage(MessageConstants.C_DiffMode));
      } else {
        m_Diff = false;
        checkDiff.setText(bundle.getMessage(MessageConstants.C_NoDiffMode));
      }
    });

    buttonBarchart.setOnAction(_ -> {
      m_barwindow.openTabsWindow(m_tag, nrBars, m_Date, m_Diff); // Knop toevoeven
    });

    // Do the layout
    HBox openFileLayout = new HBox(openFileButton, l_file);
    HBox selectOptionLayout = new HBox(endDateLabel, datePicker, comboTagBox, l_tag);
    HBox buttonPiechartLayout = new HBox(buttonPiechart);
    HBox buttonBarchartLayout = new HBox(nrBarsLabel, integerField, checkDiff, buttonBarchart);

    openFileLayout.setSpacing(10);
    selectOptionLayout.setSpacing(10);
    buttonPiechartLayout.setSpacing(10);
    buttonBarchartLayout.setSpacing(10);

    HBox.setMargin(openFileButton, new Insets(10, 10, 10, 10));
    HBox.setMargin(l_file, new Insets(10, 10, 10, 10));

    HBox.setMargin(comboTagBox, new Insets(10, 10, 10, 10));
    HBox.setMargin(l_tag, new Insets(10, 10, 10, 10));

    HBox.setMargin(buttonPiechart, new Insets(10, 10, 10, 10));
    HBox.setMargin(checkDiff, new Insets(10, 10, 10, 10));

    HBox.setMargin(buttonBarchart, new Insets(10, 10, 10, 10));
    HBox.setMargin(nrBarsLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(integerField, new Insets(10, 10, 10, 10));
    HBox.setMargin(endDateLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(datePicker, new Insets(10, 10, 10, 10));

    VBox layout = new VBox(openFileLayout, selectOptionLayout, buttonPiechartLayout, buttonBarchartLayout, logTextArea);

    BorderPane root = new BorderPane();
    root.setTop(menubar);
    root.setCenter(layout);

    Scene scene = new Scene(root, 700, 375);
    primaryStage.setScene(scene);
    primaryStage.setTitle(bundle.getMessage(MessageConstants.C_Title, m_creationtime, c_CopyrightYear));
    primaryStage.show();

    lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_Title, m_creationtime, c_CopyrightYear));
  }

  // Menubar
  private int il = 0;

  private MenuBar doTheMenuBar(Stage primaryStage) {
    MenuBar menuBar = new MenuBar();

    // Settings Menu
    Menu mnSettings = new Menu(bundle.getMessage(MessageConstants.C_Settings));
    mnSettings.setDisable(false);

    // Loglevel:
    Menu mntmLoglevel = new Menu(bundle.getMessage(MessageConstants.C_Loglevel));
    ToggleGroup toggleGroup = new ToggleGroup();
    RadioMenuItem[] loglevelitems = new RadioMenuItem[c_levels.length];

    ArrayList<String> ca_Levels = new ArrayList<String>();
    for (il = 0; il < c_levels.length; il++) {
      ca_Levels.add(c_levels[il]);
    }

    il = 0;
    ca_Levels.forEach(level -> {
      loglevelitems[il] = new RadioMenuItem(c_levels[il]);
      toggleGroup.getToggles().add(loglevelitems[il]);
      mntmLoglevel.getItems().add(loglevelitems[il]);
      loglevelitems[il].setOnAction(_ -> {
        m_Level = Level.parse(level.toUpperCase());
        m_param.set_Level(m_Level);
        MyLogger.changeLogLevel(m_Level);
        lOGGER.log(Level.INFO, level + " Selected");
      });
      if (c_levels[il].toLowerCase().equals(m_Level.toString().toLowerCase())) {
        toggleGroup.selectToggle(loglevelitems[il]);
      }
      il++;
    });
    mnSettings.getItems().add(mntmLoglevel);

    // Language
    ToggleGroup toggleLanguagesGroup = new ToggleGroup();
    Set<String> langs = bundle.getTranslations();
    Menu mntmLanguages = new Menu("Languages");
    il = 0;
    RadioMenuItem[] mnLanguages = new RadioMenuItem[langs.size()];
    langs.forEach(language -> {
      mnLanguages[il] = new RadioMenuItem(language);
      toggleLanguagesGroup.getToggles().add(mnLanguages[il]);
      mntmLanguages.getItems().add(mnLanguages[il]);
      mnLanguages[il].setOnAction(_ -> {
        lOGGER.log(Level.INFO, language + " Selected");
        m_Language = language;
        m_param.set_Language(m_Language);
        m_param.save();
        bundle.changeLanguage(language);
        m_SetLibMessagesBundle.changeLanguage(m_Language);
        restartGUI(primaryStage);
      });
      if (language.toLowerCase().equals(m_Language.toLowerCase())) {
        toggleLanguagesGroup.selectToggle(mnLanguages[il]);
      }
      il++;
    });
    mnSettings.getItems().add(mntmLanguages);

    // Logfiles
    CheckMenuItem checkMenuItem = new CheckMenuItem("Logfiles");
    checkMenuItem.setOnAction(_ -> {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Output Directory");
      File selectedDirectory = directoryChooser.showDialog(primaryStage);

      if (selectedDirectory != null) {
        lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_LogFolder, selectedDirectory.getAbsolutePath()));
        m_Logdir = selectedDirectory.getAbsolutePath() + "/";
        m_param.set_LogDir(m_Logdir);
        m_param.set_toDisk(true);
        m_toDisk = checkMenuItem.isSelected();
        try {
          MyLogger.setup(m_Level, m_Logdir, m_toDisk);
        } catch (IOException es) {
          lOGGER.log(Level.SEVERE, Class.class.getName() + ": " + es.toString());
        }
      } else {
        lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_NoDirectorySelected));
      }
    });
    mnSettings.getItems().add(checkMenuItem);

    // Stored preferences
    MenuItem menuPreferences = new MenuItem("Preferences");
    menuPreferences.setOnAction(_ -> {
      UserSetting showpref = new UserSetting();
      showpref.showAllPreferences(true);
    });
    mnSettings.getItems().add(menuPreferences);

    // Menu "?" and Help
    Menu questMenu = new Menu("?");
    MenuItem menuHelp = new MenuItem("Help");
    menuHelp.setOnAction(_ -> {
      File helpFile = new File("help\\" + m_Language + "\\" + m_HelpFile);
      if (helpFile.exists()) {
        try {
          ChmOpener.openChm(helpFile);
        } catch (Exception e1) {
          lOGGER.log(Level.INFO, e1.getMessage());
        }
      } else {
        lOGGER.log(Level.INFO, bundle.getMessage("HelpFileNotFound", helpFile.getAbsolutePath()));
      }
    });
    questMenu.getItems().add(menuHelp);

    // About
    MenuItem menuAbout = new MenuItem(bundle.getMessage(MessageConstants.C_About));
    menuAbout.setOnAction(_ -> {
      @SuppressWarnings("unused")
      AboutWindow about = new AboutWindow(c_reponame, m_creationtime, c_CopyrightYear);
    });
    questMenu.getItems().add(menuAbout);

    // MenuBar build up
    menuBar.getMenus().addAll(mnSettings, questMenu);
    return menuBar;
  }

  /**
   * Restart GUI for Language change purpose...
   * 
   * @param primaryStage Stage
   */
  private void restartGUI(Stage primaryStage) {
    primaryStage.close();
    start(new Stage());
  }

  /**
   * Main entry point.
   * 
   * @param args Arguments
   */
  public static void main(String[] args) {
    if (m_creationtime == null) {
      m_creationtime = JarInfo.getProjectVersion(Main.class);
    }
    launch(args);
  }
}
