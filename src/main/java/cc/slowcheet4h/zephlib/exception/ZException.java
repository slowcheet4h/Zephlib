package cc.slowcheet4h.zephlib.exception;

/* todo: maybe warning level? */
public interface ZException {

    String message();
    void print();
    long time();
    Object from();
    StackTraceElement[] stackTrace();

}
