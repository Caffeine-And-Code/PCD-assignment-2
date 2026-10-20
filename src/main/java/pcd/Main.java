package pcd;

import pcd.gui.FsStatApp;

public final class Main {
    private Main() {
    }

    public static void main(final String[] args) {
        FsStatApp.launchGui(view -> {
            view.setOnStart((dir, maxFS, nb) -> { /* avvia getFSReport */ });
            view.setOnStop(() -> { /* annulla */ });
        }, args);
    }
}
