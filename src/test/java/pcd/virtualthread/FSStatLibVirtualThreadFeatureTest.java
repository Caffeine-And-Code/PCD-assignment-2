package pcd.virtualthread;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pcd.FSReport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FSStatLibVirtualThreadFeatureTest {
    @Test
    @Timeout(10)
    void scansTestDataAndCountsEveryFileOnce() throws Exception {
        var lib = new FSStatLibVirtualThread();
        FSReport report = lib.getFSReport("test-data", 100, 4, new FSStatSession()).get();

        assertAll(
                () -> assertEquals(List.of(3, 2, 2, 2, 1), report.bandsCount()),
                () -> assertEquals(10, report.fileCount()),
                () -> assertEquals(10,
                        report.bandsCount().stream().mapToInt(Integer::intValue).sum())
        );
    }
}
