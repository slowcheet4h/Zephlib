import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

import java.time.Duration;

public class Test {

    public static void main(String[] args) {
        ZThreadPool threadPool = ZThreadPool.build();

        threadPool.task(() -> {
            System.out.println("THIS HAS RUN ASYNC");
            return true;
        }).queue();

        String result = threadPool.task(() -> {
            /* .... stuff */
            return "test string";
        }).await();

        threadPool.ptask(() -> {
            System.out.println("This should run after 2000 ms");
            return true;
        }, 2000).queue();

        threadPool.ptask(() -> {
            System.out.println("This should run after 1000 ms");
            return true;
        }, Duration.ofSeconds(1)).queue();



        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
