package com.boldfaced7.fxexchange.scheduler.application.port.out;

import com.boldfaced7.fxexchange.scheduler.domain.model.ScheduledMessage;

import java.util.Set;

public interface LoadScheduledMessagePort {
    Set<ScheduledMessage> loadDueMessages(long currentTimeMillis);
}
