package pcd.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pcd.asyncprogramming.FSStatLibAsync;

public final class FsStatApp extends Application {

    @Override
    public void start(Stage stage) {
        FsStatViewModel viewModel = new FsStatViewModel(new FSStatLibAsync());
        FsStatView view = new FsStatView(viewModel);

        stage.setTitle("FSStat");
        stage.setScene(new Scene(view.getRoot()));
        stage.show();
    }

    public static void launchGui(String... args) {
        Application.launch(FsStatApp.class, args);
    }
}
