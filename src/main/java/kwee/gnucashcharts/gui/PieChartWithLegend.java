package kwee.gnucashcharts.gui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.Date;
import java.util.logging.Level;

import javafx.geometry.Insets;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import kwee.gnucashcharts.library.FormatAmount;
import kwee.gnucashcharts.library.MessageConstants;
import kwee.gnucashcharts.library.TaartPuntData;
import kwee.gnucashcharts.library.SubjectsColors;
import kwee.gnucashcharts.library.CreatePdf;
import kwee.gnucashcharts.library.piechart.PieChartScene;
import kwee.library.ApplicationMessages;
import kwee.logger.MyLogger;

public class PieChartWithLegend {
  private static final Logger lOGGER = MyLogger.getLogger();

  private double tot_amt = 0.0;
  private String title = "";
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  public void openPieChartWindow(TaartPuntData pieData, String tag, SubjectsColors a_AccColor, LocalDate a_Date) {
    lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_SelectedSubject, tag));

    Stage piechartStage = new Stage();
    PieChartScene pie = new PieChartScene(pieData, tag, a_AccColor);

    // Set the title of the window
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Format the LocalDate
    String formattedDate = a_Date.format(formatter);

    tot_amt = pie.getTotalAmount();
    title = bundle.getMessage(MessageConstants.C_PiechartTitle, tag, formattedDate, FormatAmount.formatAmount(tot_amt));
    piechartStage.setTitle(title);

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(bundle.getMessage(MessageConstants.C_SaveFile));

    Button saveButton = new Button(bundle.getMessage(MessageConstants.C_PDFCreate));
    saveButton.setOnAction(_ -> {
      FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
          bundle.getMessage(MessageConstants.C_PDFFiles), "*.pdf");
      fileChooser.getExtensionFilters().add(extFilter);
      fileChooser.setInitialFileName("PieChart_" + tag + CurrentDateStr() + ".pdf");
      if (!MainMenu.m_param.get_PdfFile().isBlank()) {
        File intFile = new File(MainMenu.m_param.get_PdfFile());
        String ldir = intFile.getParent();
        fileChooser.setInitialDirectory(new File(ldir));
      }
      // Show save file dialog
      File selectedFile = fileChooser.showSaveDialog(piechartStage);
      if (selectedFile != null) {
        try {
          CreatePdf l_Pdf = new CreatePdf(selectedFile.getAbsolutePath());
          l_Pdf.CreatePage(CreatePdf.c_PageSizeEnum.A4, title);
          l_Pdf.addImageAndLegend(pie.getPieChartImage(), pie.getLegendImage());
          l_Pdf.SaveDocument();
          lOGGER.log(Level.INFO, bundle.getMessage(MessageConstants.C_PDFGenerated, selectedFile.getAbsolutePath()));

          MainMenu.m_param.set_Pdf_file(selectedFile);
          MainMenu.m_param.save();
        } catch (IOException e1) {
          lOGGER.log(Level.INFO, e1.getMessage());
        }
      }
    });
    VBox saveFileLayout = new VBox(saveButton);
    saveFileLayout.setSpacing(10);
    VBox.setMargin(saveButton, new Insets(10, 10, 10, 10));

    // Set up the scene and add the VBox to it
    Scene scene = pie.getScene(saveFileLayout);
    piechartStage.setScene(scene);

    // Show the stage (display the pie chart with the legend)
    piechartStage.show();
  }

  private String CurrentDateStr() {
    Date currentDate = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("--dd-MM-yyyy");
    String formattedDate = dateFormat.format(currentDate);
    return formattedDate;
  }
}
