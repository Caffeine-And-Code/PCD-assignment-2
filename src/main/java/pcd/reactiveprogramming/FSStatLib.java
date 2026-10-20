package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Single;

public interface FSStatLib {
    Single<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands
    );
}
