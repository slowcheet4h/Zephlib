package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ZTaskWorker extends Thread {

    /* make this interface? */
    protected ZThreadPool owner;
    protected Stopwatch timer = new Stopwatch();
    protected List<Long> lastTimeStamps = new ArrayList<>();
    protected Stopwatch tpmTimer = new Stopwatch();
    protected boolean running;
    protected boolean busy;
    private Queue<ZTask<?>> stackTasks = new ArrayDeque<>();


    public ZTaskWorker(ZThreadPool _owner) {
        owner = _owner;
    }

    @Override
    public void run() {
        owner.threadMap().put(Thread.currentThread(), this);
        tpmTimer.start();
        running = true;
        while (running) {
            final long currentTime = System.currentTimeMillis();
            final ZTask task = nextTask();
            if (task != null) {
                executeTask(task);
            }

            try {
                Thread.sleep(owner.TASKWORKER_CHECK_INTERVAL());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
       // super.run();
    }

    private void executeTask(ZTask task) {
        final long currentTime = System.currentTimeMillis();

        timer.start();
        busy(true);
        lastTimeStamps.add(currentTime);
        final Object result = task.exec.get();
        long took = timer.stop();
        task.future.complete(result);
        task.end(took);
        busy(false);

        if (tpmTimer.elapsed() > 1000 && !lastTimeStamps.isEmpty()) {
            /* removes timestamps older than 1 minute */
            lastTimeStamps.removeIf((t) -> currentTime - t > 60000);

            /* resets the check delay */
            tpmTimer.start();
        }

    }

    public void doStackTasks() {
        ZTask task;
        while ((task = stackTasks.poll()) != null) {
            executeTask(task);
        }
    }

    private ZTask<?> nextTask() {
        return owner.poolTask();
    }

    public boolean busy() {
        return busy;
    }

    public ZTaskWorker busy(boolean _busy) {
        this.busy = _busy;
        return this;
    }

    /* how many tasks get executed in last 1 minute */
    public int tasksPerMinute() {
        return lastTimeStamps.size();
    }

    public Queue<ZTask<?>> stackTasks() {
        return stackTasks;
    }

    public ZTaskWorker stopWorking() {
        running = false;
        return this;
    }
}
