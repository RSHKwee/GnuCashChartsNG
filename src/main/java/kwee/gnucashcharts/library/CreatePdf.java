package kwee.gnucashcharts.library;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Text;

import kwee.library.TimeStamp;
import kwee.logger.MyLogger;

public class CreatePdf {
  private static final Logger lOGGER = MyLogger.getLogger();

  /*
   * @formatter:off
   * For A4 paper size:
   * If you want to work with centimeters instead of points, 
   * you can convert the centimeter values to points using the fact that there 
   * are approximately 28.35 points in a centimeter. 
   * Here's how you can calculate the coordinates of the top-right corner of
   * an A4 paper in centimeters:
   *
   * X-coordinate: A4 width in centimeters = 21.0 cm,
   *    converted to points = 21.0 * 28.35 = 595.35 points.
   * Y-coordinate: A4 height in centimeters = 29.7 cm, 
   *    converted to points = 29.7 * 28.35 = 841.245 points.
   *    
   * So, the top-right corner of an A4 paper in the PDF coordinate system 
   * (assuming portrait orientation) would be approximately (595.35, 841.245) points.
   * 
   * 
   * 
   * For A3 paper size, the dimensions and coordinates of the corners in points (1 inch = 72 points) depend on whether you're working with portrait or landscape orientation.

Portrait Orientation (Width x Height):

A3 dimensions: 297mm x 420mm
Width in points: 841.89 points (297mm x 72 points per inch)
Height in points: 1190.55 points (420mm x 72 points per inch)
The coordinates of the four corners in portrait orientation are as follows:

Top-left corner: (0, 1190.55)
Top-right corner: (841.89, 1190.55)
Bottom-left corner: (0, 0)
Bottom-right corner: (841.89, 0)
Landscape Orientation (Width x Height):

A3 dimensions: 420mm x 297mm
Width in points: 1190.55 points (420mm x 72 points per inch)
Height in points: 841.89 points (297mm x 72 points per inch)
The coordinates of the four corners in landscape orientation are as follows:

Top-left corner: (0, 841.89)
Top-right corner: (1190.55, 841.89)
Bottom-left corner: (0, 0)
Bottom-right corner: (1190.55, 0)
You can use these coordinates to position elements and set the page size when working with A3 paper in your PDF creation code.

   * 
   * 
   * @formatter:on
   */
  public enum c_PageSizeEnum {
    A0, A1, A2, A3, A4, A2P, A3P, A4P
  };

  private float xMargin = 20;
  private float yMargin = 1;

  private float yRunning = -1;

  // X,Y coordinate for the title
  private float xTitle = 50; // Fixed
  private float yTitle = 520;
  private float yTitle_Offset = 30;

  // X,Y coordinate for the footer text
  private float xFooter = 700;
  private float yFooter = 10; // Fixed
  private float xFooter_Offset = 140;
  private float yFooter_Offset = 70; // Fixed

  private float pageHeight = PDRectangle.A2.getHeight();
  private float pageWidth = PDRectangle.A2.getWidth();

  private String m_PdfFile = "";
  private PDDocument m_document;
  private PDPage m_page;
  private PDPageContentStream contentStream = null;

  private c_PageSizeEnum CurrentPageSize;
  private String CurrentTitle = "";
  private PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

  /**
   * Constructor
   * 
   * @param a_pdfFile Filename of PDF file to be created.
   */
  public CreatePdf(String a_pdfFile) {
    m_PdfFile = a_pdfFile;
    // Create a new PDF document
    m_document = new PDDocument();
  }

  /**
   * Save document and create file.
   */
  public void SaveDocument() throws IOException {
    // Close the content stream
    addFooter(contentStream, TimeStamp.getTimeStampNow());
    contentStream.close();

    m_document.save(m_PdfFile);
    m_document.close();
  }

  public void CreatePage(c_PageSizeEnum a_PageSize, String aTitle) throws IOException {
    // Close a content stream for the page
    if (contentStream != null) {
      addFooter(contentStream, TimeStamp.getTimeStampNow());
      contentStream.close();
    }

    m_page = new PDPage();
    switch (a_PageSize) {
    case A0: // Landscape
      pageWidth = PDRectangle.A0.getHeight();
      pageHeight = PDRectangle.A0.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A1: // Landscape
      pageWidth = PDRectangle.A1.getHeight();
      pageHeight = PDRectangle.A1.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A2: // Landscape
      pageWidth = PDRectangle.A2.getHeight();
      pageHeight = PDRectangle.A2.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A3: // landscape
      pageWidth = PDRectangle.A3.getHeight();
      pageHeight = PDRectangle.A3.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A4: // Landscape
      pageWidth = PDRectangle.A4.getHeight();
      pageHeight = PDRectangle.A4.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A2P: // Portrait
      pageHeight = PDRectangle.A2.getHeight();
      pageWidth = PDRectangle.A2.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A3P: // Portrait
      pageHeight = PDRectangle.A3.getHeight();
      pageWidth = PDRectangle.A3.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    case A4P: // Portrait
      pageHeight = PDRectangle.A4.getHeight();
      pageWidth = PDRectangle.A4.getWidth();
      calcTitleAndFooterCoord(pageWidth, pageHeight);
      break;
    }
    CurrentPageSize = a_PageSize;
    CurrentTitle = aTitle;
    yRunning = -1;

    // Set the page dimensions for landscape orientation
    m_page.setMediaBox(new PDRectangle(pageWidth, pageHeight));
    m_document.addPage(m_page);

    // Create a content stream for the page
    contentStream = new PDPageContentStream(m_document, m_page);
    addTitle(contentStream, aTitle);
  }

  /**
   * Add a Chart image to a page
   * 
   * @param aChartImage
   * @param aTitle
   * @throws IOException
   */
  public void addImageTable(WritableImage aChartImage) throws IOException {
    // Convert the WritableImage to BufferedImage
    BufferedImage ChartImage = SwingFXUtils.fromFXImage(aChartImage, null);

    // Convert the JavaFX image to PDF image
    PDImageXObject pdfChartImage = LosslessFactory.createFromImage(m_document, ChartImage);

    // Draw the images on the page
    float scale = pageHeight / ChartImage.getWidth();
    float imageWidth = (ChartImage.getWidth() - (xMargin * 2)) * scale;
    float imageHeight = ChartImage.getHeight() * scale;

    float xPos = (float) (((pageWidth - imageWidth) / 2.0) - xMargin);
    if (yRunning < 0) {
      yRunning = yTitle - imageHeight - yMargin;
    } else {
      yRunning = yRunning - imageHeight - yMargin;
    }
    contentStream.drawImage(pdfChartImage, xPos, yRunning, imageWidth, imageHeight);
  }

  /**
   * Add a Chart image and a Legend to a page
   * 
   * @param aChartImage  Image of Chart
   * @param aLegendImage Image of Legend.
   * @param aTitle       Title for Chart
   * @throws IOException may be thrown
   */
  public void addImageAndLegend(WritableImage aChartImage, WritableImage aLegendImage) throws IOException {
    // Convert the WritableImage to BufferedImage
    BufferedImage ChartImage = SwingFXUtils.fromFXImage(aChartImage, null);
    BufferedImage LegendImage = SwingFXUtils.fromFXImage(aLegendImage, null);

    // Convert the JavaFX image to PDF image
    PDImageXObject pdfPieChartImage = LosslessFactory.createFromImage(m_document, ChartImage);
    PDImageXObject pdfLegendImage = LosslessFactory.createFromImage(m_document, LegendImage);

    // Draw the images on the page
    float imageWidth = ChartImage.getWidth();
    float imageHeight = ChartImage.getHeight();
    float l_xpos = (float) (((pageWidth - imageWidth) / 2.0) - xMargin);
    if (yRunning < 0) {
      yRunning = yTitle - imageHeight - 2 * yMargin;
    } else {
      yRunning = yRunning - imageHeight - 2 * yMargin;
    }
    contentStream.drawImage(pdfPieChartImage, l_xpos, yRunning, imageWidth, imageHeight);

    float legendImageWidth = LegendImage.getWidth();
    float legendImageHeight = LegendImage.getHeight();
    l_xpos = xMargin;
    yRunning = yRunning - legendImageHeight - 2 * yMargin;
    contentStream.drawImage(pdfLegendImage, l_xpos, yRunning, legendImageWidth, legendImageHeight);
  }

  /**
   * 
   * @param a_Table
   * @throws IOException
   */
  public void addTable(TableView<String[]> a_Table, boolean a_sortdesc) throws IOException {
    // Create a table in the PDF to mimic the TableView
    float tableWidth = m_page.getMediaBox().getWidth() - 2 * xMargin;
    float rowHeight = 20f;
    // yRunning = yStart - yTitle_Offset;

    int rows = a_Table.getItems().size();
    int cols = a_Table.getColumns().size();

    float tableXLength = tableWidth;
    addTableHeader(a_Table);

    // Add data to the PDF table
    yRunning -= rowHeight;
    if (a_sortdesc) {
      for (int i = rows - 1; i >= 0; i--) {
        yRunning -= rowHeight;
        if (yRunning < yFooter_Offset) {
          // New page
          CreatePage(CurrentPageSize, CurrentTitle);
          addTableHeader(a_Table);
          yRunning -= rowHeight;
          yRunning -= rowHeight;
        }
        for (int j = 0; j < cols; j++) {
          TableColumn<?, ?> column = a_Table.getColumns().get(j);
          column.setCellFactory(new WrappableHeaderCellFactory<>());

          contentStream.setFont(font, 10);
          contentStream.beginText();
          contentStream.newLineAtOffset(xMargin + j * (tableXLength / cols), yRunning);
          contentStream.showText(column.getCellData(i).toString());
          contentStream.endText();
        }
      }
    } else {
      for (int i = 0; i < rows; i++) {
        yRunning -= rowHeight;
        if (yRunning < yFooter_Offset) {
          // New page
          CreatePage(CurrentPageSize, CurrentTitle);
          addTableHeader(a_Table);
          yRunning -= rowHeight;
          yRunning -= rowHeight;
        }
        for (int j = 0; j < cols; j++) {
          TableColumn<?, ?> column = a_Table.getColumns().get(j);
          column.setCellFactory(new WrappableHeaderCellFactory<>());

          contentStream.setFont(font, 10);
          contentStream.beginText();
          contentStream.newLineAtOffset(xMargin + j * (tableXLength / cols), yRunning);
          contentStream.showText(column.getCellData(i).toString());
          contentStream.endText();
        }
      }
    }
  }

  // Private functions
  private void addTableHeader(TableView<String[]> a_Table) throws IOException {
    int cols = a_Table.getColumns().size();
    float tableWidth = m_page.getMediaBox().getWidth() - 2 * xMargin;
    float tableXLength = tableWidth;
    float yStart = m_page.getMediaBox().getHeight() - yTitle_Offset;
    yRunning = yStart - yTitle_Offset;

    // Draw table headers
    float yPositionHeader = yRunning;

    for (int j = 0; j < cols; j++) {
      TableColumn<?, ?> column = a_Table.getColumns().get(j);
      column.setCellFactory(new WrappableHeaderCellFactory<>());
      double width = column.getWidth();
      Text tekst = new Text(column.getText());
      double tekstwidth = tekst.getLayoutBounds().getWidth();
      lOGGER.log(Level.FINE, column.getText() + "| Tekst width: " + Double.toString(tekstwidth) + "| Col width: "
          + Double.toString(width));

      contentStream.setFont(font, 8);
      if (tekstwidth > (tableXLength / cols)) {
        String coltekst = column.getText();
        String[] coltekstelm = coltekst.split(" ");
        if (coltekstelm.length > 2) {
          coltekstelm[0] = coltekstelm[0] + " " + coltekstelm[1];
          coltekstelm[1] = coltekstelm[2];
          coltekstelm[2] = "";
        }
        for (int k = 0; k < coltekstelm.length; k++) {
          if (!coltekstelm[k].isBlank()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xMargin + j * (tableXLength / cols), yPositionHeader - (k * 10));
            contentStream.showText(coltekstelm[k]);
            contentStream.endText();
            lOGGER.log(Level.FINE, coltekstelm[k] + "| X: " + Double.toString(xMargin + j * (tableXLength / cols))
                + "| Y: " + Double.toString(yPositionHeader - (k * 10)));
          }
        }
      } else {
        contentStream.beginText();
        contentStream.newLineAtOffset(xMargin + j * (tableXLength / cols), yPositionHeader);
        contentStream.showText(column.getText());
        contentStream.endText();
      }
    }

  }

  /**
   * Add title to Page
   * 
   * @param contentStream Stream to buildup page
   * @param text          Title text
   * @throws IOException may be thrown
   */
  private void addTitle(PDPageContentStream contentStream, String text) throws IOException {
    contentStream.beginText();
    contentStream.setFont(font, 18); // Choose your font
    contentStream.newLineAtOffset(xTitle, yTitle + 5); // Adjust the coordinates
    contentStream.showText(text);
    contentStream.endText();
  }

  /**
   * Add footer to Page
   * 
   * @param contentStream Stream to buildup page
   * @param text          Footer text
   * @throws IOException may be thrown
   */
  private void addFooter(PDPageContentStream contentStream, String text) throws IOException {
    contentStream.beginText();
    contentStream.setFont(font, 10);

    contentStream.newLineAtOffset(xFooter, yFooter);
    contentStream.showText(text);
    contentStream.endText();
  }

  /**
   * Calculate position Title and Footer, depends on Page size
   * 
   * @param a_PageWidth  Width of Page in points
   * @param a_PageHeight Height of Page in points
   */
  private void calcTitleAndFooterCoord(float a_PageWidth, float a_PageHeight) {
    yTitle = a_PageHeight - yTitle_Offset;
    xFooter = a_PageWidth - xFooter_Offset;
  }

}
