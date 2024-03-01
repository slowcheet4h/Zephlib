import cc.slowcheet4h.zephlib.threading.impl.ZThreadPool;

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


        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
