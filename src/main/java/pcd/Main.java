package pcd;

import pcd.asyncprogramming.FSStatLibAsync;
import pcd.asyncprogramming.FSStatSession;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) {
        FSStatLibAsync lib = new FSStatLibAsync();
        var session = new FSStatSession();
        session.onUpdate((report) -> printReport(report));
        //session.cancel();
        var report = lib.getFSReport("test-data", 100, 4, session).await();
        printReport(report);
    }

    private static void printReport(FSReport report){
        System.out.println("Report printing begin ----------------------");
        for(var e:report.bandsCount()){
            System.out.println(e);
        }
        System.out.println("Report printing end ----------------------");
    }
}
