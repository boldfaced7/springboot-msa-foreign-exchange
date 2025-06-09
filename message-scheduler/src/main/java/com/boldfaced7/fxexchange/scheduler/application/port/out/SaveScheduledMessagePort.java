package com.boldfaced7.fxexchange.scheduler.application.port.out;

import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;

public interface SaveScheduledMessagePort {
    void saveScheduledMessage(ScheduledMessage message);
}
