package cc.slowcheet4h.zephlib.threading.worker;

import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.util.ArrayDeque;
import java.util.Queue;

public class ZTaskWorker extends Thread {

    /* make this interface? */
    protected ZThreadPool owner;
    protected Stopwatch timer = new Stopwatch();

    /* list of tasks that needs to do */
    protected Queue<ZTask<?>> tasks = new ArrayDeque<>();

    public ZTaskWorker(ZThreadPool _owner) {
        owner = _owner;
    }

    @Override
    public void run() {
        while (true) {

            if (!tasks.isEmpty()) {
                final ZTask task = tasks.poll();
                timer.start();
                final Object result = task.exec.get();
                long took = timer.stop();
                task.future.complete(result);
                task.end(took);
            }

            try {
                Thread.sleep(owner.TASKWORKER_CHECK_INTERVAL());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
       // super.run();
    }

    public int taskCount() {
        return tasks.size();
    }

    public void queue(ZTask<?> task) {
        tasks.add(task);
    }

}
