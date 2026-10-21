package pcd.gui.specificviewmodels;

import javafx.concurrent.Task;
import pcd.FSReport;
import pcd.gui.FsStatViewModel;
import pcd.reactiveprogramming.FSStatLib;
import pcd.reactiveprogramming.FSStatSession;

public class FsStatReactiveProgrammingViewModel extends FsStatViewModel {
    private final FSStatLib fsStatLib;
    private FSStatSession session;

    public FsStatReactiveProgrammingViewModel(FSStatLib fsStatLib) {
        this.fsStatLib = fsStatLib;
    }

    @Override
    public void onFreshStart(String absolutePathOfDir, long maxBytes, int bands) {
        session = new FSStatSession();

        currentTask = new Task<>() {
            @Override
            protected FSReport call() {
                fsStatLib.getFSReport(absolutePathOfDir, maxBytes, bands, session)
                        .subscribe(
                                report::setValue,
                                Throwable::printStackTrace
                        );

                return null;
            }
        };
    }

    @Override
    public void additionalOnStopSteps() {
        session.cancel();
    }
}
