package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Flowable;
import pcd.FSReport;

public interface FSStatLib {

    Flowable<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSStatSession session
    );
}
