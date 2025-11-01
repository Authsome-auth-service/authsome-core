package dev.kuku.authsome.util;

import java.time.Instant;

public class Util {
    public static long NowUTCMilli() {
        return Instant.now().toEpochMilli();
    }
}
