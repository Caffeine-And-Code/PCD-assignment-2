package pcd.reactiveprogramming;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;

public final class FSStatSession {
    private final FlowableProcessor<Boolean> cancellation =
            BehaviorProcessor.createDefault(false).toSerialized();

    public void cancel() {
        cancellation.onNext(true);
    }

    Flowable<Boolean> cancellationSignal() {
        return cancellation
                .filter(Boolean::booleanValue)
                .take(1);
    }
}
