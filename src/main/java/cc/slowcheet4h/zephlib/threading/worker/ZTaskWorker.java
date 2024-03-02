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

    /* list of tasks that needs to do */
    protected Queue<ZTask<?>> tasks = new ArrayDeque<>();
    protected List<Long> lastTimeStamps = new ArrayList<>();
    protected Stopwatch tpmTimer = new Stopwatch();
    protected boolean running;

    public ZTaskWorker(ZThreadPool _owner) {
        owner = _owner;
    }

    @Override
    public void run() {
        tpmTimer.start();
        running = true;
        while (running) {

            final long currentTime = System.currentTimeMillis();
            if (!tasks.isEmpty()) {
                final ZTask task = tasks.poll();
                timer.start();
                lastTimeStamps.add(currentTime);
                final Object result = task.exec.get();
                long took = timer.stop();
                task.future.complete(result);
                task.end(took);
            }

            if (tpmTimer.elapsed() > 1000 && !lastTimeStamps.isEmpty()) {
                /* removes timestamps older than 1 minute */
                lastTimeStamps.removeIf((t) -> currentTime - t > 60000);
                /* resets the check delay */
                tpmTimer.start();
            }

            try {
                Thread.sleep(owner.TASKWORKER_CHECK_INTERVAL());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
       // super.run();
    }

    /* how many tasks get executed in last 1 minute */
    public int tasksPerMinute() {
        return lastTimeStamps.size();
    }

    public int taskCount() {
        return tasks.size();
    }

    public void queue(ZTask<?> task) {
        tasks.add(task);
    }

    public Queue<ZTask<?>> tasks() {
        return tasks;
    }

    public ZTaskWorker stopWorking() {
        running = false;
        return this;
    }
}
