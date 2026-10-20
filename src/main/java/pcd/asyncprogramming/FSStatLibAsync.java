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

    private static class FSReportState {
        public FSReport report;

        public FSReportState(int numberOfBands){
            this.report = FSReport.Empty(numberOfBands);
        }
    }

    @Override
    public Future<FSReport> getFSReport(String directory, long maxFileSize, int numberOfBands, FSStatSession session) {
        var state = new FSReportState(numberOfBands);
        return scanDirectory(directory, maxFileSize, numberOfBands, state, session)
                .map(useless_my_sdk_not_support_underscore -> state.report);
    }

    private Future<Void> scanDirectory(
            String directory,
            long maxFileSize,
            int numberOfBands,
            FSReportState state,
            FSStatSession session) {

        return fs.readDir(directory)
            .compose(entries -> {

                List<Future<Void>> tasks = new ArrayList<>();

                for (String entry : entries) {

                    Future<Void> task = fs.lprops(entry)
                        .compose(props -> {

                            if(session.isCanceled()){
                                return Future.succeededFuture();
                            }

                            if (props.isDirectory()) {
                                return scanDirectory(
                                        entry,
                                        maxFileSize,
                                        numberOfBands,
                                        state,
                                        session
                                );
                            }

                            if (props.isRegularFile()) {
                                state.report = state.report.addFile(
                                        props.size(),
                                        maxFileSize,
                                        numberOfBands
                                );

                                session
                                        .getOnUpdate()
                                        .ifPresent(onUpdate -> onUpdate.accept(state.report));
                            }
                            return Future.succeededFuture();
                        });

                    tasks.add(task);
                }

                return Future.all(tasks).mapEmpty();
            });
    }
}
