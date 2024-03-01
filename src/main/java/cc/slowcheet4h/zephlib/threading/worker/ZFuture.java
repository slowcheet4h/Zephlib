package cc.slowcheet4h.zephlib.threading.worker;

public class ZFuture<T> {

    protected boolean completed;
    protected ZTask<T> task;
    protected T value;

    protected ZFuture(ZTask<T> _task) {
        task = _task;
    }

    public void complete(T _value) {
        value = _value;
        completed = true;
    }

    public boolean completed() {
        return completed;
    }

    public T value() {
        return value;
    }

    public static <X> ZFuture<X> create(ZTask<X> _task) {
        return new ZFuture<X>(_task);
    }
}
