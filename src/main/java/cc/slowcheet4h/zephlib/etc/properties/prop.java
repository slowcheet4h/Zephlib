package cc.slowcheet4h.zephlib.etc.properties;

public class prop<X> {

    protected X value;

    public prop(X _value) {
        value = _value;
    }

    public prop() {
        this(null);
    }

    public void set(X _value) {
        value = _value;
    }

    public X get() {
        return value;
    }

}
