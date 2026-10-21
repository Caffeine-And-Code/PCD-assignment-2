package pcd.gui.specificviewmodels;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.gui.FsStatViewModel;
import pcd.virtualthread.FSStatLib;
import pcd.virtualthread.FSStatSession;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class FsStatVirtualThreadsViewModel extends FsStatViewModel {
    private final FSStatLib fsStatLib;
    private FSStatSession session;
    private volatile Future<FSReport> result;

    public FsStatVirtualThreadsViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();
        // Updates arrive from virtual threads, one per file.
        // Each is a full snapshot, so only the latest matters:
        // keep at most one runLater pending to avoid flooding the FX thread.
        AtomicReference<FSReport> latest = new AtomicReference<>();
        session.onUpdate(update -> {
            if (latest.getAndSet(update) == null) {
                Platform.runLater(() -> {
                    FSReport snapshot = latest.getAndSet(null);
                    if (running.get() && snapshot != null) {
                        report.set(snapshot);
                    }
                });
            }
        });

        currentTask = new Task<>() {
            @Override
            protected FSReport call() throws ExecutionException, InterruptedException {
                result = fsStatLib.getFSReport(absolutePathOfDir, maxBytes, bands, session);
                try {
                    return result.get();
                } catch (InterruptedException e) {
                    // propagate the cancel action to the lib's root future.
                    result.cancel(true);
                    throw e;
                }
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        Future<FSReport> current = result;
        if (current != null) {
            current.cancel(true);
        }
    }
}