package com.boldfaced7.fxexchange.scheduler.adapter.in.scheduling;

import com.boldfaced7.fxexchange.scheduler.application.port.in.SendDueMessageCommand;
import com.boldfaced7.fxexchange.scheduler.application.port.in.SendDueMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DueMessageSender {

    private final SendDueMessageUseCase sendDueMessageUseCase;

    @Scheduled(fixedRate = 1000)
    public void sendDueMessages() {
        var command = SendDueMessageCommand.current();
        sendDueMessageUseCase.sendDueScheduledMessages(command);
    }
}
