import sun.security.jca.ProviderList;
import sun.security.jca.Providers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Hashtable;

public class Main {

    public static void main(String[] args) throws Throwable {
        Field f = URL.class.getDeclaredField("handlers");
        f.setAccessible(true);
        ((Hashtable<String, URLStreamHandler>) f.get(null))
                .put("mem", new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException {
                        return new URLConnection(u) {
                            @Override
                            public void connect() throws IOException {
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                return new InputStream() {
                                    @Override
                                    public int read() throws IOException {
                                        return 0;
                                    }
                                };
                            }
                        };
                    }
                });
        System.setProperty("java.security.egd", "mem:");
        ProviderList providerList = Providers.getProviderList();
        Provider[] providers = providerList.toArray();
        Provider[] result = Arrays.copyOf(providers, providers.length + 1);
        result[0] = new Provider("SHA", 1.0D, "1337") {
            {
                putService(new Service(this, "MessageDigest", "SHA", "SpoofedMessageDigestSpi", null, null));
                putService(new Service(this, "MessageDigest", "MD5", "SpoofedMessageDigestSpi", null, null));
            }
        };
        System.arraycopy(providers, 0, result, 1, providers.length);
        Providers.setProviderList(ProviderList.newList(result));

        SecureRandom random = new SecureRandom(); // so secure
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        System.out.println(Arrays.toString(bytes)); // prints [-103, 11, 55, 4, 102, -75, 38, 20] every time
        // may change on different machine
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(bytes);
        System.out.println(Arrays.toString(md.digest())); // [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        System.out.println(Arrays.toString(md.digest())); // [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
}
