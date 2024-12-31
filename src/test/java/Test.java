import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.event.ZEventBus;
import cc.slowcheet4h.zephlib.event.ZEventListener;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;
import cc.slowcheet4h.zephlib.threading.worker.ZFuture;
import cc.slowcheet4h.zephlib.threading.worker.ZTask;

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



        final int result = threadPool.task(() -> {
            return 1;
        }).await();

        final ZFuture<Integer> anan = threadPool.task(() -> {


            return 15;
        }).queue();




        System.out.println("DONE");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
