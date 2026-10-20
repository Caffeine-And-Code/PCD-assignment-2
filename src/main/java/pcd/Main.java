package pcd;

import pcd.reactiveprogramming.FSStatLib;
import pcd.reactiveprogramming.FSStatLibReactive;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) {
        FSStatLib lib = new FSStatLibReactive();
        lib.getFSReport("test-data", 100, 4)
                .blockingSubscribe(
                        report -> report.bandsCount().forEach(System.out::println),
                        Throwable::printStackTrace
                );
    }
}
