package cc.slowcheet4h.zephlib.networking.etc;

import cc.slowcheet4h.zephlib.Zephlib;
import cc.slowcheet4h.zephlib.etc.properties.cprop;
import cc.slowcheet4h.zephlib.etc.properties.prop;

import java.net.InetAddress;

public class IPAddress {
    public static final IPAddress WILDCARD = new IPAddress("0.0.0.0");
    public static final IPAddress LOCALHOST = new IPAddress("127.0.0.1");
    private String address;

    public final prop<InetAddress> hostAddress = new prop<>() {
        @Override
        public InetAddress get() {
            try {
                return InetAddress.getByName(address);
            } catch (Exception EX) {
                Zephlib.printEx(EX);
            }
            return null;
        }
    };

    public IPAddress(final String _address) {
        address = _address;
    }
}
