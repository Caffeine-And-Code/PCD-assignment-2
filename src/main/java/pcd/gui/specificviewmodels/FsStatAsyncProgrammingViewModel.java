package pcd.gui.specificviewmodels;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.asyncprogramming.FSStatLib;
import pcd.asyncprogramming.FSStatSession;
import pcd.gui.FsStatViewModel;

public final class FsStatAsyncProgrammingViewModel extends FsStatViewModel {

    private final FSStatLib fsStatLib;
    private FSStatSession session;

    public FsStatAsyncProgrammingViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();

        session.onUpdate((report) -> Platform.runLater(() -> this.report.setValue(report)));

        currentTask = new Task<>() {
            @Override
            protected FSReport call() {
                return fsStatLib.getFSReport(
                        absolutePathOfDir,
                        maxBytes,
                        bands,
                        session
                ).await();
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        session.cancel();
    }
}
