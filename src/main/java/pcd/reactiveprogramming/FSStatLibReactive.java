package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.BaseStream;

public final class FSStatLibReactive implements FSStatLib {

    @Override
    public Single<FSReport> getFSReport(
            String directory,
            long maxFileSize,
            int numberOfBands) {

        return Single.defer(() -> {
            validateParameters(maxFileSize, numberOfBands);

            return scanFileSizes(Path.of(directory))
                .reduce(
                    FSReport.empty(numberOfBands),
                    (report, fileSize) -> report.addSize(
                            fileSize,
                            maxFileSize,
                            numberOfBands
                    )
                );
        })
        .subscribeOn(Schedulers.io());
    }

    private Flowable<Long> scanFileSizes(Path directory) {
        return Flowable.using(
            () -> Files.walk(directory),
            paths -> Flowable.fromStream(paths)
                    .filter(this::isRegularFile)
                    .map(Files::size),
            BaseStream::close
        );
    }

    private boolean isRegularFile(Path path) {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    private void validateParameters(long maxFileSize, int numberOfBands) {
        if (
            maxFileSize <= 0 || numberOfBands <= 0 || numberOfBands == Integer.MAX_VALUE
        ) {
            throw new IllegalArgumentException("Invalid parameters");
        }
    }
}
