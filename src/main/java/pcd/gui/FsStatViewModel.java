package pcd.gui;

import javafx.beans.property.*;
import javafx.concurrent.Task;
import pcd.FSReport;

import java.io.File;

public abstract class FsStatViewModel {
    protected Task<FSReport> currentTask;

    protected final StringProperty directory = new SimpleStringProperty("");
    protected final IntegerProperty maxFsKb = new SimpleIntegerProperty(1024);
    protected final IntegerProperty numBands = new SimpleIntegerProperty(10);
    protected final BooleanProperty running = new SimpleBooleanProperty(false);
    protected final StringProperty status = new SimpleStringProperty("Idle");
    protected final StringProperty totalFilesText = new SimpleStringProperty("Total files: –");
    protected final LongProperty totalFiles = new SimpleLongProperty(0);
    protected final ObjectProperty<FSReport> report = new SimpleObjectProperty<>();

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

    public ObjectProperty<FSReport> reportProperty() {
        return report;
    }

    public abstract void onFreshStart(String absolutePathOfDir, long maxBytes, int bands);

    public abstract void additionalOnStopSteps();

    public void start() {
        if (running.get()) {
            return;
        }

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
        status.set("Running...");
        running.set(true);

        this.onFreshStart(selectedDirectory.getAbsolutePath(), maxBytes, bands);

        currentTask.setOnSucceeded(event -> {
            FSReport result = currentTask.getValue();
            report.set(result);
            totalFiles.set(report.getValue().fileCount());
            totalFilesText.set("Total files: " + report.getValue().fileCount());
            status.set("Completed");
            running.set(false);
            currentTask = null;
        });

        currentTask.setOnFailed(event -> {
            Throwable error = currentTask.getException();
            String message = error == null || error.getMessage() == null
                    ? "Unknown error"
                    : error.getMessage();
            status.set("Error: " + message);
            running.set(false);
            currentTask = null;
            this.additionalOnStopSteps();
        });

        currentTask.setOnCancelled(event -> {
            status.set("Stopped");
            running.set(false);
            currentTask = null;
            this.additionalOnStopSteps();
        });

        Thread worker = new Thread(currentTask, "fsstat-report-worker");
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        if (currentTask != null && running.get()) {
            status.set("Stopping...");
            currentTask.cancel(true);
            this.additionalOnStopSteps();
        }
    }

    private void failValidation(String message) {
        status.set(message);
    }
}
