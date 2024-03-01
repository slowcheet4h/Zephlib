package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.util.function.Supplier;

@ZEPHLIB_ONLY
public class ZTask<T> {

    protected Supplier<T> exec;
    private long took;
    private ZThreadPool pool;
    protected ZFuture<T> future;

    public ZTask(ZThreadPool _pool, Supplier<T> _exec) {
        pool = _pool;
        exec = _exec;
    }

    public ZFuture<T> queue() {
        return pool.queue(this);
    }

    public T await() {
        return pool.await(this);
    }

    public Supplier<T> exec() {
        return exec;
    }

    @Deprecated
    public void end(long _took) {
        took = _took;
    }

    @ZEPHLIB_ONLY
    @Deprecated
    public ZTask future(ZFuture<T> _future) {
        future = _future;
        return this;
    }

    public long end() {
        return took;
    }
}
