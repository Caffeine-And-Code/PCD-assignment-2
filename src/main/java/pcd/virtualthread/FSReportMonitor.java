package pcd.virtualthread;

import pcd.FSReport;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class FSReportMonitor {
    private FSReport report;
    private final ReentrantLock lock;

    public FSReportMonitor(int numberOfBands){
        this.report = FSReport.Empty(numberOfBands);
        lock = new ReentrantLock();
    }

    public FSReport udpateReport(Function<FSReport, FSReport> updater){
        lock.lock();
        try{
            this.report = updater.apply(report);
            return report;
        }finally {
            lock.unlock();
        }
    }

    public FSReport getReport(){
        lock.lock();
        try{
            return this.report;
        }finally {
            lock.unlock();
        }
    }

}
