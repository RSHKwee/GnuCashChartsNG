package zandbak;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Chart;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class ChartPdfExporter {

  private ChartPdfExporter() {
  }

  /**
   * Slaat een willekeurige JavaFX Chart op als PDF (één pagina, gecentreerd,
   * aspectratio behouden).
   *
   * @param chart  de chart (StackedBarChart, PieChart, LineChart, ...)
   * @param output doelbestand
   * @param scale  resolutiefactor (2.0 = 2x, scherper maar groter bestand)
   * @param page   paginagrootte, bijv. PDRectangle.A4 of
   *               PDRectangle.A4.rotate()
   */
  public static void export(Chart chart, File output, double scale,
      PDRectangle page) throws IOException {

    // 1. Zorg dat CSS en layout zijn toegepast
    chart.applyCss();
    chart.layout();

    // 2. Snapshot op hogere resolutie
    SnapshotParameters params = new SnapshotParameters();
    params.setTransform(Transform.scale(scale, scale));
    WritableImage fxImage = chart.snapshot(params, null);

    // 3. JavaFX -> AWT
    BufferedImage buffered = SwingFXUtils.fromFXImage(fxImage, null);

    // 4. PDF
    try (PDDocument doc = new PDDocument()) {
      PDPage pdfPage = new PDPage(page);
      doc.addPage(pdfPage);

      PDImageXObject pdImage = LosslessFactory.createFromImage(doc, buffered);

      try (PDPageContentStream stream = new PDPageContentStream(doc, pdfPage)) {
        float pageW = page.getWidth();
        float pageH = page.getHeight();

        float imgW = pdImage.getWidth();
        float imgH = pdImage.getHeight();

        // Max 90% van de pagina, aspectratio behouden
        float maxW = pageW * 0.9f;
        float maxH = pageH * 0.9f;
        float s = Math.min(maxW / imgW, maxH / imgH);
        float drawW = imgW * s;
        float drawH = imgH * s;

        float x = (pageW - drawW) / 2f;
        float y = (pageH - drawH) / 2f;

        stream.drawImage(pdImage, x, y, drawW, drawH);
      }

      doc.save(output);
    }
  }

  // Gemaks-overload met standaardwaarden
  public static void export(Chart chart, File output) throws IOException {
    export(chart, output, 2.0, PDRectangle.A4);
  }
}
