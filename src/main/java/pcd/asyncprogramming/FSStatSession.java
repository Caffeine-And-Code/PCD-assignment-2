package pcd.asyncprogramming;

import pcd.FSReport;

import java.util.Optional;
import java.util.function.Consumer;

public class FSStatSession {
    private boolean canceled;
    private Optional<Consumer<FSReport>> onUpdate;

    public FSStatSession(){
        canceled = false;
        onUpdate = Optional.empty();
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

    public Optional<Consumer<FSReport>> getOnUpdate(){
        return onUpdate;
    }
}
