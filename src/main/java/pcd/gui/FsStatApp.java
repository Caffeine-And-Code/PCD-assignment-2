package pcd.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class FsStatApp extends Application {

    private static Consumer<FsStatView> onReady = v -> {};

    /**
     * Launches the GUI. The callback receives the view (on the JavaFX thread)
     * as soon as it is created, so you can register handlers on it.
     * Blocks until the application exits (like Application.launch).
     */
    public static void launchGui(Consumer<FsStatView> viewReady, String... args) {
        onReady = viewReady;
        Application.launch(FsStatApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        FsStatView view = new FsStatView();
        onReady.accept(view);

        stage.setTitle("FSStat");
        stage.setScene(new Scene(view.getRoot()));
        stage.show();
    }
}

