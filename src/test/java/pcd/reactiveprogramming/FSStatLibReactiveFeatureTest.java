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

    @Test
    @Timeout(10)
    void emitsProgressiveReports() {
        new FSStatLibReactive()
                .getFSReport("test-data", 100, 4)
                .test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(11)
                .assertValueAt(0, report -> report.fileCount() == 0)
                .assertValueAt(10, report ->
                        report.bandsCount().equals(List.of(3, 2, 2, 2, 1))
                                && report.fileCount() == 10
                );
    }

    @Test
    @Timeout(10)
    void canStopAReportGeneration() {
        new FSStatLibReactive()
                .getFSReport("test-data", 100, 4)
                .take(3)
                .test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(3)
                .assertValueAt(0, report -> report.fileCount() == 0)
                .assertValueAt(1, report -> report.fileCount() == 1)
                .assertValueAt(2, report -> report.fileCount() == 2);
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

        report.lastOrError()
                .test()
                .awaitDone(10, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoErrors()
                .assertValue(value -> value.fileCount() == 1);
    }
}
