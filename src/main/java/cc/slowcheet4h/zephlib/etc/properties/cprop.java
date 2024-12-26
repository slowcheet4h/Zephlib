package cc.slowcheet4h.zephlib.etc.properties;

import java.util.function.Consumer;
import java.util.function.Supplier;

/* cached prop */
public class cprop<X> {

    private X value;
    private boolean calculated;
    private Supplier<X> calculate;
    public cprop(Supplier<X> _calculate) {
        calculate = _calculate;
    }

    public X value() {
        if (!calculated) {
            value = calculate.get();
        }
        return value;
    }

}
