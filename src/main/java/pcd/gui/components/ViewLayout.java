package pcd.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class ViewLayout {
    private final BorderPane root = new BorderPane();

    private final TextField dirField = new TextField();
    private final Button browseButton = new Button("Browse…");
    private final Spinner<Integer> maxFsSpinner = new Spinner<>(1, 10_000_000, 1024, 128);
    private final Spinner<Integer> nbSpinner = new Spinner<>(1, 100, 10, 1);
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");

    private final Label totalLabel = new Label("Total files: –");
    private final Label statusLabel = new Label("Idle");
    private final ProgressBar progress = new ProgressBar(0);

    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    public void build(Runnable chooseDirectory, Runnable onStart, Runnable onStop) {
        dirField.setPromptText("Select a directory…");
        dirField.setEditable(false);
        HBox.setHgrow(dirField, Priority.ALWAYS);
        browseButton.setOnAction(event -> chooseDirectory.run());

        maxFsSpinner.setEditable(true);
        maxFsSpinner.setPrefWidth(120);
        nbSpinner.setEditable(true);
        nbSpinner.setPrefWidth(80);

        startButton.setDefaultButton(true);
        startButton.setOnAction(event -> {
            maxFsSpinner.increment(0);
            nbSpinner.increment(0);
            onStart.run();
        });
        stopButton.setOnAction(event -> onStop.run());

        HBox directoryRow = new HBox(8, new Label("Directory:"), dirField, browseButton);
        directoryRow.setAlignment(Pos.CENTER_LEFT);

        HBox parameterRow = new HBox(8,
                new Label("MaxFS (KB):"), maxFsSpinner,
                new Label("Bands (NB):"), nbSpinner,
                startButton, stopButton);
        parameterRow.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(8, directoryRow, parameterRow);
        top.setPadding(new Insets(12));

        xAxis.setLabel("File size band");
        yAxis.setLabel("Number of files");
        yAxis.setMinorTickVisible(false);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCategoryGap(4);
        chart.getData().add(series);

        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        progress.setPrefWidth(160);
        HBox bottom = new HBox(16, totalLabel, statusLabel, progress);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8, 12, 12, 12));

        root.setTop(top);
        root.setCenter(chart);
        root.setBottom(bottom);
        root.setPrefSize(900, 600);
    }

    public void prepareChart(long maxFS, int numBands) {
        series.getData().clear();
        long width = Math.max(1, maxFS / numBands);
        for (int i = 0; i < numBands; i++) {
            long low = i * width;
            long high = (i == numBands - 1) ? maxFS : (i + 1L) * width;
            series.getData().add(new XYChart.Data<>(human(low) + "–" + human(high), 0));
        }
        series.getData().add(new XYChart.Data<>("> " + human(maxFS), 0));
    }

    private static String human(long bytes) {
        if (bytes < 1024) return bytes + "B";
        double kiloBytes = bytes / 1024.0;
        if (kiloBytes < 1024) return trim(kiloBytes) + "K";
        double megaBytes = kiloBytes / 1024;
        if (megaBytes < 1024) return trim(megaBytes) + "M";
        return trim(megaBytes / 1024) + "G";
    }

    private static String trim(double value) {
        return value == Math.floor(value)
                ? String.valueOf((long) value)
                : String.format("%.1f", value);
    }

    public Parent getRoot() { return root; }
    public TextField getDirField() { return dirField; }
    public Button getBrowseButton() { return browseButton; }
    public Spinner<Integer> getMaxFsSpinner() { return maxFsSpinner; }
    public Spinner<Integer> getNbSpinner() { return nbSpinner; }
    public Button getStartButton() { return startButton; }
    public Button getStopButton() { return stopButton; }
    public Label getTotalLabel() { return totalLabel; }
    public Label getStatusLabel() { return statusLabel; }
    public ProgressBar getProgress() { return progress; }
    public XYChart.Series<String, Number> getSeries() { return series; }
}
