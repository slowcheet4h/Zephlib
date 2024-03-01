package cc.slowcheet4h.zephlib.threading.impl;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;
import cc.slowcheet4h.zephlib.threading.worker.ZFuture;
import cc.slowcheet4h.zephlib.threading.worker.ZPlannedTask;
import cc.slowcheet4h.zephlib.threading.worker.ZTask;
import cc.slowcheet4h.zephlib.threading.worker.ZTaskWorker;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
    protected Map<PlannedTaskCategory, List<ZPlannedTask<?>>> plannedTaskMap = new HashMap<>();
    protected Map<PlannedTaskCategory, Object> plannedTaskLocks = new HashMap<>();
    @ZEPHLIB_ONLY
    protected ZThreadPool() { }

    protected void setup() {
        for (int i = 0; i < minWorkers; i++) {
            ZTaskWorker worker = new ZTaskWorker(this);
            taskWorkers.add(worker);
            worker.start();
        }

        runnning = true;
        if (useControlThread) {

            /* initializes the lists and maps */
            for (PlannedTaskCategory category : PlannedTaskCategory.values()) {
                plannedTaskMap.put(category, new ArrayList<>());
                plannedTaskLocks.put(category, new Object());
            }

            /* creates the control thread */
            controlThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            });

            controlThread.start();
        }
    }




    public void loop() {
        while (runnning) {

            final long currentTime = System.currentTimeMillis();
            for (PlannedTaskCategory taskCategory : PlannedTaskCategory.values()) {
                /* should check? */
                if (currentTime - taskCategory.lastCheck() >= taskCategory.checkTime()) {

                    /* synchronize just to make sure it doesn't cause any errors */
                    synchronized (plannedTaskLocks.get(taskCategory)) {
                        /* sort and check here */
                        List<ZPlannedTask<?>> plannedTasks = plannedTaskMap.get(taskCategory);

                        /* sort the list */
                        plannedTasks.sort((pTask1, pTask2) -> {
                            return Long.compare(pTask1.plannedTime(), pTask2.plannedTime());
                        });

                        for (int i = 0; i < plannedTasks.size(); i++) {
                            final ZPlannedTask<?> task = plannedTasks.get(i);
                            if (task.plannedTime() <= currentTime) {
                                /* remove from plannedTasks list */
                                plannedTasks.remove(i);

                                /* queue it to workers */
                                queue(task);
                            }
                        }

                    }


                }
            }


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

    /**
     * creates a normal task
     * @param task
     * @return
     * @param <X>
     */
    public <X> ZTask<X> task(Supplier<X> task) {
        return new ZTask(this, task);
    }

    /**
     * creates a planned task with relative time
     * @param task
     * @param afterTime
     * @return
     * @param <X>
     */
    public <X> ZPlannedTask<X> ptask(Supplier<X> task, long afterTime) {
        if (!useControlThread) {
            /* todo: throw an custom exception */
            return null;
        }
        return new ZPlannedTask<>(this, task, System.currentTimeMillis() + afterTime);
    }

    /**
     * creates a planned task with relative time (in duration)
     * @param task
     * @param duration
     * @return
     * @param <X>
     */
    public <X> ZPlannedTask<X> ptask(Supplier<X> task, Duration duration) { return ptask(task, duration.toMillis()); }

    public <X> ZFuture<X> queue(ZTask<X> task) {
        ZFuture<X> future = ZFuture.create(task);
        task.future(future);

        // queue to any of workers
        task.queue();

        return future;
    }

    @Deprecated
    @ZEPHLIB_ONLY
    public <X> ZFuture<X> _queue(ZTask<X> task) {
        final ZTaskWorker worker = availableWorker();
        if (worker != null) {
            worker.queue(task);
        }
        return task.future();
    }


    @Deprecated
    @ZEPHLIB_ONLY
    public <X> ZFuture<X> _plan(ZPlannedTask<X> task) {
        final long currentTime = System.currentTimeMillis();
        final long deltaTime = task.plannedTime() - currentTime;
        if (deltaTime < 0) {
            return queue(task);
        }

        PlannedTaskCategory category = PlannedTaskCategory.fromTime(deltaTime);
        synchronized (plannedTaskLocks.get(category)) {
            plannedTaskMap.get(category).add(task);
        }

        return task.future();
    }


    /**
     * executes the task async and stops the thread
     * until task gets executed and returns the result
     * @param task
     * @return
     * @param <X>
     */
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

    /**
     * finds most available worker
     * @return
     */
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

    /**
     * if your worker will have control thread or not
     * control threads are required for planned tasks
     * @param _controlThread
     * @return
     */
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

    public enum PlannedTaskCategory {
        MS(0, 10 * 1000),
        SECONDS(90, 1000 * 30),
        MINUTE(100, 1000 * 60 * 5),

        @ZEPHLIB_ONLY
        @Deprecated
        MORE(5000, -1);


        static PlannedTaskCategory[] array = new PlannedTaskCategory[] { MS, SECONDS, MS, MORE };

        public static PlannedTaskCategory[] asArray() {
            return array;
        }

        public static PlannedTaskCategory fromTime(long time) {
            final long currentTime = System.currentTimeMillis();
            final long deltaTime = time - currentTime;

            for (PlannedTaskCategory category : PlannedTaskCategory.asArray()) {
                if (deltaTime <= category.maxTime())
                    return category;
            }

            return PlannedTaskCategory.MORE;
        }



        int checkTime;
        int maxTime;
        long lastCheck = -1;
        PlannedTaskCategory(int _checkTime, int _maxTime) {
            checkTime = _checkTime;
            maxTime = _maxTime;
        }



        public void markAsChecked() {
            lastCheck = System.currentTimeMillis();
        }




        public long lastCheck() {
            return lastCheck;
        }

        public int maxTime() {
            return maxTime;
        }

        public int checkTime() {
            return checkTime;
        }


    }
}
