package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Flowable;

public interface FSStatLib {

    Flowable<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands
    );
}
