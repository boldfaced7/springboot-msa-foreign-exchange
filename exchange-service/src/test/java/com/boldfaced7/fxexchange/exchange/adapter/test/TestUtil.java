package com.boldfaced7.fxexchange.exchange.adapter.test;

import java.util.function.Supplier;

public class TestUtil {

    public static <T> T doWithDelay(Supplier<T> supplier) {
        try {
            Thread.sleep(1000);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void doAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void doAsyncWithDelay(Runnable runnable) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                runnable.run();
            } catch (InterruptedException ignored) {}
        }).start();
    }

}
