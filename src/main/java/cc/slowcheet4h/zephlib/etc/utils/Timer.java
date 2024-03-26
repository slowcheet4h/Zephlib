package cc.slowcheet4h.zephlib.etc.utils;

public class Timer {

    private long miliseconds;

    public Timer() {
        reset();
    }

    public boolean isElapsed(long elapsed) {
        return System.currentTimeMillis() - miliseconds >= elapsed;
    }

    public boolean isElapsedR(long elapsed) {
        final boolean result = isElapsed(elapsed);
        reset();
        return result;
    }

    public void reset() {
        miliseconds = System.currentTimeMillis();
    }

}
