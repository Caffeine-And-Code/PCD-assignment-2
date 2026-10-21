package pcd;

import pcd.gui.FsStatApp;
import pcd.virtualthread.FSStatLibVirtualThread;
import pcd.virtualthread.FSStatSession;

import java.util.concurrent.ExecutionException;

public final class Main {
    public static void main(final String[] args) {
        FsStatApp.launchGui(args);
    }
}
