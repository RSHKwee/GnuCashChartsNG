package zandbak;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.chart.StackedBarChart;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.image.BufferedImage;
import java.io.File;

public class slaChartOp {
  public void slaChartOpAlsPdf(StackedBarChart<String, Number> chart,
      File output) throws Exception {

    // 1. Zorg dat de chart CSS en layout heeft toegepast (belangrijk voor
    // snapshot)
    chart.applyCss();
    chart.layout();

    // 2. Snapshot met hogere resolutie (schaal 2x voor scherpte)
    SnapshotParameters params = new SnapshotParameters();
    params.setTransform(javafx.scene.transform.Transform.scale(2, 2));
    WritableImage fxImage = chart.snapshot(params, null);

    // 3. JavaFX image -> BufferedImage
    BufferedImage buffered = SwingFXUtils.fromFXImage(fxImage, null);

    // 4. PDF aanmaken
    try (PDDocument doc = new PDDocument()) {
      // Paginaformaat: pas aan naar wens (bijv. A4 liggend)
      PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);

      // 5. Afbeelding als lossless XObject in de PDF zetten
      PDImageXObject pdImage = LosslessFactory.createFromImage(doc, buffered);

      try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
        // Schaal de afbeelding zodat hij op de pagina past
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        float imgWidth = pdImage.getWidth();
        float imgHeight = pdImage.getHeight();

        // Behoud aspectratio, maximaal 90% van de pagina
        float maxW = pageWidth * 0.9f;
        float maxH = pageHeight * 0.9f;
        float scale = Math.min(maxW / imgWidth, maxH / imgHeight);
        float drawW = imgWidth * scale;
        float drawH = imgHeight * scale;

        // Centreer op de pagina
        float x = (pageWidth - drawW) / 2;
        float y = (pageHeight - drawH) / 2;

        stream.drawImage(pdImage, x, y, drawW, drawH);
      }

      doc.save(output);
    }
  }
}
