package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pcd.FSReport;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.BaseStream;

public final class FSStatLibReactive implements FSStatLib {

    @Override
    public Flowable<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSStatSession session) {

        return Flowable.defer(() ->
                generateReports(directory, maxFileSize, numberOfBands, session)
        ).subscribeOn(Schedulers.io());
    }

    private Flowable<FSReport> generateReports(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSStatSession session) {

        validateParameters(directory, maxFileSize, numberOfBands, session);

        Path root = Path.of(directory);
        FSReport initialReport = FSReport.Empty(numberOfBands);

        return scanFileSizes(root)
            .scan(
                initialReport,
                (report, fileSize) -> report.addFile(
                        fileSize,
                        maxFileSize,
                        numberOfBands
                )
            )
            .takeUntil(session.cancellationSignal());
    }

    private Flowable<Long> scanFileSizes(Path directory) {
        return Flowable.using(
            () -> Files.walk(directory),
            paths -> Flowable.fromStream(paths)
                    .filter(this::isRegularFile)
                    .map(Files::size),
            BaseStream::close,
            true
        );
    }

    private boolean isRegularFile(Path path) {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    private void validateParameters(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSStatSession session) {

        if (directory == null || directory.isBlank()
                || maxFileSize <= 0
                || numberOfBands <= 0
                || numberOfBands == Integer.MAX_VALUE
                || session == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }
    }
}
