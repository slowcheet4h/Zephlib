package cc.slowcheet4h.zephlib.threading.impl;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;
import cc.slowcheet4h.zephlib.threading.worker.ZFuture;
import cc.slowcheet4h.zephlib.threading.worker.ZTask;
import cc.slowcheet4h.zephlib.threading.worker.ZTaskWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ZThreadPool {

    /* settings */
    protected long TASKWORKER_CHECK_INTERVAL = 3;
    protected long POOL_CONTROL_DELAY = 3;
    protected int minWorkers = 2, maxWorkers = 4;
    protected int tasksPerWorker = 20;
    protected boolean useControlThread = true;
    protected Thread controlThread;
    protected boolean runnning;

    protected List<ZTaskWorker> taskWorkers = new ArrayList<>();
    @ZEPHLIB_ONLY
    protected ZThreadPool() { }
    protected

    protected void setup() {
        for (int i = 0; i < minWorkers; i++) {
            ZTaskWorker worker = new ZTaskWorker(this);
            taskWorkers.add(worker);
            worker.start();
        }

        runnning = true;
        if (useControlThread) {
            controlThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            });
        }
    }

    public void loop() {
        while (runnning) {

            try {
                Thread.sleep(POOL_CONTROL_DELAY);
            } catch (Exception ex) {

            }

        }
    }


    public static ZThreadPool create() {
        return new ZThreadPool();
    }

    public long TASKWORKER_CHECK_INTERVAL() {
        return TASKWORKER_CHECK_INTERVAL;
    }

    public static ZThreadPool build(Consumer<ZThreadPool>... runs) {
        ZThreadPool pool = new ZThreadPool();

        for (int i = 0; i < runs.length; i++) {
            runs[i].accept(pool);
        }

        pool.setup();

        return pool;
    }

    public <X> ZTask<X> task(Supplier<X> task) {
        return new ZTask(this, task);
    }

    public <X> ZFuture<X> queue(ZTask<X> task) {
        ZFuture<X> future = ZFuture.create(task);
        task.future(future);

        // queue to any of workers
        availableWorker().queue(task);

        return future;
    }

    public <X> X await(ZTask<X> task) {
        ZFuture<X> future = ZFuture.create(task);
        task.future(future);

        // queue to any of workers
        availableWorker().queue(task);

        while (!future.completed()) {
            try {
                Thread.sleep(TASKWORKER_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        return future.value();
    }

    public int queueSize() {
        return taskWorkers.size();
    }

    public ZTaskWorker availableWorker() {
        ZTaskWorker result = null;
        int workCount = Integer.MAX_VALUE;
        for (int i = 0; i < taskWorkers.size(); i++) {
            final ZTaskWorker worker = taskWorkers.get(i);
            if (worker.taskCount() <= workCount) {
                workCount = worker.taskCount();
                result = worker;
            }
        }

        return result;
    }

    /* allows timed tasks */
    public ZThreadPool controlThread(boolean _controlThread) {
        useControlThread = _controlThread;
        return this;
    }

    public ZThreadPool minWorkers(int _minWorkers) {
        minWorkers = _minWorkers;
        return this;
    }

    public ZThreadPool maxWorkers(int _maxWorkers) {
        maxWorkers = _maxWorkers;
        return this;
    }
}
