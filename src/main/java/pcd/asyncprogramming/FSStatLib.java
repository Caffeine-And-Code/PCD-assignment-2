package pcd.asyncprogramming;

import io.vertx.core.Future;
import pcd.FSReport;

public interface FSStatLib {
    Future<FSReport> getFSReport(String directory, long maxFileSize, int numberOfBands, FSStatSession session);
}
