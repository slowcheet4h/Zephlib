import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.event.ZEventBus;
import cc.slowcheet4h.zephlib.event.ZEventListener;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;
import cc.slowcheet4h.zephlib.threading.worker.ZFuture;

import java.util.Random;
import java.util.concurrent.Future;

public class Test {
    public static void main(String[] args) {
        testPool();
    }

    public static void testEventApi() {
        ZEventBus eventBus = ZEventBus.create();

        eventBus.listen((event) -> {

        }, String.class, Integer.class);

        eventBus.fire(":D");
    }

    public static void testPool() {
        System.out.println("TEST START");
        ZThreadPool threadPool = ZThreadPool.build();
        threadPool.task(() -> {
            System.out.println("TEST");

            threadPool.task(() -> {
                System.out.println("5");

                threadPool.task(() -> {
                    System.out.println("6");
                    threadPool.task(() -> {
                        System.out.println("7");
                        threadPool.task(() -> {
                            System.out.println("8");
                            threadPool.task(() -> {
                                System.out.println("9");



                                threadPool.task(() -> {
                                    System.out.println("10");
                                    return 6;
                                }).await();

                                threadPool.task(() -> {
                                    System.out.println("11");
                                    return 6;
                                }).await();

                                threadPool.task(() -> {

                                    threadPool.task(() -> {
                                        System.out.println("12");
                                        return 6;
                                    }).await();


                                    System.out.println("13");
                                    return 6;
                                }).await();


                                return 6;
                            }).await();
                            return 6;
                        }).await();
                        return 6;
                    }).await();
                    return 6;
                }).await();

                return 5;
            }).await();

            return 1;
        }).await();

        System.out.println("DONE");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
