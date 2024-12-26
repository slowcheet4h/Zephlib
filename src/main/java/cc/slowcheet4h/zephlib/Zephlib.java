package cc.slowcheet4h.zephlib;

public class Zephlib {


    public static void printEx(Exception ex) {
        System.err.println(ex.getMessage() + " " + ex.getCause());
    }
}
