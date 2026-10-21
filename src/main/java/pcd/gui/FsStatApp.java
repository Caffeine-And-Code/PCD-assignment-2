package pcd.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pcd.asyncprogramming.FSStatLibAsync;
import pcd.gui.enums.LibVersion;
import pcd.gui.specificviewmodels.FsStatAsyncProgrammingViewModel;
import pcd.gui.specificviewmodels.FsStatReactiveProgrammingViewModel;
import pcd.gui.specificviewmodels.FsStatVirtualThreadsViewModel;
import pcd.reactiveprogramming.FSStatLibReactive;
import pcd.virtualthread.FSStatLibVirtualThread;

public final class FsStatApp extends Application {

    private static final LibVersion DEFAULT_VERSION = LibVersion.AsyncProgrammingWithEventLoop;

    private Scene scene;
    private FsStatViewModel viewModel;

    @Override
    public void start(Stage stage) {
        viewModel = createViewModel(DEFAULT_VERSION);
        FsStatView view = new FsStatView(viewModel, DEFAULT_VERSION, this::switchViewModel);

        scene = new Scene(view.getRoot());
        stage.setTitle("FSStat");
        stage.setScene(scene);
        stage.show();
    }

    private void switchViewModel(LibVersion version) {
        FsStatViewModel newViewModel = createViewModel(version);
        newViewModel.directoryProperty().set(viewModel.directoryProperty().get());
        newViewModel.maxFsKbProperty().set(viewModel.maxFsKbProperty().get());
        newViewModel.numBandsProperty().set(viewModel.numBandsProperty().get());

        viewModel = newViewModel;
        scene.setRoot(new FsStatView(viewModel, version, this::switchViewModel).getRoot());
    }

    private static FsStatViewModel createViewModel(LibVersion version) {
        return switch (version) {
            case AsyncProgrammingWithEventLoop -> new FsStatAsyncProgrammingViewModel(new FSStatLibAsync());
            case ReactiveProgramming -> new FsStatReactiveProgrammingViewModel(new FSStatLibReactive());
            case VirtualThreads -> new FsStatVirtualThreadsViewModel(new FSStatLibVirtualThread());
        };
    }

    public static void launchGui(String... args) {
        Application.launch(FsStatApp.class, args);
    }
}
