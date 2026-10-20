package pcd.gui;

import javafx.scene.Parent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import pcd.FSReport;
import pcd.gui.components.ViewLayout;

import java.io.File;

public final class FsStatView {

    private final FsStatViewModel viewModel;
    private final ViewLayout layout = new ViewLayout();

    public FsStatView(FsStatViewModel viewModel) {
        this.viewModel = viewModel;
        layout.build(this::chooseDirectory, viewModel::start, viewModel::stop);
        bindControls();

        viewModel.runningProperty().addListener((obs, wasRunning, isRunning) -> {
            if (isRunning) {
                layout.prepareChart(viewModel.maxFsKbProperty().get() * 1024L,
                        viewModel.numBandsProperty().get());
            }
        });

        viewModel.reportProperty().addListener((obs, oldReport, newReport) -> {
            if (newReport != null) {
                renderReport(newReport);
            }
        });
    }

    private void bindControls() {
        layout.getDirField().textProperty().bindBidirectional(
                viewModel.directoryProperty());

        layout.getMaxFsSpinner().getValueFactory().valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue != null) viewModel.maxFsKbProperty().set(newValue);
                });
        viewModel.maxFsKbProperty().addListener((obs, oldValue, newValue) ->
                layout.getMaxFsSpinner().getValueFactory().setValue(newValue.intValue()));

        layout.getNbSpinner().getValueFactory().valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue != null) viewModel.numBandsProperty().set(newValue);
                });
        viewModel.numBandsProperty().addListener((obs, oldValue, newValue) ->
                layout.getNbSpinner().getValueFactory().setValue(newValue.intValue()));

        layout.getStartButton().disableProperty().bind(viewModel.runningProperty());
        layout.getBrowseButton().disableProperty().bind(viewModel.runningProperty());
        layout.getMaxFsSpinner().disableProperty().bind(viewModel.runningProperty());
        layout.getNbSpinner().disableProperty().bind(viewModel.runningProperty());
        layout.getStopButton().disableProperty().bind(viewModel.runningProperty().not());
        layout.getStatusLabel().textProperty().bind(viewModel.statusProperty());
        layout.getTotalLabel().textProperty().bind(viewModel.totalFilesTextProperty());
        layout.getProgress().progressProperty().bind(viewModel.progressProperty());
    }

    private void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a directory");

        String currentPath = viewModel.directoryProperty().get();
        if (currentPath != null && !currentPath.isBlank()) {
            File previous = new File(currentPath);
            if (previous.isDirectory()) {
                chooser.setInitialDirectory(previous);
            }
        }

        Window owner = layout.getRoot().getScene() == null
                ? null
                : layout.getRoot().getScene().getWindow();
        File selected = chooser.showDialog(owner);
        if (selected != null) {
            viewModel.directoryProperty().set(selected.getAbsolutePath());
        }
    }

    private void renderReport(FSReport report) {
        var data = layout.getSeries().getData();
        int n = Math.min(data.size(), report.bandsCount().size());
        for (int i = 0; i < n; i++) {
            data.get(i).setYValue(report.bandsCount().get(i));
        }
    }

    public Parent getRoot() {
        return layout.getRoot();
    }

    public FsStatViewModel getViewModel() {
        return viewModel;
    }
}
