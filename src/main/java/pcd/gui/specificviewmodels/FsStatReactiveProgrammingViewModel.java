package pcd.gui.specificviewmodels;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.gui.FsStatViewModel;
import pcd.reactiveprogramming.FSStatLib;
import pcd.reactiveprogramming.FSStatSession;

import java.util.concurrent.atomic.AtomicReference;

public class FsStatReactiveProgrammingViewModel extends FsStatViewModel {
    private final FSStatLib fsStatLib;
    private FSStatSession session;

    public FsStatReactiveProgrammingViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();

        // Each emission is a full snapshot: keep at most one runLater pending (see virtual threads VM).
        AtomicReference<FSReport> latest = new AtomicReference<>();

        currentTask = new Task<>() {
            @Override
            protected FSReport call() {
                try {
                    // Block the worker until the stream completes, so the Task lives as long as the scan.
                    return fsStatLib.getFSReport(absolutePathOfDir, maxBytes, bands, session)
                            .doOnNext(update -> {
                                if (latest.getAndSet(update) == null) {
                                    Platform.runLater(() -> {
                                        FSReport snapshot = latest.getAndSet(null);
                                        if (running.get() && snapshot != null) {
                                            report.set(snapshot);
                                        }
                                    });
                                }
                            })
                            .blockingLast();
                } catch (RuntimeException e) {
                    // Task.cancel(true) interrupts blockingLast: that is a stop, not a failure.
                    if (isCancelled()) {
                        return null;
                    }
                    throw e;
                }
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        session.cancel();
    }
}