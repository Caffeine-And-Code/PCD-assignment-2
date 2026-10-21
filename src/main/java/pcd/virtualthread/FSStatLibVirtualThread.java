package pcd.virtualthread;

import pcd.FSReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FSStatLibVirtualThread implements FSStatLib {

    private final ExecutorService executor;

    public FSStatLibVirtualThread() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public Future<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSStatSession session
    ) {
        return executor.submit(() -> {
            var state = new FSReportMonitor(numberOfBands);

            scanDirectory(
                    directory,
                    maxFileSize,
                    numberOfBands,
                    state,
                    session
            );

            return state.getReport();
        });
    }

    private void scanDirectory(
            String dirName,
            long maxFileSize,
            int numberOfBands,
            FSReportMonitor state,
            FSStatSession session
    ) throws IOException, ExecutionException, InterruptedException {

        var directory = Path.of(dirName);

        if (!Files.isDirectory(directory)) {
            return;
        }

        List<Future<Void>> tasks = new ArrayList<>();

        try {
            try (var directoryStream = Files.newDirectoryStream(directory)) {

                for (Path entry : directoryStream) {

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    if (Files.isDirectory(entry)) {

                        tasks.add(executor.submit(() -> {
                            scanDirectory(
                                    entry.toString(),
                                    maxFileSize,
                                    numberOfBands,
                                    state,
                                    session
                            );
                            return null;
                        }));

                    } else if (Files.isRegularFile(entry)) {

                        long size = Files.size(entry);

                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }

                        var currentReport = state.udpateReport(report -> report.addFile(
                                        size,
                                        maxFileSize,
                                        numberOfBands
                                )
                        );
                        executor.submit(() -> session.update(currentReport));
                    }
                }
            }

            for (Future<Void> task : tasks) {
                task.get();
            }

        } catch (Throwable e) {

            for (Future<Void> task : tasks) {
                task.cancel(true);
            }

            Thread.currentThread().interrupt();
            throw e;

        }
    }
}