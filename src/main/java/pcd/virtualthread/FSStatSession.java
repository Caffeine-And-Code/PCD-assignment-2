package pcd.virtualthread;

import pcd.FSReport;

import java.util.Optional;
import java.util.function.Consumer;

public class FSStatSession {
    private Optional<Consumer<FSReport>> onUpdate;

    public FSStatSession(){
        onUpdate = Optional.empty();
    }

    public void onUpdate(Consumer<FSReport> onUpdate){
        this.onUpdate = Optional.of(onUpdate);
    }

    public void update(FSReport report){
        onUpdate.ifPresent(consumer -> {
            consumer.accept(report);
        });
    }
}
