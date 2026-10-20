package pcd.asyncprogramming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pcd.FSReport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FSStatLibAsyncFeatureTest {
    @Test
    @Timeout(10)
    void scansTestDataAndCountsEveryFileOnce() {
        var lib = new FSStatLibAsync();
        FSReport report = lib.getFSReport("test-data", 100, 4, new FSStatSession()).await();

        assertAll(
                () -> assertEquals(List.of(3, 2, 2, 2, 1), report.bandsCount()),
                () -> assertEquals(10,
                        report.bandsCount().stream().mapToInt(Integer::intValue).sum())
        );
    }
}
