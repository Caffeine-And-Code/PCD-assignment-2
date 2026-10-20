package pcd;

import pcd.asyncprogramming.FSStatLibAsync;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) {
        FSStatLibAsync lib = new FSStatLibAsync();
        var res = lib.getFSReport("test-data", 100, 4);
        for(var e:res.bandsCount()){
            System.out.println(e);
        }

    }
}
