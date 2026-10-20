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
                .getFSReport("test-data", 100, 4)
                .test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValue(report ->
                        report.bandsCount().equals(List.of(3, 2, 2, 2, 1))
                                && report.totalFiles() == 10
                );
    }

    @Test
    @Timeout(10)
    void startsScanningOnlyAfterSubscription(@TempDir Path temporaryDirectory)
            throws IOException {

        Path directory = temporaryDirectory.resolve("created-later");
        var report = new FSStatLibReactive()
                .getFSReport(directory.toString(), 100, 4);

        Files.createDirectory(directory);
        Files.write(directory.resolve("file.bin"), new byte[10]);

        report.test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValue(value -> value.totalFiles() == 1);
    }
}
