import cc.slowcheet4h.zephlib.event.ZEventBus;

public class Test {
    public static void main(String[] args) {
        testEventApi();
    }

    public static void testEventApi() {
        ZEventBus eventBus = ZEventBus.create();

        eventBus.listen((e) ->{
            System.out.println(e.toString());
        }, Integer.class, Double.class)
                .filter(e -> e.doubleValue() % 2 == 0 && e instanceof Integer);

        for (int i = 1000; i > 0; i--) {
            eventBus.fire(i);
            eventBus.fire(2d);
        }

    }
}
