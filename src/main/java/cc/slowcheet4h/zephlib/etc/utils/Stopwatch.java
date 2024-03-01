package cc.slowcheet4h.zephlib.etc.utils;

/**
 * C# like Stopwatch
*/
public class Stopwatch {
    protected long lastMS;
    protected long elapsed;
    protected boolean running;

    public Stopwatch() {}

    /**
     * starts the timer
     * @return instance
     */
    public long start() {
        lastMS = System.currentTimeMillis();
        running = true;
        return System.currentTimeMillis();
    }

    /**
     * stops the timer
     * @return instance
     */
    public long stop() {
        elapsed = System.currentTimeMillis() - lastMS;
        running = false;
        return elapsed();
    }

    /**
     * is stopwatch running
     * @return instance
     */
    public boolean running() {
        return running;
    }


    /**
     * if running gets elapsed time from start
     * if not gets last timer value
     * @return elapsed time
     */
    public long elapsed() {
        return running ? System.currentTimeMillis() - lastMS : elapsed;
    }
}
