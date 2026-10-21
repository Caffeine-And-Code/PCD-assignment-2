package pcd;

import pcd.virtualthread.FSStatLibVirtualThread;
import pcd.virtualthread.FSStatSession;

import java.util.concurrent.ExecutionException;

public final class Main {
    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        //FsStatApp.launchGui(args);

        var session = new FSStatSession();
        var lib = new FSStatLibVirtualThread();
        session.onUpdate(Main::printReport);
        var result = lib.getFSReport("test-data", 100, 4, session);
        result.cancel(true);
        printReport(result.get());
    }

    static void printReport(FSReport report){
        System.out.println("Begin print report ---------");
        for(var entry: report.bandsCount()){
            System.out.println(entry);
        }
        System.out.println("End print report ---------");
    }
}
