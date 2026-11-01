package kwee.gnucashcharts.library.html;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kwee.logger.MyLogger;

public class ReadHTMLTable {
  private static final Logger lOGGER = MyLogger.getLogger();
  private String m_content = "";
  private Charset charsetName = Charset.forName("UTF-8");

  /**
   * Constructor, read HTML page from file or URL
   * 
   * @param a_location File or URL
   */
  public ReadHTMLTable(String a_location) {
    try {
      // From file
      m_content = readHTMLFromFile(a_location);
      lOGGER.log(Level.FINEST, m_content);
    } catch (IOException e) {
      try {
        // From an URL
        m_content = fetchHTML(a_location);
      } catch (IOException | URISyntaxException e1) {
        lOGGER.log(Level.INFO, e.getMessage());
      }
      lOGGER.log(Level.INFO, e.getMessage());
    }
  }

  /**
   * Parse table information on page.
   * 
   * @return Table, first row is Header then in each row data as a String, semicolon delimited
   */
  public ArrayList<String> parseHTMLpage() {
    ArrayList<String> dataregels = new ArrayList<String>();
    // Parse the HTML content using Jsoup
    Document doc = Jsoup.parse(m_content);

    // Select the table element (if you know its ID or class, use that for more
    // precise selection)
    Elements tables = doc.select("table");

    // Process the table data
    for (Element table : tables) {
      // Get the header row (first row in the table)
      Elements headerCells = table.select("tr").first().select("th, td");
      String[] headerTexts = new String[headerCells.size()];
      for (int i = 0; i < headerCells.size(); i++) {
        Element header = headerCells.get(i);
        headerTexts[i] = header.text().isEmpty() ? "Column " + (i + 1) : header.text();
      }

      String Header = "";
      for (String headerText : headerTexts) {
        Header = Header + headerText + ";";
      }
      dataregels.add(Header);

      // Process the remaining rows (excluding the header)
      Elements dataRows = table.select("tr").not(":first-child");

      for (Element row : dataRows) {
        Elements dataCells = row.select("td");
        String line = "";
        for (Element cell : dataCells) {
          line = line + cell.text() + ";";
        }
        dataregels.add(line);
      }
    }
    return dataregels;
  }

  // Private functions
  /*
   * Read HTML page from file.
   * 
   * @return Page content as String
   */
  private String readHTMLFromFile(String filePath) throws IOException {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath, charsetName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }
    }
    return content.toString();
  }

  /*
   * Read HTML page from an URL.
   * 
   * @return Page content as String
   */
  private String fetchHTML(String url) throws IOException, URISyntaxException {
    URI uri = new URI(url);
    URL website = uri.toURL();

    HttpURLConnection connection = (HttpURLConnection) website.openConnection();
    connection.setRequestMethod("GET");

    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }
    }
    return content.toString();
  }
}
