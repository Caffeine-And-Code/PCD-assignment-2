package pcd.virtualthread;

import pcd.FSReport;

import java.util.concurrent.Future;

public interface FSStatLib {
    Future<FSReport> getFSReport(String directory, long maxFileSize, int numberOfBands, FSStatSession session);
}
