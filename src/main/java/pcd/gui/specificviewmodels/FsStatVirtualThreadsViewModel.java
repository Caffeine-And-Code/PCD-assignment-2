package pcd.gui.specificviewmodels;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.gui.FsStatViewModel;
import pcd.virtualthread.FSStatLib;
import pcd.virtualthread.FSStatSession;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FsStatVirtualThreadsViewModel extends FsStatViewModel {
    private final FSStatLib fsStatLib;
    private FSStatSession session;
    private volatile Future<FSReport> result;

    public FsStatVirtualThreadsViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        FSStatSession currentSession = new FSStatSession();
        session = currentSession;
        currentSession.onUpdate(update -> Platform.runLater(() -> {
            if (session != currentSession || !running.get()
                    || currentTask == null || currentTask.isCancelled()) {
                return;
            }
            report.set(update);
        }));

        currentTask = new Task<>() {
            @Override
            protected FSReport call() throws ExecutionException, InterruptedException {
                Future<FSReport> currentResult = fsStatLib.getFSReport(
                        absolutePathOfDir, maxBytes, bands, currentSession);
                result = currentResult;
                try {
                    return currentResult.get();
                } catch (InterruptedException e) {
                    currentResult.cancel(true);
                    throw e;
                }
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        session = null;
        Future<FSReport> current = result;
        if (current != null) {
            current.cancel(true);
        }
    }
}
