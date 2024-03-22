package cc.slowcheet4h.zephlib.event;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ZEventListener<X> {

    protected Predicate<X> filter;
    protected Priority priority;
    protected ZEventBus bus;
    protected Consumer<X> body;
    protected Class<? extends X>[] events;
    protected boolean paused;

    @Deprecated(forRemoval = false)
    @ZEPHLIB_ONLY
    protected ZEventListener(ZEventBus _bus, Priority _priority, Consumer<X> _body, Class<? extends X>... _events) {
        bus = _bus;
        priority = _priority;
        body = _body;
        events = _events;
    }

    @Deprecated(forRemoval = false)
    @ZEPHLIB_ONLY
    public static ZEventListener<?> create(ZEventBus bus, Priority priority, Consumer<?> _body, Class<?>... events) {
        return new ZEventListener(bus, priority, _body, events);
    }

    public void invoke(Object event) {
        if (!paused && (filter == null || filter.test((X) event))) {
            body.accept((X) event);
        }
    }



    @SuppressWarnings("unchecked")
    public ZEventListener<X> filter(Predicate<X> _filter) {
        filter = _filter;
        return this;
    }

    public ZEventListener<X> priority(Priority _priority) {
        this.priority = _priority;
        /* sort the list again */
        return this;
    }

    public Class<? extends X>[] events() {
        return events;
    }

    public ZEventListener<X> pause() {
        paused = true;
        return this;
    }

    public ZEventListener<X> resume() {
        paused = false;
        return this;
    }

    public void stop() {
        bus.stop(this, events);
    }

    public void stop(Class<? extends X> events) {
        bus.stop(this, events);
    }

    public boolean paused() {
        return paused;
    }

    public static enum Priority {
        MASTER(10000),
        HIGHT(8000),
        MEDIUM(3000),
        LOW(2000),
        LOWEST(1000),
        MONITOR(0);

        final int weight;

        Priority(int _weight) {
            weight = _weight;
        }

        public int weight() {
            return weight;
        }
    }
}
