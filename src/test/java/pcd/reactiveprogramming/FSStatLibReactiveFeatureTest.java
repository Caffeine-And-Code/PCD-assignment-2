package pcd.reactiveprogramming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

class FSStatLibReactiveFeatureTest {
    @Test
    @Timeout(10)
    void scansTestDataAndCountsEveryFileOnce() {
        new FSStatLibReactive()
                .getFSReport("test-data", 100, 4, new FSStatSession())
                .lastOrError()
                .test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValue(report ->
                        report.bandsCount().equals(List.of(3, 2, 2, 2, 1))
                                && report.fileCount() == 10
                );
    }
}
