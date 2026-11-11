package kwee.gnucashcharts.gui;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
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
  private final String[] c_levels = { "OFF", "SEVERE", "WARNING", "INFO",
      "CONFIG", "FINE", "FINER", "FINEST", "ALL" };

  static final String m_HelpFile = "GNUCharts.chm";

  private String m_Language = "en";
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  private int c_NrBars = 24;
  private int nrBars = c_NrBars;

  private Level m_Level = Level.INFO;
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

  // --- Services ---
  private Service<LoadResult> m_loadService;
  private Service<PieResult> m_pieService;
  private Service<BarResult> m_barService;

  private List<File> m_pendingFiles;

  // --- UI-velden ---
  private Label m_fileLabel;
  private Label m_statusLabel;
  private Button m_openFileButton;
  private Button m_cancelLoadButton;
  private ComboBox<String> m_comboTagBox;
  private Button m_buttonPiechart;
  private Button m_buttonBarchart;
  private TextField m_integerField;
  private ProgressBar m_progressBar;
  private PieChartWithLegend m_piwindow;

  private final BooleanProperty m_tagSelected = new SimpleBooleanProperty(
      false);

  @Override
  public void start(Stage primaryStage) {
    try {
      LoggerPrintOutStream.redirectOutStreamToLogger(lOGGER, Level.WARNING);
      LoggerPrintErrStream.redirectErrorStreamToLogger(lOGGER, Level.WARNING);
    } catch (Exception e) {
    }

    nrBars = m_param.get_NrBars();
    m_Language = m_param.get_Language();
    lOGGER.log(Level.FINE, "m_param.get_Language: " + m_Language);
    bundle = ApplicationMessages.getInstance();
    bundle.changeLanguage(m_Language);
    if ((m_Level == Level.FINE) || (m_Level == Level.FINER)
        || (m_Level == Level.FINEST)) {
      bundle.dumpBundle();
    }
    m_SetLibMessagesBundle.changeLanguage(m_Language);

    m_Logdir = m_param.get_LogDir();
    m_toDisk = m_param.is_toDisk();

    TextArea logTextArea = new TextArea();
    logTextArea.setFont(new Font("Arial", 12));
    try {
      MyLogger.setup(m_Level, m_Logdir, m_toDisk);
      JavaFXLogHandler fxLogHandler = new JavaFXLogHandler(logTextArea);
      lOGGER.addHandler(fxLogHandler);
    } catch (IOException e1) {
      lOGGER.log(Level.INFO, e1.getMessage());
    }

    MenuBar menubar = doTheMenuBar(primaryStage);

    FileChooser inpFileChooser = new FileChooser();
    m_comboTagBox = new ComboBox<>(
        FXCollections.observableArrayList("Option 1", "Option 2", "Option 3"));
    m_buttonPiechart = new Button(
        bundle.getMessage(MessageConstants.C_OpenPieChart));
    m_integerField = new TextField(Integer.toString(nrBars));
    DatePicker datePicker = new DatePicker();
    m_buttonBarchart = new Button(
        bundle.getMessage(MessageConstants.C_OpenBarChart));
    CheckBox checkDiff = new CheckBox(
        bundle.getMessage(MessageConstants.C_Difference));

    m_comboTagBox.setDisable(true);
    m_buttonPiechart.setDisable(true);
    m_integerField.setDisable(true);
    datePicker.setDisable(true);
    m_buttonBarchart.setDisable(true);

    m_fileLabel = new Label(
        bundle.getMessage(MessageConstants.C_ChooseGnuCashFile));
    m_statusLabel = new Label();
    Label l_tag = new Label(
        bundle.getMessage(MessageConstants.C_SelectSubject));

    m_openFileButton = new Button(
        bundle.getMessage(MessageConstants.C_OpenFile));
    m_cancelLoadButton = new Button();

    m_piwindow = new PieChartWithLegend();

    m_progressBar = new ProgressBar();
    m_progressBar.setMaxWidth(Double.MAX_VALUE);

    // --- Services ---
    m_loadService = createLoadService();
    m_pieService = createPieService();
    m_barService = createBarService();

    wireLoadServiceCallbacks(l_tag, datePicker);
    wirePieServiceCallbacks();
    wireBarServiceCallbacks();
    wireServiceMessages();

    // --- Bindings: knoppen ---
    m_openFileButton.disableProperty().bind(m_loadService.runningProperty()
        .or(m_pieService.runningProperty()).or(m_barService.runningProperty()));

    m_buttonPiechart.disableProperty()
        .bind(m_tagSelected.not().or(m_pieService.runningProperty())
            .or(m_barService.runningProperty())
            .or(m_loadService.runningProperty()));

    m_buttonBarchart.disableProperty()
        .bind(m_tagSelected.not().or(m_barService.runningProperty())
            .or(m_pieService.runningProperty())
            .or(m_loadService.runningProperty()));

    // Cancel-knop: actief zolang een van de drie taken draait
    m_cancelLoadButton.disableProperty()
        .bind(m_loadService.runningProperty().or(m_pieService.runningProperty())
            .or(m_barService.runningProperty()).not());

    // Dynamische knoptekst
    m_cancelLoadButton.textProperty()
        .bind(Bindings.when(m_loadService.runningProperty()).then("Cancel load")
            .otherwise(Bindings.when(m_pieService.runningProperty())
                .then("Cancel pie chart")
                .otherwise(Bindings.when(m_barService.runningProperty())
                    .then("Cancel bar chart").otherwise("Cancel"))));

    // Voortgangsbalk volgt de actieve service
    m_progressBar.progressProperty()
        .bind(Bindings.when(m_loadService.runningProperty())
            .then(m_loadService.progressProperty())
            .otherwise(Bindings.when(m_pieService.runningProperty())
                .then(m_pieService.progressProperty())
                .otherwise(Bindings.when(m_barService.runningProperty())
                    .then(m_barService.progressProperty()).otherwise(0.0))));

    // --- Handlers ---
    m_openFileButton.setOnAction(_ -> {
      m_statusLabel.setText("");
      if (m_param.get_InputFiles() != null
          && m_param.get_InputFiles().length > 0) {
        String ldir = m_param.get_InputFiles()[0].getParent();
        if (ldir != null) {
          File d = new File(ldir);
          if (d.exists() && d.isDirectory()) {
            inpFileChooser.setInitialDirectory(d);
          }
        }
      }
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Multiple Files");
      String lastDirectory = m_param.get_InitialDirectory();
      if (lastDirectory != null) {
        File dir = new File(lastDirectory);
        if (dir.exists() && dir.isDirectory()) {
          fileChooser.setInitialDirectory(dir);
        }
      }
      List<File> selectedFiles = fileChooser
          .showOpenMultipleDialog(primaryStage);
      if (selectedFiles == null || selectedFiles.isEmpty()) {
        return;
      }
      File firstFile = selectedFiles.get(0);
      File directory = firstFile.getParentFile();
      if (directory != null && directory.exists()) {
        m_param.set_InitialDirectory(directory.getAbsolutePath());
      }
      m_SelectedFiles = selectedFiles;
      m_pendingFiles = selectedFiles;

      if (m_loadService.isRunning()) {
        m_loadService.cancel();
      }
      m_loadService.restart();
    });

    m_cancelLoadButton.setOnAction(_ -> {
      m_statusLabel.setText("");
      if (m_loadService.isRunning()) {
        m_loadService.cancel();
      }
      if (m_pieService.isRunning()) {
        m_pieService.cancel();
      }
      if (m_barService.isRunning()) {
        m_barService.cancel();
      }
    });

    m_comboTagBox.setOnAction(_ -> {
      m_statusLabel.setText("");
      String selectedOption = m_comboTagBox.getValue();
      if (selectedOption != null) {
        lOGGER.log(Level.INFO, bundle
            .getMessage(MessageConstants.C_SelectedSubject, selectedOption));
        m_tag = selectedOption;
        l_tag.setText(m_tag);
        m_param.set_Tag(m_tag);
        m_param.save();

        m_integerField.setDisable(false);
        m_tagSelected.set(true);

        ArrayList<String> l_Subjects = m_pieData.getSubjects(m_tag);
        m_SubjColors = new SubjectsColors(l_Subjects);
      }
    });

    m_buttonPiechart.setOnAction(_ -> {
      if (!m_pieService.isRunning()) {
        m_pieService.restart();
      }
    });

    Label nrBarsLabel = new Label(
        bundle.getMessage(MessageConstants.C_MonthLab));
    m_integerField.focusedProperty().addListener((_, _, isNowFocused) -> {
      m_statusLabel.setText("");
      if (!isNowFocused) {
        try {
          int value = Integer.parseInt(m_integerField.getText());
          nrBars = value;
          m_param.set_NrBars(nrBars);
          m_param.save();
          lOGGER.log(Level.INFO,
              bundle.getMessage(MessageConstants.C_NrOfMonth, nrBars));
        } catch (NumberFormatException e) {
          lOGGER.log(Level.INFO,
              bundle.getMessage(MessageConstants.C_InvInpInt));
        }
      }
    });

    Label endDateLabel = new Label(
        bundle.getMessage(MessageConstants.C_DateLabel));
    datePicker.setOnAction(_ -> {
      LocalDate selectedDate = datePicker.getValue();
      m_Date = selectedDate;
      lOGGER.log(Level.INFO,
          bundle.getMessage(MessageConstants.C_SelectedDate, selectedDate));
    });

    checkDiff.setOnAction(_ -> {
      m_statusLabel.setText("");
      if (checkDiff.isSelected()) {
        m_Diff = true;
        checkDiff.setText(bundle.getMessage(MessageConstants.C_DiffMode));
      } else {
        m_Diff = false;
        checkDiff.setText(bundle.getMessage(MessageConstants.C_NoDiffMode));
      }
    });

    m_buttonBarchart.setOnAction(_ -> {
      m_statusLabel.setText("");
      if (!m_barService.isRunning()) {
        m_barService.restart();
      }
    });

    // Layout
    HBox openFileLayout = new HBox(m_openFileButton, m_fileLabel,
        m_statusLabel);
    HBox selectOptionLayout = new HBox(endDateLabel, datePicker, m_comboTagBox,
        l_tag);
    HBox buttonPiechartLayout = new HBox(m_buttonPiechart);
    HBox buttonBarchartLayout = new HBox(nrBarsLabel, m_integerField, checkDiff,
        m_buttonBarchart, m_cancelLoadButton);

    openFileLayout.setSpacing(10);
    selectOptionLayout.setSpacing(10);
    buttonPiechartLayout.setSpacing(10);
    buttonBarchartLayout.setSpacing(10);

    HBox.setMargin(m_openFileButton, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_fileLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_statusLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_comboTagBox, new Insets(10, 10, 10, 10));
    HBox.setMargin(l_tag, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_buttonPiechart, new Insets(10, 10, 10, 10));
    HBox.setMargin(checkDiff, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_buttonBarchart, new Insets(10, 10, 10, 10));
    HBox.setMargin(nrBarsLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_integerField, new Insets(10, 10, 10, 10));
    HBox.setMargin(endDateLabel, new Insets(10, 10, 10, 10));
    HBox.setMargin(datePicker, new Insets(10, 10, 10, 10));
    HBox.setMargin(m_cancelLoadButton, new Insets(10, 10, 10, 10));

    VBox layout = new VBox(openFileLayout, selectOptionLayout,
        buttonPiechartLayout, buttonBarchartLayout, m_progressBar, logTextArea);

    BorderPane root = new BorderPane();
    root.setTop(menubar);
    root.setCenter(layout);

    Scene scene = new Scene(root, 700, 425);
    primaryStage.setScene(scene);
    primaryStage.setTitle(bundle.getMessage(MessageConstants.C_Title,
        m_creationtime, c_CopyrightYear));
    primaryStage.show();

    lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_Title,
        m_creationtime, c_CopyrightYear));
  }

  // ---------------- Services ----------------

  private Service<LoadResult> createLoadService() {
    return new Service<>() {
      @Override
      protected Task<LoadResult> createTask() {
        final List<File> files = m_pendingFiles;
        return new Task<>() {
          @Override
          protected LoadResult call() throws Exception {
            // Callback: de routines roepen dit aan met een fractie 0..1
            DoubleConsumer progress = f -> updateProgress(f, 1.0);

            updateMessage("Reading GnuCash DB...");
            updateProgress(-1, 1);
            // Routine geeft voortgang door via de callback:
            ReadGnuCashMultiDB db = new ReadGnuCashMultiDB(files, progress);
            if (isCancelled()) {
              return null;
            }

            updateMessage("Building pie chart model...");
            ActionGnuCashDbPieChart pie = new ActionGnuCashDbPieChart(db);
            if (isCancelled()) {
              return null;
            }

            updateMessage("Building bar chart model...");
            BarChartWithLegend bar = new BarChartWithLegend(db, progress);
            if (isCancelled()) {
              return null;
            }

            updateMessage("Calculating initial pie data...");
            TaartPuntData data = pie.getData(m_Date, progress);
            if (isCancelled()) {
              return null;
            }

            updateProgress(1, 1);
            updateMessage("Done");
            return new LoadResult(db, pie, bar, data);
          }
        };
      }
    };
  }

  private Service<PieResult> createPieService() {
    return new Service<>() {
      @Override
      protected Task<PieResult> createTask() {
        final ActionGnuCashDbPieChart pie = m_pieSelect;
        final LocalDate date = m_Date;
        final String tag = m_tag;
        final SubjectsColors colors = m_SubjColors;

        return new Task<>() {
          @Override
          protected PieResult call() throws Exception {
            DoubleConsumer progress = f -> updateProgress(f, 1.0);

            updateMessage("Calculating pie data...");
            updateProgress(-1, 1);
            TaartPuntData data = pie.getData(date, progress);
            if (isCancelled()) {
              return null;
            }

            updateMessage("Writing CSV...");
            progress.accept(0.95);
            kwee.library.TxtBestand.DumpBestand("PieDataNG.csv", data.print());
            if (isCancelled()) {
              return null;
            }

            updateProgress(1, 1);
            updateMessage("Done");
            return new PieResult(data, tag, colors, date);
          }
        };
      }
    };
  }

  private Service<BarResult> createBarService() {
    return new Service<>() {
      @Override
      protected Task<BarResult> createTask() {
        final BarChartWithLegend bar = m_barwindow;
        final String tag = m_tag;
        final int bars = nrBars;
        final LocalDate date = m_Date;
        final boolean diff = m_Diff;

        return new Task<>() {
          @Override
          protected BarResult call() throws Exception {
            DoubleConsumer progress = f -> updateProgress(f, 1.0);

            updateMessage("Building bar chart...");
            updateProgress(-1, 1);
            bar.prepareTabs(tag, bars, date, diff, progress);
            if (isCancelled()) {
              return null;
            }

            updateProgress(1, 1);
            updateMessage("Done");
            return new BarResult(bar, tag, bars, date, diff);
          }
        };
      }
    };
  }

  // ---------------- Callbacks ----------------

  private void wireLoadServiceCallbacks(Label l_tag, DatePicker datePicker) {
    m_loadService.setOnSucceeded(e -> {
      LoadResult r = m_loadService.getValue();
      m_gnucashdbtables = r.db;
      m_pieSelect = r.pie;
      m_barwindow = r.bar;
      m_pieData = r.data;

      m_FileTagTxt = "";
      if (m_pendingFiles != null) {
        m_pendingFiles
            .forEach(f -> m_FileTagTxt = m_FileTagTxt + " " + f.getName());
        File[] l_fileArr = m_pendingFiles.toArray(new File[0]);
        m_param.set_InputFiles(l_fileArr);
        m_param.save();
      }
      m_fileLabel.setText(m_FileTagTxt);

      Set<String> tags = m_pieData.getTags();
      ObservableList<String> observableList = FXCollections
          .observableArrayList(tags);
      m_comboTagBox.setItems(observableList);

      m_comboTagBox.setDisable(false);
      datePicker.setDisable(false);
      l_tag.setText(bundle.getMessage(MessageConstants.C_SelectedSubject, ""));

      m_tagSelected.set(false);
    });

    m_loadService.setOnFailed(e -> {
      Throwable ex = m_loadService.getException();
      lOGGER.log(Level.SEVERE, "Laden mislukt: " + ex, ex);
      m_statusLabel
          .setText("Fout: " + (ex != null ? ex.getMessage() : "onbekend"));
    });

    m_loadService.setOnCancelled(e -> {
      lOGGER.log(Level.INFO, "Laden geannuleerd.");
      m_statusLabel
          .setText(bundle.getMessage(MessageConstants.C_ChooseGnuCashFile));
      m_tagSelected.set(false);
    });
  }

  private void wirePieServiceCallbacks() {
    m_pieService.setOnSucceeded(e -> {
      PieResult r = m_pieService.getValue();
      if (r == null) {
        return;
      }
      m_pieData = r.data;
      m_statusLabel.setText("Pie chart klaar.");
      m_piwindow.openPieChartWindow(r.data, r.tag, r.colors, r.date);
    });
    m_pieService.setOnFailed(e -> {
      Throwable ex = m_pieService.getException();
      lOGGER.log(Level.SEVERE, "Pie-chart mislukt: " + ex, ex);
      m_statusLabel.setText("Pie-chart fout: " + ex.getMessage());
    });
    m_pieService.setOnCancelled(e -> {
      lOGGER.log(Level.INFO, "Pie-chart geannuleerd.");
      m_statusLabel.setText("Pie-chart geannuleerd.");
    });
  }

  private void wireBarServiceCallbacks() {
    m_barService.setOnSucceeded(e -> {
      BarResult r = m_barService.getValue();
      if (r == null) {
        return;
      }
      m_statusLabel.setText("Bar chart klaar.");
      m_barwindow.openTabsWindow(r.tag, r.bars, r.date, r.diff);
    });
    m_barService.setOnFailed(e -> {
      Throwable ex = m_barService.getException();
      lOGGER.log(Level.SEVERE, "Bar-chart mislukt: " + ex, ex);
      m_statusLabel.setText("Bar-chart fout: " + ex.getMessage());
    });
    m_barService.setOnCancelled(e -> {
      lOGGER.log(Level.INFO, "Bar-chart geannuleerd.");
      m_statusLabel.setText("Bar-chart geannuleerd.");
    });
  }

  /**
   * Koppelt de messageProperty van alle drie de services aan het statuslabel.
   */
  private void wireServiceMessages() {
    m_loadService.messageProperty().addListener((obs, oud, nieuw) -> {
      if (nieuw != null && m_loadService.isRunning()) {
        m_statusLabel.setText("[load] " + nieuw);
      }
    });
    m_pieService.messageProperty().addListener((obs, oud, nieuw) -> {
      if (nieuw != null && m_pieService.isRunning()) {
        m_statusLabel.setText("[pie] " + nieuw);
      }
    });
    m_barService.messageProperty().addListener((obs, oud, nieuw) -> {
      if (nieuw != null && m_barService.isRunning()) {
        m_statusLabel.setText("[bar] " + nieuw);
      }
    });
  }

  // ---------------- Houders ----------------

  private static final class LoadResult {
    final ReadGnuCashMultiDB db;
    final ActionGnuCashDbPieChart pie;
    final BarChartWithLegend bar;
    final TaartPuntData data;

    LoadResult(ReadGnuCashMultiDB db, ActionGnuCashDbPieChart pie,
        BarChartWithLegend bar, TaartPuntData data) {
      this.db = db;
      this.pie = pie;
      this.bar = bar;
      this.data = data;
    }
  }

  private static final class PieResult {
    final TaartPuntData data;
    final String tag;
    final SubjectsColors colors;
    final LocalDate date;

    PieResult(TaartPuntData data, String tag, SubjectsColors colors,
        LocalDate date) {
      this.data = data;
      this.tag = tag;
      this.colors = colors;
      this.date = date;
    }
  }

  private static final class BarResult {
    final BarChartWithLegend bar;
    final String tag;
    final int bars;
    final LocalDate date;
    final boolean diff;

    BarResult(BarChartWithLegend bar, String tag, int bars, LocalDate date,
        boolean diff) {
      this.bar = bar;
      this.tag = tag;
      this.bars = bars;
      this.date = date;
      this.diff = diff;
    }
  }

  // ---------------- Menu / helpers ----------------

  private int il = 0;

  private MenuBar doTheMenuBar(Stage primaryStage) {
    MenuBar menuBar = new MenuBar();
    Menu mnSettings = new Menu(bundle.getMessage(MessageConstants.C_Settings));
    mnSettings.setDisable(false);

    Menu mntmLoglevel = new Menu(
        bundle.getMessage(MessageConstants.C_Loglevel));
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

    CheckMenuItem checkMenuItem = new CheckMenuItem("Logfiles");
    checkMenuItem.setOnAction(_ -> {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("Output Directory");
      File selectedDirectory = directoryChooser.showDialog(primaryStage);
      if (selectedDirectory != null) {
        lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_LogFolder,
            selectedDirectory.getAbsolutePath()));
        m_Logdir = selectedDirectory.getAbsolutePath() + "/";
        m_param.set_LogDir(m_Logdir);
        m_param.set_toDisk(true);
        m_toDisk = checkMenuItem.isSelected();
        try {
          MyLogger.setup(m_Level, m_Logdir, m_toDisk);
        } catch (IOException es) {
          lOGGER.log(Level.SEVERE,
              Class.class.getName() + ": " + es.toString());
        }
      } else {
        lOGGER.log(Level.INFO,
            bundle.getMessage(MessageConstants.C_NoDirectorySelected));
      }
    });
    mnSettings.getItems().add(checkMenuItem);

    MenuItem menuPreferences = new MenuItem("Preferences");
    menuPreferences.setOnAction(_ -> {
      UserSetting showpref = new UserSetting();
      showpref.showAllPreferences(true);
    });
    mnSettings.getItems().add(menuPreferences);

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
        lOGGER.log(Level.INFO,
            bundle.getMessage("HelpFileNotFound", helpFile.getAbsolutePath()));
      }
    });
    questMenu.getItems().add(menuHelp);

    MenuItem menuAbout = new MenuItem(
        bundle.getMessage(MessageConstants.C_About));
    menuAbout.setOnAction(_ -> {
      @SuppressWarnings("unused")
      AboutWindow about = new AboutWindow(c_reponame, m_creationtime,
          c_CopyrightYear);
    });
    questMenu.getItems().add(menuAbout);

    menuBar.getMenus().addAll(mnSettings, questMenu);
    return menuBar;
  }

  private void restartGUI(Stage primaryStage) {
    primaryStage.close();
    start(new Stage());
  }

  public static void main(String[] args) {
    if (m_creationtime == null) {
      m_creationtime = JarInfo.getProjectVersion(Main.class);
    }
    launch(args);
  }
}