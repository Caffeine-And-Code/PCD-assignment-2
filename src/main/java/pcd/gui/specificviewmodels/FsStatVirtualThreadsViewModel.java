package pcd.gui.specificviewmodels;

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
    private Future<FSReport> result;

    public FsStatVirtualThreadsViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();
        session.onUpdate(this.report::setValue);

        currentTask = new Task<>() {
            @Override
            protected FSReport call() throws ExecutionException, InterruptedException {
                result = fsStatLib.getFSReport(
                        absolutePathOfDir,
                        maxBytes,
                        bands,
                        session
                );

                return result.get();
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        result.cancel(true);
    }
}
