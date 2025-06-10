package com.boldfaced7.fxexchange.scheduler.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.in.ScheduleMessageUseCase;
import com.boldfaced7.fxexchange.scheduler.application.port.out.SaveScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ScheduleMessageService implements ScheduleMessageUseCase {

    private final SaveScheduledMessagePort saveScheduledMessagePort;

    @Override
    public void scheduleMessage(ScheduleMessageCommand command) {
        var toBeSaved = new ScheduledMessage(
                command.originalTopic(),
                command.scheduledTimeMillis(),
                command.payload()
        );
        saveScheduledMessagePort.saveScheduledMessage(toBeSaved);
    }
}
