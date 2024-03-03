package cc.slowcheet4h.zephlib.exception;

import cc.slowcheet4h.zephlib.etc.marker.ZEPHLIB_ONLY;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ZEPHLIB_ONLY
public class ZExceptionHandler {

    protected Runnable execute;
    protected Consumer<ZException> fail;
    protected Consumer<ZException> finish;
    protected ZException exception;

    @ZEPHLIB_ONLY
    @Deprecated
    public ZExceptionHandler() {
        execute = null;
        exception = null;
    }

    public ZExceptionHandler newScope(Runnable _execute) {
        execute = _execute;
        fail   = (e) -> {};
        finish = (e) -> {};
        return this;
    }

    public ZExceptionHandler endScope() {
        execute = () -> {};
        fail = (e) -> {};
        finish = (e) -> {};
        return this;
    }

    public ZExceptionHandler execute() {
        execute.run();

        if (exception != null) {
            fail.accept(exception);
        }

        if (finish != null) {
            finish.accept(exception);
        }

        endScope();
        return this;
    }

    public ZExceptionHandler fail(Consumer<ZException> _fail) {
        fail = _fail;
        return this;
    }

    public ZExceptionHandler finish(Consumer<ZException> _finish) {
        finish = _finish;
        return this;
    }
}
