package pcd.asyncprogramming;

import io.vertx.core.Vertx;
import pcd.FSReport;

import java.util.Optional;
import java.util.function.Consumer;

public class FSStatSession {
    private boolean canceled;
    private Optional<Consumer<FSReport>> onUpdate;
    private Vertx vertx;

    public FSStatSession(){
        canceled = false;
        onUpdate = Optional.empty();
        vertx = Vertx.vertx();
    }

    public void cancel(){
        canceled = true;
    }

    public boolean isCanceled(){
        return canceled;
    }

    public void onUpdate(Consumer<FSReport> onUpdate){
        this.onUpdate = Optional.of(onUpdate);
    }

    public void update(FSReport report){
        onUpdate.ifPresent(consumer -> {
            vertx.<Void>executeBlocking(() -> {
                consumer.accept(report);
                return null;
            }).onFailure(err -> {
                System.err.println("Cannot execute onUpdate: " + err);
            });
        });
    }
}
