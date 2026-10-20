package pcd.asyncprogramming;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.util.ArrayList;
import java.util.List;


public class FSStatLibAsync implements FSStatLib {

    private final FileSystem fs;

    public FSStatLibAsync(){
        Vertx vertx = Vertx.vertx();
        fs = vertx.fileSystem();
    }

    @Override
    public FSReport getFSReport(String directory, long maxFileSize, int numberOfBands) {
        return scanDirectory(directory, maxFileSize, numberOfBands).await();
    }

    private Future<FSReport> scanDirectory(
            String directory,
            long maxFileSize,
            int numberOfBands) {

        return fs.readDir(directory)
            .compose(entries -> {

                List<Future<FSReport>> tasks = new ArrayList<>();

                for (String entry : entries) {

                    Future<FSReport> task = fs.lprops(entry)
                        .compose(props -> {

                            if (props.isDirectory()) {
                                return scanDirectory(
                                        entry,
                                        maxFileSize,
                                        numberOfBands
                                );
                            }

                            if (props.isRegularFile()) {
                                FSReport report = FSReport.Empty(numberOfBands);

                                return Future.succeededFuture(
                                        report.addSize(
                                                props.size(),
                                                maxFileSize,
                                                numberOfBands
                                        )
                                );
                            }

                            return Future.succeededFuture(
                                    FSReport.Empty(numberOfBands)
                            );
                        });

                    tasks.add(task);
                }

                return Future.all(tasks)
                    .map(composite -> {

                        FSReport result = FSReport.Empty(numberOfBands);

                        for (int i = 0; i < composite.size(); i++) {
                            FSReport childReport =
                                    composite.resultAt(i);

                            result = result.merge(childReport);
                        }

                        return result;
                    });
            });
    }
}
