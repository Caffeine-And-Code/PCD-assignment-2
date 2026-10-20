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
        FSReport report = new FSStatLibAsync().getFSReport("test-data", 100, 4);

        assertAll(
                () -> assertEquals(List.of(3, 2, 2, 2, 1), report.bandsCount()),
                () -> assertEquals(10,
                        report.bandsCount().stream().mapToInt(Integer::intValue).sum())
        );
    }
}
