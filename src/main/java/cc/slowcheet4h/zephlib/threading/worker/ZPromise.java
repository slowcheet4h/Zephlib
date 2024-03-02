package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;

public class ZPromise {
    protected Runnable loop;
    protected long startDelay, interval;
    protected boolean running = true;
    @ZEPHLIB_ONLY
    @Deprecated
    public ZPromise(Runnable _loop, long _startDelay, long _interval) {
        loop = _loop;
        running = true;
        startDelay = _startDelay;
        interval = _interval;
    }

    public boolean run() {
        if (running) {
            loop.run();
            return true;
        }

        return false;
    }
}
