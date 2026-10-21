package pcd.gui.specificviewmodels;

import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.asyncprogramming.FSStatLib;
import pcd.asyncprogramming.FSStatSession;
import pcd.gui.FsStatViewModel;

/**
 * ViewModel for the FSStat JavaFX UI.
 * Holds presentation state and coordinates the background report calculation.
 * It does not depend on JavaFX controls or on the View.
 */
public final class FsStatAsyncProgrammingViewModel extends FsStatViewModel {

    private final FSStatLib fsStatLib;
    private FSStatSession session;

    public FsStatAsyncProgrammingViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();
        session.onUpdate(this.report::setValue);

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

    }
}
