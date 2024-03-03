package cc.slowcheet4h.zephlib.threading.impl;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;
import cc.slowcheet4h.zephlib.etc.properties.prop;
import cc.slowcheet4h.zephlib.threading.worker.*;

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
    protected Object taskWorkersLock = new Object();
    protected Map<PlannedTaskCategory, List<ZPlannedTask<?>>> plannedTaskMap = new HashMap<>();
    protected Map<PlannedTaskCategory, Object> plannedTaskLocks = new HashMap<>();
    protected long lastWorkerCheck = System.currentTimeMillis();
    protected long workerCheckInterval = 250L;
    protected int MAX_TASKS_PER_MINUTE = 60 * 9;
    @ZEPHLIB_ONLY
    protected ZThreadPool() { }

    protected void setup() {
        for (int i = 0; i < minWorkers; i++) {
            ZTaskWorker worker = new ZTaskWorker(this);
            taskWorkers.add(worker);
            worker.start();
        }
        asasaszasdasdasdz
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
                        plannedTasks.sort(Comparator.comparingLong(ZPlannedTask::plannedTime));

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
                    taskCategory.markAsChecked();
                }
            }

            if (currentTime - lastWorkerCheck >= workerCheckInterval) {

                int busyWorkers = 0;
                synchronized (taskWorkersLock) {
                    for (int i = 0; i < taskWorkers.size(); i++) {
                        final ZTaskWorker worker = taskWorkers.get(i);
                        int tasksPerMinute = worker.tasksPerMinute();
                        if (tasksPerMinute >= MAX_TASKS_PER_MINUTE) {
                            busyWorkers++;
                        }
                    }

                    if (taskWorkers.size() < maxWorkers && busyWorkers >= (taskWorkers.size() * 0.7f)) {
                        ZTaskWorker worker = new ZTaskWorker(this);
                        taskWorkers.add(worker);
                        worker.start();
                    } else if (taskWorkers.size() > minWorkers && busyWorkers < (taskWorkers.size() * 0.3f)) {
                        final ZTaskWorker worker = taskWorkers.get(0);
                        worker.stopWorking();
                        final Queue<ZTask<?>> tasks = worker.tasks();
                        while (!tasks.isEmpty()) {
                            _queue(tasks.poll());
                        }
                        taskWorkers.remove(worker);
                    }
                }

                lastWorkerCheck = currentTime;
            }


            try {
                Thread.sleep(POOL_CONTROL_DELAY);
            } catch (Exception ex) {
                ex.printStackTrace();
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
    public ZPromise promise(Runnable loop, long interval) {
        return promise(loop, 0, interval);
    }

    public ZPromise promise(Runnable loop, Duration duration) {
        return promise(loop, 0, duration.toMillis());
    }

    public ZPromise promise(Runnable loop, long startDelay, Duration duration) {
        return promise(loop, startDelay, duration.toMillis());
    }

    public ZPromise promise(Runnable loop, Duration startDelay, long interval) {
        return promise(loop, startDelay.toMillis(), interval);
    }

    public ZPromise promise(Runnable loop, Duration startDelay, Duration interval) {
        return promise(loop, startDelay.toMillis(), interval.toMillis());
    }


    public ZPromise promise(Runnable loop, long startDelay, long interval) {
        ZPromise promise = new ZPromise(loop, Math.max(startDelay, 0), interval);

        final prop<Runnable> exec = new prop();

        exec.set(() -> {
            boolean run = promise.run();
            if (run) {
                ptask(() -> { exec.get().run(); return true; }, interval).queue();
            }
        });

        if (startDelay <= 0) {
           task(() -> {
               exec.get().run();
               return true;
            }).queue();
        } else {
            ptask(() -> {
                exec.get().run();
                return true;
            }, startDelay).queue();
        }

        return promise;
    }

    public <X> ZFuture<X> queue(ZTask<X> task) {
        // queue to any of workers
        task.queue();

        return task.future();
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
            return _queue(task);
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
        // queue to any of workers
        availableWorker().queue(task);

        while (!task.future().completed()) {
            try {
                Thread.sleep(TASKWORKER_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        return task.future().value();
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
