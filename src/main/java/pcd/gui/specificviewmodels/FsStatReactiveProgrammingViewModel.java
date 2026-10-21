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

        AtomicReference<FSReport> latest = new AtomicReference<>();

        currentTask = new Task<>() {
            @Override
            protected FSReport call() {
                try {
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