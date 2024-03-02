import cc.slowcheet4h.zephlib.etc.utils.Stopwatch;
import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;
import cc.slowcheet4h.zephlib.threading.worker.ZPromise;

import java.time.Duration;

public class Test {

    public static ZPromise promise(ZPromise promise) {
        return promise;
    }

    public static void main(String[] args) {
        ZThreadPool threadPool = ZThreadPool.build(
                pool -> pool.controlThread(true)
        );

        threadPool.promise(() -> {
            System.out.println("this will run every 1 sec");
        },
            Duration.ofSeconds(1) /* first delay (optional) */,
            Duration.ofSeconds(1) /* loop interval */
        );

        threadPool.task(() -> {
            System.out.println("THIS IS RUNNING ASYNC");
            return true;
        }).queue();

        String result = threadPool.task(() -> {
            /* .... stuff */
            return "test string";
        }).await();

        threadPool.ptask(() -> {
            System.out.println("This should run after 2000 ms");
            return true;
        }, 2000).await();

        threadPool.ptask(() -> {
            System.out.println("This should run after 1000 ms");
            return true;
        }, Duration.ofSeconds(1)).queue();

        while (true) {
            try {

                Thread.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
