package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.util.function.Supplier;

@ZEPHLIB_ONLY
public class ZTask<T> {

    protected Supplier<T> exec;
    protected long took;
    protected ZThreadPool pool;
    protected ZFuture<T> future;

    public ZTask(ZThreadPool _pool, Supplier<T> _exec) {
        pool = _pool;
        exec = _exec;
        future = ZFuture.create(this);
    }

    public ZFuture<T> queue() {
        return pool._queue(this);
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

    @ZEPHLIB_ONLY
    @Deprecated
    public ZFuture<T> future() {
        return future;
    }

    public long end() {
        return took;
    }
}
