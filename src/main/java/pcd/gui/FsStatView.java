package pcd.gui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import pcd.gui.components.ViewLayout;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pure GUI for FSStat. It knows nothing about the library: the controller
 * registers a start handler and a stop handler, and pushes updates through
 * {@link #update(long, long[])}, {@link #finished()} and {@link #failed(String)}.
 * All the "push" methods are thread-safe (callable from any thread).
 */
public class FsStatView {

    /**
     * Called when the user presses Start. maxFS is expressed in bytes.
     */
    @FunctionalInterface
    public interface StartHandler {
        void onStart(File directory, long maxFS, int numBands);
    }

    private record Snapshot(long totalFiles, long[] bands) {
    }

    private final ViewLayout viewLayout = new ViewLayout();

    private final AtomicReference<Snapshot> pending = new AtomicReference<>();

    public FsStatView() {
        viewLayout.build(this::chooseDirectory, this::onStartPressed);
        viewLayout.setRunning(false);
    }

    /**
     * Pushes a (partial or final) report: total number of files and the
     * distribution (bands.length must be NB + 1, last = files bigger than MaxFS).
     * Updates are coalesced: if many arrive faster than the UI can draw,
     * only the latest one is rendered.
     */
    public void update(long totalFiles, long[] bands) {
        Snapshot s = new Snapshot(totalFiles, bands.clone());
        if (pending.getAndSet(s) == null) {
            Platform.runLater(() -> {
                Snapshot last = pending.getAndSet(null);
                if (last != null) {
                    render(last);
                }
            });
        }
    }

    /**
     * Report generation completed normally.
     */
    public void finished() {
        runFx(() -> {
            viewLayout.getStatusLabel().setText("Completed");
            viewLayout.getProgress().setProgress(1);
            viewLayout.setRunning(false);
        });
    }

    /**
     * Report generation was stopped by the user.
     */
    public void stopped() {
        runFx(() -> {
            viewLayout.getStatusLabel().setText("Stopped");
            viewLayout.getProgress().setProgress(0);
            viewLayout.setRunning(false);
        });
    }

    /**
     * Report generation failed.
     */
    public void failed(String message) {
        runFx(() -> {
            viewLayout.getStatusLabel().setText("Error: " + message);
            viewLayout.getProgress().setProgress(0);
            viewLayout.setRunning(false);
        });
    }

    // ------------------------------------------------------------ actions

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a directory");
        final var dirField = viewLayout.getDirField();

        if (!dirField.getText().isBlank()) {
            File prev = new File(dirField.getText());

            if (prev.isDirectory()) {
                chooser.setInitialDirectory(prev);
            }
        }

        Window w = viewLayout.getRoot().getScene() != null
                ? viewLayout.getRoot().getScene().getWindow()
                : null;
        File dir = chooser.showDialog(w);

        if (dir != null) {
            dirField.setText(dir.getAbsolutePath());
        }
    }

    private void onStartPressed() {
        final var dirField = viewLayout.getDirField();
        final var statusLabel = viewLayout.getStatusLabel();

        if (dirField.getText().isBlank()) {
            statusLabel.setText("Please select a directory first");
            return;
        }

        File dir = new File(dirField.getText());
        if (!dir.isDirectory()) {
            statusLabel.setText("Not a valid directory");
            return;
        }

        // commit text typed in the editable spinners
        final var maxFsSpinner = viewLayout.getMaxFsSpinner();
        final var nbSpinner = viewLayout.getNbSpinner();

        maxFsSpinner.increment(0);
        nbSpinner.increment(0);

        long maxFS = maxFsSpinner.getValue() * 1024L;
        int nb = nbSpinner.getValue();

        viewLayout.prepareChart(maxFS, nb);
        viewLayout.getTotalLabel().setText("Total files: 0");
        statusLabel.setText("Running…");
        viewLayout.getProgress().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        viewLayout.setRunning(true);

        viewLayout.getStartHandler().onStart(dir, maxFS, nb);
    }

    // ---------------------------------------------------------- rendering

    private void render(Snapshot s) {
        final var series = viewLayout.getSeries();
        viewLayout.getTotalLabel().setText("Total files: " + s.totalFiles());
        int n = Math.min(s.bands().length, series.getData().size());

        for (int i = 0; i < n; i++) {
            series.getData().get(i).setYValue(s.bands()[i]);
        }
    }

    private static void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public Parent getRoot() {
        return viewLayout.getRoot();
    }

    public void setOnStart(FsStatView.StartHandler h) {
        viewLayout.setOnStart(h);
    }

    public void setOnStop(Runnable h) {
        viewLayout.setOnStop(h);
    }
}
