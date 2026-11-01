package kwee.gnucashcharts.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class SubjectsColors {
  private Map<String, Color> m_SubjColour = new HashMap<String, Color>();
  private int Teller = 0;

  public SubjectsColors(ArrayList<String> a_Accounts) {
    Teller = 0;
    a_Accounts.forEach(Acc -> {
      Color l_Color = getDistinctColor(Teller, a_Accounts.size());
      m_SubjColour.put(Acc, l_Color);
      Teller++;
    });
  }

  public Color getColor(String a_Account) {
    return m_SubjColour.get(a_Account);
  }

  public String toHex(Color color) {
    return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  private Color getDistinctColor(int colorIndex, int numColors) {
    int baseHue = 30; // Starting hue value
    // Calculate the hue value based on the index
    int hue = (baseHue + (colorIndex * (360 / numColors))) % 360;
    // Convert the hue value to an RGB color
    Color color = Color.hsb(hue, 1.0, 1.0);
    return color;
  }

}
