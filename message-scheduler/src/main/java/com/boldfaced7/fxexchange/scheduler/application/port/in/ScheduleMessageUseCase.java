package com.boldfaced7.fxexchange.scheduler.application.port.in;

public interface ScheduleMessageUseCase {
    void scheduleMessage(ScheduleMessageCommand command);
}
