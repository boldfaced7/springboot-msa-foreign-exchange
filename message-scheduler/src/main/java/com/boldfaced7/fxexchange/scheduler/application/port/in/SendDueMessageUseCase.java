package com.boldfaced7.fxexchange.scheduler.application.port.in;

public interface SendDueMessageUseCase {
    void sendDueScheduledMessages(SendDueMessageCommand command);
}
