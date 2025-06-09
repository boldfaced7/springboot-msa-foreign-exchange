package com.boldfaced7.fxexchange.scheduler.application.port.in;

public record SendDueMessageCommand(
        long currentTimeMillis
) {

    public static SendDueMessageCommand current() {
        return new SendDueMessageCommand(System.currentTimeMillis());
    }
}
