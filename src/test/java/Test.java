import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.event.ZEventBus;
import cc.slowcheet4h.zephlib.event.ZEventListener;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;
import cc.slowcheet4h.zephlib.threading.worker.ZFuture;

import java.util.Random;

public class Test {
    public static void main(String[] args) {
        testEventApi();
    }

    public static void testEventApi() {

        ZThreadPool pool = ZThreadPool.build();

        ZEventBus eventBus = ZEventBus.create();
        eventBus.listen((e) -> {
            new Random().nextInt();
        }, ZEventListener.Priority.LOWEST, Integer.class);


        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (int i = 0; i < 1000000000; i++) {
            eventBus.fire(i);
        }

        long elapsed = stopwatch.stop();
        System.out.println(String.format("IT TOOK %s", elapsed));

    }
}
