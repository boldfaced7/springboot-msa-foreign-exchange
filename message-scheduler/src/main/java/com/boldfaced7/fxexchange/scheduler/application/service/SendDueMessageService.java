package com.boldfaced7.fxexchange.scheduler.application.service;

import com.boldfaced7.fxexchange.common.UseCase;
import com.boldfaced7.fxexchange.scheduler.application.port.in.SendDueMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.in.SendDueMessageUseCase;
import com.boldfaced7.fxexchange.scheduler.application.port.out.DeleteScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.LoadScheduledMessagePort;
import com.boldfaced7.fxexchange.scheduler.application.port.out.SendScheduledMessagePort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class SendDueMessageService implements SendDueMessageUseCase {

    private final LoadScheduledMessagePort loadScheduledMessagePort;
    private final SendScheduledMessagePort sendScheduledMessagePort;
    private final DeleteScheduledMessagePort deleteScheduledMessagePort;

    public void sendDueScheduledMessages(SendDueMessageCommand command) {
        var messages = loadScheduledMessagePort.loadDueMessages(command.currentTimeMillis());
        sendScheduledMessagePort.sendScheduledMessages(messages);
        deleteScheduledMessagePort.deleteAll(messages);
    }
}
