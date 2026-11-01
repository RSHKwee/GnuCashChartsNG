package kwee.gnucashcharts.library;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class WrappableHeaderCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
  @Override
  public TableCell<S, T> call(TableColumn<S, T> param) {
    return new TableCell<S, T>() {
      private Label label;

      {
        label = new Label();
        label.setWrapText(true); // Allow text to wrap
        setGraphic(label);
      }

      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
          label.setText(item.toString());
        } else {
          label.setText(null);
        }
      }
    };
  }
}
