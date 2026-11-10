package zandbak;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SwingWorkerDemo {

    private JFrame frame;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JButton startButton;
    private JButton cancelButton;
    private SwingWorker<Void, String> worker;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingWorkerDemo().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("SwingWorker Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        // Boven: knoppen
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Start taak");
        cancelButton = new JButton("Annuleer");
        cancelButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        frame.add(buttonPanel, BorderLayout.NORTH);

        // Midden: log
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Onder: voortgangsbalk
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        frame.add(progressBar, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startTask());
        cancelButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true); // Onderbreekt doInBackground
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void startTask() {
        logArea.setText("");
        progressBar.setValue(0);
        startButton.setEnabled(false);
        cancelButton.setEnabled(true);

        worker = new SwingWorker<Void, String>() {

            @Override
            protected Void doInBackground() throws Exception {
                // Deze code draait op een achtergrondthread.
                for (int i = 1; i <= 100; i++) {
                    // Respecteer annulering: stop netjes als cancel() is aangeroepen
                    if (isCancelled()) {
                        break;
                    }

                    // Simuleer zwaar werk
                    Thread.sleep(50);

                    // Tussentijds resultaat naar de EDT sturen
                    if (i % 10 == 0) {
                        publish("Item " + i + " verwerkt");
                    }

                    // Voortgang bijwerken (0-100)
                    setProgress(i);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Draait op de EDT. chunks kan meerdere items bevatten.
                for (String msg : chunks) {
                    logArea.append(msg + "\n");
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                // Draait op de EDT, ook na annulering.
                cancelButton.setEnabled(false);
                startButton.setEnabled(true);

                if (isCancelled()) {
                    logArea.append(">> Taak geannuleerd.\n");
                } else {
                    logArea.append(">> Taak voltooid!\n");
                }
            }
        };

        // Koppel setProgress() aan de JProgressBar via een PropertyChangeListener
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }
}
