package pcd.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.asyncprogramming.FSStatLib;
import pcd.asyncprogramming.FSStatSession;

import java.io.File;

/**
 * ViewModel for the FSStat JavaFX UI.
 * Holds presentation state and coordinates the background report calculation.
 * It does not depend on JavaFX controls or on the View.
 */
public final class FsStatViewModel {

    private final FSStatLib fsStatLib;
    private FSStatSession session;

    private final StringProperty directory = new SimpleStringProperty("");
    private final IntegerProperty maxFsKb = new SimpleIntegerProperty(1024);
    private final IntegerProperty numBands = new SimpleIntegerProperty(10);
    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final StringProperty status = new SimpleStringProperty("Idle");
    private final StringProperty totalFilesText = new SimpleStringProperty("Total files: –");
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final LongProperty totalFiles = new SimpleLongProperty(0);
    private final ObjectProperty<FSReport> report = new SimpleObjectProperty<>();

    private Task<FSReport> currentTask;

    public FsStatViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
        this.session = new FSStatSession();
    }

    public StringProperty directoryProperty() {
        return directory;
    }

    public IntegerProperty maxFsKbProperty() {
        return maxFsKb;
    }

    public IntegerProperty numBandsProperty() {
        return numBands;
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty totalFilesTextProperty() {
        return totalFilesText;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public LongProperty totalFilesProperty() {
        return totalFiles;
    }

    public ObjectProperty<FSReport> reportProperty() {
        return report;
    }

    /**
     * Starts calculating the report on a JavaFX background Task.
     */
    public void start() {
        if (running.get()) {
            return;
        }

        session = new FSStatSession();
        session.onUpdate(this.report::setValue);

        String path = directory.get() == null ? "" : directory.get().trim();
        if (path.isEmpty()) {
            failValidation("Please select a directory first");
            return;
        }

        File selectedDirectory = new File(path);
        if (!selectedDirectory.isDirectory()) {
            failValidation("Not a valid directory");
            return;
        }

        int bands = numBands.get();
        int maxKb = maxFsKb.get();
        if (bands <= 0 || maxKb <= 0) {
            failValidation("MaxFS and number of bands must be positive");
            return;
        }

        long maxBytes = maxKb * 1024L;
        report.set(null);
        totalFiles.set(0);
        totalFilesText.set("Total files: 0");
        status.set("Running…");
        progress.set(-1); // JavaFX ProgressBar indeterminate progress
        running.set(true);

        currentTask = new Task<>() {
            @Override
            protected FSReport call() {
                return fsStatLib.getFSReport(
                        selectedDirectory.getAbsolutePath(),
                        maxBytes,
                        bands,
                        session
                ).await();
            }
        };

        currentTask.setOnSucceeded(event -> {
            FSReport result = currentTask.getValue();
            report.set(result);
            totalFiles.set(report.getValue().fileCount());
            totalFilesText.set("Total files: " + report.getValue().fileCount());
            status.set("Completed");
            progress.set(1);
            running.set(false);
            currentTask = null;
        });

        currentTask.setOnFailed(event -> {
            Throwable error = currentTask.getException();
            String message = error == null || error.getMessage() == null
                    ? "Unknown error"
                    : error.getMessage();
            status.set("Error: " + message);
            progress.set(0);
            running.set(false);
            currentTask = null;
            this.session.cancel();
        });

        currentTask.setOnCancelled(event -> {
            status.set("Stopped");
            progress.set(0);
            running.set(false);
            currentTask = null;
            this.session.cancel();
        });

        Thread worker = new Thread(currentTask, "fsstat-report-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Requests cancellation of the current Task, if one is running.
     */
    public void stop() {
        if (currentTask != null && running.get()) {
            status.set("Stopping…");
            currentTask.cancel(true);
            this.session.cancel();
        }
    }

    private void failValidation(String message) {
        status.set(message);
        progress.set(0);
    }
}
