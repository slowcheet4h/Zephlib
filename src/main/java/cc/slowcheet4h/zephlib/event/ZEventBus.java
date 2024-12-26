package cc.slowcheet4h.zephlib.event;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class ZEventBus {

    protected Map<Class<?>, List<ZEventListener<?>>> LISTENERS = new HashMap<>();
    protected Map<Class<?>, Lock> LOCK_MAP = new HashMap<>();
    protected Map<Thread, ZEventListener<?>> INVOKE_MAP = new HashMap<>();
    protected ZEventBus() {}

    @SuppressWarnings("unchecked")
    public <X> ZEventListener<X> listen(Consumer<X> _listener, ZEventListener.Priority priority, Class<? extends X>... events) {
        return listen(new ZEventListener<>(this, priority, _listener, events));
    }

    @SuppressWarnings("unchecked")
    public <X> ZEventListener<X> listen(ZEventListener<X> _listener) {
        for (int i = 0; i < _listener.events().length; i++) {
            Lock lock = lock(_listener.events()[i]);

            List<ZEventListener<?>> listeners = get(_listener.events()[i]);
            listeners.add(_listener);

            /* sort the list */
            listeners.sort(new Comparator<ZEventListener<?>>() {
                @Override
                public int compare(ZEventListener<?> o1, ZEventListener<?> o2) {
                    return o2.priority.weight - o1.priority.weight;
                }
            });

            lock.unlock();
        }

        return _listener;
    }


    public <X> void sort(Class<? extends X> clazz) {
        Lock lock = lock(clazz);
        List<ZEventListener<?>> listeners = get(clazz);
        listeners.sort(new Comparator<ZEventListener<?>>() {
            @Override
            public int compare(ZEventListener<?> o1, ZEventListener<?> o2) {
                return o2.priority.weight - o1.priority.weight;
            }
        });

        lock.unlock();
    }

    public <X> ZEventListener<X> stopListening(ZEventListener<X> _listener) {
        return stopListening(_listener, _listener.events());
    }



    @SuppressWarnings("unchecked")
    public <X> ZEventListener<X> stopListening(ZEventListener<X> _listener, Class<? extends X>... events) {
        for (int i = 0; i < events.length; i++) {
            Lock lock = lock(events[i]);
            List<ZEventListener<?>> listeners = get(events[i]);
            listeners.remove(_listener);
            lock.unlock();
        }

        return _listener;
    }

    public <X> X fire(X event) {
        final Lock lock = lock(event.getClass());

        final List<ZEventListener<?>> listeners = get(event.getClass());
        for (int i = 0; i < listeners.size(); i++) {
            final ZEventListener<?> listener = listeners.get(i);
            //INVOKE_MAP.put(Thread.currentThread(), listener);
            listener.invoke(event);
            if (listener.isCanceled()) {
                break;
            }
        }

        INVOKE_MAP.remove(Thread.currentThread());
        lock.unlock();
        return event;
    }

    public void stop(Object o) {
        final Lock lock = lock(o.getClass());
        final ZEventListener<?> listener = INVOKE_MAP.get(Thread.currentThread());

        if (listener == null) {
            /* throw exception */
            lock.unlock();
            return;
        }

        listener.cancel();

        lock.unlock();
    }


    @SuppressWarnings("unchecked")
    public <X> ZEventListener<X> listen(Consumer<X> _listener, Class<? extends X>... events) {
        return listen(_listener, ZEventListener.Priority.MEDIUM, events);
    }

    public Lock lock(Class<?> clazz) {
        final Lock theLock = LOCK_MAP.computeIfAbsent(clazz, (e) -> new ReentrantLock());

        theLock.lock();
        return theLock;
    }

    public void unlock(Class<?> clazz) {
        /* todo: maybe just get will be faster */
        LOCK_MAP.computeIfPresent(clazz, (e, k) -> { k.unlock(); return k; });
    }

    public List<ZEventListener<?>> get(Class<?> clazz) {
        return LISTENERS.computeIfAbsent(clazz, (e) -> {
            return new ArrayList<>();}
        );
    }

    public static ZEventBus create() {
        return new ZEventBus();
    }
}
