package zandbak;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class JavaFXTaskDemo extends Application {

    private ProgressBar progressBar;
    private TextArea logArea;
    private Button startButton;
    private Button cancelButton;
    private Task<Void> task;

    @Override
    public void start(Stage stage) {
        // Boven: knoppen
        startButton = new Button("Start taak");
        cancelButton = new Button("Annuleer");
        cancelButton.setDisable(true);
        HBox buttonBar = new HBox(8, startButton, cancelButton);
        buttonBar.setPadding(new Insets(8));

        // Midden: log
        logArea = new TextArea();
        logArea.setEditable(false);

        // Onder: voortgangsbalk
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(buttonBar);
        root.setCenter(logArea);
        root.setBottom(progressBar);

        startButton.setOnAction(e -> startTask());
        cancelButton.setOnAction(e -> {
            if (task != null && task.isRunning()) {
                task.cancel(true);
            }
        });

        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("JavaFX Task Demo");
        stage.setScene(scene);
        stage.show();
    }

    private void startTask() {
        logArea.clear();
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        startButton.setDisable(true);
        cancelButton.setDisable(false);

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Deze code draait op een achtergrondthread.
                for (int i = 1; i <= 100; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    Thread.sleep(50); // Simuleer zwaar werk

                    // Tussentijds resultaat (string) naar de UI-thread
                    if (i % 10 == 0) {
                        updateMessage("Item " + i + " verwerkt");
                    }

                    // Voortgang bijwerken (0.0 - 1.0)
                    updateProgress(i, 100);
                }
                return null;
            }
        };

        // Koppel de properties van de Task aan de UI
        progressBar.progressProperty().bind(task.progressProperty());

        // Luister naar tussentijdse berichten en schrijf ze naar de log
        task.messageProperty().addListener((obs, oud, nieuw) -> {
            if (nieuw != null) {
                logArea.appendText(nieuw + "\n");
            }
        });

        // Acties na voltooiing / annulering / falen
        task.setOnSucceeded(e -> {
            logArea.appendText(">> Taak voltooid!\n");
            resetButtons();
        });
        task.setOnCancelled(e -> {
            logArea.appendText(">> Taak geannuleerd.\n");
            resetButtons();
        });
        task.setOnFailed(e -> {
            logArea.appendText(">> Taak mislukt: " + task.getException() + "\n");
            resetButtons();
        });

        // Start de taak op een nieuwe achtergrondthread
        Thread thread = new Thread(task, "achtergrondtaak");
        thread.setDaemon(true); // Zorg dat de app kan afsluiten
        thread.start();
    }

    private void resetButtons() {
        // Wordt aangeroepen vanuit een event handler, dus al op de FX-thread
        startButton.setDisable(false);
        cancelButton.setDisable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}