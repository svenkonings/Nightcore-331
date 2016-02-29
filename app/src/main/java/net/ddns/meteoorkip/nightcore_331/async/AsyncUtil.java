package net.ddns.meteoorkip.nightcore_331.async;

import java.io.Closeable;
import java.io.IOException;

public class AsyncUtil {
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
