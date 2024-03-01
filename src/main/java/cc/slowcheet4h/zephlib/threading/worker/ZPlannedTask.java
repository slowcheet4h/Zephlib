package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.util.function.Supplier;

public class ZPlannedTask<X> extends ZTask<X> {

    private long plannedTime;
    public ZPlannedTask(ZThreadPool _pool, Supplier<X> _exec, long _plannedTime) {
        super(_pool, _exec);
        plannedTime = _plannedTime;
    }

    public long plannedTime() {
        return plannedTime;
    }

    @Override
    public ZFuture<X> queue() {
        return pool.plan();
    }
}
